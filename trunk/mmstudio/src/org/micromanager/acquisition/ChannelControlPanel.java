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
import org.micromanager.graph.GraphData;
import org.micromanager.graph.HistogramPanel;
import org.micromanager.graph.HistogramPanel.CursorListener;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.MathFunctions;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class ChannelControlPanel extends javax.swing.JPanel {

   private final int channelIndex_;
   private final MMVirtualAcquisitionDisplay acq_;
   private final HistogramPanel hp_;
   

   /** Creates new form ChannelControlsPanel */
   public ChannelControlPanel(MMVirtualAcquisitionDisplay acq, int channelIndex) {
      initComponents();
      channelIndex_ = channelIndex;
      acq_ = acq;
      hp_ = addHistogramPanel();
      updateChannelSettings();
      drawDisplaySettings();
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
      histogramPanelHolder = new javax.swing.JPanel();

      setOpaque(false);
      setPreferredSize(new java.awt.Dimension(250, 100));

      fullButton.setFont(fullButton.getFont().deriveFont((float)9));
      fullButton.setText("Full");
      fullButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
      fullButton.setPreferredSize(new java.awt.Dimension(75, 30));
      fullButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullButtonActionPerformed(evt);
         }
      });

      autoButton.setFont(autoButton.getFont().deriveFont((float)9));
      autoButton.setText("Auto");
      autoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      autoButton.setIconTextGap(0);
      autoButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
      autoButton.setMaximumSize(new java.awt.Dimension(75, 30));
      autoButton.setMinimumSize(new java.awt.Dimension(75, 30));
      autoButton.setPreferredSize(new java.awt.Dimension(75, 30));
      autoButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            autoButtonActionPerformed(evt);
         }
      });

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

      histogramPanelHolder.setAlignmentX(0.3F);
      histogramPanelHolder.setPreferredSize(new java.awt.Dimension(0, 100));
      histogramPanelHolder.setLayout(new java.awt.GridLayout(1, 1));

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(layout.createSequentialGroup()
                  .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(0, 0, 0))
               .add(layout.createSequentialGroup()
                  .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .add(9, 9, 9)))
            .add(histogramPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(10, 10, 10)
            .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(43, 43, 43))
         .add(histogramPanelHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      );
   }// </editor-fold>//GEN-END:initComponents

    private void fullButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullButtonActionPerformed
       setFullRange();
    }//GEN-LAST:event_fullButtonActionPerformed

    private void colorPickerLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorPickerLabelMouseClicked
       editColor();
    }//GEN-LAST:event_colorPickerLabelMouseClicked

    private void channelNameCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelNameCheckboxActionPerformed
       updateChannelVisibility();
    }//GEN-LAST:event_channelNameCheckboxActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
       setAutoRange();
    }//GEN-LAST:event_autoButtonActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton autoButton;
   private javax.swing.JCheckBox channelNameCheckbox;
   private javax.swing.JLabel colorPickerLabel;
   private javax.swing.JButton fullButton;
   private javax.swing.JPanel histogramPanelHolder;
   // End of variables declaration//GEN-END:variables

   private void editColor() {

      Color newColor = JColorChooser.showDialog(this, "Choose a color for the "
              + acq_.getChannelNames()[channelIndex_]
              + " channel", acq_.getChannelColor(channelIndex_));

      if (newColor != null && acq_ != null) {
         try {
            acq_.setChannelColor(channelIndex_, newColor.getRGB());
         } catch (MMScriptException ex) {
            ReportingUtils.logError(ex);
         }
      }
      updateChannelSettings();
   }

   public ChannelDisplaySettings getSettings() {
      return acq_.getChannelDisplaySettings(channelIndex_);
   }

   private void updateChannelVisibility() {
      acq_.setChannelVisibility(channelIndex_, channelNameCheckbox.isSelected());
   }

   public final HistogramPanel addHistogramPanel() {
      HistogramPanel hp = new HistogramPanel();
      hp.setMargins(8, 8);
      hp.setTextVisible(false);
      hp.setGridVisible(false);
      hp.setCursors(0,255,1.0);
      histogramPanelHolder.add(hp);

      hp.addCursorListener(new CursorListener() {


         public void onLeftCursor(double pos) {
            int max = acq_.getChannelMax(channelIndex_);

            int min = (int) pos;
            if (min > max) {
               max = min;
            }
            setDisplayRange(min, max);
         }

         public void onRightCursor(double pos) {
            int min = acq_.getChannelMin(channelIndex_);

            int max = (int) pos;
            if (max < min) {
               min = max;
            }
            setDisplayRange(min, max);
         }

         public void onGammaCurve(double gamma) {
            if (gamma == 0)
               return;
            
            if (gamma < 1.15 && gamma > 0.85)
               gamma = 1.0;
            else
               gamma = MathFunctions.clip(0.05, gamma, 20);
            acq_.setChannelGamma(channelIndex_, gamma);
            drawDisplaySettings();
         }
      });

      return hp;
   }


   public final void updateChannelSettings() {
      if (acq_ != null) {
         Color color = acq_.getChannelColor(channelIndex_);
         colorPickerLabel.setBackground(color);

         String [] names = acq_.getChannelNames();
         if (names != null) {
            String name = acq_.getChannelNames()[channelIndex_];
            channelNameCheckbox.setText(name);
         }

         int [] histogram = acq_.getChannelHistogram(channelIndex_);
         hp_.setData(makeGraphData(histogram));
         hp_.setAutoBounds();
         hp_.setTraceStyle(true, color);
         drawDisplaySettings();
      }
   }

   public void setDisplayRange(int min, int max) {
      acq_.setChannelDisplayRange(channelIndex_,
              min, max);
      drawDisplaySettings();
   }

   public final void drawDisplaySettings() {
      ChannelDisplaySettings settings = getSettings();
      hp_.setCursors(settings.min,
              settings.max,
              settings.gamma);
      hp_.repaint();
   }


   private GraphData makeGraphData(int [] rawHistogram) {
      GraphData graphData = new GraphData();
      if (rawHistogram == null) {
         return graphData;
      } // 256 bins

      int[] histogram = new int[256];
      int binSize_ = 1;
      int limit = Math.min(rawHistogram.length / binSize_, 256);
      for (int i = 0; i < limit; i++) {
         histogram[i] = 0;
         for (int j = 0; j < binSize_; j++) {
            histogram[i] += rawHistogram[i * binSize_ + j];
         }
      }

      if (false) {
         for (int i = 0; i < histogram.length; i++) {
            histogram[i] = histogram[i] > 0 ? (int) (1000 * Math.log(histogram[i])) : 0;
         }
      }

      graphData.setData(histogram);
      return graphData;
   }


   private void setFullRange() {
      setDisplayRange(0, 255);
   }

   private void setAutoRange() {
      int [] histogram = acq_.getChannelHistogram(channelIndex_);
      int min = 0;
      int max = 0;
      for (int i=0;i<histogram.length;++i) {
         if (histogram[i] != 0 && min == 0) {
            min = i;
            max = min;
         }
         if (histogram[i] != 0) {
            max = i;
         }
      }
      setDisplayRange(min, max);
   }




}
