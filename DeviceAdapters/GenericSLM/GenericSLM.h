// DESCRIPTION:   GenericSLM device adapter
// COPYRIGHT:     2009-2016 Regents of the University of California
//                2016 Open Imaging, Inc.
//
// AUTHOR:        Arthur Edelstein, arthuredelstein@gmail.com, 3/17/2009
//                Mark Tsuchida (refactor/rewrite), 2016
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

#pragma once

#include "SLMColor.h"
#include "RefreshWaiter.h"

#include "DeviceBase.h"
#include "DeviceUtils.h"

class OffscreenBuffer;
class SLMWindow;
class SleepBlocker;


// Note: Only one SLM is currently supported. The effect of adding 2 or more is
// undefined.
class GenericSLM : public CSLMBase<GenericSLM>
{
public:
   GenericSLM(const char* name);
   virtual ~GenericSLM();

   // Device API
   virtual int Initialize();
   virtual int Shutdown();

   virtual void GetName(char* pszName) const;
   virtual bool Busy();

   // SLM API
   virtual unsigned int GetWidth();
   virtual unsigned int GetHeight();
   virtual unsigned int GetNumberOfComponents();
   virtual unsigned int GetBytesPerPixel();

   virtual int SetExposure(double interval_ms);
   virtual double GetExposure();

   virtual int SetImage(unsigned char* pixels);
   virtual int SetImage(unsigned int* pixels);
   virtual int SetPixelsTo(unsigned char intensity);
   virtual int SetPixelsTo(unsigned char red, unsigned char green, unsigned char blue);
   virtual int DisplayImage();

   virtual int IsSLMSequenceable(bool& isSequenceable) const
   { isSequenceable = false; return DEVICE_OK; }

private: // Action handlers
   int OnInversion(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnMonochromeColor(MM::PropertyBase* pProp, MM::ActionType eAct);

private: // Private data
   const std::string name_;

   // Used in constructor, pre-init properties, and Initiazlie()
   std::vector<std::string> availableMonitors_;

   std::string monitorName_;

   SLMWindow* window_;
   OffscreenBuffer* offscreen_;
   SleepBlocker* sleepBlocker_;
   RefreshWaiter refreshWaiter_;

   bool shouldBlitInverted_;

   bool invert_;
   std::string inversionStr_;

   SLMColor monoColor_;
   std::string monoColorStr_;

private:
	GenericSLM& operator=(const GenericSLM&);
};
