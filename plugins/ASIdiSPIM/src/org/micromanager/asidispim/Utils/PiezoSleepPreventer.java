///////////////////////////////////////////////////////////////////////////////
//FILE:          PiezoSleepPreventer.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2014
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

package org.micromanager.asidispim.Utils;

import java.util.Timer;
import java.util.TimerTask;

import org.micromanager.api.ScriptInterface;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Properties;

import mmcorej.CMMCore;

/**
 *
 * @author Jon
 */
public class PiezoSleepPreventer {
   private final CMMCore core_;
   private final Devices devices_;
   private final Properties props_;
   private Timer timer_;
   
   /**
    * Utility class for stage position timer.
    * 
    * The timer will be constructed when the start function is called.
    * Panels to be informed of updated stage positions should be added
    * using the addPanel function.
    * 
    * @param positions
    * @param props
    */
   public PiezoSleepPreventer(ScriptInterface gui, Devices devices, Properties props) {
      core_ = gui.getMMCore();
      devices_ = devices;
      props_ = props;
      timer_ = null;
   }
   
   /**
    * Start the timer.  Uses its own thread viajava.util.Timer.scheduleAtFixedRate().
    * Call stop() to stop.
    */
   public void start() {
      // end any existing updater before starting (anew)
      if (timer_ != null) {
         timer_.cancel();
      }
      timer_ = new Timer(true);
      timer_.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               unsleepPiezos();
            }
          }, 0, 40*1000);  // run every 40 seconds
//      unsleepPiezos();
   }
   
   /**
    * Stops the timer.
    */
   public void stop() {
      if (timer_ != null) {
         timer_.cancel();
      }
   }
   
   /**
    * Moves the piezos by 0 to reset their sleep timer.
    */
   public void unsleepPiezos() {
      try {
         for (Devices.Keys piezoKey : Devices.PIEZOS) {
            if (devices_.isValidMMDevice(piezoKey)) {
               if (props_.getPropValueInteger(piezoKey, Properties.Keys.AUTO_SLEEP_DELAY) > 0) {
                  core_.setRelativePosition(devices_.getMMDevice(piezoKey), 0);
               }
            }
         }
      } catch (Exception e) {
         MyDialogUtils.showError("Could not reset piezo's positions");
      }
   }
   
}
