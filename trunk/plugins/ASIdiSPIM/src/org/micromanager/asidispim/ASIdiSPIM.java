///////////////////////////////////////////////////////////////////////////////
//FILE:          ASIdiSPIM.java
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

import mmcorej.CMMCore;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ReportingUtils;


public class ASIdiSPIM implements MMPlugin {
   public static String menuName = "ASI diSPIM";
   public static String tooltipDescription = "Control the ASI diSPIM ";
   private CMMCore core_;
   private ScriptInterface gui_;
   private ASIdiSPIMFrame myFrame_;

    @Override
   public void setApp(ScriptInterface app) {
      gui_ = app;                                        
      core_ = app.getMMCore();
     // if (myFrame_ == null) {
         try {
            myFrame_ = new ASIdiSPIMFrame(gui_);
            myFrame_.setBackground(gui_.getBackgroundColor());
            gui_.addMMListener(myFrame_);
            gui_.addMMBackgroundListener(myFrame_);
         } catch (Exception e) {
            ReportingUtils.showError(e);
         }
      //}
      myFrame_.setVisible(true);
   }

    @Override
   public void dispose() {
      if (myFrame_ != null)
         myFrame_.dispose();
   }

    @Override
   public void show() {
         String ig = "ASI diSPIM";
   }

    @Override
   public String getInfo () {
      return "ASI diSPIM";
   }

    @Override
   public String getDescription() {
      return tooltipDescription;
   }
   
    @Override
   public String getVersion() {
      return "0.1";
   }
   
    @Override
   public String getCopyright() {
      return "University of California and ASI, 2013";
   }
}
