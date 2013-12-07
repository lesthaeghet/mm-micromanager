///////////////////////////////////////////////////////////////////////////////
// FILE:          ASIPiezo.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   ASI motorized one-axis stage device adapter
//
// COPYRIGHT:     Applied Scientific Instrumentation, Eugene OR
//
// LICENSE:       This file is distributed under the BSD license.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Jon Daniels (jon@asiimaging.com) 09/2013
//
// BASED ON:      ASIStage.h and others
//

#ifndef _ASIPiezo_H_
#define _ASIPiezo_H_

#include "ASIDevice.h"
#include "../../MMDevice/MMDevice.h"
#include "../../MMDevice/DeviceBase.h"

class CPiezo : public CStageBase<CPiezo>, ASIDevice
{
public:
   CPiezo(const char* name);
   ~CPiezo() { Shutdown(); }
  
   // Device API
   // ----------
   int Initialize();
   bool Busy();
   int Shutdown() { return ASIDevice::Shutdown(); }
   void GetName(char* pszName) const { ASIDevice::GetName(pszName); }

   // Piezo API
   // -----------
   int Stop();

   // the step size is the programming unit for dimensions and is integer
   // see http://micro-manager.3463995.n2.nabble.com/what-are-quot-steps-quot-for-stages-td7580724.html
   double GetStepSize() {return stepSizeUm_;}
   int GetPositionSteps(long& steps);
   int SetPositionSteps(long steps);
   int SetPositionUm(double pos);
   int GetPositionUm(double& pos);
   int SetRelativePositionUm(double d);
   int GetLimits(double& min, double& max);
   int SetOrigin();

   bool IsContinuousFocusDrive() const {return false;}  // todo figure out what this means and if it's accurate
   int IsStageSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }
   int GetStageSequenceMaxLength(long& nrEvents) const  { nrEvents = 0; return DEVICE_OK; }

   // below aren't implemented yet
   int StartStageSequence()  { return DEVICE_UNSUPPORTED_COMMAND; }
   int StopStageSequence()   { return DEVICE_UNSUPPORTED_COMMAND; }
   int ClearStageSequence()  { return DEVICE_UNSUPPORTED_COMMAND; }
   int AddToStageSequence(double /*position*/)  { return DEVICE_UNSUPPORTED_COMMAND; }
   int SendStageSequence()  { return DEVICE_UNSUPPORTED_COMMAND; }

   // action interface
   // ----------------
   int OnSaveCardSettings     (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnRefreshProperties    (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnLowerLim             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnUpperLim             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPiezoMode            (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnJoystickFastSpeed    (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnJoystickSlowSpeed    (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnJoystickMirror       (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnJoystickSelect       (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnMotorControl         (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnModeFourOvershoot    (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnModeFourMaxTime      (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAAmplitude          (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAOffset             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAPeriod             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAMode               (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAPattern            (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAAdvanced           (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAClkSrc             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAClkPol             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSATTLOut             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSATTLPol             (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSAPatternByte        (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSetHomeHere          (MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSPIMNumSlices        (MM::PropertyBase* pProp, MM::ActionType eAct);


private:
   double unitMult_;
   double stepSizeUm_;
   string axisLetter_;
};

#endif //_ASIPiezo_H_
