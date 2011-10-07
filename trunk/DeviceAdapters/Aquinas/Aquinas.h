///////////////////////////////////////////////////////////////////////////////
// FILE:          Aquinas.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Interfaces with Aquinas microfluidics controller
// COPYRIGHT:     UCSF, 2011
// LICENSE:       LGPL
// AUTHOR:        Nico Stuurman, nico@cmp.ucsf.edu


#ifndef _AQUINAS_H_
#define _AQUINAS_H

#include "../../MMDevice/MMDevice.h"
#include "../../MMDevice/DeviceBase.h"
#include "../../MMDevice/ModuleInterface.h"

//////////////////////////////////////////////////////////////////////////////
// Error codes                                                
//

#define ERR_PORT_CHANGE_FORBIDDEN    101

class AqController: public CGenericBase<AqController>
{
public: 
   AqController();
   ~AqController();

    // MMDevice API
    // ------------
    int Initialize();
    int Shutdown();

    void GetName(char* pszName) const;
    bool Busy();

   // action interface
   // ---------------
   int OnSetPressure(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnValveState(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnValveOnOff(MM::PropertyBase* pProp, MM::ActionType eAct, long valveNr);

private:
   std::string port_;
   bool initialized_;
};

#endif
