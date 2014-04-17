///////////////////////////////////////////////////////////////////////////////
//FILE:          NewTaggedImageFlipper.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Arthur Edelstein, Nico Stuurman
//
// COPYRIGHT:    University of California, San Francisco, 2011, 2012
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

package org.micromanager.newimageflipper;

import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

/**
 * Example demonstrating the use of DataProcessors.  DataProcessors can 
 * get hold of images coming out of the acquisition engine before they 
 * are inserted into the ImageCache.  DataProcessors can modify images 
 * or even generate totally new ones.
 * 
 * This specific example has grown out to modify images only from a specific camera
 * and is therefore very useful when using multiple cameras
 * 
 * @author arthur
 */
public class NewTaggedImageFlipper implements MMPlugin {
   public static String menuName = "Image Flipper";
   public static String tooltipDescription = "Mirrors, flips and rotates images on the fly";
   private ScriptInterface gui_;
   private NewImageFlipperControls controls_;

   

   public void dispose() {
      if (controls_ != null)
         controls_.safePrefs();
   }

   public void setApp(ScriptInterface app) {
      gui_ = app;
   }

   public void show() {
      if (controls_ == null) {
         controls_ = new NewImageFlipperControls();
         MMStudioMainFrame.getInstance().addMMBackgroundListener(controls_);
      }
      else {
         controls_.updateCameras();
      }
      gui_.addImageProcessor(controls_.getProcessor());
      controls_.setVisible(true);
   }

   public void configurationChanged() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String getDescription() {
      return "Rotates and/or mirrors images coming from the selected camera";
   }

   public String getInfo() {
      return "Not supported yet.";
   }

   public String getVersion() {
      return "Version 0.1";
   }

   public String getCopyright() {
      return "Copyright University of California San Francisco, 2012";
   }

}
