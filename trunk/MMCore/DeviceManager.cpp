// DESCRIPTION:   Loading/unloading and bookkeeping of device instances
//
// COPYRIGHT:     University of California, San Francisco, 2006-2014
//
// LICENSE:       This file is distributed under the "Lesser GPL" (LGPL) license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Nenad Amodaj, nenad@amodaj.com, 08/10/2005

#include "DeviceManager.h"

#include "Devices/HubInstance.h"
#include "CoreUtils.h"
#include "Devices/DeviceInstance.h"
#include "Error.h"

namespace mm
{


DeviceManager::~DeviceManager()
{
   UnloadAllDevices();
}


boost::shared_ptr<DeviceInstance>
DeviceManager::LoadDevice(CMMCore* core, const char* label,
      const char* moduleName, const char* deviceName,
      boost::shared_ptr<mm::logging::Logger> deviceLogger,
      boost::shared_ptr<mm::logging::Logger> coreLogger)
{
   for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      if (it->first == label)
         throw CMMError("The specified label " + ToQuotedString(label) + " is already in use",
               MMERR_DuplicateLabel);
   }

   if (strlen(label) == 0)
      throw CMMError("Invalid label (empty string)", MMERR_InvalidLabel);

   boost::shared_ptr<LoadedDeviceAdapter> module =
      pluginManager_.GetDeviceAdapter(moduleName);

   boost::shared_ptr<DeviceInstance> device = module->LoadDevice(core,
         deviceName, label, deviceLogger, coreLogger);

   char descr[MM::MaxStrLength] = "N/A";
   module->GetDeviceDescription(deviceName, descr, MM::MaxStrLength);
   device->SetDescription(descr);

   devices_.push_back(std::make_pair(label, device));
   deviceRawPtrIndex_.insert(std::make_pair(device->GetRawPtr(), device));
   return device;
}


void
DeviceManager::UnloadDevice(boost::shared_ptr<DeviceInstance> device)
{
   if (device == 0)
      return;

   for (DeviceIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      if (it->second == device)
      {
         device->Shutdown(); // TODO Should be automatic
         deviceRawPtrIndex_.erase(it->second->GetRawPtr());
         devices_.erase(it);
         break;
      }
   }
}


void
DeviceManager::UnloadAllDevices()
{
   // do a two pass unloading so that USB ports and com ports unload last.
   // We plan unloading, and then carry it out, so as not to iterate
   // over a changing collection. Down with mutable collections.
   // XXX This ordering should be handled by strong references from device to
   // device. Also, peripehrals should explicitly be unloaded before hubs
   // instead of relying on the load order.

   std::vector< boost::shared_ptr<DeviceInstance> > nonSerialDevices;
   std::vector< boost::shared_ptr<DeviceInstance> > serialDevices;
   for (DeviceIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      if (it->second->GetType() == MM::SerialDevice)
      {
         serialDevices.push_back(it->second);
      }
      else
      {
         nonSerialDevices.push_back(it->second);
      }
   }

   // Call Shutdown before removing devices from index, so that the deivce's
   // Shutdown() has access (through the CoreCallback) to its own
   // DeviceInstance.
   // TODO We need a mechanism to ensure automatic Shutdown (1:1 with
   // Initialize()).
   for (std::vector< boost::shared_ptr<DeviceInstance> >::reverse_iterator
         it = nonSerialDevices.rbegin(), end = nonSerialDevices.rend();
         it != end; ++it)
   {
      (*it)->Shutdown();
   }
   for (std::vector< boost::shared_ptr<DeviceInstance> >::reverse_iterator
         it = serialDevices.rbegin(), end = serialDevices.rend();
         it != end; ++it)
   {
      (*it)->Shutdown();
   }

   deviceRawPtrIndex_.clear();
   devices_.clear();

   // Now the only remaining references to the device objects should be in
   // serialDevices and nonSerialDevices. Release the devices in order.
   while (nonSerialDevices.size() > 0)
   {
      nonSerialDevices.pop_back();
   }
   while (serialDevices.size() > 0)
   {
      serialDevices.pop_back();
   }
}


boost::shared_ptr<DeviceInstance>
DeviceManager::GetDevice(const std::string& label) const
{
   for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      if (it->first == label)
      {
         return it->second;
      }
   }

   std::string msg = "No device with label \"" + label + "\"";
   throw CMMError(msg.c_str(), MMERR_InvalidLabel);
}


boost::shared_ptr<DeviceInstance>
DeviceManager::GetDevice(const MM::Device* rawPtr) const
{
   typedef std::map< const MM::Device*, boost::weak_ptr<DeviceInstance> >::const_iterator Iterator;
   Iterator it = deviceRawPtrIndex_.find(rawPtr);
   if (it == deviceRawPtrIndex_.end())
      throw CMMError("Invalid device pointer");
   return it->second.lock();
}


std::vector<std::string>
DeviceManager::GetDeviceList(MM::DeviceType type) const
{
   std::vector<std::string> labels;
   for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      if (type == MM::AnyType || it->second->GetType() == type)
      {
         labels.push_back(it->first);
      }
   }
   return labels;
}


std::vector<std::string>
DeviceManager::GetLoadedPeripherals(const char* label) const
{
   std::vector<std::string> labels;

   // get hub
   boost::shared_ptr<DeviceInstance> pDev;
   try
   {
      pDev = GetDevice(label);
      if (pDev->GetType() != MM::HubDevice)
         return labels;
   }
   catch (...)
   {
      return labels;
   }

   for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
   {
      char parentID[MM::MaxStrLength];
      it->second->GetParentID(parentID);
      if (strncmp(label, parentID, MM::MaxStrLength) == 0)
      {
         labels.push_back(it->second->GetLabel());
      }
   }

   return labels;
}


boost::shared_ptr<HubInstance>
DeviceManager::GetParentDevice(boost::shared_ptr<DeviceInstance> device) const
{
   char parentLabel[MM::MaxStrLength];
   device->GetParentID(parentLabel);

   if (strlen(parentLabel) == 0)
   {
      // no parent specified, but we will try to infer one anyway
      // TODO So what happens if there is more than one hub in a given device
      // adapter? Answer: bad things.
      boost::shared_ptr<HubInstance> parentHub;
      for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
      {
         if (it->second->GetType() == MM::HubDevice &&
               device->GetAdapterModule() == it->second->GetAdapterModule())
         {
            parentHub = boost::static_pointer_cast<HubInstance>(it->second);
         }
      }
      // This returns the last matching hub; not sure why it was coded that
      // way, and it probably should be an error if there are more than 1.
      // TODO We should probably throw when parentHub is null.
      return parentHub;
   }
   else
   {
      for (DeviceConstIterator it = devices_.begin(), end = devices_.end(); it != end; ++it)
      {
         if (it->first == parentLabel &&
               it->second->GetType() == MM::HubDevice &&
               it->second->GetAdapterModule() == device->GetAdapterModule())
         {
            return boost::static_pointer_cast<HubInstance>(it->second);
         }
      }
      // TODO We should probably throw when the parent is missing.
      return boost::shared_ptr<HubInstance>();
   }
}


MMThreadLock*
DeviceManager::getModuleLock(boost::shared_ptr<DeviceInstance> pDev)
{
   if (pDev == 0)
      return 0;

   return pDev->GetAdapterModule()->GetLock();
}


} // namespace mm
