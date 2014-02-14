///////////////////////////////////////////////////////////////////////////////
// FILE:          PVCAMAdapter.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   PVCAM camera module
//                
// AUTHOR:        Nico Stuurman, Nenad Amodaj nenad@amodaj.com, 09/13/2005
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
//                Micromax compatible adapter is moved to PVCAMPI project, N.A. 10/2007
//
// CVS:           $Id: PVCAM.h 8240 2011-12-04 01:05:17Z nico $

#ifndef _PVCAMADAPTER_H_
#define _PVCAMADAPTER_H_

#include "DeviceBase.h"
#include "../../MMDevice/ImgBuffer.h"
#include "../../MMDevice/Debayer.h"
#include "../../MMDevice/DeviceUtils.h"
#include "../../MMDevice/DeviceThreads.h"

#ifdef WIN32
#pragma warning(push)
#include "../../../3rdpartypublic/Photometrics/PVCAM/SDK/Headers/master.h"
#include "../../../3rdpartypublic/Photometrics/PVCAM/SDK/Headers/pvcam.h"
#pragma warning(pop)
#endif

#ifdef __APPLE__
#define __mac_os_x
#include <PVCAM/master.h>
#include <PVCAM/pvcam.h>
#endif

#ifdef linux
#include <pvcam/master.h>
#include <pvcam/pvcam.h>
#endif

#if(WIN32 && NDEBUG)
   WINBASEAPI
   BOOL
   WINAPI
   TryEnterCriticalSection(
      __inout LPCRITICAL_SECTION lpCriticalSection
    );
#endif

#ifdef WIN32
// FRAME_INFO is currently supported on Windows only (PVCAM 2.9.5+)
#define PVCAM_FRAME_INFO_SUPPORTED
// Callbacks are not supported on Linux and Mac (as for 01/2014)
#define PVCAM_CALLBACKS_SUPPORTED
// The new parameter is implmented in PVCAM for Windows only (PVCAM 3+)
#define PVCAM_PARAM_EXPOSE_OUT_DEFINED
#endif

#include <string>
#include <map>

//////////////////////////////////////////////////////////////////////////////
// Error codes
//
#define ERR_INVALID_BUFFER            10002
#define ERR_INVALID_PARAMETER_VALUE   10003
#define ERR_BUSY_ACQUIRING            10004
#define ERR_STREAM_MODE_NOT_SUPPORTED 10005
#define ERR_CAMERA_NOT_FOUND          10006
#define ERR_ROI_SIZE_NOT_SUPPORTED    10007

/***
* User selected region of interest
*/
struct ROI {
   uns16 x;
   uns16 newX;
   uns16 y;
   uns16 newY;
   uns16 xSize;
   uns16 newXSize;
   uns16 ySize;
   uns16 newYSize;
   uns16 binXSize;
   uns16 binYSize;

   // added this function to the ROI struct because it only applies to this data structure,
   //  and nothing else.
   void PVCAMRegion(uns16 x_, uns16 y_, uns16 xSize_, uns16 ySize_, \
                    unsigned binXSize_, unsigned binYSize_, rgn_type &newRegion)
   {
      // set to full frame
      x = x_;
      y = y_;
      xSize = xSize_;
      ySize = ySize_;

      // set our member binning information
      binXSize = (uns16) binXSize_;
      binYSize = (uns16) binYSize_;

      // save ROI-related dimentions into other data members
      newX = x/binXSize;
      newY = y/binYSize;
      newXSize = xSize/binXSize;
      newYSize = ySize/binYSize;

      // round the sizes to the proper devisible boundaries
      x = newX * binXSize;
      y = newY * binYSize;
      xSize = newXSize * binXSize;
      ySize = newYSize * binYSize;

      // set PVCAM-specific region
      newRegion.s1 = x;
      newRegion.s2 = x + xSize-1;
      newRegion.sbin = binXSize;
      newRegion.p1 = y;
      newRegion.p2 = y + ySize-1;
      newRegion.pbin = binYSize;
   }
};

/***
* Struct used for Universal Parameters definition
*/
typedef struct 
{
   const char * name;
   uns32 id;
} ParamNameIdPair;

/***
* Speed table row
*/
typedef struct
{
    uns16 pixTime;         // Readout rate in ns
    int16 bitDepth;        // Bit depth
    int16 gainMin;         // Min gain index for this speed
    int16 gainMax;         // Max gain index for this speed
    int16 spdIndex;        // Speed index 
    uns32 portIndex;       // Port index
    std::string spdString; // A string that describes this choice in GUI
} SpdTabEntry;

class AcqSequenceThread;
template<class T> class PvParam;
class PvUniversalParam;
class PvEnumParam;

/***
* Class used by post processing, a list of these elements is built up one for each post processing function
* so the call back function in CPropertyActionEx can get to information about that particular feature in
* the call back function
*/ 
class PProc 
{

public:

   PProc(std::string name = "", int ppIndex = -1, int propIndex = -1)
   {
      mName = name, mppIndex = ppIndex, mpropIndex = propIndex, mcurValue = ppIndex;
   }
   std::string GetName()        { return mName; }
   int         GetppIndex()     { return mppIndex; }
   int         GetpropIndex()   { return mpropIndex; }
   int         GetRange()       { return mRange; }
   double      GetcurValue()    { return mcurValue; }
   void        SetName(std::string name)    { mName      = name; }
   void        SetppIndex(int ppIndex)      { mppIndex   = ppIndex; }
   void        SetpropInex(int propIndex)   { mpropIndex = propIndex; }
   void        SetcurValue(double curValue) { mcurValue  = curValue; }
   void        SetRange(int range)          { mRange     = range; }

   void SetPostProc(PProc& tmp)
   {
      mName = tmp.GetName(), mppIndex = tmp.GetppIndex(), mpropIndex = tmp.GetpropIndex();
   }

protected:

   std::string mName;
   int         mppIndex;
   int         mpropIndex;
   double      mcurValue;
   int         mRange;

};

/***
* Implementation of the MMDevice and MMCamera interfaces for all PVCAM cameras
*/
class Universal : public CCameraBase<Universal>
{

public:
   
   Universal(short id);
   ~Universal();

   // MMDevice API
   int  Initialize();
   int  Shutdown();
   void GetName(char* pszName) const;
   bool Busy();
   bool GetErrorText(int errorCode, char* text) const;

   // MMCamera API
   int SnapImage();
   const unsigned char* GetImageBuffer();
   const unsigned int* GetImageBufferAsRGB32();
   unsigned GetImageWidth() const         { return img_.Width(); }
   unsigned GetImageHeight() const        { return img_.Height(); }
   unsigned GetImageBytesPerPixel() const { return rgbaColor_ ? colorImg_.Depth() : img_.Depth(); } 
   long GetImageBufferSize() const;
   unsigned GetBitDepth() const;
   int GetBinning() const;
   int SetBinning(int binSize);
   double GetExposure() const;
   void SetExposure(double dExp);
   int IsExposureSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }
   unsigned GetNumberOfComponents() const {return rgbaColor_ ? 4 : 1;}

#ifndef linux
   // micromanager calls the "live" acquisition a "sequence"
   //  don't get this confused with a PVCAM sequence acquisition, it's actually circular buffer mode
   int PrepareSequenceAcqusition();
   int StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow);
   int StopSequenceAcquisition();
#endif

   // action interface
   int OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBinningX(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBinningY(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnExposure(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnGain(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnReadoutRate(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnReadNoiseProperties(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnMultiplierGain(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnReadoutPort(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTemperature(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTemperatureSetPoint(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnUniversalProperty(MM::PropertyBase* pProp, MM::ActionType eAct, long index);
#ifdef WIN32 //This is only compiled for Windows at the moment
   int OnResetPostProcProperties(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPostProcProperties(MM::PropertyBase* pProp, MM::ActionType eAct, long index);
   int OnActGainProperties(MM::PropertyBase* pProp, MM::ActionType eAct);
#endif
   int OnTriggerMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnExposeOutMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTriggerTimeOut(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnOutputTriggerFirstMissing(MM::PropertyBase* pProp, MM::ActionType eAct); 
   int OnCircBufferFrameCount(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnColorMode(MM::PropertyBase* pProp, MM::ActionType eAct);
#ifdef PVCAM_CALLBACKS_SUPPORTED
   int OnAcquisitionMethod(MM::PropertyBase* pProp, MM::ActionType eAct);
#endif

   bool IsCapturing();

   // Published to allow other classes access the camera
   short Handle() { return hPVCAM_; }
   // Utility logging functions (published to allow usage from other classes)
   int16 LogCamError(int lineNr, std::string message="", bool debug=false) throw();
   int   LogMMError(int errCode, int lineNr, std::string message="", bool debug=false) const throw();
   void  LogMMMessage(int lineNr, std::string message="", bool debug=true) const throw();

protected:

#ifndef linux
   int ThreadRun(void);
   void OnThreadExiting() throw();
#endif

   int FrameDone();
   int BuildMetadata( Metadata& md );
   int PushImage(const unsigned char* pixBuffer, Metadata* pMd );

private:

   Universal(Universal&) {}
   int GetPvExposureSettings( int16& pvExposeOutMode, uns32& pvExposureValue );
   int ResizeImageBufferContinuous();
   int ResizeImageBufferSingle();
   bool WaitForExposureDone() throw();
   MM::MMTime GetCurrentTime() { return GetCurrentMMTime();}


   bool            initialized_;          // Driver initialization status in this class instance
   long            numImages_;            // Number of images to acquire
   long            curImageCnt_;          // Current number of images acquired
   short           hPVCAM_;               // Camera handle
   static int      refCount_;             // This class reference counter
   static bool     PVCAM_initialized_;    // Global PVCAM initialization status
   ImgBuffer       img_;                  // Single image buffer
   ImgBuffer       colorImg_;             // color image buffer
   Debayer         debayer_;              // debayer processor

   MM::MMTime      startTime_;            // Acquisition start time

   short           cameraId_;             // 0-based camera ID, used to allow multiple cameras connected
   unsigned char*  circBuffer_;           // a buffer used for pl_exp_start_cont
   unsigned long   circBufferSize_;       // total byte-wise size of the circular buffer
   long            circBufferFrameCount_; // number of frames to allocate the buffer for
   bool            stopOnOverflow_;       // Stop inserting images to MM buffer if it's full
   bool            snappingSingleFrame_;  // Single frame mode acquisition ongoing
   bool            singleFrameModeReady_; // Single frame mode acquisition prepared
   bool            sequenceModeReady_;    // Continuous acquisition prepared

   bool            isUsingCallbacks_;
   bool            isAcquiring_;

   long            triggerTimeout_;       // Max time to wait for an external trigger
   bool            microsecResSupported_; // True if camera supports microsecond exposures
#ifdef PVCAM_FRAME_INFO_SUPPORTED
   PFRAME_INFO     pFrameInfo_;           // PVCAM frame metadata
#endif
   friend class    AcqSequenceThread;
   AcqSequenceThread* uniAcqThd_;         // Pointer to the sequencing thread

   long            outputTriggerFirstMissing_;

   /// CAMERA PARAMETERS:
   ROI             roi_;                  // Current user-selected ROI
   rgn_type        camRegion_;            // Current PVCAM region based on ROI
   uns16           camParSize_;           // CCD parallel size
   uns16           camSerSize_;           // CCD serial size
   uns32           camFWellCapacity_;     // CCD full well capacity
   double          exposure_;             // Current Exposure
   unsigned        binSize_;              // Symmetric binning value
   unsigned        binXSize_;             // Asymmetric binning value
   unsigned        binYSize_;             // Asymmetric binning value

   // These are cached values for binning. Used when changing binning during live mode
   unsigned        newBinSize_;
   unsigned        newBinXSize_;
   unsigned        newBinYSize_;

   char            camName_[CAM_NAME_LEN];
   char            camChipName_[CCD_NAME_LEN];
   PvParam<int16>* prmTemp_;              // CCD temperature
   PvParam<int16>* prmTempSetpoint_;      // Desired CCD temperature
   PvParam<int16>* prmGainIndex_;
   PvParam<uns16>* prmGainMultFactor_;
   PvEnumParam*    prmTriggerMode_;       // (PARAM_EXPOSURE_MODE)
   PvParam<uns16>* prmExpResIndex_;
   PvEnumParam*    prmExpRes_;
   PvEnumParam*    prmExposeOutMode_;
   PvEnumParam*    prmReadoutPort_;
   PvEnumParam*    prmColorMode_;

   // color mode
   bool rgbaColor_;

   // List of post processing features
   std::vector<PProc> PostProc_;

   // Camera speed table
   //  usage: SpdTabEntry e = camSpdTable_[port][speed];
   std::map<uns32, std::map<int16, SpdTabEntry> > camSpdTable_;
   // Reverse speed table to get the speed based on UI selection
   //  usage: SpdTabEntry e = camSpdTableReverse_[port][ui_selected_string];
   std::map<uns32, std::map<std::string, SpdTabEntry> > camSpdTableReverse_;
   // Currently selected speed
   SpdTabEntry camCurrentSpeed_;

   // 'Universal' parameters
   std::vector<PvUniversalParam*> universalParams_;

   /// CAMERA PARAMETER initializers
   int initializeStaticCameraParams();
   int initializeUniversalParams();
   int initializePostProcessing();
   int refreshPostProcValues();
   int revertPostProcValue( long absoluteParamIdx, MM::PropertyBase* pProp);
   int buildSpdTable();
   int speedChanged();
   int portChanged();

   // other internal functions
   int ClearROI();
   int SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize); 
   int GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize);

private:

#ifdef PVCAM_CALLBACKS_SUPPORTED
   static void PvcamCallbackEofEx3( PFRAME_INFO pNewFrameInfo, void* pContext );
#endif

};

/***
 * Acquisition thread
 */
class AcqSequenceThread : public MMDeviceThreadBase
{
   public:
      AcqSequenceThread(Universal* camera) : 
         stop_(true), camera_(camera) {}
      ~AcqSequenceThread() {}
      int svc (void);

      void setStop(bool stop) {stop_ = stop;}
      bool getStop() {return stop_;}
      void Start() {stop_ = false; activate();}
    
   private:
      bool stop_;
      Universal* camera_;
};

#endif //_PVCAMADAPTER_H_
