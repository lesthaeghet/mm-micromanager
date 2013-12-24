///////////////////////////////////////////////////////////////////////////////
// FILE:          MMDevice.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     MMDevice - Device adapter kit
//-----------------------------------------------------------------------------
// DESCRIPTION:   The interface to the Micro-Manager devices. Defines the 
//                plugin API for all devices.
//
// NOTE:          This file is also used in the main control module MMCore.
//                Do not change it undless as a part of the MMCore module
//                revision. Discrepancy between this file and the one used to
//                build MMCore will cause a malfunction and likely a crash too.
// 
// AUTHOR:        Nenad Amodaj, nenad@amodaj.com, 06/08/2005
//
// COPYRIGHT:     University of California, San Francisco, 2006
//                100X Imaging Inc, 2008
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
// CVS:           $Id$
//

///////////////////////////////////////////////////////////////////////////////
// Header version
// If any of the class declarations changes, the interface version
// must be incremented
#define DEVICE_INTERFACE_VERSION 55
///////////////////////////////////////////////////////////////////////////////


// N.B.
//
// Never add parameters or return values that are not POD
// (http://stackoverflow.com/a/146454) to any method of class Device and its
// derived classes declared in this file. For example, a std::string parameter
// is not acceptable (use const char*). This is to prevent inter-DLL
// incompatibilities.


#pragma once
#ifndef MMMMDEVICE_H
#define MMMMDEVICE_H

#include "MMDeviceConstants.h"
#include "DeviceUtils.h"
#include "ImageMetadata.h"
#include "DeviceThreads.h"
#include <string>
#include <cstring>
#include <climits>
#include <cstdlib>
#include <vector>
#include <sstream>



#ifdef WIN32
   #define WIN32_LEAN_AND_MEAN
   #include <windows.h>
   #define snprintf _snprintf 

   typedef HMODULE HDEVMODULE;
#else
   typedef void* HDEVMODULE;
#endif


class ImgBuffer;



namespace MM {

   // forward declaration for the MMCore callback class
   class Core;

   /**
    * Utility class used both MMCore and devices to maintain time intervals
    * in the uniform, platfrom independent way.
    */
   class MMTime
   {
      public:
         MMTime(double uSecTotal = 0.0)
         {
            sec_ = (long) (uSecTotal / 1.0e6);
            uSec_ = (long) (uSecTotal - sec_ * 1.0e6); 
         }

         MMTime(long sec, long uSec) : sec_(sec), uSec_(uSec)
         {
            Normalize();
         }

         ~MMTime() {}

         MMTime(std::string serialized) {
            std::stringstream is(serialized);
            is >> sec_ >> uSec_;
            Normalize();
         }

         std::string serialize() {
            std::ostringstream os;
            os << sec_ << " " << uSec_;
            return os.str().c_str();
         }

         long sec_;
         long uSec_;

         MMTime operator+(const MMTime &other) const
         {
            MMTime res(sec_ + other.sec_, uSec_ + other.uSec_);
            return res;
         }

         MMTime operator-(const MMTime &other) const
         {
            MMTime res(sec_ - other.sec_, uSec_ - other.uSec_);
            return res;
         }

         bool operator>(const MMTime &other) const
         {
            if (sec_ > other.sec_)
               return true;
            else if (sec_ < other.sec_)
               return false;

            if (uSec_ > other.uSec_)
               return true;
            else
               return false;
         }

         bool operator<(const MMTime &other) const
         {
            if (*this == other)
               return false;

            return ! (*this > other);
         }

         bool operator==(const MMTime &other) const
         {
            if (sec_ == other.sec_ && uSec_ == other.uSec_)
               return true;
            else
               return false;
         }

         double getMsec() const
         {
            return sec_ * 1000.0 + uSec_ / 1000.0;
         }

         double getUsec() const
         {
            return sec_ * 1.0e6 + uSec_;
         }

      private:
         void Normalize()
         {
            if (sec_ < 0)
            {
               sec_ = 0L;
               uSec_ = 0L;
               return;
            }

            if (uSec_ < 0)
            {
               sec_--;
               uSec_ = 1000000L + uSec_; 
            }

            long overflow = uSec_ / 1000000L;
            if (overflow > 0)
            {
               sec_ += overflow;
               uSec_ -= overflow * 1000000L;
            }
         }
   };


   /**
    * Timeout utility class
    */
   class TimeoutMs
   {
   public:
      // arguments:  MMTime start time, millisecond interval time
      TimeoutMs(const MMTime startTime, const unsigned long intervalMs) : 
         startTime_(startTime), 
         interval_(0, 1000*intervalMs)
      {
      }
      TimeoutMs(const MMTime startTime, const MMTime interval) : 
         startTime_(startTime), 
         interval_(interval)
      {
      }
      ~TimeoutMs()
      {
      }
      bool expired(const MMTime tnow)
      {
         MMTime elapsed = tnow - startTime_;
         return ( interval_ < elapsed );
      }
   private:
      TimeoutMs(const MM::TimeoutMs&) {}
      const TimeoutMs& operator=(const MM::TimeoutMs&) {return *this;}
      MMTime startTime_; // start time
      MMTime interval_; // interval in milliseconds
   };





   /**
    * Information about images passed from the camera
    */
   struct ImageMetadata
   {
      ImageMetadata() : exposureMs(0.0), ZUm(0.0), score(0.0) {}
      ImageMetadata(MMTime& time, double expMs) : exposureMs(expMs), timestamp(time) {}

      double exposureMs;
      MMTime timestamp;
      double ZUm;
      double score;
   };

   /**
    * Generic device interface.
    */
   class Device {
   public:
      Device() {}
      virtual ~Device() {}
 
      virtual unsigned GetNumberOfProperties() const = 0;
      virtual int GetProperty(const char* name, char* value) const = 0;  
      virtual int SetProperty(const char* name, const char* value) = 0;
      virtual bool HasProperty(const char* name) const = 0;
      virtual bool GetPropertyName(unsigned idx, char* name) const = 0;
      virtual int GetPropertyReadOnly(const char* name, bool& readOnly) const = 0;
      virtual int GetPropertyInitStatus(const char* name, bool& preInit) const = 0;
      virtual int HasPropertyLimits(const char* name, bool& hasLimits) const = 0;
      virtual int GetPropertyLowerLimit(const char* name, double& lowLimit) const = 0;
      virtual int GetPropertyUpperLimit(const char* name, double& hiLimit) const = 0;
      virtual int GetPropertyType(const char* name, MM::PropertyType& pt) const = 0;
      virtual unsigned GetNumberOfPropertyValues(const char* propertyName) const = 0;
      virtual bool GetPropertyValueAt(const char* propertyName, unsigned index, char* value) const = 0;
      /* Sequences can be used for fast acquisitions, sycnchronized by TTLs rather than
       * computer commands. 
       * Sequences of states can be uploaded to the device.  The device will cycle through
       * the uploaded list of states (triggered by an external trigger - most often coming 
       * from the camera).  If the device is capable (and ready) to do so isSequenceable will
       * be true
       */
      virtual int IsPropertySequenceable(const char* name, bool& isSequenceable) const = 0;
      /*
       * The largest sequence that can be stored in the device
       */
      virtual int GetPropertySequenceMaxLength(const char* propertyName, long& nrEvents) const = 0;
      /* 
       * Starts execution of the sequence
       */
      virtual int StartPropertySequence(const char* propertyName) = 0;
      /*
       * Stops execution of the device
       */
      virtual int StopPropertySequence(const char* propertyName) = 0;
      /*
       * remove previously added sequence
       */
      virtual int ClearPropertySequence(const char* propertyName) = 0;
      /*
       * Add one value to the sequence
       */
      virtual int AddToPropertySequence(const char* propertyName, const char* value) = 0;
      /*
       * Signal that we are done sending sequence values so that the adapter can send the whole sequence to the device
       */
      virtual int SendPropertySequence(const char* propertyName) = 0; 

      virtual bool GetErrorText(int errorCode, char* errMessage) const = 0;
      virtual bool Busy() = 0;
      virtual double GetDelayMs() const = 0;
      virtual void SetDelayMs(double delay) = 0;
      virtual bool UsesDelay() = 0;

      /*
       * library handle management (for use only in the client code)
       */
      // TODO Get/SetModuleHandle() is no longer used; can remove at a
      // convenient time.
      virtual HDEVMODULE GetModuleHandle() const = 0;
      virtual void SetModuleHandle(HDEVMODULE hLibraryHandle) = 0;
      virtual void SetLabel(const char* label) = 0;
      virtual void GetLabel(char* name) const = 0;
      virtual void SetModuleName(const char* moduleName) = 0;
      virtual void GetModuleName(char* moduleName) const = 0;
      virtual void SetDescription(const char* description) = 0;
      virtual void GetDescription(char* description) const = 0;

      virtual int Initialize() = 0;
      /**
       * Shuts down (unloads) the device.
       * Required by the MM::Device API.
       * Ideally this method will completely unload the device and release all resources.
       * Shutdown() may be called multiple times in a row.
       * After Shutdown() we should be allowed to call Initialize() again to load the device
       * without causing problems.
       */
      virtual int Shutdown() = 0;
   
      virtual DeviceType GetType() const = 0;
      virtual void GetName(char* name) const = 0;
      virtual void SetCallback(Core* callback) = 0;

      // acq context api
      virtual int AcqBefore() = 0;
      virtual int AcqAfter() = 0;
      virtual int AcqBeforeFrame() = 0;
      virtual int AcqAfterFrame() = 0;
      virtual int AcqBeforeStack() = 0;
      virtual int AcqAfterStack() = 0;

      //device discovery API
      virtual MM::DeviceDetectionStatus DetectDevice(void) = 0;

      // hub-peripheral relationship
      virtual void SetParentID(const char* parentId) = 0;
      virtual void GetParentID(char* parentID) const = 0;
      // virtual void SetID(const char* id) = 0;
      // virtual void GetID(char* id) const = 0;
   };

   /** 
    * Camera API
    */
   class Camera : public Device {
   public:
      Camera() {}
      virtual ~Camera() {}

      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = CameraDevice;

      // Camera API
      /**
       * Performs exposure and grabs a single image.
       * Required by the MM::Camera API.
       *
       * SnapImage should start the image exposure in the camera and block until
       * the exposure is finished.  It should not wait for read-out and transfer of data.
       * Return DEVICE_OK on succes, error code otherwise.
       */
      virtual int SnapImage() = 0;
      /**
       * Returns pixel data.
       * Required by the MM::Camera API.
       * GetImageBuffer will be called shortly after SnapImage returns.  
       * Use it to wait for camera read-out and transfer of data into memory
       * Return a pointer to a buffer containing the image data
       * The calling program will assume the size of the buffer based on the values
       * obtained from GetImageBufferSize(), which in turn should be consistent with
       * values returned by GetImageWidth(), GetImageHight() and GetImageBytesPerPixel().
       * The calling program allso assumes that camera never changes the size of
       * the pixel buffer on its own. In other words, the buffer can change only if
       * appropriate properties are set (such as binning, pixel type, etc.)
       * Multi-Channel cameras should return the content of the first channel in this call.
       *
       */
      virtual const unsigned char* GetImageBuffer() = 0;
      /**
       * Returns pixel data for cameras with multiple channels.
       * See description for GetImageBuffer() for details.
       * Use this overloaded version for cameras with multiple channels
       * When calling this function for a single channel camera, this function
       * should return the content of the imagebuffer as returned by the function
       * GetImageBuffer().  This behavior is implemented in the DeviceBase.
       * When GetImageBuffer() is called for a multi-channel camera, the 
       * camera adapter should return the ImageBuffer for the first channel
       * @param channelNr Number of the channel for which the image data are requested.
       */
      virtual const unsigned char* GetImageBuffer(unsigned channelNr) = 0;
      /**
       * Returns pixel data with interleaved RGB pixels in 32 bpp format
       */
      virtual const unsigned int* GetImageBufferAsRGB32() = 0;
      /**
       * Returns the number of components in this image.  This is '1' for grayscale cameras,
       * and '4' for RGB cameras.
       */
      virtual unsigned GetNumberOfComponents() const = 0;
      /**
       * Returns the name for each component 
       */
      virtual int GetComponentName(unsigned component, char* name) = 0;
      /**
       * Returns the number of simultaneous channels that camera is capaable of.
       * This should be used by devices capable of generating mutiple channels of imagedata simultanuously.
       * Note: this should not be used by color cameras (use getNumberOfComponents instead).
       */
      virtual int unsigned GetNumberOfChannels() const = 0;
      /**
       * Returns the name for each Channel.
       * An implementation of this function is provided in DeviceBase.h.  It will return an empty string
       */
      virtual int GetChannelName(unsigned channel, char* name) = 0;
      /**
       * Returns the size in bytes of the image buffer.
       * Required by the MM::Camera API.
       * For multi-channel cameras, return the size of a single channel
       */
      virtual long GetImageBufferSize()const = 0;
      /**
       * Returns image buffer X-size in pixels.
       * Required by the MM::Camera API.
       */
      virtual unsigned GetImageWidth() const = 0;
      /**
       * Returns image buffer Y-size in pixels.
       * Required by the MM::Camera API.
       */
      virtual unsigned GetImageHeight() const = 0;
      /**
       * Returns image buffer pixel depth in bytes.
       * Required by the MM::Camera API.
       */
      virtual unsigned GetImageBytesPerPixel() const = 0;
      /**
       * Returns the bit depth (dynamic range) of the pixel.
       * This does not affect the buffer size, it just gives the client application
       * a guideline on how to interpret pixel values.
       * Required by the MM::Camera API.
       */
      virtual unsigned GetBitDepth() const = 0;
      /**
       * Returns binnings factor.  Used to calculate current pixelsize
       * Not appropriately named.  Implemented in DeviceBase.h
       */
      virtual double GetPixelSizeUm() const = 0;
      /**
       * Returns the current binning factor.
       */
      virtual int GetBinning() const = 0;
      /**
       * Sets binning factor.
       */
      virtual int SetBinning(int binSize) = 0;
      /**
       * Sets exposure in milliseconds.
       */
      virtual void SetExposure(double exp_ms) = 0;
      /**
       * Returns the current exposure setting in milliseconds.
       */
      virtual double GetExposure() const = 0;
      /**
       * Sets the camera Region Of Interest.
       * Required by the MM::Camera API.
       * This command will change the dimensions of the image.
       * Depending on the hardware capabilities the camera may not be able to configure the
       * exact dimensions requested - but should try do as close as possible.
       * If the hardware does not have this capability the software should simulate the ROI by
       * appropriately cropping each frame.
       * @param x - top-left corner coordinate
       * @param y - top-left corner coordinate
       * @param xSize - width
       * @param ySize - height
       */
      virtual int SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize) = 0; 
      /**
       * Returns the actual dimensions of the current ROI.
       */
      virtual int GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize) = 0;
      /**
       * Resets the Region of Interest to full frame.
       */
      virtual int ClearROI() = 0;
      /**
       * Starts continuous acquisition.
       */
      virtual int StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow) = 0;
      /**
       * Starts Sequence Acquisition with given interval.  
       * Most camera adapters will ignore this number
       * */
      virtual int StartSequenceAcquisition(double interval_ms) = 0;
      /**
       * Stops an ongoing sequence acquisition
       */
      virtual int StopSequenceAcquisition() = 0;
      /**
       * Sets up the camera so that Sequence acquisition can start without delay
       */
      virtual int PrepareSequenceAcqusition() = 0;
      /**
       * Flag to indicate whether Sequence Acquisition is currently running.
       * Return true when Sequence acquisition is activce, false otherwise
       */
      virtual bool IsCapturing() = 0;

      /**
       * Get the metadata tags stored in this device.
       * These tags will automatically be add to the metadata of an image inserted 
       * into the circular buffer
       *
       */
      virtual void GetTags(char* serializedMetadata) = 0;

      /**
       * Adds new tag or modifies the value of an existing one 
       * These will automatically be added to images inserted into the circular buffer.
       * Use this mechanism for tags that do not change often.  For metadata that
       * change often, create an instance of metadata yourself and add to one of 
       * the versions of the InsertImage function
       */
      virtual void AddTag(const char* key, const char* deviceLabel, const char* value) = 0;

      /**
       * Removes an existing tag from the metadata assoicated with this device
       * These tags will automatically be add to the metadata of an image inserted 
       * into the circular buffer
       */
      virtual void RemoveTag(const char* key) = 0;

      /*
       * Returns whether a camera's exposure time can be sequenced.
       * If returning true, then a Camera adapter class should also inherit
       * the SequenceableExposure class and implement its methods.
       */
      virtual int IsExposureSequenceable(bool& isSequenceable) const = 0;

      // Sequence functions
      // Sequences can be used for fast acquisitions, sycnchronized by TTLs rather than
      // computer commands. 
      // Sequences of exposures can be uploaded to the camera.  The camera will cycle through
      // the uploaded list of exposures (triggered by either an internal or 
      // external trigger).  If the device is capable (and ready) to do so isSequenceable will
      // be true. If your device can not execute this (true for most cameras)
      // simply set IsExposureSequenceable to false
      virtual int GetExposureSequenceMaxLength(long& nrEvents) const = 0;
      virtual int StartExposureSequence() = 0;
      virtual int StopExposureSequence() = 0;
      // Remove all values in the sequence
      virtual int ClearExposureSequence() = 0;
      // Add one value to the sequence
      virtual int AddToExposureSequence(double exposureTime_ms) = 0;
      // Signal that we are done sending sequence values so that the adapter can send the whole sequence to the device
      virtual int SendExposureSequence() const = 0;
   };

   /** 
    * Shutter API
    */
   class Shutter : public Device
   {
   public:
      Shutter() {}
      virtual ~Shutter() {}
   
      // Device API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = ShutterDevice;
   
      // Shutter API
      virtual int SetOpen(bool open = true) = 0;
      virtual int GetOpen(bool& open) = 0;
      /**
       * Opens the shutter for the given duration, then closes it again.
       * Currently not implemented in any shutter adapters
       */
      virtual int Fire(double deltaT) = 0;
   };

   /** 
    * Single axis stage API
    */
   class Stage : public Device
   {
   public:
      Stage() {}
      virtual ~Stage() {}
   
      // Device API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = StageDevice;
   
      // Stage API
      virtual int SetPositionUm(double pos) = 0;
      virtual int SetRelativePositionUm(double d) = 0;
      virtual int Move(double velocity) = 0;
      virtual int SetAdapterOriginUm(double d) = 0;
      virtual int GetPositionUm(double& pos) = 0;
      virtual int SetPositionSteps(long steps) = 0;
      virtual int GetPositionSteps(long& steps) = 0;
      virtual int SetOrigin() = 0;
      virtual int GetLimits(double& lower, double& upper) = 0;
      /*
       * Returns whether a stage can be sequenced (synchronized by TTLs)
       * If returning true, then a Stage class should also inherit
       * the SequenceableStage class and implement its methods.
       */
      virtual int IsStageSequenceable(bool& isSequenceable) const = 0;

      // Check if a stage has continuous focusing capability (positions can be set while continuous focus runs).
      virtual bool IsContinuousFocusDrive() const = 0;

      // Sequence functions
      // Sequences can be used for fast acquisitions, sycnchronized by TTLs rather than
      // computer commands. 
      // Sequences of positions can be uploaded to the stage.  The device will cycle through
      // the uploaded list of states (triggered by an external trigger - most often coming 
      // from the camera).  If the device is capable (and ready) to do so isSequenceable will
      // be true. If your device can not execute this (true for most stages)
      // simply set isSequenceable to false
      virtual int GetStageSequenceMaxLength(long& nrEvents) const = 0;
      virtual int StartStageSequence() = 0;
      virtual int StopStageSequence() = 0;
      // Remove all values in the sequence
      virtual int ClearStageSequence() = 0;
      // Add one value to the sequence
      virtual int AddToStageSequence(double position) = 0;
      // Signal that we are done sending sequence values so that the adapter can send the whole sequence to the device
      virtual int SendStageSequence() = 0;
   };

   /** 
    * Dual axis stage API
    */
   class XYStage : public Device
   {
   public:
      XYStage() {}
      virtual ~XYStage() {}

      // Device API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = XYStageDevice;

      // XYStage API
      // it is recommended that device adapters implement the  "Steps" methods taking
      // long integers but leave the default implementations (in DeviceBase.h) for
      // the "Um" methods taking doubles.  The latter utilize directionality and origin
      // settings set by user and operate via the "Steps" methods.  The step size is
      // the inherent minimum distance/step and should be defined by the adapter.
      virtual int SetPositionUm(double x, double y) = 0;
      virtual int SetRelativePositionUm(double dx, double dy) = 0;
      virtual int SetAdapterOriginUm(double x, double y) = 0;
      virtual int GetPositionUm(double& x, double& y) = 0;
      virtual int GetLimitsUm(double& xMin, double& xMax, double& yMin, double& yMax) = 0;
      virtual int Move(double vx, double vy) = 0;

      virtual int SetPositionSteps(long x, long y) = 0;
      virtual int GetPositionSteps(long& x, long& y) = 0;
      virtual int SetRelativePositionSteps(long x, long y) = 0;
      virtual int Home() = 0;
      virtual int Stop() = 0;
      virtual int SetOrigin() = 0;//jizhen, 4/12/2007
      virtual int GetStepLimits(long& xMin, long& xMax, long& yMin, long& yMax) = 0;
      virtual double GetStepSizeXUm() = 0;
      virtual double GetStepSizeYUm() = 0;
       /*
       * Returns whether a stage can be sequenced (synchronized by TTLs)
       * If returning true, then an XYStage class should also inherit
       * the SequenceableXYStage class and implement its methods.
       */
      virtual int IsXYStageSequenceable(bool& isSequenceable) const = 0;     
      // Sequence functions
      // Sequences can be used for fast acquisitions, sycnchronized by TTLs rather than
      // computer commands. 
      // Sequences of positions can be uploaded to the XY stage.  The device will cycle through
      // the uploaded list of states (triggered by an external trigger - most often coming 
      // from the camera).  If the device is capable (and ready) to do so isSequenceable will
      // be true. If your device can not execute this (true for most XY stages
      // simply set isSequenceable to false
      virtual int GetXYStageSequenceMaxLength(long& nrEvents) const = 0;
      virtual int StartXYStageSequence() = 0;
      virtual int StopXYStageSequence() = 0;
      // Remove all values in the sequence
      virtual int ClearXYStageSequence() = 0;
      // Add one value to the sequence
      virtual int AddToXYStageSequence(double positionX, double positionY) = 0;
      // Signal that we are done sending sequence values so that the adapter can send the whole sequence to the device
      virtual int SendXYStageSequence() = 0;

   };

   /**
    * State device API, e.g. filter wheel, objective turret, etc.
    */
   class State : public Device
   {
   public:
      State() {}
      virtual ~State() {}
      
      // MMDevice API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = StateDevice;
      
      // MMStateDevice API
      virtual int SetPosition(long pos) = 0;
      virtual int SetPosition(const char* label) = 0;
      virtual int GetPosition(long& pos) const = 0;
      virtual int GetPosition(char* label) const = 0;
      virtual int GetPositionLabel(long pos, char* label) const = 0;
      virtual int GetLabelPosition(const char* label, long& pos) const = 0;
      virtual int SetPositionLabel(long pos, const char* label) = 0;
      virtual unsigned long GetNumberOfPositions() const = 0;
      virtual int SetGateOpen(bool open = true) = 0;
      virtual int GetGateOpen(bool& open) = 0;
   };

   /**
    * Programmable I/O device API, for programmable I/O boards with patterns
    * and sequences
    */
   class ProgrammableIO : public Device
   {
   public:
      ProgrammableIO() {}
      virtual ~ProgrammableIO() {}
      
      // MMDevice API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = ProgrammableIODevice;
      
      // ProgrammableIO API

      /**
       * Stores the current set of properties (state) into a specified slot.
       * The device should automatically resize its internal pattern array to the highest
       * requested index, or return error if it can't.
       * NOTE: the device must handle the concept of the 'undefined'or 'default' pattern in order to
       * pad the pattern array if necessary
       */
      virtual int DefineCurrentStateAsPattern(long index) = 0;

      /**
       * Cycles through the pattern array.
       * Convenient to use for external triggering.
       */
      virtual int SetNextPattern() = 0;

      /**
       * Set the pattern based either on the index.
       */
      virtual int SetPattern(long index) = 0;
      /**
       * Set the pattern based either on the label.
       */
      virtual int SetPattern(const char* label) = 0;

      /**
       * Get current pattern index.
       */
      virtual int GetPattern(long& index) const = 0;
      /**
       * Get current pattern label.
       */
      virtual int GetPattern(char* label) const = 0;

      /**
       * Get the label assigned to a specific position.
       */
      virtual int GetPatternLabel(long pos, char* label) const = 0;
      /**
       * Assign a label to the specific position.
       */
      virtual int SetPatternLabel(long pos, const char* label) = 0;

      /**
       * Returns size of the pattern array.
       */
      virtual unsigned long GetNumberOfPatterns() const = 0;
   };


   /**
    * Serial port API.
    */
   class Serial : public Device
   {
   public:
      Serial() {}
      virtual ~Serial() {}
      
      // MMDevice API
      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = SerialDevice;
      
      // Serial API
      virtual PortType GetPortType() const = 0;
      virtual int SetCommand(const char* command, const char* term) = 0;
      virtual int GetAnswer(char* txt, unsigned maxChars, const char* term) = 0;
      virtual int Write(const unsigned char* buf, unsigned long bufLen) = 0;
      virtual int Read(unsigned char* buf, unsigned long bufLen, unsigned long& charsRead) = 0;
      virtual int Purge() = 0; 
   };

   /**
    * Auto-focus device API.
    */
   class AutoFocus : public Device
   {
   public:
      AutoFocus() {}
      virtual ~AutoFocus() {}
      
      // MMDevice API
      virtual DeviceType GetType() const {return AutoFocusDevice;}
      static const DeviceType Type = AutoFocusDevice;

      // AutoFocus API
      virtual int SetContinuousFocusing(bool state) = 0;
      virtual int GetContinuousFocusing(bool& state) = 0;
      virtual bool IsContinuousFocusLocked() = 0;
      virtual int FullFocus() = 0;
      virtual int IncrementalFocus() = 0;
      virtual int GetLastFocusScore(double& score) = 0;
      virtual int GetCurrentFocusScore(double& score) = 0;
      virtual int AutoSetParameters() = 0;
      virtual int GetOffset(double &offset) = 0;
      virtual int SetOffset(double offset) = 0;
   };

   /**
    * Streaming API.
    */
   class ImageStreamer : public Device
   {
   public:
      ImageStreamer();
      virtual ~ImageStreamer();

      // MM Device API
      virtual DeviceType GetType() const {return ImageStreamerDevice;}
      static const DeviceType Type = ImageStreamerDevice;

      // image streaming API
      virtual int OpenContext(unsigned width, unsigned height, unsigned depth, const char* path, const Metadata* contextMd = 0);
      virtual int CloseContext();
      virtual int SaveImage(unsigned char* buffer, unsigned width, unsigned height, unsigned depth, const Metadata* imageMd = 0);
   };

   /**
    * Image processor API.
    */
   class ImageProcessor : public Device
   {
      public:
         ImageProcessor() {}
         virtual ~ImageProcessor() {}

      // MMDevice API
      virtual DeviceType GetType() const {return ImageProcessorDevice;}
      static const DeviceType Type = ImageProcessorDevice;

      // image processor API
      virtual int Process(unsigned char* buffer, unsigned width, unsigned height, unsigned byteDepth) = 0;

      
   };

   /**
    * ADC and DAC interface.
    */
   class SignalIO : public Device
   {
   public:
      SignalIO() {}
      virtual ~SignalIO() {}

      // MMDevice API
      virtual DeviceType GetType() const {return SignalIODevice;}
      static const DeviceType Type = SignalIODevice;

      // signal io API
      virtual int SetGateOpen(bool open = true) = 0;
      virtual int GetGateOpen(bool& open) = 0;
      virtual int SetSignal(double volts) = 0;
      virtual int GetSignal(double& volts) = 0;
      virtual int GetLimits(double& minVolts, double& maxVolts) = 0;

      /**
       * Lets the UI know whether or not this DA device accepts sequences
       * If the device is sequenceable, it is usually best to add a property through which 
       * the user can set "isSequenceable", since only the user knows whether the device
       * is actually connected to a trigger source.
       * If isDASequenceable returns true, the device adapter must
       * also inherit the SequenceableDA class and provide method
       * implementations.
       * @param isSequenceable signals whether other sequence functions will work
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int IsDASequenceable(bool& isSequenceable) const = 0;

      // Sequence functions
      // Sequences can be used for fast acquisitions, synchronized by TTLs rather than
      // computer commands. 
      // Sequences of voltages can be uploaded to the DA.  The device will cycle through
      // the uploaded list of voltages (triggered by an external trigger - most often coming 
      // from the camera).  If the device is capable (and ready) to do so isSequenceable will
      // be true. If your device can not execute this simply set isSequenceable to false
      /**
       * Returns the maximum length of a sequence that the hardware can store
       * @param nrEvents max length of sequence
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int GetDASequenceMaxLength(long& nrEvents) const = 0; 
      /**
       * Tells the device to start running a sequnece (i.e. start switching between voltages 
       * send previously, triggered by a TTL
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int StartDASequence() = 0;
      /**
       * Tells the device to stop running the sequence
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int StopDASequence() = 0;
      /**
       * Clears the DA sequnce from the device and the adapter.
       * If this functions is not called in between running 
       * two sequences, it is expected that the same sequence will run twice.
       * To upload a new sequence, first call this functions, then call AddToDASequence(double
       * voltage) as often as needed.
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int ClearDASequence() = 0;

      /**
       * Adds a new data point (voltgae) to the sequence
       * The data point can eithed be added to a representation of the sequence in the 
       * adapter, or it can be directly written to the device
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int AddToDASequence(double voltage) = 0;
      /**
       * Sends the complete sequence to the device
       * If the individual data points were already send to the device, there is 
       * nothing to be done.
       * @return errorcode (DEVICE_OK if no error)
       */
      virtual int SendDASequence() = 0;

   };

   /**
   * Devices that can change magnification of the system
   */
   class Magnifier : public Device
   {
   public:
      Magnifier() {}
      virtual ~Magnifier() {}

      // MMDevice API
      virtual DeviceType GetType() const {return MagnifierDevice;}
      static const DeviceType Type = MagnifierDevice;

      virtual double GetMagnification() = 0;
   };


   /** 
    * SLM API
    */
   class SLM : public Device
   {
   public:
      SLM() {}
      virtual ~SLM() {}

      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = SLMDevice;

      // SLM API
      /**
       * Load the image into the SLM device adapter.
       */
      virtual int SetImage(unsigned char * pixels) = 0;

     /**
       * Load a 32-bit image into the SLM device adapter.
       */
      virtual int SetImage(unsigned int * pixels) = 0;

      /**
       * Command the SLM to display the loaded image.
       */
      virtual int DisplayImage() = 0;

      /**
       * Command the SLM to display one 8-bit intensity.
       */
      virtual int SetPixelsTo(unsigned char intensity) = 0;

      /**
       * Command the SLM to display one 32-bit color.
       */
      virtual int SetPixelsTo(unsigned char red, unsigned char green, unsigned char blue) = 0;

      /**
       * Get the SLM width in pixels.
       */
      virtual unsigned GetWidth() = 0;

      /**
       * Get the SLM height in pixels.
       */
      virtual unsigned GetHeight() = 0;

      /**
       * Get the SLM number of components (colors).
       */
      virtual unsigned GetNumberOfComponents() = 0;

      /**
       * Get the SLM number of bytes per pixel.
       */
      virtual unsigned GetBytesPerPixel() = 0;

   };

   /**
    * Galvo API
    */
   class Galvo : public Device
   {
   public:
      Galvo() {}
      virtual ~Galvo() {}

      virtual DeviceType GetType() const {return Type;}
      static const DeviceType Type = GalvoDevice;
      
   //Galvo API:
      virtual int PointAndFire(double x, double y, double time_us) = 0;
      virtual int SetSpotInterval(double pulseInterval_us) = 0;
      virtual int SetPosition(double x, double y) = 0;
      virtual int GetPosition(double& x, double& y) = 0;
      virtual int SetIlluminationState(bool on) = 0;
      virtual double GetXRange() = 0;
      virtual double GetYRange() = 0;
      virtual int AddPolygonVertex(int polygonIndex, double x, double y) = 0;
      virtual int DeletePolygons() = 0;
      virtual int RunSequence() = 0;
      virtual int LoadPolygons() = 0;
      virtual int SetPolygonRepetitions(int repetitions) = 0;
      virtual int RunPolygons() = 0;
      virtual int StopSequence() = 0;
      virtual int GetChannel(char* channelName) = 0;
   };

   /**
    * Command monitoring and control device.
    */
   class CommandDispatch : public Device
   {
   public:
      CommandDispatch() {}
      virtual ~CommandDispatch() {}

      // MMDevice API
      virtual DeviceType GetType() const {return CommandDispatchDevice;}
      static const DeviceType Type = CommandDispatchDevice;

      // Command dispatch API
      virtual int LogCommand(const char* logCommandText) = 0;
   };

   /**
    * HUB device. Used for complex uber-device functionality in microscope stands
    * and managing auto-configuration (discovery) of other devices
    */
   class Hub : public Device
   {
   public:
      Hub() {}
      virtual ~Hub() {}

      // MMDevice API
      virtual DeviceType GetType() const {return HubDevice;}
      static const DeviceType Type = HubDevice;

      /**
       * Attempts to detect child device hardware by communicating with hub hardware.
       * If any child hardware is detected, causes module to instantiate
       * appropriate child Device instance(s).
       */
      virtual int DetectInstalledDevices() = 0;

      /**
       * Removes all Device instances that were created by DetectInstalledDevices()
       */
      virtual void ClearInstalledDevices() = 0;

      /**
       * Returns the number of child Devices after DetectInstalledDevices was called.
       */
      virtual unsigned GetNumberOfInstalledDevices() = 0;
      
      /**
       * Returns a pointer to the Device with index devIdx. 0 <= devIdx < GetNumberOfInstalledDevices().
       */
      virtual Device* GetInstalledDevice(int devIdx) = 0;
   };

   /**
    * Callback API to the core control module.
    * Devices use this abstract interface to use Core services
    */
   class Core
   {
   public:
      Core() {}
      virtual ~Core() {}

      virtual int LogMessage(const Device* caller, const char* msg, bool debugOnly) const = 0;
      virtual Device* GetDevice(const Device* caller, const char* label) = 0;
      virtual int GetDeviceProperty(const char* deviceName, const char* propName, char* value) = 0;
      virtual int SetDeviceProperty(const char* deviceName, const char* propName, const char* value) = 0;
      virtual void GetLoadedDeviceOfType(const Device* caller, MM::DeviceType devType, char* pDeviceName, const unsigned int deviceIterator) = 0;
      virtual int SetSerialProperties(const char* portName,
                                      const char* answerTimeout,
                                      const char* baudRate,
                                      const char* delayBetweenCharsMs,
                                      const char* handshaking,
                                      const char* parity,
                                      const char* stopBits) = 0;
      virtual int SetSerialCommand(const Device* caller, const char* portName, const char* command, const char* term) = 0;
      virtual int GetSerialAnswer(const Device* caller, const char* portName, unsigned long ansLength, char* answer, const char* term) = 0;
      virtual int WriteToSerial(const Device* caller, const char* port, const unsigned char* buf, unsigned long length) = 0;
      virtual int ReadFromSerial(const Device* caller, const char* port, unsigned char* buf, unsigned long length, unsigned long& read) = 0;
      virtual int PurgeSerial(const Device* caller, const char* portName) = 0;
      virtual MM::PortType GetSerialPortType(const char* portName) const = 0;
      virtual int OnStatusChanged(const Device* caller) = 0;
      virtual int OnFinished(const Device* caller) = 0;
      virtual int OnPropertiesChanged(const Device* caller) = 0;
      /**
       * Callback to signal the UI that a property changed
       * The Core will check if groups or pixel size changed as a consequence of 
       * the change of this property and inform the UI
       */
      virtual int OnPropertyChanged(const Device* caller, const char* propName, const char* propValue) = 0;
      /**
       * If the stage is aware that it has reached a new position, it should call
       * this callback to signal the UI
       */
      virtual int OnStagePositionChanged(const Device* caller, double pos) = 0;
      /**
       * If an XY stage is aware that it has reached a new position, it should call
       * this callback to signal the UI
       */
      virtual int OnXYStagePositionChanged(const Device* caller, double xPos, double yPos) = 0;
      /**
       * When the exposure time has changed, use this callback to inform the UI
       */
      virtual int OnExposureChanged(const Device* caller, double newExposure) = 0;
      /**
       * Magnifiers can use this to signal changes in magnification
       */
      virtual int OnMagnifierChanged(const Device* caller) = 0;

      virtual unsigned long GetClockTicksUs(const Device* caller) = 0;
      virtual MM::MMTime GetCurrentMMTime() = 0;

      // continuous acquisition
      virtual int OpenFrame(const Device* caller) = 0;
      virtual int CloseFrame(const Device* caller) = 0;
      virtual int AcqFinished(const Device* caller, int statusCode) = 0;
      virtual int PrepareForAcq(const Device* caller) = 0;
      virtual int InsertImage(const Device* caller, const ImgBuffer& buf) = 0;
      virtual int InsertImage(const Device* caller, const unsigned char* buf, unsigned width, unsigned height, unsigned byteDepth, const Metadata* md = 0, const bool doProcess = true) = 0;
      virtual int InsertImage(const Device* caller, const unsigned char* buf, unsigned width, unsigned height, unsigned byteDepth, const char* serializedMetadata, const bool doProcess = true) = 0;
      virtual void ClearImageBuffer(const Device* caller) = 0;
      virtual bool InitializeImageBuffer(unsigned channels, unsigned slices, unsigned int w, unsigned int h, unsigned int pixDepth) = 0;
      virtual int InsertMultiChannel(const Device* caller, const unsigned char* buf, unsigned numChannels, unsigned width, unsigned height, unsigned byteDepth, Metadata* md = 0) = 0;
      virtual void SetAcqStatus(const Device* caller, int statusCode) = 0;
      virtual long getImageBufferTotalFrames() = 0;
      virtual long getImageBufferFreeFrames() = 0;

      // autofocus
      virtual const char* GetImage() = 0;
      virtual int GetImageDimensions(int& width, int& height, int& depth) = 0;
      virtual int GetFocusPosition(double& pos) = 0;
      virtual int SetFocusPosition(double pos) = 0;
      virtual int MoveFocus(double velocity) = 0;
      virtual int SetXYPosition(double x, double y) = 0;
      virtual int GetXYPosition(double& x, double& y) = 0;
      virtual int MoveXYStage(double vX, double vY) = 0;
      virtual int SetExposure(double expMs) = 0;
      virtual int GetExposure(double& expMs) = 0;
      virtual int SetConfig(const char* group, const char* name) = 0;
      virtual int GetCurrentConfig(const char* group, int bufLen, char* name) = 0;
      virtual int GetChannelConfig(char* channelConfigName, const unsigned int channelConfigIterator) = 0;

      // direct access to specific device types
      virtual MM::ImageProcessor* GetImageProcessor(const MM::Device* caller) = 0;
      virtual MM::AutoFocus* GetAutoFocus(const MM::Device* caller) = 0;
      virtual MM::Hub* GetParentHub(const MM::Device* caller) const = 0;
      virtual MM::Device* GetPeripheral(const MM::Device* caller, unsigned idx) const = 0;
      virtual unsigned GetNumberOfPeripherals(const MM::Device* caller) = 0;

      virtual MM::State* GetStateDevice(const MM::Device* caller, const char* deviceName) = 0;
      virtual MM::SignalIO* GetSignalIODevice(const MM::Device* caller, const char* deviceName) = 0;

      // asynchronous error handling
      virtual void NextPostedError(int& /*errorCode*/, char* /*pMessage*/, int /*maxlen*/, int& /*messageLength*/) = 0;
      virtual void PostError(const int, const char* ) = 0;
      virtual void ClearPostedErrors( void) = 0;

      // thread locking
      virtual MMThreadLock* getModuleLock(const MM::Device* caller) = 0;
      virtual void removeModuleLock(const MM::Device* caller) = 0;
   
   };

} // namespace MM

#endif //MMMMDEVICE_H

