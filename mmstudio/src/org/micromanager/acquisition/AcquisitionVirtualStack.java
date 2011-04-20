/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.image.ColorModel;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 * This stack class provides the ImagePlus with images from the MMImageCache.
 * 
 */
public class AcquisitionVirtualStack extends ij.VirtualStack {
   final private MMImageCache imageCache_;
   final private VirtualAcquisitionDisplay acq_;
   final protected int width_, height_, type_;
   private int nSlices_;
   private int positionIndex_ = 0;

   public AcquisitionVirtualStack(int width, int height, int type,
           ColorModel cm, MMImageCache imageCache, int nSlices,
           VirtualAcquisitionDisplay acq) {
      super(width, height, cm, "");
      imageCache_ = imageCache;
      width_ = width;
      height_ = height;
      nSlices_ = nSlices;
      acq_ = acq;
      type_ = type;
   }

   public void setPositionIndex(int pos) {
      positionIndex_ = pos;
   }

   public int getPositionIndex() {
      return positionIndex_;
   }

   public VirtualAcquisitionDisplay getVirtualAcquisitionDisplay() {
      return acq_;
   }

   public void setSize(int size) {
      nSlices_ = size;
   }

   public MMImageCache getCache() {
      return imageCache_;
   }

   public TaggedImage getTaggedImage(int flatIndex) {
      try {
         int[] pos;
         // If we don't have the ImagePlus yet, then we need to assume
         // we are on the very first image.
            ImagePlus imagePlus = acq_.getImagePlus();
         int nSlices;
         if (imagePlus == null) {
            pos = new int [] {1, 1, 1};
            nSlices = 1;
         } else {
            pos = imagePlus.convertIndexToPosition(flatIndex);
            nSlices = imagePlus.getNSlices();
         }
         int chanIndex = acq_.grayToRGBChannel(pos[0]-1);
         TaggedImage img ;
         int frame = pos[2] - 1;
         int slice = pos[1] - 1;
         
         do {
            int sliceSearchIndex = 0;
            do {
               img = imageCache_.getImage(chanIndex,
                       (nSlices + slice - sliceSearchIndex) % nSlices,
                       frame, positionIndex_);
               ++sliceSearchIndex;
            } while (img == null && sliceSearchIndex < nSlices && frame < imageCache_.lastAcquiredFrame());
            --frame;
         } while (img == null && frame >= 0  && frame < (imageCache_.lastAcquiredFrame()-1));
         return img;
      } catch (Exception e) {
         ReportingUtils.logError(e);
         return null;
      }
   }

   @Override
   public Object getPixels(int flatIndex) {
      Object pixels = null;
      try {
         TaggedImage image = getTaggedImage(flatIndex);
         if (image == null)
            pixels = ImageUtils.makeProcessor(type_, width_, height_).getPixels();
         else if(MDUtils.isGRAY(image)) {
            pixels = image.pix;
         } else if (MDUtils.isRGB32(image)) {
            pixels = ImageUtils.singleChannelFromRGB32((byte []) image.pix, (flatIndex-1) % 3);
         } else if (MDUtils.isRGB64(image)) {
            pixels = ImageUtils.singleChannelFromRGB64((short []) image.pix, (flatIndex-1) % 3);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }

      return pixels;
   }

   @Override
   public ImageProcessor getProcessor(int flatIndex) {
      return ImageUtils.makeProcessor(type_, width_, height_, getPixels(flatIndex));
   }

   @Override
   public int getSize() {
      return acq_.getStackSize();
   }

   @Override
   public String getSliceLabel(int n) {
      TaggedImage img = getTaggedImage(n);
      if (img == null)
         return "";
      JSONObject md = img.tags;
      try {
         return md.get("Acquisition-PixelSizeUm") + " um/px";
         //return MDUtils.getChannelName(md) + ", " + md.get("Acquisition-ZPositionUm") + " um(z), " + md.get("Acquisition-TimeMs") + " s";
      } catch (Exception ex) {
         return "";
      }
   }



}
