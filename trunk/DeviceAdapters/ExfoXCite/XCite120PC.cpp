///////////////////////////////////////////////////////////////////////////////
// FILE:         XCite120PC.cpp
// PROJECT:      Micro-Manager
// SUBSYSTEM:    DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:  This is the Micro-Manager device adapter for the EXFO X-Cite 120PC
//
// AUTHOR:       Mark Allen Neil, markallenneil@yahoo.com, Dec-2010
//               This code reuses work done by Jannis Uhlendorf, 2010
//
// COPYRIGHT:    Mission Bay Imaging, 2010
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

#ifdef WIN32
   #include <windows.h>
   #define snprintf _snprintf 
#endif

#include "XCite120PC.h"
#include "../../MMDevice/ModuleInterface.h"

#include <string>
#include <math.h>
#include <time.h>
#include <algorithm>
#include <sstream>
#include <iostream>

using namespace std;

// Commands
const char* XCite120PC::cmdConnect               = "tt";
const char* XCite120PC::cmdLockFrontPanel        = "ll";
const char* XCite120PC::cmdUnlockFrontPanel      = "nn";
const char* XCite120PC::cmdClearAlarm            = "aa";
const char* XCite120PC::cmdOpenShutter           = "mm";
const char* XCite120PC::cmdCloseShutter          = "zz";
const char* XCite120PC::cmdTurnLampOn            = "bb";
const char* XCite120PC::cmdTurnLampOff           = "ss";
const char* XCite120PC::cmdGetSoftwareVersion    = "vv";
const char* XCite120PC::cmdGetLampHours          = "hh";   
const char* XCite120PC::cmdGetUnitStatus         = "uu";
const char* XCite120PC::cmdGetIntensityLevel     = "ii";
const char* XCite120PC::cmdSetIntensityLevel     = "i";

// Return codes
const char* XCite120PC::retOk                    = "";
const char* XCite120PC::retError                 = "e";

XCite120PC::XCite120PC(const char* name) :
   initialized_(false),
   deviceName_(name),
   serialPort_("Undefined"),
   shutterOpen_(false),
   frontPanelLocked_("False"),
   lampIntensity_("0"),
   lampState_("On")
{
  InitializeDefaultErrorMessages();

  CreateProperty(MM::g_Keyword_Name, deviceName_.c_str(), MM::String, true);

  CPropertyAction* pAct = new CPropertyAction(this, &XCite120PC::OnPort);
  CreateProperty(MM::g_Keyword_Port, "Undefined", MM::String, false, pAct, true);
}

XCite120PC::~XCite120PC()
{
  Shutdown();
}

int XCite120PC::Initialize()
{
   int status;
   string response;
   vector<string> allowedValues;

   LogMessage("XCite120PC: Initialization");

   // Connect to hardware
   status = ExecuteCommand(cmdConnect);
   if (status != DEVICE_OK)
      return status;

   // Clear alarm
   status = ExecuteCommand(cmdClearAlarm);
   if (status != DEVICE_OK)
      return status;

   // Lamp intensity
   CPropertyAction *pAct = new CPropertyAction(this, &XCite120PC::OnIntensity);
   CreateProperty("Lamp-Intensity", "100", MM::Integer, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("0");
   allowedValues.push_back("12");
   allowedValues.push_back("25");
   allowedValues.push_back("50");
   allowedValues.push_back("100");
   SetAllowedValues("Lamp-Intensity", allowedValues);
  
   // Shutter state
   pAct = new CPropertyAction(this, &XCite120PC::OnShutterState);
   CreateProperty("Shutter-State", "Closed", MM::String, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("Closed");
   allowedValues.push_back("Open");
   SetAllowedValues("Shutter-State", allowedValues);

   // Front panel state
   pAct = new CPropertyAction(this, &XCite120PC::OnPanelLock);
   CreateProperty("Front-Panel-Lock", "False", MM::String, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("True");
   allowedValues.push_back("False");
   SetAllowedValues("Front-Panel-Lock", allowedValues);
 
   // Lamp state
   pAct = new CPropertyAction(this, &XCite120PC::OnLampState);
   CreateProperty("Lamp-State", "On", MM::String, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("On");
   allowedValues.push_back("Off");
   SetAllowedValues("Lamp-State", allowedValues);

   // Alarm state ("button")
   pAct = new CPropertyAction(this, &XCite120PC::OnClearAlarm);
   CreateProperty("Alarm-Clear", "Clear", MM::String, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("Clear");
   SetAllowedValues("Alarm-Clear", allowedValues);
     
   // Software version
   status = ExecuteCommand(cmdGetSoftwareVersion, NULL, 0, &response);
   if (status != DEVICE_OK)
      return status;
   CreateProperty("Software-Version", response.c_str(), MM::String, true);
   
   // Lamp hours
   status = ExecuteCommand(cmdGetLampHours, NULL, 0, &response);
   if (status != DEVICE_OK)
      return status;
   CreateProperty("Lamp-Hours", response.c_str(), MM::String, true);

   // Unit status ("fields")
   CreateProperty("Unit-Status-Alarm-State", "Unknown", MM::String, false);
   CreateProperty("Unit-Status-Lamp-State", "Unknown", MM::String, false);
   CreateProperty("Unit-Status-Shutter-State", "Unknown", MM::String, false);
   CreateProperty("Unit-Status-Home", "Unknown", MM::String, false);
   CreateProperty("Unit-Status-Lamp-Ready", "Unknown", MM::String, false);
   CreateProperty("Unit-Status-Front-Panel", "Unknown", MM::String, false);

   // Unit status ("button")
   pAct = new CPropertyAction(this, &XCite120PC::OnUnitStatus);
   CreateProperty("Unit-Status", "Update", MM::String, false, pAct);
   allowedValues.clear();
   allowedValues.push_back("Update");
   SetAllowedValues("Unit-Status", allowedValues);

   // Update and decode status
   status = ExecuteCommand(cmdGetUnitStatus, NULL, 0, &response);
   if (status != DEVICE_OK)
      return status;
   status = atoi(response.c_str());
   DecodeAndUpdateStatus(status);

   // Update existing state based on existing status
   shutterOpen_ = 0 != (status & 4);
   SetProperty("Shutter-State", shutterOpen_ ? "Open" : "Closed");
   lampState_ = 0 != (status & 2) ? "On" : "Off";
   SetProperty("Lamp-State", lampState_.c_str());
   frontPanelLocked_ = 0 != (status & 32) ? "True" : "False";
   SetProperty("Front-Panel-Lock", frontPanelLocked_.c_str());

   // Initialize intensity from existing state
   status = ExecuteCommand(cmdGetIntensityLevel, NULL, 0, &response);
   if (status != DEVICE_OK)
      return status;
   if (0 == response.compare("0"))
      lampIntensity_ = "0";
   else if (0 == response.compare("1"))
      lampIntensity_ = "12";
   else if (0 == response.compare("2"))
      lampIntensity_ = "25";
   else if (0 == response.compare("3"))
      lampIntensity_ = "50";
   else if (0 == response.compare("4"))
      lampIntensity_ = "100";

   initialized_ = true;
   return DEVICE_OK;
}

int XCite120PC::Shutdown()
{
   initialized_ = false;
   return DEVICE_OK;
}

void XCite120PC::GetName(char* Name) const
{
   CDeviceUtils::CopyLimitedString(Name, deviceName_.c_str());
}

bool XCite120PC::Busy()
{
   // All commands wait for a response, so we should never be busy
   return false;
}

int XCite120PC::SetOpen(bool open)
{
   shutterOpen_ = open;
   if (open)
   {
      LogMessage("XCite120PC: Open Shutter");
      return ExecuteCommand(cmdOpenShutter);
   }
   else
   {
      LogMessage("XCite120PC: Close Shutter");
      return ExecuteCommand(cmdCloseShutter);
   }
}

int XCite120PC::GetOpen(bool& open)
{
  open = shutterOpen_;
  return DEVICE_OK;
}

int XCite120PC::Fire(double /* deltaT */)
{
   // Not supported
   return DEVICE_UNSUPPORTED_COMMAND;
}

int XCite120PC::OnPort(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set(serialPort_.c_str());
   else if (eAct == MM::AfterSet)
   {
      if (initialized_)
      {
         // Revert
         pProp->Set(serialPort_.c_str());
         return ERR_PORT_CHANGE_FORBIDDEN;
      }
      pProp->Get(serialPort_);
      LogMessage("XCite120PC: Using Port: " + serialPort_);
   }
   return DEVICE_OK;
}
  
int XCite120PC::OnIntensity(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set(lampIntensity_.c_str());
   else if (eAct == MM::AfterSet)
   {
      pProp->Get(lampIntensity_);
      LogMessage("XCite120PC: Set Intensity: " + lampIntensity_);
      char intensity_code[] = "0";
      if (0 == lampIntensity_.compare("0"))
         intensity_code[0] = '0';
      else if (0 == lampIntensity_.compare("12"))
         intensity_code[0] = '1';
      else if (0 == lampIntensity_.compare("25"))
         intensity_code[0] = '2';
      else if (0 == lampIntensity_.compare("50"))
         intensity_code[0] = '3';
      else if (0 == lampIntensity_.compare("100"))
         intensity_code[0] = '4';
      return ExecuteCommand(cmdSetIntensityLevel, intensity_code, 1);
   }
   return DEVICE_OK;
}

int XCite120PC::OnPanelLock(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set(frontPanelLocked_.c_str());
   else if (eAct==MM::AfterSet)
   {
      pProp->Get(frontPanelLocked_);
      LogMessage("XCite120PC: Front Panel Lock: " + frontPanelLocked_);
      if (0 == frontPanelLocked_.compare("True"))
         return ExecuteCommand(cmdLockFrontPanel);
      else if (0 == frontPanelLocked_.compare("False"))
         return ExecuteCommand(cmdUnlockFrontPanel);
   }
   return DEVICE_OK;
}

int XCite120PC::OnLampState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set(lampState_.c_str());
   else if (eAct==MM::AfterSet)
   {
      pProp->Get(lampState_);
      LogMessage("XCite120PC: Lamp State: " + lampState_);
      if (0 == lampState_.compare("On"))
         return ExecuteCommand(cmdTurnLampOn);
      else if (0 == lampState_.compare("Off"))
         return ExecuteCommand(cmdTurnLampOff);
   }
   return DEVICE_OK;  
}

int XCite120PC::OnShutterState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set(shutterOpen_ ? "Open" : "Closed");
   else if (eAct == MM::AfterSet)
   {
      string buff;
      pProp->Get(buff);
      SetOpen(0 == buff.compare("Open"));
   }
   return DEVICE_OK;
}

int XCite120PC::OnClearAlarm(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set("Clear");
   else if (eAct == MM::AfterSet)
   {
      LogMessage("XCite120PC: Alarm Cleared");
      return ExecuteCommand(cmdClearAlarm);      
   }
   return DEVICE_OK;
}

int XCite120PC::OnUnitStatus(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
      pProp->Set("Update");
   else if (eAct == MM::AfterSet)
   {
      string response;
      int status = ExecuteCommand(cmdGetUnitStatus, NULL, 0, &response);
      if (status != DEVICE_OK)
         return status;
      DecodeAndUpdateStatus(atoi(response.c_str()));
      LogMessage("XCite120PC: Unit Status: " + response);
   }
   return DEVICE_OK;
}

void XCite120PC::DecodeAndUpdateStatus(int status)
{
   if (HasProperty("Unit-Status-Alarm-State"))
      SetProperty("Unit-Status-Alarm-State", 0 != (status & 1) ? "ON" : "OFF");
   if (HasProperty("Unit-Status-Lamp-State"))
      SetProperty("Unit-Status-Lamp-State", 0 != (status & 2) ? "ON" : "OFF");
   if (HasProperty("Unit-Status-Shutter-State"))
      SetProperty("Unit-Status-Shutter-State", 0 != (status & 4) ? "OPEN" : "CLOSED");
   if (HasProperty("Unit-Status-Home"))
      SetProperty("Unit-Status-Home", 0 != (status & 8) ? "FAULT" : "PASS");
   if (HasProperty("Unit-Status-Lamp-Ready"))
      SetProperty("Unit-Status-Lamp-Ready", 0 != (status & 16) ? "READY" : "NOT READY");
   if (HasProperty("Unit-Status-Front-Panel"))
      SetProperty("Unit-Status-Front-Panel", 0 != (status & 32) ? "LOCKED" : "NOT LOCKED");
   OnPropertiesChanged();
}

// Exedute a command, input, inputlen and ret are 0 by default
//   if a pointer to input and a value for inputlen is given, this input is sent to the device
//   if a pointer to ret is given, the return value of the device it returned in retVal
int XCite120PC::ExecuteCommand(const string& cmd, const char* input, int inputLen, string* retVal)
{
   char* cmd_i;

   if (input == NULL) // No input
   {
      cmd_i = new char[cmd.size()+1];
      strcpy(cmd_i,cmd.c_str());
   }
   else  // Command with input
   {
      cmd_i = new char[cmd.size() + inputLen + 1];
      strcpy(cmd_i, cmd.c_str());
      strncat(cmd_i, input, inputLen);
   }
  
   // Clear comport
   int status = PurgeComPort(serialPort_.c_str());
   if (status != DEVICE_OK)
      return status;

   // Send command
   status = SendSerialCommand(serialPort_.c_str(), cmd_i, "\r");
   if (status != DEVICE_OK)
      return status;

   delete [] cmd_i;

   // Get status
   string buff;
   status = GetSerialAnswer(serialPort_.c_str(), "\r", buff);
   if (status != DEVICE_OK)
      return status;

   if (0 == buff.compare(retError))
      return DEVICE_ERR;

   if (0 != buff.compare(retOk) && retVal == NULL)
      return DEVICE_NOT_CONNECTED;

   if (retVal != NULL) // Return value
      *retVal = buff;
   
   return DEVICE_OK;
}

