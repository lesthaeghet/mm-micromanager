///////////////////////////////////////////////////////////////////////////////
// FILE:          Rapp.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Rapp Scanner adapter
//
// COPYRIGHT:     University of California, San Francisco
//
// LICENSE:       This file is distributed under the BSD license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER(S) OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Arthur Edelstein, 12/22/2011
//

#ifndef _Rapp_H_
#define _Rapp_H_

#include "../../MMDevice/MMDevice.h"
#include "../../MMDevice/DeviceBase.h"
#include <string>
#include <map>

#pragma warning( push )
#pragma warning( disable : 4251 )
#include "obsROE_Device.h"
#pragma warning( pop )

class RappScanner : public CGalvoBase<RappScanner>
{
public:
   RappScanner();
   ~RappScanner();
  
   // Device API
   // ----------
   int Initialize();
   int Shutdown();
  
   void GetName(char* pszName) const;
   bool Busy();

   // so far, only the RappScanner attempts to get the controller status on initialization, so
   // that's where the device detection is going for now
   MM::DeviceDetectionStatus DetectDevice(void);

   // Galvo API
   int PointAndFire(double x, double y, double pulseTime_us);
   int SetPosition(double x, double y);
   int GetPosition(double& x, double& y);
   int AddPolygonVertex(int polygonIndex, double x, double y);
   int DeletePolygons();
   int RunSequence();

   double GetXRange();
   double GetYRange();

   // Property action handlers
   int OnCalibrationMode(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnPort(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnSequence(MM::PropertyBase* pProp, MM::ActionType eAct);
   int OnTTLTriggered(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
   bool initialized_;
   std::string port_;
   obsROE_Device* UGA_;
   long calibrationMode_;
   std::vector<tPointList> polygons_;
   int polygonAccuracy_;
   pointf polygonMinRectSize_;
   double currentX_;
   double currentY_;
   std::string sequence_;
   long ttlTriggered_;

   // Helper functions
   void RappScanner::RunDummyCalibration();

};

std::vector<std::string> & split(const std::string &s, char delim, std::vector<std::string> &elems);
std::vector<std::string> split(const std::string &s, char delim);
std::string replaceChar(std::string str, char ch1, char ch2);

#endif //_Rapp_H_
