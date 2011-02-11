///////////////////////////////////////////////////////////////////////////////
// FILE:          TetheredCamera.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Camera driver for Canon and Nikon cameras using 
//                DSLRRemote, NKRemote, or PSRemote tethering software.
//                
// AUTHOR:        Koen De Vleeschauwer, www.kdvelectronics.eu, 2010
//
// COPYRIGHT:     (c) 2010, Koen De Vleeschauwer, www.kdvelectronics.eu
//                (c) 2007, Regents of the University of California
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
//

#ifndef _TETHEREDCAMERA_H_
#define _TETHEREDCAMERA_H_

#include "../../MMDevice/DeviceBase.h"
#include "../../MMDevice/ImgBuffer.h"
#include "../../MMDevice/DeviceThreads.h"
#include <string>
#include <map>
#include <wincodec.h>

// Add libraw.h, but disable warnings
#pragma warning (push)
#pragma warning (disable: 4127)
#include "libraw/libraw.h"
#pragma warning (pop)

//////////////////////////////////////////////////////////////////////////////
// Error codes
//
//#define ERR_UNKNOWN_MODE         102
#define ERR_CAM_BAD_PARAM        103
#define ERR_CAM_NO_IMAGE         104
#define ERR_CAM_NOT_RUNNING      105
#define ERR_CAM_NOT_CONNECTED    106
#define ERR_CAM_BUSY             107
#define ERR_CAM_TIMEOUT          108
#define ERR_CAM_SHUTTER          109
#define ERR_CAM_UNKNOWN          110
#define ERR_CAM_LOAD             111
#define ERR_CAM_RAW              112
#define ERR_CAM_CONVERSION       113
#define ERR_CAM_SHUTTER_SPEEDS   114


//////////////////////////////////////////////////////////////////////////////
// Properties
//
#define g_Keyword_ShutterSpeeds  "ShutterSpeeds"
#define g_Keyword_KeepOriginals  "KeepOriginals"
#define g_Keyword_BitDepth       "BitDepth"
#define g_Keyword_ImageDecoder   "ImageDecoder"

//////////////////////////////////////////////////////////////////////////////
// CDemoCamera class
// Simulation of the Camera device
//////////////////////////////////////////////////////////////////////////////
class CTetheredCamera : public CCameraBase<CTetheredCamera>  
{
public:
   CTetheredCamera();
   ~CTetheredCamera();
  
   // MMDevice API
   // ------------
   int Initialize();
   int Shutdown();
  
   void GetName(char* name) const;      
   
   // MMCamera API
   // ------------
   int SnapImage();
   const unsigned char* GetImageBuffer();
   const unsigned int* GetImageBufferAsRGB32();
   unsigned GetNumberOfComponents() const;
   int GetComponentName(unsigned int channel, char* name);
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

   // action interface
   // ----------------
   int OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnKeepOriginals(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnBitDepth(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnImageDecoder(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
   int AcquireFrame();
   int ResizeImageBuffer();

   int SetAllowedBinning();
   static const double nominalPixelSizeUm_;
   bool GetBoolProperty(const char *const propName);

   int SetCameraExposure(double exp_ms);
   int GetCameraName();
   int GetReturnCode(int status);
   int LoadWICImage(IWICImagingFactory *factory, const char* filename); /* Load an image from file using Windows WIC Codecs */
   int LoadRawImage(IWICImagingFactory *factory, const char* filename); /* Load a raw image from file using libraw */
   int Convert64bppRGBAto64bppBGRA(ImgBuffer *img);  

   ImgBuffer img_;
   LibRaw rawProcessor_;

   bool initialized_;
   std::string cameraName_; /* Camera manufacturer and model */
   IWICBitmap *frameBitmap; /* last captured frame */
   bool grayScale_;
   bool keepOriginals_;
   bool internalDecoder_;
   unsigned bitDepth_;
   unsigned roiX_; /* Region Of Interest */
   unsigned roiY_;
   unsigned roiXSize_;
   unsigned roiYSize_;
   unsigned scaleFactor_; /* binning of current image */
   unsigned originX_; /* coordinates of lower left corner of view window */
   unsigned originY_;
};

#endif //_TETHEREDCAMERA_H_
