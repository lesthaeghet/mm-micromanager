/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.micromanager.api;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.TaggedImage;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public abstract class TaggedImageProcessor extends Thread {
   private TaggedImageQueue input_;
   private TaggedImageQueue output_;
   private boolean stopRequested_ = false;

   protected abstract void process();
   /*    The "Identity" process method:
    * {
    *    produce(poll());
    * }
    */

   public void run() {
      while (!stopRequested_) {
         process();
      }
   }

   public synchronized void requestStop() {
      stopRequested_ = true;
   }

   public TaggedImageProcessor() {}

   public void setInput(TaggedImageQueue input) {
      input_ = input;
   }

   public void setOutput(TaggedImageQueue output) {
      output_ = output;
   }

   protected TaggedImage poll() {
      while (!stopRequested()) {
         try {
            TaggedImage image = input_.poll(100, TimeUnit.MILLISECONDS);
            if (image != null) {
               return image;
            }
         } catch (InterruptedException ex) {
            ReportingUtils.logError(ex);
         }
      }
      return TaggedImageQueue.POISON;
   }

   protected void drainTo(Collection<TaggedImage> taggedImages) {
      input_.drainTo(taggedImages);
   }

   protected void produce(TaggedImage taggedImage) {
      try {
         output_.put(taggedImage);
      } catch (InterruptedException ex) {
         ReportingUtils.logError(ex);
      }
   };

   protected synchronized boolean stopRequested() {
      return stopRequested_;
   }



}
