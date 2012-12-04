
/**
 * BFCorrectorFrame.java
 * 
 *  Plugin for flatfield and background correction
 * 
 * 
 * Nico Stuurman, copyright UCSF, 2012
 *  
 * LICENSE:      This file is distributed under the BSD license.
 *               License text is included with the source distribution.
 *
 *               This file is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty
 *               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 */

package org.micromanager.bfcorrector;

import ij.ImagePlus;
import java.io.File;
import java.util.prefs.Preferences;

import org.micromanager.api.AcquisitionEngine;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.FileDialogs.FileType;


/**
 * This plugin can attach a Processor to the acquisition engine
 * The Processor can execute either a flatfield correction, background subtraction
 * or both.
 * Both FlatField and Background images are read in as ImageJ (i.e. not as Micro-Manager)
 * images
 * The Flatfield image is normalized by its mean.  If a flatfield image is selected,
 * the incoming image will be divided (pixel by pixel) by the normalized flatfield image.
 * Division results in a 32-bit float image that is re-transformed to the original type.
 * If a background image is selected, it will be subtracted from the original image.
 * 
 * 
 * 
 * @author nico
 */
public class BFCorrectorFrame extends javax.swing.JFrame {
   private final ScriptInterface gui_;
   private Preferences prefs_;

   private int frameXPos_ = 100;
   private int frameYPos_ = 100;
   
   private final BFProcessor processor_;

   private static final String FRAMEXPOS = "FRAMEXPOS";
   private static final String FRAMEYPOS = "FRAMEYPOS";
   private static final String FLATFIELDFILENAME = "FlatfieldFileName";
   private static final String BACKGROUNDFILENAME = "BackgroundFileName";
   private static final String USECHECKBOX = "UseCheckBox";
   private final String[] IMAGESUFFIXES = {"tif", "tiff", "jpg", "png"};
   private String flatfieldFileName_;
   private String backgroundFileName_;


    /** 
    * Constructor
    * 
    * @param gui - Reference to MM script interface
    */
    public BFCorrectorFrame(ScriptInterface gui) {
       gui_ = gui;
       prefs_ = Preferences.userNodeForPackage(this.getClass());

       initComponents();
       setBackground(gui_.getBackgroundColor());
          
       processor_ = new BFProcessor();
       
       // Read preferences and apply to the dialog
       frameXPos_ = prefs_.getInt(FRAMEXPOS, frameXPos_);
       frameYPos_ = prefs_.getInt(FRAMEYPOS, frameYPos_);
       setLocation(frameXPos_, frameYPos_);
      
       useCheckBox_.setSelected(prefs_.getBoolean(USECHECKBOX, true));
       
       String flatFieldFileName = prefs_.get(FLATFIELDFILENAME, "");
       processFlatFieldImage(flatFieldFileName);
       flatFieldTextField_.setText(flatFieldFileName);
       
       if (useCheckBox_.isSelected()) {
          attachProcessor();
       }

    }
    
    
    private void attachProcessor() {
       AcquisitionEngine eng = gui_.getAcquisitionEngine();
       eng.addImageProcessor(processor_);
    }
    
    private void dettachProcessor() {
       AcquisitionEngine eng = gui_.getAcquisitionEngine();
       eng.removeImageProcessor(processor_);
    }
     
    private void processFlatFieldImage(String fileName) {
       ij.io.Opener opener = new ij.io.Opener();
       
       ImagePlus ip = opener.openImage(fileName);
       
       // set flat field even if the processor is null
       // otherwise, the user has no way to only select baground subtraction
       processor_.setFlatField(ip);
       
       
       flatfieldFileName_ = fileName;
       flatFieldTextField_.setText(flatfieldFileName_);
       prefs_.put(FLATFIELDFILENAME, flatfieldFileName_);
           
    }
    
    private void processBackgroundImage(String fileName) {
       ij.io.Opener opener = new ij.io.Opener();
       
       ImagePlus ip = opener.openImage(fileName);
       
       // set beckground even if the processor is null
       // otherwise, the uses has no way to only select flatfielding
       processor_.setBackground(ip);
             
       backgroundFileName_ = fileName;
       backgroundTextField_.setText(backgroundFileName_);
       prefs_.put(BACKGROUNDFILENAME, backgroundFileName_);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField5 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        flatFieldTextField_ = new javax.swing.JTextField();
        flatFieldButton_ = new javax.swing.JButton();
        backgroundTextField_ = new javax.swing.JTextField();
        backGroundButton_ = new javax.swing.JButton();
        useCheckBox_ = new javax.swing.JCheckBox();

        setTitle("BFCorrector Plugin");
        setLocationByPlatform(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosing(evt);
            }
        });

        jLabel1.setText("Background");

        jLabel2.setText("Flatfield");

        flatFieldTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatFieldTextField_ActionPerformed(evt);
            }
        });
        flatFieldTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                flatFieldTextField_FocusLost(evt);
            }
        });

        flatFieldButton_.setText("...");
        flatFieldButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flatFieldButton_ActionPerformed(evt);
            }
        });

        backgroundTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundTextField_ActionPerformed(evt);
            }
        });

        backGroundButton_.setText("...");
        backGroundButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backGroundButton_ActionPerformed(evt);
            }
        });

        useCheckBox_.setText("Use");
        useCheckBox_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCheckBox_ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(useCheckBox_)
                    .add(backgroundTextField_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(flatFieldTextField_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(flatFieldButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(backGroundButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(flatFieldTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(flatFieldButton_))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(61, 61, 61)
                        .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(backgroundTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backGroundButton_))))
                .addContainerGap(32, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(155, Short.MAX_VALUE)
                .add(useCheckBox_)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  
    /**
     * When window closes, take the opportunity to save settings to Preferences
     * @param evt 
     */
    private void onWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosing
       prefs_.putInt(FRAMEXPOS, (int) getLocation().getX());
       prefs_.putInt(FRAMEYPOS, (int) getLocation().getY());

    }//GEN-LAST:event_onWindowClosing

   private void flatFieldTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flatFieldTextField_ActionPerformed
      processFlatFieldImage(flatFieldTextField_.getText());
   }//GEN-LAST:event_flatFieldTextField_ActionPerformed

   private void flatFieldButton_ActionPerformed(java.awt.event.ActionEvent evt) {
      File f = FileDialogs.openFile(this, "Flatfield image",
              new FileType("MMAcq", "Flatfield image",
              flatfieldFileName_, true, IMAGESUFFIXES));
      if (f != null) {
         processFlatFieldImage(f.getAbsolutePath());
      }
   }
 
   private void backgroundTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundTextField_ActionPerformed
      processBackgroundImage(backgroundTextField_.getText());
   }//GEN-LAST:event_backgroundTextField_ActionPerformed

   private void backGroundButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backGroundButton_ActionPerformed
      File f = FileDialogs.openFile(this, "Background image",
              new FileType("MMAcq", "Bakcground image",
              backgroundFileName_, true, IMAGESUFFIXES));
      if (f != null) {
         processBackgroundImage(f.getAbsolutePath());
      }
   }//GEN-LAST:event_backGroundButton_ActionPerformed

   private void useCheckBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCheckBox_ActionPerformed
      if (useCheckBox_.isSelected()) {
         attachProcessor();
      } else {
         dettachProcessor();
      }
      prefs_.putBoolean(USECHECKBOX, useCheckBox_.isSelected());
   }//GEN-LAST:event_useCheckBox_ActionPerformed

   private void flatFieldTextField_FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_flatFieldTextField_FocusLost
      processFlatFieldImage(flatFieldTextField_.getText());
   }//GEN-LAST:event_flatFieldTextField_FocusLost

 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backGroundButton_;
    private javax.swing.JTextField backgroundTextField_;
    private javax.swing.JButton flatFieldButton_;
    private javax.swing.JTextField flatFieldTextField_;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JCheckBox useCheckBox_;
    // End of variables declaration//GEN-END:variables

}
