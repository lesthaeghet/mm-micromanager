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

#include <string>
#include <map>

#include "../../MMDevice/ImgBuffer.h"
#include "../../MMDevice/DeviceUtils.h"
#include "../../MMDevice/DeviceThreads.h"

#include "DeviceBase.h"
#include "PvDebayer.h"
#include "PVCAMIncludes.h"
#include "Event.h"

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
// The new parameter is implemented in PVCAM for Windows only (PVCAM 3+)
#define PVCAM_PARAM_EXPOSE_OUT_DEFINED
// The SMART streaming feature is currently only supported on Windows (PVCAM 2.8.0+)
#define PVCAM_SMART_STREAMING_SUPPORTED
// Metadata, Multi-ROI, Centroids and other features that were added to PVCAM 3.0.12 (Win)
#define PVCAM_3_0_12_SUPPORTED // TODO: Rename this once the PVCAM is officially out
#endif

// PVCAM 3.1+ has some additional PL_COLOR_MODES defined which we use across the code
// even if we don't compile against that PVCAM. To make it easier we define them ourselves.
#ifndef PVCAM_3_0_12_SUPPORTED
#define COLOR_GRBG 3
#define COLOR_GBRG 4
#define COLOR_BGGR 5
#endif

#include "NotificationEntry.h"
#include "PvCircularBuffer.h"
#include "PpParam.h"
#include "PvRoi.h"
#include "AcqConfig.h"


//////////////////////////////////////////////////////////////////////////////
// Error codes
//
#define ERR_INVALID_BUFFER              10002
#define ERR_INVALID_PARAMETER_VALUE     10003
#define ERR_BUSY_ACQUIRING              10004
#define ERR_STREAM_MODE_NOT_SUPPORTED   10005
#define ERR_CAMERA_NOT_FOUND            10006
#define ERR_ROI_SIZE_NOT_SUPPORTED      10007
#define ERR_BUFFER_TOO_LARGE            10008
#define ERR_ROI_DEFINITION_INVALID      10009
#define ERR_BUFFER_PROCESSING_FAILED    10010
#define ERR_BINNING_INVALID             10011 // Binning value is not valid for current configuration
#define ERR_OPERATION_TIMED_OUT         10012 // Generic timeout error
#define ERR_FRAME_READOUT_FAILED        10013 // Polling: status = READOUT_FAILED

//////////////////////////////////////////////////////////////////////////////
// Constants
//
#define SMART_STREAM_MAX_EXPOSURES 128


/***
* Struct used for Universal Parameters definition
*/
typedef struct 
{
   const char * name;
   const char * debugName;
   uns32 id;
} ParamNameIdPair;

/***
* Speed table row
*/
typedef struct
{
    uns16 pixTime;         // Readout rate in ns
    int16 bitDepth;        // Bit depth
    rs_bool gainAvail;     // Gain available
    int16   gainMin;       // Min gain index for this speed
    int16   gainMax;       // Max gain index for this speed
    int16   gainDef;       // Default gain for this speed
    std::map<std::string, int16> gainNameMap; // Gain names (i.e., "name:index" map)
    std::map<int16, std::string> gainNameMapReverse; // Reverse lookup map
    int16 spdIndex;        // Speed index 
    uns32 portIndex;       // Port index
    std::string spdString; // A string that describes this choice in GUI
    int32       colorMask;    // Sensor color mask (PARAM_COLOR_MODE) 
    std::string colorMaskStr; // Sensor color mask description (retrieved from PVCAM)
} SpdTabEntry;

/***
* Camera Model is identified mostly by Chip Name. Most of the cameras and every
* unknown camera is treated as "Generic". PVCAM and this uM adapter is mostly
* camera-agnostic, however a couple of camera models may need special treatment.
*/
typedef enum PvCameraModel
{
    PvCameraModel_Generic = 0,
    PvCameraModel_OptiMos_M1,
    PvCameraModel_Retiga6000C
} PvCameraModel;

inline double round( double value )
{
   return floor( 0.5 + value);
};


class PollingThread;
class NotificationThread;
class AcqThread;
template<class T> class PvParam;
class PvUniversalParam;
class PvEnumParam;

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
   unsigned GetImageWidth() const         { return acqCfgCur_.Roi.ImageRgnWidth(); }
   unsigned GetImageHeight() const        { return acqCfgCur_.Roi.ImageRgnHeight(); }
   unsigned GetImageBytesPerPixel() const { return acqCfgCur_.ColorProcessingEnabled ? 4 : 2; } 
   long GetImageBufferSize() const;
   unsigned GetBitDepth() const;
   int GetBinning() const;
   int SetBinning(int binSize);
   double GetExposure() const;
   void SetExposure(double dExp);
   int IsExposureSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }
   unsigned GetNumberOfComponents() const {return acqCfgCur_.ColorProcessingEnabled ? 4 : 1;}

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
   int OnClearCycles(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTriggerTimeOut(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnOutputTriggerFirstMissing(MM::PropertyBase* pProp, MM::ActionType eAct); 
   int OnCircBufferEnabled(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCircBufferSizeAuto(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCircBufferFrameCount(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCircBufferFrameRecovery(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnColorMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSensorCfaMask(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnRedScale(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnGreenScale(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBlueScale(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnAlgorithmCfaMask(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnAlgorithmCfaMaskAuto(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnInterpolationAlgorithm(MM::PropertyBase* pProp, MM::ActionType eAct);
#ifdef PVCAM_CALLBACKS_SUPPORTED
   int OnAcquisitionMethod(MM::PropertyBase* pProp, MM::ActionType eAct);
#endif
#ifdef PVCAM_SMART_STREAMING_SUPPORTED
   int OnSmartStreamingEnable(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSmartStreamingValues(MM::PropertyBase* pProp, MM::ActionType eAct);
#endif
   int OnMetadataEnabled(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCentroidsEnabled(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCentroidsRadius(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnCentroidsCount(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnFanSpeedSetpoint(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTrigTabLastMux(MM::PropertyBase* pProp, MM::ActionType eAct, long trigSignal);
   int OnPMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   bool IsCapturing();

   // Published to allow other classes access the camera
   short Handle() { return hPVCAM_; }
   // Utility logging functions (published to allow usage from other classes)
   int16 LogCamError(int lineNr, const std::string& message, int16 pvErrCode = pl_error_code(), bool debug = false) throw();
   int   LogMMError(int errCode, int lineNr, std::string message="", bool debug=false) const throw();
   void  LogMMMessage(int lineNr, std::string message="", bool debug=true) const throw();

protected:

#ifndef linux
   int  PollingThreadRun(void);
   void PollingThreadExiting() throw();
#endif

   // Called once we detect an arrival of a new frame from the camera, this
   // could be called either from PVCAM callback or Polling thread
   int FrameAcquired();
   // Pushes a final image with its metadata to the MMCore
   int PushImageToMmCore(const unsigned char* pixBuffer, Metadata* pMd );
   // Called from the Notification Thread. Prepares the frame for
   // insertion to the MMCore.
   int ProcessNotification( const NotificationEntry& entry );

private:

   Universal(Universal&) {}
   int GetPvExposureSettings( int16& pvExposeOutMode, uns32& pvExposureValue );
   unsigned int EstimateMaxReadoutTimeMs() const;
   int ResizeImageBufferContinuous();
   int ResizeImageBufferSingle();

   int acquireFrameSeq();
   int waitForFrameSeq();
   int waitForFrameSeqPolling(const MM::MMTime& timeout);
   int waitForFrameSeqCallbacks(const MM::MMTime& timeout);

   int waitForFrameConPolling(const MM::MMTime& timeout);
#ifdef PVCAM_SMART_STREAMING_SUPPORTED
   int SendSmartStreamingToCamera();
#endif
   MM::MMTime GetCurrentTime() { return GetCurrentMMTime();}


   bool            initialized_;          // Driver initialization status in this class instance
   long            imagesToAcquire_;      // Number of images to acquire
   long            imagesInserted_;       // Current number of images inserted to MMCore buffer
   long            imagesAcquired_;       // Current number of images acquired by the camera
   long            imagesRecovered_;      // Total number of images recovered from missed callback(s)
   short           hPVCAM_;               // Camera handle
   static int      refCount_;             // This class reference counter
   static bool     PVCAM_initialized_;    // Global PVCAM initialization status
   PvDebayer       debayer_;              // debayer processor

   MM::MMTime      startTime_;            // Acquisition start time

   short           cameraId_;             // 0-based camera ID, used to allow multiple cameras connected
   PvCameraModel   cameraModel_;
   char            deviceLabel_[MM::MaxStrLength]; // Cached device label used when inserting metadata

   bool            circBufSizeAuto_;
   int             circBufFrameCount_; // number of frames to allocate the buffer for
   bool            circBufFrameRecoveryEnabled_; // True if we perform recovery from lost callbacks



   bool            stopOnOverflow_;       // Stop inserting images to MM buffer if it's full
   bool            snappingSingleFrame_;  // Single frame mode acquisition ongoing
   bool            singleFrameModeReady_; // Single frame mode acquisition prepared
   bool            sequenceModeReady_;    // Continuous acquisition prepared

   bool            isAcquiring_;

   long            triggerTimeout_;       // Max time to wait for an external trigger
   bool            microsecResSupported_; // True if camera supports microsecond exposures

   friend class    PollingThread;
   PollingThread*  pollingThd_;           // Pointer to the sequencing thread
   friend class    NotificationThread;
   NotificationThread* notificationThd_;  // Frame notification thread
   friend class    AcqThread;
   AcqThread*      acqThd_;               // Non-CB live thread

   long            outputTriggerFirstMissing_;

   /// CAMERA PARAMETERS:
   uns16           camParSize_;           // CCD parallel size
   uns16           camSerSize_;           // CCD serial size
   double          exposure_;             // Current Exposure

   char            camName_[CAM_NAME_LEN];
   std::string     camChipName_;
   PvParam<int16>* prmTemp_;              // CCD temperature
   PvParam<int16>* prmTempSetpoint_;      // Desired CCD temperature
   PvParam<int16>* prmGainIndex_;
   PvParam<uns16>* prmGainMultFactor_;
   PvEnumParam*    prmBinningSer_;
   PvEnumParam*    prmBinningPar_;

   std::vector<std::string>        binningLabels_;
   std::vector<int32>              binningValuesX_;
   std::vector<int32>              binningValuesY_;
   bool                            binningRestricted_;

   double           redScale_;
   double           greenScale_;
   double           blueScale_;

   // Acquisition configuration
   AcqConfig acqCfgCur_; // Current configuration
   AcqConfig acqCfgNew_; // New configuration waiting to be applied

   // Single Snaps and Live mode has each its own buffer. However, depending on
   // the configuration the buffer may need to be further processed before its used by MMCore.

   // PVCAM helper structure for decoding an embedded-metadata-enabled frame buffer
#ifdef PVCAM_3_0_12_SUPPORTED
   md_frame*        metaFrameStruct_;
#endif
   // A buffer used for creating a black-filled frame when Centroids or Multi-ROI
   // acquisition is running. Used in both single snap and live mode if needed.
   unsigned char*   metaBlackFilledBuf_;
   size_t           metaBlackFilledBufSz_;
   // A buffer used in setup_seq() only (single snaps mode)
   unsigned char*   singleFrameBufRaw_;
   size_t           singleFrameBufRawSz_;
   // A pointer to the final, post processed image buffer that will be returned
   // in GetImageBuffer() and GetImageBufferAsRGB32(). This is a pointer only that
   // points to either RAW, RGB or Black-Filled buffer.
   unsigned char*   singleFrameBufFinal_;
   // Circular buffer, used in setup_cont() only (live mode)
   PvCircularBuffer circBuf_;
   // Color image buffer. Used in both single snap and live mode if needed.
   ImgBuffer*       rgbImgBuf_;

   Event            eofEvent_;
   MMThreadLock     acqLock_;

#ifdef PVCAM_SMART_STREAMING_SUPPORTED
   double          smartStreamValuesDouble_[SMART_STREAM_MAX_EXPOSURES];
   uns16           smartStreamEntries_;
   bool            ssWasOn_;              // Remember SMART streaming state before Snap was pressed
#endif

#ifdef PVCAM_FRAME_INFO_SUPPORTED
   PFRAME_INFO     pFrameInfo_;           // PVCAM frame metadata
#endif
   int             lastPvFrameNr_;        // The last FrameNr reported by PVCAM
   bool            enableFrameRecovery_;  // Attempt to recover from missed callbacks

#ifdef PVCAM_SMART_STREAMING_SUPPORTED
   PvParam<smart_stream_type>* prmSmartStreamingValues_;
   PvParam<rs_bool>* prmSmartStreamingEnabled_;
#endif
   PvEnumParam*      prmTriggerMode_;     // (PARAM_EXPOSURE_MODE)
   PvParam<uns16>*   prmExpResIndex_;
   PvEnumParam*      prmExpRes_;
   PvEnumParam*      prmExposeOutMode_;
   PvParam<uns16>*   prmClearCycles_;
   PvEnumParam*      prmReadoutPort_;
   PvEnumParam*      prmColorMode_;
   PvParam<ulong64>* prmFrameBufSize_;

   PvParam<rs_bool>* prmMetadataEnabled_;
   PvParam<rs_bool>* prmCentroidsEnabled_;
   PvParam<uns16>*   prmCentroidsRadius_;
   PvParam<uns16>*   prmCentroidsCount_;
   PvEnumParam*      prmFanSpeedSetpoint_;
   PvEnumParam*      prmTrigTabSignal_;
   PvParam<uns8>*    prmLastMuxedSignal_;
   PvEnumParam*      prmPMode_;

   // List of post processing features
   std::vector<PpParam> PostProc_;

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
   int portChanged();
   int speedChanged();
   int buildSpdTable();
   int postExpSetupInit();
   int updateCircBufRange(unsigned int frameSize);
   int selectDebayerAlgMask(int xRoiPos, int yRoiPos, int32 colorMask) const;

   int applyAcqConfig();
   int reinitProcessingBuffers();
   int postProcessSingleFrame(unsigned char** pOutBuf, unsigned char* pInBuf, size_t inBufSz);

   // other internal functions
   int ClearROI();
   int SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize); 
   int GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize);

private:

#ifdef PVCAM_CALLBACKS_SUPPORTED
   static void PvcamCallbackEofEx3( PFRAME_INFO pNewFrameInfo, void* pContext );
#endif

};

#endif //_PVCAMADAPTER_H_
