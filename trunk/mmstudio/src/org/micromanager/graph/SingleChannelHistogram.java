///////////////////////////////////////////////////////////////////////////////
//FILE:          MetadataPanel.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//                (Based off of a similar class by Nenad Amodaj, 2006)
//
// COPYRIGHT:    University of California, San Francisco, 2012
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package org.micromanager.graph;

import com.swtdesigner.SwingResourceManager;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.LUT;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.json.JSONException;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.acquisition.MetadataPanel;
import org.micromanager.acquisition.VirtualAcquisitionDisplay;
import org.micromanager.api.Histograms;
import org.micromanager.api.ImageCache;
import org.micromanager.graph.HistogramPanel.CursorListener;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.HistogramUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.NumberUtils;

/**
 * Slider and histogram panel for adjusting contrast and brightness.
 * 
 */
public class SingleChannelHistogram extends JPanel implements Histograms, CursorListener {

   private static final long serialVersionUID = 1L;
   private JComboBox histRangeComboBox_;
   private HistogramPanel histogramPanel_;
   private JLabel maxLabel_;
   private JLabel minLabel_;
   private JLabel meanLabel_;
   private JLabel stdDevLabel_;
   private double gamma_ = 1.0;
   private int histMax_;
   private int maxIntensity_;
   private int bitDepth_;
   private double mean_;
   private double stdDev_;
   private int pixelMin_ = 0;
   private int pixelMax_ = 255;
   private double binSize_ = 1;
   private static final int HIST_BINS = 256;
   private int contrastMin_;
   private int contrastMax_;
   private double minAfterRejectingOutliers_;
   private double maxAfterRejectingOutliers_;
   private ContrastPanel contrastPanel_;
   private MetadataPanel mdPanel_;

   public SingleChannelHistogram(MetadataPanel md, ContrastPanel cp) {
      super();
      mdPanel_ = md;
      contrastPanel_ = cp;
      init();
   }

   private void init() {
      this.setLayout(new BorderLayout());
      this.setFont(new Font("", Font.PLAIN, 10));

      histogramPanel_ = new HistogramPanel() {

         public void paint(Graphics g) {
            super.paint(g);
            //For drawing max label
            g.setColor(Color.black);
            g.setFont(new Font("Lucida Grande", 0, 10));
            String label = "" + histMax_;
            g.drawString(label, this.getSize().width - 7 * label.length(), this.getSize().height);
         }
      };
      histogramPanel_.setMargins(8, 10);
      histogramPanel_.setTraceStyle(true, Color.white);
      histogramPanel_.setTextVisible(false);
      histogramPanel_.setGridVisible(false);
      histogramPanel_.addCursorListener(this);
      this.add(histogramPanel_, BorderLayout.CENTER);

      JPanel controls = new JPanel();
      JPanel controlHolder = new JPanel(new BorderLayout());
      controlHolder.add(controls, BorderLayout.PAGE_START);
      this.add(controlHolder, BorderLayout.LINE_START);

      JButton fullScaleButton = new JButton();
      fullScaleButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            fullButtonAction();
         }
      });
      fullScaleButton.setFont(new Font("Arial", Font.PLAIN, 10));
      fullScaleButton.setToolTipText("Set display levels to full pixel range");
      fullScaleButton.setText("Full");

      final JButton autoScaleButton = new JButton();
      autoScaleButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            autoButtonAction();
         }
      });
      autoScaleButton.setFont(new Font("Arial", Font.PLAIN, 10));
      autoScaleButton.setToolTipText("Set display levels to maximum contrast");
      autoScaleButton.setText("Auto");

      minLabel_ = new JLabel();
      minLabel_.setFont(new Font("", Font.PLAIN, 10));
      maxLabel_ = new JLabel();
      maxLabel_.setFont(new Font("", Font.PLAIN, 10));
      meanLabel_ = new JLabel();
      meanLabel_.setFont(new Font("", Font.PLAIN, 10));
      stdDevLabel_ = new JLabel();
      stdDevLabel_.setFont(new Font("", Font.PLAIN, 10));


      JButton zoomInButton = new JButton();
      zoomInButton.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/zoom_in.png"));
      JButton zoomOutButton = new JButton();
      zoomOutButton.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/zoom_out.png"));
      zoomInButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            zoomInAction();
         }
      });
      zoomOutButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            zoomOutAction();
         }
      });
      zoomInButton.setPreferredSize(new Dimension(22, 22));
      zoomOutButton.setPreferredSize(new Dimension(22, 22));


      JPanel p = new JPanel();
      JLabel histRangeLabel = new JLabel("Hist range:");
      histRangeLabel.setFont(new Font("Arial", Font.PLAIN, 10));

      p.add(histRangeLabel);
      p.add(zoomInButton);
      p.add(zoomOutButton);

      histRangeComboBox_ = new JComboBox();
      histRangeComboBox_.setFont(new Font("", Font.PLAIN, 10));
      histRangeComboBox_.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            histRangeComboAction();
         }
      });
      histRangeComboBox_.setModel(new DefaultComboBoxModel(new String[]{
                 "Auto", "4bit (0-15)", "5bit (0-31)", "6bit (0-63)", "7bit (0-127)",
                 "8bit (0-255)", "9bit (0-511)", "10bit (0-1023)", "11bit (0-2047)",
                 "12bit (0-4095)", "13bit (0-8191)", "14bit (0-16383)", "15bit (32767)", "16bit (0-65535)"}));



      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      controls.setLayout(layout);


      JPanel statsPanel = new JPanel(new GridLayout(5, 1));
      statsPanel.add(new JLabel(" "));
      statsPanel.add(minLabel_);
      statsPanel.add(maxLabel_);
      statsPanel.add(meanLabel_);
      statsPanel.add(stdDevLabel_);

      JPanel histZoomLine = new JPanel();
      histZoomLine.add(histRangeLabel);
      histZoomLine.add(zoomInButton);
      histZoomLine.add(zoomOutButton);

      gbc = new GridBagConstraints();
      gbc.gridy = 0;
      gbc.weightx = 0;
      gbc.gridwidth = 2;
      gbc.fill = GridBagConstraints.BOTH;
      controls.add(new JLabel(" "), gbc);

      gbc = new GridBagConstraints();
      gbc.gridy = 1;
      gbc.weightx = 1;
      gbc.ipadx = 4;
      gbc.ipady = 4;
      fullScaleButton.setPreferredSize(new Dimension(60, 15));
      controls.add(fullScaleButton, gbc);

      gbc = new GridBagConstraints();
      gbc.gridy = 1;
      gbc.gridx = 1;
      gbc.weightx = 1;
      gbc.ipadx = 4;
      gbc.ipady = 4;
      autoScaleButton.setPreferredSize(new Dimension(60, 15));
      controls.add(autoScaleButton, gbc);

      gbc = new GridBagConstraints();
      gbc.gridy = 2;
      gbc.weightx = 1;
      gbc.gridwidth = 2;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      controls.add(histZoomLine, gbc);

      gbc = new GridBagConstraints();
      gbc.gridy = 3;
      gbc.weightx = 1;
      gbc.gridwidth = 2;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      controls.add(histRangeComboBox_, gbc);

      gbc = new GridBagConstraints();
      gbc.gridy = 4;
      gbc.weightx = 1;
      gbc.gridwidth = 2;

      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      controls.add(statsPanel, gbc);
   }

   private void autoButtonAction() {
      autostretch();
      mdPanel_.drawWithoutUpdate();
   }

   private void fullButtonAction() {
      setFullScale();
      mdPanel_.drawWithoutUpdate();
   }

   public void setLogScale() {
      ImagePlus img = mdPanel_.getCurrentImage();
      if (img != null) {
         calcAndDisplayHistAndStats(img, true);
      }
   }

   private void histRangeComboAction() {
      setHistMaxAndBinSize();
      ImagePlus img = mdPanel_.getCurrentImage();
      if (img != null) {
         calcAndDisplayHistAndStats(img, true);
      }
   }

   public void rejectOutliersChangeAction() {
      calcAndDisplayHistAndStats(mdPanel_.getCurrentImage(), true);
      autoButtonAction();
   }

   public void autoscaleAllChannels() {
      autoButtonAction();
   }

   public void applyLUTToImage(ImagePlus img, ImageCache cache) {
      if (img == null) {
         return;
      }
      ImageProcessor ip = img.getProcessor();
      if (ip == null) {
         return;
      }

      double maxValue = 255.0;
      byte[] r = new byte[256];
      byte[] g = new byte[256];
      byte[] b = new byte[256];
      for (int i = 0; i < 256; i++) {
         double val = Math.pow((double) i / maxValue, gamma_) * (double) maxValue;
         r[i] = (byte) val;
         g[i] = (byte) val;
         b[i] = (byte) val;
      }
      //apply gamma and contrast to image
      ip.setColorModel(new LUT(8, 256, r, g, b));    //doesnt explicitly redraw
      ip.setMinAndMax(contrastMin_, contrastMax_);   //doesnt explicitly redraw

      saveDisplaySettings(cache);

      updateHistogram();
   }

   public void saveDisplaySettings(ImageCache cache) {
      int histMax = histRangeComboBox_.getSelectedIndex() == 0 ? histMax = -1 : histMax_;
      cache.storeChannelDisplaySettings(0, (int) contrastMin_, (int) contrastMax_, gamma_, histMax);
   }

   public void setChannelHistogramDisplayMax(int channelIndex, int histMax) {
      if (channelIndex != 0) {
         return;
      }
      int index = (int) (histMax == -1 ? 0 : Math.ceil(Math.log(histMax) / Math.log(2)) - 3);
      histRangeComboBox_.setSelectedIndex(index);
   }

   private void updateHistogram() {
      histogramPanel_.setCursors(contrastMin_ / binSize_, contrastMax_ / binSize_, gamma_);
      histogramPanel_.repaint();
   }

   private void zoomInAction() {
      int selected = histRangeComboBox_.getSelectedIndex();
      if (selected == 0) {
         selected = bitDepth_ - 3;
      }
      if (selected != 1) {
         selected--;
      }
      histRangeComboBox_.setSelectedIndex(selected);
   }

   private void zoomOutAction() {
      int selected = histRangeComboBox_.getSelectedIndex();
      if (selected == 0) {
         selected = bitDepth_ - 3;
      }
      if (selected < histRangeComboBox_.getModel().getSize() - 1) {
         selected++;
      }
      histRangeComboBox_.setSelectedIndex(selected);
   }

   private void setHistMaxAndBinSize() {
      int bits = histRangeComboBox_.getSelectedIndex() + 3;
      if (bits == 3) {
         histMax_ = maxIntensity_;
      } else {
         histMax_ = (int) (Math.pow(2, bits) - 1);
      }
      binSize_ = ((double) (histMax_ + 1)) / ((double) HIST_BINS);
      updateHistogram();

      ImagePlus img = mdPanel_.getCurrentImage();
      if (img == null || VirtualAcquisitionDisplay.getDisplay(img) == null) {
         return;
      }
      ImageCache cache = VirtualAcquisitionDisplay.getDisplay(img).getImageCache();
      saveDisplaySettings(cache);
   }

   //Calculates autostretch, doesnt apply or redraw
   public void autostretch() {
      contrastMin_ = pixelMin_;
      contrastMax_ = pixelMax_;
      if (pixelMin_ == pixelMax_) {
         if (pixelMax_ > 0) {
            contrastMin_--;
         } else {
            contrastMax_++;
         }
      }
      if (contrastPanel_.getRejectOutliers()) {
         if (contrastMin_ < minAfterRejectingOutliers_) {
            if (0 < minAfterRejectingOutliers_) {
               contrastMin_ = (int) minAfterRejectingOutliers_;
            }
         }
         if (maxAfterRejectingOutliers_ < contrastMax_) {
            contrastMax_ = (int) maxAfterRejectingOutliers_;
         }
         if (contrastMax_ <= contrastMin_) {
            if (contrastMax_ > 0) {
               contrastMin_ = contrastMax_ - 1;
            } else {
               contrastMax_ = contrastMin_ + 1;
            }
         }
      }
   }

   private void setFullScale() {
      setHistMaxAndBinSize();
      contrastPanel_.disableAutostretch();
      contrastMin_ = 0;
      contrastMax_ = histMax_;
   }

   private void loadContrastSettings(ImageCache cache) {
      contrastMax_ = cache.getChannelMax(0);
      if (contrastMax_ < 0) {
         contrastMax_ = maxIntensity_;
      }
      contrastMin_ = cache.getChannelMin(0);
      gamma_ = cache.getChannelGamma(0);

      int histMax = cache.getChannelHistogramMax(0);
      if (histMax != -1) {
         int index = (int) (Math.ceil(Math.log(histMax) / Math.log(2)) - 3);
         histRangeComboBox_.setSelectedIndex(index);
      }
   }

   public void imageChanged(ImagePlus img, ImageCache cache, boolean drawHist, boolean slowHistUpdate) {
      if (slowHistUpdate) {
         calcAndDisplayHistAndStats(img, drawHist);
         if (contrastPanel_.getAutostretch()) {
            autostretch();
         }
         applyLUTToImage(img, cache);
      }
   }

   public void displayChanged(ImagePlus img, ImageCache cache) {
      try {
         VirtualAcquisitionDisplay vad = VirtualAcquisitionDisplay.getDisplay(mdPanel_.getCurrentImage());
         bitDepth_ = MDUtils.getBitDepth(vad.getSummaryMetadata());
      } catch (JSONException ex) {
         ReportingUtils.logError("BitDepth not in summary metadata");
         bitDepth_ = 16;
      }
      maxIntensity_ = (int) (Math.pow(2, bitDepth_) - 1);

      setHistMaxAndBinSize();
      calcAndDisplayHistAndStats(img, true);
      loadContrastSettings(cache);
      if (contrastPanel_.getAutostretch()) {
         autostretch();
      }
      mdPanel_.drawWithoutUpdate(img);
   }

   public void calcAndDisplayHistAndStats(ImagePlus img, boolean drawHist) {
      if (img != null && img.getProcessor() != null) {
         int[] rawHistogram = img.getProcessor().getHistogram();
         int imgWidth = img.getWidth();
         int imgHeight = img.getHeight();
         if (contrastPanel_.getRejectOutliers()) {
            // todo handle negative values
            maxAfterRejectingOutliers_ = rawHistogram.length;
            // specified percent of pixels are ignored in the automatic contrast setting
            int totalPoints = imgHeight * imgWidth;
            HistogramUtils hu = new HistogramUtils(rawHistogram, totalPoints, contrastPanel_.getIgnorePercent());
            minAfterRejectingOutliers_ = hu.getMinAfterRejectingOutliers();
            maxAfterRejectingOutliers_ = hu.getMaxAfterRejectingOutliers();
         }
         GraphData histogramData = new GraphData();

         pixelMin_ = -1;
         pixelMax_ = 0;
         mean_ = 0;

         int numBins = (int) Math.min(rawHistogram.length / binSize_, HIST_BINS);
         int[] histogram = new int[HIST_BINS];
         int total = 0;
         for (int i = 0; i < numBins; i++) {
            histogram[i] = 0;
            for (int j = 0; j < binSize_; j++) {
               int rawHistIndex = (int) (i * binSize_ + j);
               int rawHistVal = rawHistogram[rawHistIndex];
               histogram[i] += rawHistVal;
               if (rawHistVal > 0) {
                  pixelMax_ = rawHistIndex;
                  if (pixelMin_ == -1) {
                     pixelMin_ = rawHistIndex;
                  }
                  mean_ += rawHistIndex * rawHistVal;
               }
            }
            total += histogram[i];
            if (contrastPanel_.getLogHist()) {
               histogram[i] = histogram[i] > 0 ? (int) (1000 * Math.log(histogram[i])) : 0;
            }
         }
         mean_ /= imgWidth * imgHeight;

         // work around what is apparently a bug in ImageJ
         if (total == 0) {
            if (img.getProcessor().getMin() == 0) {
               histogram[0] = imgWidth * imgHeight;
            } else {
               histogram[numBins - 1] = imgWidth * imgHeight;
            }
         }

         //need to recalc mean if hist display mode isnt auto
         if (histRangeComboBox_.getSelectedIndex() != -1) {
            mean_ = 0;
            for (int i = 0; i < rawHistogram.length; i++) {
               mean_ += i * rawHistogram[i];
            }
            mean_ /= imgWidth * imgHeight;
         }
         if (drawHist) {
            stdDev_ = 0;
            if (histRangeComboBox_.getSelectedIndex() != -1) {
               pixelMin_ = rawHistogram.length - 1;
            }
            for (int i = rawHistogram.length - 1; i >= 0; i--) {
               if (histRangeComboBox_.getSelectedIndex() != -1) {
                  if (rawHistogram[i] > 0 && i < pixelMin_) {
                     pixelMin_ = i;
                  }
                  if (rawHistogram[i] > 0 && i > pixelMax_) {
                     pixelMax_ = i;
                  }
               }

               for (int j = 0; j < rawHistogram[i]; j++) {
                  stdDev_ += (i - mean_) * (i - mean_);
               }

            }
            stdDev_ = Math.sqrt(stdDev_ / (imgWidth * imgHeight));
            //Draw histogram and stats
            histogramData.setData(histogram);
            histogramPanel_.setData(histogramData);
            histogramPanel_.setAutoScale();

            maxLabel_.setText("Max: " + NumberUtils.intToDisplayString((int) pixelMax_));
            minLabel_.setText("Min: " + NumberUtils.intToDisplayString((int) pixelMin_));
            meanLabel_.setText("Mean: " + NumberUtils.intToDisplayString((int) mean_));
            stdDevLabel_.setText("Std Dev: " + NumberUtils.intToDisplayString((int) stdDev_));

            histogramPanel_.repaint();
         }
      }
   }

   public void setChannelContrast(int channelIndex, int min, int max, double gamma) {
      if (channelIndex != 0) {
         return;
      }
      contrastMax_ = max;
      contrastMin_ = min;
      gamma_ = gamma;
   }

   public void onLeftCursor(double pos) {
      if (contrastPanel_.getAutostretch()) {
         contrastPanel_.disableAutostretch();
      }
      contrastMin_ = (int) (Math.max(0, pos) * binSize_);
      if (contrastMin_ >= maxIntensity_) {
         contrastMin_ = maxIntensity_ - 1;
      }
      if (contrastMax_ < contrastMin_) {
         contrastMax_ = contrastMin_ + 1;
      }
      mdPanel_.drawWithoutUpdate();

   }

   public void onRightCursor(double pos) {
      if (contrastPanel_.getAutostretch()) {
         contrastPanel_.disableAutostretch();
      }

      contrastMax_ = (int) (Math.min(255, pos) * binSize_);
      if (contrastMin_ > contrastMax_) {
         contrastMin_ = contrastMax_;
      }
      mdPanel_.drawWithoutUpdate();
   }

   public void onGammaCurve(double gamma) {
      if (gamma != 0) {
         if (gamma > 0.9 & gamma < 1.1) {
            gamma_ = 1;
         } else {
            gamma_ = gamma;
         }
         mdPanel_.drawWithoutUpdate();
      }
   }

   public void setupChannelControls(ImageCache cache) {
   }

   public ContrastSettings getChannelContrastSettings(int channel) {
      if (channel != 0) {
         return null;
      }
      return new ContrastSettings(contrastMin_, contrastMax_, gamma_);
   }
}