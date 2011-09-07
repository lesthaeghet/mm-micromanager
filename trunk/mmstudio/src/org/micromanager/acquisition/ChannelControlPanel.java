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
import org.micromanager.utils.HistogramUtils;
import org.micromanager.utils.MDUtils;
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
   private final MetadataPanel metadataPanel_;

   private double binSize_;
   private boolean autostretch_ = false;
   private boolean rejectOutliers_ = false;
   private boolean logScale_ = false;
   private double fractionToReject_;

   /** Creates new form ChannelControlsPanel */
   public ChannelControlPanel(VirtualAcquisitionDisplay acq, int channelIndex,
           MetadataPanel metadataPanel) {
      initComponents();
      channelIndex_ = channelIndex;
      acq_ = acq;
      hp_ = addHistogramPanel();
      metadataPanel_ = metadataPanel;
      HistogramUtils huu = new HistogramUtils(null);
      fractionToReject_ = huu.getFractionToReject();
      updateChannelSettings();

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
      minLabel = new javax.swing.JLabel();
      maxLabel = new javax.swing.JLabel();
      histMaxLabel = new javax.swing.JLabel();

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
      zoomInButton.setToolTipText("Histogram zoom in");
      zoomInButton.setMaximumSize(new java.awt.Dimension(20, 20));
      zoomInButton.setMinimumSize(new java.awt.Dimension(20, 20));
      zoomInButton.setPreferredSize(new java.awt.Dimension(20, 20));
      zoomInButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            zoomInButtonActionPerformed(evt);
         }
      });

      zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/zoom_out.png"))); // NOI18N
      zoomOutButton.setToolTipText("Histogram zoom out");
      zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            zoomOutButtonActionPerformed(evt);
         }
      });

      minLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      minLabel.setText("min");

      maxLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      maxLabel.setText("max");

      histMaxLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      histMaxLabel.setText("    ");

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(layout.createSequentialGroup()
                  .add(20, 20, 20)
                  .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(22, 22, 22)
                  .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(layout.createSequentialGroup()
                  .add(10, 10, 10)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                        .add(zoomInButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(29, 29, 29)
                        .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(minLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(zoomOutButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(maxLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
            .add(4, 4, 4)
            .add(histogramPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .add(205, 205, 205)
            .add(histMaxLabel))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(channelNameCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(3, 3, 3)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(colorPickerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(fullButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .add(2, 2, 2)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(zoomInButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(autoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(minLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(zoomOutButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(layout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(maxLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
               .add(histogramPanelHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(0, 0, 0)
            .add(histMaxLabel)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
   private javax.swing.JLabel histMaxLabel;
   private javax.swing.JPanel histogramPanelHolder;
   private javax.swing.JLabel maxLabel;
   private javax.swing.JLabel minLabel;
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
            acq_.setChannelColor(channelIndex_, newColor.getRGB(), true);
         } catch (MMScriptException ex) {
            ReportingUtils.logError(ex);
         }
      }
      updateChannelSettings();
   }

   public void setAutostretch(boolean state) {
      if (state && !autostretch_) {
         setAutoRange();
        }
        autostretch_ = state;
   }

   public void setRejectOutliers(boolean v){
      rejectOutliers_ = v;
   }

   public void setLogScale(boolean logScale) {
      logScale_ = logScale;
      updateChannelSettings();
   }

   private void updateChannelVisibility() {
      acq_.setChannelVisibility(channelIndex_, channelNameCheckbox.isSelected());
   }

   private void turnOffAutostretch() {
      metadataPanel_.setAutostretch(false);
      autostretch_ = false;
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
            turnOffAutostretch();
            int max = acq_.getChannelMax(channelIndex_);

            int min = (int) (pos * binSize_);
            if (min > max) {
               max = min;
            }
            setDisplayRange(min, max);
         }

         public void onRightCursor(double pos) {
            turnOffAutostretch();
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
            acq_.setChannelGamma(channelIndex_, gamma, true);
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
         if (type == ImagePlus.GRAY16) {
            try {
               maxValue = 1 << MDUtils.getBitDepth(acq_.getSummaryMetadata());
            } catch (Exception ex) {
               maxValue = 1 << 16;
            }
         }
         return maxValue;
   }

   private void updateBinSize() {
      if (binSize_ == 0) {
         binSize_ = getMaxValue() / 256;
      }
   }

   public void setDisplayRange(int min, int max) {
      acq_.setChannelDisplayRange(channelIndex_,
              min, max, true);
      drawDisplaySettings();
   }

   public final void drawDisplaySettings() {
       int min;
       int max;
       if (autostretch_) {
         if( rejectOutliers_){
            // calculations here are correct but aren't used to update display image
            // until user 'cycles' the autostretch check box.
            // so feature is temporarily disabled.

            // image may have dropped or saturated pixels which should not influence contrast setting, i.e.
				// don't let pixels lying outside 3 sigma influence the automatic contrast setting
            int totalPoints =  acq_.getHyperImage().getWidth() * acq_.getHyperImage().getHeight();
            int[] histogram = acq_.getChannelHistogram(channelIndex_);
            
            if (histogram != null) {
               HistogramUtils hu = new HistogramUtils(histogram, totalPoints, fractionToReject_);
      			min = hu.getMinAfterRejectingOutliers();
               max = hu.getMaxAfterRejectingOutliers();
            } else {
               min = getMin();
               max = getMax();
            }
         } else {
            min = getMin();
            max = getMax();
         }
         acq_.setChannelDisplayRange(channelIndex_, min, max, true);
       }

      hp_.setCursors(acq_.getChannelMin(channelIndex_)/binSize_,
              acq_.getChannelMax(channelIndex_)/binSize_,
              acq_.getChannelGamma(channelIndex_));
      minLabel.setText("min: "+getMin());
      maxLabel.setText("max: "+getMax());
      histMaxLabel.setText(Integer.toString((int) (binSize_ * 256)));
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
         if (logScale_) {
            histogram[i] = histogram[i] > 0 ? (int) (1000 * Math.log(histogram[i])) : 0;
         }
      }

      graphData.setData(histogram);
      return graphData;
   }


   private void setFullRange() {
      int maxValue = getMaxValue();
      turnOffAutostretch();
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


   private int getMin() {

      int[] histogram = acq_.getChannelHistogram(channelIndex_);
      if (histogram != null) {
         int min = 0;
         for (int i = 0; i < histogram.length; ++i) {
            if (histogram[i] != 0 && min == 0) {
               min = i;
            }
         }

         return min;
      } else {
         return 0;
      }
   }

   private int getMax() {
      int[] histogram = acq_.getChannelHistogram(channelIndex_);
      if (histogram != null) {
         int min = 0;
         int max = 0;
         for (int i = 0; i < histogram.length; ++i) {
            if (histogram[i] != 0 && min == 0) {
               min = i;
               max = min;
            }
            if (histogram[i] != 0) {
               max = i;
            }
         }
         return max;
      } else {
         return 0;
      }
   }


   public double getFractionToReject(){
      return fractionToReject_;
   }

   public void setFractionToReject(double v){
      double oldVal = fractionToReject_;
      fractionToReject_ = v;
      if (v != oldVal) {
         updateChannelSettings();
      }

   }

}
