
/**
 * Intelligent Acquisition Frame
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

package org.micromanager.intelligentacquisition;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import java.io.File;
import java.text.ParseException;
import mmcorej.CMMCore;

import java.text.NumberFormat;

import java.util.prefs.Preferences;

import org.micromanager.api.ScriptInterface;

import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.FileDialogs.FileType;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;

import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;
import java.awt.Frame;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.micromanager.MMStudio;
import org.micromanager.api.MMWindow;
import org.micromanager.utils.JavaUtils;

/**
 *
 * @author nico
 */
public class IntelligentAcquisitionFrame extends javax.swing.JFrame {
   private final ScriptInterface gui_;
   private final CMMCore core_;
   private Preferences prefs_;
   
   private NumberFormat nf_;

   private int frameXPos_ = 100;
   private int frameYPos_ = 100;

   private static final String FRAMEXPOS = "FRAMEXPOS";
   private static final String FRAMEYPOS = "FRAMEYPOS";
   private static final String ACQFILENAMEA = "ACQFILENAMEA";
   private static final String ACQFILENAMEB = "ACQFILENAMEB";
   private static final String SCRIPTFILENAME = "SCRIPTFILENAME";
   private static final String ROIWIDTHX = "ROIWDITHX";
   private static final String ROIWIDTHY = "ROIWIDTHY";
   private static final String EXPFIELDSX = "EXPFIELDSX";
   private static final String EXPFIELDSY = "EXPFIELDSY";
   
   private final String[] ACQSUFFIXES = {"xml"};
   private final String[] SCRIPTSUFFIXES = {"bsh", "txt", "ijm"};
   
   private String acqFileNameA_ = "";
   private String acqFileNameB_ = "";
   private String scriptFileName_ = "";
   
   private int explorationX_ = 5;
   private int explorationY_ = 5;
   private long roiWidthX_ = 256;
   private long roiWidthY_ = 256;
   
   private double pixelWidthMicron_ = 0.1;
  
   private String xyStage_ = "";
   
   private AtomicBoolean stop_;


    /** 
    * Constructor
    * 
    * @param gui - Reference to MM script interface
    */
    public IntelligentAcquisitionFrame(ScriptInterface gui) {
       gui_ = gui;
       core_ = gui_.getMMCore();
       nf_ = NumberFormat.getInstance();
       prefs_ = Preferences.userNodeForPackage(this.getClass());

       // Read values from PREFS
       frameXPos_ = prefs_.getInt(FRAMEXPOS, frameXPos_);
       frameYPos_ = prefs_.getInt(FRAMEYPOS, frameYPos_);
       acqFileNameA_ = prefs_.get(ACQFILENAMEA, acqFileNameA_);
       acqFileNameB_ = prefs_.get(ACQFILENAMEB, acqFileNameB_);
       scriptFileName_ = prefs_.get(SCRIPTFILENAME, scriptFileName_); 
       explorationX_ = prefs_.getInt(EXPFIELDSX, explorationX_);
       explorationY_ = prefs_.getInt(EXPFIELDSY, explorationY_);
       roiWidthX_ = prefs_.getLong(ROIWIDTHX, roiWidthX_);
       roiWidthY_ = prefs_.getLong(ROIWIDTHY, roiWidthY_);
       
       initComponents();

       setLocation(frameXPos_, frameYPos_);
       acqTextField1_.setText(acqFileNameA_);
       acqTextField2_.setText(acqFileNameB_);
       scriptTextField_.setText(scriptFileName_);
       expAreaFieldX_.setText(NumberUtils.intToDisplayString(explorationX_));
       expAreaFieldY_.setText(NumberUtils.intToDisplayString(explorationY_));
       roiFieldX_.setText(NumberUtils.longToDisplayString(roiWidthX_));
       roiFieldY_.setText(NumberUtils.longToDisplayString(roiWidthY_));
       
       stop_ = new AtomicBoolean();
       
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
      goButton_ = new javax.swing.JButton();
      acqTextField1_ = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      acqTextField2_ = new javax.swing.JTextField();
      acqSettingsButton1_ = new javax.swing.JButton();
      acqSettingsButton2_ = new javax.swing.JButton();
      jLabel4 = new javax.swing.JLabel();
      scriptTextField_ = new javax.swing.JTextField();
      scriptButton_ = new javax.swing.JButton();
      jLabel5 = new javax.swing.JLabel();
      jLabel6 = new javax.swing.JLabel();
      jLabel7 = new javax.swing.JLabel();
      roiFieldY_ = new javax.swing.JTextField();
      roiFieldX_ = new javax.swing.JTextField();
      helpButton_ = new javax.swing.JButton();
      fullROIButton_ = new javax.swing.JButton();
      halfROIButton_ = new javax.swing.JButton();
      jLabel8 = new javax.swing.JLabel();
      jLabel9 = new javax.swing.JLabel();
      expAreaFieldY_ = new javax.swing.JTextField();
      expAreaFieldX_ = new javax.swing.JTextField();
      jLabel10 = new javax.swing.JLabel();
      stopButton_ = new javax.swing.JButton();
      testButton_ = new javax.swing.JButton();

      setTitle("Intelligent Acquisition");
      setLocationByPlatform(true);
      setResizable(false);
      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
            onWindowClosing(evt);
         }
         public void windowClosed(java.awt.event.WindowEvent evt) {
            formWindowClosed(evt);
         }
      });

      jLabel1.setText("Acquisition Settings");

      goButton_.setText("Go!");
      goButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            goButton_ActionPerformed(evt);
         }
      });

      acqTextField1_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N

      jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel2.setText("Exploration");

      jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel3.setText("Imaging");

      acqTextField2_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N

      acqSettingsButton1_.setText("...");
      acqSettingsButton1_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            acqSettingsButton1_ActionPerformed(evt);
         }
      });

      acqSettingsButton2_.setText("...");
      acqSettingsButton2_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            acqSettingsButton2_ActionPerformed(evt);
         }
      });

      jLabel4.setText("Analysis Macro");

      scriptTextField_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N

      scriptButton_.setText("...");
      scriptButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            scriptButton_ActionPerformed(evt);
         }
      });

      jLabel5.setText("Imaging ROI (pixels)");

      jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel6.setText("X");

      jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel7.setText("Y");

      roiFieldY_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      roiFieldY_.setText("512");
      roiFieldY_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            roiFieldY_ActionPerformed(evt);
         }
      });

      roiFieldX_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      roiFieldX_.setText("512");
      roiFieldX_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            roiFieldX_ActionPerformed(evt);
         }
      });

      helpButton_.setText("Help");
      helpButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            helpButton_ActionPerformed(evt);
         }
      });

      fullROIButton_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      fullROIButton_.setText("Full");
      fullROIButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullROIButton_ActionPerformed(evt);
         }
      });

      halfROIButton_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      halfROIButton_.setText("Half");
      halfROIButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            halfROIButton_ActionPerformed(evt);
         }
      });

      jLabel8.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel8.setText("X");

      jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      jLabel9.setText("Y");

      expAreaFieldY_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      expAreaFieldY_.setText("512");
      expAreaFieldY_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            expAreaFieldY_ActionPerformed(evt);
         }
      });

      expAreaFieldX_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      expAreaFieldX_.setText("512");
      expAreaFieldX_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            expAreaFieldX_ActionPerformed(evt);
         }
      });

      jLabel10.setText("Exploration Area (fields)");

      stopButton_.setText("Stop");
      stopButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            stopButton_ActionPerformed(evt);
         }
      });

      testButton_.setText("Test");
      testButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            testButton_ActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(helpButton_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testButton_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                        .addComponent(stopButton_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(goButton_))
                     .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(layout.createSequentialGroup()
                              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel2)
                                 .addComponent(jLabel3))
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(scriptTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(acqTextField2_, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(acqTextField1_, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(acqSettingsButton1_, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(acqSettingsButton2_, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(scriptButton_, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                           .addComponent(jLabel4)
                           .addComponent(jLabel1)
                           .addComponent(jLabel10)
                           .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                  .addGap(18, 18, 18)
                  .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expAreaFieldX_, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addGap(6, 6, 6)
                        .addComponent(expAreaFieldY_, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roiFieldX_, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addGap(6, 6, 6)
                        .addComponent(roiFieldY_, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(fullROIButton_)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(halfROIButton_)
                  .addContainerGap())))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGap(164, 164, 164)
                  .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGap(9, 9, 9)
                  .addComponent(jLabel1)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(acqTextField1_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(acqSettingsButton1_))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(acqTextField2_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(acqSettingsButton2_))
                  .addGap(18, 18, 18)
                  .addComponent(jLabel4)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(scriptTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(scriptButton_))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(jLabel10)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(expAreaFieldY_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(expAreaFieldX_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel9))
                  .addGap(18, 18, 18)
                  .addComponent(jLabel5)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(halfROIButton_, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(fullROIButton_, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel6)
                     .addComponent(roiFieldY_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(roiFieldX_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel7))
                  .addGap(10, 10, 10)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(goButton_)
                     .addComponent(helpButton_)
                     .addComponent(stopButton_)
                     .addComponent(testButton_))))
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
       try {
         prefs_.putInt(EXPFIELDSX, 
               NumberUtils.displayStringToInt(expAreaFieldX_.getText()));
       } catch (ParseException ex) {}
       try {
         prefs_.putInt(EXPFIELDSY, 
                 NumberUtils.displayStringToInt(expAreaFieldY_.getText()));
       } catch (ParseException ex) {}
       prefs_.put(SCRIPTFILENAME, scriptTextField_.getText());
       prefs_.put(ACQFILENAMEA, acqTextField1_.getText());
       prefs_.put(ACQFILENAMEB, acqTextField2_.getText());

    }//GEN-LAST:event_onWindowClosing

    public void closeWindow() {
       onWindowClosing(null);
    }
    
   private void acqSettingsButton1_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acqSettingsButton1_ActionPerformed
      File f = FileDialogs.openFile(this, "Exploration acquisition settings", 
              new FileType("MMAcq", "Micro-Manager acquisition settings",
                      acqFileNameA_, true, ACQSUFFIXES ) );
      if (f != null) {
         acqFileNameA_ = f.getAbsolutePath();
         acqTextField1_.setText(acqFileNameA_);
      }   
      
      prefs_.put(ACQFILENAMEA, acqFileNameA_);
   }//GEN-LAST:event_acqSettingsButton1_ActionPerformed

   private void acqSettingsButton2_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acqSettingsButton2_ActionPerformed
      File f = FileDialogs.openFile(this, "Imaging acquisition settings",
              new FileType("MMAcq", "Micro-Manager acquisition settings",
              acqFileNameB_, true, ACQSUFFIXES));
      if (f != null) {
         acqFileNameB_ = f.getAbsolutePath();
         acqTextField2_.setText(acqFileNameB_);
      }
      
      prefs_.put(ACQFILENAMEB, acqFileNameB_);     
   }//GEN-LAST:event_acqSettingsButton2_ActionPerformed

   private void scriptButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptButton_ActionPerformed
      File f = FileDialogs.openFile(this, "Analysis script",
              new FileType("Script", "Image analysis script (ImageJ macro or " + 
              "Beanshell script",
              scriptFileName_, true, SCRIPTSUFFIXES));
      if (f != null) {
         scriptFileName_ = f.getAbsolutePath();
         scriptTextField_.setText(scriptFileName_);
      }
      
      prefs_.put(SCRIPTFILENAME, scriptFileName_);
   }//GEN-LAST:event_scriptButton_ActionPerformed

   private void roiFieldX_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roiFieldX_ActionPerformed
      try {
         roiWidthX_ = NumberUtils.displayStringToInt(roiFieldX_.getText());
      } catch (ParseException ex) {
         ReportingUtils.logError(ex);
      }
      prefs_.putLong(ROIWIDTHX, roiWidthX_);
      
   }//GEN-LAST:event_roiFieldX_ActionPerformed

   private void roiFieldY_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roiFieldY_ActionPerformed
      try {
         roiWidthY_ = NumberUtils.displayStringToInt(roiFieldY_.getText());
      } catch (ParseException ex) {
         ReportingUtils.logError(ex);
      } 
      prefs_.putLong(ROIWIDTHY, roiWidthY_);
   }//GEN-LAST:event_roiFieldY_ActionPerformed

   private void expAreaFieldY_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expAreaFieldY_ActionPerformed
      try {
         explorationY_ = NumberUtils.displayStringToInt(expAreaFieldY_.getText());
         prefs_.putInt(EXPFIELDSY, explorationY_);
      } catch (ParseException ex) {
         ReportingUtils.logError(ex);
      }
   }//GEN-LAST:event_expAreaFieldY_ActionPerformed

   private void expAreaFieldX_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expAreaFieldX_ActionPerformed
      try {
         explorationX_ = NumberUtils.displayStringToInt(expAreaFieldX_.getText());
         prefs_.putInt(EXPFIELDSX, explorationX_);
      } catch (ParseException ex) {
         ReportingUtils.logError(ex);
      }
   }//GEN-LAST:event_expAreaFieldX_ActionPerformed

   private void fullROIButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullROIButton_ActionPerformed
      roiWidthX_ = core_.getImageWidth();
      roiWidthY_ = core_.getImageHeight();
      updateROI();
   }//GEN-LAST:event_fullROIButton_ActionPerformed

   private void halfROIButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_halfROIButton_ActionPerformed
      roiWidthX_ = core_.getImageWidth() / 2;
      roiWidthY_ = core_.getImageHeight() / 2;
      updateROI();
   }//GEN-LAST:event_halfROIButton_ActionPerformed

   
   private class RunAcq implements Runnable {
      public RunAcq() {
      }
      @Override
      public void run() {
         try {
            gui_.runAcquisition();
         } catch (MMScriptException ex) {
            ReportingUtils.showError(ex, "Error during acquisition");
         }
      }
   }
   
    
   /**
    * Runs the actual intelligent acquisition
    * @param evt 
    */
   private void goButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButton_ActionPerformed

      // we need to run this code on a seperate thread to keep the EDT 
      // responsive and to make the acquisition succeed
      Thread executeGo = new Thread() {

         @Override
         public void run() {
            
            stop_.set(false);
            
            // Get a number of useful settings from the core
            pixelWidthMicron_ = core_.getPixelSizeUm();
            double imageWidthMicronX = pixelWidthMicron_ * core_.getImageWidth();
            double imageWidthMicronY = pixelWidthMicron_ * core_.getImageHeight();

            long imageIndex = 0;

            xyStage_ = core_.getXYStageDevice();

            // read settings needed to relate stage movement to camera movement

            
            AffineTransform af = null;
            try {
               af = (AffineTransform) JavaUtils.getObjectFromPrefs
                    (Preferences.userNodeForPackage(MMStudio.class), "affine_transform_" + core_.getCurrentPixelSizeConfig(), null);
            } catch (Exception ex) {
            }
            if (af == null) {
               ReportingUtils.logError("No pixel calibration data found, please run the Pixel Calibrator");
            }
            
            /*
            boolean transposeMirorX = false;
            boolean transposeMirorY = false;
            boolean transposeXY = false;
            try {
               transposeMirorX = core_.getProperty(core_.getCameraDevice(),
                       "TransposeMirrorX").equals("1");
               transposeMirorY = core_.getProperty(core_.getCameraDevice(),
                       "TransposeMirrorY").equals("1");
               transposeXY = core_.getProperty(core_.getCameraDevice(),
                       "TransposeXY").equals("1");
            } catch (Exception ex) {
               ReportingUtils.showError("Problem reading transpose settings from camera");
               return;
            }
             */

            // Preload acqFileNameB_ to make sure that it works
            try {
               acqFileNameB_ = acqTextField2_.getText();
               gui_.loadAcquisition(acqFileNameB_);
            } catch (MMScriptException ex) {
               ReportingUtils.showError("Unable to load Imaging Acquisition Settings. "
                       + "Please select a valid file and try again");
               return;
            }

            // load the exploration acq settings, give a second chance if it does not work
            try {
               acqFileNameA_ = acqTextField1_.getText();
               gui_.loadAcquisition(acqFileNameA_);
            } catch (MMScriptException ex) {
               try {
                  acqSettingsButton1_ActionPerformed(null);
                  gui_.loadAcquisition(acqFileNameA_);
               } catch (MMScriptException ex1) {
                  ReportingUtils.showError(ex1, "Failed to load exploration acquisition settings");
                  return;
               }
            }
            
            try {
               explorationX_ = NumberUtils.displayStringToInt(expAreaFieldX_.getText());
            } catch (ParseException ex) {
               ReportingUtils.showError("Failed to parse Number of fields in X");
            }

            try {
               explorationY_ = NumberUtils.displayStringToInt(expAreaFieldY_.getText());
            } catch (ParseException ex) {
               ReportingUtils.showError("Failed to parse Number of fields in Y");
            }

            while (!stop_.get()) {
               // run exploration acquisition
               String expAcq = "";
               try {
                  expAcq = gui_.runAcquisition();
               } catch (MMScriptException e) {
                  ReportingUtils.showError(e, "Exploration acquisition failed");
                  break;
               }

               try {
                  // get x and y coordinates, not sure why
                  double xPos = core_.getXPosition(xyStage_);
                  double yPos = core_.getYPosition(xyStage_);

               } catch (Exception ex) {
                  ReportingUtils.showError(ex, "Failed to read XY stage position");
               }

               scriptFileName_ = scriptTextField_.getText();
               ij.IJ.runMacroFile(scriptFileName_);

               ResultsTable res = ij.measure.ResultsTable.getResultsTable();
               if (res.getCounter() > 0) {
                  try {
                     // X and Y coordinates of object found in microns
                     double xPos = res.getValue("X", 0);
                     double yPos = res.getValue("Y", 0);
                     
                     
                     if (af != null) {
                        
                        // TODO: testing!!!
                        Point2D newStagePos = af.inverseTransform(new Point2D.Double(xPos, yPos), null);
                        core_.setRelativeXYPosition(xyStage_, newStagePos.getX(),
                                newStagePos.getY() );
                        core_.setROI((int) (core_.getImageWidth() / 2 - roiWidthX_ / 2),
                                (int) (core_.getImageHeight() / 2 - roiWidthY_ / 2),
                                (int) roiWidthX_, (int) roiWidthY_);
                     }
                     ReportingUtils.showMessage("Imaging interesting cell at position: "
                             + xPos + ", " + yPos);

                     gui_.loadAcquisition(acqFileNameB_);
                     String goodStuff = gui_.runAcquisition();
                     gui_.closeAcquisitionWindow(goodStuff);
                     if (af != null) {
                        core_.setRelativeXYPosition(xyStage_, -xPos * pixelWidthMicron_, -yPos * pixelWidthMicron_);
                        core_.clearROI();
                     }
                     // org.micromanager.utils.JavaUtils.sleep(200);
                  } catch (Exception ex) {
                     ReportingUtils.showError(ex, "Imaging acquisition failed...");
                  }
               }
               try {
                  // need sleep to ensure that data have been written to disk
                  //gui_.sleep(100);
                  gui_.closeAcquisitionWindow(expAcq);
               } catch (MMScriptException ex) {
                  ReportingUtils.showError(ex, "Failed to close acquisition window");
               }

               imageIndex++;
               int xDirection = 1;

               if (((imageIndex % explorationX_) % 2) == 1) {
                  xDirection = -1;
               }

               try {
                  if ((imageIndex % explorationX_) == 0) {
                     core_.setRelativeXYPosition(xyStage_, 0, imageWidthMicronY);
                  } else {
                     core_.setRelativeXYPosition(xyStage_, xDirection * imageWidthMicronX, 0);
                  }
               } catch (Exception ex) {
                  ReportingUtils.showError(ex, "Problem moving XY Stage");
                  // what to do now???
               }
               
               if (imageIndex >= (explorationX_ * explorationY_) )
                  stop_.set(true);
            }
         }

      };
      
      executeGo.start();

   }//GEN-LAST:event_goButton_ActionPerformed

   private void stopButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButton_ActionPerformed
      stop_.set(true);
      // try to stop ongoing acquisitions here
      gui_.getAcquisitionEngine2010().stop();
   }//GEN-LAST:event_stopButton_ActionPerformed

   private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
       prefs_.putInt(FRAMEXPOS, (int) getLocation().getX());
       prefs_.putInt(FRAMEYPOS, (int) getLocation().getY());
       prefs_.putInt(EXPFIELDSX, explorationX_);
       prefs_.putInt(EXPFIELDSY, explorationY_);
   }//GEN-LAST:event_formWindowClosed

   private void testButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButton_ActionPerformed
      Thread executeTest = new Thread() {

         @Override
         public void run() {

            stop_.set(false);
            // take the active ImageJ image
            ImagePlus siPlus = null;
            try {
               siPlus = IJ.getImage();
            } catch (Exception ex) {
               return;
            }
            MMWindow mw = new MMWindow(siPlus);

            
            ResultsTable outTable = new ResultsTable();
            String outTableName = Terms.RESULTTABLENAME;

            if (!mw.isMMWindow()) {
               // run the script on the current window
               ij.IJ.runMacroFile(scriptFileName_);
               // results should be in results window
            } else { // MMImageWindow
               int nrPositions = mw.getNumberOfPositions();

               for (int p = 1; p <= nrPositions && !stop_.get(); p++) {
                  try {
                     mw.setPosition(p);
                  } catch (MMScriptException ms) {
                     ReportingUtils.showError(ms, "Error setting position in MMWindow");
                  }
                  ij.IJ.runMacroFile(scriptFileName_);
                  ResultsTable res = ij.measure.ResultsTable.getResultsTable();
                  // get results out, stick them in new window that has listeners coupling to image window 
                  if (res.getCounter() > 0) {
                     for (int i = 0; i < res.getCounter(); i++) {
                        double xPos = res.getValue(Terms.X, i);
                        double yPos = res.getValue(Terms.Y, i);
                        outTable.incrementCounter();
                        outTable.addValue(Terms.POSITION, p);
                        outTable.addValue(Terms.X, xPos);
                        outTable.addValue(Terms.Y, yPos);
                     }
                  }
                  outTable.show(outTableName);
               }
            }
            
            // add listeners to our ResultsTable that let user click on row and go to cell that was found
            TextPanel tp;
            TextWindow win;
            Frame frame = WindowManager.getFrame(outTableName);
            if (frame != null && frame instanceof TextWindow && siPlus != null) {
               win = (TextWindow) frame;
               tp = win.getTextPanel();

               // TODO: the following does not work, there is some voodoo going on here
               for (MouseListener ms : tp.getMouseListeners()) {
                  tp.removeMouseListener(ms);
               }
               for (KeyListener ks : tp.getKeyListeners()) {
                  tp.removeKeyListener(ks);
               }

               ResultsListener myk = new ResultsListener(siPlus, outTable, win);
               tp.addKeyListener(myk);
               tp.addMouseListener(myk);
               frame.toFront();
            }

         }
      };
      executeTest.start();

   }//GEN-LAST:event_testButton_ActionPerformed

   class Helper extends Thread{
         Helper(){
            super("Helper");
         }
         @Override
         public void run(){
          try {
               ij.plugin.BrowserLauncher.openURL("http://micro-manager.org/wiki/Intelligent_Acquisition");
            } catch (IOException e1) {
               ReportingUtils.showError(e1);
            }
         }

      }
   
   
   private void helpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButton_ActionPerformed
      Helper h = new Helper();
      h.start();
   }//GEN-LAST:event_helpButton_ActionPerformed

   

   private void updateROI() {
      roiFieldX_.setText(NumberUtils.longToDisplayString(roiWidthX_));
      roiFieldY_.setText(NumberUtils.longToDisplayString(roiWidthY_));
      prefs_.putLong(ROIWIDTHX, roiWidthX_);
      prefs_.putLong(ROIWIDTHY, roiWidthY_);
   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton acqSettingsButton1_;
   private javax.swing.JButton acqSettingsButton2_;
   private javax.swing.JTextField acqTextField1_;
   private javax.swing.JTextField acqTextField2_;
   private javax.swing.JTextField expAreaFieldX_;
   private javax.swing.JTextField expAreaFieldY_;
   private javax.swing.JButton fullROIButton_;
   private javax.swing.JButton goButton_;
   private javax.swing.JButton halfROIButton_;
   private javax.swing.JButton helpButton_;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel7;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JTextField jTextField5;
   private javax.swing.JTextField roiFieldX_;
   private javax.swing.JTextField roiFieldY_;
   private javax.swing.JButton scriptButton_;
   private javax.swing.JTextField scriptTextField_;
   private javax.swing.JButton stopButton_;
   private javax.swing.JButton testButton_;
   // End of variables declaration//GEN-END:variables
}
