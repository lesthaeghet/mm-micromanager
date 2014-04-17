///////////////////////////////////////////////////////////////////////////////
//FILE:          NewImageFlipperControls.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Arthur Edelstein, Nico Stuurman
//
// COPYRIGHT:    University of California, San Francisco, 2011, 2012, 2013
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

import ij.ImagePlus;
import ij.process.ByteProcessor;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import mmcorej.StrVector;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.DataProcessor;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class NewImageFlipperControls extends javax.swing.JFrame {

   private final NewImageFlippingProcessor processor_;
   private String selectedCamera_;
   private final String FRAMEXPOS = "NewImageFlipperXPos";
   private final String FRAMEYPOS = "NewImageFlipperYPos";
   private final String R0 = "0" + "\u00B0";
   private final String R90 = "90" + "\u00B0";
   private final String R180 = "180" + "\u00B0";
   private final String R270 = "270" + "\u00B0";
   private final String[] RS = {R0, R90, R180, R270};
   private final String ROTATEBOX = "RotateBox";
   private final String MIRRORCHECKBOX = "MirrorCheckBox";
   private final String ROTATE = "_Rotation";
   private final String MIRROR = "_Mirror";
   private final String SELECTEDCAMERA = "SelectedCamera";
   private Preferences prefs_;
   private int frameXPos_ = 300;
   private int frameYPos_ = 300;   

   /** 
    * Creates form NewImageFlipperControls 
    */
   public NewImageFlipperControls() {

      prefs_ = Preferences.userNodeForPackage(this.getClass());

      frameXPos_ = prefs_.getInt(FRAMEXPOS, frameXPos_);
      frameYPos_ = prefs_.getInt(FRAMEYPOS, frameYPos_);

      initComponents();
      
      selectedCamera_ = prefs_.get(SELECTEDCAMERA, 
              MMStudioMainFrame.getInstance().getCore().getCameraDevice());
      
      mirrorCheckBox_.setSelected(prefs_.getBoolean(selectedCamera_ + MIRROR, false));
            
      rotateComboBox_.removeAllItems();
      for (String item: RS)
         rotateComboBox_.addItem(item);
      rotateComboBox_.setSelectedItem(prefs_.get(selectedCamera_ + ROTATE, R0));

      setLocation(frameXPos_, frameYPos_);
       
      updateCameras();
      
      setBackground(MMStudioMainFrame.getInstance().getBackgroundColor());
      processor_ = new NewImageFlippingProcessor(this.getCamera(), 
            getMirror(), getRotate());
   }

   public DataProcessor<TaggedImage> getProcessor() {
      return processor_;
   }

   public void safePrefs() {
      prefs_.putInt(FRAMEXPOS, this.getX());
      prefs_.putInt(FRAMEYPOS, this.getY());
   }

   /**
    * updates the content of the camera selection drop down box
    * 
    * Shows all available cameras and sets the currently selected camera
    * as the selected item in the drop down box
    */
   final public void updateCameras() {
      cameraComboBox_.removeAllItems();
      try {
         StrVector cameras = MMStudioMainFrame.getInstance().getCore().getAllowedPropertyValues("Core", "Camera");
         for (String camera : cameras) {
            cameraComboBox_.addItem(camera);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
      cameraComboBox_.setSelectedItem(selectedCamera_);
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      mirrorCheckBox_ = new javax.swing.JCheckBox();
      exampleImageSource_ = new javax.swing.JLabel();
      exampleImageTarget_ = new javax.swing.JLabel();
      cameraComboBox_ = new javax.swing.JComboBox();
      rotateComboBox_ = new javax.swing.JComboBox();
      jLabel1 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Image Flipper");
      setBounds(new java.awt.Rectangle(300, 300, 150, 150));
      setMinimumSize(new java.awt.Dimension(200, 200));
      setResizable(false);
      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosed(java.awt.event.WindowEvent evt) {
            formWindowClosed(evt);
         }
      });

      mirrorCheckBox_.setText("Mirror");
      mirrorCheckBox_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            mirrorCheckBox_ActionPerformed(evt);
         }
      });

      exampleImageSource_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/R.png"))); // NOI18N

      exampleImageTarget_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/R.png"))); // NOI18N

      cameraComboBox_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cameraComboBox_ActionPerformed(evt);
         }
      });

      rotateComboBox_.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "90", "180", "270" }));
      rotateComboBox_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            rotateComboBox_ActionPerformed(evt);
         }
      });

      jLabel1.setText("Rotate");

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, cameraComboBox_, 0, 153, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                        .add(exampleImageSource_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(exampleImageTarget_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(rotateComboBox_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                  .add(38, 38, 38))
               .add(layout.createSequentialGroup()
                  .add(mirrorCheckBox_)
                  .addContainerGap(121, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .add(11, 11, 11)
            .add(cameraComboBox_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(exampleImageTarget_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
               .add(exampleImageSource_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(mirrorCheckBox_)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(rotateComboBox_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel1))
            .add(25, 25, 25))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void mirrorCheckBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirrorCheckBox_ActionPerformed
       processExample();
       String camera = (String) cameraComboBox_.getSelectedItem();
       if (camera != null) {
          prefs_.putBoolean(camera + MIRROR, mirrorCheckBox_.isSelected());
          if (processor_ != null) {
             processor_.setIsMirrored(getMirror());
          }
       }
    }//GEN-LAST:event_mirrorCheckBox_ActionPerformed

   private void rotateComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComboBox_ActionPerformed
      processExample();
      String camera = (String) cameraComboBox_.getSelectedItem();
      if (camera != null && rotateComboBox_.getSelectedItem() != null) {
         prefs_.put(camera + ROTATE, (String) rotateComboBox_.getSelectedItem());
         if (processor_ != null) {
            processor_.setRotation(getRotate());
         }
      }
   }//GEN-LAST:event_rotateComboBox_ActionPerformed

   private void cameraComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraComboBox_ActionPerformed
      String camera = (String) cameraComboBox_.getSelectedItem();
      if (camera != null) {
         if (processor_ != null) {
            processor_.setCamera(camera);
         }
         mirrorCheckBox_.setSelected(prefs_.getBoolean(camera + MIRROR, false));
         rotateComboBox_.setSelectedItem(prefs_.get(camera + ROTATE, R0));
         prefs_.put(SELECTEDCAMERA, camera);
      }
   }//GEN-LAST:event_cameraComboBox_ActionPerformed

   private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
      MMStudioMainFrame.getInstance().removeImageProcessor(getProcessor());
   }//GEN-LAST:event_formWindowClosed

   /**
    * @param args the command line arguments
    */
   public static void main(String args[]) {
      java.awt.EventQueue.invokeLater(new Runnable() {

         public void run() {
            new NewImageFlipperControls().setVisible(true);
         }
      });
   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox cameraComboBox_;
   private javax.swing.JLabel exampleImageSource_;
   private javax.swing.JLabel exampleImageTarget_;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JCheckBox mirrorCheckBox_;
   private javax.swing.JComboBox rotateComboBox_;
   // End of variables declaration//GEN-END:variables

   public boolean getMirror() {
      return mirrorCheckBox_.isSelected();
   }

   /**
    * Indicates users choice for rotation:
    * 0 - 0 degrees
    * 1 - 90 degrees
    * 2 - 180 degrees
    * 3 - 270 degrees
    * degrees are anti-clockwise
    * 
    * @return coded rotation
    */
   public NewImageFlippingProcessor.Rotation getRotate() {
      if (R90.equals((String) rotateComboBox_.getSelectedItem())) {
         return NewImageFlippingProcessor.Rotation.R90;
      }
      if (R180.equals((String) rotateComboBox_.getSelectedItem())) {
         return NewImageFlippingProcessor.Rotation.R180;
      }
      if (R270.equals((String) rotateComboBox_.getSelectedItem())) {
         return NewImageFlippingProcessor.Rotation.R270;
      }
      return NewImageFlippingProcessor.Rotation.R0;
   }

   public String getCamera() {
      return (String) cameraComboBox_.getSelectedItem();
   }

   private void processExample() {

      ImageIcon exampleIcon = (ImageIcon) exampleImageSource_.getIcon();

      ByteProcessor proc = new ByteProcessor(exampleIcon.getImage());


      try {
         JSONObject newTags = new JSONObject();
         MDUtils.setWidth(newTags, proc.getWidth());
         MDUtils.setHeight(newTags, proc.getHeight());
         MDUtils.setPixelType(newTags, ImagePlus.GRAY8);
         TaggedImage result = NewImageFlippingProcessor.proccessTaggedImage(
                 new TaggedImage(proc.getPixels(), newTags), getMirror(), getRotate() );
         exampleImageTarget_.setIcon(
                 new ImageIcon(ImageUtils.makeProcessor(result).createImage()));
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }
}
