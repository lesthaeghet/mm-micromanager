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

import ij.ImagePlus;
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
   private final VirtualAcquisitionDisplay acq_;
   private final HistogramPanel hp_;
   private double binSize_;
   

   /** Creates new form ChannelControlsPanel */
   public ChannelControlPanel(VirtualAcquisitionDisplay acq, int channelIndex) {
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
      zoomInButton = new javax.swing.JButton();
      zoomOutButton = new javax.swing.JButton();

      setOpaque(false);
      setPreferredSize(new java.awt.Dimension(250, 100));

      fullButton.setFont(fullButton.getFont().deriveFont((float)9));
      fullButton.setText("Full");
      fullButton.setToolTipText("Stretch the display gamma curve over the full pixel range");
      fullButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
      fullButton.setPreferredSize(new java.awt.Dimension(75, 30));
      fullButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fullButtonActionPerformed(evt);
         }
      });

      autoButton.setFont(autoButton.getFont().deriveFont((float)9));
      autoButton.setText("Auto");
      autoButton.setToolTipText("Align the display gamma curve with minimum and maximum measured intensity values");
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
      colorPickerLabel.setToolTipText("Change the color for displaying this channel");
      colorPickerLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      colorPickerLabel.setOpaque(true);
      colorPickerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            colorPickerLabelMouseClicked(evt);
         }
      });

      channelNameCheckbox.setSelected(true);
      channelNameCheckbox.setText("Channel");
      channelNameCheckbox.setToolTipText("Show/hide this channel in the multi-dimensional viewer");
      channelNameCheckbox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            channelNameCheckboxActionPerformed(evt);
         }
      });

      histogramPanelHolder.setToolTipText("Adjust the brightness and contrast by dragging triangles at top and bottom. Change the gamma by dragging the curve. (These controls only change display, and do not edit the image data.)");
      histogramPanelHolder.setAlignmentX(0.3F);
      histogramPanelHolder.setPreferredSize(new java.awt.Dimension(0, 100));
      histogramPanelHolder.setLayout(new java.awt.GridLayout(1, 1));

      zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/zoom_in.png"))); // NOI18N
      zoomInButton.setToolTipText("Zoom in the histogram");
      zoomInButton.setMaximumSize(new java.awt.Dimension(20, 20));
      zoomInButton.setMinimumSize(new java.awt.Dimension(20, 20));
      zoomInButton.setPreferredSize(new java.awt.Dimension(20, 20));
      zoomInButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            zoomInButtonActionPerformed(evt);
         }
      });

      zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/zoom_out.png"))); // NOI18N
      zoomOutButton.setToolTipText("Zoom out the histogram");
      zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            zoomOutButtonActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(layout.createSequentialGroup()
                  .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(4, 4, 4))
               .add(layout.createSequentialGroup()
                  .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(layout.createSequentialGroup()
                        .add(zoomInButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(zoomOutButton, 0, 20, Short.MAX_VALUE))
                     .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .add(18, 18, 18)))
            .add(histogramPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
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
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(zoomInButton, 0, 21, Short.MAX_VALUE)
               .add(zoomOutButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, Short.MAX_VALUE)))
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

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
       binSize_ = Math.max(binSize_ / 2, 1./8);
       updateChannelSettings();
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
       binSize_ = Math.min(binSize_ * 2, 256);
       updateChannelSettings();
    }//GEN-LAST:event_zoomOutButtonActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton autoButton;
   private javax.swing.JCheckBox channelNameCheckbox;
   private javax.swing.JLabel colorPickerLabel;
   private javax.swing.JButton fullButton;
   private javax.swing.JPanel histogramPanelHolder;
   private javax.swing.JButton zoomInButton;
   private javax.swing.JButton zoomOutButton;
   // End of variables declaration//GEN-END:variables

   private void editColor() {
      String name = "selected";
      if (acq_.getChannelNames() != null) {
         name = acq_.getChannelNames()[channelIndex_];
      }
      Color newColor = JColorChooser.showDialog(this, "Choose a color for the "
              + name
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
      //hp.setCursors(0,255,1.0);
      histogramPanelHolder.add(hp);
      updateBinSize();
      
      hp.addCursorListener(new CursorListener() {


         public void onLeftCursor(double pos) {
            int max = acq_.getChannelMax(channelIndex_);

            int min = (int) (pos * binSize_);
            if (min > max) {
               max = min;
            }
            setDisplayRange(min, max);
         }

         public void onRightCursor(double pos) {
            int min = acq_.getChannelMin(channelIndex_);

            int max = (int) (pos * binSize_);
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
         if (names != null && channelIndex_ < names.length) {
            channelNameCheckbox.setText(names[channelIndex_]);
         }

         int [] histogram = acq_.getChannelHistogram(channelIndex_);
         hp_.setData(makeGraphData(histogram));
         hp_.setAutoBounds();
         hp_.setTraceStyle(true, color);
         drawDisplaySettings();
      }
   }

   private int getMaxValue() {
         int type = acq_.getImagePlus().getType();
         int maxValue = 0;
         if (type == ImagePlus.GRAY8)
            maxValue = 1 << 8;
         if (type == ImagePlus.GRAY16)
            maxValue = 1 << 16;
         return maxValue;
   }

   private void updateBinSize() {
      if (binSize_ == 0) {
         binSize_ = getMaxValue() / 256;
      }
   }

   public void setDisplayRange(int min, int max) {
      acq_.setChannelDisplayRange(channelIndex_,
              min, max);
      drawDisplaySettings();
   }

   public final void drawDisplaySettings() {
      ChannelDisplaySettings settings = getSettings();
      hp_.setCursors(settings.min/binSize_,
              settings.max/binSize_,
              settings.gamma);
      hp_.repaint();
   }


   private GraphData makeGraphData(int [] rawHistogram) {
      GraphData graphData = new GraphData();
      if (rawHistogram == null) {
         return graphData;
      }

      updateBinSize();

      int[] histogram = new int[256]; // 256 bins
      int limit = Math.min((int) (rawHistogram.length / binSize_), 256);
      for (int i = 0; i < limit; i++) {
         histogram[i] = 0;
         for (int j = 0; j < binSize_; j++) {
            histogram[i] += rawHistogram[(int) (i * binSize_) + j];
         }
      }

      graphData.setData(histogram);
      return graphData;
   }


   private void setFullRange() {
      int maxValue = getMaxValue();
      setDisplayRange(0, maxValue);
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
