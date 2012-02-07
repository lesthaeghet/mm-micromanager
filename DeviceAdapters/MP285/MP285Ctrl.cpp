//////////////////////////////////////////////////////////////////////////////
// FILE:          MP285Ctrl.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   MP285 Controller Driver
//
// COPYRIGHT:     Sutter Instrument,
//				  Mission Bay Imaging, San Francisco, 2011
//                All rights reserved
//
// LICENSE:       This library is free software; you can redistribute it and/or
//                modify it under the terms of the GNU Lesser General Public
//                License as published by the Free Software Foundation.
//                
//                You should have received a copy of the GNU Lesser General Public
//                License along with the source distribution; if not, write to
//                the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
//                Boston, MA  02111-1307  USA
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
//
// AUTHOR:        Lon Chu (lonchu@yahoo.com), created on June 2011
//

#ifdef WIN32
   #include <windows.h>
   #define snprintf _snprintf 
#endif

#include <stdio.h>
#include <string>
#include <fstream>
#include <iostream>
#include <sstream>
#include <math.h>
//#include <strsafe.h>
#include "../../MMCore/MMCore.h"
#include "../../MMDevice/ModuleInterface.h"
#include "../../MMDevice/DeviceUtils.h"
#include "MP285Error.h"
#include "MP285.h"
#include "MP285Ctrl.h"

using namespace std;


//////////////////////////////////////////////////////
// MP285 Controller
//////////////////////////////////////////////////////
//
// Controller - Controller for XYZ Stage.
// Note that this adapter uses two coordinate systems.  There is the adapters own coordinate
// system with the X and Y axis going the 'Micro-Manager standard' direction
// Then, there is the MP285s native system.  All functions using 'steps' use the MP285 system
// All functions using Um use the Micro-Manager coordinate system
//


//
// MP285 Controller Constructor
//
MP285Ctrl::MP285Ctrl() :
    //m_nAnswerTimeoutMs(1000),   // wait time out set 1000 ms
    m_yInitialized(false)       // initialized flag set to false
{
    // call initialization of error messages
    InitializeDefaultErrorMessages();

    m_nAnswerTimeoutMs = MP285::Instance()->GetTimeoutInterval();
    m_nAnswerTimeoutTrys = MP285::Instance()->GetTimeoutTrys();

    // Port:
    CPropertyAction* pAct = new CPropertyAction(this, &MP285Ctrl::OnPort);
    int ret = CreateProperty(MM::g_Keyword_Port, "Undefined", MM::String, false, pAct, true);
    
    std::ostringstream osMessage;
    osMessage << "<MP285Ctrl::class-constructor> CreateProperty(" << MM::g_Keyword_Port << " = Undfined), ReturnCode=" << ret;
    this->LogMessage(osMessage.str().c_str());
}

//
// MP285 Controller Destructor
//
MP285Ctrl::~MP285Ctrl()
{
    Shutdown();
}

//
// return device name of the MP285 controller
//
void MP285Ctrl::GetName(char* sName) const
{
    CDeviceUtils::CopyLimitedString(sName, MP285::Instance()->GetMPStr(MP285::MPSTR_CtrlDevName).c_str());
}

//
// Initialize the MP285 controller
//
int MP285Ctrl::Initialize()
{
    std::ostringstream osMessage;

    // empty the Rx serial buffer before sending command
    int ret = ClearPort(*this, *GetCoreCallback(), MP285::Instance()->GetSerialPort().c_str());

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> ClearPort(Port = " << MP285::Instance()->GetSerialPort().c_str() << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    // Name
    ret = CreateProperty(MM::g_Keyword_Name, MP285::Instance()->GetMPStr(MP285::MPSTR_CtrlDevName).c_str(), MM::String, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MM::g_Keyword_Name << " = " << MP285::Instance()->GetMPStr(MP285::MPSTR_CtrlDevName).c_str() << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    // Description
    ret = CreateProperty(MM::g_Keyword_Description, "Sutter MP-285 Controller", MM::String, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MM::g_Keyword_Description << " = " << "Sutter MP-285 Controller" << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK)  return ret;

    // Create read-only property for version info
    // MP285 Adpater Version Property
    // const char* sVersionLabel = MP285::Instance()->GetMPStr(MP285::MPSTR_MP285VerLabel).c_str();
    // const char* sVersion = MP285::Instance()->GetMPStr(MP285::MPSTR_MP285Version).c_str();
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_MP285VerLabel).c_str(), MP285::Instance()->GetMPStr(MP285::MPSTR_MP285Version).c_str(), MM::String, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_MP285VerLabel).c_str() << " = " << MP285::Instance()->GetMPStr(MP285::MPSTR_MP285Version).c_str() << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    char sTimeoutInterval[20];
    memset(sTimeoutInterval, 0, 20);
    sprintf(sTimeoutInterval, "%d", MP285::Instance()->GetTimeoutInterval());

    CPropertyAction* pActOnTimeoutInterval = new CPropertyAction(this, &MP285Ctrl::OnTimeoutInterval);
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutInterval).c_str(), sTimeoutInterval, MM::Integer,  false, pActOnTimeoutInterval); 

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutInterval).c_str() << " = " << sTimeoutInterval << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    char sTimeoutTrys[20];
    memset(sTimeoutTrys, 0, 20);
    sprintf(sTimeoutTrys, "%d", MP285::Instance()->GetTimeoutTrys());

    CPropertyAction* pActOnTimeoutTrys = new CPropertyAction(this, &MP285Ctrl::OnTimeoutTrys);
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutTrys).c_str(), sTimeoutTrys, MM::Integer,  false, pActOnTimeoutTrys); 

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutTrys).c_str() << " = " << sTimeoutTrys << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;


    // Read status data
    unsigned int nLength = 256;
    unsigned char sResponse[256];
    ret = CheckStatus(sResponse, nLength);

    if (ret != DEVICE_OK) return ret;

    bool yCommError = CheckError(sResponse[0]);

    char sCommStat[30];
    if (yCommError)
        sprintf((char*)sCommStat, "Error Code ==> <%2x>", sResponse[0]);
    else
        strcpy(sCommStat, "Success");
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_CommStateLabel).c_str(), sCommStat, MM::String, true);


     // Firmware Version data is started at the 29th byte and 2 bytes long
    char sFirmVersion[20];
    memset(sFirmVersion, 0, 20);
    if (!yCommError)
    {
        unsigned int nFirmVersion = sResponse[31] * 256 + sResponse[30];
        sprintf(sFirmVersion, "%d", nFirmVersion);
    }
    else
    {
        strcpy(sFirmVersion, "Undefined");
    }

    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_FirmwareVerLabel).c_str(), sFirmVersion, MM::String, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_FirmwareVerLabel).c_str() << " = " << sFirmVersion << "), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    // Create read-only property with um unit information
    // unit information started at the 23rd byte and 2 bytes long
    char sUm2UStepUnit[20];
    memset(sUm2UStepUnit, 0, 20);
    unsigned int nUm2UStepUnit = MP285::Instance()->GetUm2UStep();
    if (!yCommError)
    {
        nUm2UStepUnit = sResponse[25] * 256 + sResponse[24];
        MP285::Instance()->SetUm2UStep(nUm2UStepUnit);
    }
    sprintf(sUm2UStepUnit, "%d", nUm2UStepUnit);

    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_Um2UStepUnit).c_str(), sUm2UStepUnit, MM::Integer, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_Um2UStepUnit).c_str() << " = s[" << sUm2UStepUnit << "] :: n[" << nUm2UStepUnit << "] :: m[" << MP285::GetUm2UStep() << "]), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    // Create read-only property with nm unit information
    // unit information started at the 25th byte and 2 bytes long
    char sUStep2NmUnit[20];
    unsigned int nUStep2NmUnit = MP285::Instance()->GetUStep2Nm();
    memset(sUStep2NmUnit, 0, 20);
    if (!yCommError)
    {
        nUStep2NmUnit = sResponse[27] * 256 + sResponse[26];
        MP285::Instance()->SetUStep2Nm(nUStep2NmUnit);
    }
    sprintf(sUStep2NmUnit, "%d", nUStep2NmUnit);

    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_UStep2NmUnit).c_str(), sUStep2NmUnit, MM::Integer, true);

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_UStep2NmUnit).c_str() << " = s[" << sUStep2NmUnit << "] :: n[" << nUStep2NmUnit << "] :: m[" << MP285::GetUStep2Nm() << "]), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    // Speed (in mm/s)
    // -----------------
    // Get current speed from the controller
    // Speed information started at the 27th byte and 2 bytes long
    char sVelocity[20];
    memset(sVelocity, 0, 20);
    long lVelocity = MP285::Instance()->GetVelocity();
    if (!yCommError)
    {
        lVelocity = (sResponse[29] & 0x7F) * 256 + sResponse[28];
        MP285::Instance()->SetVelocity(lVelocity);
    }
    sprintf(sVelocity, "%ld", lVelocity);

    CPropertyAction* pActOnSpeed = new CPropertyAction(this, &MP285Ctrl::OnSpeed);
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str(), sVelocity, MM::Integer,  false, pActOnSpeed); // usteps/step
    //ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str(), sVelocity, MM::Integer,  true); // usteps/step

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str() << " = s[ << " << sVelocity << "] :: n[" << lVelocity << "] :: m[" << MP285::GetVelocity() << "]), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    //SetPropertyLimits(MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str(), 0.0, 40000.0);   
    //if (ret != DEVICE_OK) return ret;

    char sResolution[20];
    memset(sResolution, 0, 20);
    unsigned int nResolution = MP285::Instance()->GetResolution();
    if (!yCommError)
    {
        nResolution = (sResponse[29]&0x80) ? 50 : 10;
        MP285::Instance()->SetResolution(nResolution);    
    }
    sprintf(sResolution, "%d", nResolution);

    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_ResolutionLabel).c_str(), sResolution, MM::Integer, true);  // 0x0000 = 10 ; 0x8000 = 50

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_ResolutionLabel).c_str() << " = s[" << sResolution << "] :: n[" << nResolution << "] :: m[" << MP285::GetResolution() << "]), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    MP285::Instance()->SetMotionMode(0);
    char sMotionMode[20];
    memset(sMotionMode, 0, 20);
    sprintf(sMotionMode, "0");

    CPropertyAction* pActOnMotionMode = new CPropertyAction(this, &MP285Ctrl::OnMotionMode);
    ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_MotionMode).c_str(), "Undefined", MM::Integer, false, pActOnMotionMode);  // Absolute  vs Relative 
    //ret = CreateProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_MotionMode).c_str(), sMotionMode, MM::Integer, true);  // Absolute  vs Relative 

    osMessage.str("");
    osMessage << "<MP285Ctrl::Initialize> CreateProperty(" << MP285::Instance()->GetMPStr(MP285::MPSTR_MotionMode).c_str() << " = Undefined), ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK)  return ret;

    //SetPropertyLimits(MP285::Instance()->GetMPStr(MP285::MPSTR_MotionMode).c_str(), 0.0, 30000.0); // 0 : absolute , 1 : = relative    
    //if (ret != DEVICE_OK) return ret;

    ret = UpdateStatus();
    if (ret != DEVICE_OK) return ret;

    m_yInitialized = true;
    MP285::Instance()->SetDeviceAvailable(true);

    return DEVICE_OK;
}

//
// check controller's status bytes
//
int MP285Ctrl::CheckStatus(unsigned char* sResponse, unsigned int nLength)
{
    std::ostringstream osMessage;
    unsigned char sCommand[6] = { 0x73, MP285::MP285_TxTerm, 0x0A, 0x00, 0x00, 0x00 };
    int ret = WriteCommand(sCommand, 3);

    if (ret != DEVICE_OK) return ret;

    //unsigned int nBufLen = 256;
    //unsigned char sAnswer[256];
    memset(sResponse, 0, nLength);
    ret = ReadMessage(sResponse, 34);
    osMessage << "<MP285Ctrl::CheckStatus::ReadMessage> (ReturnCode = " << ret << ")";
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

//
// shutdown the controller
//
int MP285Ctrl::Shutdown()
{ 
    m_yInitialized = false;
    MP285::Instance()->SetDeviceAvailable(false);
    return DEVICE_OK;
}

//////////////// Action Handlers (Hub) /////////////////

//
// check for valid communication port
//
int MP285Ctrl::OnPort(MM::PropertyBase* pProp, MM::ActionType pAct)
{
    std::ostringstream osMessage;
    if (pAct == MM::BeforeGet)
    {
        osMessage << "<MP285Ctrl::OnPort> (BeforeGet::PORT=<" << MP285::Instance()->GetSerialPort().c_str() << ">";
        pProp->Set(MP285::Instance()->GetSerialPort().c_str());
        osMessage << " PROPSET=<" << MP285::Instance()->GetSerialPort().c_str() << ">)";
    }
    else if (pAct == MM::AfterSet)
    {
        osMessage << "<MP285Ctrl::OnPort> (AfterSet::PORT=<" << MP285::Instance()->GetSerialPort().c_str() << ">";
        if (m_yInitialized)
        {
            pProp->Set(MP285::Instance()->GetSerialPort().c_str());
            osMessage << "Initialized::SET=<" << MP285::Instance()->GetSerialPort().c_str() << ">";
            return DEVICE_INVALID_INPUT_PARAM;
        }
        pProp->Get(MP285::Instance()->GetSerialPort());
        osMessage << " SPROPGET=<" << MP285::Instance()->GetSerialPort().c_str() << ">)";
    }
    this->LogMessage(osMessage.str().c_str());
    return DEVICE_OK;
}


//
// Set current position as origin (0,0) coordinate of the controller.
//
int MP285Ctrl::SetOrigin()
{
    unsigned char sCommand[6] = { 0x6F, MP285::MP285_TxTerm, 0x0A, 0x00, 0x00, 0x00 };
    int ret = WriteCommand(sCommand, 3);

    std::ostringstream osMessage;
    osMessage << "<MP285::MP285::SetOrigin> (ReturnCode=" << ret << ")";
    this->LogMessage(osMessage.str().c_str());

    if (ret!=DEVICE_OK) return ret;

    unsigned char sResponse[64];

    memset(sResponse, 0, 64);
    ret = ReadMessage(sResponse, 2);
    osMessage << "<MP285Ctrl::CheckStatus::SetOrigin> (ReturnCode = " << ret << ")";
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    bool yCommError = CheckError(sResponse[0]);

    char sCommStat[30];
    if (yCommError)
        sprintf(sCommStat, "Error Code ==> <%2x>", sResponse[0]);
    else
        strcpy(sCommStat, "Success");

    ret = SetProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_CommStateLabel).c_str(), sCommStat);

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

//
// Set velocity.
//
int MP285Ctrl::SetVelocity(long lVelocity)
{
    std::ostringstream osMessage;
    unsigned char sCmdSet[6] = { 0x56, 0x00, 0x00, MP285::MP285_TxTerm, 0x0A, 0x00 };
    unsigned char sResponse[64];
    int ret = DEVICE_OK;
    char sCommStat[30];
    bool yCommError = false;

    if (MP285::Instance()->GetResolution() == 50)
        lVelocity = (lVelocity & 0x7FFF) | 0x8000;
    else
        lVelocity = lVelocity & 0x7FFF;

    sCmdSet[1] = (unsigned char)((lVelocity & 0xFF00) / 256);
    sCmdSet[2] = (unsigned char)(lVelocity & 0xFF);
        
    ret = WriteCommand(sCmdSet, 5);

    osMessage << "<MP285Ctrl::SetVelocity> = " << lVelocity << ", ReturnCode = " << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    ret = ReadMessage(sResponse, 2);

    if (ret != DEVICE_OK) return ret;

    MP285::Instance()->SetVelocity(lVelocity);

    yCommError = CheckError(sResponse[0]);
    if (yCommError)
        sprintf((char*)sCommStat, "Error Code ==> <%2x>", sResponse[0]);
    else
        strcpy(sCommStat, "Success");

    ret = SetProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_CommStateLabel).c_str(), sCommStat);

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

//
// Set Motion Mode
//
int MP285Ctrl::SetMotionMode(long lMotionMode)
{
    std::ostringstream osMessage;
    unsigned char sCommand[6] = { 0x00, MP285::MP285_TxTerm, 0x0A, 0x00, 0x00, 0x00 };
    unsigned char sResponse[64];
    int ret = DEVICE_OK;
    char sCommStat[30];
    bool yCommError = false;
        
    if (lMotionMode == 0)
        sCommand[0] = 'a';
    else
        sCommand[0] = 'b';

    ret = WriteCommand(sCommand, 3);

    osMessage << "<MP285Ctrl::SetMotionMode> = [" << lMotionMode << "," << sCommand[0] << "], Returncode =" << ret;
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    ret = ReadMessage(sResponse, 2);

    if (ret != DEVICE_OK) return ret;

    MP285::Instance()->SetMotionMode(lMotionMode);

    yCommError = CheckError(sResponse[0]);
    if (yCommError)
        sprintf((char*)sCommStat, "Error Code ==> <%2x>", sResponse[0]);
    else
        strcpy((char*)sCommStat, "Success");

    ret = SetProperty(MP285::Instance()->GetMPStr(MP285::MPSTR_CommStateLabel).c_str(), (const char*)sCommStat);

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

//
// stop and interrupt Z stage motion
//
int MP285Ctrl::Stop()
{
    unsigned char sCommand[6] = { 0x03, MP285::MP285_TxTerm, 0x0A, 0x00, 0x00, 0x00 };

    int ret = WriteCommand(sCommand, 3);

    ostringstream osMessage;
    osMessage << "<MP285::MP285Ctrl::Stop> (ReturnCode = " << ret << ")";
    this->LogMessage(osMessage.str().c_str());

    return ret;
}

/*
 * Speed as returned by device is in um/s
 */
int MP285Ctrl::OnSpeed(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    std::ostringstream osMessage;
    int ret = DEVICE_OK;
    long lVelocity = MP285::Instance()->GetVelocity();


    if (eAct == MM::BeforeGet)
    {
        pProp->Set(lVelocity);

        osMessage << "<MP285Ctrl::OnSpeed> BeforeGet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str() << " = [" << lVelocity << "], ReturnCode = " << ret;
    }
    else if (eAct == MM::AfterSet)
    {
        pProp->Get(lVelocity);

        ret = SetVelocity(lVelocity);

        osMessage << "<MP285Ctrl::OnSpeed> AfterSet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_VelocityLabel).c_str() << " = [" << lVelocity << "], ReturnCode = " << ret;
    }
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

/*
 * Speed as returned by device is in um/s
 */
int MP285Ctrl::OnMotionMode(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    std::string sMotionMode;
    std::ostringstream osMessage;
    long lMotionMode = (long)MP285::Instance()->GetMotionMode();
    int ret = DEVICE_OK;

    if (eAct == MM::BeforeGet)
    {
        pProp->Set(lMotionMode);

        osMessage << "<MP285Ctrl::OnMotionMode> BeforeGet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_MotionMode).c_str() << " = " << lMotionMode << "), ReturnCode = " << ret;
    }
    else if (eAct == MM::AfterSet)
    {
        pProp->Get(lMotionMode);

        ret = SetMotionMode(lMotionMode);

        osMessage << "<MP285Ctrl::OnSpeed> AfterSet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_ResolutionLabel).c_str() << " = " << lMotionMode <<  "), ReturnCode = " << ret;
    }
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

/*
 * Set/Get Timeout Interval
 */
int MP285Ctrl::OnTimeoutInterval(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    std::ostringstream osMessage;
    long lTimeoutInterval = (long)MP285::Instance()->GetTimeoutInterval();
    int ret = DEVICE_OK;

    if (eAct == MM::BeforeGet)
    {
        pProp->Set(lTimeoutInterval);

        osMessage << "<MP285Ctrl::OnTimeoutInterval> BeforeGet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutInterval).c_str() << " = " << lTimeoutInterval << "), ReturnCode = " << ret;
    }
    else if (eAct == MM::AfterSet)
    {
        pProp->Get(lTimeoutInterval);
        MP285::Instance()->SetTimeoutInterval((int)lTimeoutInterval);

        osMessage << "<MP285Ctrl::OnTimeoutInterval> AfterSet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutInterval).c_str() << " = " << MP285::Instance()->GetTimeoutInterval() <<  "), ReturnCode = " << ret;
    }
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

/*
 * Set/Get Timeout Trys
 */
int MP285Ctrl::OnTimeoutTrys(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    std::ostringstream osMessage;
    long lTimeoutTrys = (long)MP285::Instance()->GetTimeoutTrys();
    int ret = DEVICE_OK;

    if (eAct == MM::BeforeGet)
    {
        pProp->Set(lTimeoutTrys);

        osMessage << "<MP285Ctrl::OnTimeoutTrys> BeforeGet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutTrys).c_str() << " = " << lTimeoutTrys << "), ReturnCode = " << ret;
    }
    else if (eAct == MM::AfterSet)
    {
        pProp->Get(lTimeoutTrys);
        MP285::Instance()->SetTimeoutTrys((int)lTimeoutTrys);

        osMessage << "<MP285Ctrl::OnTimeoutTrys> AfterSet(" << MP285::Instance()->GetMPStr(MP285::MPSTR_TimeoutTrys).c_str() << " = " << MP285::Instance()->GetTimeoutTrys() <<  "), ReturnCode = " << ret;
    }
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

///////////////////////////////////////////////////////////////////////////////
// Internal, helper methods
///////////////////////////////////////////////////////////////////////////////

//
// Write a coomand to serial port
//
int MP285Ctrl::WriteCommand(unsigned char* sCommand, int nLength)
{
    int ret = DEVICE_OK;
    ostringstream osMessage;
    osMessage << "<MP285Ctrl::WriteCommand> (Command=";
    char sHex[4] = { NULL, NULL, NULL, NULL };
    for (int nBytes = 0; nBytes < nLength && ret == DEVICE_OK; nBytes++)
    {
        ret = WriteToComPort(MP285::Instance()->GetSerialPort().c_str(), (const unsigned char*)&sCommand[nBytes], 1);
        MP285::Instance()->Byte2Hex((const unsigned char)sCommand[nBytes], sHex);
        osMessage << "[" << nBytes << "]=<" << sHex << ">";
        CDeviceUtils::SleepMs(1);
    }
    osMessage << ")";
    this->LogMessage(osMessage.str().c_str());

    if (ret != DEVICE_OK) return ret;

    return DEVICE_OK;
}

//
// Read a message from serial port
//
int MP285Ctrl::ReadMessage(unsigned char* sResponse, int nBytesRead)
{
    // block/wait for acknowledge, or until we time out;
    unsigned int nLength = 256;
    unsigned char sAnswer[256];
    memset(sAnswer, 0, nLength);
    unsigned long lRead = 0;
    unsigned long lStartTime = GetClockTicksUs();

    ostringstream osMessage;
    char sHex[4] = { NULL, NULL, NULL, NULL };
    int ret = DEVICE_OK;
    bool yRead = false;
    bool yTimeout = false;
    while (!yRead && !yTimeout && ret == DEVICE_OK )
    {
        unsigned long lByteRead;

        const MM::Device* pDevice = this;
        ret = (GetCoreCallback())->ReadFromSerial(pDevice, MP285::Instance()->GetSerialPort().c_str(), (unsigned char *)&sAnswer[lRead], (unsigned long)nLength-lRead, lByteRead);
       
        osMessage.str("");
        osMessage << "<MP285Ctrl::ReadMessage> (ReadFromSerial = (" << nBytesRead << "," << lRead << "," << lByteRead << ")::<";
        for (unsigned long lIndx=0; lIndx < lByteRead; lIndx++)
        {
            // convert to hext format
            MP285::Instance()->Byte2Hex(sAnswer[lRead+lIndx], sHex);
            osMessage << "[" << sHex  << "]";
        }
        osMessage << ">";
        this->LogMessage(osMessage.str().c_str());

        // concade new string
        lRead += lByteRead;

        if (lRead > 2)
        {
            yRead = (sAnswer[0] == 0x30 || sAnswer[0] == 0x31 || sAnswer[0] == 0x32 || sAnswer[0] == 0x34 || sAnswer[0] == 0x38) &&
                    (sAnswer[1] == 0x0D) &&
                    (sAnswer[2] == 0x0D);
        }

        yRead = yRead || (lRead >= (unsigned long)nBytesRead);

        if (yRead) break;
        
        // check for timeout
        yTimeout = ((double)(GetClockTicksUs() - lStartTime) / 1000.) > (double) m_nAnswerTimeoutMs;
        if (!yTimeout) CDeviceUtils::SleepMs(3);

        osMessage.str("");
        osMessage << "<MP285Ctrl::ReadMessage> (ReadFromSerial = (" << nBytesRead << "," << lRead << "," << yRead << yTimeout << ")";
        this->LogMessage(osMessage.str().c_str());

    }

    // block/wait for acknowledge, or until we time out
    // if (!yRead || yTimeout) return DEVICE_SERIAL_TIMEOUT;
    // MP285::Instance()->ByteCopy(sResponse, sAnswer, nBytesRead);
    // if (checkError(sAnswer[0])) ret = DEVICE_SERIAL_COMMAND_FAILED;

    osMessage.str("");
    osMessage << "<MP285Ctrl::ReadMessage> (ReadFromSerial = <";
    for (unsigned long lIndx=0; lIndx < (unsigned long)nBytesRead; lIndx++)
    {
        sResponse[lIndx] = sAnswer[lIndx];
        MP285::Instance()->Byte2Hex(sResponse[lIndx], sHex);
        osMessage << "[" << sHex  << ",";
        MP285::Instance()->Byte2Hex(sAnswer[lIndx], sHex);
        osMessage << sHex  << "]";
    }
    osMessage << ">";
    this->LogMessage(osMessage.str().c_str());

    return DEVICE_OK;
}

//
// check the error code for the message returned from serial communivation
//
bool MP285Ctrl::CheckError(unsigned char bErrorCode)
{
    // if the return message is 2 bytes message including CR
    unsigned int nErrorCode = 0;
    ostringstream osMessage;

    // check 4 error code
    if (bErrorCode == MP285::MP285_SP_OVER_RUN)
    {
        // Serial command buffer over run
        nErrorCode = MPError::MPERR_SerialOverRun;       
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }
    else if (bErrorCode == MP285::MP285_FRAME_ERROR)
    {
        // Receiving serial command time out
        nErrorCode = MPError::MPERR_SerialTimeout;       
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }
    else if (bErrorCode == MP285::MP285_BUFFER_OVER_RUN)
    {
        // Serial command buffer full
        nErrorCode = MPError::MPERR_SerialBufferFull;       
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }
    else if (bErrorCode == MP285::MP285_BAD_COMMAND)
    {
        // Invalid serial command
        nErrorCode = MPError::MPERR_SerialInpInvalid;       
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }
    else if (bErrorCode == MP285::MP285_MOVE_INTERRUPTED)
    {
        // Serial command interrupt motion
        nErrorCode = MPError::MPERR_SerialIntrupMove;       
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }
    else if (bErrorCode == 0x00)
    {
        // No response from serial port
        nErrorCode = MPError::MPERR_SerialZeroReturn;
        osMessage << "<MP285Ctrl::checkError> ErrorCode=[" << MPError::Instance()->GetErrorText(nErrorCode).c_str() << "])";
    }

    this->LogMessage(osMessage.str().c_str());
    return (nErrorCode!=0);
}


