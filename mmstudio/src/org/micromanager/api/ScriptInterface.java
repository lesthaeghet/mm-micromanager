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

package org.micromanager.api;

import ij.gui.ImageWindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.json.JSONObject;
import org.micromanager.AcqControlDlg;
import org.micromanager.PositionListDlg;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.MMScriptException;

/**
 * Interface to execute commands in the main panel. Implemented by
 * MMStudioMainFrame and available as the gui object in the Beanshell
 * scripting panel.
 * 
 * Most functions throw MMScriptException
 */
public interface ScriptInterface {
	
	/**
	 * Pauses the script for the specified number of milliseconds
	 */
   public void sleep(long ms) throws MMScriptException;
         
   /**
    * Displays text in the scripting console output window.
    * @throws MMScriptException 
    */
   public void message(String text) throws MMScriptException;
   
   /**
    * Clears scripting console output window.
    * @throws MMScriptException 
    */
   public void clearMessageWindow() throws MMScriptException;

   /**
    * Brings GUI up to date with the recent changes in the mmcore.
    */
   public void refreshGUI();
   
    /**
    * Brings GUI up to date with the recent changes in the mmcore.
    * Does not communicate with hardware, only checks Cache
    */
   public void refreshGUIFromCache();

   /**
    * Snaps image and displays in AcqWindow.
    * Opens a new AcqWindow when current one is not open.
    * Calling this function is the same as pressing the "Snap" button on the main
    * Micro-manager GUI
    */
   public void snapSingleImage();
   
   /**
    * Opens an acquisition data set
    * @param summaryMetadata The metadata describing the acquisition parameters
    * @param diskCached True if images are cached on disk; false if they are kept in RAM only.
    * @param displayOff True if no display is to be created or shown.
    * @throws MMScriptException
    */
   public String createDataSet(String root, String name, boolean diskCached, boolean displayOff) throws MMScriptException;
   // TODO: add data set format specifier (multi-file or single file) to the parameter list
   
   /**
    * Inserts image into the acquisition handle.
    * @param name Name of the acquisition.
    * @param taggedImg Tagged Image (image with associated metadata).  The metadata determines where
    * in the acquisition this image will be inserted (i.e. frame, channel, slice, and position indecies)    
    * @throws MMScriptException
    */
   public void addImageToDataSet(String name, int frame, String channel, int slice, String position, TaggedImage taggedImg) throws MMScriptException;   
   
   /**
    * Returns a name beginning with stem that is not yet used.
    * @param stem Base name from which a unique name will be constructed
    */
   public String getUniqueAcquisitionName(String stem);
   
   /**
    * Returns the name of the current album (i.e. the most recently created one)
    * In addition to their use through the scripting interface, Albums are used
    * by the "Camera --> Album" button in the main window of Micro-Manager and 
    * the "--> Album" button on the snap/live window
    * @return Name of the current Album.
    */
   public String getCurrentAlbum();

   /**
    * Add a TaggedImage to an album; creates a new album if the image and current album
    * do not match in image dimensions, bits per pixel, bytes per pixel, or number of channels.
    * The current album is the most recently created one
    * Albums are also used by the "Camera --> Album" button in the main window of Micro-Manager and 
    * the "--> Album" button on the snap/live window
    */
   public void addToAlbum(TaggedImage image) throws MMScriptException;
   
   /**
    * Checks whether an acquisition with the given name already exists.
    */
   public Boolean acquisitionExists(String name);

   /**
    * Closes the acquisition.
    * After this command metadata is complete, all the references to this data set are cleaned-up,
    * and no additional images can be added to the acquisition
    * Does not close the window in which the acquisition data is displayed
    * @throws MMScriptException 
    */
   public void closeAcquisition(String name) throws MMScriptException;
   
   /**
    * Closes all currently open acquisitions.
    */
   public void closeAllAcquisitions();
      
   /**
    * Gets an Array with names of all open acquisitions
    * @return Arrays with names of all acquisitions that are currently open
    */
   public String[] getAcquisitionNames();

   
   
   /**
    * Returns the width (in pixels) of images in this acquisition
    */
   public int getAcquisitionImageWidth(String acqName) throws MMScriptException;

   /**
    * Returns the width (in pixels) of images in this acquisition
    */
   public int getAcquisitionImageHeight(String acqName) throws MMScriptException;
   
   /**
    * Returns the number of bits used per pixel
    */
   public int getAcquisitionImageBitDepth(String acqName) throws MMScriptException;
   
   /**
    * Returns the number of bytes used per pixel
    */
   public int getAcquisitionImageByteDepth(String acqName) throws MMScriptException;

   /**
    * Returns boolean specifying whether multiple cameras used in this acquisition
    */
   public int getAcquisitionMultiCamNumChannels(String acqName) throws MMScriptException;
   
   /**
    * Sets custom property attached to the acquisition summary
    */
   public void setAcquisitionProperty(String acqName, String propertyName, String value) throws MMScriptException;
   
   /**
    * Sets property attached to an individual image.
    */
   public void setImageProperty(String acqName, int frame, int channel, int slice, String propName, String value) throws MMScriptException;
   
   /**
    * Executes Acquisition with current settings
    * Will open the Acquisition Dialog when it is not open yet
    * Returns after Acquisition finishes
    * Note that this function should not be executed on the EDT (which is the
    * thread running the UI).  
    * @return The name of the acquisition created.
    * @throws MMScriptException
    */
   public String runAcquisition() throws MMScriptException;
   
   /**
    * Executes Acquisition with current settings but allows for changing the data path.
    * Will open the Acquisition Dialog when it is not open yet.
    * Returns after Acquisition finishes.
    * Note that this function should not be executed on the EDT (which is the
    * thread running the UI).
    * @param name Name of this acquisition.
    * @param root Place in the file system where data can be stored.
    * @return The name of the acquisition created
    * @throws MMScriptException
    */
   public String runAcquisition(String name, String root) throws MMScriptException;

   /**
    * Loads setting for Acquisition Dialog from file
    * Will open Acquisition Dialog when it is not open yet
    * @throws MMScriptException
    */  
   public void loadAcquisition(String path) throws MMScriptException;
   
   /**
    * Makes this the 'current' PositionList, i.e., the one used by the Acquisition Protocol
    * Replaces the list in the PositionList Window
    * It will open a position list dialog if it was not already open.
    * @throws MMScriptException
    */
   public void setPositionList(PositionList pl) throws MMScriptException;
   
   /**
    * Returns a copy of the current PositionList, the one used by the Acquisition Protocol
    * @throws MMScriptException
    */
   public PositionList getPositionList() throws MMScriptException;
   
   /**
    * Sets the color of the specified channel in the image viewer.  Only has an effect
    * for images with 2 or more channels
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
    * Updates the exposure time associated with the given preset
    * If the channelgroup and channel name match the current state
    * the exposure time will also be updated
    * 
    * @param channelGroup - 
    * 
    * @param channel - preset for which to change exposure time
    * @param exposure - desired exposure time
    */
   public void setChannelExposureTime(String channelGroup, String channel,
           double exposure);
   
   /**
    * Sets min (black) and max (white or the channel's color) pixel value clipping levels for each channel.
    * @param title - acquisition name
    * @param channel - channel index (use 0 if there is only a single channel)
    * @param min - black clipping level
    * @param max - white clipping level
    * @throws MMScriptException
    */
   public void setChannelContrast(String title, int channel, int min, int max) throws MMScriptException;
   
   /**
    * Autoscales contrast for each channel at the current position based on pixel values
    * at the current slice and frame
    * @param title - acquisition name
    * @param frame - frame number
    * @param slice - slice number
    * @throws MMScriptException
    */
   public void setContrastBasedOnFrame(String title, int frame, int slice) throws MMScriptException;
   
   
    /**
    * Returns exposure time for the desired preset in the given channelgroup
    * Acquires its info from the preferences
    * Same thing is used in MDA window, but this class keeps its own copy
    * 
    * @param channelGroup
    * @param channel - 
    * @param defaultExp - default value
    * @return exposure time
    */
   public double getChannelExposureTime(String channelGroup, String channel,
           double defaultExp);
   
   
   /**
    * Closes the image window corresponding to the acquisition.  If being used along with
    * closeAcquisitiion, this method should be called first
    * @param acquisitionName - Name of the acquisition
    * @throws MMScriptException
    */
   public void closeAcquisitionWindow(String acquisitionName) throws MMScriptException;

   /**
    * Obtain the current XY stage position.
    * Returns a point in device coordinates in microns.
    * @throws MMScriptException
    */
   public Point2D.Double getXYStagePosition()  throws MMScriptException;

    /**
    * Move default Focus (Z) and block until done
    * @param z absolute z position
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
    * @throws MMScriptException
    */
   public void setXYStagePosition(double x, double y)  throws MMScriptException ;

    /**
    * Move default XY stage relative to current position and block until done.
    * @param x - coordinate in um
    * @param y - coordinate in um
    * @throws MMScriptException
    */
   public void setRelativeXYStagePosition(double x, double y)  throws MMScriptException ;

    /**
    * There can be multiple XY stage devices in a system.  This function returns
    * the name of the currently active one
    * @return Name of the active XYStage device
    */
   public String getXYStageName();


   /**
    * Assigns the current stage position of the default xy-stage to be (x,y),
    * thereby offseting the coordinates of all other positions.
    * @throws MMScriptException
    */
   public void setXYOrigin(double x, double y) throws MMScriptException;
   
   /**
    * Save current configuration
    */
   public void saveConfigPresets();

   /**
    * Returns the ImageJ ImageWindow instance that is used for Snap and Live display.
    */
   public ImageWindow getSnapLiveWin();
   
   /**
   * Installs a plugin class from the class path.
   */
   public String installPlugin(String className);
   
   /**
   * Installs an autofocus plugin class from the class path.
   */
   public String installAutofocusPlugin(String className);

   /**
    * Provides access to the Core and its functionality.
    * @return Micro-Manager core object. 
    */
   public CMMCore getMMCore();

   /**
    * Currently active autofocus device (can be either a Java or C++ coded device).
    * @return currently active autofocus device
    */
   public Autofocus getAutofocus();

   /**
    * Shows the dialog with options for the currently active autofocus device.
    */
   public void showAutofocusDialog();

   /**
    * Adds a message to the Micro-Manager log (found in Corelogtxt).
    * @param msg - message to be added to the log
    */
   public void logMessage(String msg);

   /**
    * Shows a message in the UI.
    * @param msg - message to be shown
    */
   public void showMessage(String msg);

   /**
    * Writes the stacktrace and a message to the Micro-Manager log (Corelog.txt).
    * @param e - Java exception to be logged
    * @param msg - message to be shown
    */
   public void logError(Exception e, String msg);

   /**
    * Writes a stacktrace to the Micro-Manager log.
    * @param e - Java exception to be logged
    */
   public void logError(Exception e);

   /**
    * Writes an error to the Micro-Manager log (same as logMessage).
    * @param msg - message to be logged
    */
   public void logError(String msg);

   /**
    * Shows an error including stacktrace in the UI and logs to the Micro-
    * Manager log
    * @param e - Java exception to be shown and logged
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

   /**
    * Allows MMListeners to register themselves so that they will receive
    * alerts as defined in the MMListenerInterface
    */
   public void addMMListener(MMListenerInterface newL);

   /**
    * Allows MMListeners to remove themselves
    */
   public void removeMMListener(MMListenerInterface oldL);

   /**
    * Lets Components register themselves so that their background can be
    * manipulated by the Micro-Manager UI.
    * @param frame Component to which the listener should be added.
    */
   public void addMMBackgroundListener(Component frame);

   /**
    * Lets Components remove themselves from the list whose background gets
    * changed by the Micro-Manager UI.
    * $param frame Component from which the listener should be removed.
    */
   public void removeMMBackgroundListener(Component frame);

   /**
    * Returns the current color of the main window so that it can be used in
    * derived windows/plugins as well
    * @return Current backgroundColor
    */
   public Color getBackgroundColor();

     /**
    * Show a TaggedImage in the snap/live window (uses current camera settings
    * to figure out the shape of the image)
    * @param image TaggedImage (pixel data and metadata tags) to be displayed
    */
   public boolean displayImage(TaggedImage image);

   /**
    * Determines whether live mode is currently running.
    * @return when true, live mode is running, when false live mode is not running.
    */
   public boolean isLiveModeOn();

   /**
    * Turn live mode on or off (equivalent to pressing the Live mode button).
    * @param b true starts live mode, false stops live mode.
    */
   public void enableLiveMode(boolean b);

   /**
    * Get the default camera's ROI -- a convenience function.
    * @throws MMScriptException
    */
   public Rectangle getROI() throws MMScriptException;

   /**
    * Set the default camera's ROI -- a convenience function.
    * @throws MMScriptException
    */
   public void setROI(Rectangle r) throws MMScriptException;

   /**
    * Get a reference to the ImageCache object associated with the acquisition.
    * @param acquisitionName Name of the acquisition
    */
   public ImageCache getAcquisitionImageCache(String acquisitionName) throws MMScriptException;


   /**
    * Opens the XYPositionList when it is not opened.
    * Adds the current position to the list (same as pressing the "Mark" button in the XYPositionList)
    */
   public void markCurrentPosition();

   /**
    * Returns the Multi-Dimensional Acquisition Window.
    * To show the window, call:
    * AcqControlDlg dlg = gui.getAcqDlg();
    * dlg.setVisible(true);
    * @return Handle to the MDA acquisition dialog
    */
   public AcqControlDlg getAcqDlg();

   /**
    * Returns the PositionList Dialog.
    * If the Dialog did not yet exist, it will be created.
    * The Dialog will not necessarily be shown, call the setVisibile method of the dialog to do so
    * @return Handle to the positionList Dialog
    */
   public PositionListDlg getXYPosListDlg();

   /**
    * Returns true when an acquisition is currently running (note: this function will
    * not return true if live mode, snap, or "Camera --> Album" is currently running
    */
   public boolean isAcquisitionRunning();

   /**
    * Displays an error message and returns true if the run-time Micro-Manager version
    * is less than the one specified.
    * Versions in Micro-Manager are of the format:
    * major.minor.minute date
    * where ' date' can be omitted
    * Examples:
    * 1.4.6
    * 1.4.6 20110831
    * When a date is appended to a version number, it will be newer than the same version 
    * without a date
    * @param version - minimum version needen to run this code
    * @throws MMScriptException
    */
   public boolean versionLessThan(String version) throws MMScriptException;

   /**
    * Write various properties of MM and the OS to the log.
    */
   public void logStartupProperties();

   /*
    * Make the main window the frontmost, active window again
    */
   public void makeActive();

   /**
    * @return the currently selected AutoFocusManger object
    */
   public AutofocusManager getAutofocusManager();

   /**
    * @return the current Micro-Manager background style--"Day" or "Night"
    */ 
   public String getBackgroundStyle();

   /**
    * @return the currently running Micro-Manager version
    */
   public String getVersion();

   /**
    * Sets the background color of the GUI and all its registered components to 
    * the selected backGroundType
    * @param backgroundType either "Day" or "Night"
    */
   public void setBackgroundStyle(String backgroundType);

   /**
    * lets the GUI know that the current configuration has been changed.  Activates
    * the save button it status is true
    * @param status 
    */
   public void setConfigChanged(boolean status);

   /**
    * shows the position list dialog
    */
   public void showXYPositionList();

   /**
    * Open an existing data set. Shows the acquisition in a window.
    * @return The acquisition object.
    */
   public String openAcquisitionData(String location, boolean inRAM) throws MMScriptException;


   /**
    * Open an existing data set.
    * @return The name of the acquisition object.
    */
   public String openAcquisitionData(String location, boolean inRAM, boolean show) throws MMScriptException;

   /**
    * Enabled or disable the ROI buttons on the main window.
    */
   public void enableRoiButtons(final boolean enabled);

   /**
    * Set the format for saving images to disk.  Images can be written to disk one per a file
    * or multiple images per file.  Using multiple images per file should be faster on most systems
    * @param imageSavingClass use either org.micromanager.acquisition.TaggedImageStorageDiskDefault.class
    * for single-image files of org.micromanager.acquisition.TaggedImageStorageMultipageTiff.class for 
    * multi-image files
    */
   public void setImageSavingFormat(Class imageSavingClass) throws MMScriptException;

   /*
    * Returns true if the user has chosen to allow MM to autoreload devices
    * that throw an error.
    */
   public boolean getAutoreloadOption();

   /*
    * Returns the pipeline
    */
   public IAcquisitionEngine2010 getAcquisitionEngine2010();
   
   /*
    * Returns true if user has chosen to hide MDA window when it runs.
    */
   public boolean getHideMDADisplayOption();
   
   /**
    * Adds an image processor to the DataProcessor pipeline.
    */
   public void addImageProcessor(DataProcessor<TaggedImage> processor);

   /**
    * Removes an image processor from the DataProcessor pipeline.
    */
   public void removeImageProcessor(DataProcessor<TaggedImage> taggedImageProcessor);
   
   /**
    * Pause/Unpause a running acquistion
    */
   public void setPause(boolean state);
   
   /**
    * Returns true if the acquisition is currently paused.
    */
   public boolean isPaused();

   /**
    * Attach a runnable to the acquisition engine. Each index (f, p, c, s) can
    * be specified. Passing a value of -1 should result in the runnable being attached
    * at all values of that index. For example, if the first argument is -1,
    * then the runnable should execute at every frame.
    */
   public void attachRunnable(int frame, int position, int channel, int slice, Runnable runnable);

   /**
    * Remove runnables from the acquisition engine
    */
   public void clearRunnables();
   
   /**
    * Return current acquisition settings
    */ 
    SequenceSettings getAcqusitionSettings();
    
    /**
     * Return the current acquisition path, or null if not applicable
     */
    public String getAcquisitionPath();

    /**
     * Display dialog to save data for one of the currently open acquisitions
     */
    public void promptToSaveAcqusition(String name, boolean prompt) throws MMScriptException;
    
    /**
     * OBSOLETE!!!
     * TO BE REMOVED >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     * @return
     */
    public MMAcquisition getAcquisition(String name) throws MMScriptException;
    
    /**
     * Snaps an image with current settings and moves pixels into the specified layer of the MDA viewer.
     * @param name Name of the acquisition.
     * @param frame Frame number (time point, 0-based) in which this image should be inserted.
     * @param channel Channel number (0-based) in which this image should be inserted.
     * @param z Slice number (0-based) in which this image should be inserted.
     * @throws MMScriptException 
     */
    // public void snapAndAddImage(String name, int frame, int channel, int z) throws MMScriptException;
    // replace with:
    // mmc.snapImage();
    // gui.addImage(acqName, mmc.getTaggedImage());

    /**
     * Snaps an image with the current settings and places pixels in the specified position
     * of the Micro-Manager Image viewer
     * @param name Name of the acquisition.
     * @param frame Frame number (time point, 0-based) in which this image should be inserted.
     * @param channel Channel number (0-based) in which this image should be inserted.
     * @param z Slice number (0-based) in which this image should be inserted.
     * @param position Position number (0-based) in which this image should be inserted. 
     * @throws MMScriptException
     */
    // public void snapAndAddImage(String name, int frame, int channel, int z, int position) throws MMScriptException;
    // replace with:
    // mmc.snapImage();
    // gui.addImage(acqName, mmc.getTaggedImage());

    /**
     * Opens a new acquisition context with explicit image physical parameters.
     * This command will determine the recorded date and time of the acquisition.
     * All relative (elapsed) time stamps will be determined with respect to this time.
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @throws MMScriptException 
     */
    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices) throws MMScriptException;

    /**
     * Opens a new acquisition context with explicit image physical parameters.
     * This command will determine the recorded date and time of the acquisition.
     * All relative (elapsed) time stamps will be determined with respect to this time.
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @param nrPositions Number of (XY) Positions in this acquisition.
     * @throws MMScriptException
     */
    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, int nrPositions) throws MMScriptException;

    /**
     * Opens a new acquisition context with explicit image physical parameters.
     * Makes it possible to run acquisition without displaying a window
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @param nrPositions Number of (XY) Positions in this acquisition.
     * @param show Whether or not to show this acquisition.
     * @throws MMScriptException 
     */

    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, int nrPositions, boolean show) throws MMScriptException;

    /**
     * Variant of openAcquisition that allows specifying whether or not the data should be saved during acquisition.
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @param nrPositions Number of (XY) Positions in this acquisition.
     * @param show Whether or not to show this acquisition.
     * @param save Whether or not save data during acquisition.
     * @throws MMScriptException
     */
    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, int nrPositions, boolean show, boolean save) throws MMScriptException;


    /**
     * Single position variant of openAcquisition that allows specifying whether or not to show data during acquisition.  
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @param show Whether or not to show this acquisition.
     * @throws MMScriptException
     */
    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, boolean show) throws MMScriptException;


    /**
     * Single position variant of openAcquisition that allows specifying whether or not to show and save data during acquisition.  
     * @param name Name of the new acquisition context.
     * @param rootDir Place in the file system where data may be stored.
     * @param nrFrames Nunmber of Frames (time points) in this acquisition.  This number can grow dynamically.
     * @param nrChannels Number of Channels in this acquisition.  This number is fixed.
     * @param nrSlices Number of Slices (Z-positions) in this acquisition.
     * @param show Whether or not to show this acquisition.
     * @throws MMScriptException
     */
    public void openAcquisition(String name, String rootDir, int nrFrames, int nrChannels, int nrSlices, boolean show, boolean save) throws MMScriptException;
    /**
     * Set up a Simple Acquisition that has already been opened
     * Simple Acquisitions are used in Live and Snap modes.  They
     * only store a single image at a time, and automatically store
     * this image in RAM, regardless of whether the conserve RAM
     * option in tools-options is checked
     */
    public void initializeSimpleAcquisition(String name, int width, int height, 
            int byteDepth, int bitDepth, int multiCamNumCh) throws MMScriptException;
    
    /**
     * Set up an acquisition that has already been opened.
     */
    public void initializeAcquisition(String name, int width, int height, int byteDepth) throws MMScriptException;
    
    /**
     * Set up an acquisition that has already been opened.
     */
    public void initializeAcquisition(String name, int width, int height, int byteDepth, int bitDepth) throws MMScriptException;
    
    /**
     * Opens and initializes an acquisition according to summaryMetadata
     * (as typically generated by acquisition dialog).
     * @param summaryMetadata The metadata describing the acquisition parameters
     * @param diskCached True if images are cached on disk; false if they are kept in RAM only.
     * @throws MMScriptException
     */
    public String createAcquisition(JSONObject summaryMetadata, boolean diskCached);

    /**
     * Opens and initializes an acquisition according to summaryMetadata
     * (as typically generated by acquisition dialog).
     * @param summaryMetadata The metadata describing the acquisition parameters
     * @param diskCached True if images are cached on disk; false if they are kept in RAM only.
     * @param displayOff True if no display is to be created or shown.
     * @throws MMScriptException
     */
    public String createAcquisition(JSONObject summaryMetadata, boolean diskCached, boolean displayOff);
    
    /**
     * Sets the summary metadata for an acquisition (as opposed to metadata for individual planes).
     */
    public void setAcquisitionSummary(String acqName, JSONObject md) throws MMScriptException;

    /**
     * Inserts image into the acquisition handle.
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata).  The metadata determines where
     * in the acquisition this image will be inserted (i.e. frame, channel, slice, and position indecies)    
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg) throws MMScriptException;

    /**
     * Inserts image into the acquisition handle and gives option whether or not to update the display.
     * This version will wait for the display to finish drawing the image before the function returns
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata).  The metadata determines where
     * in the acquisition this image will be inserted (i.e. frame, channel, slice, and position indecies)    * @param updateDisplay Flag used to indicate whether or not to update the display.
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg, boolean updateDisplay) throws MMScriptException;

    /**
     * Inserts image into the acquisition handle and gives option whether or not to update the display.
     * Also optionally waits for the display to finish drawing the image before the function returns
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata).  The metadata determines where
     * in the acquisition this image will be inserted (i.e. frame, channel, slice, and position indecies)
     * @param updateDisplay Flag used to indicate whether or not to update the display.
     * @param waitForDisplay flag that determines if the function should wait for the display to finish
     * drawing the image before returning
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg, 
            boolean updateDisplay, boolean waitForDisplay) throws MMScriptException;

   /**
     * Inserts image into the acquisition handle.
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata)
     * @param frame index of the frame where image should be inserted
     * @param channel index of the channel where image should be inserted
     * @param slice index of the slice where image should be inserted
     * @param position index of the position where image should be inserted
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg, int frame, int channel, 
            int slice, int position) throws MMScriptException;


    /**
     * Inserts image into the acquisition handle and gives option whether or not to update the display.
     * This version will wait for the display to finish drawing the image before the function returns
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata)  
     * @param frame index of the frame where image should be inserted
     * @param channel index of the channel where image should be inserted
     * @param slice index of the slice where image should be inserted
     * @param position index of the position where image should be inserted  
     * @param updateDisplay Flag used to indicate whether or not to update the display.
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg, int frame, int channel, 
            int slice, int position, boolean updateDisplay) throws MMScriptException;

    /**
     * Inserts image into the acquisition handle and gives option whether or not to update the display.
     * Also optionally waits for the display to finish drawing the image before the function returns
     * @param name Name of the acquisition.
     * @param taggedImg Tagged Image (image with associated metadata) 
     * @param frame index of the frame where image should be inserted
     * @param channel index of the channel where image should be inserted
     * @param slice index of the slice where image should be inserted
     * @param position index of the position where image should be inserted
     * @param updateDisplay Flag used to indicate whether or not to update the display.
     * @param waitForDisplay flag that determines if the function should wait for the display to finish
     * drawing the image before returning
     * @throws MMScriptException
     */
    public void addImage(String name, TaggedImage taggedImg, int frame, int channel, 
            int slice, int position, boolean updateDisplay, boolean waitForDisplay) throws MMScriptException;

}
