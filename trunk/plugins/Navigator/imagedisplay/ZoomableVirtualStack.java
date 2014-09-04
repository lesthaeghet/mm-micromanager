package imagedisplay;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import acq.Acquisition;
import acq.ExploreAcquisition;
import acq.MultiResMultipageTiffStorage;
import java.awt.Point;
import mmcorej.TaggedImage;
import org.micromanager.imagedisplay.AcquisitionVirtualStack;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.api.TaggedImageStorage;

/**
 * This class acts as an intermediary between display and multiresolution storage.
 * Since the number of pixels in the viewer does not change, this class should track,
 * the zoom level and the part of the larger stitched image that is showing, and
 * deliver appropriate images to the viewer as such
 * @author henrypinkard
 */
public class ZoomableVirtualStack extends AcquisitionVirtualStack {
         
   private int downsampleIndex_ = 0;
   private int displayImageWidth_, displayImageHeight_;
   private int xView_ = 0, yView_ = 0;  //top left pixel of view in current res
   private MultiResMultipageTiffStorage multiResStorage_;
   private int tileWidth_, tileHeight_;
   private Acquisition acquisition_;
   
   public ZoomableVirtualStack(int type, int width, int height, TaggedImageStorage imageCache,
           int nSlices, VirtualAcquisitionDisplay vad, MultiResMultipageTiffStorage multiResStorage,
           int outerPixelBuffer, Acquisition exploreAcq) {
      super(width, height,type, null, imageCache, nSlices, vad);
      multiResStorage_ = multiResStorage;
      //display image could conceivably be bigger than a single FOV, but not smaller
      displayImageWidth_ = width;
      displayImageHeight_ = height;
      tileHeight_ = multiResStorage.getTileHeight();
      tileWidth_ = multiResStorage.getTileWidth();      
      xView_ = -outerPixelBuffer;
      yView_ = -outerPixelBuffer;
      acquisition_ = exploreAcq;
   }  
   
   public int getDownsampleIndex() {
      return downsampleIndex_;
   }
   
   public int getDownsampleFactor() {
      return (int) Math.pow(2, downsampleIndex_);
   }
   
   public void zoom(Point mouseLocation, int numLevels) {
      if (mouseLocation == null) {
         //if mouse not over image zoom to center
         mouseLocation = new Point(displayImageWidth_ /2, displayImageHeight_/2);
      } 
      
      //TODO: add ability to arbitraily zoom out
      //If we haven't already gotten to this low of a resolution, create it
      while (downsampleIndex_ + numLevels >= multiResStorage_.getNumResLevels()) {
         boolean success = multiResStorage_.addLowerResolution();
         if (!success) {
            return;
         }
      }
      
      //keep cursor in same location relative to full res data for fast zooming/unzooming
      if (downsampleIndex_ + numLevels >= 0 && downsampleIndex_ + numLevels < multiResStorage_.getNumResLevels()) {
         //get pixel location in full res image
         int dsFactor = (int) Math.pow(2,downsampleIndex_);    
         int fullResX = (int) (dsFactor*(mouseLocation.x + xView_));
         int fullResY = (int) (dsFactor*(mouseLocation.y + yView_));                          
         downsampleIndex_ += numLevels;
         dsFactor = (int) Math.pow(2, downsampleIndex_);         
         xView_ = (int) (fullResX / dsFactor - mouseLocation.x );
         yView_ = (int) (fullResY / dsFactor - mouseLocation.y );;          
      }    
   }
   
   public void translateView(int dx, int dy) {
      xView_ += dx;
      yView_ += dy;
   }
   
   /**
    * return the absolute pixel coordinate of the image at full resolution
    * @param x x pixel coordinate at current res level
    * @param y y pixel coordinate at current res level
    * @return 
    */
   public Point getAbsoluteFullResPixelCoordinate(int x, int y) {
      //add view offsets and convert to full resolution to get pixel location in full res image
      int fullResX = (int) ((x + xView_) * Math.pow(2, downsampleIndex_));
      int fullResY = (int) ((y + yView_) * Math.pow(2, downsampleIndex_));
      return new Point(fullResX, fullResY);
   }
   
   /**
    * Return tile indices from pixel displayed in viewer
    * @param x x pixel coordinate at current res level
    * @param y y pixel coordinate at current res level
    * @return
    */
   public Point getTileIndicesFromDisplayedPixel(int x, int y) {      
      //add view offsets and convert to full resolution to get pixel location in full res image
      int fullResX = (int) ((x + xView_) * Math.pow(2, downsampleIndex_));
      int fullResY = (int) ((y + yView_) * Math.pow(2, downsampleIndex_));
      int xTileIndex = fullResX / tileWidth_  - (fullResX >= 0 ? 0 : 1);
      int yTileIndex = fullResY / tileHeight_  - (fullResY >= 0 ? 0 : 1);
      return new Point(xTileIndex, yTileIndex);
   }
   

   /**
    * return the pixel location in coordinates at appropriate res level
    * of the top left pixel for the given row/column
    * @param row
    * @param col
    * @return
    */
   public Point getDisplayedPixel(int row, int col) {
      int x = (col * tileWidth_) / getDownsampleFactor() - xView_;
      int y = (row * tileHeight_) / getDownsampleFactor() - yView_;
      return new Point(x,y);
   }
   
   /**
    * Account for zoom and viewing are to get the display image pixel corresponding
    * to the full res image pixel given
    * @param fullImageCoords
    * @return 
    */  
   public Point getDisplayImageCoordsFromFullImageCoords(Point fullImageCoords) {
      return new Point(fullImageCoords.x / getDownsampleFactor() - xView_, fullImageCoords.y / getDownsampleFactor() - yView_);
   }
   
   /**
    * 
    * @param sliceIndex
    * @return 
    */
   public double getZCoordinateOfDisplayedSlice(int displayedSliceIndex, int displayedFrameIndex) {
      return acquisition_.getZCoordinateOfSlice(displayedSliceIndex, displayedFrameIndex);
   }
   
   public int getDisplaySliceIndexFromZCoordinate(double zPos, int displayedFrameIndex) {
      return acquisition_.getDisplaySliceIndexFromZCoordinate(zPos, displayedFrameIndex);
   }
   
   //this method is called to get the tagged image for display purposes only
   //return the zoomed or downsampled image here for fast performance
   @Override
   protected TaggedImage getTaggedImage(int channel, int slice, int frame) {
      //tags and images ultimately get split apart from this function, so it is okay
      //to alter image size and not change tags to reflect that

      //compensate for the possibility of negative slice indices in explore acquisition
      if (acquisition_ instanceof ExploreAcquisition) {
         slice += ((ExploreAcquisition) acquisition_).getLowestSliceIndex();        
      }

      return multiResStorage_.getImageForDisplay(channel, slice, frame, downsampleIndex_,
              xView_, yView_, displayImageWidth_, displayImageHeight_);

   }
}
