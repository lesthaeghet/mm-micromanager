///////////////////////////////////////////////////////////////////////////////
//FILE:          BeamSubPanel.java
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.micromanager.api.ScriptInterface;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.MyStrings;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.PanelUtils;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Jon
 */
@SuppressWarnings("serial")
public final class BeamSubPanel extends ListeningJPanel {
   private final Devices devices_;
   private final Prefs prefs_;
   private final Properties props_;
   private final String instanceLabel_;
   private final Devices.Sides side_;
   private final Devices.Sides otherSide_;
   
   private final JCheckBox beamABox_;
   private final JCheckBox beamBBox_;
   private final JCheckBox sheetABox_;
   private final JCheckBox sheetBBox_;
   private final JCheckBox updateOnTab_;
   private final ItemListener disableSheetA_;
   private final ItemListener disableSheetB_;
   
   /**
    * 
    * @param devices the (single) instance of the Devices class
    */
   public BeamSubPanel(ScriptInterface gui, Devices devices, String instanceLabel, 
         Devices.Sides side, Prefs prefs, Properties props) {    
      super (MyStrings.TabNames.BEAM_SUBPANEL.toString() + instanceLabel,
            new MigLayout(
              "", 
              "[right]8[left]",
              "[]4[]"));

      
      devices_ = devices;
      prefs_ = prefs;
      props_ = props;
      instanceLabel_ = instanceLabel;
      
      PanelUtils pu = new PanelUtils(gui, prefs_, props_, devices_);
      
      // check to see if we are on "neutral" side (NONE)
      // if so act like we are on side A but remember that so we can label accordingly
      boolean noSide = false;
      if (side == Devices.Sides.NONE) {
         noSide = true;
         side = Devices.Sides.A;
      }
      
      side_ = side;
      otherSide_ = Devices.getOppositeSide(side);
         
      // NB: "A" and "B" in names doesn't necessarily mean path A and path B anymore
      // beamABox_ and scanBBox associated with the side passed as a parameter
      beamABox_ = pu.makeCheckBox("Beam",
            Properties.Values.NO, Properties.Values.YES,
            Devices.getSideSpecificKey(Devices.Keys.GALVOA, side_),
            Properties.Keys.BEAM_ENABLED, instanceLabel_, Prefs.Keys.SHEET_BEAM_ENABLED);
      sheetABox_ = pu.makeCheckBox("Sheet", 
            Properties.Values.SAM_DISABLED, Properties.Values.SAM_ENABLED,
            Devices.getSideSpecificKey(Devices.Keys.GALVOA, side_),
            Properties.Keys.SA_MODE_X, instanceLabel_, Prefs.Keys.SHEET_SCAN_ENABLED);

      beamBBox_ = pu.makeCheckBox("Beam",
            Properties.Values.NO, Properties.Values.YES,
            Devices.getSideSpecificKey(Devices.Keys.GALVOA, otherSide_),
            Properties.Keys.BEAM_ENABLED, instanceLabel_, Prefs.Keys.EPI_BEAM_ENABLED);
      sheetBBox_ = pu.makeCheckBox("Sheet", 
            Properties.Values.SAM_DISABLED, Properties.Values.SAM_ENABLED,
            Devices.getSideSpecificKey(Devices.Keys.GALVOA, otherSide_),
            Properties.Keys.SA_MODE_X, instanceLabel_, Prefs.Keys.EPI_SCAN_ENABLED);
      
      if (noSide) {
         add(new JLabel("Path A:"));
         add(beamABox_);
         add(sheetABox_, "wrap");
         add(new JLabel("Path B:"));
         add(beamBBox_);
         add(sheetBBox_, "wrap");         
      } else {
         add(new JLabel("Sheet side:"));
         add(beamABox_);
         add(sheetABox_, "wrap");
         add(new JLabel("Epi side:"));
         add(beamBBox_);
         add(sheetBBox_, "wrap");   
      }
      
      // mechanism to disable the sheetA/B boxes when beam is off and vice versa
      disableSheetA_ = makeDisableSheetListener(sheetABox_);
      disableSheetB_ = makeDisableSheetListener(sheetBBox_);
      beamABox_.addItemListener(disableSheetA_);
      beamBBox_.addItemListener(disableSheetB_);
      
      updateOnTab_ = new JCheckBox("Change settings on tab activate");
      updateOnTab_.setSelected(prefs_.getBoolean(instanceLabel_, Prefs.Keys.ENABLE_BEAM_SETTINGS, true));
      add(updateOnTab_, "center, span 3");

   }//constructor
   
   
   private ItemListener makeDisableSheetListener(JCheckBox sheetBox) {
      /**
       * nested inner class 
       * @author Jon
       */
      class checkBoxDisableListener implements ItemListener {
         JCheckBox sheetBox_;
         
         public checkBoxDisableListener(JCheckBox sheetBox) {
            sheetBox_ = sheetBox;
         }
         
         // only called when the user selects/deselects from GUI or the value _changes_ programmatically
         @Override
         public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
               sheetBox_.setEnabled(true);
            } else {
               sheetBox_.setEnabled(false);
               sheetBox_.setSelected(false);  // turn off sheet too, since that's what firmware does
            }
         }//itemStateChanged
         
      }// nested inner class
      
      // main body of function
      return new checkBoxDisableListener(sheetBox);
   }
   
   
   /**
   * Should be called when enclosing panel gets focus (need to call in gotSelected() function of enclosing frame).
   * Includes first time if the frame is selected initially.
   */ 
   @Override
  public void gotSelected() {
      // have to do this the "hard way" because itemStateChanged only responds to _changes_ in value
      //   and changing the beam setting (to refresh it) erases the state of the sheet setting in firmware
      syncPropertyAndCheckBox(beamABox_, Devices.getSideSpecificKey(Devices.Keys.GALVOA, side_), 
            Properties.Keys.BEAM_ENABLED, Properties.Values.YES, Properties.Values.NO,
            updateOnTab_.isSelected());
      syncPropertyAndCheckBox(sheetABox_, Devices.getSideSpecificKey(Devices.Keys.GALVOA, side_),
            Properties.Keys.SA_MODE_X, Properties.Values.SAM_ENABLED, Properties.Values.SAM_DISABLED,
            updateOnTab_.isSelected());
      syncPropertyAndCheckBox(beamBBox_, Devices.getSideSpecificKey(Devices.Keys.GALVOA, otherSide_),
            Properties.Keys.BEAM_ENABLED, Properties.Values.YES, Properties.Values.NO,
            updateOnTab_.isSelected());
      syncPropertyAndCheckBox(sheetBBox_, Devices.getSideSpecificKey(Devices.Keys.GALVOA, otherSide_), 
            Properties.Keys.SA_MODE_X, Properties.Values.SAM_ENABLED, Properties.Values.SAM_DISABLED,
            updateOnTab_.isSelected());
      // make sure the sheet enables get set properly; this is handled on user input by 
      //   listeners based on makeDisableSheetListener
      sheetABox_.setEnabled(beamABox_.isSelected());
      sheetBBox_.setEnabled(beamBBox_.isSelected());
  }
   
   
   /**
    * Internal function for updating the controller properties according to checkbox settings
    * or vice versa.
    * @param box
    * @param devKey
    * @param propKey
    * @param onValue
    */
   private void syncPropertyAndCheckBox(JCheckBox box, Devices.Keys devKey, Properties.Keys propKey, 
         Properties.Values onValue, Properties.Values offValue, boolean checkBoxWins) {
      if (devices_.getMMDevice(devKey)==null) {
         box.setEnabled(false);
         box.setSelected(false);
      } else {
         box.setEnabled(true);
         if (checkBoxWins) {
            boolean boxVal = box.isSelected();
            boolean propVal = props_.getPropValueString(devKey, propKey).equals(onValue.toString());
            if (boxVal != propVal) {  // if property value is "wrong" then change it
               props_.setPropValue(devKey, propKey, (boxVal ? onValue : offValue), true);
            }
         } else {
            box.setSelected(props_.getPropValueString(devKey, propKey, true).equals(onValue.toString()));
         }
      }
   }
   
   
   @Override
   public void saveSettings() {

   }
   
   
}
