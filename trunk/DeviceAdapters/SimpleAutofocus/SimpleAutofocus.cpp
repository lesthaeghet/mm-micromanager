///////////////////////////////////////////////////////////////////////////////
// FILE:          SimpleAutofocus.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   SimpleAutofocus controller adapter
// COPYRIGHT:     University of California, San Francisco, 2009
//
// AUTHOR:        Karl Hoover, UCSF
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
// CVS:           
//



#ifdef WIN32
   #include <windows.h>
   #define snprintf _snprintf 
#endif


#include "SimpleAutofocus.h"
#include <string>
#include <math.h>
#include <sstream>

#include "../../MMDevice/ModuleInterface.h"
#include "../../MMCore/Error.h"


// property names:
// Controller
const char* g_ControllerName = "SimpleAutofocus";



///////////////////////////////////////////////////////////////////////////////
// Exported MMDevice API
///////////////////////////////////////////////////////////////////////////////
MODULE_API void InitializeModuleData()
{
   AddAvailableDeviceName(g_ControllerName, "SimpleAutofocus Finder");
   
}

MODULE_API MM::Device* CreateDevice(const char* deviceName)
{
   if (deviceName == 0)
      return 0;

   if (strcmp(deviceName, g_ControllerName) == 0)
   {
      // create Controller
      SimpleAutofocus* pSimpleAutofocus = new SimpleAutofocus(g_ControllerName);
      return pSimpleAutofocus;
   }

   return 0;
}

MODULE_API void DeleteDevice(MM::Device* pDevice)
{
   delete pDevice;
}

///////////////////////////////////////////////////////////////////////////////
// Controller implementation
// ~~~~~~~~~~~~~~~~~~~~

SimpleAutofocus::SimpleAutofocus(const char* name) : name_(name), pCore_(NULL), cropFactor_(0.2), busy_(false)
{
}

int SimpleAutofocus::Shutdown()
{
return DEVICE_OK;
}


SimpleAutofocus::~SimpleAutofocus()
{
   Shutdown();
}

bool SimpleAutofocus::Busy()
{
     return busy_;
}

void SimpleAutofocus::GetName(char* name) const
{
   assert(name_.length() < CDeviceUtils::GetMaxStringLength());
   CDeviceUtils::CopyLimitedString(name, name_.c_str());
}


int SimpleAutofocus::Initialize()
{
   LogMessage("SimpleAutofocus::Initialize()");
   pCore_ = GetCoreCallback();


   // Set Exposure
   CPropertyAction *pAct = new CPropertyAction (this, &SimpleAutofocus::OnExposure);
   CreateProperty(MM::g_Keyword_Exposure, "10", MM::Integer, false, pAct); 

   // Set the depth for coarse search
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnSearchSpanCoarse);
   CreateProperty("FullSpan","300",MM::Float, false, pAct);

   // Set the depth for fine search
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnSearchSpanFine);
   CreateProperty("IncrementalSpan","100",MM::Float, false, pAct);

   // Set the span for coarse search
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnStepsizeCoarse);
   CreateProperty("FullStep","10",MM::Float, false, pAct);

   // Set the span for fine search
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnStepSizeFine);
   CreateProperty("IncrementalStep","3",MM::Float, false, pAct);

   pAct = new CPropertyAction(this, &SimpleAutofocus::OnChannelForAutofocus);
   CreateProperty("ChannelForAutofocus", "", MM::String, false, pAct);

   // Set the sharpness threshold
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnThreshold);
   CreateProperty("Threshold","0.05",MM::Float, false, pAct);

   // Set the cropping factor to speed up computation
   pAct = new CPropertyAction(this, &SimpleAutofocus::OnCropFactor);
   CreateProperty("CropFactor","0.2",MM::Float, false, pAct);

   pAct = new CPropertyAction(this, &SimpleAutofocus::OnSharpnessScore);
   CreateProperty("SharpnessScore","0.2",MM::Float, true, pAct);


   UpdateStatus();


   return DEVICE_OK;
}


// API

bool SimpleAutofocus::IsContinuousFocusLocked(){ 
   return locked_;} ;
int SimpleAutofocus::FullFocus(){ 
   return 0;};
int SimpleAutofocus::IncrementalFocus(){ 
   return 0;};
int SimpleAutofocus::GetLastFocusScore(double& score){ 
   return 0;};
int SimpleAutofocus::GetCurrentFocusScore(double& score){ 
   return 0;};
int SimpleAutofocus::AutoSetParameters(){ 
   return 0;};
int SimpleAutofocus::GetOffset(double &offset){ 
   return 0;};
int SimpleAutofocus::SetOffset(double offset){ 
   return 0;};


/////////////////////////////////////////////
// Property Generators
/////////////////////////////////////////////




///////////////////////////////////////////////////////////////////////////////
// Action handlers
///////////////////////////////////////////////////////////////////////////////

// action interface
// ---------------
int SimpleAutofocus::OnExposure(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   try
   {
      if (eAct == MM::BeforeGet)
      {
         // retrieve value from the camera via the core
         double v;
         pCore_->GetExposure(v);
         pProp->Set(v);
      }
      else if (eAct == MM::AfterSet)
      {
         // set the value to the camera via the core
         double val;
         pProp->Get(val);
         pCore_->SetExposure(val);
      }
   }
   catch(CMMError& e)
   {


   }
   catch(...)
   {
      return DEVICE_ERR;
   }
   return DEVICE_OK;
}


 
int SimpleAutofocus::OnSearchSpanCoarse(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnSearchSpanFine(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnStepsizeCoarse(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnStepSizeFine(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnChannelForAutofocus(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnThreshold(MM::PropertyBase* pProp, MM::ActionType eAct){ return 0;};
int SimpleAutofocus::OnCropFactor(MM::PropertyBase* pProp, MM::ActionType eAct)
{ 
      if (eAct == MM::BeforeGet)
      {
         pProp->Set(cropFactor_);
      }
      else if (eAct == MM::AfterSet)
      {
         pProp->Get(cropFactor_);
      }
   return DEVICE_OK;

};

int SimpleAutofocus::OnSharpnessScore(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   try
   {
      if (eAct == MM::BeforeGet)
      {
         // retrieve value from the camera via the core
         double v;
         v = SharpnessAtCurrentSettings();
         pProp->Set(v);
      }
      else if (eAct == MM::AfterSet)
      {
         // never do anything for a read-only property
      }
   }
   catch(...)
   {
      return DEVICE_ERR;
   }
   return DEVICE_OK;


}








// computational utilities
short SimpleAutofocus::findMedian(short* arr, const int lengthMinusOne)
{ 
  short tmp;

   // looks like a bubble sort....
   for(int i=0; i<lengthMinusOne; ++i)
   {
      for(int j=0; j<lengthMinusOne-i; ++j)
      {
         if (arr[j+1]<arr[j])
         {
            tmp = arr[j];
            arr[j]=arr[j+1];
            arr[j+1]=tmp;
         }
      }
   }
   return arr[lengthMinusOne/2 +1];
}


double SimpleAutofocus::SharpnessAtCurrentSettings()
{
   	int w0 = 0, h0 = 0, d0 = 0;
      int ret  = pCore_->GetImageDimensions(w0, h0, d0);


      int width =  (int)(cropFactor_*w0);
      int height = (int)(cropFactor_*h0);
      int ow = (int)(((1-cropFactor_)/2)*w0);
      int oh = (int)(((1-cropFactor_)/2)*h0);

      short* medPix = new short[ width*height];
      double sharpNess = 0;
      short* windo = new short[9];

      // copy from MM image to the working buffer

	   ImgBuffer image(w0,h0,d0);
      //snap an image
      const char* pI = pCore_->GetImage();
      short* pShort = NULL;
      const short* pSInput = reinterpret_cast<const short*>(pI);

      int iindex;
      // to keep it simple always copy to a new short array
      switch( d0)
      {
      case 1:
         pShort = (short*)malloc( sizeof(short)*w0*h0);
         for(iindex = 0; iindex < w0*h0; ++iindex)
         {
            pShort[iindex] = pI[iindex];
         }
         break;

      case 2:
         pShort = (short*)malloc( sizeof(short)*w0*h0);
         for(iindex = 0; iindex < w0*h0; ++iindex)
         {
            pShort[iindex] = pSInput[iindex];
         }
         break;
      default:
         break;
      }

      if( NULL !=pShort)
      {
            
         // ToDO -- eliminate copy above.

         /*Apply 3x3 median filter to reduce shot noise*/
         for (int i=0; i<width; i++){
            for (int j=0; j<height; j++){

               windo[0] = pShort[ow+i-1 + width*(oh+j-1)];
               windo[1] = pShort[ow+i+ width*(oh+j-1)];
               windo[2] = pShort[ow+i+1+ width*(oh+j-1)];
               windo[3] = pShort[ow+i-1+ width*(oh+j)];
               windo[4] = pShort[ow+i+ width*(oh+j)];
               windo[5] = pShort[ow+i+1+ width*(oh+j)];
               windo[6] = pShort[ow+i-1+ width*(oh+j+1)];
               windo[7] = pShort[ow+i+ width*(oh+j+1)];
               windo[8] = pShort[ow+i+1+ width*(oh+j+1)];

               medPix[i + j*width] = findMedian(windo,8);
            } 
         }

         /*Edge detection using a 3x3 filter: [-2 -1 0; -1 0 1; 0 1 2]. Then sum all pixel values. Ideally, the sum is large if most edges are sharp*/

         for (int k=1; k<width-1; k++){
            for (int l=1; l<height-1; l++)
            {
               double convolvedValue = -2.0*medPix[k-1 + width*(l-1)] - (double)medPix[k+ width*(l-1)]-(double)medPix[k-1 + width*l]+(double)medPix[k+1 + width*l]+(double)medPix[k+ width*(l+1)]+2.0*medPix[k+1+ width*(l+1)];
               sharpNess = sharpNess + convolvedValue*convolvedValue;

            } 
         }

         free(pShort);


      }
      delete medPix;
      delete windo;
      return sharpNess;
   }



void  SimpleAutofocus::Z(const double value)
{
   pCore_->SetFocusPosition(value);
}

double SimpleAutofocus::Z(void)
{
   double value;
   pCore_->GetFocusPosition(value);
   return value;
}


void SimpleAutofocus::Exposure(const int value)
{ 
   pCore_->SetExposure(value);
};

int SimpleAutofocus::Exposure(void){
   double value;
   pCore_->GetExposure(value);
   return (int)(0.5+value);
};
