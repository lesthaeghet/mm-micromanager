// PROJECT:       Micro-Manager
// SUBSYSTEM:     MMCore
//
// COPYRIGHT:     University of California, San Francisco, 2014,
//                All Rights reserved
//
// LICENSE:       This file is distributed under the "Lesser GPL" (LGPL) license.
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
// AUTHOR:        Mark Tsuchida

#pragma once

#include "DeviceInstanceBase.h"


class StageInstance : public DeviceInstanceBase<MM::Stage>
{
public:
   StageInstance(CMMCore* core,
         boost::shared_ptr<LoadedDeviceAdapter> adapter,
         const std::string& name,
         MM::Device* pDevice,
         DeleteDeviceFunction deleteFunction,
         const std::string& label,
         boost::shared_ptr<mm::logging::Logger> logger) :
      DeviceInstanceBase<MM::Stage>(core, adapter, name, pDevice, deleteFunction, label, logger)
   {}

   int SetPositionUm(double pos);
   int SetRelativePositionUm(double d);
   int Move(double velocity);
   int SetAdapterOriginUm(double d);
   int GetPositionUm(double& pos);
   int SetPositionSteps(long steps);
   int GetPositionSteps(long& steps);
   int SetOrigin();
   int GetLimits(double& lower, double& upper);
   int IsStageSequenceable(bool& isSequenceable) const;
   bool IsContinuousFocusDrive() const;
   int GetStageSequenceMaxLength(long& nrEvents) const;
   int StartStageSequence();
   int StopStageSequence();
   int ClearStageSequence();
   int AddToStageSequence(double position);
   int SendStageSequence();
};
