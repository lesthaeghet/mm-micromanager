/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PixelCalibratorDialog.java
 *
 * Created on Mar 1, 2010, 10:21:50 AM
 */

package org.micromanager.pixelcalibrator;

import java.awt.Dimension;
import java.awt.Point;
import java.util.prefs.Preferences;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.JavaUtils;

/**
 *
 * @author arthur
 */
public class PixelCalibratorDialog extends javax.swing.JFrame {
   private PixelCalibratorPlugin plugin_;


    /** Creates new form PixelCalibratorDialog */
   PixelCalibratorDialog(PixelCalibratorPlugin plugin) {
      plugin_ = plugin;
      initComponents();
      GUIUtils.recallPosition(this);
   }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      explanationLabel = new javax.swing.JLabel();
      calibrationProgressBar = new javax.swing.JProgressBar();
      startButton = new javax.swing.JButton();
      stopButton = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Pixel Calibrator (BETA)");
      setResizable(false);
      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
            formWindowClosing(evt);
         }
      });

      explanationLabel.setText("<html>This plugin automatically measures size of the default camera's pixels at the sample plane.<br><br>To calibrate:<br><ol><li>Make sure you are using a correctly calibrated motorized xy stage.</li><li>Choose a nonperiodic specimen (e.g., a cell) and adjust your illumination and focus until you obtain crisp, high-contrast images. <li>Press Start (below).</li></ol></html>");

      calibrationProgressBar.setForeground(new java.awt.Color(255, 0, 51));

      startButton.setText("Start");
      startButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            startButtonActionPerformed(evt);
         }
      });

      stopButton.setText("Stop");
      stopButton.setEnabled(false);
      stopButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            stopButtonActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(70, 70, 70)
                  .add(stopButton))
               .add(startButton))
            .add(18, 18, 18)
            .add(calibrationProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(44, 44, 44))
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(explanationLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 363, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(31, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(explanationLabel)
            .add(48, 48, 48)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(stopButton)
                  .add(startButton))
               .add(calibrationProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
      setBounds((screenSize.width-414)/2, (screenSize.height-316)/2, 414, 316);
   }// </editor-fold>//GEN-END:initComponents

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
       plugin_.stopCalibration();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
       plugin_.startCalibration();
    }//GEN-LAST:event_startButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
       GUIUtils.storePosition(this);
       plugin_.dispose();
    }//GEN-LAST:event_formWindowClosing

    public void updateStatus(boolean running, double progress) {
       if (!running) {
          startButton.setEnabled(true);
          stopButton.setEnabled(false);
          calibrationProgressBar.setEnabled(false);
       } else {
          toFront();
          startButton.setEnabled(false);
          stopButton.setEnabled(true);
          calibrationProgressBar.setEnabled(true);
       }
       calibrationProgressBar.setValue((int) (progress*100));
       this.repaint();
    }
    

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JProgressBar calibrationProgressBar;
   private javax.swing.JLabel explanationLabel;
   private javax.swing.JButton startButton;
   private javax.swing.JButton stopButton;
   // End of variables declaration//GEN-END:variables

   public void dispose() {
      GUIUtils.storePosition(this);
      super.dispose();
   }
   
   public void setPlugin(PixelCalibratorPlugin plugin) {
      plugin_ = plugin;
   }


}
