// Mock device adapter for testing of device sequencing
//
// Copyright (C) 2014 University of California, San Francisco.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation.
//
// This library is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
//
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this library; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//
// Author: Mark Tsuchida

#pragma once

#include "DeviceBase.h"

#include <boost/thread.hpp>
#include <string>


template <class TDevice>
class LoggedSetting
{
   TDevice* device_;

public:
   LoggedSetting(TDevice* device);
   virtual ~LoggedSetting();

   virtual MM::ActionFunctor* NewPropertyAction() = 0;
};


template <class TDevice>
class LoggedIntegerSetting : public LoggedSetting<TDevice>
{
   bool hasMinMax_;
   long min_;
   long max_;

   long setValue_;

public:
   LoggedIntegerSetting(TDevice* device, long initialValue,
         bool hasMinMax, long minimum, long maximum);

   bool HasMinMax() const { return hasMinMax_; }
   long GetMin() const { return min_; }
   long GetMax() const { return max_; }

   int Set(long newValue);
   int Get(long& value) const;
   long Get() const;
   virtual MM::ActionFunctor* NewPropertyAction();
};


template <class TDevice>
class LoggedFloatSetting : public LoggedSetting<TDevice>
{
   bool hasMinMax_;
   double min_;
   double max_;

   double setValue_;

public:
   LoggedFloatSetting(TDevice* device, double initialValue,
         bool hasMinMax, double minimum, double maximum);

   bool HasMinMax() const { return hasMinMax_; }
   double GetMin() const { return min_; }
   double GetMax() const { return max_; }

   int Set(double newValue);
   int Get(double& value) const;
   double Get() const;
   virtual MM::ActionFunctor* NewPropertyAction();
};


class TesterHub;


template <template <class> class TDeviceBase, class UConcreteDevice>
class TesterBase : public TDeviceBase<UConcreteDevice>
{
public:
   typedef TesterBase Self;
   typedef TDeviceBase<UConcreteDevice> Super;

   TesterBase(const std::string& name);
   virtual ~TesterBase();

   virtual void GetName(char* name) const;
   virtual int Initialize();
   virtual int Shutdown();
   virtual bool Busy();

   void MarkBusy();

protected:
   virtual TesterHub* GetHub();
   void CreateIntegerProperty(const std::string& name,
         LoggedIntegerSetting<UConcreteDevice>& setting);
   void CreateFloatProperty(const std::string& name,
         LoggedFloatSetting<UConcreteDevice>& setting);

private:
   const std::string name_;
   unsigned busyCount_;
};


class TesterHub : public TesterBase<HubBase, TesterHub>
{
public:
   typedef TesterHub Self;
   typedef TesterBase< ::HubBase, TesterHub > Super;

   TesterHub(const std::string& name);

   virtual int Initialize();
   virtual int Shutdown();

   virtual int DetectInstalledDevices();

   virtual TesterHub* GetHub() { return this; }
};


class TesterCamera : public TesterBase<CCameraBase, TesterCamera>
{
   typedef TesterCamera Self;
   typedef TesterBase< ::CCameraBase, TesterCamera > Super;

public:
   TesterCamera(const std::string& name);
   virtual ~TesterCamera();

   virtual int Initialize();
   virtual int Shutdown();

   virtual int SnapImage();
   virtual const unsigned char* GetImageBuffer();

   virtual long GetImageBufferSize() const;
   virtual unsigned GetImageWidth() const;
   virtual unsigned GetImageHeight() const;
   virtual unsigned GetImageBytesPerPixel() const { return 1; }
   virtual unsigned GetBitDepth() const { return 8; }

   virtual int GetBinning() const;
   virtual int SetBinning(int binSize);
   virtual void SetExposure(double exposureMs);
   virtual double GetExposure() const;
   virtual int SetROI(unsigned x, unsigned y, unsigned w, unsigned h);
   virtual int GetROI(unsigned& x, unsigned& y, unsigned& w, unsigned& h);
   virtual int ClearROI() { return DEVICE_OK; }

   virtual int StartSequenceAcquisition(long count, double intervalMs,
         bool stopOnOverflow);
   virtual int StartSequenceAcquisition(double intervalMs);
   virtual int StopSequenceAcquisition();
   virtual int PrepareSequenceAcquisition() { return DEVICE_OK; }
   virtual bool IsCapturing();
   virtual int IsExposureSequenceable(bool& f) const
   { f = false; return DEVICE_OK; }

private:
   // Returned pointer should be delete[]d by caller
   const unsigned char* GenerateLogImage(bool isSequenceImage);

   int StartSequenceAcquisitionImpl(bool finite, long count,
         bool stopOnOverflow);

   void SendSequence(bool finite, long count, bool stopOnOverflow);

private:
   size_t snapCounter_;
   size_t sequenceCounter_;
   size_t cumulativeSequenceCounter_;

   const unsigned char* snapImage_;

   boost::mutex sequenceMutex_;
   bool stopSequence_;
   // Note: boost::future in more recent versions
   boost::unique_future<void> sequenceFuture_;
   boost::thread sequenceThread_;

   LoggedFloatSetting<Self> exposureSetting_;
   LoggedIntegerSetting<Self> binningSetting_;
};
