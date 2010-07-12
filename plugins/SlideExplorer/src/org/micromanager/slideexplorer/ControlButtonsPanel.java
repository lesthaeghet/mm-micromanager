/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ControlButtons.java
 *
 * Created on Nov 12, 2009, 1:21:28 PM
 */

package org.micromanager.slideexplorer;

import ij.IJ;
import ij.gui.Toolbar;
import java.awt.Color;
import org.micromanager.slideexplorer.Hub.ModeManager;
import org.micromanager.utils.JavaUtils;

/**
 *
 * @author arthur
 */
public class ControlButtonsPanel extends javax.swing.JPanel {
    private final Display display_;


    public ControlButtonsPanel(Display display) {
        initComponents();
        display_ = display;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      navigatorButton = new javax.swing.JButton();
      explorerButton = new javax.swing.JButton();
      fullScreenButton = new javax.swing.JButton();
      configureButton = new javax.swing.JButton();
      mosaicButton = new javax.swing.JButton();
      snapButton = new javax.swing.JButton();
      jComboBox1 = new javax.swing.JComboBox();
      clearROIsButton = new javax.swing.JButton();

      setBackground(new java.awt.Color(255, 255, 255));
      setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
      setFocusable(false);
      setMaximumSize(new java.awt.Dimension(32767, 30));
      setMinimumSize(new java.awt.Dimension(0, 29));
      setPreferredSize(new java.awt.Dimension(600, 30));

      navigatorButton.setText("Navigate");
      navigatorButton.setToolTipText("In Navigate mode, you can move the stage by double-clicking on the slide map.");
      navigatorButton.setFocusable(false);
      navigatorButton.setMaximumSize(new java.awt.Dimension(65, 23));
      navigatorButton.setMinimumSize(new java.awt.Dimension(65, 23));
      navigatorButton.setPreferredSize(new java.awt.Dimension(65, 23));
      navigatorButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            navigatorButtonActionPerformed(evt);
         }
      });

      explorerButton.setText("Explore");
      explorerButton.setToolTipText("In Explore mode, the slide map will be acquired as you zoom and pan.");
      explorerButton.setDoubleBuffered(true);
      explorerButton.setFocusable(false);
      explorerButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
      explorerButton.setMaximumSize(new java.awt.Dimension(79, 23));
      explorerButton.setMinimumSize(new java.awt.Dimension(79, 23));
      explorerButton.setPreferredSize(new java.awt.Dimension(79, 23));
      explorerButton.setSelected(true);
      explorerButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            explorerButtonActionPerformed(evt);
         }
      });

      fullScreenButton.setMnemonic(' ');
      fullScreenButton.setText("Fullscreen");
      fullScreenButton.setToolTipText("Turn on fullscreen mode.");
      fullScreenButton.setFocusable(false);
      fullScreenButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullScreenButtonActionPerformed(evt);
         }
      });

      configureButton.setText("Configure...");
      configureButton.setToolTipText("Configure the slide explorer");
      configureButton.setFocusable(false);
      configureButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            configureButtonActionPerformed(evt);
         }
      });

      mosaicButton.setText("5D Acq");
      mosaicButton.setToolTipText("Acquire tiled 5D images of one or more ROIs.");
      mosaicButton.setFocusable(false);
      mosaicButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            mosaicButtonActionPerformed(evt);
         }
      });

      snapButton.setText("Snap");
      snapButton.setToolTipText("Snap an image at the current location");
      snapButton.setFocusable(false);
      snapButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            snapButtonActionPerformed(evt);
         }
      });

      jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hand", "Points", "Rectangle", "Polygon", "Freehand" }));
      jComboBox1.setToolTipText("Choose the Hand tool for Panning and the other tools for drawing ROIs to create 5D Mosaics.");
      jComboBox1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jComboBox1ActionPerformed(evt);
         }
      });

      clearROIsButton.setText("Clear ROIs");
      clearROIsButton.setToolTipText("Snap an image at the current location");
      clearROIsButton.setFocusable(false);
      clearROIsButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            clearROIsButtonActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(explorerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(navigatorButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(mosaicButton)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(fullScreenButton)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(configureButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(snapButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(clearROIsButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(89, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(explorerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(navigatorButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(mosaicButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(fullScreenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(configureButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(snapButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(clearROIsButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      configureButton.getAccessibleContext().setAccessibleDescription("Configure the slide explorer");
   }// </editor-fold>//GEN-END:initComponents

    private void navigatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navigatorButtonActionPerformed
        if (!navigatorButton.isSelected())  {
            display_.pauseSlideExplorer();
        }

    }//GEN-LAST:event_navigatorButtonActionPerformed

    private void explorerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_explorerButtonActionPerformed
        if (!explorerButton.isSelected()) {
            display_.survey();
        }
    }//GEN-LAST:event_explorerButtonActionPerformed

    private void fullScreenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullScreenButtonActionPerformed
        display_.getWindow().toggleFullscreen();
    }//GEN-LAST:event_fullScreenButtonActionPerformed

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        display_.showConfig();
    }//GEN-LAST:event_configureButtonActionPerformed

    private void mosaicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mosaicButtonActionPerformed
        if (!mosaicButton.isSelected()) {
            display_.pauseSlideExplorer();
            display_.acquireMosaics();
        }
    }//GEN-LAST:event_mosaicButtonActionPerformed

    private void snapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapButtonActionPerformed
        display_.snap();
    }//GEN-LAST:event_snapButtonActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String cmd = jComboBox1.getSelectedItem().toString();
        if(cmd.equals("Hand"))
           IJ.setTool(Toolbar.HAND);
        if(cmd.equals("Rectangle"))
           IJ.setTool(Toolbar.RECTANGLE);
        if(cmd.equals("Points"))
           IJ.setTool(Toolbar.POINT);
        if(cmd.equals("Polygon"))
           IJ.setTool(Toolbar.POLYGON);
        if(cmd.equals("Freehand"))
           IJ.setTool(Toolbar.FREEROI);

        
}//GEN-LAST:event_jComboBox1ActionPerformed

    private void clearROIsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearROIsButtonActionPerformed
       display_.clearRois();
    }//GEN-LAST:event_clearROIsButtonActionPerformed


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton clearROIsButton;
   private javax.swing.JButton configureButton;
   private javax.swing.JButton explorerButton;
   private javax.swing.JButton fullScreenButton;
   private javax.swing.JComboBox jComboBox1;
   private javax.swing.JButton mosaicButton;
   private javax.swing.JButton navigatorButton;
   private javax.swing.JButton snapButton;
   // End of variables declaration//GEN-END:variables


    void updateControls() {

        fullScreenButton.setSelected(display_.getWindow().isFullscreen());

        snapButton.setEnabled(!display_.getWindow().isFullscreen() || JavaUtils.isWindows());

        switch (display_.getMode()) {
            case ModeManager.SURVEY:
                explorerButton.setSelected(true);
                explorerButton.setForeground(Color.RED);
                navigatorButton.setSelected(false);
                navigatorButton.setForeground(Color.BLACK);
                mosaicButton.setSelected(false);
                break;
                
            case ModeManager.MOSAIC5D:
                explorerButton.setSelected(false);
                navigatorButton.setSelected(false);
                mosaicButton.setSelected(true);
                break;

            case ModeManager.NAVIGATE:
                explorerButton.setSelected(false);
                explorerButton.setForeground(Color.BLACK);
                navigatorButton.setSelected(true);
                navigatorButton.setForeground(Color.RED);
                mosaicButton.setSelected(false);
                break;
        }

    }


}
