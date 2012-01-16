///////////////////////////////////////////////////////////////////////////////
// FILE:          IntegratedFilterWheel.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Thorlabs device adapters: Integrated Filter Wheel
//
// COPYRIGHT:     Thorlabs, 2011
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
// AUTHOR:        Nenad Amodaj, http://nenad.amodaj.com, 2011
//


#ifdef WIN32
   #define WIN32_LEAN_AND_MEAN
   #include <windows.h>
   #define snprintf _snprintf 
#endif

#include "Thorlabs.h"
#include <cstdio>
#include <string>
#include <math.h>
#include <sstream>
using namespace std;


///////////
// commands
///////////

// MGMSG_MOT_MOVE_HOME
const unsigned char homeCmd[] = {0x43, 0x04, 0x10, 0x00, 0x50, 0x01};
const unsigned char homeRsp[] = {0x44, 0x04, 0x10, 0x00, 0x01, 0x50};

// get position
const unsigned char getPosCmd[] =  {         0x11, // cmd low byte
                                             0x04, // cmd high byte
                                             0x01, // channel id low
                                             0x00, // channel id hi
                                             0x50, // dest low
                                             0x01, // dest hi
                                          };             


const unsigned char getPosRsp[] = {          0x12, // cmd low byte
                                             0x04, // cmd high byte
                                             0x06, // num bytes low
                                             0x00, // num bytes hi
                                             0x81, // 
                                             0x50, // 
                                             0x01, // channel low
                                             0x00, // channel hi
                                             0x00, // position low byte
                                             0x00,  // position  
                                             0x00, // position
                                             0x00  // position high byte
                                          };             

// set position
const unsigned char setPosCmd[] =  {         0x53, // cmd low byte
                                             0x04, // cmd high byte
                                             0x06, // nun bytes low
                                             0x00, // num bytes hi
                                             0x81, // 
                                             0x50, //
                                             0x01, // ch low
                                             0x00, // ch hi
                                             0x00, // position low byte
                                             0x00, // position  
                                             0x00, // position
                                             0x00  // position high byte
                                          };             

// MGMSG_MOT_REQ_ DEVPARAMS
const unsigned char reqParamsCmd[] =  {      0x15, // cmd low byte
                                             0x00, // cmd high byte
                                             0x20, // num bytes low
                                             0x00, // num bytes hi
                                             0x50, // 
                                             0x01
                                          };             

const unsigned char getParamsRsp[] = {       0x16, // cmd low
                                             0x00, //
                                             0x28, // 
                                             0x00, // 
                                             0x81, // 
                                             0x50, // cmd hi

                                             0x20, // num pos low
                                             0x00, // 
                                             0x00, //
                                             0x00, // num pos hi

                                             0x01, // ch ID low
                                             0x00, //   
                                             0x00, // 
                                             0x00,  // ch ID hi

                                             0x08, // num pos low
                                             0x00,
                                             0x00,
                                             0x00 // num pos hi
                                          };

using namespace std;

extern const char* g_WheelDeviceName;

IntegratedFilterWheel::IntegratedFilterWheel() : 
   numPos_(6), 
   busy_(false),
   home_(false),
   initialized_(false), 
   changedTime_(0.0),
   position_(0),
   port_(""),
   answerTimeoutMs_(1000.0)
{
   InitializeDefaultErrorMessages();

   //Com port
   CPropertyAction* pAct = new CPropertyAction (this, &IntegratedFilterWheel::OnCOMPort);
   CreateProperty(MM::g_Keyword_Port, "", MM::String, false, pAct, true);

   EnableDelay();
}

IntegratedFilterWheel::~IntegratedFilterWheel()
{
   Shutdown();
}

void IntegratedFilterWheel::GetName(char* Name) const
{
   CDeviceUtils::CopyLimitedString(Name, g_WheelDeviceName);
}


int IntegratedFilterWheel::Initialize()
{
   if (initialized_)
      return DEVICE_OK;

   // set property list
   // -----------------

   // Name
   int ret = CreateProperty(MM::g_Keyword_Name, g_WheelDeviceName, MM::String, true);
   if (DEVICE_OK != ret)
      return ret;

   // Description
   ret = CreateProperty(MM::g_Keyword_Description, "Integrated filter wheel", MM::String, true);
   if (DEVICE_OK != ret)
      return ret;

   // create default positions and labels
   char buf[MM::MaxStrLength];
   for (long i=0; i<numPos_; i++)
   {
      snprintf(buf, MM::MaxStrLength, "Position-%ld", i + 1);
      SetPositionLabel(i, buf);
   }

   // State
   // -----
   CPropertyAction* pAct = new CPropertyAction (this, &IntegratedFilterWheel::OnState);
   ret = CreateProperty(MM::g_Keyword_State, "0", MM::Integer, false, pAct);
   if (ret != DEVICE_OK)
      return ret;

   // Label
   // -----
   pAct = new CPropertyAction (this, &CStateBase::OnLabel);
   ret = CreateProperty(MM::g_Keyword_Label, "", MM::String, false, pAct);
   if (ret != DEVICE_OK)
      return ret;

   // discover number of positions
   int numPos_ = DiscoverNumberOfPositions();
   if (numPos_ == 0)
   {
      // if the number iz zero, homing required
      ret = Home();
      if (ret != DEVICE_OK)
         return ret;

      // try again
      numPos_ = DiscoverNumberOfPositions();

      // if the number is still zero, something went wrong
      if (numPos_ == 0)
         return ERR_INVALID_NUMBER_OF_POS;
   }

   ret = RetrieveCurrentPosition(position_);
    if (ret != DEVICE_OK)
      return ret; 

   ret = UpdateStatus();
   if (ret != DEVICE_OK)
      return ret;

   initialized_ = true;

   return DEVICE_OK;
}

bool IntegratedFilterWheel::Busy()
{
   MM::MMTime interval = GetCurrentMMTime() - changedTime_;
   MM::MMTime delay(GetDelayMs()*1000.0);
   if (interval < delay)
      return true;
   else
      return false;
}


int IntegratedFilterWheel::Shutdown()
{
   if (initialized_)
   {
      initialized_ = false;
   }
   return DEVICE_OK;
}


///////////////////////////////////////////////////////////////////////////////
// private methods
///////////////////////////////////////////////////////////////////////////////

/**
 * Sends a binary seqence of bytes to the com port.
 */
int IntegratedFilterWheel::SetCommand(const unsigned char* command, unsigned length)
{
   int ret = WriteToComPort(port_.c_str(), command, length);
   if (ret != DEVICE_OK)
      return ret;
   return DEVICE_OK;
}

/**
 * Retrieves specified number of bytes (length) from the Rx buffer.
 * We block until we collect (length) bytes from the port.
 * As soon as bytes are retrieved the method returns with DEVICE_OK code.
 * If the specified number of bytes is not retrieved from the port within
 * (timeoutMs) interval, we return with error.
 */
int IntegratedFilterWheel::GetCommand(unsigned char* response, unsigned length, double timeoutMs)
{
   MM::MMTime startTime = GetCurrentMMTime();
   unsigned long totalBytesRead = 0;
   while ((totalBytesRead < length))
   {
      if ((GetCurrentMMTime() - startTime).getMsec() > timeoutMs)
         return ERR_RESPONSE_TIMEOUT;

      unsigned long bytesRead(0);
      int ret = ReadFromComPort(port_.c_str(), response + totalBytesRead, length-totalBytesRead, bytesRead);
      if (ret != DEVICE_OK)
         return ret;
      totalBytesRead += bytesRead;
   }
   return DEVICE_OK;
}
/**
 * Performs homing for the filter wheel
 */
int IntegratedFilterWheel::Home()
{
   ClearPort(*this, *GetCoreCallback(), port_);
   int ret = SetCommand(homeCmd, sizeof(homeCmd));
   if (ret != DEVICE_OK)
      return ret;

   const int cmdLength = sizeof(homeRsp);
   unsigned char answer[cmdLength];
   memset(answer, 0, cmdLength);
   ret = GetCommand(answer, cmdLength, answerTimeoutMs_);
   if (ret != DEVICE_OK)
      return ret;

   if (memcmp(answer, homeRsp, cmdLength) == 0)
      return ERR_UNRECOGNIZED_ANSWER;

   home_ = true; // successfully homed

   return DEVICE_OK;
}

/**
 * Determine the number of positions on the wheel.
 * Zero positions means that the wheel has not been homed yet.
 */
int IntegratedFilterWheel::DiscoverNumberOfPositions()
{
   ClearPort(*this, *GetCoreCallback(), port_);
   int ret = SetCommand(reqParamsCmd, sizeof(reqParamsCmd));
   if (ret != DEVICE_OK)
      return 0;

   const int answLength = sizeof(getParamsRsp);
   unsigned char answer[answLength];
   memset(answer, 0, answLength);
   ret = GetCommand(answer, answLength, answerTimeoutMs_);
   if (ret != DEVICE_OK)
      return 0;

   // check response signature
   if (memcmp(answer, getParamsRsp, 14) != 0)
      return ERR_UNRECOGNIZED_ANSWER;

   return (int)*(answer+14);
}

/**
 * Move to the specified position.
 */
int IntegratedFilterWheel::GoToPosition(long pos)
{
   ClearPort(*this, *GetCoreCallback(), port_);

   // send command
   unsigned char cmd[sizeof(setPosCmd)];
   memcpy(cmd, setPosCmd, sizeof(setPosCmd));
   long* posPtr = (long*)(cmd + 8);
   *posPtr = pos;
   int ret = SetCommand(cmd, sizeof(setPosCmd));
   if (ret != DEVICE_OK)
      return ret;

   // TODO: sleep?
 
   return DEVICE_OK;
}

/**
 * Retrieve current position.
 */
int IntegratedFilterWheel::RetrieveCurrentPosition(long& pos)
{
   ClearPort(*this, *GetCoreCallback(), port_);

   // send command
   unsigned char cmd[sizeof(getPosCmd)];
   memcpy(cmd, getPosCmd, sizeof(getPosCmd));
   long* posPtr = (long*)(cmd + 8);
   *posPtr = pos;
   int ret = SetCommand(cmd, sizeof(getPosCmd));
   if (ret != DEVICE_OK)
      return ret;

   // parse response
   const int answLength = sizeof(getPosRsp);
   unsigned char answer[answLength];
   memset(answer, 0, answLength);
   ret = GetCommand(answer, answLength, answerTimeoutMs_);
   if (ret != DEVICE_OK)
      return 0;

   // check response signature
   if (memcmp(answer, getParamsRsp, 8) != 0)
      return ERR_UNRECOGNIZED_ANSWER;

   pos =(int)*(answer+8);

   return DEVICE_OK;
}


///////////////////////////////////////////////////////////////////////////////
// Action handlers
///////////////////////////////////////////////////////////////////////////////

int IntegratedFilterWheel::OnState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   int ret = DEVICE_OK;

   if (eAct == MM::BeforeGet)
   {
      pProp->Set(position_);
   }
   else if (eAct == MM::AfterSet)
   {
      // busy timer
      changedTime_ = GetCurrentMMTime();

      long pos;
      pProp->Get(pos);
      if (pos >= numPos_ || pos < 0)
      {
         pProp->Set(position_); // revert
         return ERR_INVALID_POSITION;
      }
      
      ret = GoToPosition(pos);

      if (ret != DEVICE_OK)
         return ret;

      position_ = pos;
   }

   return DEVICE_OK;;
}

int IntegratedFilterWheel::OnCOMPort(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
   {
      pProp->Set(port_.c_str());
   }
   else if (eAct == MM::AfterSet)
   {
      if (initialized_)
         pProp->Set(port_.c_str());
 
      pProp->Get(port_);                                                     
   }                                                                         
                                                                             
   return DEVICE_OK;                                                         
}  
