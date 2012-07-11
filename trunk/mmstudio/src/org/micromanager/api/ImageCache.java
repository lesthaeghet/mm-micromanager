/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.micromanager.api;

import java.awt.Color;
import java.util.Set;
import org.json.JSONObject;

/**
 * An interface, implemented by MMImageCache. See also TaggedImageStorage.
 * The ImageCache is expected to use another TaggedImageStorage for actual
 * storage. It contains various helper functions for interrogating the
 * stored data set.
 */
public interface ImageCache extends TaggedImageStorage {

   /*
    * Adds the provided cache listener. The listener will be notified
    * whenever an image is added or finished() has been called.
    */
   void addImageCacheListener(ImageCacheListener l);

   /*
    * Get a list of keys that are not identical for every TaggedImage.
    * The Set returns changes over time as new images are loaded
    * or received.
    */
   Set<String> getChangingKeys();

   /*
    * Gets the overal comment string for this data set.
    */
   String getComment();

   /*
    * Gets a list of image storage listeners.
    */
   ImageCacheListener[] getImageStorageListeners();

   /*
    * Returns the image tags for the last received image.
    */
   JSONObject getLastImageTags();

   /*
    * Removes an imageCacheListener so that it will no longer be notified
    * of relevant events.
    */
   void removeImageStorageListener(ImageCacheListener l);

   /*
    * Save a new copy of a TaggedImage data set, stored in a TaggedImageStorage
    * object. The new data set will be used by this cache in the future.
    */
   void saveAs(TaggedImageStorage newImageFileManager);

   /*
    * Save a new copy of a TaggedImage data set, stored in a TaggedImageStorage
    * object. The data set will be use by this cache in the future if
    * moveToNewStorage is true.
    */
   void saveAs(TaggedImageStorage newImageFileManager, boolean moveToNewStorage);

   /*
    * Set the data set's comment string for this image cache.
    */
   void setComment(String text);

   /*
    * Set the comment string for an individual image. The image is specified
    * by indices given in tags.
    */
   void setImageComment(String comment, JSONObject tags);

   /*
    * Returns the image comment for a particular image. The image is specified
    * by indices given in tags.
    */
   String getImageComment(JSONObject tags);

   /*
    * Store the display settings for a particular channel.
    * @channelIndex - The channel index for which settings are being specified
    * @min - The minimum display intensity value (shown as black)
    * @max - The maximum display intensity value (shown as full-intensity color)
    * @gamma - The gamma value (curvature of the value-to-display curve)
    * @histMax - The prefered maximum value at which the histogram is displayed.
    */
   public void storeChannelDisplaySettings(int channelIndex, int min, int max, double gamma, int histMax);

   /*
    * Returns a JSONObject with channel settings for the channel whose index
    * is the argument.
    */
   public JSONObject getChannelSetting(int channel);

   /*
    * Returns the bit depth of all images in the image cache.
    */
   public int getBitDepth();
   //public int getChannelBitDepth(int channelIndex);

   /*
    * Returns the preferred display color for a channel, specified by channelIndex.
    */
   public Color getChannelColor(int channelIndex);

   /*
    * Sets the preferred display color for a channel
    * @channel - The channel index
    * @rgb - A 6-byteinteger specifying the color: e.g., 0xFFFFFF is white.
    */
   public void setChannelColor(int channel, int rgb);

   /*
    * Returns the name of a channel specified by channelIndex.
    */
   public String getChannelName(int channelIndex);

   /*
    * Gets the minimum intensity value for a channel display (typically shown
    * as black).
    */
   public int getChannelMin(int channelIndex);

   /*
    * Gets the maximum intensity value for a channel display (typically shown
    * as full intensity color).
    */
   public int getChannelMax(int channelIndex) ;

   /*
    * Returns the gamma for the channel display.
    */
   public double getChannelGamma(int channelIndex);

   /*
    * Returns the preferred maximum value for the channel's histogram.
    */
   public int getChannelHistogramMax(int channelIndex);

   /*
    * Returns the number of channels in the ImageCache. More channels
    * may appear if more images are received with new channel indices.
    */
   public int getNumChannels();

   /*
    * Returns the pixel type for images in this image cache.
    */
   public String getPixelType();
   
  

}
