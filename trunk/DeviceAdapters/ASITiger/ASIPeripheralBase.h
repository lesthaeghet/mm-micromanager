///////////////////////////////////////////////////////////////////////////////
// FILE:          ASIPeripheralBase.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   ASI generic device class templates
//
// COPYRIGHT:     Applied Scientific Instrumentation, Eugene OR
//
// LICENSE:       This file is distributed under the BSD license.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Jon Daniels (jon@asiimaging.com) 09/2013
//
// BASED ON:      ASIStage.h
//

#ifndef _ASIPeripheralBase_H_
#define _ASIPeripheralBase_H_

#include "ASITiger.h"
#include "ASIBase.h"
#include "ASIHub.h"


// See ASIBase.h for an explanation of how this class works.


template <template <typename> class TDeviceBase, class UConcreteDevice>
class ASIPeripheralBase : public ASIBase<TDeviceBase, UConcreteDevice>
{
public:
   ASIPeripheralBase(const char* name) :
      ASIBase<TDeviceBase, UConcreteDevice>(name),
      hub_(NULL),
      addressString_(g_EmptyCardAddressStr),
      ret_(DEVICE_OK) // TODO Remove this; should use local var
   {
      // sometimes constructor gets called without the full name like in the case of the hub
      //   so only set up these properties if we have the required information
      if (IsExtendedName(name))
      {
         addressString_ = GetHexAddrFromExtName(name);
         if (addressString_.compare(g_EmptyCardAddressStr) != 0 )
         {
            this->CreateProperty(g_TigerHexAddrPropertyName, addressString_.c_str(), MM::String, true);
            addressChar_ = ConvertToTigerRawAddress(addressString_);
         }
      }
   }

   // The Initialize() of child (concrete peripheral) classes must call this.
   // Note that initialize_ is set by the concrete child class.
   int PeripheralInitialize(bool skipFirmware = false)
   {
      // get the hub information
      MM::Hub* genericHub = this->GetParentHub();
      if (!genericHub)
         return DEVICE_COMM_HUB_MISSING;
      hub_ = dynamic_cast<ASIHub*>(genericHub);
      if (!hub_)
         return DEVICE_COMM_HUB_MISSING;

      // get the firmware version and expose that as property plus store it
      // also the firmware compile date and build name
      // skip in special cases (when called with true flag, false is default) including
      //    FWheel: different serial terminator => own Initialize() handles
      //    TigerCommTub: needs to return different value plus doesn't have address
      if (!skipFirmware)
      {
         ostringstream command; command.str("");
         command << addressChar_ << "V";
         RETURN_ON_MM_ERROR ( hub_->QueryCommandVerify(command.str(),":A v") );
         RETURN_ON_MM_ERROR ( hub_->ParseAnswerAfterPosition(4, this->firmwareVersion_ ));
         command.str("");
         command << this->firmwareVersion_;
         RETURN_ON_MM_ERROR ( this->CreateProperty(g_FirmwareVersionPropertyName, command.str().c_str(), MM::Float, true) );

         // also grab the firmware compile date
         // currently for user information only, stored as a string so would need to convert it to proper type to use in comparisons, etc.
         command.str("");
         command << addressChar_ << "CD";
         RETURN_ON_MM_ERROR ( hub_->QueryCommand(command.str()) );
         this->firmwareDate_ = hub_->LastSerialAnswer();
         RETURN_ON_MM_ERROR ( this->CreateProperty(g_FirmwareDatePropertyName, this->firmwareDate_.c_str(), MM::String, true) );

         // also grab the firmware build name
         command.str("");
         command << addressChar_ << "BU";
         RETURN_ON_MM_ERROR ( hub_->QueryCommand(command.str()) );
         this->firmwareBuild_ = hub_->LastSerialAnswer();
         RETURN_ON_MM_ERROR ( this->CreateProperty(g_FirmwareBuildPropertyName, this->firmwareBuild_.c_str(), MM::String, true) );
      }

      return DEVICE_OK;
   }

protected:
   ASIHub *hub_;           // used for serial communication
   string addressString_;  // address within hub, in hex format
                           // generally two characters (e.g. '31') but could be four characters (e.g. '3132') if device axes are split between cards
   string addressChar_;    // address within hub, in single character (possibly extended ASCII)
                           // in case of XY device, the MM device is split across two HW cards so it will be two characters
   int ret_;               // return code for use with Micro-manager functions


   // related to creating "extended" names containing address and axis letters
   bool IsExtendedName(const char* name) const
   {
      vector<string> vName;
      CDeviceUtils::Tokenize(name, vName, ":");
      return (vName.size() > 2);
   }

   // returns single-character string
   string GetAxisLetterFromExtName(const char* name, int position = 0) const
   {
      vector<string> vName;
      CDeviceUtils::Tokenize(name, vName, ":");
      if (vName.size() > 1)
         return (vName[1].substr(position,1));
      else
         return g_EmptyAxisLetterStr;
   }

   string GetHexAddrFromExtName(const char* name) const
   {
      vector<string> vName;
      CDeviceUtils::Tokenize(name, vName, ":");
      if (vName.size() > 2)
         return (vName[2]);
      else
         return g_EmptyCardAddressStr;
   }

private:
   // does the dirty work of converting a two-character hex (e.g. F5) into the single character
   // see ConvertToTigerRawAddress comments for more details
   string ConvertTwoCharStringToHexChar(const string &s) const
   {
      if (s.size() != 2)
         return g_EmptyCardAddressCode;

      unsigned int code;
      stringstream ss;
      ss << hex << s;
      ss >> code;
      if ((code >= 0x31 && code <= 0x39) || (code >= 0x81 && code <= 0xF5))
      {
         ss.str("");
         ss.clear(); // clears hex flag
         ss << (char) code;
         string s2;
         ss >> s2;
         return s2;
      }
      else
         return g_EmptyCardAddressCode;
   }

   string ConvertToTigerRawAddress(const string &s) const
   {
      // Tiger addresses are 0x31 to 0x39 and then 0x81 to 0xF5 (i.e. '1' to '9' and then extended ASCII)
      // these addresses are prepended (in extended ASCII char) to serial commands that are addressed
      // MM doesn't handle names with with extended ASCII, and we need to include address in MM's device name
      // so in MM device names we represent the address in 2-digit hex string (31..39, 81..F5)
      // use this function to get the serial char to send from a MM 2-digit hex string
      // returns g_EmptyCardAddressCode in case of error (set to space for minimum impact on Tiger operation)

      // make sure the number of characters is even and strictly positive
      if ((s.size() == 0) || (s.size() % 2))
         return g_EmptyCardAddressCode;

      string s2 = "";
      for(std::string::size_type iii = 0; iii < s.size()/2; ++iii)
      {
         // operate on chunks of two characters
         s2.append(ConvertTwoCharStringToHexChar(s.substr(iii*2, 2)));
      }
      return s2;
   }
};


#endif // _ASIPeripheralBase_H_
