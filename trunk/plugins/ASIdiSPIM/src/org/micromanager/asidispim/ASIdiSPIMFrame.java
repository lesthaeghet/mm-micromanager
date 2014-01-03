///////////////////////////////////////////////////////////////////////////////
//FILE:          ASIdiSPIMFrame.java
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

import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Joystick;
import org.micromanager.asidispim.Data.Positions;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.ListeningJTabbedPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;
import org.micromanager.MMStudioMainFrame; 
import org.micromanager.internalinterfaces.LiveModeListener; 

// TODO figure out update of slider limits when devices changed
// TODO able to disable position update
// TODO fix scan A/B when switching between tabs
// TODO figure out why NR Z, NV X, and NV Y are called by devices_.callListeners()
// TODO resolve whether Home/Stop should be added to 1axis stage API, use here if possible
// TODO add sethome property to device adapter and use it here
// TODO add help text and/or link to wiki
// TODO check if window position is off screen (can happen if changing monitors) and reset if so
// TODO centralize preference handling?
// TODO make position listener instead of current update mechanism?

/**
 *
 * @author nico
 * @author Jon
 */
@SuppressWarnings("serial")
public class ASIdiSPIMFrame extends javax.swing.JFrame  
      implements MMListenerInterface {
   
   public Properties props_;  // using like global variable so I don't have to pass object all the way down to event handlers
   private Preferences prefs_;
   private Devices devices_;
   private Joystick joystick_;
   private Positions positions_;
   
   private static final String PREF_XLOCATION = "xlocation";
   private static final String PREF_YLOCATION = "ylocation";
   private static final String PREF_TABINDEX = "tabIndex";
   
   /**
    * Creates the ASIdiSPIM plugin frame
    * @param gui - Micro-Manager script interface
    */
   public ASIdiSPIMFrame(ScriptInterface gui)  {
      prefs_ = Preferences.userNodeForPackage(this.getClass());
      devices_ = new Devices();
      props_ = new Properties(devices_);  // doesn't have its own frame, but is an object used by other classes
      positions_ = new Positions(devices_);
      joystick_ = new Joystick(devices_, props_);
      
      final ListeningJTabbedPane tabbedPane = new ListeningJTabbedPane();
        
      // all added tabs must be of type ListeningJPanel
      // only use addLTab, not addTab to guarantee this
      tabbedPane.addLTab("Devices", new DevicesPanel(devices_));
      tabbedPane.addLTab("SPIM Params", new SpimParamsPanel(devices_, props_));
      final ListeningJPanel setupPanelA = new SetupPanel(devices_, props_, joystick_, Devices.Sides.A, positions_);
      tabbedPane.addLTab("Setup Path A", setupPanelA);
      MMStudioMainFrame.getInstance().addLiveModeListener((LiveModeListener) setupPanelA);
      final ListeningJPanel setupPanelB = new SetupPanel(devices_, props_, joystick_, Devices.Sides.B, positions_); 
      tabbedPane.addLTab("Setup Path B", setupPanelB);
      MMStudioMainFrame.getInstance().addLiveModeListener((LiveModeListener) setupPanelB);
      final ListeningJPanel navigationPanel = new NavigationPanel(devices_, joystick_, positions_);
      tabbedPane.addLTab("Navigation", navigationPanel);
      tabbedPane.addLTab("Help", new HelpPanel());
      
      final Timer stagePosUpdater = new Timer(1000000, new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            // update stage positions in devices
            positions_.updateStagePositions();
            // notify listeners that positions are updated
            setupPanelA.updateStagePositions();
            setupPanelB.updateStagePositions();
            navigationPanel.updateStagePositions();
         }
      });
      stagePosUpdater.start();
      
      // make sure gotSelected() gets called whenever we switch tabs
      tabbedPane.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            ((ListeningJPanel) tabbedPane.getSelectedComponent()).gotSelected();
         }
      });
    
      // put pane back where it was last time
      // gotSelected will be called because we put it after the ChangeListener code
      setLocation(prefs_.getInt(PREF_XLOCATION, 100), prefs_.getInt(PREF_YLOCATION, 100));
      tabbedPane.setSelectedIndex(prefs_.getInt(PREF_TABINDEX, 0));

      // set up the window
      add(tabbedPane);  // add the pane to the GUI window
      setTitle("ASI diSPIM Control"); 
      pack();           // shrinks the window as much as it can
      setResizable(false);
      
      // take care of shutdown tasks when window is closed
      addWindowListener(new java.awt.event.WindowAdapter() {
         @Override
         public void windowClosing(java.awt.event.WindowEvent evt) {
            // stop the timer for updating stages
            stagePosUpdater.stop();

            // save selections in device tab
            devices_.saveSettings();

            // save pane location in prefs
            prefs_.putInt(PREF_XLOCATION, evt.getWindow().getX());
            prefs_.putInt(PREF_YLOCATION, evt.getWindow().getY());
            prefs_.putInt(PREF_TABINDEX, tabbedPane.getSelectedIndex());
         }
      });
   }
   
   public void unsetAllJoysticks() {
      joystick_.unsetAllJoysticks();
   }
    

   // MMListener mandated member functions
   public void propertiesChangedAlert() {
      // doesn't seem to actually be called by core when property changes
      props_.callListeners();
   }

   public void propertyChangedAlert(String device, String property, String value) {
      props_.callListeners();
   }

   public void configGroupChangedAlert(String groupName, String newConfig) {
   }

   public void systemConfigurationLoaded() {
   }

   public void pixelSizeChangedAlert(double newPixelSizeUm) {
   }

   public void stagePositionChangedAlert(String deviceName, double pos) {
   }

   public void xyStagePositionChanged(String deviceName, double xPos, double yPos) {
   }

   public void exposureChanged(String cameraName, double newExposureTime) {
   }

}
