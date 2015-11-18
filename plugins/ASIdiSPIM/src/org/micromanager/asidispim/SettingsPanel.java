///////////////////////////////////////////////////////////////////////////////
//FILE:          SettingsPanel.java
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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.micromanager.MMStudio;
import org.micromanager.asidispim.Data.CameraModes;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.MyStrings;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.MyDialogUtils;
import org.micromanager.asidispim.Utils.PanelUtils;
import org.micromanager.asidispim.Utils.StagePositionUpdater;
import org.micromanager.utils.FileDialogs;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Jon
 */
@SuppressWarnings("serial")
public class SettingsPanel extends ListeningJPanel {
   
   private final Devices devices_;
   private final Properties props_;
   private final Prefs prefs_;
   private final StagePositionUpdater stagePosUpdater_;
   
   private final JFormattedTextField rawPath_;
   
   /**
    * 
    * @param devices the (single) instance of the Devices class
    * @param props 
    * @param prefs
    * @param stagePosUpdater
    */
   public SettingsPanel(Devices devices, Properties props, 
         Prefs prefs, StagePositionUpdater stagePosUpdater) {    
      super (MyStrings.PanelNames.SETTINGS.toString(), 
            new MigLayout(
              "", 
              "[right]16[center]16[center]",
              "[]16[]"));
     
      devices_ = devices;
      props_ = props;
      prefs_ = prefs;
      stagePosUpdater_ = stagePosUpdater;
      
      PanelUtils pu = new PanelUtils(prefs_, props_, devices_);

      
      // start GUI panel
      
      final JPanel guiPanel = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      guiPanel.setBorder(PanelUtils.makeTitledBorder("GUI"));
      
      final JCheckBox activeTimerCheckBox = pu.makeCheckBox("Update axis positions continually",
            Properties.Keys.PREFS_ENABLE_POSITION_UPDATES, panelName_, true); 
      ActionListener ae = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) { 
            if (activeTimerCheckBox.isSelected()) {
               stagePosUpdater_.start();
            } else {
               stagePosUpdater_.stop();
            }
         }
      };
      activeTimerCheckBox.addActionListener(ae);
      // programmatically click twice to make sure the action handler is called;
      //   it is not called by setSelected unless there is a change in the value
      activeTimerCheckBox.doClick();
      activeTimerCheckBox.doClick();
      guiPanel.add(activeTimerCheckBox, "center, span 2, wrap");
      
      guiPanel.add(new JLabel("Position refresh interval (s):"));
      final JSpinner positionRefreshInterval = pu.makeSpinnerFloat(0.5, 1000, 0.5,
            Devices.Keys.PLUGIN,
            Properties.Keys.PLUGIN_POSITION_REFRESH_INTERVAL, 1);
      ChangeListener listenerLast = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            stagePosUpdater_.restartIfRunning();
            prefs_.putFloat(panelName_, Properties.Keys.PLUGIN_POSITION_REFRESH_INTERVAL,
                  PanelUtils.getSpinnerFloatValue(positionRefreshInterval));
         }
      };
      pu.addListenerLast(positionRefreshInterval, listenerLast);
      guiPanel.add(positionRefreshInterval, "wrap");
      
      final JCheckBox ignoreScannerMissing = pu.makeCheckBox("Ignore missing scanner (for debug)",
            Properties.Keys.PREFS_IGNORE_MISSING_SCANNER, panelName_, false);
      guiPanel.add(ignoreScannerMissing, "center, span 2, wrap");
      
      // end GUI subpanel
      
      // start scanner panel
      
      final JPanel scannerPanel = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      scannerPanel.setBorder(PanelUtils.makeTitledBorder("Scanner"));

      scannerPanel.add(new JLabel("Filter freq, sheet axis [kHz]:"));
      final JSpinner scannerFilterX = pu.makeSpinnerFloat(0.1, 5, 0.1,
            new Devices.Keys [] {Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SCANNER_FILTER_X, 0.8);
      scannerPanel.add(scannerFilterX, "wrap");
      
      scannerPanel.add(new JLabel("Filter freq, slice axis [kHz]:"));
      final JSpinner scannerFilterY = pu.makeSpinnerFloat(0.1, 5, 0.1,
            new Devices.Keys [] {Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SCANNER_FILTER_Y, 0.4);
      scannerPanel.add(scannerFilterY, "wrap");
      
      final JCheckBox scanOppositeDirectionsCB = pu.makeCheckBox("Scan beam opposite directions each slice",
            Properties.Keys.PREFS_SCAN_OPPOSITE_DIRECTIONS, panelName_, false);
      scannerPanel.add(scanOppositeDirectionsCB, "center, span 2, wrap");
      
      // end scanner panel
      
      
      // start camera panel
      
      final JPanel cameraPanel = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      cameraPanel.setBorder(PanelUtils.makeTitledBorder("Camera"));
      CameraModes camModeObject = new CameraModes(devices_, props_, prefs_);
      JComboBox camModeCB = camModeObject.getComboBox();
      cameraPanel.add(camModeCB);
      
      // end camera panel
      
      
      // start test acquisition panel
      
      final JPanel testAcqPanel = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      testAcqPanel.setBorder(PanelUtils.makeTitledBorder("Test Acquisition"));
      
      final JCheckBox testAcqSave = pu.makeCheckBox("Save test acquisition as raw data",
            Properties.Keys.PLUGIN_TESTACQ_SAVE, panelName_, false);
      testAcqPanel.add(testAcqSave, "span 2, wrap");
      
      DefaultFormatter formatter = new DefaultFormatter();
      rawPath_ = new JFormattedTextField(formatter);
      rawPath_.setText( prefs_.getString(panelName_, 
              Properties.Keys.PLUGIN_TESTACQ_PATH, "") );
      rawPath_.addPropertyChangeListener(new PropertyChangeListener() {
         // will respond to commitEdit() as well as GUI edit on commit
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
            prefs_.putString(panelName_, Properties.Keys.PLUGIN_TESTACQ_PATH,
                  rawPath_.getText());
         }
      });
      rawPath_.setColumns(20);
      testAcqPanel.add(rawPath_);

      JButton browseFileButton = new JButton();
      browseFileButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            File result = FileDialogs.openFile(null,
                  "Please choose a file for raw image data",
                  MMStudio.MM_DATA_SET);
            if (result != null) {
               rawPath_.setText(result.getAbsolutePath());
               try {
                  rawPath_.commitEdit();
               } catch (ParseException ex) {
                  MyDialogUtils.showError("Invalid file selected for test acquisition raw data");
               }
            }
         }
      });
      browseFileButton.setMargin(new Insets(2, 5, 2, 5));
      browseFileButton.setText("...");
      testAcqPanel.add(browseFileButton, "wrap");

      
      // end test acquisition panel
      
      
      // construct main panel
      add(guiPanel);
      add(scannerPanel);
      add(cameraPanel, "wrap");
      add(testAcqPanel);
      

      // start stage scan panel
      // only add this panel if stage scanning is supported
      // TODO create method to determine this instead of separate code here and in AcquisitionPanel
      if (devices_.isTigerDevice(Devices.Keys.XYSTAGE)
            && props_.hasProperty(Devices.Keys.XYSTAGE, Properties.Keys.STAGESCAN_NUMLINES)) {
         final JPanel stageScanPanel = new JPanel(new MigLayout(
               "",
               "[right]16[center]",
               "[]8[]"));
         stageScanPanel.setBorder(PanelUtils.makeTitledBorder("Stage scanning"));
         stageScanPanel.add(new JLabel("Motor acceleration time [ms]:"));
         final JSpinner stageAccelTime = pu.makeSpinnerFloat(10, 1000, 10,
               Devices.Keys.XYSTAGE,
               Properties.Keys.STAGESCAN_MOTOR_ACCEL, 50);
         stageScanPanel.add(stageAccelTime, "wrap");
         add(stageScanPanel, "growx");
       }
      // end stage scan panel
      
      
   }
   
   @Override
   public void saveSettings() {

   }
   
   
}
