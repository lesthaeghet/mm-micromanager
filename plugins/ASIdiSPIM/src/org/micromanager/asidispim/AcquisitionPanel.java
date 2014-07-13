///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionPanel.java
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


import org.micromanager.asidispim.Data.Cameras;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Joystick;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.DevicesListenerInterface;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.PanelUtils;
import org.micromanager.asidispim.Utils.StagePositionUpdater;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.json.JSONException;
import org.json.JSONObject;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.micromanager.api.ScriptInterface;
import org.micromanager.api.ImageCache;
import org.micromanager.api.MMTags;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.acquisition.ComponentTitledBorder;
import org.micromanager.acquisition.DefaultTaggedImageSink;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.MMScriptException;

/**
 *
 * @author nico
 * @author Jon
 */
@SuppressWarnings("serial")
public class AcquisitionPanel extends ListeningJPanel implements DevicesListenerInterface {

   private final Devices devices_;
   private final Properties props_;
   private final Joystick joystick_;
   private final Cameras cameras_;
   private final Prefs prefs_;
   private final CMMCore core_;
   private final ScriptInterface gui_;
   private final JCheckBox sliceTimingEnabled_;
   private final JSpinner numSlices_;
   private final JSpinner numSides_;
   private final JComboBox firstSide_;
   private final JSpinner numScansPerSlice_;
   private final JSpinner lineScanPeriod_;
   private final JSpinner delayScan_;
   private final JSpinner delayLaser_;
   private final JSpinner delayCamera_;
   private final JSpinner durationCamera_;  // NB: not the same as camera exposure
   private final JSpinner durationLaser_;
   private final JSpinner delaySide_;
   private JLabel actualSlicePeriodLabel_;
   private JLabel actualVolumeDurationLabel_;
   private JLabel actualTimeLapseDurationLabel_;
   private final JSpinner numAcquisitions_;
   private final JSpinner acquisitionInterval_;
   private final JButton buttonStart_;
   private final JButton buttonStop_;
   private final JPanel volPanel_;
   private final JPanel slicePanel_;
   private final JPanel repeatPanel_;
   private final JPanel savePanel_;
   private final JTextField rootField_;
   private final JTextField nameField_;
   private final JLabel acquisitionStatusLabel_;
   private int numTimePointsDone_;
   private AtomicBoolean stop_ = new AtomicBoolean(false);
   private final StagePositionUpdater stagePosUpdater_;
   private final JSpinner stepSize_;
   private final JLabel desiredSlicePeriodLabel_;
   private final JSpinner desiredSlicePeriod_;
   private final JLabel desiredLightExposureLabel_;
   private final JSpinner desiredLightExposure_;
   private final JButton calculateSliceTiming_;
   private final JCheckBox separateTimePointsCB_;
   private final JCheckBox saveCB_;
   
   private float cameraReadoutTime_;
   
   
   
   /**
    * Associative container for slice timing information.
    * @author Jon
    *
    */
   public static class SliceTiming {
      float scanDelay;
      int scanNum;
      int scanPeriod;
      float laserDelay;
      float laserDuration;
      float cameraDelay;
      float cameraDuration;

      /**
       * Chooses some reasonable defaults (may not be controller defaults).
       */
      public SliceTiming() {
         scanDelay = 0;
         scanNum = 1;
         scanPeriod = 10;
         laserDelay = 0;
         laserDuration = 1;
         cameraDelay = 0;
         cameraDuration = 1;
      }
      
      @Override
      public boolean equals(Object obj) {
         if ((obj instanceof SliceTiming)) {
            SliceTiming s = (SliceTiming) obj;
            return(scanDelay == s.scanDelay
                  && scanNum == s.scanNum
                  && scanPeriod == s.scanPeriod
                  && laserDelay == s.laserDelay
                  && laserDuration == s.laserDuration
                  && cameraDelay == s.cameraDelay
                  && cameraDuration == s.cameraDuration);
         } else {
            return false;
         }
         
         
      }
    
   }
   
   //private static final String ZSTEPTAG = "z-step_um";
//   private static final String ELAPSEDTIME = "ElapsedTime-ms";

   public AcquisitionPanel(ScriptInterface gui, 
           Devices devices, 
           Properties props, 
           Joystick joystick,
           Cameras cameras, 
           Prefs prefs, 
           StagePositionUpdater stagePosUpdater) {
      super("Acquisition",
              new MigLayout(
              "",
              "[right]16[center]16[center]16[center]16[center]",
              "[]8[]"));
      gui_ = gui;
      devices_ = devices;
      props_ = props;
      joystick_ = joystick;
      cameras_ = cameras;
      prefs_ = prefs;
      stagePosUpdater_ = stagePosUpdater;
      core_ = gui_.getMMCore();
      numTimePointsDone_ = 0;
      
      PanelUtils pu = new PanelUtils(gui_, prefs_);

      // added to spinner controls where we should re-calculate the displayed
      // slice period and/or volume duration
      ChangeListener recalculateTimingDisplay = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            updateActualSlicePeriodLabel();
            updateActualVolumeDurationLabel();
         }
      };
      
      
      // start volume (main) sub-panel

      volPanel_ = new JPanel(new MigLayout(
              "",
              "[right]16[center]",
              "[]8[]"));

      volPanel_.setBorder(PanelUtils.makeTitledBorder("Volume Settings"));

      volPanel_.add(new JLabel("Number of sides:"));
      numSides_ = pu.makeSpinnerInteger(1, 2, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_NUM_SIDES, 2);
      numSides_.addChangeListener(recalculateTimingDisplay);
      volPanel_.add(numSides_, "wrap");

      volPanel_.add(new JLabel("First side:"));
      String[] ab = {Devices.Sides.A.toString(), Devices.Sides.B.toString()};
      firstSide_ = pu.makeDropDownBox(ab, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_FIRSTSIDE);
      volPanel_.add(firstSide_, "wrap");
      
      volPanel_.add(new JLabel("Delay before side [ms]:"));
      delaySide_ = pu.makeSpinnerFloat(0, 10000, 0.25, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_DELAY_SIDE, 0);
      delaySide_.addChangeListener(recalculateTimingDisplay);
      volPanel_.add(delaySide_, "wrap");

      volPanel_.add(new JLabel("Slices per volume:"));
      numSlices_ = pu.makeSpinnerInteger(1, 1000, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_NUM_SLICES, 20);
      numSlices_.addChangeListener(recalculateTimingDisplay);
      volPanel_.add(numSlices_, "wrap");
      
      volPanel_.add(new JLabel("Slice step size [\u00B5m]:"));
      stepSize_ = pu.makeSpinnerFloat(0, 100, 0.1, props_, devices_,
            new Devices.Keys[]{Devices.Keys.PLUGIN}, Properties.Keys.PLUGIN_SLICE_STEP_SIZE,
            1.0);
      volPanel_.add(stepSize_, "wrap");
      
      // special field that is enabled/disabled depending on whether advanced timing is enabled
      desiredSlicePeriodLabel_ = new JLabel("Desired slice period [ms]:"); 
      volPanel_.add(desiredSlicePeriodLabel_);
      desiredSlicePeriod_ = pu.makeSpinnerFloat(5, 1000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.PLUGIN}, Properties.Keys.PLUGIN_DESIRED_SLICE_PERIOD, 30);
      volPanel_.add(desiredSlicePeriod_, "wrap");
      desiredSlicePeriod_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent ce) {
            // make sure is multiple of 0.25
            float val = PanelUtils.getSpinnerFloatValue(desiredSlicePeriod_);
            float nearestValid = PanelUtils.roundToQuarterMs(val);
            if (!PanelUtils.floatsEqual(val, nearestValid)) {
               PanelUtils.setSpinnerFloatValue(desiredSlicePeriod_, nearestValid);
            }
         }
      });
      
      // special field that is enabled/disabled depending on whether advanced timing is enabled
      desiredLightExposureLabel_ = new JLabel("Desired laser exposure [ms]:"); 
      volPanel_.add(desiredLightExposureLabel_);
      desiredLightExposure_ = pu.makeSpinnerFloat(2.5, 1000.5, 1, props_, devices_,
            new Devices.Keys[]{Devices.Keys.PLUGIN}, Properties.Keys.PLUGIN_DESIRED_EXPOSURE, 8.5);
      desiredLightExposure_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent ce) {
            // make sure is 2.5, 2.5, 3.5, ... 
            float val = PanelUtils.getSpinnerFloatValue(desiredLightExposure_);
            float nearestValid = (float) Math.round(val+(float)0.5) - (float)0.5; 
            if (!PanelUtils.floatsEqual(val, nearestValid)) {
               PanelUtils.setSpinnerFloatValue(desiredLightExposure_, nearestValid);
            }
         }
      });
      volPanel_.add(desiredLightExposure_, "wrap");
      
      calculateSliceTiming_ = new JButton("Calculate slice timing");
      
      calculateSliceTiming_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            recalculateSliceTiming();
         }
      });
      volPanel_.add(calculateSliceTiming_, "center, span 2, wrap");
      
      actualVolumeDurationLabel_ = new JLabel();
      volPanel_.add(actualVolumeDurationLabel_, "center, span 2, wrap");

      // end volume sub-panel
      
      
      // start slice timing (advanced) sub-panel

      slicePanel_ = new JPanel(new MigLayout(
              "",
              "[right]16[center]",
              "[]8[]"));
      
      final boolean slicePanelEnabled = prefs_.getBoolean(panelName_,
            Properties.Keys.PLUGIN_ADVANCED_SLICE_TIMING, true);
      
      // special checkbox in titled border to enable/disable sub-panel plus more
      sliceTimingEnabled_ = new JCheckBox("Slice Timing Settings (Advanced)", slicePanelEnabled); 
      sliceTimingEnabled_.setFocusPainted(false); 
      ComponentTitledBorder componentBorder = 
              new ComponentTitledBorder(sliceTimingEnabled_, slicePanel_ 
              , BorderFactory.createLineBorder(ASIdiSPIM.borderColor)); 

      // this action listener takes care of enabling/disabling inputs
      // we call this to get GUI looking right
      ActionListener sliceTimingDisableGUIInputs = new ActionListener(){ 
         public void actionPerformed(ActionEvent e){ 
            boolean enabled = sliceTimingEnabled_.isSelected(); 
            Component comp[] = slicePanel_.getComponents(); 
            for(int i = 0; i<comp.length; i++){ 
               comp[i].setEnabled(enabled); 
            }
            desiredSlicePeriod_.setEnabled(!enabled);
            desiredSlicePeriodLabel_.setEnabled(!enabled);
            desiredLightExposure_.setEnabled(!enabled);
            desiredLightExposureLabel_.setEnabled(!enabled);
            calculateSliceTiming_.setEnabled(!enabled);
            actualSlicePeriodLabel_.setEnabled(true);
         } 
      };
      
      // this action listener actually recalculates the timings
      // don't add this action listener until after GUI is set
      ActionListener sliceTimingCalculate = new ActionListener(){
        public void actionPerformed(ActionEvent e){
           boolean enabled = sliceTimingEnabled_.isSelected(); 
           prefs_.putBoolean(panelName_,
                 Properties.Keys.PLUGIN_ADVANCED_SLICE_TIMING, enabled);
        }
      };
      slicePanel_.setBorder(componentBorder);

      slicePanel_.add(new JLabel("Delay before scan [ms]:"));
      delayScan_ = pu.makeSpinnerFloat(0, 10000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_SCAN, 0);
      delayScan_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(delayScan_, "wrap");

      slicePanel_.add(new JLabel("Lines scans per slice:"));
      numScansPerSlice_ = pu.makeSpinnerInteger(1, 1000, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_NUM_SCANSPERSLICE, 1);
      numScansPerSlice_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(numScansPerSlice_, "wrap");

      slicePanel_.add(new JLabel("Line scan period [ms]:"));
      lineScanPeriod_ = pu.makeSpinnerInteger(1, 10000, props_, devices_,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_LINESCAN_PERIOD, 10);
      lineScanPeriod_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(lineScanPeriod_, "wrap");
      
      slicePanel_.add(new JSeparator(), "span 2, wrap");
      
      slicePanel_.add(new JLabel("Delay before laser [ms]:"));
      delayLaser_ = pu.makeSpinnerFloat(0, 10000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_LASER, 0);
      delayLaser_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(delayLaser_, "wrap");
      
      slicePanel_.add(new JLabel("Laser duration [ms]:"));
      durationLaser_ = pu.makeSpinnerFloat(0, 10000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DURATION_LASER, 1);
      durationLaser_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(durationLaser_, "span 2, wrap");
      
      slicePanel_.add(new JSeparator(), "wrap");

      slicePanel_.add(new JLabel("Delay before camera [ms]:"));
      delayCamera_ = pu.makeSpinnerFloat(0, 10000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_CAMERA, 0);
      delayCamera_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(delayCamera_, "wrap");
      
      slicePanel_.add(new JLabel("Camera duration [ms]:"));
      durationCamera_ = pu.makeSpinnerFloat(0, 1000, 0.25, props_, devices_,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DURATION_CAMERA, 0);
      durationCamera_.addChangeListener(recalculateTimingDisplay);
      slicePanel_.add(durationCamera_, "wrap");
      
      actualSlicePeriodLabel_ = new JLabel();
      slicePanel_.add(actualSlicePeriodLabel_, "center, span 2, wrap");
      
      // end slice sub-panel
      

      // start repeat (time lapse) sub-panel

      repeatPanel_ = new JPanel(new MigLayout(
              "",
              "[right]16[center]",
              "[]8[]"));

      repeatPanel_.setBorder(PanelUtils.makeTitledBorder("Time Lapse Settings"));
      
      ChangeListener recalculateTimeLapseDisplay = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            updateActualTimeLapseDurationLabel();
         }
      };

      repeatPanel_.add(new JLabel("Number of time points:"));
      numAcquisitions_ = pu.makeSpinnerInteger(1, 32000, props_, devices_,
              new Devices.Keys[]{Devices.Keys.PLUGIN},
              Properties.Keys.PLUGIN_NUM_ACQUISITIONS, 1);
      numAcquisitions_.addChangeListener(recalculateTimeLapseDisplay);
      repeatPanel_.add(numAcquisitions_, "wrap");

      repeatPanel_.add(new JLabel("Time point interval [s]:"));
      acquisitionInterval_ = pu.makeSpinnerFloat(1, 32000, 0.1, props_, devices_,
              new Devices.Keys[]{Devices.Keys.PLUGIN},
              Properties.Keys.PLUGIN_ACQUISITION_INTERVAL, 60);
      acquisitionInterval_.addChangeListener(recalculateTimeLapseDisplay);
      repeatPanel_.add(acquisitionInterval_, "wrap");
      
      actualTimeLapseDurationLabel_ = new JLabel();
      repeatPanel_.add(actualTimeLapseDurationLabel_, "center, span 2, wrap");

      // end repeat sub-panel
      
      
      // start savePanel
      
      int textFieldWidth = 20;
      savePanel_ = new JPanel(new MigLayout(
              "",
              "[right]16[center]16[left]",
              "[]8[]"));
      savePanel_.setBorder(PanelUtils.makeTitledBorder("Data Saving Settings"));
      
      separateTimePointsCB_ = new JCheckBox("Separate viewer for each time point");
      separateTimePointsCB_.setSelected(prefs_.getBoolean(panelName_, 
              Properties.Keys.PLUGIN_SEPARATE_VIEWERS_FOR_TIMEPOINTS, false));
      savePanel_.add(separateTimePointsCB_, "span 3, left, wrap");
      
      saveCB_ = new JCheckBox("Save while acquiring");
      saveCB_.setSelected(prefs_.getBoolean(panelName_, 
              Properties.Keys.PLUGIN_SAVE_WHILE_ACQUIRING, false));
      
      savePanel_.add(saveCB_, "span 3, left, wrap");

      JLabel dirRootLabel = new JLabel ("Directory root");
      savePanel_.add(dirRootLabel);

      rootField_ = new JTextField();
      rootField_.setText( prefs_.getString(panelName_, 
              Properties.Keys.PLUGIN_DIRECTORY_ROOT, "") );
      rootField_.setColumns(textFieldWidth);
      savePanel_.add(rootField_);

      JButton browseRootButton = new JButton();
      browseRootButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            setRootDirectory(rootField_);
            prefs_.putString(panelName_, Properties.Keys.PLUGIN_DIRECTORY_ROOT, 
                    rootField_.getText());
         }
      });
      browseRootButton.setMargin(new Insets(2, 5, 2, 5));
      browseRootButton.setText("...");
      savePanel_.add(browseRootButton, "wrap");

      JLabel namePrefixLabel = new JLabel();
      namePrefixLabel.setText("Name prefix");
      savePanel_.add(namePrefixLabel);

      nameField_ = new JTextField("acq");
      nameField_.setText( prefs_.getString(panelName_,
              Properties.Keys.PLUGIN_NAME_PREFIX, "acq"));
      nameField_.setColumns(textFieldWidth);
      savePanel_.add(nameField_, "wrap");
      
      final JComponent[] saveComponents = { browseRootButton, rootField_, 
                                            dirRootLabel };
      setDataSavingComponents(saveComponents);
      
      saveCB_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setDataSavingComponents(saveComponents);
         }
      });
      
      // end save panel
      

      buttonStart_ = new JButton("Start!");
      buttonStart_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            //if (!cameras_.isCurrentCameraValid()) {
            //   ReportingUtils.showError("Must set valid camera for acquisition!");
            //   return;
            //}
            stop_.set(false);

            class acqThread extends Thread {
               acqThread(String threadName) {
                  super(threadName);
               }

               @Override
               public void run() {
                  buttonStart_.setEnabled(false);
                  buttonStop_.setEnabled(true);
                  runAcquisition();
                  buttonStop_.setEnabled(false);
                  buttonStart_.setEnabled(true);
               }
            }            
            acqThread acqt = new acqThread("diSPIM Acquisition");
            acqt.start();          
         }
      });

      buttonStop_ = new JButton("Stop!");
      buttonStop_.setEnabled(false);
      buttonStop_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            stop_.set(true);
            buttonStop_.setEnabled(false);
            buttonStart_.setEnabled(true);
         }
      });

      acquisitionStatusLabel_ = new JLabel("");
      updateAcquisitionStatusNone();

      // set up tabbed panel for GUI
      add(slicePanel_, "spany 2, top");
      add(volPanel_, "spany2, top");
      add(repeatPanel_, "top, wrap");
      add(savePanel_, "wrap");
      add(buttonStart_, "cell 0 2, split 2, center");
      add(buttonStop_, "center");
      add(acquisitionStatusLabel_, "center");
      
      cameraReadoutTime_ = computeCameraReadoutTime();
      
      // properly initialize the advanced slice timing
      sliceTimingEnabled_.addActionListener(sliceTimingDisableGUIInputs);
      sliceTimingEnabled_.doClick();
      sliceTimingEnabled_.doClick();
      sliceTimingEnabled_.addActionListener(sliceTimingCalculate);
      
      updateActualSlicePeriodLabel();
      updateActualVolumeDurationLabel();
      updateActualTimeLapseDurationLabel();
      

   }//end constructor

   
   
   private SliceTiming getTimingFromPeriodAndLightExposure(float desiredPeriod, float desiredExposure) {
      
      // uses algorithm Jon worked out in Octave code
      // each slice period goes like this:
      // 1. camera readout time
      // 2. any extra delay time
      // 3. camera reset
      // 4. start scan and then slice (0.25 time on either end of the scan the laser is off
      
      final float scanLaserBufferTime = (float) 0.25;
      SliceTiming s = new SliceTiming();
      cameraReadoutTime_ = computeCameraReadoutTime();  // recalculate this as a safety
      
      // this assumes "usual" camera mode, not Hamamatsu's "synchronous" or Zyla's "overlap" mode
      // TODO: add the ability to use these faster modes (will require changes in several places
      // and a GUI setting for camera mode)
      float cameraReadout_max = PanelUtils.ceilToQuarterMs(cameraReadoutTime_);
      float cameraReset_max = PanelUtils.ceilToQuarterMs(cameraReadoutTime_ + (float) 0.1);
      // 0.1 in there as kludge for delay between trigger and exposure start
      // 9H = 88us for Hamamatsu, 67us for PCO 
      // TODO: fix the kludge factor
      float slicePeriod = PanelUtils.roundToQuarterMs(desiredPeriod);
      int scanPeriod = Math.round(desiredExposure + 2*scanLaserBufferTime);
      // scan will be longer than laser by 0.25ms at both start and end
      float laserDuration = scanPeriod - 2*scanLaserBufferTime;  // will be integer plus 0.5
      
      float globalDelay = slicePeriod - cameraReadout_max - cameraReset_max - scanPeriod;
      
      // if calculated delay is negative then we have to reduce exposure time in 1 sec increments
      if (globalDelay < 0) {
         int extraTimeNeeded = (int) Math.ceil((float)-1*globalDelay);  // positive number
            globalDelay += extraTimeNeeded;
            JOptionPane.showMessageDialog(null,
                  "Slice timing: increased slice period to meet laser exposure"
                  + " constraint (some time required for camera readout).",
                  "Warning",
                  JOptionPane.WARNING_MESSAGE);
      }
      
      s.scanDelay = cameraReadout_max + globalDelay + cameraReset_max;
      s.scanNum = 1;
      s.scanPeriod = scanPeriod;
      s.laserDelay = cameraReadout_max + globalDelay + cameraReset_max + scanLaserBufferTime;
      s.laserDuration = laserDuration;
      s.cameraDelay = cameraReadout_max + globalDelay;
      s.cameraDuration = cameraReset_max + scanPeriod;
      
      return s;
   }
   
   /**
    * Re-calculate the controller's timing settings when user changes 
    * desired slice period or desired light exposure in "easy timing" mode.
    * @return true if any change actually made  
    */
   private boolean recalculateSliceTiming() {
      SliceTiming currentTiming = getCurrentSliceTiming();
      SliceTiming newTiming = getTimingFromPeriodAndLightExposure(
            PanelUtils.getSpinnerFloatValue(desiredSlicePeriod_),
            PanelUtils.getSpinnerFloatValue(desiredLightExposure_));
      if (!currentTiming.equals(newTiming)) {
         setCurrentSliceTiming(newTiming);
         return true;
      }
      return false;
   }
   
   /**
    * Gets the slice timing from the controller's properties
    * @return
    */
   private SliceTiming getCurrentSliceTiming() {
      SliceTiming s = new SliceTiming();
      s.scanDelay = PanelUtils.getSpinnerFloatValue(delayScan_);
      s.scanNum = (Integer) numScansPerSlice_.getValue();
      s.scanPeriod = (Integer) lineScanPeriod_.getValue();
      s.laserDelay = PanelUtils.getSpinnerFloatValue(delayLaser_);
      s.laserDuration = PanelUtils.getSpinnerFloatValue(durationLaser_);
      s.cameraDelay = PanelUtils.getSpinnerFloatValue(delayCamera_);
      s.cameraDuration = PanelUtils.getSpinnerFloatValue(durationCamera_);
      return s;
   }
   
   /**
    * Gets the slice timing from the controller's properties
    * @return
    */
   private void setCurrentSliceTiming(SliceTiming s) {
      PanelUtils.setSpinnerFloatValue(delayScan_, s.scanDelay);
      numScansPerSlice_.setValue(s.scanNum);
      lineScanPeriod_.setValue(s.scanPeriod);
      PanelUtils.setSpinnerFloatValue(delayLaser_, s.laserDelay);
      PanelUtils.setSpinnerFloatValue(durationLaser_, s.laserDuration);
      PanelUtils.setSpinnerFloatValue(delayCamera_, s.cameraDelay);
      PanelUtils.setSpinnerFloatValue(durationCamera_, s.cameraDuration );
   }
   
   /**
    * Compute slice period in ms based on controller's timing settings.
    * @return period in ms
    */
   private double computeActualSlicePeriod() {
      double period = Math.max(Math.max(
            PanelUtils.getSpinnerFloatValue(delayScan_) +   // scan time
            ((Integer) lineScanPeriod_.getValue() * 
                  (Integer) numScansPerSlice_.getValue()),
                  PanelUtils.getSpinnerFloatValue(delayLaser_)
                  + PanelUtils.getSpinnerFloatValue(durationLaser_)  // laser time
            ),
            PanelUtils.getSpinnerFloatValue(delayCamera_)
            + PanelUtils.getSpinnerFloatValue(durationCamera_)  // camera time
            );
      return period;
   }

   /**
    * Update the displayed slice period.
    */
   private void updateActualSlicePeriodLabel() {
      actualSlicePeriodLabel_.setText("Slice period: " + 
            NumberUtils.doubleToDisplayString(computeActualSlicePeriod()) +
            " ms");
   }
   
   /**
    * Compute the volume duration in ms based on controller's timing settings.
    * @return duration in ms
    */
   private double computeActualVolumeDuration() {
      double duration = (Integer) numSides_.getValue() * 
            (PanelUtils.getSpinnerFloatValue(delaySide_) +
                  (Integer) numSlices_.getValue() * computeActualSlicePeriod());
      return duration;
   }
   
   /**
    * Update the displayed volume duration.
    */
   private void updateActualVolumeDurationLabel() {
      actualVolumeDurationLabel_.setText("Volume duration: " + 
            NumberUtils.doubleToDisplayString(computeActualVolumeDuration()) +
            " ms");
   }
   
   /**
    * Compute the time lapse duration
    * @return duration in s
    */
   private double computeActualTimeLapseDuration() {
      double duration = ((Integer) numAcquisitions_.getValue() * 
            PanelUtils.getSpinnerFloatValue(acquisitionInterval_));
      return duration;
   }
   
   /**
    * Update the displayed time lapse duration.
    */
   private void updateActualTimeLapseDurationLabel() {
      actualTimeLapseDurationLabel_.setText("Time lapse duration: " + 
            NumberUtils.doubleToDisplayString(computeActualTimeLapseDuration()/60) +
            " min");
   }
   
   /**
    * Gets an estimate of a specific camera's readout time based on ROI or otherwise.
    * @param camKey device key for camera in question
    * @return readout time in ms
    */
   private float computeCameraReadoutTime(Devices.Keys camKey) {
      float readout = 10;
      Devices.Libraries camLibrary = devices_.getMMDeviceLibrary(camKey);
      switch (camLibrary) {
      case HAMCAM:
         // device adapter provides rounded to nearest 0.1ms
         // we may need to add small increment as buffer or calculate ourselves based on the ROI
         readout = props_.getPropValueFloat(camKey, Properties.Keys.READOUTTIME) * (float) 1000;
         break;
      case PCOCAM:
         JOptionPane.showMessageDialog(null,
               "Readout time for PCO cameras not yet implemented in plugin.",
               "Warning",
               JOptionPane.WARNING_MESSAGE);
         break;
      case ANDORCAM:
         JOptionPane.showMessageDialog(null,
               "Readout time for Andor cameras not yet implemented in plugin.",
               "Warning",
               JOptionPane.WARNING_MESSAGE);
         break;
      default: break;   
      }
      return readout;
   }
   
   /**
    * Computes the readout time of the SPIM cameras set on Devices panel.
    * Handles single-side operation.
    * Needed for computing (semi-)optimized slice timing in "easy timing" mode.
    * @return
    */
   private float computeCameraReadoutTime() {
      float readoutTime = 0;
      if(((Integer) numSides_.getValue()) > 1) {
         readoutTime = Math.max(computeCameraReadoutTime(Devices.Keys.CAMERAA),
               computeCameraReadoutTime(Devices.Keys.CAMERAB));
      } else {
         if(firstSide_.getSelectedItem().equals("A")) {
            readoutTime = computeCameraReadoutTime(Devices.Keys.CAMERAA);
         } else {
            readoutTime = computeCameraReadoutTime(Devices.Keys.CAMERAB);
         }
      }
      return readoutTime;
   }

   private void updateAcquisitionStatusNone() {
      acquisitionStatusLabel_.setText("No acquisition in progress.");
   }
   
   private void updateAcqusitionStatusAcquiringTimePoint() {
      // updates the same field as 
      acquisitionStatusLabel_.setText("Acquiring time point "
              + NumberUtils.intToDisplayString(numTimePointsDone_)
              + " of "
              + NumberUtils.intToDisplayString((Integer) numAcquisitions_.getValue()));
   }
   
   private void updateAcquisitonStatusWaitingForNext(int secsToNextAcquisition) {
      acquisitionStatusLabel_.setText("Finished "
              + NumberUtils.intToDisplayString(numTimePointsDone_)
              + " of "
              + NumberUtils.intToDisplayString((Integer) numAcquisitions_.getValue())
              + " time points; next in "
              + NumberUtils.intToDisplayString(secsToNextAcquisition)
              + " s."
              );
   }
   
   private void updateAcqusitionStatusDone() {
      acquisitionStatusLabel_.setText("Acquisition finished with "
            + NumberUtils.intToDisplayString(numTimePointsDone_)
            + " time points.");
   }
   

   private void setDataSavingComponents(JComponent[] saveComponents) {
      if (saveCB_.isSelected()) {
         for (JComponent c : saveComponents) {
            c.setEnabled(true);
         }
      } else {
         for (JComponent c : saveComponents) {
            c.setEnabled(false);
         }
      }
   }

   /**
    * Implementation of acquisition that orchestrates image
    * acquisition itself rather than using the acquisition engine
    *
    * @return
    */
   private boolean runAcquisition() {
      if (gui_.isAcquisitionRunning()) {
         gui_.showError("An acquisition is already running");
         return false;
      }

      // TODO check both ROIs
      
      boolean liveMode = gui_.isLiveModeOn();
      gui_.enableLiveMode(false);
      cameras_.setSPIMCameraTriggerMode(Cameras.TriggerModes.EXTERNAL_START);
      
      // get MM device names for first/second cameras to acquire
      String cameraA = devices_.getMMDevice(Devices.Keys.CAMERAA);
      String cameraB = devices_.getMMDevice(Devices.Keys.CAMERAB);
      String firstSide = (String) firstSide_.getSelectedItem();
      String firstCamera, secondCamera;
      if (firstSide.equals("A")) {
         firstCamera = cameraA;
         secondCamera = cameraB;
      } else {
         firstCamera = cameraB;
         secondCamera = cameraA;
      }
      
      cameraReadoutTime_ = computeCameraReadoutTime();
      double exposureTime = PanelUtils.getSpinnerFloatValue(durationCamera_)
            - cameraReadoutTime_ - 0.1;
      // 0.1 in there as kludge for delay between trigger and exposure start
      // 9H = 88us for Hamamatsu, 67us for PCO 
      // TODO: fix the kludge factor
      
      // TODO: get these from the UI
      boolean show = true;
      boolean save = saveCB_.isSelected();
      boolean singleTimePoints = separateTimePointsCB_.isSelected();
      String rootDir = rootField_.getText();

      int nrRepeats = (Integer) numAcquisitions_.getValue();
      int nrFrames = 1;
      if (!singleTimePoints) {
         nrFrames = nrRepeats;
         nrRepeats = 1;
      }

      long timeBetweenTimepointsMs = Math.round(
              PanelUtils.getSpinnerFloatValue(acquisitionInterval_) * 1000d);
      int nrSides = (Integer) numSides_.getValue();  // TODO: multi-channel
      int nrSlices = (Integer) numSlices_.getValue();
      int nrPos = 1;

      boolean autoShutter = core_.getAutoShutter();
      boolean shutterOpen = false;

      // Sanity checks
      if (firstCamera == null) {
         gui_.showError("Please set up a camera first on the Devices Panel");
         return false;
      }
      if (nrSides == 2 && secondCamera == null) {
         gui_.showError("2 Sides requested, but second camera is not configured."
                 + "\nPlease configure the Imaging Path B camera on the Devices Panel");
         return false;
      }
      double lineScanTime = computeActualSlicePeriod();
      if (exposureTime + cameraReadoutTime_ > lineScanTime) {
         gui_.showError("Exposure time is longer than time needed for a line scan.\n" +
                 "This will result in dropped frames.\n" +
                 "Please change input");
         return false;
      }
      double volumeDuration = computeActualVolumeDuration();
      if (volumeDuration > timeBetweenTimepointsMs) {
         gui_.showError("Repeat interval is set too short, shorter than" +
                 " the time to collect a single volume.\n" +
                 "Please change input");
         return false;
      }

      long acqStart = System.currentTimeMillis();

      try {
         // empty out circular buffer
         while (core_.getRemainingImageCount() > 0) {
            core_.popNextImage();
         }
      } catch (Exception ex) {
         gui_.showError(ex, "Error emptying out the circular buffer");
         return false;
      }

      // disable the serial communication during acquisition
      stagePosUpdater_.setAcqRunning(true);
      
      numTimePointsDone_ = 0;

      for (int tp = 0; tp < nrRepeats && !stop_.get(); tp++) {
         BlockingQueue<TaggedImage> bq = new LinkedBlockingQueue<TaggedImage>(10);
         String acqName = gui_.getUniqueAcquisitionName(nameField_.getText());
         if (singleTimePoints) {
            acqName = gui_.getUniqueAcquisitionName(nameField_.getText() + "-" + tp);
         }
         try {
            gui_.openAcquisition(acqName, rootDir, nrFrames, nrSides, nrSlices, nrPos,
                    show, save);
            core_.setExposure(firstCamera, exposureTime);
            if (secondCamera != null) {
               core_.setExposure(secondCamera, exposureTime);
            }
            gui_.setChannelName(acqName, 0, firstCamera);
            if (nrSides == 2 && secondCamera != null) {
               gui_.setChannelName(acqName, 1, secondCamera);
            }
            gui_.initializeAcquisition(acqName, (int) core_.getImageWidth(),
                    (int) core_.getImageHeight(), (int) core_.getBytesPerPixel(),
                    (int) core_.getImageBitDepth());
            MMAcquisition acq = gui_.getAcquisition(acqName);
            
            // Dive into MM internals since script interface does not support pipelines
            ImageCache imageCache = acq.getImageCache();
            VirtualAcquisitionDisplay vad = acq.getAcquisitionWindow();
            imageCache.addImageCacheListener(vad);

            // Start pumping images into the ImageCache
            DefaultTaggedImageSink sink = new DefaultTaggedImageSink(bq, imageCache);
            sink.start();

            // If the interval between frames is shorter than the time to acquire
            // them, we can switch to hardware based solution.  Not sure how important 
            // that feature is, so leave it out for now.
            for (int f = 0; f < nrFrames && !stop_.get(); f++) {
               long acqNow = System.currentTimeMillis();
               long delay = acqStart + f * timeBetweenTimepointsMs - acqNow;
               while (delay > 0 && !stop_.get()) {
                  updateAcquisitonStatusWaitingForNext((int) (delay / 1000));
                  long sleepTime = Math.min(1000, delay);
                  Thread.sleep(sleepTime);
                  acqNow = System.currentTimeMillis();
                  delay = acqStart + f * timeBetweenTimepointsMs - acqNow;
               }

               numTimePointsDone_++;
               updateAcqusitionStatusAcquiringTimePoint();

               core_.startSequenceAcquisition(firstCamera, nrSlices, 0, true);
               if (nrSides == 2) {
                  core_.startSequenceAcquisition(secondCamera, nrSlices, 0, true);
               }

               // get controller armed
               // Need to calculate the sheet amplitude based on settings 
               // in the Setup panels
               // We get these through the preferences

               int numSlices = (Integer) numSlices_.getValue();
               float piezoAmplitude = (float) ( (numSlices - 1) * 
                       PanelUtils.getSpinnerFloatValue(stepSize_));
               
               float sheetARate = prefs_.getFloat(
                       Properties.Keys.PLUGIN_SETUP_PANEL_NAME.toString() + Devices.Sides.A, 
                       Properties.Keys.PLUGIN_RATE_PIEZO_SHEET, -80);
               // catch divide by 0 errors
               float sheetAmplitudeA = piezoAmplitude / sheetARate;
               float sheetBRate = prefs_.getFloat(
                       Properties.Keys.PLUGIN_SETUP_PANEL_NAME.toString() + Devices.Sides.B, 
                       Properties.Keys.PLUGIN_RATE_PIEZO_SHEET, -80);
               float sheetAmplitudeB = piezoAmplitude / sheetBRate ;
               
               // set the appropriate properties on the controller
               props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SA_AMPLITUDE_Y_DEG,
                       sheetAmplitudeA);
               props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.BEAM_ENABLED,
                       Properties.Values.NO, true);
               props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SA_AMPLITUDE_Y_DEG,
                       sheetAmplitudeB);
               props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.BEAM_ENABLED,
                       Properties.Values.NO, true);
               props_.setPropValue(Devices.Keys.PIEZOA, Properties.Keys.SPIM_NUM_SLICES,
                       (Integer) numSlices_.getValue(), true);
               props_.setPropValue(Devices.Keys.PIEZOA, Properties.Keys.SA_AMPLITUDE,
                       piezoAmplitude );
               props_.setPropValue(Devices.Keys.PIEZOB, Properties.Keys.SPIM_NUM_SLICES,
                       (Integer) numSlices_.getValue(), true);
               props_.setPropValue(Devices.Keys.PIEZOB, Properties.Keys.SA_AMPLITUDE,
                       piezoAmplitude );
               props_.setPropValue(Devices.Keys.PIEZOA, Properties.Keys.SPIM_STATE,
                       Properties.Values.SPIM_ARMED, true);
               props_.setPropValue(Devices.Keys.PIEZOB, Properties.Keys.SPIM_STATE,
                       Properties.Values.SPIM_ARMED, true);

               // deal with shutter
               if (autoShutter) {
                  core_.setAutoShutter(false);
                  shutterOpen = core_.getShutterOpen();
                  if (!shutterOpen) {
                     core_.setShutterOpen(true);
                  }
               }

               // trigger controller
               // TODO generalize this for different ways of running SPIM
               props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SPIM_STATE,
                       Properties.Values.SPIM_RUNNING, true);

               // get images from camera and stick into acquisition
               // Wait for first image to create ImageWindow, so that we can be sure about image size
               long start = System.currentTimeMillis();
               long now = start;
               long timeout = 10000;
               while (core_.getRemainingImageCount() == 0 && (now - start < timeout)) {
                  now = System.currentTimeMillis();
                  Thread.sleep(5);
               }
               if (now - start >= timeout) {
                  throw new Exception("Camera did not send image within a reasonable time");
               }

               // run the loop that takes images from the cameras and puts them 
               // into the acquisition
               int[] frNumber = new int[2];
               boolean done = false;
               long timeout2;
               timeout2 = Math.max(10000, Math.round(computeActualVolumeDuration()));
               start = System.currentTimeMillis();
               while ((core_.getRemainingImageCount() > 0
                       || core_.isSequenceRunning(firstCamera)
                       || (secondCamera != null && core_.isSequenceRunning(secondCamera)))
                       && !stop_.get() && !done) {
                  now = System.currentTimeMillis();
                  if (core_.getRemainingImageCount() > 0) {
                     TaggedImage timg = core_.popNextTaggedImage();
                     String camera = (String) timg.tags.get("Camera");
                     int ch = 0;
                     if (camera.equals(secondCamera)) {
                        ch = 1;
                     }
                     addImageToAcquisition(acqName, f, ch, frNumber[ch], 0,
                             now - acqStart, timg, bq);
                     frNumber[ch]++;
                  } else {
                     Thread.sleep(1);
                  }
                  if (frNumber[0] == frNumber[1] && frNumber[0] == nrSlices) {
                     done = true;
                  }
                  if (now - start >= timeout2) {
                     gui_.logError("No image arrived withing a reasonable period");
                     // stop_.set(true);
                     done = true;
                  }
               }

               if (core_.isSequenceRunning(firstCamera)) {
                  core_.stopSequenceAcquisition(firstCamera);
               }
               if (secondCamera != null && core_.isSequenceRunning(secondCamera)) {
                  core_.stopSequenceAcquisition(secondCamera);
               }
               if (autoShutter) {
                  core_.setAutoShutter(true);

                  if (!shutterOpen) {
                     core_.setShutterOpen(false);
                  }
               }
            }
         } catch (MMScriptException mex) {
            gui_.showError(mex);
         } catch (Exception ex) {
            gui_.showError(ex);
         } finally {
            try {
               if (core_.isSequenceRunning(firstCamera)) {
                  core_.stopSequenceAcquisition(firstCamera);
               }
               if (secondCamera != null && core_.isSequenceRunning(secondCamera)) {
                  core_.stopSequenceAcquisition(secondCamera);
               }
               if (autoShutter) {
                  core_.setAutoShutter(true);

                  if (!shutterOpen) {
                     core_.setShutterOpen(false);
                  }
               }
               
               // the controller will end with both beams disabled and scan off so reflect that here
               props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.BEAM_ENABLED,
                     Properties.Values.NO, true);
               props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.BEAM_ENABLED,
                     Properties.Values.NO, true);
               props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SA_MODE_X,
                     Properties.Values.SAM_DISABLED, true);
               props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SA_MODE_X,
                     Properties.Values.SAM_DISABLED, true);
               
               updateAcqusitionStatusDone();

               stagePosUpdater_.setAcqRunning(false);
               bq.add(TaggedImageQueue.POISON);
               gui_.closeAcquisition(acqName);
               gui_.logMessage("Acquisition took: " + 
                       (System.currentTimeMillis() - acqStart) + "ms");
               
               // return camera trigger mode 
               cameras_.setSPIMCameraTriggerMode(Cameras.TriggerModes.INTERNAL);
               gui_.enableLiveMode(liveMode);
               
            } catch (Exception ex) {
               // exception while stopping sequence acquisition, not sure what to do...
               gui_.showError(ex, "Problem while finsihing acquisition");
            }
         }

      }

      return true;
   }
   

   @Override
   public void saveSettings() {
      prefs_.putBoolean(panelName_, Properties.Keys.PLUGIN_SAVE_WHILE_ACQUIRING,
              saveCB_.isSelected());
      prefs_.putString(panelName_, Properties.Keys.PLUGIN_DIRECTORY_ROOT,
              rootField_.getText());
      prefs_.putString(panelName_, Properties.Keys.PLUGIN_NAME_PREFIX,
              nameField_.getText());
      prefs_.putBoolean(panelName_,
              Properties.Keys.PLUGIN_SEPARATE_VIEWERS_FOR_TIMEPOINTS,
              separateTimePointsCB_.isSelected());

      // save controller settings
      props_.setPropValue(Devices.Keys.PIEZOA, Properties.Keys.SAVE_CARD_SETTINGS,
              Properties.Values.DO_SSZ, true);
      props_.setPropValue(Devices.Keys.PIEZOB, Properties.Keys.SAVE_CARD_SETTINGS,
              Properties.Values.DO_SSZ, true);
      props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SAVE_CARD_SETTINGS,
              Properties.Values.DO_SSZ, true);
      props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SAVE_CARD_SETTINGS,
              Properties.Values.DO_SSZ, true);

   }

   /**
    * Gets called when this tab gets focus. Refreshes values from properties.
    */
   @Override
   public void gotSelected() {
      props_.callListeners();
      joystick_.unsetAllJoysticks();  // disable all joysticks on this tab
   }

   /**
    * called when tab looses focus.
    */
   @Override
   public void gotDeSelected() {
      saveSettings();
   }

   @Override
   public void devicesChangedAlert() {
      devices_.callListeners();
   }

   private void setRootDirectory(JTextField rootField) {
      File result = FileDialogs.openDir(null,
              "Please choose a directory root for image data",
              MMStudioMainFrame.MM_DATA_SET);
      if (result != null) {
         rootField.setText(result.getAbsolutePath());
      }
   }

   /**
    * The basic method for adding images to an existing data set. If the
    * acquisition was not previously initialized, it will attempt to initialize
    * it from the available image data. This version uses a blocking queue and is 
    * much faster than the one currently implemented in the ScriptInterface
    * Eventually, this function should be replaced by the ScriptInterface version
    * of the same.
    * @param name - named acquisition to add image to
    * @param frame - frame nr at which to insert the image
    * @param channel - channel at which to insert image
    * @param slice - (z) slice at which to insert image
    * @param position - position at which to insert image
    * @param ms - Time stamp to be added to the image metadata
    * @param taggedImg - image + metadata to be added
    * @param bq - Blocking queue to which the image should be added.  This queue
    * should be hooked up to the ImageCache belonging to this acquisitions
    * @throws org.micromanager.utils.MMScriptException
    */
   public void addImageToAcquisition(String name,
           int frame,
           int channel,
           int slice,
           int position,
           long ms,
           TaggedImage taggedImg,
           BlockingQueue<TaggedImage> bq) throws MMScriptException {

      MMAcquisition acq = gui_.getAcquisition(name);

      // check position, for multi-position data set the number of declared 
      // positions should be at least 2
      if (acq.getPositions() <= 1 && position > 0) {
         throw new MMScriptException("The acquisition was opened as a single position data set.\n"
                 + "Open acqusition with two or more positions in order to crate a multi-position data set.");
      }

      // check position, for multi-position data set the number of declared 
      // positions should be at least 2
      if (acq.getChannels() <= channel) {
         throw new MMScriptException("This acquisition was opened with " + acq.getChannels() + " channels.\n"
                 + "The channel number must not exceed declared number of positions.");
      }


      JSONObject tags = taggedImg.tags;

      if (!acq.isInitialized()) {
         throw new MMScriptException("Error in the ASIdiSPIM logic.  Acquisition should have been initialized");
      }

      // create required coordinate tags
      try {
         tags.put(MMTags.Image.FRAME_INDEX, frame);
         tags.put(MMTags.Image.FRAME, frame);
         tags.put(MMTags.Image.CHANNEL_INDEX, channel);
         tags.put(MMTags.Image.SLICE_INDEX, slice);
         tags.put(MMTags.Image.POS_INDEX, position);
         tags.put(MMTags.Image.ELAPSED_TIME_MS, ms);

         if (!tags.has(MMTags.Summary.SLICES_FIRST) && !tags.has(MMTags.Summary.TIME_FIRST)) {
            // add default setting
            tags.put(MMTags.Summary.SLICES_FIRST, true);
            tags.put(MMTags.Summary.TIME_FIRST, false);
         }

         if (acq.getPositions() > 1) {
            // if no position name is defined we need to insert a default one
            if (tags.has(MMTags.Image.POS_NAME)) {
               tags.put(MMTags.Image.POS_NAME, "Pos" + position);
            }
         }

         // update frames if necessary
         if (acq.getFrames() <= frame) {
            acq.setProperty(MMTags.Summary.FRAMES, Integer.toString(frame + 1));
         }

      } catch (JSONException e) {
         throw new MMScriptException(e);
      }

      bq.add(taggedImg);
   }
   
   
}
