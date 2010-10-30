/*
 * DualAndorFrame.java
 *
 * Created on Oct 29, 2010, 11:32:29 AM
 *
 * Copyright UCSF, 2010
 *
 Author: Nico Stuurman: nico at cmp.ucsf.edu
 *
 */

package org.micromanager.multicamera;


import java.awt.Rectangle;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.MMCoreJ;

import org.micromanager.api.ScriptInterface;
import org.micromanager.api.DeviceControlGUI;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;


/**
 *
 * @author Nico Stuurman
 */
public class MultiCameraFrame extends javax.swing.JFrame {
   private final ScriptInterface gui_;
   private final DeviceControlGUI dGui_;
   private final CMMCore core_;
   private Preferences prefs_;
   private NumberFormat nf_;

   private int frameXPos_ = 100;
   private int frameYPos_ = 100;
   
   private long imageWidth_ = 512;
   private long imageHeight_ = 512;

   private String[] cameras_;

   private static final String FRAMEXPOS = "FRAMEXPOS";
   private static final String FRAMEYPOS = "FRAMEYPOS";

    /** Creates new form DualAndorFrame */
    public MultiCameraFrame(ScriptInterface gui) throws Exception {
       gui_ = gui;
       dGui_ = (DeviceControlGUI) gui_;
       core_ = gui_.getMMCore();
       nf_ = NumberFormat.getInstance();
       prefs_ = Preferences.userNodeForPackage(this.getClass());


       mmcorej.StrVector cameras = core_.getLoadedDevicesOfType(DeviceType.CameraDevice);
       cameras_ = cameras.toArray();

       if (cameras_.length <= 1) {
          gui_.showError("This plugin needs at least two cameras");
          throw new IllegalArgumentException("This plugin needs at least two cameras");
       }

       String currentCamera = core_.getCameraDevice();
       imageWidth_ = core_.getImageWidth();
       imageHeight_ = core_.getImageHeight();
       for (String camera : cameras_) {
          if (!camera.equals(currentCamera)) {
             core_.setCameraDevice(camera);
             if (imageWidth_ != core_.getImageWidth() ||
                 imageHeight_ != core_.getImageHeight()) {
                throw new IllegalArgumentException("Plugin failed to load since the attached cameras differ in image size");
             }
          }
       }

       // Read values from PREFS
       frameXPos_ = prefs_.getInt(FRAMEXPOS, frameXPos_);
       frameYPos_ = prefs_.getInt(FRAMEYPOS, frameYPos_);

       initComponents();

       setLocation(frameXPos_, frameYPos_);

       setBackground(gui_.getBackgroundColor());
       gui_.addMMBackgroundListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jLabel1 = new javax.swing.JLabel();
      jButton2 = new javax.swing.JButton();
      jButton3 = new javax.swing.JButton();
      jLabel2 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      jComboBox1 = new javax.swing.JComboBox();
      jTextField1 = new javax.swing.JTextField();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Multi-Camera Control");

      jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
      jLabel1.setText("ROI");

      jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/shape_handles.png"))); // NOI18N
      jButton2.setToolTipText("Set Region of Interest");
      jButton2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton2ActionPerformed(evt);
         }
      });

      jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/arrow_out.png"))); // NOI18N
      jButton3.setToolTipText("Set Region of Interest");
      jButton3.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
         }
      });

      jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 12));
      jLabel2.setText("Exposure [ms]");

      jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 12));
      jLabel3.setText("Binning");

      jComboBox1.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
      jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "4", "8" }));
      jComboBox1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jComboBox1ActionPerformed(evt);
         }
      });

      jTextField1.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
      jTextField1.setText("10");
      jTextField1.setToolTipText("Exposure time in ms");
      jTextField1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jTextField1ActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(24, 24, 24)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(jLabel1)
               .add(layout.createSequentialGroup()
                  .add(jButton2)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jButton3))
               .add(layout.createSequentialGroup()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel2)
                     .add(jLabel3))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jComboBox1, 0, 87, Short.MAX_VALUE)
                     .add(jTextField1))))
            .addContainerGap(369, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel2)
               .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 58, Short.MAX_VALUE)
            .add(jLabel1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jButton3)
               .add(jButton2))
            .add(19, 19, 19))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       setRoi();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       setRoi(new Rectangle(0,0,(int)imageWidth_, (int)imageHeight_));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
       boolean liveRunning = dGui_.getLiveMode();
       String currentCamera = "";
       try {
          double exposure = NumberUtils.displayStringToDouble(jTextField1.getText());
          currentCamera =  core_.getCameraDevice();
          dGui_.enableLiveMode(false);
          for(String camera: cameras_) {
             core_.setCameraDevice(camera);
             core_.setExposure(exposure);
          }
          jTextField1.setText(NumberUtils.doubleToDisplayString(exposure));

         dGui_.updateGUI(false);
         dGui_.enableLiveMode(liveRunning);
      } catch (Exception exp) {
         // Do nothing.
      }
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
       boolean liveRunning = dGui_.getLiveMode();
       Object item = jComboBox1.getSelectedItem();
       String binning = item.toString();
       try {
          dGui_.enableLiveMode(false);
          for(String camera: cameras_) {
             if (!camera.equals("")) {
                core_.setProperty(camera, MMCoreJ.getG_Keyword_Binning(), binning);
             }
          }
          dGui_.enableLiveMode(liveRunning);
          dGui_.updateGUI(false);
       } catch (Exception ex) {
          Logger.getLogger(MultiCameraFrame.class.getName()).log(Level.SEVERE, null, ex);
       }
    }//GEN-LAST:event_jComboBox1ActionPerformed


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButton2;
   private javax.swing.JButton jButton3;
   private javax.swing.JComboBox jComboBox1;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JTextField jTextField1;
   // End of variables declaration//GEN-END:variables


   private void setRoi (Rectangle roi) {
      boolean liveRunning = dGui_.getLiveMode();
      String currentCamera = "";
      try {
         currentCamera =  core_.getCameraDevice();
         dGui_.enableLiveMode(false);
         for(String camera: cameras_) {
            core_.setCameraDevice(camera);
            core_.setROI(roi.x, roi.y, roi.width, roi.height);
         }
         core_.setCameraDevice(currentCamera);
         dGui_.enableLiveMode(liveRunning);
      } catch (Exception ex) {
         Logger.getLogger(MultiCameraFrame.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void setRoi() {
      ImagePlus curImage = WindowManager.getCurrentImage();
      if (curImage == null) {
         return;
      }

      Roi roi = curImage.getRoi();

      try {
         if (roi == null) {
            // if there is no ROI, create one
            Rectangle r = curImage.getProcessor().getRoi();
            int iWidth = r.width;
            int iHeight = r.height;
            int iXROI = r.x;
            int iYROI = r.y;
            if (roi == null) {
               iWidth /= 2;
               iHeight /= 2;
               iXROI += iWidth / 2;
               iYROI += iHeight / 2;
            }

            curImage.setRoi(iXROI, iYROI, iWidth, iHeight);
            roi = curImage.getRoi();
         }

         if (roi.getType() != Roi.RECTANGLE) {
            gui_.showError("ROI must be a rectangle.\nUse the ImageJ rectangle tool to draw the ROI.");
            return;
         }

         Rectangle r = roi.getBoundingRect();


         // Stop (and restart) live mode if it is running
         setRoi(r);

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }
}
