///////////////////////////////////////////////////////////////////////////////
// FILE:          Andor.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Andor camera module 
//                
// AUTHOR:        Nenad Amodaj, nenad@amodaj.com, 06/30/2006
// COPYRIGHT:     University of California, San Francisco, 2006
//                100X Imaging Inc, 2008
//
// LICENSE:       This file is distributed under the BSD license.
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
// REVISIONS:     May 21, 2007, Jizhen Zhao, Andor Technologies
//                Temerature control and other additional related properties added,
//                gain bug fixed, refernce counting fixed for shutter adapter.
//
//				  May 23 & 24, 2007, Daigang Wen, Andor Technology plc added/modified:
//				  Cooler is turned on at startup and turned off at shutdown
//				  Cooler control is changed to cooler mode control
//				  Pre-Amp-Gain property is added
//				  Temperature Setpoint property is added
//				  Temperature is resumed as readonly
//				  EMGainRangeMax and EMGainRangeMin are added
//
// FUTURE DEVELOPMENT: From September 1 2007, the development of this adaptor is taken over by Andor Technology plc. Daigang Wen (d.wen@andor.com) is the main contact. Changes made by him will not be labeled.
//
// CVS:           $Id$
//
#ifndef _ANDOR_H_
#define _ANDOR_H_

#include "../../MMDevice/DeviceBase.h"
#include "../../MMDevice/MMDevice.h"
#include "../../MMDevice/ImgBuffer.h"
#include "../../MMDevice/DeviceUtils.h"
#include "../../MMDevice/DeviceThreads.h"
#include <string>
#include <sstream>
#include <map>

// error codes
#define ERR_BUFFER_ALLOCATION_FAILED 101
#define ERR_INCOMPLETE_SNAP_IMAGE_CYCLE 102
#define ERR_INVALID_ROI 103
#define ERR_INVALID_READOUT_MODE_SETUP 104
#define ERR_CAMERA_DOES_NOT_EXIST 105
#define ERR_BUSY_ACQUIRING 106
#define ERR_INVALID_PREAMPGAIN 107
#define ERR_INVALID_VSPEED 108
#define ERR_TRIGGER_NOT_SUPPORTED 109
#define ERR_OPEN_OR_CLOSE_SHUTTER_IN_ACQUISITION_NOT_ALLOWEDD 110
#define ERR_NO_AVAIL_AMPS 111
#define ERR_SOFTWARE_TRIGGER_IN_USE 112
#define ERR_INVALID_SHUTTER_OPENTIME 113
#define ERR_INVALID_SHUTTER_CLOSETIME 114
#define ERR_INVALID_SHUTTER_MODE 115
#define ERR_INVALID_SNAPIMAGEDELAY 116

class AcqSequenceThread;
 
//////////////////////////////////////////////////////////////////////////////
// Implementation of the MMDevice and MMCamera interfaces
//
class AndorCamera : public CCameraBase<AndorCamera>
{
public:
   friend class AcqSequenceThread;
   static AndorCamera* GetInstance();

   ~AndorCamera();
   
   // MMDevice API
   int Initialize();
   int Shutdown();
   
   void GetName(char* pszName) const;
   bool Busy() {return false;}
   
   // MMCamera API
   int SnapImage();
   const unsigned char* GetImageBuffer();
   unsigned GetImageWidth() const {return img_.Width();}
   unsigned GetImageHeight() const {return img_.Height();}
   unsigned GetImageBytesPerPixel() const {return img_.Depth();} 
   long GetImageBufferSize() const {return img_.Width() * img_.Height() * GetImageBytesPerPixel();}
   unsigned GetBitDepth() const;
   int GetBinning() const;
   int SetBinning(int binSize);
   double GetExposure() const;
   void SetExposure(double dExp);
   int SetROI(unsigned uX, unsigned uY, unsigned uXSize, unsigned uYSize); 
   int GetROI(unsigned& uX, unsigned& uY, unsigned& uXSize, unsigned& uYSize);
   int ClearROI();
   int IsExposureSequenceable(bool& isSequenceable) const {isSequenceable = false; return DEVICE_OK;}

   // high-speed interface
   int PrepareSequenceAcqusition()
   { 
      return DEVICE_OK; 
   }
   int StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow);
   /**
   * Continuous sequence acquisition.  
   * Default to sequence acquisition with a high number of images
   */
   int StartSequenceAcquisition(double interval)
   {
      return StartSequenceAcquisition(LONG_MAX, interval, false);
   }

   int StopSequenceAcquisition(); // temporary=true 
   int StopSequenceAcquisition(bool temporary);

   bool IsCapturing(){return sequenceRunning_;};

   // action interface for the camera
   int OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnExposure(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnGain(MM::PropertyBase* pProp, MM::ActionType eAct);   
   int OnEMSwitch(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnReadoutMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnReadoutTime(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnOffset(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTemperature(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnDriverDir(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnShutterMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnInternalShutterMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCooler(MM::PropertyBase* pProp, MM::ActionType eAct);// jizhen 05.11.2007
   int OnFanMode(MM::PropertyBase* pProp, MM::ActionType eAct);// jizhen 05.16.2007
   int OnTemperatureSetPoint(MM::PropertyBase* pProp, MM::ActionType eAct);// Daigang 23-May-2007  
   int OnEMGainRangeMax(MM::PropertyBase* pProp, MM::ActionType eAct);// Daigang 24-May-2007  
   int OnEMGainRangeMin(MM::PropertyBase* pProp, MM::ActionType eAct);// Daigang 24-May-2007  
   int OnPreAmpGain(MM::PropertyBase* pProp, MM::ActionType eAct);// Daigang 24-May-2007  
   int OnFrameTransfer(MM::PropertyBase* pProp, MM::ActionType eAct); 
   int OnVSpeed(MM::PropertyBase* pProp, MM::ActionType eAct);  
   int OnShutterOpeningTime(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSnapImageDelay(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSnapImageMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnShutterClosingTime(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnShutterTTL(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnOutputAmplifier(MM::PropertyBase* pProp, MM::ActionType eAct); 
   int OnADChannel(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCamera(MM::PropertyBase* pProp, MM::ActionType eAct);//for multiple camera support
   int OnCameraName(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OniCamFeatures(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTemperatureRangeMin(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTemperatureRangeMax(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnVCVoltage(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBaselineClamp(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCropModeSwitch(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCropModeWidth(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCropModeHeight(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnActualIntervalMS(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSelectTrigger(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTimeOut(MM::PropertyBase* pProp, MM::ActionType eAct);  // kdb July-30-2009
   int OnCountConvert(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCountConvertWavelength(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSpuriousNoiseFilter(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSpuriousNoiseFilterThreshold(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSpuriousNoiseFilterDescription(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnOptAcquireMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   void UpdateOAParams(const char*  OAModeName);
   int OnOADescription(MM::PropertyBase* pProp, MM::ActionType eAct);


   // custom interface for the thread
   int PushImage();

   //static void ReleaseInstance(AndorCamera * AndorCamera);

    int GetNumberOfWorkableCameras() const { return NumberOfWorkableCameras_; } 
    int GetMyCameraID() const { return myCameraID_; } 

private:
   AndorCamera();
   int ResizeImageBuffer();
   int StopCameraAcquisition();
   void UpdateEMGainRange();
   void CheckError(unsigned int errorVal);
   bool IsThermoSteady();
   void SetToIdle();
   bool IsAcquiring();

   void LogStatus();
	int PrepareSnap();
	unsigned int UpdateSnapTriggerMode();
   std::string GetTriggerModeString(int mode);
   unsigned int ApplyTriggerMode(int mode);
   int GetTriggerModeInt(std::string mode);

   bool EMSwitch_;

   unsigned int ui_swVersion;

   static AndorCamera* instance_;
   static unsigned refCount_;
   ImgBuffer img_;
   bool initialized_;
   bool snapInProgress_;
   bool sequenceRunning_;
   long imageCounter_;
   MM::MMTime startTime_;
   long imageTimeOut_ms_;
   long sequenceLength_;
   bool stopOnOverflow_;
   double intervalMs_;
   std::string countConvertMode_;
   double countConvertWavelength_;
   std::string spuriousNoiseFilter_;
   double spuriousNoiseFilterThreshold_;
   std::string spuriousNoiseFilterDescriptionStr_;
   std::string optAcquireModeStr_;
   std::string optAcquireDescriptionStr_;

   long lSnapImageCnt_;
   std::vector<std::string> PreAmpGains_;
   long currentGain_;   

   bool cropModeSwitch_;
   long currentCropWidth_;
   long currentCropHeight_;
   std::vector<std::string> VSpeeds_;


   double currentExpMS_;

   long ReadoutTime_, KeepCleanTime_;

   unsigned int UpdateTimings();
   unsigned int ApplyShutterSettings();

#ifdef __linux__
   HDEVMODULE hAndorDll; 
#endif

   bool busy_;

   struct ROI {
      int x;
      int y;
      int xSize;
      int ySize;

      ROI() : x(0), y(0), xSize(0), ySize(0) {}
      ~ROI() {}

      bool isEmpty() {return x==0 && y==0 && xSize==0 && ySize == 0;}
   };

   ROI roi_;
   int binSize_;
   double expMs_; //value used by camera
   std::string driverDir_;
   int fullFrameX_;
   int fullFrameY_;
   int tempFrameX_;
   int tempFrameY_;
   short* fullFrameBuffer_;
   std::vector<std::string> readoutModes_;

   int EmCCDGainLow_, EmCCDGainHigh_;   
   int minTemp_, maxTemp_;
   //Daigang 24-may-2007
   bool ThermoSteady_;

   AcqSequenceThread* seqThread_;

   bool bShuttersIndependant_;
   int ADChannelIndex_, OutputAmplifierIndex_;
   void UpdateHSSpeeds();
   int UpdateExposureFromCamera();
   int UpdatePreampGains();
   int GetPreAmpGainString(int PreAmpgainIdx, char * PreAmpGainString);

   int HSSpeedIdx_;
   int PreAmpGainIdx_;

   bool bSoftwareTriggerSupported_;
   int  iCurrentTriggerMode_;

   enum {
      INTERNAL=0,
      EXTERNAL=1,
      EXTERNALSTART=6,
      EXTERNALEXPOSURE=7,
      SOFTWARE=10,
      FASTEXTERNAL=101
   };

   enum {
      AUTO=0,
      OPEN=1,
      CLOSED=2
   };

   int iInternalShutterMode_;
   int iShutterMode_;
   int iShutterOpeningTime_;
   int iShutterClosingTime_;
   int iSnapImageDelay_;
   bool bSnapImageWaitForReadout_;
   int iShutterTTL_;

   at_32 myCameraID_;
   at_32 NumberOfAvailableCameras_;
   at_32 NumberOfWorkableCameras_;
   std::vector<std::string> cameraName_;
   std::vector<std::string> cameraSN_;
   std::vector<int> cameraID_;
   int GetListOfAvailableCameras();
   std::string CameraName_;
   std::string iCamFeatures_;
   std::string TemperatureRangeMin_;
   std::string TemperatureRangeMax_;
   std::string PreAmpGain_;
   std::string VSpeed_;
   std::string TemperatureSetPoint_;
   std::vector<std::string> VCVoltages_;
   std::string VCVoltage_;
   std::vector<std::string> BaselineClampValues_;
   std::string BaselineClampValue_;
   float ActualInterval_ms_;
   std::string ActualInterval_ms_str_;
   std::string strCurrentTriggerMode_;
   std::vector<std::string> vTriggerModes;

    std::string strCurrentAmp;
   std::vector<std::string> vAvailAmps;
   std::map<std::string, int> mapAmps;

   std::string strCurrentChannel;
   std::vector<std::string> vChannels;

   bool bFrameTransfer_;
   

   std::string m_str_frameTransferProp;
   std::string m_str_camType;
   std::vector<std::string> vCameraType;

   unsigned char* pImgBuffer_;
   unsigned char* GetAcquiredImage();
   std::string getCameraType();
   unsigned int createGainProperty(AndorCapabilities * caps);
   unsigned int createTriggerProperty(AndorCapabilities * caps);
   unsigned int createSnapTriggerMode();
   unsigned int createShutterProperty(AndorCapabilities * caps);
   unsigned int AddTriggerProperty(int mode);
   unsigned int createIsolatedCropModeProperty(AndorCapabilities * caps);

   bool mb_canUseFan;
   bool mb_canSetTemp;
   bool bEMGainSupported;

   bool sequencePaused_;

};


/**
 * Acquisition thread
 */
class AcqSequenceThread : public MMDeviceThreadBase
{
public:
   AcqSequenceThread(AndorCamera* pCam) : 
      intervalMs_(100.0), 
      numImages_(1),
      waitTime_(10),
      busy_(false), 
      stop_(false) 
   {
      camera_ = pCam;
   };
   ~AcqSequenceThread() {}
 
   int svc(void);

   void SetInterval(double intervalMs) {intervalMs_ = intervalMs;}
   void SetWaitTime (long waitTime) { waitTime_ = waitTime;}
   void SetTimeOut (long imageTimeOut) { imageTimeOut_ = imageTimeOut;}
   void SetLength(long images) {numImages_ = images;}
   void Stop() {stop_ = true;}
   void Start() {stop_ = false; activate();}

private:
   AndorCamera* camera_;
   double intervalMs_;
   long numImages_;
   long waitTime_;
   long imageTimeOut_;
   bool busy_;
   bool stop_;
};


class DriverGuard
{
public:
   DriverGuard(const AndorCamera * cam);
   ~DriverGuard();

};

#endif //_ANDOR_H_
