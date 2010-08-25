///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionData.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, December 3, 2006
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
// CVS:          $Id: DeviceControlGUI.java 869 2008-02-02 00:15:51Z nenad $
//
package org.micromanager.api;

import java.awt.Color;
import java.awt.Component;

import org.micromanager.navigation.PositionList;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMScriptException;

/**
 * Interface to execute commands in the main panel.
 */
public interface DeviceControlGUI {
   public void updateGUI(boolean updateConfigPadStructure);
   public void initializeGUI();
   public String getVersion();
   public boolean updateImage();
   public boolean displayImage(Object pixels);
   public boolean displayImageWithStatusLine(Object pixels, String statusLine);   
   public void displayStatusLine(String statusLine);
   public boolean okToAcquire();
   public void stopAllActivity();
   public boolean getLiveMode();
   public void enableLiveMode(boolean enable);
   public void setBackgroundStyle(String backgroundType); 
   public String getBackgroundStyle();
   public Color getBackgroundColor();
   /**
    * Lets Components register themselves so that their background can be  
    * manipulated by the Micro-Manager UI
    */
   public void addMMBackgroundListener(Component frame);
   /**
    * Lets Components remove themselves from the list whose background gets
    * changed by the Micro-Manager UI
    */
   public void removeMMBackgroundListener(Component frame);
   public void setConfigChanged(boolean status);
   public void refreshGUI();
   public void applyContrastSettings(ContrastSettings contrast8_, ContrastSettings contrast16_);
   public ContrastSettings getContrastSettings();
   public boolean is16bit();
   public void showXYPositionList();
   /*
    * Make the mian window the frontmose, active window again
    */
   public void makeActive();
   

   // acquisition control
   public void startBurstAcquisition() throws MMScriptException;
   public void runBurstAcquisition() throws MMScriptException;
   public void startAcquisition() throws MMScriptException;
   public boolean isBurstAcquisitionRunning() throws MMScriptException;
   public void loadAcquisition(String path) throws MMScriptException;
   public void sleep(long ms) throws MMScriptException;
   
   public void setPositionList(PositionList pl) throws MMScriptException;
   public AutofocusManager getAutofocusManager();
}
