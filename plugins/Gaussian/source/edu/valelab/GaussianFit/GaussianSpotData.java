package edu.valelab.GaussianFit;


import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class GaussianSpotData {

   // lock to avoid clashes during access to image data
   public static final Object lockIP = new Object();

   private ImageProcessor ip_ = null;   // ImageProcessor for given spot
   private int frame_;        // frame number in the original stack
   private int channel_;      // channel number in the original stack
   private int slice_;        // slice number in the original stack
   private int position_;     // position number in the original stack
   private int nr_;           // spot index in given image
   private int x_;            // x as found by spotfinder
   private int y_;            // y as found by spotfinder
   private double intensity_; // total intensity expressed in photons
   private double background_;// background expressed in photons (may or may not be corrected for baseline)
   private double xCenter_;      // center of gaussian in nm (image coordinate system)
   private double yCenter_;      // center of gaussian in nm (image coordinate system)
   private double width_;         // width of the gaussian (in nm)
   private double a_;         // shape of the peak, defined as width(long axis) / width (short axis)
   private double theta_;     // shape factor for spot (rotation of assymetric peak)
   private double sigma_;     // Estimate of error in localization based on Web et al. formula
                              // that uses # of photons, background and width of gaussian

   public GaussianSpotData(ImageProcessor ip, int channel, int slice, int frame, int position, int nr, int x, int y) {
      ip_ = ip;
      frame_ = frame;
      channel_ = channel;
      slice_ = slice;
      position_ = position;
      nr_ = nr;
      x_ = x;
      y_ = y;
   }
   
   /**
    * Copy constructor.  Only copies frame, slice, channel, position , and x, and y!
    */
   public GaussianSpotData(GaussianSpotData spot) {
      frame_ = spot.frame_;
      slice_ = spot.slice_;
      channel_ = spot.channel_;
      position_ = spot.position_;
      nr_ = spot.nr_;
      x_ = spot.x_;
      y_ = spot.y_;
   }

   public void setData(double intensity, double background, double xCenter, double yCenter,
           double width, double a, double theta, double sigma) {
      intensity_ = intensity;
      background_ = background;
      xCenter_ = xCenter;
      yCenter_ = yCenter;
      width_ = width;
      a_ = a;
      theta_ = theta;
      sigma_ = sigma;
   }

   public ImageProcessor getImageProcessor() {
      return ip_;
   }
   public void setImageProcessor (ImageProcessor ip) {
      ip_ = ip;
   }
   public int getFrame() {
      return frame_;
   }
   public int getSlice() {
      return slice_;
   }
   public int getChannel() {
      return channel_;
   }
   public int getPosition() {
      return position_;
   }
   public int getNr() {
      return nr_;
   }
   public int getX() {
      return x_;
   }
   public int getY() {
      return y_;
   }
   public double getIntensity() {
      return intensity_;
   }
   public double getBackground() {
      return background_;
   }
   public double getXCenter() {
      return xCenter_;
   }
   public double getYCenter() {
      return yCenter_;
   }
   public double getWidth() {
      return width_;
   }
   public double getA() {
      return a_;
   }
   public double getTheta() {
      return theta_;
   }
   public double getSigma() {
      return sigma_;
   }

   // For performance reasons, it is much better to use the cached version of the processor
   public ImageProcessor getSpotProcessor(ImagePlus siPlus, int halfSize) {
      if (ip_ != null)
         return ip_;
      synchronized(lockIP) {
         Roi spotRoi = new Roi(x_ - halfSize, y_ - halfSize, 2 * halfSize, 2 * halfSize);
         siPlus.setPositionWithoutUpdate(channel_, slice_, frame_);
         siPlus.setRoi(spotRoi, false);
         return siPlus.getProcessor().crop();
      }
   }

   public ImageProcessor getSpotProcessor(ImageProcessor siProc, int halfSize) {
      if (ip_ != null)
         return ip_;
      synchronized(lockIP) {
         Roi spotRoi = new Roi(x_ - halfSize, y_ - halfSize, 2 * halfSize, 2 * halfSize);
         //siProc.setSliceWithoutUpdate(frame_);
         siProc.setRoi(spotRoi);
         return siProc.crop();
      }
   }

   public static ImageProcessor getSpotProcessor(ImageProcessor siProc, int halfSize, int x, int y) {
      synchronized(lockIP) {
         Roi spotRoi = new Roi(x - halfSize, y - halfSize, 2 * halfSize, 2 * halfSize);
         siProc.setRoi(spotRoi);
         return siProc.crop();
      }
   }
}
