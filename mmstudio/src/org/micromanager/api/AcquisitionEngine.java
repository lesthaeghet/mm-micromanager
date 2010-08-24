///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionEngine.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, November 1, 2005
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
// CVS:          $Id: AcquisitionEngine.java 318 2007-07-02 22:29:55Z nenad $
//
package org.micromanager.api;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import mmcorej.CMMCore;
import org.micromanager.acquisition.engine.TaggedImageProcessor;

import org.micromanager.metadata.MMAcqDataException;
import org.micromanager.metadata.WellAcquisitionData;
import org.micromanager.navigation.PositionList;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMException;

/**
 * Acquisition engine interface.
 */
public interface AcquisitionEngine {
   
   public static final String cameraGroup_ = "Camera";
   public static final DecimalFormat FMT2 = new DecimalFormat("#0.00");
   public static final String DEFAULT_ROOT_NAME = "C:/AcquisitionData";
   
   // initialization
   public void setCore(CMMCore core_, AutofocusManager afMgr);
   public void setPositionList(PositionList posList);
   public void setParentGUI(DeviceControlGUI parent);

   /**
    * Sets which device will be used as the Z (focus) axis
    */
   public void setZStageDevice(String stageLabel_);

   /**
    * Sets whether the Live window will be updated during acquistion
    */
   public void setUpdateLiveWindow(boolean b);
   
   // run-time control

   /**
    * Starts acquisition as defined in the Multi-Dimensional Acquistion Window
    * @throws MMException
    * @throws MMAcqDataException
    */
   public void acquire() throws MMException, MMAcqDataException;

   /**
    * Starts acquisition of a single well, based on the current protocol, using the supplied
    * acquisition data structure.
    * This command is specially designed for plate scanning and will automatically re-set
    * all appropriate parameters.
    * @throws MMAcqDataException 
    * @throws Exception
    */
   public boolean acquireWellScan(WellAcquisitionData wad) throws MMException, MMAcqDataException;

   /**
    * Stops a running Acquisition
    * @param   interrupted when set, multifield acquisition will also be stopped
    */
   public void stop(boolean interrupted);


   /**
    * Request immediate abort of current task
    */
   public void abortRequest();

   /**
    * Signals that a running acquisition is done.
    */
   public void setFinished();

   /**
    * Returns true when Acquisition is running
    */
   public boolean isAcquisitionRunning();

   /**
    * Determines if a multi-field acquistion is running
    */
   public boolean isMultiFieldRunning();

   /**
    * Returns the number of frames acquired so far
    */
   public int getCurrentFrameCount();

   /**
    * Unconditional shutdown.  Will stop acuiqistion and multi-field acquisition
    */
   public void shutdown();

   /**
    * Pause/Unpause a running acquistion
    */
   public void setPause(boolean state);
   
   // settings
   /**
    * Returns Frame Interval set by user in Multi-Dimensional Acquistion Windows
    */
   public double getFrameIntervalMs();

   /**
    * Returns Z slice Step Size set by user in Multi-Dimensional Acquistion Windows
    */
   public double getSliceZStepUm();

   /**
    * Returns Z slice bottom position set by user in Multi-Dimensional Acquistion Windows
    */
   public double getSliceZBottomUm();

   /**
    * Sets channel specification in the given row
    */
   public void setChannel(int row, ChannelSpec channel);

   /**
    * Find out which groups are available
    */
   public String getFirstConfigGroup();

   /**
    * Find out which channels are currently available for the selected channel group.
    * @return - list of channel (preset) names
    */
   public String[] getChannelConfigs();

   /**
    * Returns number of frames set by user in Multi-Dimensional Acquistion Window
    */
   public int getNumFrames();

   /**
    * Returns the configuration preset group currently selected in the Multi-Dimensional Acquistion Window
    */
   public String getChannelGroup();

   /**
    * Set the channel group if the current hardware configuration permits.
    * @param group
    * @return - true if successful
    */
   public boolean setChannelGroup(String newGroup);

   /**
    * Resets the engine
    */
   public void clear();
   public void setFrames(int numFrames, double interval);
   public double getMinZStepUm();
   public void setSlices(double bottom, double top, double step, boolean b);

   public boolean isFramesSettingEnabled();

   public void enableFramesSetting(boolean enable);

   public boolean isChannelsSettingEnabled();

   public void enableChannelsSetting(boolean enable);

   public boolean isZSliceSettingEnabled();

   /**
    * returns Z slice top position set by user in Multi-Dimensional Acquistion Windows
    */
   public double getZTopUm();

   /**
    * Flag indicating whether to override autoshutter behavior and keep the shutter
    * open for a Z-stack.  This only has an effect when autoshutter is on, and when 
    * mode "Slices First" has been chosen.
    */
   public void keepShutterOpenForStack(boolean open);

   /**
    * Returns flag indicating whether to override autoshutter behavior during z-stack
    */
   public boolean isShutterOpenForStack();

   /**
    * Flag indicating whether to override autoshutter behavior and keep the shutter
    * open for channel imaging.  This only has an effect when autoshutter is on, and when 
    * mode "Channels First" has been chosen.
    */
   public void keepShutterOpenForChannels(boolean open);

   /**
    * Returns flag indicating whether to override autoshutter behavior during channel acquisition 
    */
   public boolean isShutterOpenForChannels();

   public void enableZSliceSetting(boolean boolean1);
   public void enableMultiPosition(boolean selected);
   public boolean isMultiPositionEnabled();
   public ArrayList<ChannelSpec> getChannels();
   public void setChannels(ArrayList<ChannelSpec> channels);
   public String getRootName();
   public void setRootName(String absolutePath);
   public void setCameraConfig(String config);
   public void setDirName(String text);
   public void setComment(String text);
   /**
    * @deprecated
    */
   public boolean addChannel(String name, double exp, double offset, ContrastSettings s8, ContrastSettings s16, int skip, Color c);
   public boolean addChannel(String name, double exp, Boolean doZStack, double offset, ContrastSettings s8, ContrastSettings s16, int skip, Color c);
   public void setSaveFiles(boolean selected);
   public boolean getSaveFiles();
   public void setDisplayMode(int mode);
   public int getSliceMode();
   public int getDisplayMode();
   public void setSliceMode(int mode);
   public int getPositionMode();
   public void setPositionMode(int mode);
   public void enableAutoFocus(boolean enabled);
   public boolean isAutoFocusEnabled();
   public int getAfSkipInterval();
   public void setAfSkipInterval (int interval);
   public void setParameterPreferences(Preferences prefs);

   public void setSingleFrame(boolean selected); // @deprecated
   public void setSingleWindow(boolean selected); // @deprecated
   public String installAutofocusPlugin(String className); // @deprecated
   
   // utility
   public String getVerboseSummary();
   public boolean isConfigAvailable(String config_);
   public String[] getCameraConfigs();
   public String[] getAvailableGroups();
   public double getCurrentZPos();
   public boolean isPaused();
   public void restoreSystem();

   public void addProcessor(TaggedImageProcessor processor);
   public void removeProcessor(TaggedImageProcessor processor);
}
