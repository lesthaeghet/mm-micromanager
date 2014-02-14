///////////////////////////////////////////////////////////////////////////////
//FILE:          ListeningJPanel.java
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

package org.micromanager.asidispim.Utils;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Extension of JPanel that adds a few callbacks making it possible for the
 * enclosing frame to easily inform tabs of events
 * @author nico
 */
@SuppressWarnings("serial")
public class ListeningJPanel extends JPanel {
   
   protected String panelName_;
   
   public ListeningJPanel(String panelName, LayoutManager l) {
      super (l);
      panelName_ = panelName;
   }
   
   /**
    * Will be called when this Panel is selected in the parent TabbedPanel 
    */
   public void gotSelected() {};
   
   /**
    * Should force the panel to write its current settings to its preferences
    */
   public void saveSettings() {};
   
   /**
    * Called when new stage positions are available in the device class
    */
   public void updateStagePositions() {};
   
   /**
    * returns the name of the panel 
    */
   public String getPanelName() { return panelName_; }
   
}
