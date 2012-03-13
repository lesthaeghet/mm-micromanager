/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProjectorControlForm.java
 *
 * Created on Apr 3, 2010, 12:37:36 PM
 */

package org.micromanager.projector;

import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import org.micromanager.utils.GUIUtils;

/**
 *
 * @author arthur
 */
public class ProjectorControlForm extends javax.swing.JFrame implements OnStateListener {
   private final ProjectorController controller_;
   private final ProjectorPlugin plugin_;

    /** Creates new form ProjectorControlForm */
    public ProjectorControlForm(ProjectorPlugin plugin, ProjectorController controller) {
        initComponents();
        controller_ = controller;
        plugin_ = plugin;
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

      calibrateButton = new javax.swing.JButton();
      onButton = new javax.swing.JButton();
      offButton = new javax.swing.JButton();
      jTabbedPane1 = new javax.swing.JTabbedPane();
      jPanel2 = new javax.swing.JPanel();
      jLabel3 = new javax.swing.JLabel();
      startFrameMDA_ = new javax.swing.JSpinner();
      jButton1 = new javax.swing.JButton();
      repeatCheckBox = new javax.swing.JCheckBox();
      repeatFrameMDA_ = new javax.swing.JSpinner();
      jLabel5 = new javax.swing.JLabel();
      jPanel1 = new javax.swing.JPanel();
      pointAndShootToggleButton = new javax.swing.JToggleButton();
      jLabel1 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      pointAndShootIntervalSpinner = new javax.swing.JSpinner();
      pointAndShootIntervalCheckbox = new javax.swing.JCheckBox();
      jPanel3 = new javax.swing.JPanel();
      jLabel4 = new javax.swing.JLabel();
      jLabel6 = new javax.swing.JLabel();
      setRoiButton = new javax.swing.JButton();
      jButton2 = new javax.swing.JButton();
      roiRepetitionsSpinner = new javax.swing.JSpinner();
      allPixelsButton = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Projector Controls");
      setResizable(false);

      calibrateButton.setText("Calibrate");
      calibrateButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            calibrateButtonActionPerformed(evt);
         }
      });

      onButton.setText("On");
      onButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            onButtonActionPerformed(evt);
         }
      });

      offButton.setText("Off");
      offButton.setSelected(true);
      offButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            offButtonActionPerformed(evt);
         }
      });

      jLabel3.setText("Start Frame");

      jButton1.setText("Attach to Acquisition Engine");
      jButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
         }
      });

      repeatCheckBox.setText("Repeat every");

      jLabel5.setText("frames");

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(29, 29, 29)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel2Layout.createSequentialGroup()
                  .add(repeatCheckBox)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(repeatFrameMDA_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jLabel5))
               .add(jButton1)
               .add(jPanel2Layout.createSequentialGroup()
                  .add(jLabel3)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(startFrameMDA_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(70, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(startFrameMDA_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(repeatCheckBox)
               .add(repeatFrameMDA_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel5))
            .add(13, 13, 13)
            .add(jButton1)
            .addContainerGap(24, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("Multi D Acq.", jPanel2);

      pointAndShootToggleButton.setText("Off");
      pointAndShootToggleButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            pointAndShootToggleButtonActionPerformed(evt);
         }
      });

      jLabel1.setText("Point and shoot mode:");

      jLabel2.setText("ms");

      pointAndShootIntervalSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
         public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
            pointAndShootIntervalSpinnerVetoableChange(evt);
         }
      });

      pointAndShootIntervalCheckbox.setText("Close shutter after:");
      pointAndShootIntervalCheckbox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            pointAndShootIntervalCheckboxActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jLabel1)
               .add(pointAndShootIntervalCheckbox))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(pointAndShootToggleButton)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(pointAndShootIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                  .add(jLabel2)))
            .addContainerGap(62, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(25, 25, 25)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel1)
               .add(pointAndShootToggleButton))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel2)
               .add(pointAndShootIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(pointAndShootIntervalCheckbox))
            .addContainerGap(43, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("Point and Shoot", jPanel1);

      jLabel4.setText("Repeat:");

      jLabel6.setText("times");

      setRoiButton.setText("Set ROI(s)");
      setRoiButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            setRoiButtonActionPerformed(evt);
         }
      });

      jButton2.setText("Go!");
      jButton2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton2ActionPerformed(evt);
         }
      });

      roiRepetitionsSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            roiRepetitionsSpinnerStateChanged(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .add(66, 66, 66)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(setRoiButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jPanel3Layout.createSequentialGroup()
                  .add(jLabel4)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                  .add(roiRepetitionsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                  .add(jLabel6))
               .add(jPanel3Layout.createSequentialGroup()
                  .add(32, 32, 32)
                  .add(jButton2)))
            .addContainerGap(93, Short.MAX_VALUE))
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .add(setRoiButton)
            .add(18, 18, 18)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel4)
               .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(roiRepetitionsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(18, 18, 18)
            .add(jButton2)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("ROIs", jPanel3);

      allPixelsButton.setText("All Pixels");
      allPixelsButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            allPixelsButtonActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(onButton)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(offButton)
                  .add(18, 18, 18)
                  .add(allPixelsButton)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(calibrateButton))
               .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(onButton)
               .add(offButton)
               .add(allPixelsButton)
               .add(calibrateButton))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 41, Short.MAX_VALUE)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void calibrateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrateButtonActionPerformed
       controller_.calibrate();
    }//GEN-LAST:event_calibrateButtonActionPerformed

    private void onButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onButtonActionPerformed
       controller_.turnOn();
       offButton.setSelected(false);
       onButton.setSelected(true);
    }//GEN-LAST:event_onButtonActionPerformed

    private void offButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offButtonActionPerformed
       controller_.turnOff();
       offButton.setSelected(true);
       onButton.setSelected(false);
    }//GEN-LAST:event_offButtonActionPerformed

    private void allPixelsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allPixelsButtonActionPerformed
       controller_.activateAllPixels();
    }//GEN-LAST:event_allPixelsButtonActionPerformed

    private void setRoiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setRoiButtonActionPerformed
       controller_.setRois();
}//GEN-LAST:event_setRoiButtonActionPerformed

    private void pointAndShootIntervalCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointAndShootIntervalCheckboxActionPerformed
       updatePointAndShoot();
}//GEN-LAST:event_pointAndShootIntervalCheckboxActionPerformed

    private void pointAndShootIntervalSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_pointAndShootIntervalSpinnerVetoableChange
       updatePointAndShoot();
}//GEN-LAST:event_pointAndShootIntervalSpinnerVetoableChange

    private void pointAndShootToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointAndShootToggleButtonActionPerformed
       updatePointAndShoot();
}//GEN-LAST:event_pointAndShootToggleButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       controller_.attachToMDA(getSpinnerValue(this.startFrameMDA_)-1,
               this.repeatCheckBox.isSelected(),
               getSpinnerValue(this.repeatFrameMDA_),
               getSpinnerValue(roiRepetitionsSpinner));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       controller_.runPolygons(getSpinnerValue(roiRepetitionsSpinner));
    }//GEN-LAST:event_jButton2ActionPerformed

    private void roiRepetitionsSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_roiRepetitionsSpinnerStateChanged
       updatePointAndShoot();
    }//GEN-LAST:event_roiRepetitionsSpinnerStateChanged

    private int getSpinnerValue(JSpinner spinner) {
       return Integer.parseInt(spinner.getValue().toString());
    }
    public void updatePointAndShoot() {
       pointAndShootToggleButton.setText(pointAndShootToggleButton.isSelected() ? "On" : "Off");
       controller_.setPointAndShootUseInterval(this.pointAndShootIntervalCheckbox.isSelected());
       controller_.setPointAndShootInterval(1000 * Double.parseDouble(this.pointAndShootIntervalSpinner.getValue().toString()));
       controller_.activatePointAndShootMode(pointAndShootToggleButton.isSelected());

    }

   public void dispose() {
      super.dispose();
   }
   
    private void formWindowClosing(java.awt.event.WindowEvent evt) {
       plugin_.dispose();
    }


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton allPixelsButton;
   private javax.swing.JButton calibrateButton;
   private javax.swing.JButton jButton1;
   private javax.swing.JButton jButton2;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JTabbedPane jTabbedPane1;
   private javax.swing.JButton offButton;
   private javax.swing.JButton onButton;
   private javax.swing.JCheckBox pointAndShootIntervalCheckbox;
   private javax.swing.JSpinner pointAndShootIntervalSpinner;
   private javax.swing.JToggleButton pointAndShootToggleButton;
   private javax.swing.JCheckBox repeatCheckBox;
   private javax.swing.JSpinner repeatFrameMDA_;
   private javax.swing.JSpinner roiRepetitionsSpinner;
   private javax.swing.JButton setRoiButton;
   private javax.swing.JSpinner startFrameMDA_;
   // End of variables declaration//GEN-END:variables

   public void turnedOn() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            onButton.setSelected(true);
            offButton.setSelected(false);
         }
      });
   }

   public void turnedOff() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            onButton.setSelected(false);
            offButton.setSelected(true);
         }
      });
   }
}
