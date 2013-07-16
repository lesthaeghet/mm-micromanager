///////////////////////////////////////////////////////////////////////////////
// MODULE:			BFCamera.h
// SYSTEM:        100X Imaging base utilities
// AUTHOR:			Nenad Amodaj
//
// DESCRIPTION:	Encapsulation for the Bitflow generic camera interface Ci
//
// COPYRIGHT:     Nenad Amodaj 2011, 100X Imaging Inc 2009
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
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.  
//                
// AUTHOR:        Nenad Amodaj, November 2009
//
///////////////////////////////////////////////////////////////////////////////
#pragma once

#include <vector>
#include	"BFApi.h"
#include	"BFErApi.h"
#include	"DSApi.h"
#include "CiApi.h"
#include "MMDevice.h"

#define BF_INCOMPATIBLE_CAMERAS 1010
#define BF_NOT_INITIALIZED 1011
#define BF_BUSY_ACQUIRING 1012
class BitFlowCamera;

class BFCamera
{
public:
   BFCamera(bool dual);
   ~BFCamera();

   int Initialize(MM::Device* caller, MM::Core* core);
   int Shutdown();
   unsigned long GetBufferSize() {return (unsigned long)width_ * height_ * depth_ + 2 * MAX_FRAME_OFFSET;}
   unsigned GetNumberOfBuffers() {return (unsigned) boards_.size();}
   const unsigned char* GetImage(unsigned& retCode, char* errText, unsigned bufLen, BitFlowCamera* bf);
   bool WaitForImage();
   const unsigned char* GetImageCont();
   int StartContinuousAcq();
   int StopContinuousAcq();
   int StartSequence();
   int StopSequence();
   bool isAcquiring() {return acquiring_;}
   int GetTimeout();
  
   unsigned Width() {return width_;}
   unsigned Height() {return height_;}
   unsigned Depth() {return depth_;}

   bool isInitialized() {return initialized_;}

   static const int MAX_FRAME_OFFSET = 128;

private:
   unsigned char* buf_;
   std::vector<Bd> boards_;
   unsigned width_;
   unsigned height_;
   unsigned depth_;
   bool initialized_;
   CiSIGNAL eofSignal_;
   BFU32 timeoutMs_;
   bool acquiring_;
   int frameOffset_;
   bool dual_;
   MM::Device* caller_;
   MM::Core* core_;
};
