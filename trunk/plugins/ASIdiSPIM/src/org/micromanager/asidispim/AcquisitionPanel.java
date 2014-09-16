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


import org.micromanager.asidispim.Data.AcquisitionModes;
import org.micromanager.asidispim.Data.CameraModes;
import org.micromanager.asidispim.Data.Cameras;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Joystick;
import org.micromanager.asidispim.Data.MyStrings;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.DevicesListenerInterface;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.MyNumberUtils;
import org.micromanager.asidispim.Utils.PanelUtils;
import org.micromanager.asidispim.Utils.SliceTiming;
import org.micromanager.asidispim.Utils.StagePositionUpdater;

import java.awt.Color;
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
import javax.swing.JToggleButton;
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
import org.micromanager.acquisition.ComponentTitledBorder;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.DefaultTaggedImageSink;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.acquisition.TaggedImageStorageDiskDefault;
import org.micromanager.acquisition.TaggedImageStorageMultipageTiff;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;

import com.swtdesigner.SwingResourceManager;

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
   private final JCheckBox advancedSliceTimingCB_;
   private final JSpinner numSlices_;
   private final JComboBox numSides_;
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
   private final JSpinner numTimepoints_;
   private final JSpinner acquisitionInterval_;
   private final JToggleButton buttonStart_;
   private final JPanel volPanel_;
   private final JPanel slicePanel_;
   private final JPanel repeatPanel_;
   private final JPanel savePanel_;
   private final JPanel durationPanel_;
   private final JTextField rootField_;
   private final JTextField nameField_;
   private final JLabel acquisitionStatusLabel_;
   private int numTimePointsDone_;
   private AtomicBoolean stop_ = new AtomicBoolean(false);  // true if we should stop acquisition
   private final StagePositionUpdater stagePosUpdater_;
   private final JSpinner stepSize_;
   private final JLabel desiredSlicePeriodLabel_;
   private final JSpinner desiredSlicePeriod_;
   private final JLabel desiredLightExposureLabel_;
   private final JSpinner desiredLightExposure_;
   private final JButton calculateSliceTiming_;
   private final JCheckBox minSlicePeriodCB_;
   private final JCheckBox separateTimePointsCB_;
   private final JCheckBox saveCB_;
   private final JCheckBox hideCB_;
   private final JComboBox spimMode_;
   private final JCheckBox navigationJoysticksCB_;
   private final JPanel leftColumnPanel_;
   private final JPanel centerColumnPanel_;
   private final JPanel rightColumnPanel_;
   
   
   public AcquisitionPanel(ScriptInterface gui, 
           Devices devices, 
           Properties props, 
           Joystick joystick,
           Cameras cameras, 
           Prefs prefs, 
           StagePositionUpdater stagePosUpdater) {
      super(MyStrings.PanelNames.ACQUSITION.toString(),
              new MigLayout(
              "",
              "[center]0[center]0[center]",
              "[top]0[]"));
      gui_ = gui;
      devices_ = devices;
      props_ = props;
      joystick_ = joystick;
      cameras_ = cameras;
      prefs_ = prefs;
      stagePosUpdater_ = stagePosUpdater;
      core_ = gui_.getMMCore();
      numTimePointsDone_ = 0;
      
      PanelUtils pu = new PanelUtils(gui_, prefs_, props_, devices_);
      
      // added to spinner controls where we should re-calculate the displayed
      // slice period, volume duration, and time lapse duration
      ChangeListener recalculateTimingDisplayCL = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            updateActualSlicePeriodLabel();
            updateActualVolumeDurationLabel();
            updateActualTimeLapseDurationLabel();
         }
      };
      
      // added to combobox controls where we should re-calculate the displayed
      // slice period, volume duration, and time lapse duration
      ActionListener recalculateTimingDisplayAL = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            updateActualSlicePeriodLabel();
            updateActualVolumeDurationLabel();
            updateActualTimeLapseDurationLabel();
         }
      };
      
      
      // start volume (main) sub-panel

      volPanel_ = new JPanel(new MigLayout(
              "",
              "[right]10[center]",
              "[]8[]"));

      volPanel_.setBorder(PanelUtils.makeTitledBorder("Volume Settings"));

      volPanel_.add(new JLabel("Number of sides:"));
      String [] sides21 = {"2", "1"};
      numSides_ = pu.makeDropDownBox(sides21, Devices.Keys.PLUGIN,
            Properties.Keys.PLUGIN_NUM_SIDES);
      numSides_.addActionListener(recalculateTimingDisplayAL);
      volPanel_.add(numSides_, "wrap");

      volPanel_.add(new JLabel("First side:"));
      String[] ab = {Devices.Sides.A.toString(), Devices.Sides.B.toString()};
      firstSide_ = pu.makeDropDownBox(ab, Devices.Keys.PLUGIN,
            Properties.Keys.PLUGIN_FIRST_SIDE);
      volPanel_.add(firstSide_, "wrap");
      
      volPanel_.add(new JLabel("Delay before side [ms]:"));
      delaySide_ = pu.makeSpinnerFloat(0, 10000, 0.25,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_DELAY_SIDE, 0);
      delaySide_.addChangeListener(recalculateTimingDisplayCL);
      volPanel_.add(delaySide_, "wrap");

      volPanel_.add(new JLabel("Slices per volume:"));
      numSlices_ = pu.makeSpinnerInteger(1, 1000,
              Devices.Keys.PLUGIN,
              Properties.Keys.PLUGIN_NUM_SLICES, 20);
      numSlices_.addChangeListener(recalculateTimingDisplayCL);
      volPanel_.add(numSlices_, "wrap");
      
      volPanel_.add(new JLabel("Slice step size [\u00B5m]:"));
      stepSize_ = pu.makeSpinnerFloat(0, 100, 0.1,
            Devices.Keys.PLUGIN, Properties.Keys.PLUGIN_SLICE_STEP_SIZE,
            1.0);
      volPanel_.add(stepSize_, "wrap");
      
      // out of order so we can reference it
      desiredSlicePeriod_ = pu.makeSpinnerFloat(1, 1000, 0.25,
            Devices.Keys.PLUGIN, Properties.Keys.PLUGIN_DESIRED_SLICE_PERIOD, 30);
      
      minSlicePeriodCB_ = pu.makeCheckBox("Minimize slice period",
            Properties.Keys.PLUGIN_MINIMIZE_SLICE_PERIOD, panelName_, false); 
      minSlicePeriodCB_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            boolean doMin = minSlicePeriodCB_.isSelected();
            desiredSlicePeriod_.setEnabled(!doMin);
         }
      });
      // initialize correctly
      minSlicePeriodCB_.doClick();
      minSlicePeriodCB_.doClick();
      volPanel_.add(minSlicePeriodCB_, "span 2, wrap");
      
      // special field that is enabled/disabled depending on whether advanced timing is enabled
      desiredSlicePeriodLabel_ = new JLabel("Slice period [ms]:"); 
      volPanel_.add(desiredSlicePeriodLabel_);
      volPanel_.add(desiredSlicePeriod_, "wrap");
      desiredSlicePeriod_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent ce) {
            // make sure is multiple of 0.25
            float userVal = PanelUtils.getSpinnerFloatValue(desiredSlicePeriod_);
            float nearestValid = MyNumberUtils.roundToQuarterMs(userVal);
            if (!MyNumberUtils.floatsEqual(userVal, nearestValid)) {
               PanelUtils.setSpinnerFloatValue(desiredSlicePeriod_, nearestValid);
            }
         }
      });
      
      // special field that is enabled/disabled depending on whether advanced timing is enabled
      desiredLightExposureLabel_ = new JLabel("Sample exposure [ms]:"); 
      volPanel_.add(desiredLightExposureLabel_);
      desiredLightExposure_ = pu.makeSpinnerFloat(2.5, 1000.5, 1,
            Devices.Keys.PLUGIN, Properties.Keys.PLUGIN_DESIRED_EXPOSURE, 8.5);
      desiredLightExposure_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent ce) {
            // make sure is 2.5, 2.5, 3.5, ... 
            float val = PanelUtils.getSpinnerFloatValue(desiredLightExposure_);
            float nearestValid = (float) Math.round(val+(float)0.5) - (float)0.5; 
            if (!MyNumberUtils.floatsEqual(val, nearestValid)) {
               PanelUtils.setSpinnerFloatValue(desiredLightExposure_, nearestValid);
            }
         }
      });
      volPanel_.add(desiredLightExposure_, "wrap");
      
      calculateSliceTiming_ = new JButton("Calculate slice timing");
      calculateSliceTiming_.setToolTipText("Must recalculate after changing the camera ROI.");
      calculateSliceTiming_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            recalculateSliceTiming(!minSlicePeriodCB_.isSelected());
         }
      });
      volPanel_.add(calculateSliceTiming_, "center, span 2, wrap");
      
      // end volume sub-panel
      
      
      // start slice timing (advanced) sub-panel

      slicePanel_ = new JPanel(new MigLayout(
              "",
              "[right]10[center]",
              "[]8[]"));
      
      // special checkbox in titled border to enable/disable sub-panel plus more
      
      advancedSliceTimingCB_ = pu.makeCheckBox("Slice Timing Settings (Advanced)",
            Properties.Keys.PLUGIN_ADVANCED_SLICE_TIMING, panelName_, true);
      advancedSliceTimingCB_.setToolTipText("See ASI Tiger SPIM documentation for details");
      advancedSliceTimingCB_.setFocusPainted(false); 
      ComponentTitledBorder componentBorder = 
              new ComponentTitledBorder(advancedSliceTimingCB_, slicePanel_ 
              , BorderFactory.createLineBorder(ASIdiSPIM.borderColor)); 

      // this action listener takes care of enabling/disabling inputs
      // we call this to get GUI looking right
      ActionListener sliceTimingDisableGUIInputs = new ActionListener(){ 
         public void actionPerformed(ActionEvent e){ 
            boolean enabled = advancedSliceTimingCB_.isSelected(); 
            Component comp[] = slicePanel_.getComponents(); 
            for(int i = 0; i<comp.length; i++){ 
               comp[i].setEnabled(enabled); 
            }
            desiredSlicePeriod_.setEnabled(!enabled && !minSlicePeriodCB_.isSelected());
            desiredSlicePeriodLabel_.setEnabled(!enabled);
            desiredLightExposure_.setEnabled(!enabled);
            desiredLightExposureLabel_.setEnabled(!enabled);
            calculateSliceTiming_.setEnabled(!enabled);
            minSlicePeriodCB_.setEnabled(!enabled);
         } 
      };
      
      // this action listener actually recalculates the timings
      // don't add this action listener until after GUI is set
      ActionListener sliceTimingCalculate = new ActionListener(){
        public void actionPerformed(ActionEvent e){
           boolean enabled = advancedSliceTimingCB_.isSelected(); 
           prefs_.putBoolean(panelName_,
                 Properties.Keys.PLUGIN_ADVANCED_SLICE_TIMING, enabled);
        }
      };
      slicePanel_.setBorder(componentBorder);

      slicePanel_.add(new JLabel("Delay before scan [ms]:"));
      delayScan_ = pu.makeSpinnerFloat(0, 10000, 0.25,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_SCAN, 0);
      delayScan_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(delayScan_, "wrap");

      slicePanel_.add(new JLabel("Lines scans per slice:"));
      numScansPerSlice_ = pu.makeSpinnerInteger(1, 1000,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_NUM_SCANSPERSLICE, 1);
      numScansPerSlice_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(numScansPerSlice_, "wrap");

      slicePanel_.add(new JLabel("Line scan period [ms]:"));
      lineScanPeriod_ = pu.makeSpinnerInteger(1, 10000,
              new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
              Properties.Keys.SPIM_LINESCAN_PERIOD, 10);
      lineScanPeriod_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(lineScanPeriod_, "wrap");
      
      slicePanel_.add(new JSeparator(), "span 2, wrap");
      
      slicePanel_.add(new JLabel("Delay before laser [ms]:"));
      delayLaser_ = pu.makeSpinnerFloat(0, 10000, 0.25,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_LASER, 0);
      delayLaser_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(delayLaser_, "wrap");
      
      slicePanel_.add(new JLabel("Laser trig duration [ms]:"));
      durationLaser_ = pu.makeSpinnerFloat(0, 10000, 0.25,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DURATION_LASER, 1);
      durationLaser_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(durationLaser_, "span 2, wrap");
      
      slicePanel_.add(new JSeparator(), "wrap");

      slicePanel_.add(new JLabel("Delay before camera [ms]:"));
      delayCamera_ = pu.makeSpinnerFloat(0, 10000, 0.25,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DELAY_CAMERA, 0);
      delayCamera_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(delayCamera_, "wrap");
      
      slicePanel_.add(new JLabel("Camera trig duration [ms]:"));
      durationCamera_ = pu.makeSpinnerFloat(0, 1000, 0.25,
            new Devices.Keys[]{Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SPIM_DURATION_CAMERA, 0);
      durationCamera_.addChangeListener(recalculateTimingDisplayCL);
      slicePanel_.add(durationCamera_, "wrap");
      
      // end slice sub-panel
      

      // start repeat (time lapse) sub-panel

      repeatPanel_ = new JPanel(new MigLayout(
              "",
              "[right]12[center]",
              "[]8[]"));

      repeatPanel_.setBorder(PanelUtils.makeTitledBorder("Time Lapse Settings"));
      
      ChangeListener recalculateTimeLapseDisplay = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            updateActualTimeLapseDurationLabel();
         }
      };

      repeatPanel_.add(new JLabel("Num time points:"));
      numTimepoints_ = pu.makeSpinnerInteger(1, 32000,
              Devices.Keys.PLUGIN,
              Properties.Keys.PLUGIN_NUM_ACQUISITIONS, 1);
      numTimepoints_.addChangeListener(recalculateTimeLapseDisplay);
      repeatPanel_.add(numTimepoints_, "wrap");

      repeatPanel_.add(new JLabel("Interval [s]:"));
      acquisitionInterval_ = pu.makeSpinnerFloat(1, 32000, 0.1,
              Devices.Keys.PLUGIN,
              Properties.Keys.PLUGIN_ACQUISITION_INTERVAL, 60);
      acquisitionInterval_.addChangeListener(recalculateTimeLapseDisplay);
      repeatPanel_.add(acquisitionInterval_, "wrap");
      
      // end repeat sub-panel
      
      
      // start savePanel
      
      final int textFieldWidth = 20;
      savePanel_ = new JPanel(new MigLayout(
              "",
              "[right]10[center]8[left]",
              "[]8[]"));
      savePanel_.setBorder(PanelUtils.makeTitledBorder("Data Saving Settings"));
      
      separateTimePointsCB_ = pu.makeCheckBox("Separate viewer / file for each time point",
            Properties.Keys.PLUGIN_SEPARATE_VIEWERS_FOR_TIMEPOINTS, panelName_, false); 
      savePanel_.add(separateTimePointsCB_, "span 3, left, wrap");
      
      hideCB_ = pu.makeCheckBox("Hide viewer",
            Properties.Keys.PLUGIN_HIDE_WHILE_ACQUIRING, panelName_, false); 
      savePanel_.add(hideCB_, "left");
      hideCB_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
            // if viewer is hidden then force saving to disk
            if (hideCB_.isSelected()) {
               if (!saveCB_.isSelected()) {
                  saveCB_.doClick();
               }
               saveCB_.setEnabled(false);
            } else {
               saveCB_.setEnabled(true);
            }
         }
      });
      
      
      saveCB_ = pu.makeCheckBox("Save while acquiring",
            Properties.Keys.PLUGIN_SAVE_WHILE_ACQUIRING, panelName_, false);
      // init the save while acquiring CB; could also do two doClick() calls
      if (hideCB_.isSelected()) {
         saveCB_.setEnabled(false);
      }
      savePanel_.add(saveCB_, "span 2, center, wrap");

      JLabel dirRootLabel = new JLabel ("Directory root:");
      savePanel_.add(dirRootLabel);

      rootField_ = new JTextField();
      rootField_.setText( prefs_.getString(panelName_, 
              Properties.Keys.PLUGIN_DIRECTORY_ROOT, "") );
      rootField_.setColumns(textFieldWidth);
      savePanel_.add(rootField_, "span 2");

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
      namePrefixLabel.setText("Name prefix:");
      savePanel_.add(namePrefixLabel);

      nameField_ = new JTextField("acq");
      nameField_.setText( prefs_.getString(panelName_,
              Properties.Keys.PLUGIN_NAME_PREFIX, "acq"));
      nameField_.setColumns(textFieldWidth);
      savePanel_.add(nameField_, "span 2, wrap");
      
      final JComponent[] saveComponents = { browseRootButton, rootField_, 
                                            dirRootLabel, namePrefixLabel, nameField_ };
      setDataSavingComponents(saveComponents);
      
      saveCB_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setDataSavingComponents(saveComponents);
         }
      });
      
      // end save panel
      
      // start duration report panel
      
      durationPanel_ = new JPanel(new MigLayout(
            "",
            "[right]6[left, 40%!]",
            "[]6[]"));
      durationPanel_.setBorder(PanelUtils.makeTitledBorder("Durations"));
      
      durationPanel_.add(new JLabel("Slice:"));
      actualSlicePeriodLabel_ = new JLabel();
      durationPanel_.add(actualSlicePeriodLabel_, "wrap");
      
      durationPanel_.add(new JLabel("Volume:"));
      actualVolumeDurationLabel_ = new JLabel();
      durationPanel_.add(actualVolumeDurationLabel_, "wrap");
      
      durationPanel_.add(new JLabel("Time lapse:"));
      actualTimeLapseDurationLabel_ = new JLabel();
      durationPanel_.add(actualTimeLapseDurationLabel_, "wrap");
      
      // end duration report panel
      
      navigationJoysticksCB_ = new JCheckBox("Use Navigation joystick settings");
      navigationJoysticksCB_.setSelected(prefs_.getBoolean(panelName_,
            Properties.Keys.PLUGIN_USE_NAVIGATION_JOYSTICKS, false));
      navigationJoysticksCB_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) { 
            if (navigationJoysticksCB_.isSelected()) {
               ASIdiSPIM.getFrame().getNavigationPanel().doJoystickSettings();
            } else {
               joystick_.unsetAllJoysticks();
            }
            prefs_.putBoolean(panelName_, Properties.Keys.PLUGIN_USE_NAVIGATION_JOYSTICKS,
                  navigationJoysticksCB_.isSelected());
         }
      });
      
      buttonStart_ = new JToggleButton();
      buttonStart_.setIconTextGap(6);
      buttonStart_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            updateStartButton();
         }
      });
      updateStartButton();  // do once to start, isSelected() will be false

      acquisitionStatusLabel_ = new JLabel("");
      updateAcquisitionStatus(AcquisitionStatus.NONE);
      
      // set up tabbed panel for GUI, 3 panels for columns of settings to
      // get vertical space right in each column
      
      leftColumnPanel_ = new JPanel(new MigLayout(
            "",
            "[]",
            "[]8[]"));
      
      leftColumnPanel_.add(repeatPanel_, "split 2");
      leftColumnPanel_.add(durationPanel_, "wrap");
      leftColumnPanel_.add(savePanel_, "wrap");
      leftColumnPanel_.add(new JLabel("SPIM mode: "), "split 2, left");
      AcquisitionModes acqModes = new AcquisitionModes(devices_, props_, prefs_);
      spimMode_ = acqModes.getComboBox(); 
      leftColumnPanel_.add(spimMode_, "wrap");
      leftColumnPanel_.add(buttonStart_, "split 2, left");
      leftColumnPanel_.add(acquisitionStatusLabel_);
      
      centerColumnPanel_ = new JPanel(new MigLayout(
            "",
            "[]",
            "[]8[]"));
      
      centerColumnPanel_.add(volPanel_, "wrap");
      centerColumnPanel_.add(navigationJoysticksCB_);
      
      rightColumnPanel_ = new JPanel(new MigLayout(
            "",
            "[]",
            "[]8[]"));
      
      
      rightColumnPanel_.add(slicePanel_);
      
      // add the column panels to the main panel
      add(leftColumnPanel_);
      add(centerColumnPanel_);
      add(rightColumnPanel_);
      
      // properly initialize the advanced slice timing
      advancedSliceTimingCB_.addActionListener(sliceTimingDisableGUIInputs);
      advancedSliceTimingCB_.doClick();
      advancedSliceTimingCB_.doClick();
      advancedSliceTimingCB_.addActionListener(sliceTimingCalculate);
      
      updateActualSlicePeriodLabel();
      updateActualVolumeDurationLabel();
      updateActualTimeLapseDurationLabel();
      
   }//end constructor
   
   private void updateStartButton() {
      boolean started = buttonStart_.isSelected();
      stop_.set(!started);
      if (started) {
         class acqThread extends Thread {
            acqThread(String threadName) {
               super(threadName);
            }

            @Override
            public void run() {
               runAcquisition();
               if (buttonStart_.isSelected()) {
                  buttonStart_.doClick();
               }
            }
         }            
         acqThread acqt = new acqThread("diSPIM Acquisition");
         acqt.start(); 
      }
      buttonStart_.setText(started ? "Stop!" : "Start!");
      buttonStart_.setBackground(started ? Color.red : Color.green);
      buttonStart_.setIcon(started ?
            SwingResourceManager.
            getIcon(MMStudio.class,
            "/org/micromanager/icons/cancel.png")
            : SwingResourceManager.getIcon(MMStudio.class,
                  "/org/micromanager/icons/arrow_right.png"));
   }
   
   /**
    * @return either "A" or "B"
    */
   private String getFirstSide() {
      return (String)firstSide_.getSelectedItem();
   }
   
   private boolean isFirstSideA() {
      return getFirstSide().equals("A");
   }

   /**
    * @return either 1 or 2
    */
   private int getNumSides() {
      if (numSides_.getSelectedIndex() == 1) {
         return 1;
      } else {
         return 2;
      }
   }
   
   private boolean isTwoSided() {
      return (numSides_.getSelectedIndex() == 0);
   }
   
   private int getNumTimepoints() {
      return (Integer) numTimepoints_.getValue();
   }
   
   private int getLineScanPeriod() {
      return (Integer) lineScanPeriod_.getValue();
   }
   
   private int getNumScansPerSlice() {
      return (Integer) numScansPerSlice_.getValue();
   }
   
   private int getNumSlices() {
      return (Integer) numSlices_.getValue();
   }
   
   private double getStepSizeUm() {
      return PanelUtils.getSpinnerFloatValue(stepSize_);
   }
   
   
   
   /**
    * 
    * @param showWarnings true to warn user about needing to change slice period
    * @return
    */
   private SliceTiming getTimingFromPeriodAndLightExposure(boolean showWarnings) {
      // uses algorithm Jon worked out in Octave code; each slice period goes like this:
      // 1. camera readout time
      // 2. any extra delay time
      // 3. camera reset
      // 4. start scan and then turn on laser (the laser is off 0.25ms at the start and end of the scan)
      
      final float scanLaserBufferTime = (float) 0.25;
      final Color foregroundColorOK = Color.BLACK;
      final Color foregroundColorError = Color.RED;
      final Component elementToColor  = desiredSlicePeriod_.getEditor().getComponent(0);
      
      SliceTiming s = new SliceTiming();
      float cameraResetTime = computeCameraResetTime();      // recalculate for safety
      float cameraReadoutTime = computeCameraReadoutTime();  // recalculate for safety
      
      CameraModes.Keys cameraMode = CameraModes.getKeyFromPrefCode(
            prefs_.getInt(MyStrings.PanelNames.SETTINGS.toString(),
                  Properties.Keys.PLUGIN_CAMERA_MODE, 0));
      if (cameraMode == CameraModes.Keys.OVERLAP) {
         cameraReadoutTime = 0;
      }
      
      float desiredPeriod = minSlicePeriodCB_.isSelected() ? 0 :
         PanelUtils.getSpinnerFloatValue(desiredSlicePeriod_);
      float desiredExposure = PanelUtils.getSpinnerFloatValue(desiredLightExposure_);
      
      // this assumes "usual" camera mode, not Hamamatsu's "synchronous" or Zyla's "overlap" mode
      // TODO: add the ability to use these faster modes (will require changes in several places
      // and a GUI setting for camera mode); do have Cameras.resetAndReadoutOverlap() to see
      float cameraReadout_max = MyNumberUtils.ceilToQuarterMs(cameraReadoutTime);
      float cameraReset_max = MyNumberUtils.ceilToQuarterMs(cameraResetTime);
      float slicePeriod = MyNumberUtils.roundToQuarterMs(desiredPeriod);
      int scanPeriod = Math.round(desiredExposure + 2*scanLaserBufferTime);
      // scan will be longer than laser by 0.25ms at both start and end
      float laserDuration = scanPeriod - 2*scanLaserBufferTime;  // will be integer plus 0.5
      
      float globalDelay = slicePeriod - cameraReadout_max - cameraReset_max - scanPeriod;
      
      // if calculated delay is negative then we have to reduce exposure time in 1 sec increments
      if (globalDelay < 0) {
         float extraTimeNeeded = MyNumberUtils.ceilToQuarterMs((float)-1*globalDelay);  // positive number
            globalDelay += extraTimeNeeded;
            if (showWarnings) {
               JOptionPane.showMessageDialog(this,
                     "Increasing slice period to meet laser exposure constraint\n"
                           + "(time required for camera readout; readout time depends on ROI).\n",
                           "Warning",
                  JOptionPane.WARNING_MESSAGE);
               elementToColor.setForeground(foregroundColorError);
               // considered actually changing the value, but decided against it because
               // maybe the user just needs to set the ROI appropriately and recalculate
            } else {
               elementToColor.setForeground(foregroundColorOK);
            }
      } else {
         elementToColor.setForeground(foregroundColorOK);
      }
      
      s.scanDelay = cameraReadout_max + globalDelay + cameraReset_max;
      s.scanNum = 1;
      s.scanPeriod = scanPeriod;
      s.laserDelay = cameraReadout_max + globalDelay + cameraReset_max + scanLaserBufferTime;
      s.laserDuration = laserDuration;
      s.cameraDelay = cameraReadout_max + globalDelay;
      s.cameraDuration = cameraReset_max + scanPeriod;  // approx. same as exposure, can be used in bulb mode
      
      if (cameraMode == CameraModes.Keys.OVERLAP) {
         s.cameraDuration = 1;
      }
      
      return s;
   }
   
   
   /**
    * @return true if the slice timing matches the current user parameters and ROI
    */
   private boolean isSliceTimingUpToDate() {
      SliceTiming currentTiming = getCurrentSliceTiming();
      SliceTiming newTiming = getTimingFromPeriodAndLightExposure(false);
      return currentTiming.equals(newTiming);
   }
   
   
   /**
    * Re-calculate the controller's timing settings for "easy timing" mode.
    * If the values are the same nothing happens.  If they should be changed,
    * then the controller's properties will be set.  Parameter sets whether or
    * not user needs to give permission for change.
    * @param promptBeforeChange true means user has to agree to change
    * @param showWarnings will show warning if the user-specified slice period too short
    * @return true if any change actually made  
    */
   private boolean recalculateSliceTiming(boolean showWarnings) {
      if (!isSliceTimingUpToDate() || 
            ! MyNumberUtils.floatsEqual((float)computeActualSlicePeriod(),
                  PanelUtils.getSpinnerFloatValue(desiredSlicePeriod_))) {
         SliceTiming newTiming = getTimingFromPeriodAndLightExposure(showWarnings);
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
      s.scanNum = getNumScansPerSlice();
      s.scanPeriod = getLineScanPeriod();
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
            (getLineScanPeriod() * getNumScansPerSlice()),
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
      actualSlicePeriodLabel_.setText(
            NumberUtils.doubleToDisplayString(computeActualSlicePeriod()) +
            " ms");
   }
   
   /**
    * Compute the volume duration in ms based on controller's timing settings.
    * @return duration in ms
    */
   private double computeActualVolumeDuration() {
      double duration = getNumSides() * 
            (PanelUtils.getSpinnerFloatValue(delaySide_) +
                  getNumSlices() * computeActualSlicePeriod());
      return duration;
   }
   
   /**
    * Update the displayed volume duration.
    */
   private void updateActualVolumeDurationLabel() {
      actualVolumeDurationLabel_.setText(
            NumberUtils.doubleToDisplayString(computeActualVolumeDuration()) +
            " ms");
   }
   
   /**
    * Compute the time lapse duration
    * @return duration in s
    */
   private double computeActualTimeLapseDuration() {
      double duration = (getNumTimepoints() - 1) * 
            PanelUtils.getSpinnerFloatValue(acquisitionInterval_)
            + computeActualVolumeDuration()/1000;
      return duration;
   }
   
   /**
    * Update the displayed time lapse duration.
    */
   private void updateActualTimeLapseDurationLabel() {
      String s = "";
      double duration = computeActualTimeLapseDuration();
      if (duration < 60) {  // less than 1 min
         s += NumberUtils.doubleToDisplayString(duration) + " s";
      } else if (duration < 60*60) { // between 1 min and 1 hour
         s += NumberUtils.doubleToDisplayString(Math.floor(duration/60)) + " min ";
         s += NumberUtils.doubleToDisplayString(Math.round(duration %  60)) + " s";
      } else { // longer than 1 hour
         s += NumberUtils.doubleToDisplayString(Math.floor(duration/(60*60))) + " hr ";
         s +=  NumberUtils.doubleToDisplayString(Math.round((duration % (60*60))/60)) + " min";
      }
      actualTimeLapseDurationLabel_.setText(s);
   }
   
   /**
    * Computes the reset time of the SPIM cameras set on Devices panel.
    * Handles single-side operation.
    * Needed for computing (semi-)optimized slice timing in "easy timing" mode.
    * @return
    */
   private float computeCameraResetTime() {
      float resetTime;
      if (isTwoSided()) {
         resetTime = Math.max(cameras_.computeCameraResetTime(Devices.Keys.CAMERAA),
               cameras_.computeCameraResetTime(Devices.Keys.CAMERAB));
      } else {
         if (isFirstSideA()) {
            resetTime = cameras_.computeCameraResetTime(Devices.Keys.CAMERAA);
         } else {
            resetTime = cameras_.computeCameraResetTime(Devices.Keys.CAMERAB);
         }
      }
      return resetTime;
   }
   
   /**
    * Computes the readout time of the SPIM cameras set on Devices panel.
    * Handles single-side operation.
    * Needed for computing (semi-)optimized slice timing in "easy timing" mode.
    * @return
    */
   private float computeCameraReadoutTime() {
      float readoutTime;
      if (isTwoSided()) {
         readoutTime = Math.max(cameras_.computeCameraReadoutTime(Devices.Keys.CAMERAA),
               cameras_.computeCameraReadoutTime(Devices.Keys.CAMERAB));
      } else {
         if (isFirstSideA()) {
            readoutTime = cameras_.computeCameraReadoutTime(Devices.Keys.CAMERAA);
         } else {
            readoutTime = cameras_.computeCameraReadoutTime(Devices.Keys.CAMERAB);
         }
      }
      return readoutTime;
   }
   
   /**
    * used for updateAcquisitionStatus() calls 
    */
   private static enum AcquisitionStatus {
      NONE,
      ACQUIRING,
      WAITING,
      DONE,
   }
   
   private void updateAcquisitionStatus(AcquisitionStatus phase) {
      updateAcquisitionStatus(phase, (int) 0);
   }
   
   private void updateAcquisitionStatus(AcquisitionStatus phase, int secsToNextAcquisition) {
      String text = "";
      switch(phase) {
      case NONE:
         text = "No acquisition in progress.";
         break;
      case ACQUIRING:
         text = "Acquiring time point "
               + NumberUtils.intToDisplayString(numTimePointsDone_)
               + " of "
               + NumberUtils.intToDisplayString(getNumTimepoints());
         break;
      case WAITING:
         text = "Finished "
               + NumberUtils.intToDisplayString(numTimePointsDone_)
               + " of "
               + NumberUtils.intToDisplayString(getNumTimepoints())
               + " time points; next in "
               + NumberUtils.intToDisplayString(secsToNextAcquisition)
               + " s.";
         break;
      case DONE:
         text = "Acquisition finished with "
               + NumberUtils.intToDisplayString(numTimePointsDone_)
               + " time points.";
         break;
      default:
         break;   
      }
      acquisitionStatusLabel_.setText(text);
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
    * Sets all the controller's properties according to volume settings
    * and otherwise gets controller all ready for acquisition
    * (except for final trigger).
    * @param side
    * @return false if there was some error that should abort acquisition
    */
   private boolean prepareControllerForAquisition(Devices.Sides side) {
      
      Devices.Keys galvoDevice = Devices.getSideSpecificKey(Devices.Keys.GALVOA, side);
      Devices.Keys piezoDevice = Devices.getSideSpecificKey(Devices.Keys.PIEZOA, side);
      
      // checks to prevent hard-to-diagnose other errors
      if (!devices_.isValidMMDevice(galvoDevice)) {
         gui_.showError("Scanner device required; please check Devices tab.",
               ASIdiSPIM.getFrame());
            return false;
      }

      props_.setPropValue(galvoDevice, Properties.Keys.SPIM_NUM_REPEATS, 1);
      
      AcquisitionModes.Keys spimMode = (AcquisitionModes.Keys) spimMode_.getSelectedItem();
      
      // figure out the piezo parameters
      int numSlices = getNumSlices();
      float piezoAmplitude =  ( (numSlices - 1) * 
              PanelUtils.getSpinnerFloatValue(stepSize_));
      float piezoCenter = prefs_.getFloat(
            MyStrings.PanelNames.SETUP.toString() + side.toString(), 
            Properties.Keys.PLUGIN_PIEZO_CENTER_POS, 0);
      
      // if we set piezoAmplitude to 0 here then sliceAmplitude will also be 0
      if (spimMode.equals(AcquisitionModes.Keys.NO_SCAN)) {
         piezoAmplitude = (float) 0.0;
      }
      
      // tweak the parameters if we are using synchronous/overlap mode
      // object is to get exact same piezo/scanner positions in first
      // N frames (piezo/scanner will move to N+1st position but no image taken)
      CameraModes.Keys cameraMode = CameraModes.getKeyFromPrefCode(
            prefs_.getInt(MyStrings.PanelNames.SETTINGS.toString(),
                  Properties.Keys.PLUGIN_CAMERA_MODE, 0));
      if (cameraMode == CameraModes.Keys.OVERLAP) {
         piezoAmplitude *= ((float)numSlices)/(numSlices-1);
         piezoCenter += piezoAmplitude/(2*numSlices);
         numSlices += 1;
      }
      
      float sliceRate = prefs_.getFloat(
            MyStrings.PanelNames.SETUP.toString() + side.toString(), 
            Properties.Keys.PLUGIN_RATE_PIEZO_SHEET, -80);
      if (MyNumberUtils.floatsEqual(sliceRate, (float) 0.0)) {
         gui_.showError("Rate for slice " + side.toString() + 
               " cannot be zero. Re-do calibration on Setup tab.",
               ASIdiSPIM.getFrame());
         return false;
      }
      float sliceOffset = prefs_.getFloat(
            MyStrings.PanelNames.SETUP.toString() + side.toString(), 
            Properties.Keys.PLUGIN_OFFSET_PIEZO_SHEET, 0);
      float sliceAmplitude = piezoAmplitude / sliceRate;
      float sliceCenter = (piezoCenter - sliceOffset) / sliceRate;

      // get the micro-mirror card ready
      // SA_AMPLITUDE_X_DEG and SA_OFFSET_X_DEG done by setup tabs
      props_.setPropValue(galvoDevice,
            Properties.Keys.SA_AMPLITUDE_Y_DEG, sliceAmplitude);
      props_.setPropValue(galvoDevice,
            Properties.Keys.SA_OFFSET_Y_DEG, sliceCenter);
      props_.setPropValue(galvoDevice,
            Properties.Keys.BEAM_ENABLED, Properties.Values.NO);
      props_.setPropValue(galvoDevice,
            Properties.Keys.SPIM_NUM_SLICES, numSlices);
      props_.setPropValue(galvoDevice,
            Properties.Keys.SPIM_NUM_SIDES, getNumSides());
      props_.setPropValue(galvoDevice,
            Properties.Keys.SPIM_FIRSTSIDE, getFirstSide());
      
      // get the piezo card ready
      if (spimMode.equals(AcquisitionModes.Keys.SLICE_SCAN_ONLY)) {
         piezoAmplitude = (float) 0.0;
      }
      props_.setPropValue(piezoDevice,
            Properties.Keys.SA_AMPLITUDE, piezoAmplitude);
      props_.setPropValue(piezoDevice,
            Properties.Keys.SA_OFFSET, piezoCenter);
      props_.setPropValue(piezoDevice,
            Properties.Keys.SPIM_NUM_SLICES, numSlices);
      props_.setPropValue(piezoDevice,
            Properties.Keys.SPIM_STATE, Properties.Values.SPIM_ARMED);
      return true;
   }

   /**
    * Implementation of acquisition that orchestrates image
    * acquisition itself rather than using the acquisition engine
    * 
    * This methods is public so that the scriptinterface can call it
    * Please do not access this yourself directly
    *
    * @return
    */
   public boolean runAcquisition() {
      
      if (gui_.isAcquisitionRunning()) {
         gui_.showError("An acquisition is already running",
               ASIdiSPIM.getFrame());
         return false;
      }
      
      boolean liveModeOriginally = gui_.isLiveModeOn();
      if (liveModeOriginally) {
         gui_.enableLiveMode(false);
      }
      
      // get MM device names for first/second cameras to acquire
      String firstCamera, secondCamera;
      if (isFirstSideA()) {
         firstCamera = devices_.getMMDevice(Devices.Keys.CAMERAA);
         secondCamera = devices_.getMMDevice(Devices.Keys.CAMERAB);
      } else {
         firstCamera = devices_.getMMDevice(Devices.Keys.CAMERAB);
         secondCamera = devices_.getMMDevice(Devices.Keys.CAMERAA);
      }
      
      boolean sideActiveA, sideActiveB;
      if (isTwoSided()) {
         sideActiveA = true;
         sideActiveB = true;
      } else {
         secondCamera = null;
         if (isFirstSideA()) {
            sideActiveA = true;
            sideActiveB = false;
         } else {
            sideActiveA = false;
            sideActiveB = true;
         }
      }
      
      // make sure we have cameras selected
      int nrSides = getNumSides();  // TODO: multi-channel in sense of excitation color, etc.
      if (firstCamera == null) {
         gui_.showError("Please select a valid camera for the first " +
               "imaging path on the Devices Panel",
               ASIdiSPIM.getFrame());
         return false;
      }
      if (nrSides == 2 && secondCamera == null) {
         gui_.showError("Please select a valid camera for the second " +
               "imaging path on the Devices Panel.",
               ASIdiSPIM.getFrame());
         return false;
      }
      
      // make sure slice timings are up to date 
      if (!advancedSliceTimingCB_.isSelected()) {
         if(!isSliceTimingUpToDate()) {
            gui_.showError("Slice timing is not up to date, please recalculate.", 
                  ASIdiSPIM.getFrame());
            return false;
         }
      }
      
      float cameraReadoutTime = computeCameraReadoutTime();
      double exposureTime = PanelUtils.getSpinnerFloatValue(durationCamera_);
      
      boolean show = !hideCB_.isSelected();
      boolean save = saveCB_.isSelected();
      boolean singleTimePointViewers = separateTimePointsCB_.isSelected();
      String rootDir = rootField_.getText();

      int nrRepeats;  // how many acquisition windows to open
      int nrFrames;   // how many Micro-manager "frames" = time points to take
      if (singleTimePointViewers) {
         nrFrames = 1;
         nrRepeats = getNumTimepoints();
      } else {
         nrFrames = getNumTimepoints();
         nrRepeats = 1;
      }

      long timepointsIntervalMs = Math.round(
              PanelUtils.getSpinnerFloatValue(acquisitionInterval_) * 1000d);
      int nrSlices = getNumSlices();
      int nrPos = 1;  // TODO: multi XY points
      
      boolean autoShutter = core_.getAutoShutter();
      boolean shutterOpen = false;

      // more sanity checks
      double lineScanTime = computeActualSlicePeriod();
      if (exposureTime + cameraReadoutTime > lineScanTime) {
         gui_.showError("Exposure time is longer than time needed for a line scan.\n" +
                 "This will result in dropped frames.\n" +
                 "Please change input",
                 ASIdiSPIM.getFrame());
         return false;
      }
      double volumeDuration = computeActualVolumeDuration();
      if (getNumTimepoints() > 1 && 
            volumeDuration > timepointsIntervalMs) {
         gui_.showError("Time point interval shorter than" +
                 " the time to collect a single volume.\n",
                 ASIdiSPIM.getFrame());
         return false;
      }
      if (nrRepeats > 10 && separateTimePointsCB_.isSelected()) {
         int dialogResult = JOptionPane.showConfirmDialog(null,
               "This will generate " + nrRepeats + " separate windows. "
               + "Do you really want to proceed?",
               "Warning",
               JOptionPane.OK_CANCEL_OPTION);
         if (dialogResult == JOptionPane.CANCEL_OPTION) {
            return false;
         }
      }
      if (hideCB_.isSelected() && !saveCB_.isSelected()) {
         gui_.showError("Must save data to disk if viewer is hidden",
               ASIdiSPIM.getFrame());
         return false;
      }
      if (hideCB_.isSelected() && separateTimePointsCB_.isSelected()) {
         gui_.showError("Cannot have hidden viewer with separate viewers per time point." +
               "Pester the developers if you really need this.",
               ASIdiSPIM.getFrame());
         return false;
      }

      try {
         // empty out circular buffer
         while (core_.getRemainingImageCount() > 0) {
            core_.popNextImage();
         }
      } catch (Exception ex) {
         gui_.showError(ex, "Error emptying out the circular buffer",
               ASIdiSPIM.getFrame());
         return false;
      }
      
      cameras_.setSPIMCamerasForAcquisition(true);

      // stop the serial traffic for position updates during acquisition
      stagePosUpdater_.setAcqRunning(true);
      
      numTimePointsDone_ = 0;
      
      // force saving as image stacks, not individual files
      // implementation assumes just two options, either 
      //  TaggedImageStorageDiskDefault.class or TaggedImageStorageMultipageTiff.class
      boolean separateImageFilesOriginally =
            ImageUtils.getImageStorageClass().equals(TaggedImageStorageDiskDefault.class);
      ImageUtils.setImageStorageClass(TaggedImageStorageMultipageTiff.class);
      
      // Set up controller SPIM parameters (including from Setup panel settings)
      if (sideActiveA) {
         boolean success = prepareControllerForAquisition(Devices.Sides.A);
         if (! success) {
            return false;
         }
      }
      if (sideActiveB) {
         boolean success = prepareControllerForAquisition(Devices.Sides.B);
         if (! success) {
            return false;
         }
      }

      long acqStart = System.currentTimeMillis();

      // do not want to return from within this loop
      // loop is executed once per acquisition (once if separate viewers isn't selected)
      for (int tp = 0; tp < nrRepeats && !stop_.get(); tp++) {
         BlockingQueue<TaggedImage> bq = new LinkedBlockingQueue<TaggedImage>(10);
         String acqName;
         if (singleTimePointViewers) {
            acqName = gui_.getUniqueAcquisitionName(nameField_.getText() + "_" + tp);
         } else {
            acqName = gui_.getUniqueAcquisitionName(nameField_.getText());
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
            
            // This Property has to be set before initialization to be propagated 
            // to the ImageJ metadata
            gui_.setAcquisitionProperty(acqName, "z-step_um",  
                    NumberUtils.doubleToDisplayString(getStepSizeUm()) );
            
            // initialize acquisition
            gui_.initializeAcquisition(acqName, (int) core_.getImageWidth(),
                    (int) core_.getImageHeight(), (int) core_.getBytesPerPixel(),
                    (int) core_.getImageBitDepth());
            
            // These metadata have to added after initialization, otherwise
            // they will not be shown?!
            gui_.setAcquisitionProperty(acqName, "NumberOfSides", 
                    NumberUtils.doubleToDisplayString(getNumSides()) );
            String firstSide = "B";
            if (isFirstSideA()) {
               firstSide = "A";
            }            
            gui_.setAcquisitionProperty(acqName, "FirstSide", firstSide);
            gui_.setAcquisitionProperty(acqName, "SlicePeriod_ms", 
                  actualSlicePeriodLabel_.getText());
            gui_.setAcquisitionProperty(acqName, "LaserExposure_ms",
                  NumberUtils.doubleToDisplayString(
                        (double)PanelUtils.getSpinnerFloatValue(durationLaser_)));
            gui_.setAcquisitionProperty(acqName, "VolumeDuration", 
                    actualVolumeDurationLabel_.getText());
            gui_.setAcquisitionProperty(acqName, "SPIMmode", 
                    ((AcquisitionModes.Keys) spimMode_.getSelectedItem()).toString());
            
            // TODO: use new acqusition interface that goes through the pipeline
            //gui_.setAcquisitionAddImageAsynchronous(acqName); 
            MMAcquisition acq = gui_.getAcquisition(acqName);
            
            // Dive into MM internals since script interface does not support pipelines
            ImageCache imageCache = acq.getImageCache();
            VirtualAcquisitionDisplay vad = acq.getAcquisitionWindow();
            imageCache.addImageCacheListener(vad);

            // Start pumping images into the ImageCache
            DefaultTaggedImageSink sink = new DefaultTaggedImageSink(bq, imageCache);
            sink.start();

            // Loop over all the time points
            // If the interval between frames is shorter than the time to acquire
            // them, we can switch to hardware based solution.  Not sure how important 
            // that feature is, so leave it out for now.
            for (int f = 0; f < nrFrames && !stop_.get(); f++) {
               long acqNow = System.currentTimeMillis();
               long delay = acqStart + f * timepointsIntervalMs - acqNow;
               while (delay > 0 && !stop_.get()) {
                  updateAcquisitionStatus(AcquisitionStatus.WAITING, (int) (delay / 1000));
                  long sleepTime = Math.min(1000, delay);
                  Thread.sleep(sleepTime);
                  acqNow = System.currentTimeMillis();
                  delay = acqStart + f * timepointsIntervalMs - acqNow;
               }

               numTimePointsDone_++;
               updateAcquisitionStatus(AcquisitionStatus.ACQUIRING);
               
               if (core_.getBufferTotalCapacity() == 0) {
                  core_.initializeCircularBuffer();
               }
               core_.startSequenceAcquisition(firstCamera, nrSlices, 0, true);
               if (nrSides == 2) {
                  core_.startSequenceAcquisition(secondCamera, nrSlices, 0, true);
               }

               // deal with shutter
               if (autoShutter) {
                  core_.setAutoShutter(false);
                  shutterOpen = core_.getShutterOpen();
                  if (!shutterOpen) {
                     core_.setShutterOpen(true);
                  }
               }

               // trigger the Tiger controller
               // TODO generalize this for different ways of running SPIM
               // only matters which device we trigger if there are two micro-mirror cards
               if (isFirstSideA()) {
                  props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SPIM_STATE,
                        Properties.Values.SPIM_RUNNING, true);
               } else {
                  props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SPIM_STATE,
                        Properties.Values.SPIM_RUNNING, true);
               }
                  
               // Wait for first image to create ImageWindow, so that we can be sure about image size
               long start = System.currentTimeMillis();
               long now = start;
               long timeout = 10000;
               while (core_.getRemainingImageCount() == 0 && (now - start < timeout)
                     && !stop_.get()) {
                  now = System.currentTimeMillis();
                  Thread.sleep(5);
               }
               if (now - start >= timeout) {
                  throw new Exception("Camera did not send image within a reasonable time");
               }
               if (stop_.get()) {
                  throw new IllegalMonitorStateException("User stopped the acquisition");
               }

               // gather all the images from the cameras, put them into the acquisition
               int[] frNumber = new int[2];
               boolean done = false;
               long timeout2;
               timeout2 = Math.max(10000, Math.round(1.2*computeActualVolumeDuration()));
               start = System.currentTimeMillis();
               try {
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
                        done = true;
                     }
                  }
               } catch (InterruptedException iex) {
                  gui_.showError(iex, (Component) ASIdiSPIM.getFrame());
               }
            }
         } catch (IllegalMonitorStateException ex) {
            // do nothing, the acquisition was simply halted before the camera sent images 
         } catch (MMScriptException mex) {
            gui_.showError(mex, (Component) ASIdiSPIM.getFrame());
         } catch (Exception ex) {
            gui_.showError(ex, (Component) ASIdiSPIM.getFrame());
         } finally {  // end of this acquisition (could be about to restart if separate viewers)
            try {
               if (core_.isSequenceRunning(firstCamera)) {
                  core_.stopSequenceAcquisition(firstCamera);
               }
               if (secondCamera != null && core_.isSequenceRunning(secondCamera)) {
                  core_.stopSequenceAcquisition(secondCamera);
               }
               if (autoShutter) {
                  core_.setAutoShutter(true);
                  if (shutterOpen) {
                     core_.setShutterOpen(false);
                  }
               }
               
               bq.put(TaggedImageQueue.POISON);
               gui_.closeAcquisition(acqName);
               gui_.logMessage("diSPIM plugin acquisition took: " + 
                       (System.currentTimeMillis() - acqStart) + "ms");
               
            } catch (Exception ex) {
               // exception while stopping sequence acquisition, not sure what to do...
               gui_.showError(ex, "Problem while finsihing acquisition", ASIdiSPIM.getFrame());
            }
         }

      }
      
      // cleanup after end of all acquisitions
      
      // the controller will end with both beams disabled and scan off so reflect
      // that in device properties
      props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.BEAM_ENABLED,
            Properties.Values.NO, true);
      props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.BEAM_ENABLED,
            Properties.Values.NO, true);
      props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SA_MODE_X,
            Properties.Values.SAM_DISABLED, true);
      props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SA_MODE_X,
            Properties.Values.SAM_DISABLED, true);
      
      if (stop_.get()) {  // if user stopped us in middle
         numTimePointsDone_--;  
         // make sure to stop the SPIM state machine in case the acquisition was cancelled
         props_.setPropValue(Devices.Keys.GALVOA, Properties.Keys.SPIM_STATE,
               Properties.Values.SPIM_IDLE, true);
         props_.setPropValue(Devices.Keys.GALVOB, Properties.Keys.SPIM_STATE,
               Properties.Values.SPIM_IDLE, true);
      }
      updateAcquisitionStatus(AcquisitionStatus.DONE);
      stagePosUpdater_.setAcqRunning(false);
      
      if (separateImageFilesOriginally) {
         ImageUtils.setImageStorageClass(TaggedImageStorageDiskDefault.class);
      }
      cameras_.setSPIMCamerasForAcquisition(false);
      if (liveModeOriginally) {
         gui_.enableLiveMode(true);
      }

      return true;
   }

   @Override
   public void saveSettings() {
      prefs_.putString(panelName_, Properties.Keys.PLUGIN_DIRECTORY_ROOT,
              rootField_.getText());
      prefs_.putString(panelName_, Properties.Keys.PLUGIN_NAME_PREFIX,
              nameField_.getText());

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
      if (navigationJoysticksCB_.isSelected()) {
         if (ASIdiSPIM.getFrame() != null) {
            ASIdiSPIM.getFrame().getNavigationPanel().doJoystickSettings();
         }
      } else {
         joystick_.unsetAllJoysticks();  // disable all joysticks on this tab
      }
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
              MMStudio.MM_DATA_SET);
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
           BlockingQueue<TaggedImage> bq) throws MMScriptException, InterruptedException {

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
         MDUtils.setFrameIndex(tags, frame);
         tags.put(MMTags.Image.FRAME, frame);
         MDUtils.setChannelIndex(tags, channel);
         MDUtils.setSliceIndex(tags, slice);
         MDUtils.setPositionIndex(tags, position);
         MDUtils.setElapsedTimeMs(tags, ms);
         MDUtils.setImageTime(tags, MDUtils.getCurrentTime());
         MDUtils.setZStepUm(tags, PanelUtils.getSpinnerFloatValue(stepSize_));
         
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

      bq.put(taggedImg);
   }
   
   
}
