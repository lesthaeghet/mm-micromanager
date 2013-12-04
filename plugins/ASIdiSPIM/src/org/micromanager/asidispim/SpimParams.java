///////////////////////////////////////////////////////////////////////////////
//FILE:          SpimParams.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.asidispim;

import java.util.HashMap;
import java.util.prefs.Preferences;
import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;


/**
 *
 * @author nico
 */
public class SpimParams {
   private Devices devices_;
   private ScriptInterface gui_;
   private CMMCore core_;
   
   private final HashMap<String, Integer> integerInfo_;
   private final HashMap<String, String> sidesInfo_;
   private final HashMap<String, Float> floatInfo_;
   private Preferences prefs_;
   
   public static final String NSIDES = "NSides";
   public static final String NREPEATS = "NRepeats";
   public static final String NSHEETSA = "NSheetsA";
   public static final String NSHEETSB = "NSHeetsB";
   public static final String NLINESCANSPERSHEETA = "NLinesScansPerSheetA";
   public static final String NLINESCANSPERSHEETB = "NLinesScansPerSheetB";
   public static final String LINESCANPERIODA = "LineScanPeriodA";
   public static final String LINESCANPERIODB = "LineScanPeriodB";
   public static final String DELAYBEFORESHEETA = "DelayBeforeSheetA";
   public static final String DELAYBEFORESHEETB = "DelayBeforeSheetB";
   public static final String DELAYBEFORESIDEA = "DelayBeforeSideA";
   public static final String DELAYBEFORESIDEB = "DelayBeforeSideB";
   private static final String[] INTS = {
         NSIDES, NREPEATS, NSHEETSA, NSHEETSB, NLINESCANSPERSHEETA, 
         NLINESCANSPERSHEETB, LINESCANPERIODA, LINESCANPERIODB,
         DELAYBEFORESHEETA, DELAYBEFORESHEETB, DELAYBEFORESIDEA, DELAYBEFORESIDEB};

   public static final String FIRSTSIDE = "FirstSide";
   public static final String A = "A";
   public static final String B = "B";
   

   public SpimParams (ScriptInterface gui, Devices devices) {
      gui_ = gui;
      core_ = gui_.getMMCore();
      devices_ = devices;
      
      prefs_ = Preferences.userNodeForPackage(this.getClass());
      
      integerInfo_ = new HashMap<String, Integer>();
      sidesInfo_ = new HashMap<String, String>();
      floatInfo_ = new HashMap<String, Float>();
   
      for  (String iInfo : INTS) {
         integerInfo_.put(iInfo, prefs_.getInt(iInfo, 1));
         int res;
         String device = null;
         try {
         if (iInfo.equals(LINESCANPERIODA)) {
             device = Devices.GALVOA;
             res = getLineScanProp(device, devices_.getAxisDirInfo(Devices.FASTAXISADIR));
             integerInfo_.put(iInfo, res);
         }
         if (iInfo.equals(LINESCANPERIODB)) {
             device = Devices.GALVOB;
             res = getLineScanProp(device, devices_.getAxisDirInfo(Devices.FASTAXISBDIR));
             integerInfo_.put(iInfo, res);
         }
            
         } catch (Exception ex) {
            ReportingUtils.showError("Problem communicating with device: " + 
                    devices_.getDeviceInfo(device));
         }
           
      }
      
      sidesInfo_.put(FIRSTSIDE, prefs_.get(FIRSTSIDE, "A"));
   }
   
   public void putIntInfo(String key, int val) {
      synchronized (integerInfo_) {
         if (integerInfo_.containsKey(key)) {
            String mma = null;
            String propName = null;
            try {
               if (key.equals(LINESCANPERIODA)) {
                  mma = devices_.getDeviceInfo(Devices.GALVOA);
                  propName = "SingleAxis"
                          + devices_.getAxisDirInfo(Devices.FASTAXISADIR) + "Period(ms)";
                  core_.setProperty(mma, propName, val);
               }
               if (key.equals(LINESCANPERIODB)) {
                  mma = devices_.getDeviceInfo(Devices.FASTAXISBDIR);
                  propName = "SingleAxis"
                          + devices_.getAxisDirInfo(Devices.GALVOB) + "Period(ms)";
                  core_.setProperty(mma, propName, val);
               }
               integerInfo_.put(key, val);
            } catch (Exception ex) {
               ReportingUtils.showError("Error setting property " + propName + 
                       "in device" + mma);
            }
         }
      }
   }

   private int getLineScanProp(String deviceName, String fastAxis) throws Exception {
      String mm = devices_.getDeviceInfo(deviceName);
      String propName = "SingleAxis"
              + fastAxis + "Period(ms)";
      String result =  core_.getProperty(mm, propName);
      return NumberUtils.coreStringToInt(result);
   }
   
   public int getIntInfo(String key) {
      synchronized (integerInfo_) {
         return integerInfo_.get(key);
      }
   }
   
   public void putSidesInfo(String key, String val) {
      synchronized (sidesInfo_) {
         if (val.equals(A) || val.equals(B) && sidesInfo_.containsKey(key)) {
            sidesInfo_.put(key, val);
         }
      }
   }

   public String getSidesInfo(String key) {
      synchronized (sidesInfo_) {
         return sidesInfo_.get(key);
      }
   }
   
   public void putFloatInfo(String key, float val) {
      synchronized (floatInfo_) {
         if (floatInfo_.containsKey(key)) {
            floatInfo_.put(key, val);
         }
      }
   }
   
   public float getFloatInfo(String key) {
      synchronized(floatInfo_) {
         return floatInfo_.get(key);
      }
   }
   
  /**
    * Writes info_ back to Preferences
    */
   public  void saveSettings() {
      for (String myI : INTS) {
         prefs_.putInt(myI, integerInfo_.get(myI));
      }
      prefs_.put(FIRSTSIDE, sidesInfo_.get(FIRSTSIDE));
   }
   
}
