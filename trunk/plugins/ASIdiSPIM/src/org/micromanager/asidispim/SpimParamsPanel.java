///////////////////////////////////////////////////////////////////////////////
//FILE:          SpimParamsPanel.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author nico
 */
public class SpimParamsPanel extends JPanel {

   SpimParams params_;
   Devices devices_;

   public SpimParamsPanel(SpimParams params, Devices devices) {
      super(new MigLayout(
              "",
              "[right]8[center]8[center]",
              "[]8[]"));
      params_ = params;
      devices_ = devices;

      add(new JLabel("Number of sides:"), "split 2");
      add(makeSpinner(SpimParams.NSIDES, 1, 2));
      
      add(new JLabel("First side:"), "align right");
      add(makeABBox(SpimParams.FIRSTSIDE), "wrap");
      
      add(new JLabel("Side A"), "cell 1 2");
      add(new JLabel("Side B"), "wrap");
      
      add(new JLabel("Number of Sheets:"));
      add(makeSpinner(SpimParams.NSHEETSA, 1, 99));
      add(makeSpinner(SpimParams.NSHEETSB, 1, 99), "wrap");
      
      add(new JLabel("Lines scans per Sheet:"));
      add(makeSpinner(SpimParams.NLINESCANSPERSHEETA, 1, 20));
      add(makeSpinner(SpimParams.NLINESCANSPERSHEETB, 1, 20), "wrap");
      
      add(new JLabel("Line scan period (ms):"));
      add(makeSpinner(SpimParams.LINESCANPERIODA, 1, 10000));
      add(makeSpinner(SpimParams.LINESCANPERIODB, 1, 10000), "wrap");
      
      add(new JLabel("Delay before each sheet (ms):"));
      add(makeSpinner(SpimParams.DELAYBEFORESHEETA, 0, 10000));
      add(makeSpinner(SpimParams.DELAYBEFORESHEETB, 0, 10000), "wrap");
      
      add(new JLabel("Delay before each side (ms):"));
      add(makeSpinner(SpimParams.DELAYBEFORESIDEA, 0, 10000));
      add(makeSpinner(SpimParams.DELAYBEFORESIDEB, 0, 10000), "wrap");
      
   }
   
   /**
    * Listener for the Axis directions combox boxes
    * Updates the model in the Devices class with any GUI changes
    */
   class IntSpinnerListener implements ChangeListener {
      String axis_;
      JSpinner sp_;

      public IntSpinnerListener(String axis, JSpinner sp) {
         axis_ = axis;
         sp_ = sp;
      }

      public void stateChanged(ChangeEvent ce) {
         params_.putIntInfo(axis_, (Integer) sp_.getValue());
      }
   };
   
   private JSpinner makeSpinner(String name, int min, int max) {
      SpinnerModel jspm = new SpinnerNumberModel(params_.getIntInfo(name), min, max, 1);
      JSpinner jsp = new JSpinner(jspm);
      jsp.addChangeListener(new IntSpinnerListener(name, jsp));
 
      return jsp;
   }
   
   
      /**
    * Listener for the Axis directions combox boxes
    * Updates the model in the Devices class with any GUI changes
    */
   class SidesBoxListener implements ActionListener {
      String side_;
      JComboBox box_;

      public SidesBoxListener(String side, JComboBox box) {
         side_ = side;
         box_ = box;
      }

      public void actionPerformed(ActionEvent ae) {
         params_.putSidesInfo(side_, (String) box_.getSelectedItem());
      }
   };
   
   
  /**
    * Constructs a DropDown box containing X/Y.
    * Sets selection based on info in the Devices class and attaches
    * a Listener
    * 
    * @param side - Name under which this side is known in the Device class
    * @return constructed JComboBox
    */
   private JComboBox makeABBox(String side) {
      String[] ab = {SpimParams.A, SpimParams.B};
      JComboBox jcb = new JComboBox(ab);
      jcb.setSelectedItem(params_.getSidesInfo(side));
      jcb.addActionListener(new SidesBoxListener (side, jcb));
 
      return jcb;
   }
   
}
