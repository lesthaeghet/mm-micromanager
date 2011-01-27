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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import mmcorej.CMMCore;

import org.micromanager.api.DeviceControlGUI;
import org.micromanager.utils.GUIColors;
import org.micromanager.utils.HttpUtils;
import org.micromanager.utils.MMDialog;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;
import sun.misc.UUEncoder;

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
   private DeviceControlGUI parent_;
   private GUIColors guiColors_;

   /**
    * Create the dialog
    */
   public OptionsDlg(MMOptions opts, CMMCore core, Preferences mainPrefs, DeviceControlGUI parent) {
      super();
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
      setBounds(100, 100, 371, 287);
      guiColors_ = new GUIColors();
      Dimension buttonSize = new Dimension(120, 20);

      if (opts_.displayBackground.equals("Day"))
         setBackground(java.awt.SystemColor.control);
      else if (opts_.displayBackground.equals("Night"))
         setBackground(java.awt.Color.gray);
      Preferences root = Preferences.userNodeForPackage(this.getClass());
      setPrefsNode(root.node(root.absolutePath() + "/OptionsDlg"));
      
      Rectangle r = getBounds();
      loadPosition(r.x, r.y);

      final JCheckBox debugLogEnabledCheckBox = new JCheckBox();
      debugLogEnabledCheckBox.setToolTipText("Set extra verbose logging for debugging purposes");
      debugLogEnabledCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            opts_.debugLogEnabled = debugLogEnabledCheckBox.isSelected();
            core_.enableDebugLog(opts_.debugLogEnabled);
         }
      });
      debugLogEnabledCheckBox.setText("Debug log enabled");
      getContentPane().add(debugLogEnabledCheckBox);
      springLayout.putConstraint(SpringLayout.SOUTH, debugLogEnabledCheckBox, 35, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, debugLogEnabledCheckBox, 12, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, debugLogEnabledCheckBox, 190, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, debugLogEnabledCheckBox, 10, SpringLayout.WEST, getContentPane());

      final JButton clearLogFileButton = new JButton();
      clearLogFileButton.setMargin(new Insets(0,0,0,0));
      clearLogFileButton.setToolTipText("Erases all entries in the current log file (recommended)");
      clearLogFileButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            core_.clearLog();
            core_.logMessage("MM Studio version: " + parent_.getVersion());
            core_.logMessage (core_.getVersionInfo());
            core_.logMessage (core_.getAPIVersionInfo());
            core_.logMessage ("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") );
         }
      });
      clearLogFileButton.setFont(new Font("", Font.PLAIN, 10));
      clearLogFileButton.setText("Clear log file");
      clearLogFileButton.setPreferredSize(buttonSize);
      getContentPane().add(clearLogFileButton);
      //springLayout.putConstraint(SpringLayout.SOUTH, clearLogFileButton, 166, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, clearLogFileButton, 175, SpringLayout.NORTH, getContentPane());

      final JButton sendLogFileButton = new JButton();
      sendLogFileButton.setMargin(new Insets(0,0,0,0));
      sendLogFileButton.setToolTipText("Send a compressed archive of your log file to Micro-manager.org");
      sendLogFileButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
             String archPath = core_.saveLogArchive();
             
            try{
                HttpUtils httpu = new HttpUtils();
                List<File> list = new ArrayList<File>();
                File archiveFile = new File(archPath);

                // contruct a filename for the archive which is extremely
                // likely to be unique as follows:
                // yyyyMMddHHmm + timezone + ip address + host name + mm user + file name
                String qualifiedArchiveFileName = "";
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                    qualifiedArchiveFileName += df.format(new Date());
                    String shortTZName = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
                    qualifiedArchiveFileName += shortTZName;
                    qualifiedArchiveFileName += "_";
                    try {

                        qualifiedArchiveFileName += InetAddress.getLocalHost().getHostAddress();
                        //qualifiedArchiveFileName += InetAddress.getLocalHost().getHostName();
                        //qualifiedArchiveFileName += "_";

                    } catch (UnknownHostException e2) {
                    }
                   // qualifiedArchiveFileName += core_.getUserId();
                    //qualifiedArchiveFileName += "_";
                } catch (Throwable t) {
                }

                // get the file name part of the path
                //qualifiedArchiveFileName += archiveFile.getName();
                // try ensure valid and convenient UNIX file name
                qualifiedArchiveFileName.replace(' ', '_');
                qualifiedArchiveFileName.replace('*', '_');
                qualifiedArchiveFileName.replace('|', '_');
                qualifiedArchiveFileName.replace('>', '_');
                qualifiedArchiveFileName.replace('<', '_');
                qualifiedArchiveFileName.replace('(', '_');
                qualifiedArchiveFileName.replace(')', '_');
                qualifiedArchiveFileName.replace(':', '_');
                qualifiedArchiveFileName.replace(';', '_');                //File fileToSend = new File(qualifiedArchiveFileName);
                qualifiedArchiveFileName += ".log";

                //FileReader reader = new FileReader(archiveFile);
                //FileWriter writer = new FileWriter(fileToSend);

                UUEncoder uuec = new UUEncoder();
                InputStream reader = new FileInputStream(archiveFile);
                OutputStream writer = new FileOutputStream(qualifiedArchiveFileName);
                uuec.encodeBuffer(reader, writer);

                reader.close();
                writer.close();
                File fileToSend = new File(qualifiedArchiveFileName);
                try {

                    URL url = new URL("http://valelab.ucsf.edu/~MM/upload_corelog.php");

                    List flist = new ArrayList<File>();
                    flist.add(fileToSend);
                    // for each of a colleciton of files to send...
                    for (Object o0 : flist) {
                        File f0 = (File)o0;
                        try {
                            httpu.upload(url, f0);
                        } catch (java.net.UnknownHostException e2) {
                            ReportingUtils.logError(e2, " log archive upload");

                        } catch (IOException e2) {
                            ReportingUtils.logError(e2);
                        } catch (SecurityException e2) {
                            ReportingUtils.logError(e2, "");
                        } catch (Exception e2) {
                            ReportingUtils.logError(e2);
                        }
                    }
                } catch (MalformedURLException e2) {
                    ReportingUtils.logError(e2);
                }
            } catch (IOException e2) {
               ReportingUtils.showError(e2);
          }
       
             
             
         }
      });
      sendLogFileButton.setFont(new Font("", Font.PLAIN, 10));
      sendLogFileButton.setText("Send core log to MM.org");
      sendLogFileButton.setPreferredSize(buttonSize);
      getContentPane().add(sendLogFileButton);
      // put send log file button to the right of clear log file button
      springLayout.putConstraint(SpringLayout.NORTH, sendLogFileButton, 0, SpringLayout.NORTH, clearLogFileButton);
      springLayout.putConstraint(SpringLayout.WEST, sendLogFileButton, 20, SpringLayout.EAST, clearLogFileButton);

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
      springLayout.putConstraint(SpringLayout.EAST, clearLogFileButton, 0, SpringLayout.EAST, clearRegistryButton);
      springLayout.putConstraint(SpringLayout.WEST, clearLogFileButton, 0, SpringLayout.WEST, clearRegistryButton);
      springLayout.putConstraint(SpringLayout.NORTH, clearRegistryButton, 210, SpringLayout.NORTH, getContentPane());
      //springLayout.putConstraint(SpringLayout.EAST, clearRegistryButton, 80, SpringLayout.WEST, getContentPane());

      final JButton okButton = new JButton();
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            try {
            opts_.circularBufferSizeMB = NumberUtils.displayStringToInt(bufSizeField_.getText());
         } catch (Exception e1) {
            ReportingUtils.showError(e1);
            return;
         }
            opts_.startupScript = startupScriptFile_.getText();
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
      
      debugLogEnabledCheckBox.setSelected(opts_.debugLogEnabled);

      final JCheckBox doNotAskForConfigFileCheckBox = new JCheckBox();
      doNotAskForConfigFileCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            opts_.doNotAskForConfigFile = doNotAskForConfigFileCheckBox.isSelected();
         }
      });
      doNotAskForConfigFileCheckBox.setText("Do not ask for config file");
      getContentPane().add(doNotAskForConfigFileCheckBox);
      springLayout.putConstraint(SpringLayout.EAST, doNotAskForConfigFileCheckBox, 220, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, doNotAskForConfigFileCheckBox, 0, SpringLayout.WEST, debugLogEnabledCheckBox);
      springLayout.putConstraint(SpringLayout.SOUTH, doNotAskForConfigFileCheckBox, 60, SpringLayout.NORTH, getContentPane());
      doNotAskForConfigFileCheckBox.setSelected(opts_.doNotAskForConfigFile);

      final JLabel sequenceBufferSizeLabel = new JLabel();
      sequenceBufferSizeLabel.setText("Sequence buffer size [MB]");
      getContentPane().add(sequenceBufferSizeLabel);
      springLayout.putConstraint(SpringLayout.EAST, sequenceBufferSizeLabel, 180, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, sequenceBufferSizeLabel, 15, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, sequenceBufferSizeLabel, 84, SpringLayout.NORTH, getContentPane());
      //springLayout.putConstraint(SpringLayout.NORTH, sequenceBufferSizeLabel, 95, SpringLayout.NORTH, getContentPane());

      bufSizeField_ = new JTextField(Integer.toString(opts_.circularBufferSizeMB));
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
      comboDisplayBackground_.setSelectedItem(opts_.displayBackground);
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
      springLayout.putConstraint(SpringLayout.WEST, clearRegistryButton, 5, SpringLayout.WEST, startupScriptLabel);
      springLayout.putConstraint(SpringLayout.EAST, startupScriptLabel, 115, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, startupScriptLabel, 0, SpringLayout.WEST, displayLabel);
      springLayout.putConstraint(SpringLayout.SOUTH, startupScriptLabel, 135, SpringLayout.NORTH, getContentPane());

      startupScriptFile_ = new JTextField(opts_.startupScript);
      getContentPane().add(startupScriptFile_);
      springLayout.putConstraint(SpringLayout.EAST, okButton, 0, SpringLayout.EAST, startupScriptFile_);
      springLayout.putConstraint(SpringLayout.WEST, okButton, 250, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, startupScriptFile_, 131, SpringLayout.WEST, comboDisplayBackground_);
      springLayout.putConstraint(SpringLayout.WEST, startupScriptFile_, 140, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, startupScriptFile_, 137, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, startupScriptFile_, 5, SpringLayout.SOUTH, comboDisplayBackground_);
   }

   private void changeBackground() {
       String background = (String)comboDisplayBackground_.getSelectedItem();
       opts_.displayBackground = background;
       setBackground(guiColors_.background.get(background));

       if (parent_ != null) // test for null just to avoid crashes (should never be null)
       {
          // set background and trigger redraw of parent and its descendant windows
          parent_.setBackgroundStyle(background);
       }
   }

}
