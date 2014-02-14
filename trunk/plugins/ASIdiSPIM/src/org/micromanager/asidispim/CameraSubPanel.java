///////////////////////////////////////////////////////////////////////////////
//FILE:          CameraSubPanel.java
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
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.ScriptInterface;
import org.micromanager.asidispim.Data.Cameras;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Utils.DevicesListenerInterface;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.internalinterfaces.LiveModeListener;

import com.swtdesigner.SwingResourceManager;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Jon
 */
@SuppressWarnings("serial")
public final class CameraSubPanel extends ListeningJPanel implements LiveModeListener {
   
   private final Devices devices_;
   private final Prefs prefs_;
   private final Cameras cameras_;
   private final ScriptInterface gui_;
   private final Devices.Sides side_;
   private final String instanceLabel_;
   private final JComboBox cameraBox_;
   private final JToggleButton toggleButtonLive_;
   
   /**
    * 
    * @param devices the (single) instance of the Devices class
    */
   public CameraSubPanel(Cameras cameras, Devices devices, String instanceLabel, 
         Devices.Sides side, Prefs prefs) {    
      super ("Camera_"+instanceLabel,
            new MigLayout(
              "", 
              "[right]8[align center]",
              "[]8[]"));

      cameras_ = cameras;
      devices_ = devices;
      side_ = side;
      prefs_ = prefs;
      instanceLabel_ = instanceLabel;
      gui_ = MMStudioMainFrame.getInstance();
      
      add(new JLabel("Camera:"));
      cameraBox_ = makeCameraSelectionBox();
      add(cameraBox_, "wrap");
      
      toggleButtonLive_ = new JToggleButton();
      toggleButtonLive_.setMargin(new Insets(0, 10, 0, 10));
      toggleButtonLive_.setIconTextGap(6);
      toggleButtonLive_.setToolTipText("Continuous live view");
      setLiveButtonAppearance(gui_.isLiveModeOn());
      toggleButtonLive_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setLiveButtonAppearance(!gui_.isLiveModeOn());
            cameras_.enableLiveMode(!gui_.isLiveModeOn());
         }
      });
      add(toggleButtonLive_, "width 100px, skip 1");
   }
   
   /**
    * required by LiveModeListener interface
    */
   public void liveModeEnabled(boolean enable) { 
      setLiveButtonAppearance(enable); 
   } 
   
   /** 
   * Changes the looks of the live button 
   * @param enable - true: live mode is switched on 
   */ 
   public final void setLiveButtonAppearance(boolean enable) {
      toggleButtonLive_.setIcon(enable ? SwingResourceManager.getIcon(MMStudioMainFrame.class,
            "/org/micromanager/icons/cancel.png")
            : SwingResourceManager.getIcon(MMStudioMainFrame.class,
                  "/org/micromanager/icons/camera_go.png"));
      toggleButtonLive_.setSelected(false);
      toggleButtonLive_.setText(enable ? "Stop Live" : "Live");
   }
   
   
   private JComboBox makeCameraSelectionBox() {
      JComboBox cameraBox = new JComboBox();
      CameraSelectionBoxListener csbl = new CameraSelectionBoxListener(cameraBox);
      cameraBox.addActionListener(csbl);
      devices_.addListener(csbl);
      // intentionally set from prefs after adding listeners so programmatic change from prefs will be handled like user change
      String selectedItem = prefs_.getString(instanceLabel_, Prefs.Keys.CAMERA, "");
      cameraBox.setSelectedItem(selectedItem);
      return cameraBox;
   }
   
   /**
    * Listener for Selection boxes that attach cameras
    */
   private class CameraSelectionBoxListener implements ActionListener, DevicesListenerInterface {
      JComboBox box_;
      HashMap<String, Cameras.CameraData> CameraDataHash_;
      
      public CameraSelectionBoxListener(JComboBox box) {
         box_ = box;
         CameraDataHash_ = new HashMap<String, Cameras.CameraData>();
         this.updateCameraSelections();  // do initial rendering
      }    

      /**
       * This will be called whenever the user selects a new item, but also when
       * the tab to which this selectionbox belongs is selected (via gotSelected() method)
       * Save the selection to preferences every time (easy but inefficient)
       *
       * @param ae
       */
      public void actionPerformed(ActionEvent ae) {
         Cameras.CameraData sel = CameraDataHash_.get( (String) box_.getSelectedItem());
         if (sel != null) {
            if (sel.deviceKey != Devices.Keys.NONE) {
               cameras_.setCamera(sel.deviceKey);
               prefs_.putString(instanceLabel_, Prefs.Keys.CAMERA, (String) box_.getSelectedItem());
            }
         }
      }
      
      
      /**
       * called whenever one of the devices is changed in the "Devices" tab
       */
      public void devicesChangedAlert() {
         updateCameraSelections();
      }
      
      /**
       * Resets the items in the combo box based on current contents of device tab.
       * Besides being called on original combo box creation, it is called whenever something in the devices tab is changed.
       * Based on similar code for joystick combo box, which is more complex
       */
      private void updateCameraSelections() {
         // save the existing selection if it exists
         String itemOrig = (String) box_.getSelectedItem();
         
         // get the appropriate list of strings (in form of CameraData array)
         Cameras.CameraData[] CameraDataItems = null;
         CameraDataHash_.clear();
         CameraDataItems = cameras_.getCameraData();

         boolean itemInNew = false;
         box_.removeAllItems();
         for (Cameras.CameraData a : CameraDataItems) {
            String s = a.displayString;
            if (side_ != Devices.Sides.NONE) {  // add imaging/epi label as appropriate
               
               if (side_ == a.side) {
                  s += " (Imaging)";
               } else if (Devices.getOppositeSide(side_) == a.side){
                  s += " (Epi)";
               }
            }
            box_.addItem(s);
            CameraDataHash_.put(s, a);
            if (s.equals(itemOrig)) {
               itemInNew = true;
            }
         }
         
         // restore the original selection if it's still present
         if (itemInNew) {
            box_.setSelectedItem(itemOrig);
         }
         else {
            box_.setSelectedItem("");
         }
      }//updateCameraSelections
      
   }//class CameraSelectionBoxListener
   
   
   /**
   * Should be called when enclosing panel gets focus.
   */
   @Override
  public void gotSelected() {
      cameraBox_.setSelectedItem(cameraBox_.getSelectedItem());
  }
   
   
}
