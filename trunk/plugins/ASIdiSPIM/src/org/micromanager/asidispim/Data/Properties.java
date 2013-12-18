///////////////////////////////////////////////////////////////////////////////
//FILE:          Properties.java
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

package org.micromanager.asidispim.Data;

import java.text.ParseException;
import java.util.HashMap;

import mmcorej.CMMCore;

import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;


/**
 * Contains data and methods related to getting and setting device properties.  Ideally this is the single
 * place where properties are read and set, but currently this also happens other places.
 * One instance of this class exists in the top-level code, and is passed as a parameter anywhere it's needed.
 * @author Jon
 * @author nico
 */
public class Properties {
   private Devices devices_;
   private ScriptInterface gui_;
   private CMMCore core_;

   private final HashMap<String, PropertyData> propInfo_;  // contains all the information about the corresponding property
   
   /**
    * types of properties that Micro-Manager supports 
    * @author Jon
    */
   public static enum PropTypes { STRING, FLOAT, INTEGER };
   
   /**
    * associative class to store information about MicroManager properties
    * @author Jon
    */
   public static class PropertyData {
      public String pluginName;
      public String adapterName;
      public String pluginDevice;
      public PropTypes propType;
      
      /**
       * 
       * @param pluginName property name in Java; used as key to hashmap
       * @param adapterName property name in ASITiger.h
       * @param pluginDevice device name in Java, usually in Devices class
       * @param propType STRING, FLOAT, or INTEGER
       */
      public PropertyData(String pluginName, String adapterName, String pluginDevice, PropTypes propType) {
         this.pluginName = pluginName;
         this.adapterName = adapterName;
         this.pluginDevice = pluginDevice;
         this.propType = propType;
      }
   }

   /**
    * Constructor.  Creates HashMap but does not populate it; that should be done
    * by calling addPropertyData() method
    * @param gui
    * @param devices
    * @author Jon
    */
   public Properties (ScriptInterface gui, Devices devices) {
      gui_ = gui;
      core_ = gui_.getMMCore();
      devices_ = devices;
      
      // only create a blank hashmap to store the information now
      // other constructors should call addPropertyData() method
      propInfo_ = new HashMap<String, PropertyData>();
   }

   public void addPropertyData(PropertyData prop) {
      propInfo_.put(prop.pluginName, prop);
   }
   
   /**
    * adds an entry with property information; overwrites any previous entry under the identical name
    * no enforcement is done to prevent the same underlying property from being added multiple times
    * @param pluginName property name in Java; used as key to hashmap
    * @param adapterName property name in ASITiger.h
    * @param pluginDevice device name in Java, usually in Devices class
    * @param propType STRING, FLOAT, or INTEGER
    */
   public void addPropertyData(String pluginName, String adapterName, String pluginDevice, PropTypes propType) {
      if (propInfo_.containsKey(pluginName)) {
         propInfo_.remove(pluginDevice);
      }
      PropertyData prop = new PropertyData(pluginName, adapterName, pluginDevice, propType);
      propInfo_.put(pluginName, prop);
   }
   
   /**
    * writes string property value to the device adapter using a core call
    * @param key property name in Java; key to property hashmap
    * @param strVal value in string form, sent to core using setProperty()
    */
   public void setPropValue(String key, String strVal) {
      synchronized (propInfo_) {
         if (propInfo_.containsKey(key)) {
            PropertyData prop = propInfo_.get(key);
            String mmDevice = null;
            try {
               mmDevice = devices_.getMMDevice(prop.pluginDevice);
               core_.setProperty(mmDevice, prop.adapterName, strVal);
            } catch (Exception ex) {
               ReportingUtils.showError("Error setting string property "+ prop.adapterName + " to " + strVal + " in device " + mmDevice);
            }

         }
      }
   }

   /**
    * writes integer property value to the device adapter using a core call
    * @param key property name in Java; key to property hashmap
    * @param intVal value in integer form, sent to core using setProperty()
    */
   public void setPropValue(String key, int intVal) {
      synchronized (propInfo_) {
         if (propInfo_.containsKey(key)) {
            PropertyData prop = propInfo_.get(key);
            String mmDevice = null;
            try {
               mmDevice = devices_.getMMDevice(prop.pluginDevice);
               core_.setProperty(mmDevice, prop.adapterName, intVal);
            } catch (Exception ex) {
               ReportingUtils.showError("Error setting int property " + prop.adapterName + " in device " + mmDevice);
            }

         }
      }
   }

   /**
    * writes float property value to the device adapter using a core call
    * @param key property name in Java; key to property hashmap
    * @param intVal value in integer form, sent to core using setProperty()
    */
   public void setPropValue(String key, float floatVal) {
      synchronized (propInfo_) {
         if (propInfo_.containsKey(key)) {
            PropertyData prop = propInfo_.get(key);
            String mmDevice = null;
            try {
               mmDevice = devices_.getMMDevice(prop.pluginDevice);
               core_.setProperty(mmDevice, prop.adapterName, floatVal);
            } catch (Exception ex) {
               ReportingUtils.showError("Error setting float property " + prop.adapterName + " in device " + mmDevice);
            }

         }
      }
   }

   /**
    * gets the property hashmap data
    * @param key property name in Java; key to property hashmap
    * @return the associative array with property info
    */
   private PropertyData getPropEntry(String key) {
      return propInfo_.get(key);
   }

   /**
    * reads the property value from the device adapter using a core call
    * @param key property name in Java; key to property hashmap
    * @return value in string form, returned from core call to getProperty()
    */
   private String getPropValue(String key) {
      // TODO see if this needs synchronize statement
      String val = null;
      PropertyData prop = propInfo_.get(key);
      boolean throwException = true;
      if ((prop==null)) {
         if (throwException) {
            ReportingUtils.showError("Could not get property for " + key);
         }
         return val;
      }
      String mmDevice = devices_.getMMDevice(prop.pluginDevice);
      if (mmDevice==null || mmDevice.equals("")) {
         if (throwException) {
            ReportingUtils.showError("Could not get device for property " + key + " with " + prop.pluginDevice);
         }
      }
      else
      {
         try {
            val = core_.getProperty(mmDevice, prop.adapterName);
         } catch (Exception ex) {
            ReportingUtils.showError("Could not get property " + prop.adapterName + " from device " + mmDevice);
         }
      }
      return val;
   }

   /**
    * returns an integer value for the specified property (assumes the caller knows the property contains an integer)
    * @param key property name in Java; key to property hashmap
    * @return
    * @throws ParseException
    */
   public int getPropValueInteger(String key) {
      int val = 0;
      try {
         val = NumberUtils.coreStringToInt(getPropValue(key));
      } catch (ParseException ex) {
         ReportingUtils.showError("Could not parse int value of " + key);
      }
      catch (Exception ex) {
         ReportingUtils.showError("Could not get int value of property " + key);
      }
      return val;
   }

   /**
    * returns an float value for the specified property (assumes the caller knows the property contains a float)
    * @param key property name in Java; key to property hashmap
    * @return
    * @throws ParseException
    */
   public float getPropValueFloat(String key) {
      float val = 0;
      try {
         val = (float)NumberUtils.coreStringToDouble(getPropValue(key));
      } catch (ParseException ex) {
         ReportingUtils.showError("Could not parse float value of " + key);
      }
      catch (Exception ex) {
         ReportingUtils.showError("Could not get float value of property " + key);
      }
      return val;
   }

   /**
    * returns a string value for the specified property
    * @param key property name in Java; key to property hashmap
    * @return
    */
   public String getPropValueString(String key) {
      return getPropValue(key);
   }

   public PropTypes getPropType(String key) {
      return getPropEntry(key).propType;
   }
   
   

}
