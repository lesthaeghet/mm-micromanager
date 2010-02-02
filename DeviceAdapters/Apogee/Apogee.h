///////////////////////////////////////////////////////////////////////////////
// FILE:          Apogee.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Apogee camera module
//                
// AUTHOR:        Bob Dougherty <bob@vischeck.com> 
// COPYRIGHT:     Bob Dougherty and Apogee Instruments, California, 2008
// LICENSE:       This file is distributed under the LGPL license.
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
// CVS:           $Id: $
//
#ifndef _APOGEE_H_
#define _APOGEE_H_

#include "../../MMDevice/DeviceBase.h"
#include "../../MMDevice/ImgBuffer.h"
#include "../../MMDevice/DeviceUtils.h"
#include <string>
#include <map>

// Include the Apogee Alta driver:
#import "Apogee.DLL" no_namespace

#define ERR_UNKNOWN_CAMERA_TYPE  11

// External names used by the rest of the system to load particular devices from ApogeeCam.dll. 
const char* g_CameraDeviceName =    "ApogeeCamera";

//////////////////////////////////////////////////////////////////////////////
// Implementation of the MMDevice and MMCamera interfaces
//
class CApogeeCamera : public CCameraBase<CApogeeCamera>
{
public:
    CApogeeCamera();
    ~CApogeeCamera();

    // MMDevice API
    // ------------
    int Initialize();
    int Shutdown();

    // return the name we use for referring to this device adapter:
    void GetName(char* name) const {CDeviceUtils::CopyLimitedString(name, g_CameraDeviceName);}
    // return true if device processing async command:
    bool Busy() { return m_bBusy; } 

    // MMCamera API
    // ------------
    int SnapImage();
    const unsigned char* GetImageBuffer();
    unsigned GetImageWidth() const;
    unsigned GetImageHeight() const;
    unsigned GetImageBytesPerPixel() const; 
    unsigned GetBitDepth() const;
    long GetImageBufferSize() const;
    double GetExposure() const;
    void SetExposure(double exp);
    int SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize); 
    int GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize); 
    int ClearROI();
    double GetNominalPixelSizeUm() const {return nominalPixelSizeUm_;}
    double GetPixelSizeUm() const {return nominalPixelSizeUm_ * GetBinning();}
    int GetBinning() const;
    int SetBinning(int binSize);
    //int StartSequenceAcquisition(long numImages, double /*interval_ms*/, bool stopOnOverflow);
    //int StopSequenceAcquisition();
    //bool IsCapturing();

    // action interface
    int OnCameraInterface(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCameraIdOne(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCameraIdTwo(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCCDType(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCCDPixSizeX(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCCDPixSizeY(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnExposure(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnXBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnYBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnGain(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnOffset(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnLightMode(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCameraTemperatureSetPoint(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCameraTemperatureBackoffPoint(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCCDTemperature(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnHeatsinkTemperature(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCoolerDriveLevel(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCoolerFanMode(MM::PropertyBase* pProp, MM::ActionType eAct);
    int OnCoolerEnable(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnCoolerStatus(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin1Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin2Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin3Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin4Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin5Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnIoPin6Mode(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnShutterMode(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
	ICamera2Ptr AltaCamera;		// Camera interface

    int ResizeImageBuffer();

	int OnIoPinMode(MM::PropertyBase* pProp, MM::ActionType eAct, long mask, const char *altStr);
	int OnIoPortDirMode(MM::PropertyBase* pProp, MM::ActionType eAct, long mask);

	ImgBuffer img_;
	int pixelDepth_;
	double m_dExposure; 
	bool m_bBusy;
	bool m_bInitialized;

	 // Apogee data
    long m_nInterfaceType;
    long m_nCamIdOne;
    long m_nCamIdTwo;
    bool m_nLightImgMode;
	double nominalPixelSizeUm_;
	// We store the unbinned pixel positions of the ROI
    long m_roiX;
    long m_roiY;
    long m_roiH;
	long m_roiV;
};


#endif //_APOGEE_H_
