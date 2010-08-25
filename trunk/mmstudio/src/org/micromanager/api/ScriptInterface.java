///////////////////////////////////////////////////////////////////////////////
//FILE:          ScriptInterface.java
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

import ij.gui.ImageWindow;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.micromanager.metadata.WellAcquisitionData;
import org.micromanager.navigation.PositionList;
import org.micromanager.utils.MMScriptException;
/**
 * Interface to execute commands in the main panel.
 * All functions throw MMScriptException (TBD)
 */
public interface ScriptInterface {
      
   /**
    * Blocks the script execution for the specified number of milliseconds.
    * Script can be aborted during sleep.
    */
   public void sleep(long ms) throws MMScriptException;
   
   /**
    * Displays text in the console output window.
    * @throws MMScriptException 
    */
   public void message(String text) throws MMScriptException;
   
   /**
    * Clears console output window.
    * @throws MMScriptException 
    */
   public void clearMessageWindow() throws MMScriptException;

   /**
    * Brings GUI up to date with the recent changes in the mmcore.
    */
   public void refreshGUI();

   /**
    * Snaps image and displays in AcqWindow
    * Opens a new AcqWindow when current one is not open
    */
   public void snapSingleImage();

   /**
    * Opens a new acquisition context with explicit image physical parameters.
    * This command will determine the recorded date and time of the acquisition.
    * All relative (elapsed) time stamps will be determined with respect to this time.
    * @throws MMScriptException 
    */
   public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices) throws MMScriptException;
   
   /**
    * Opens a new acquisition context with explicit image physical parameters.
    * Makes it possible to run acquisition without displaying a window
    * @throws MMScriptException 
    */
   
   public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, boolean show) throws MMScriptException;

   public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, boolean show, boolean virtual) throws MMScriptException;


   public void initializeAcquisition(String name, int width, int height, int depth) throws MMScriptException;
   
   /**
    * Checks whether an acquisition already exists
    */
   public Boolean acquisitionExists(String name);

   /**
    * Closes the acquisition.
    * After this command metadata is complete and all the references to this data set are cleaned-up
    * @throws MMScriptException 
    */
   public void closeAcquisition(String name) throws MMScriptException;
   
   /**
    * Closes all currently open acquisitions.
    */
   public void closeAllAcquisitions();
   
   /**
    * Snaps an image with current settings and moves pixels into the specified layer of the image5d
    * @throws MMScriptException 
    */
   public void snapAndAddImage(String name, int frame, int channel, int z) throws MMScriptException;
   
   /**
    * Inserts image into the acquisition handle
    */
   public void addImage(String name, Object img, int frame, int channel, int z) throws MMScriptException;

   /**
    * Inserts image into the acquisition handle
    */
   public void addImage(String name, TaggedImage taggedImg) throws MMScriptException;

   /**
    * Sets custom property attached to the acquisition summary
    */
   public void setAcquisitionProperty(String acqName, String propertyName, String value) throws MMScriptException;

   public void setAcquisitionSystemState(String acqName, Map<String,String> md) throws MMScriptException;

   public void setAcquisitionSummary(String acqName, Map<String,String> md) throws MMScriptException;
   
   /**
    * Sets property attached to an individual image
    */
   public void setImageProperty(String acqName, int frame, int channel, int slice, String propName, String value) throws MMScriptException;

   /**
    * Blocks the script until the system is ready to start acquiring
    */
   //public void waitForSystem();
   
   /**
    * Execute burst acquisition with settings from Burst Acquisition Dialog
    * Will open the Dialog when it is not open yet
    * Returns after Burst Acquisition finishes
    */
   public void runBurstAcquisition() throws MMScriptException;
   
   /**
    * Execute burst acquisition with settings from Burst Acquisition Dialog
    * changed using the provided parameters
    * Will open the Dialog when it is not open yet
    * Returns after Burst Acquisition finishes
    * @param name - imagename to save the data to
    * @param root - root directory for image data
    * @param nr - nr of frames
    * @throws MMScriptExcpetion 
    *
    */
   public void runBurstAcquisition(int nr, String name, String root) throws MMScriptException;
   
   /**
    * Execute burst acquisition with settings from Burst Acquisition Dialog
    * changed using the provided parameters
    * Will open the Dialog when it is not open yet
    * Returns after Burst Acquisition finishes
    * @param nr - nr of frames
    * @throws MMScriptExcpetion 
    *
    */
   public void runBurstAcquisition(int nr) throws MMScriptException;
   
   /**
    * Load setting for Burst Acquisition from file
    * Will open Burst Acquisition Dialog when it is not yet open
    * Not Implemented!
    * @Depreciated
    */
   public void loadBurstAcquisition(String path) throws MMScriptException;

   /**
    * Executes Acquisition with current settings
    * Will open the Acquisition Dialog when it is not open yet
    * Returns after Acquisition finishes
    */
   public void runAcquisition() throws MMScriptException;
   
   /**
    * Executes Acquisition with current settings but allows for changing the data path
    * Will open the Acquisition Dialog when it is not open yet.
    * Returns after Acquisition finishes
    * @Depreciated - typo
    */
   public void runAcqusition(String name, String root) throws MMScriptException;

   /**
    * Executes Acquisition with current settings but allows for changing the data path
    * Will open the Acquisition Dialog when it is not open yet.
    * Returns after Acquisition finishes
    * @Depreciated - typo
    */
   public void runAcquisition(String name, String root) throws MMScriptException;

   /**
    * Executes Acquisition for a single well, using the plate scanning convention and data structure.
    * Returns after Acquisition finishes.
    * @param wad - well acquisition data objec
    * @param incrementalAF - enable or disable incremental autofocusing between imaging sites
    */
   public boolean runWellScan(WellAcquisitionData wad) throws MMScriptException;

   /**
    * Loads setting for Acquisition Dialog from file
    * Will open Acquisition Dialog when it is not open yet
    */  
   public void loadAcquisition(String path) throws MMScriptException;
   
   /**
    * Makes this the 'current' PositionList, i.e., the one used by the Acquisition Protocol
    * Replaces the list in the PositionList Window
    * It will open a position list dialog if it was not already open.
    */
   public void setPositionList(PositionList pl) throws MMScriptException;
   
   /**
    * Returns a copy of the current PositionList, the one used by the Acquisition Protocol
    */
   public PositionList getPositionList() throws MMScriptException;
   
   /**
    * Sets the color of the specified channel in an image5d
    */
   public void setChannelColor(String title, int channel, Color color) throws MMScriptException;
   
   /**
    * Sets the channel name (label)
    * @param title - acquisition name
    * @param channel - channel index
    * @param name - channel label
    * @throws MMScriptException
    */
   public void setChannelName(String title, int channel, String name) throws MMScriptException;
   
   /**
    * Sets black (min) and white (max) clipping levels for each channel.
    * @param channel - channel index
    * @param min - black clipping level
    * @param max - white clipping level
    * @throws MMScriptException
    */
   public void setChannelContrast(String title, int channel, int min, int max) throws MMScriptException;
   
   /**
    * Automatically adjusts channel contrast display settings based on the specified frame-slice 
    * @param title - acquisition name
    * @param frame - frame number
    * @param slice - slice number
    * @throws MMScriptException
    */
   public void setContrastBasedOnFrame(String title, int frame, int slice) throws MMScriptException;
      
   /**
    * Closes Image5D window. 
    */
   public void closeAcquisitionImage5D(String title) throws MMScriptException;

   /**
    * Obtain the current XY stage position.
    * Returns a point in device coordinates in microns.
    */
   public Point2D.Double getXYStagePosition()  throws MMScriptException;

    /**
    * Move default Focus (Z) and block until done
    * @param z
    * @throws MMScriptException
    */
   public void setStagePosition(double z) throws MMScriptException;

   /**
    * Move default Focus (Z) relative to current position and block until done
    * @param z
    * @throws MMScriptException
    */
   public void setRelativeStagePosition(double z) throws MMScriptException;

   /**
    * Move default XY stage and block until done.
    * @param x - coordinate in um
    * @param y - coordinate in um
    */
   public void setXYStagePosition(double x, double y)  throws MMScriptException ;

    /**
    * Move default XY stage relative to current position and block until done.
    * @param x - coordinate in um
    * @param y - coordinate in um
    */
   public void setRelativeXYStagePosition(double x, double y)  throws MMScriptException ;

   /**
    * Open empty image acquisition window
    * @throws MMScriptException
    */
   //public void openCompatibleImage5D(String title, int frames, int channels, int slices) throws MMScriptException;
   
   public String getXYStageName();
   
   public void setXYOrigin(double x, double y) throws MMScriptException;
   
   
   /**
    * Save current configuration
    */
   public void saveConfigPresets();

   public ImageWindow getImageWin();

   /**
   * Installs a plugin class from the class path.
   */
   public String installPlugin(String className);
   
   /**
    * Deprecated. Use installPlugin(String className) instead.
    */
   public String installPlugin(String className, String menuName); 

   /**
   * Installs an autofocus plugin class from the class path.
   */
   public String installAutofocusPlugin(String className);

   /**
    * Provides access to the Core and its functionality
    */
   public CMMCore getMMCore();

   /**
    * Currently active autofocus device (can be either a Java or C++ coded device)
    * @return currently active autofocus device
    */
   public Autofocus getAutofocus();

   /**
    * Shows the dialog with options for the currenyl active autofocus device
    */
   public void showAutofocusDialog();

   /**
    * The acquisition engine carries out the MDA acquistion
    * You can get access to its functionality through this function
    * @return acquisition engine
    */
   public AcquisitionEngine getAcquisitionEngine();


   /**
    * Adds a message to the Micro-Manager log (found in Corelogtxt)
    * @param msg - message to be added to the log
    */
   public void logMessage(String msg);

   /**
    * Shows a message in the UI
    * @param msg - message to be shown
    */
   public void showMessage(String msg);

   /**
    * Writes the stacktrace and a message to the Micro-Manager log (Corelog.txt)
    * @param e - Java exception to be logged
    * @param msg - message to be shown
    */
   public void logError(Exception e, String msg);

   /**
    * Writes a stacktrace to the Micro-Manager log
    * @param e - Java exception to be logged
    */
   public void logError(Exception e);

   /**
    * Writes an error to the Micro-Manager log (sane as logMessage)
    * @param msg - message to be logged
    */
   public void logError(String msg);

   /**
    * Shows an error including stacktrace in the UI and logs to the Micro-
    * Manager log
    * @param e - Java excpetion to be shown and logged
    * @param msg - Error message to be shown and logged
    */
   public void showError(Exception e, String msg);

   /**
    * Shows and logs a Java exception
    * @param e - Java excpetion to be shown and logged
    */
   public void showError(Exception e);

   /**
    * Shows an error message in the UI and logs to the Micro-Manager log
    * @param msg - error message to be shown and logged
    */
   public void showError(String msg);
}
