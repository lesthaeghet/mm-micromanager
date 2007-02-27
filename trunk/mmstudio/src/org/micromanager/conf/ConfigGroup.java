///////////////////////////////////////////////////////////////////////////////
//FILE:          ConfigGroup.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, November 7, 2006
//
// COPYRIGHT:    University of California, San Francisco, 2006
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
//
// CVS:          $Id$
//

package org.micromanager.conf;

import java.util.Hashtable;

/**
 * Configuration group encapsulation for use in Configuration Wizard. 
 */
public class ConfigGroup {
   String name_;
   Hashtable configs_;
   
   public ConfigGroup(String name) {
      name_ = new String(name);
      configs_ = new Hashtable();
   }
   
   public void addConfigPreset(ConfigPreset p) {
      configs_.put(p.getName(), p);
   }
   
   public String getName() {
      return name_;
   }
   
   public void addConfigSetting(String presetName, String device, String property, String value) {
      ConfigPreset cp = (ConfigPreset)configs_.get(presetName);
      if (cp == null) {
         cp = new ConfigPreset(presetName);
         configs_.put(presetName, cp);
      }
      
      cp.addSetting(new Setting(device, property, value));
   }
   
   public ConfigPreset[] getConfigPresets() {
      Object objs[] = configs_.values().toArray();
      ConfigPreset[] cps = new ConfigPreset[objs.length];
      for (int i=0; i<objs.length; i++)
         cps[i] = (ConfigPreset)objs[i];
      return cps;
   }
   
   public String toString() {
      return new String("Group: " + name_);
   }

   public void removePreset(String name) {
      configs_.remove(name);
   }

   public ConfigPreset findConfigPreset(String name) {
      return (ConfigPreset)configs_.get(name);
   }

   public void setName(String name) {
      name_ = name;
   }

   public void renamePreset(ConfigPreset prs, String name) {
      configs_.remove(prs.getName());
      prs.setName(name);
      configs_.put(name, prs);
   }
   
}
