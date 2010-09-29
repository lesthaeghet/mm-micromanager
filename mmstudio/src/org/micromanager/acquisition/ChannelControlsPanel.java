/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChannelControlsPanel.java
 *
 * Created on Sep 27, 2010, 1:27:24 PM
 */

package org.micromanager.acquisition;

import java.awt.Color;
import javax.swing.JColorChooser;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class ChannelControlsPanel extends javax.swing.JPanel {
    private final int channelIndex_;
    private final MMVirtualAcquisition acq_;

    /** Creates new form ChannelControlsPanel */
    public ChannelControlsPanel(MMVirtualAcquisition acq, int channelIndex) {
        initComponents();
        channelIndex_ = channelIndex;
        acq_ = acq;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      fullButton = new javax.swing.JButton();
      autoButton = new javax.swing.JButton();
      colorPickerLabel = new javax.swing.JLabel();
      channelNameCheckbox = new javax.swing.JCheckBox();

      setOpaque(false);
      setPreferredSize(new java.awt.Dimension(100, 150));
      setSize(new java.awt.Dimension(100, 150));

      fullButton.setFont(fullButton.getFont().deriveFont((float)9));
      fullButton.setText("Full");
      fullButton.setPreferredSize(new java.awt.Dimension(75, 30));
      fullButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullButtonActionPerformed(evt);
         }
      });

      autoButton.setFont(autoButton.getFont().deriveFont((float)9));
      autoButton.setText("Auto");
      autoButton.setMaximumSize(new java.awt.Dimension(75, 30));
      autoButton.setMinimumSize(new java.awt.Dimension(75, 30));
      autoButton.setPreferredSize(new java.awt.Dimension(75, 30));

      colorPickerLabel.setBackground(new java.awt.Color(255, 102, 51));
      colorPickerLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      colorPickerLabel.setOpaque(true);
      colorPickerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            colorPickerLabelMouseClicked(evt);
         }
      });

      channelNameCheckbox.setSelected(true);
      channelNameCheckbox.setText("Channel");
      channelNameCheckbox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            channelNameCheckboxActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(68, Short.MAX_VALUE))
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap(124, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                  .add(11, 11, 11)
                  .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(13, 13, 13)))
            .add(34, 34, 34))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(channelNameCheckbox)
            .add(0, 0, 0)
            .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(90, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents

    private void fullButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullButtonActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_fullButtonActionPerformed

    private void colorPickerLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorPickerLabelMouseClicked
       editColor();
    }//GEN-LAST:event_colorPickerLabelMouseClicked

    private void channelNameCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelNameCheckboxActionPerformed
       updateChannelVisibility();
    }//GEN-LAST:event_channelNameCheckboxActionPerformed


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton autoButton;
   private javax.swing.JCheckBox channelNameCheckbox;
   private javax.swing.JLabel colorPickerLabel;
   private javax.swing.JButton fullButton;
   // End of variables declaration//GEN-END:variables

   private void editColor() {

      Color newColor = JColorChooser.showDialog(this, "Choose a color for the "
              + acq_.getChannelNames()[channelIndex_] +
              " channel", acq_.getChannelColor(channelIndex_+1));

      try {
         if (newColor != null && acq_ != null)
            acq_.setChannelColor(channelIndex_, newColor.getRGB());
         
      } catch (MMScriptException ex) {
         ReportingUtils.showError(ex);
      }
   }

   private void updateChannelVisibility() {
      acq_.setChannelVisibility(channelIndex_, channelNameCheckbox.isSelected());
   }


}
