/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import java.util.concurrent.ConcurrentLinkedQueue;
import mmcorej.Metadata;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */

public class MMImageCache {

   private ConcurrentLinkedQueue<TaggedImage> taggedImgQueue_;
   private int taggedImgQueueSize_ = 50;
   private final ImageFileManagerInterface imageFileManager_;
   
   MMImageCache(ImageFileManagerInterface imageFileManager) {
      imageFileManager_ = imageFileManager;
      taggedImgQueue_ = new ConcurrentLinkedQueue<TaggedImage>();
   }



   public String putImage(Object img, Metadata md) {
      return putImage(new TaggedImage(img, md));
   }

   public String putImage(TaggedImage taggedImg) {
      try {
      cacheImage(taggedImg);
      return imageFileManager_.writeImage(taggedImg);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
         return "";
      }
   }

   public TaggedImage getImage(String filename) {
      for (TaggedImage taggedImg:taggedImgQueue_) {
         if (taggedImg.filename.equals(filename)) {
            return taggedImg;
         }
      }

      TaggedImage taggedImg = imageFileManager_.readImage(filename);
      cacheImage(taggedImg);
      return taggedImg;
   }

   private void cacheImage(TaggedImage taggedImg) {
      taggedImgQueue_.add(taggedImg);
      if (taggedImgQueue_.size() > taggedImgQueueSize_) { // If the queue is full,
         taggedImgQueue_.poll();                       // remove the oldest image.
      }
   }


}
