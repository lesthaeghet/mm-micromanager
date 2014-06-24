package org.micromanager.positionlist;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.micromanager.utils.MMDialog;
import org.micromanager.utils.ReportingUtils;

/**
 * This class provides a dialog that allows the user to apply an offset to the
 * selected stage positions. Ultimately it calls
 * PositionListDlg.offsetSelectedSites to adjust positions.
 */
class OffsetPositionsDialog extends MMDialog {
   
   private PositionListDlg parent_;
   private CMMCore core_;
   // This panel holds dynamically-generated inputs appropriate to the 
   // device we want to set offsets for.
   private JPanel axisPanel_;
   // Inputs used to set the offsets.
   private Vector<JTextField> axisInputs_;
   // Name of the device we're setting offsets for.
   private String deviceName_;

   public OffsetPositionsDialog(PositionListDlg parent, CMMCore core) {
      super();
      parent_ = parent;
      core_ = core;
      axisInputs_ = new Vector<JTextField>();
      
      setSize(new Dimension(320, 300));
      setTitle("Add offset to stage positions");
      setResizable(false);
      setModal(true);
      setLayout(new MigLayout("flowy"));

      JLabel label = new JLabel("<html><p>This dialog allows you to add offsets to the currently-selected stage positions.</p></html>");
      label.setPreferredSize(new Dimension(300, 20));
      add(label, "align center");

      // Add a menu to select a positioner, and then a panel which will
      // dynamically get the text fields for the positioner as appropriate.
      Vector<String> options = new Vector<String>();
      StrVector xyStages = core.getLoadedDevicesOfType(DeviceType.XYStageDevice);
      for (int i = 0; i < xyStages.size(); ++i) {
         options.add(xyStages.get(i));
      }
      StrVector stages = core.getLoadedDevicesOfType(DeviceType.StageDevice);
      for (int i = 0; i < stages.size(); ++i) {
         options.add(stages.get(i));
      }
      deviceName_ = options.get(0);

      final JComboBox stageOptions = new JComboBox(options);
      stageOptions.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent event) {
            String stageName = (String) stageOptions.getSelectedItem();
            setControlsFor(stageName);
         }
      });
      add(new JLabel("Select stage device: "), "split 2, flowx");
      add(stageOptions);

      axisPanel_ = new JPanel(new MigLayout("flowy"));
      add(axisPanel_);
      
      JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent event) {
            Vector<Float> offsets = new Vector<Float>();
            for (JTextField input : axisInputs_) {
               try {
                  Float value = Float.parseFloat(input.getText());
                  offsets.add(value);
               }
               catch (NumberFormatException e) {
                  // Invalid format; just use 0.
                  offsets.add(new Float(0));
               }
            }
            parent_.offsetSelectedSites(deviceName_, offsets);
            dispose();
         }
      });
      add(okButton, "split 2, flowx");

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         @Override
            public void actionPerformed(ActionEvent event) {
               dispose();
            }
      });
      add(cancelButton);

      setControlsFor(deviceName_);
      setVisible(true);
   }

   /**
    * Given a device name for either a StageDevice or XYStageDevice, display
    * a set of one or two text fields to let them set either the Z or XY
    * offsets, respectively.
    */
   private void setControlsFor(String deviceName) {
      // Remove existing controls.
      axisPanel_.removeAll();
      axisInputs_.clear();
      deviceName_ = deviceName;

      // Determine the axis labels.
      Vector<String> axes = new Vector<String>();
      DeviceType type;
      try {
         type = core_.getDeviceType(deviceName);
      }
      catch (Exception e) {
         // Something went wrong; give up.
         ReportingUtils.logError("Couldn't determine type of device named " + deviceName);
         return;
      }
      if (type == DeviceType.StageDevice) {
         // Dealing with a Z device; only one label.
         axes.add("Z offset:");
      }
      else if (type == DeviceType.XYStageDevice) {
         // Two axes.
         axes.add("X offset:");
         axes.add("Y offset:");
      }
      // Generate a control for each axis.
      for (String axis : axes) {
         JLabel prompt = new JLabel(axis);
         axisPanel_.add(prompt, "split 2, flowx");
         JTextField field = new JTextField(4);
         field.setText("0");
         axisPanel_.add(field);
         axisInputs_.add(field);
      }
      pack();
      invalidate();
   }
}
