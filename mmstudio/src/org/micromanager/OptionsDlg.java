///////////////////////////////////////////////////////////////////////////////
//FILE:          OptionsDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, September 12, 2006
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
package org.micromanager;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import mmcorej.CMMCore;

import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.GUIColors;
import org.micromanager.utils.MMDialog;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;

/**
 * Options dialog for MMStudio.
 *
 */
public class OptionsDlg extends MMDialog {

   private JTextField startupScriptFile_;
   private static final long serialVersionUID = 1L;
   private JTextField bufSizeField_;
   private MMOptions opts_;
   private CMMCore core_;
   private SpringLayout springLayout;
   private Preferences mainPrefs_;
   private JComboBox comboDisplayBackground_;
   private ScriptInterface parent_;
   private GUIColors guiColors_;
   private String currentCfgPath_;
   private JComboBox windowZoomCombo_;

   /**
    * Create the dialog
    */
   public OptionsDlg(MMOptions opts, CMMCore core, Preferences mainPrefs, ScriptInterface parent, String cfgPath) {
      super();
      currentCfgPath_ = cfgPath;
      parent_ = parent;
      addWindowListener(new WindowAdapter() {

         @Override
         public void windowClosing(final WindowEvent e) {
            savePosition();
            parent_.makeActive();
         }
      });
      setResizable(false);
      setModal(true);
      opts_ = opts;
      core_ = core;
      mainPrefs_ = mainPrefs;
      setTitle("Micro-Manager Options");
      springLayout = new SpringLayout();
      getContentPane().setLayout(springLayout);
      setBounds(100, 100, 380, 430);
      guiColors_ = new GUIColors();
      Dimension buttonSize = new Dimension(120, 20);

      if (opts_.displayBackground_.equals("Day")) {
         setBackground(java.awt.SystemColor.control);
      } else if (opts_.displayBackground_.equals("Night")) {
         setBackground(java.awt.Color.gray);
      }
      Preferences root = Preferences.userNodeForPackage(this.getClass());
      setPrefsNode(root.node(root.absolutePath() + "/OptionsDlg"));

      Rectangle r = getBounds();
      loadPosition(r.x, r.y);

      final JCheckBox debugLogEnabledCheckBox = new JCheckBox();
      debugLogEnabledCheckBox.setToolTipText("Set extra verbose logging for debugging purposes");
      debugLogEnabledCheckBox.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            opts_.debugLogEnabled_ = debugLogEnabledCheckBox.isSelected();
            core_.enableDebugLog(opts_.debugLogEnabled_);
         }
      });
      debugLogEnabledCheckBox.setText("Debug log enabled");
      getContentPane().add(debugLogEnabledCheckBox);
      springLayout.putConstraint(SpringLayout.SOUTH, debugLogEnabledCheckBox, 30, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, debugLogEnabledCheckBox, 7, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, debugLogEnabledCheckBox, 190, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, debugLogEnabledCheckBox, 10, SpringLayout.WEST, getContentPane());
      debugLogEnabledCheckBox.setSelected(opts_.debugLogEnabled_);

      final JCheckBox doNotAskForConfigFileCheckBox = new JCheckBox();
      doNotAskForConfigFileCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.doNotAskForConfigFile_ = doNotAskForConfigFileCheckBox.isSelected();
         }
      });
      doNotAskForConfigFileCheckBox.setText("Do not ask for config file");
      getContentPane().add(doNotAskForConfigFileCheckBox);
      springLayout.putConstraint(SpringLayout.EAST, doNotAskForConfigFileCheckBox, 220, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, doNotAskForConfigFileCheckBox, 0, SpringLayout.WEST, debugLogEnabledCheckBox);
      springLayout.putConstraint(SpringLayout.SOUTH, doNotAskForConfigFileCheckBox, 50, SpringLayout.NORTH, getContentPane());
      doNotAskForConfigFileCheckBox.setSelected(opts_.doNotAskForConfigFile_);

      final JButton clearLogFileButton = new JButton();
      clearLogFileButton.setMargin(new Insets(0, 0, 0, 0));
      clearLogFileButton.setToolTipText("Erases all entries in the current log file (recommended)");
      clearLogFileButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            core_.clearLog();
            parent_.logStartupProperties();
         }
      });
      clearLogFileButton.setFont(new Font("", Font.PLAIN, 10));
      clearLogFileButton.setText("Clear log file");
      clearLogFileButton.setPreferredSize(buttonSize);
      getContentPane().add(clearLogFileButton);
      //springLayout.putConstraint(SpringLayout.SOUTH, clearLogFileButton, 166, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, clearLogFileButton, 175, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, clearLogFileButton, 20, SpringLayout.NORTH, getContentPane());

      final JButton clearRegistryButton = new JButton();
      clearRegistryButton.setToolTipText("Clears all persistent settings and returns to defaults");
      clearRegistryButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            try {
               boolean previouslyRegistered = mainPrefs_.getBoolean(RegistrationDlg.REGISTRATION, false);
               mainPrefs_.clear();
               Preferences acqPrefs = mainPrefs_.node(mainPrefs_.absolutePath() + "/" + AcqControlDlg.ACQ_SETTINGS_NODE);
               acqPrefs.clear();

               // restore registration flag
               mainPrefs_.putBoolean(RegistrationDlg.REGISTRATION, previouslyRegistered);

            } catch (BackingStoreException exc) {
               ReportingUtils.showError(e);
            }
         }
      });
      clearRegistryButton.setText("Clear registry");
      clearRegistryButton.setFont(new Font("", Font.PLAIN, 10));
      clearRegistryButton.setPreferredSize(buttonSize);
      getContentPane().add(clearRegistryButton);
      springLayout.putConstraint(SpringLayout.EAST, clearRegistryButton, 0, SpringLayout.EAST, clearLogFileButton);
      springLayout.putConstraint(SpringLayout.WEST, clearRegistryButton, 0, SpringLayout.WEST, clearLogFileButton);
      springLayout.putConstraint(SpringLayout.NORTH, clearRegistryButton, 5, SpringLayout.SOUTH, clearLogFileButton);
      //springLayout.putConstraint(SpringLayout.EAST, clearRegistryButton, 80, SpringLayout.WEST, getContentPane());

      final JButton okButton = new JButton();
      okButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            try {
               opts_.circularBufferSizeMB_ = NumberUtils.displayStringToInt(bufSizeField_.getText());
            } catch (Exception e1) {
               ReportingUtils.showError(e1);
               return;
            }
            opts_.startupScript_ = startupScriptFile_.getText();
            savePosition();
            parent_.makeActive();
            dispose();
         }
      });
      okButton.setText("Close");
      okButton.setFont(new Font("", Font.PLAIN, 10));
      okButton.setPreferredSize(buttonSize);
      getContentPane().add(okButton);
      springLayout.putConstraint(SpringLayout.NORTH, okButton, 12, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, okButton, 35, SpringLayout.NORTH, getContentPane());



      final JLabel sequenceBufferSizeLabel = new JLabel();
      sequenceBufferSizeLabel.setText("Sequence buffer size [MB]");
      getContentPane().add(sequenceBufferSizeLabel);
      springLayout.putConstraint(SpringLayout.EAST, sequenceBufferSizeLabel, 180, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, sequenceBufferSizeLabel, 15, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, sequenceBufferSizeLabel, 84, SpringLayout.NORTH, getContentPane());
      //springLayout.putConstraint(SpringLayout.NORTH, sequenceBufferSizeLabel, 95, SpringLayout.NORTH, getContentPane());

      bufSizeField_ = new JTextField(Integer.toString(opts_.circularBufferSizeMB_));
      getContentPane().add(bufSizeField_);
      springLayout.putConstraint(SpringLayout.SOUTH, bufSizeField_, 85, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, bufSizeField_, 65, SpringLayout.NORTH, getContentPane());

      final JLabel displayLabel = new JLabel();
      //displayLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      displayLabel.setText("Display-Background");
      getContentPane().add(displayLabel);
      springLayout.putConstraint(SpringLayout.EAST, displayLabel, 170, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, displayLabel, 15, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, displayLabel, 108, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, displayLabel, 92, SpringLayout.NORTH, getContentPane());

      comboDisplayBackground_ = new JComboBox(guiColors_.styleOptions);
      comboDisplayBackground_.setFont(new Font("Arial", Font.PLAIN, 10));
      comboDisplayBackground_.setMaximumRowCount(2);
      comboDisplayBackground_.setSelectedItem(opts_.displayBackground_);
      comboDisplayBackground_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            changeBackground();
         }
      });
      getContentPane().add(comboDisplayBackground_);
      springLayout.putConstraint(SpringLayout.EAST, bufSizeField_, 0, SpringLayout.EAST, comboDisplayBackground_);
      springLayout.putConstraint(SpringLayout.WEST, bufSizeField_, 220, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, comboDisplayBackground_, 331, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, comboDisplayBackground_, 220, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, comboDisplayBackground_, 114, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, comboDisplayBackground_, 91, SpringLayout.NORTH, getContentPane());

      final JLabel startupScriptLabel = new JLabel();
      startupScriptLabel.setText("Startup script");
      getContentPane().add(startupScriptLabel);
      springLayout.putConstraint(SpringLayout.EAST, startupScriptLabel, 115, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, startupScriptLabel, 0, SpringLayout.WEST, displayLabel);
      springLayout.putConstraint(SpringLayout.SOUTH, startupScriptLabel, 135, SpringLayout.NORTH, getContentPane());

      startupScriptFile_ = new JTextField(opts_.startupScript_);
      getContentPane().add(startupScriptFile_);
      springLayout.putConstraint(SpringLayout.EAST, okButton, 0, SpringLayout.EAST, startupScriptFile_);
      springLayout.putConstraint(SpringLayout.WEST, okButton, 250, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, startupScriptFile_, 131, SpringLayout.WEST, comboDisplayBackground_);
      springLayout.putConstraint(SpringLayout.WEST, startupScriptFile_, 140, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, startupScriptFile_, 137, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, startupScriptFile_, 5, SpringLayout.SOUTH, comboDisplayBackground_);


      final JCheckBox autoreloadDevicesCheckBox = new JCheckBox();
      autoreloadDevicesCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.autoreloadDevices_ = autoreloadDevicesCheckBox.isSelected();
         }
      });
      autoreloadDevicesCheckBox.setText("Auto-reload devices (Danger!)");
      getContentPane().add(autoreloadDevicesCheckBox);
      springLayout.putConstraint(SpringLayout.NORTH, autoreloadDevicesCheckBox, 0, SpringLayout.NORTH, clearLogFileButton);
      springLayout.putConstraint(SpringLayout.WEST, autoreloadDevicesCheckBox, 5, SpringLayout.EAST, clearLogFileButton);
      autoreloadDevicesCheckBox.setSelected(opts_.autoreloadDevices_);

      final JCheckBox closeOnExitCheckBox = new JCheckBox();
      closeOnExitCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.closeOnExit_ = closeOnExitCheckBox.isSelected();
            MMStudioMainFrame.getInstance().setExitStrategy(opts_.closeOnExit_);
         }
      });
      closeOnExitCheckBox.setText("Close app when quitting MM");
      getContentPane().add(closeOnExitCheckBox);
      //springLayout.putConstraint(SpringLayout.EAST, closeOnExitCheckBox, 220, SpringLayout.WEST, getContentPane());
      //springLayout.putConstraint(SpringLayout.WEST, closeOnExitCheckBox, 0, SpringLayout.WEST, debugLogEnabledCheckBox);
      //springLayout.putConstraint(SpringLayout.SOUTH, closeOnExitCheckBox, 60, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, closeOnExitCheckBox, 20, SpringLayout.NORTH, autoreloadDevicesCheckBox);
      springLayout.putConstraint(SpringLayout.WEST, closeOnExitCheckBox, 0, SpringLayout.WEST, autoreloadDevicesCheckBox);
      closeOnExitCheckBox.setSelected(opts_.closeOnExit_);


      final JComboBox prefZoomCombo = new JComboBox();
      prefZoomCombo.setModel(new DefaultComboBoxModel(new String[]{
         "8%", "12%", "16%",  "25%",  "33%", "50%", "75%", "100%", "150%","200%","300%","400%","600%"
      }));
    
      double mag = opts_.windowMag_;
      int index = 0;
      if (mag == 0.25 / 3.0) {
         index = 0;
      } else if (mag == 0.125) {
         index = 1;
      } else if (mag == 0.16) {
         index = 2;
      } else if (mag == 0.25) {
         index = 3;
      } else if (mag == 0.33) {
         index = 4;
      } else if (mag == 0.5) {
         index = 5;
      } else if (mag == 0.75) {
         index = 6;
      } else if (mag == 1.0) {
         index = 7;
      } else if (mag == 1.5) {
         index = 8;
      } else if (mag == 2.0) {
         index = 9;
      } else if (mag == 3.0) {
         index = 10;
      } else if (mag == 4.0) {
         index = 11;
      } else if (mag == 6.0) {
         index = 12;
      }
      prefZoomCombo.setSelectedIndex(index);
      
      prefZoomCombo.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            switch (prefZoomCombo.getSelectedIndex()) {
               case (0):
                  opts_.windowMag_ = 0.25 / 3.0;
                  break;
               case (1):
                  opts_.windowMag_ = 0.125;
                  break;
               case (2):
                  opts_.windowMag_ = 0.16;
                  break;
               case (3):
                  opts_.windowMag_ = 0.25;
                  break;
               case (4):
                  opts_.windowMag_ = 0.33;
                  break;
               case (5):
                  opts_.windowMag_ = 0.5;
                  break;
               case (6):
                  opts_.windowMag_ = 0.75;
                  break;
               case (7):
                  opts_.windowMag_ = 1.0;
                  break;
               case (8):
                  opts_.windowMag_ = 1.5;
                  break;
              case (9):
                  opts_.windowMag_ = 2.0;
                  break;
               case (10):
                  opts_.windowMag_ = 3.0;
                  break;
               case (11):
                  opts_.windowMag_ = 4.0;
                  break;
               case (12):
                  opts_.windowMag_ = 6.0;
                  break;
            }
            }
         }
      );
      
      getContentPane().add(prefZoomCombo);
      springLayout.putConstraint(SpringLayout.NORTH, prefZoomCombo, 30, SpringLayout.NORTH, closeOnExitCheckBox);
      springLayout.putConstraint(SpringLayout.WEST, prefZoomCombo, 90, SpringLayout.WEST, autoreloadDevicesCheckBox);
      
      JLabel prefZoomLabel = new JLabel("Preferred image window zoom:");
      getContentPane().add(prefZoomLabel);
      
      springLayout.putConstraint(SpringLayout.WEST, prefZoomLabel, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, prefZoomLabel, 5, SpringLayout.NORTH, prefZoomCombo);
      
      
      final JCheckBox metadataFileWithMultipageTiffCheckBox = new JCheckBox();
      metadataFileWithMultipageTiffCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.mpTiffMetadataFile_ = metadataFileWithMultipageTiffCheckBox.isSelected();
         }
      });
      metadataFileWithMultipageTiffCheckBox.setText("Create metadata txt file with image stack file saving");
      getContentPane().add(metadataFileWithMultipageTiffCheckBox);
      springLayout.putConstraint(SpringLayout.WEST, metadataFileWithMultipageTiffCheckBox, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, metadataFileWithMultipageTiffCheckBox, 10, SpringLayout.SOUTH, prefZoomLabel);
      metadataFileWithMultipageTiffCheckBox.setSelected(opts_.mpTiffMetadataFile_);
      
      final JCheckBox seperateFilesForPositionsMPTiffCheckBox = new JCheckBox();
      seperateFilesForPositionsMPTiffCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.mpTiffSeperateFilesForPositions_ = seperateFilesForPositionsMPTiffCheckBox.isSelected();
         }
      });
      seperateFilesForPositionsMPTiffCheckBox.setText("Save XY Positions in seperate image stack files");
      getContentPane().add(seperateFilesForPositionsMPTiffCheckBox);
      springLayout.putConstraint(SpringLayout.WEST, seperateFilesForPositionsMPTiffCheckBox, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, seperateFilesForPositionsMPTiffCheckBox, 5, SpringLayout.SOUTH, metadataFileWithMultipageTiffCheckBox);
      seperateFilesForPositionsMPTiffCheckBox.setSelected(opts_.mpTiffSeperateFilesForPositions_);
  
      final JCheckBox syncExposureMainAndMDA = new JCheckBox();
      syncExposureMainAndMDA.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.syncExposureMainAndMDA_ = syncExposureMainAndMDA.isSelected();
         }
      });
      syncExposureMainAndMDA.setText("Sync exposure between Main and MDA windows");
      getContentPane().add(syncExposureMainAndMDA);
      springLayout.putConstraint(SpringLayout.WEST, syncExposureMainAndMDA, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, syncExposureMainAndMDA, 5, SpringLayout.SOUTH, seperateFilesForPositionsMPTiffCheckBox);
      syncExposureMainAndMDA.setSelected(opts_.syncExposureMainAndMDA_);
  
      
      final JCheckBox hideMDAdisplay = new JCheckBox();
      hideMDAdisplay.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.hideMDADisplay_ = hideMDAdisplay.isSelected();
         }
      });
      hideMDAdisplay.setText("Hide MDA display");
      getContentPane().add(hideMDAdisplay);
      springLayout.putConstraint(SpringLayout.WEST, hideMDAdisplay, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, hideMDAdisplay, 5, SpringLayout.SOUTH, syncExposureMainAndMDA);
      hideMDAdisplay.setSelected(opts_.hideMDADisplay_);
      
      final JCheckBox fastStorage = new JCheckBox();
      fastStorage.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.fastStorage_ = fastStorage.isSelected();
         }
      });
      fastStorage.setText("Fast storage");
      getContentPane().add(fastStorage);
      springLayout.putConstraint(SpringLayout.WEST, fastStorage, 20, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, fastStorage, 5, SpringLayout.SOUTH, hideMDAdisplay);
      fastStorage.setSelected(opts_.fastStorage_);
   }

   private void changeBackground() {
      String background = (String) comboDisplayBackground_.getSelectedItem();
      opts_.displayBackground_ = background;
      setBackground(guiColors_.background.get(background));

      if (parent_ != null) // test for null just to avoid crashes (should never be null)
      {
         // set background and trigger redraw of parent and its descendant windows
         MMStudioMainFrame.getInstance().setBackgroundStyle(background);
      }
   }
}
