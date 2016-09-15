///////////////////////////////////////////////////////////////////////////////
// FILE:          PointGrey.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Point Grey FlyCapture Micro-Manager adapter
//                
// AUTHOR:        Nico Stuurman
// COPYRIGHT:     University of California, 2016
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

#include "DeviceBase.h"
#include "ImgBuffer.h"
#include "DeviceThreads.h"
#include "ImgBuffer.h"

#include "FlyCapture2.h"

using namespace FlyCapture2;

//////////////////////////////////////////////////////////////////////////////

class SequenceThread;

class PointGrey : public CCameraBase<PointGrey>  
{
public:
   PointGrey();
   ~PointGrey();
  
   //////////////////////////////////////////////////////////////
   // MMDevice API
   int Initialize();
   int Shutdown();
   void GetName(char* name) const;      
   
   //////////////////////////////////////////////////////////////
   // PointGreyCamera API
   int SnapImage();
   const unsigned char* GetImageBuffer();
   unsigned int GetNumberOfComponents()  const { return nComponents_;};
   //////////////////////////////////////////////////////////////
   unsigned int GetImageWidth() const;
   unsigned int GetImageHeight() const;
   //////////////////////////////////////////////////////////////
   unsigned int GetImageBytesPerPixel() const;
   unsigned int GetBitDepth() const;
   long     GetImageBufferSize() const;
   //////////////////////////////////////////////////////////////
   double   GetExposure() const;
   void     SetExposure(double exp);
   //////////////////////////////////////////////////////////////
   int      SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize); 
   int      GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize); 
   int      ClearROI();
   //////////////////////////////////////////////////////////////
   int      PrepareSequenceAcqusition(){ return DEVICE_OK; };
   int      StartSequenceAcquisition(double interval);
   int      StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow);
   int      StopSequenceAcquisition();
   bool     IsCapturing();
   int      InsertImage(Image* pImg) const;
   int      GetBinning() const;
   int      SetBinning(int binSize);
   int      IsExposureSequenceable(bool& seq) const {seq = false; return DEVICE_OK;}
   /////////////////////////////////////////////////////////////
   // Functions to convert between PGR and MM
   int CameraPGRGuid(FlyCapture2::BusManager* busMgr, FlyCapture2::PGRGuid* guid, int nr);
   int CameraID(FlyCapture2::PGRGuid id, std::string* camIDString);
   int CameraGUIDfromOurID(FlyCapture2::BusManager* busMgr, FlyCapture2::PGRGuid* guid, std::string ourID);
   void VideoModeAndFrameRateStringFromEnums(std::string &readableString, FlyCapture2::VideoMode vm, FlyCapture2::FrameRate fr) const;
   int VideoModeAndFrameRateEnumsFromString(std::string readableString, FlyCapture2::VideoMode &vm, FlyCapture2::FrameRate &fr) const;
   std::string PixelTypeAsString(PixelFormat pixelFormat) const;
   PixelFormat PixelFormatFromString(std::string pixelType) const;
   std::string Format7ModeAsString(Mode mode) const;
   int Format7ModeFromString(std::string pixelType,  Mode* mode) const;

   //////////////////////////////////////////////////////////////
   // action interface
   int OnCameraId(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnAbsValue(MM::PropertyBase* pProp, MM::ActionType eAct, long value);
   int OnValue(MM::PropertyBase* pProp, MM::ActionType eAct, long value);
   int OnOnOff(MM::PropertyBase* pProp, MM::ActionType eAct, long value);
   int OnAutoManual(MM::PropertyBase* pProp, MM::ActionType eAct, long value);
   int OnVideoModeAndFrameRate(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnFormat7Mode(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
   void updatePixelFormats(unsigned int pixelFormatBitField);
   int SetEndianess(bool little);

   FlyCapture2::PGRGuid guid_;
   FlyCapture2::Camera cam_;
   FlyCapture2::Image image_;
   unsigned int nComponents_;
   bool initialized_;
   std::string name_;
   std::string cameraId_;
   MM::MMTime sequenceStartTime_;
   MM::MMTime sequenceStartTimeStamp_;
   long imageCounter_;
   bool stopOnOverflow_;
   long desiredNumImages_;
   bool isCapturing_;
   FlyCapture2::Format7Info format7Info_;
   std::map<VideoMode, std::vector<FrameRate>> videoModeFrameRateMap_;
   std::vector<FlyCapture2::Mode> availableFormat7Modes_;
   bool f7InUse_;
   double exposureTimeMs_;
   FlyCapture2::PixelFormat pixelFormat8Bit_;
   FlyCapture2::PixelFormat pixelFormat16Bit_;
};