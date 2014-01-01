///////////////////////////////////////////////////////////////////////////////
//FILE:          DevicesPanel.java
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

import org.micromanager.utils.ReportingUtils;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Utils.ListeningJPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import mmcorej.CMMCore;
import mmcorej.StrVector;
import net.miginfocom.swing.MigLayout;


/**
 * Draws the Devices tab in the ASI diSPIM GUI
 * @author nico
 * @author Jon
 */
@SuppressWarnings("serial")
public class DevicesPanel extends ListeningJPanel {
   Devices devices_;
   private CMMCore core_;
   
   /**
    * Constructs the GUI Panel that lets the user specify which device to use
    * @param gui - hook to MMstudioMainFrame script interface
    * @param devices - instance of class that holds information about devices
    */
   public DevicesPanel(Devices devices) {
      super(new MigLayout(
              "",
              "[right]25[align center]16[align center]",
              "[]5[]"));
      devices_ = devices;
      core_ = MMStudioMainFrame.getInstance().getCore();

      add(new JLabel(devices_.getDeviceDisplay(Devices.Keys.XYSTAGE) + ":", null, JLabel.RIGHT));
      JComboBox tmp_cb = makeDeviceBox(mmcorej.DeviceType.XYStageDevice, Devices.Keys.XYSTAGE); 
      add(tmp_cb, "span 2, center, wrap");
      
      add(new JLabel(devices_.getDeviceDisplay(Devices.Keys.LOWERZDRIVE) + ":", null, JLabel.RIGHT));
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.StageDevice, Devices.Keys.LOWERZDRIVE);
      add(tmp_cb, "span 2, center, wrap");
      
      add(new JLabel(devices_.getDeviceDisplay(Devices.Keys.UPPERZDRIVE) + ":", null, JLabel.RIGHT));
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.StageDevice, Devices.Keys.UPPERZDRIVE);
      add(tmp_cb, "span 2, center, wrap");
            
      add(new JLabel(devices_.getDeviceDisplay(Devices.Keys.MULTICAMERA) + ":", null, JLabel.RIGHT));
      add(makeDualCameraDeviceBox(Devices.Keys.MULTICAMERA), "span 2, center, wrap");

      add(new JLabel("Imaging Path A"), "skip 1");
      add(new JLabel("Imaging Path B"), "wrap");

      add(new JLabel("Camera:", null, JLabel.RIGHT));
      add(makeDeviceBox(mmcorej.DeviceType.CameraDevice, Devices.Keys.CAMERAA));
      add(makeDeviceBox(mmcorej.DeviceType.CameraDevice, Devices.Keys.CAMERAB), "wrap");

      add(new JLabel(devices_.getDeviceDisplayGeneric(Devices.Keys.PIEZOA) + ":", null, JLabel.RIGHT));
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.StageDevice, Devices.Keys.PIEZOA);
      add(tmp_cb);
      
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.StageDevice, Devices.Keys.PIEZOB);
      add(tmp_cb, "wrap");

      JLabel label = new JLabel(devices_.getDeviceDisplayGeneric(Devices.Keys.GALVOA) + ":", null, JLabel.RIGHT);
      label.setToolTipText("Should be the first two axes on the MicroMirror card, usually AB");
      add (label);
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.GalvoDevice, Devices.Keys.GALVOA);
      add(tmp_cb);
      tmp_cb = makeDeviceBox(mmcorej.DeviceType.GalvoDevice, Devices.Keys.GALVOB);
      add(tmp_cb, "wrap");
      
   }//constructor
   
   // was nested class in makeDeviceBox, needed to move back out for makeDualCameraDeviceBox()
   // TODO clean up
   class DeviceBoxListener implements ActionListener {
      Devices.Keys key_;
      JComboBox box_;

      public DeviceBoxListener(Devices.Keys key, JComboBox box) {
         key_ = key;
         box_ = box;
      }
      
      @Override
      public void actionPerformed(ActionEvent ae) {
         devices_.setMMDevice(key_, (String) box_.getSelectedItem());
      }
   };
   
   /**
    * Constructs a JComboBox populated with devices of specified Micro-Manager type
    * Attaches a listener and sets selected item to what is specified in the Devices
    * class
    * 
    * @param deviceType - Micro-Manager device type (mmcorej.DeviceType)
    * @param deviceName - ASi diSPIM device type (see Devices class)
    * @return final JComboBox
    */
   private JComboBox makeDeviceBox(mmcorej.DeviceType deviceType, Devices.Keys deviceKey) {
      
      // class DeviceBoxListener was here as nested class
      
      StrVector strvDevices =  MMStudioMainFrame.getInstance().getMMCore().getLoadedDevicesOfType(deviceType);
      ArrayList<String> devices = new ArrayList<String>(Arrays.asList(strvDevices.toArray()));
      devices.add(0, "");  // adds initial blank
      JComboBox deviceBox = new JComboBox(devices.toArray());
      deviceBox.setSelectedItem(devices_.getMMDevice(deviceKey));
      deviceBox.addActionListener(new DeviceBoxListener(deviceKey, deviceBox));
      return deviceBox;
   }
   
   // TODO (for Jon) understand what this does
   private JComboBox makeDualCameraDeviceBox(Devices.Keys deviceName) {
      List<String> multiCameras = new ArrayList<String>();
      multiCameras.add("");
      try {
         StrVector strvDevices = core_.getLoadedDevicesOfType(
               mmcorej.DeviceType.CameraDevice);

         String originalCamera = core_.getProperty("Core", "Camera");

         for (int i = 0; i < strvDevices.size(); i++) {
            String test = strvDevices.get(i);
            core_.setProperty("Core", "Camera", test);
            if (core_.getNumberOfCameraChannels() > 1) {
               multiCameras.add(test);
            }
         }
         core_.setProperty("Core", "Camera", originalCamera);

      } catch (Exception ex) {
         ReportingUtils.showError("Error detecting multiCamera devices");
      }

      JComboBox deviceBox = new JComboBox(multiCameras.toArray());
      deviceBox.setSelectedItem(devices_.getMMDevice(deviceName));
      deviceBox.addActionListener(new DevicesPanel.DeviceBoxListener(deviceName, deviceBox));
      return deviceBox;
   }

   
   // below is code for features that have been removed, specifically
   // changing which axis generates the sheet (now always X) and
   // changing the sign of X axis (maybe will add back later)
   
//   /**
//    * Listener for the Axis directions combox boxes
//    * Updates the model in the Devices class with any GUI changes
//    */
//   class AxisDirBoxListener implements ActionListener {
//      String axis_;
//      JComboBox box_;
//
//      public AxisDirBoxListener(String axis, JComboBox box) {
//         axis_ = axis;
//         box_ = box;
//      }
//
//      @Override
//      public void actionPerformed(ActionEvent ae) {
//         devices_.putAxisDirInfo(axis_, (String) box_.getSelectedItem());
//      }
//   }
//   
//   /**
//    * Constructs a DropDown box containing X/Y.
//    * Sets selection based on info in the Devices class and attaches
//    * a Listener
//    * 
//    * @param axis - Name under which this axis is known in the Device class
//    * @return constructed JComboBox
//    */
//   private JComboBox makeXYBox(String axis) {
//      String[] xy = {"X", "Y"};
//      JComboBox jcb = new JComboBox(xy);
//      jcb.setSelectedItem(devices_.getAxisDirInfo(axis));
//      jcb.addActionListener(new DevicesPanel.AxisDirBoxListener(axis, jcb));
// 
//      return jcb;
//   }
//   
//   /**
//    * Listener for the Checkbox indicating whether this axis should be reversed
//    * Updates the Devices model with GUI selections
//    */
//   class ReverseCheckBoxListener implements ActionListener {
//      String axis_;
//      JCheckBox box_;
//
//      public ReverseCheckBoxListener(String axis, JCheckBox box) {
//         axis_ = axis;
//         box_ = box;
//      }
//
//      @Override
//      public void actionPerformed(ActionEvent ae) {
//         devices_.putFastAxisRevInfo(axis_, box_.isSelected());
//      }
//   };
//   
//   /**
//    * Constructs the JCheckBox through which the user can set the direction of
//    * the sheet
//    * @param fastAxisDir name under which this axis is known in the Devices class
//    * @return constructed JCheckBox
//    */
//   private JCheckBox makeReverseCheckBox(String fastAxisDir) {
//      JCheckBox jc = new JCheckBox("Reverse");
//      jc.setSelected(devices_.getFastAxisRevInfo(fastAxisDir));
//      jc.addActionListener(new DevicesPanel.ReverseCheckBoxListener(fastAxisDir, jc));
//      
//      return jc;
//   }

   
}
