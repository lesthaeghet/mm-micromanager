// Mock device adapter for testing of device sequencing
//
// Copyright (C) 2014 University of California, San Francisco.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation.
//
// This library is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
//
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this library; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//
// Author: Mark Tsuchida

#include "SequenceTester.h"
#include "SequenceTesterImpl.h"

#include "ModuleInterface.h"

#include <msgpack.hpp>

#include <boost/bind.hpp>
#include <boost/move/move.hpp>
#include <exception>
#include <sstream>


// Container for Micro-Manager error code
class DeviceError : public std::exception
{
   const int code_;
public:
   DeviceError(int code) : code_(code) {}
   int GetCode() const { return code_; }
};


MODULE_API void
InitializeModuleData()
{
   RegisterDevice("THub", MM::HubDevice,
         "Fake devices for automated testing");
}


inline bool StartsWith(const std::string& prefix, const std::string& s)
{
   return s.substr(0, prefix.size()) == prefix;
}


MODULE_API MM::Device*
CreateDevice(const char* deviceName)
{
   if (!deviceName)
      return 0;
   const std::string name(deviceName);
   if (name == "THub")
      return new TesterHub(name);
   if (StartsWith("TCamera", name))
      return new TesterCamera(name);
   return 0;
}


MODULE_API void
DeleteDevice(MM::Device* pDevice)
{
   delete pDevice;
}


TesterHub::TesterHub(const std::string& name) :
   Super(name)
{
}


int
TesterHub::Initialize()
{
   int err;

   err = Super::Initialize();
   if (err != DEVICE_OK)
      return err;

   return DEVICE_OK;
}


int
TesterHub::Shutdown()
{
   int err;

   err = Super::Shutdown();
   if (err != DEVICE_OK)
      return err;

   return DEVICE_OK;
}


int
TesterHub::DetectInstalledDevices()
{
   ClearInstalledDevices();
   AddInstalledDevice(new TesterCamera("TCamera"));
   return DEVICE_OK;
}


TesterCamera::TesterCamera(const std::string& name) :
   Super(name),
   snapCounter_(0),
   sequenceCounter_(0),
   cumulativeSequenceCounter_(0),
   snapImage_(0),
   stopSequence_(true),
   exposureSetting_(this, 100.0, true, 0.1, 1000.0),
   binningSetting_(this, 1, true, 1, 1)
{
}


TesterCamera::~TesterCamera()
{
   delete[] snapImage_;
}


int
TesterCamera::Initialize()
{
   int err;

   err = Super::Initialize();
   if (err != DEVICE_OK)
      return err;

   CreateFloatProperty("Exposure", exposureSetting_);
   CreateIntegerProperty("Binning", binningSetting_);

   return DEVICE_OK;
}


int
TesterCamera::Shutdown()
{
   int err;

   err = Super::Shutdown();
   if (err != DEVICE_OK)
      return err;

   return DEVICE_OK;
}


int
TesterCamera::SnapImage()
{
   boost::lock_guard<boost::mutex> lock(sequenceMutex_);

   delete[] snapImage_;
   snapImage_ = GenerateLogImage(false);
   ++snapCounter_;

   return DEVICE_OK;
}


const unsigned char*
TesterCamera::GetImageBuffer()
{
   boost::lock_guard<boost::mutex> lock(sequenceMutex_);
   return snapImage_;
}


int
TesterCamera::GetBinning() const
{
   return static_cast<int>(binningSetting_.Get());
}


int
TesterCamera::SetBinning(int binSize)
{
   return binningSetting_.Set(binSize);
}


long
TesterCamera::GetImageBufferSize() const
{
   return GetImageWidth() * GetImageHeight() * GetImageBytesPerPixel();
}


unsigned
TesterCamera::GetImageWidth() const
{
   return 512; // TODO
}


unsigned
TesterCamera::GetImageHeight() const
{
   return 512; // TODO
}


void
TesterCamera::SetExposure(double exposureMs)
{
   exposureSetting_.Set(exposureMs);
}


double
TesterCamera::GetExposure() const
{
   return exposureSetting_.Get();
}


int
TesterCamera::SetROI(unsigned, unsigned, unsigned, unsigned)
{
   return DEVICE_UNSUPPORTED_COMMAND;
}


int
TesterCamera::GetROI(unsigned& x, unsigned& y, unsigned& w, unsigned& h)
{
   // Always full-frame
   x = y = 0;
   w = GetImageWidth();
   h = GetImageHeight();
   return DEVICE_OK;
}


int
TesterCamera::StartSequenceAcquisition(long count, double, bool stopOnOverflow)
{
   return StartSequenceAcquisitionImpl(true, count, stopOnOverflow);
}


int
TesterCamera::StartSequenceAcquisition(double)
{
   return StartSequenceAcquisitionImpl(false, 0, false);
}


int
TesterCamera::StartSequenceAcquisitionImpl(bool finite, long count,
      bool stopOnOverflow)
{
   {
      boost::lock_guard<boost::mutex> lock(sequenceMutex_);
      if (!stopSequence_)
         return DEVICE_ERR;
      stopSequence_ = false;
   }

   GetCoreCallback()->PrepareForAcq(this);

   // Note: boost::packaged_task<void ()> in more recent versions of Boost.
   boost::packaged_task<void> captureTask(
         boost::bind(&TesterCamera::SendSequence, this,
            finite, count, stopOnOverflow));
   sequenceFuture_ = captureTask.get_future();

   boost::thread captureThread(boost::move(captureTask));
   captureThread.detach();

   return DEVICE_OK;
}


int
TesterCamera::StopSequenceAcquisition()
{
   {
      boost::lock_guard<boost::mutex> lock(sequenceMutex_);
      if (stopSequence_)
         return DEVICE_OK;
      stopSequence_ = true;
   }

   // In newer Boost versions: if (sequenceFuture_.valid())
   if (sequenceFuture_.get_state() != boost::future_state::uninitialized)
   {
      try
      {
         sequenceFuture_.get();
      }
      catch (const DeviceError& e)
      {
         sequenceFuture_ = boost::unique_future<void>();
         return e.GetCode();
      }
      sequenceFuture_ = boost::unique_future<void>();
   }

   return GetCoreCallback()->AcqFinished(this, 0);
}


bool
TesterCamera::IsCapturing()
{
   boost::lock_guard<boost::mutex> lock(sequenceMutex_);
   return !stopSequence_;
}


const unsigned char*
TesterCamera::GenerateLogImage(bool isSequenceImage)
{
   msgpack::sbuffer sbuf;

   // Placeholder
   msgpack::pack(sbuf, isSequenceImage);
   if (isSequenceImage)
   {
      msgpack::pack(sbuf, cumulativeSequenceCounter_);
      msgpack::pack(sbuf, sequenceCounter_);
   }
   else
   {
      msgpack::pack(sbuf, snapCounter_);
   }
   // End placeholder

   size_t bufSize = GetImageBufferSize();
   unsigned char* bytes = new unsigned char[bufSize];
   if (sbuf.size() <= bufSize)
   {
      memcpy(bytes, sbuf.data(), sbuf.size());
      memset(bytes + sbuf.size(), 0, bufSize - sbuf.size());
   }
   else // Won't fit; return zeroed image
   {
      memset(bytes, 0, bufSize);
   }
   return bytes;
}


void
TesterCamera::SendSequence(bool finite, long count, bool stopOnOverflow)
{
   MM::Core* core = GetCoreCallback();

   char label[MM::MaxStrLength];
   GetLabel(label);
   Metadata md;
   md.put("Camera", label);
   std::string serializedMD(md.Serialize());

   const unsigned char* bytes = 0;

   for (long frame = 0; !finite || frame < count; ++frame)
   {
      unsigned width, height, bytesPerPixel;
      {
         boost::lock_guard<boost::mutex> lock(sequenceMutex_);
         if (stopSequence_)
            break;

         width = GetImageWidth();
         height = GetImageHeight();
         bytesPerPixel = GetImageBytesPerPixel();

         sequenceCounter_ = frame;
         delete[] bytes;
         bytes = GenerateLogImage(true);
         ++cumulativeSequenceCounter_;
      }

      try
      {
         int err;
         err = core->InsertImage(this, bytes, width, height,
               bytesPerPixel, serializedMD.c_str());

         if (!stopOnOverflow && err == DEVICE_BUFFER_OVERFLOW)
         {
            core->ClearImageBuffer(this);
            err = core->InsertImage(this, bytes, width, height,
                  bytesPerPixel, serializedMD.c_str(), false);
         }

         if (err != DEVICE_OK)
         {
            bool stopped;
            {
               boost::lock_guard<boost::mutex> lock(sequenceMutex_);
               stopped = stopSequence_;
            }
            // If we're stopped already, that could be the reason for the
            // error.
            if (!stopped)
               BOOST_THROW_EXCEPTION(DeviceError(err));
            else
               break;
         }
      }
      catch (...)
      {
         delete[] bytes;
         throw;
      }
   }

   delete[] bytes;
}
