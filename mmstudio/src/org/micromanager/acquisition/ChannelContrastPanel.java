/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import ij.IJ;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.JPanel;

/**
 *
 * @author arthur
 */
public class ChannelContrastPanel extends JPanel {

   BufferedImage resultBuffer;
   private Graphics2D resultGraphics;
   private Graphics2D displayGraphics;
   BufferedImage scaledPlotBuffer;
   BufferedImage plotBuffer;
   int xmin, xmax, ymin, ymax;
   int currentHandle;
   int margin=10;
   int xl;
   int xr;

   static BufferedImage createBlankRGBBufferedImage(int width, int height, Color color) {
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = bi.createGraphics();
      g.setColor(color);
      g.fillRect(0, 0, width, height);
      return bi;
   }

   static void drawPlot(BufferedImage bi, int[] h, Color color) {
      int hmax = 0;
      for (int i = 0; i < h.length; ++i) {
         hmax = Math.max(hmax, h[i]);
      }
      Graphics2D g = bi.createGraphics();
      g.setColor(color);
      for (int i = 0; i < h.length; ++i) {
         int y = bi.getHeight() * h[i] / hmax;
         g.drawLine(i, 0, i, y);
      }
   }

   static void drawTriangle(Graphics g, int x, int y, boolean flip, Color color) {
      int s = 8;
      if (flip) {
         s = -s;
      }
      int[] xs = {x, x - s, x + s};
      int[] ys = {y, y + s, y + s};
      g.setColor(color);
      g.fillPolygon(xs, ys, 3);
      g.setColor(Color.black);
      g.drawPolygon(xs, ys, 3);
   }

   static void drawLUTHandles(Graphics g, int xmin, int xmax, int ymin, int ymax) {
      g.setColor(Color.black);
      g.drawLine(xmin, ymin, xmax, ymax);
      drawTriangle(g, xmin, ymin, false, Color.black);
      drawTriangle(g, xmax, ymax, true, Color.white);
   }



   void redrawLUTHandles() {
      resultGraphics.drawImage(scaledPlotBuffer, null, null);
      resultGraphics.setColor(Color.black);
      drawLUTHandles(resultGraphics, xmin, xmax, ymin, ymax);
      resultGraphics.drawRect(margin, margin, resultBuffer.getWidth() - 2 * margin, resultBuffer.getHeight() - 2 * margin);
      displayGraphics.drawImage(resultBuffer, null, null);
   }

   void updateLUTHandles(int xclick) {
      if (currentHandle == 0) {
         return;
      }
      int x = clipVal(xclick, xl, xr);
      if (currentHandle == 1) {
         xmin = x;
         if (xmin > xmax) {
            xmax = xmin;
         }
         redrawLUTHandles();
      } else if (currentHandle == 2) {
         xmax = x;
         if (xmin > xmax) {
            xmin = xmax;
         }
         redrawLUTHandles();
      }
   }

   int getMargin(int y) {
      if (y < ymin + 10 && y > ymin) {
         return 1;
      } else if (y < ymax && y > ymax - 8) {
         return 2;
      } else {
         return 0;
      }
   }

   int getLUTHandle(int x, int y) {
      if (x > xmin - 8 && x < xmin + 8 && y < ymin + 8 && y > ymin) {
         return 1;
      } else if (x > xmax - 8 && x < xmax + 8 && y < ymax && y > ymax - 8) {
         return 2;
      } else {
         return 0;
      }
   }

   static int clipVal(int v, int min, int max) {
      return Math.max(min, Math.min(v, max));
   }

   static int[] compressHistogram(int[] h) {
      if (h.length == 256) {
         return h;
      }
      if (h.length == 256 * 256) {
         int[] h2 = new int[256];
         int j = 0;
         for (int i = 0; i < 256; ++i) {
            h2[i] += h[j];
            ++j;
         }
         return h2;
      }
      return null;
   }

   static void rescalePlot(BufferedImage plotBuffer, BufferedImage scaledPlotBuffer, int w, int h, int margin) {
      double wScale = ((double) w - 2 * margin) / plotBuffer.getWidth();
      double hScale = ((double) h - 2 * margin) / plotBuffer.getHeight();
      AffineTransform at = new AffineTransform(wScale, 0, 0, -hScale, margin, h - margin);
      AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
      atop.filter(plotBuffer, scaledPlotBuffer);
   }

   void renderScaledPlot(BufferedImage plotBuffer, BufferedImage scaledPlotBuffer, int[] h, Color color) {
      drawPlot(plotBuffer, h, color);
      rescalePlot(plotBuffer, scaledPlotBuffer, scaledPlotBuffer.getWidth(), scaledPlotBuffer.getHeight(), margin);
   }

   ChannelContrastPanel() {
      plotBuffer = createBlankRGBBufferedImage(256, 100, Color.white);
      scaledPlotBuffer = createBlankRGBBufferedImage(this.getWidth(), this.getHeight(), Color.white);
      resultBuffer = createBlankRGBBufferedImage(this.getWidth(), this.getHeight(), Color.white);
      resultGraphics = resultBuffer.createGraphics();
      currentHandle = 0;

      int xmin = margin;
      int xmax = resultBuffer.getWidth() - margin;
      int ymin = resultBuffer.getHeight() - margin;
      int ymax = margin;

      xl = margin;
      xr = resultBuffer.getWidth() - margin;

      addMouseListener(new MouseAdapter() {

         public void mousePressed(MouseEvent e) {
            currentHandle = getMargin(e.getY());
            updateLUTHandles(e.getX());
         }

         public void mouseReleased(MouseEvent e) {
            currentHandle = 0;
         }
      });

      addMouseMotionListener(new MouseMotionAdapter() {

         public void mouseDragged(MouseEvent e) {
            updateLUTHandles(e.getX());
         }
      });
   }

   public void setHistogram(int[] h) {
      renderScaledPlot(plotBuffer, scaledPlotBuffer, compressHistogram(h), Color.blue);
   }












}
