/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.Timer;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author Henry
 */
public class LiveModeTimer extends javax.swing.Timer {

   private static final String CCHANNELINDEX = "CameraChannelIndex";
   private static final String ACQ_NAME = MMStudioMainFrame.SIMPLE_ACQ;
   private VirtualAcquisitionDisplay win_;
   private CMMCore core_;
   private MMStudioMainFrame gui_;
   private long multiChannelCameraNrCh_;
   private boolean shutterOriginalState_;
   private boolean autoShutterOriginalState_;
   private long fpsTimer_;
   private long fpsCounter_;
   private long fpsInterval_ = 5000;
   private final NumberFormat format_;
   

   public LiveModeTimer(int delay) {
      super(delay, null);
      gui_ = MMStudioMainFrame.getInstance();
      core_ = gui_.getCore();
      format_ = NumberFormat.getInstance();
      format_.setMaximumFractionDigits(0x1);
   }

   private void setInterval() {
      double interval = 33;
      try {
         interval = Math.max(core_.getExposure(), 33);
      } catch (Exception e) {
         ReportingUtils.logError("Unable to get exposure from core");
      }
      fpsInterval_ =  (long) (20 *  interval);
      
      this.setDelay((int) interval);
   }

   private void setType() {
      if (!super.isRunning()) {
         ActionListener[] listeners = super.getActionListeners();
         for (ActionListener a : listeners) {
            this.removeActionListener(a);
         }

         multiChannelCameraNrCh_ = core_.getNumberOfCameraChannels();
         if (multiChannelCameraNrCh_ == 1) {
            this.addActionListener(singleCameraLiveAction());
         } else {
            this.addActionListener(multiCamLiveAction());
         }
      }
   }

   public void begin() throws Exception {

         manageShutter(true);
         core_.startContinuousSequenceAcquisition(0);
         setType();
         setInterval();

         // Wait for first image to create ImageWindow, so that we can be sure about image size
         long start = System.currentTimeMillis();
         long now = start;
         long timeout = Math.min(10000, this.getDelay() * 150);
         while (core_.getRemainingImageCount() == 0 && (now - start < timeout) ) {
            now = System.currentTimeMillis();
         }
         if (now - start >= timeout) {
            throw new Exception("Camera did not send image within a reasonable time");
         }
                  
         TaggedImage ti = core_.getLastTaggedImage();

         // With first image acquired, create the display
         gui_.checkSimpleAcquisition();
         win_ = MMStudioMainFrame.getSimpleDisplay();
         
         // Add first image here so initial autoscale works correctly
         addTags(ti, multiChannelCameraNrCh_ > 1 ? ti.tags.getInt(
                 core_.getCameraDevice() + "-" + CCHANNELINDEX) : 0);
         if (multiChannelCameraNrCh_ <= 1) {
            gui_.addImage(ACQ_NAME, ti, true, true);
         }

         // Add another image if multicamera
         if (multiChannelCameraNrCh_ > 1) {
            String camera = core_.getCameraDevice();
            int channel = ti.tags.getInt(camera + "-" + CCHANNELINDEX);
            TaggedImage[] images = new TaggedImage[(int) multiChannelCameraNrCh_];
            images[channel] = ti;
            int numFound = 1;
            while (numFound < multiChannelCameraNrCh_) {
               for (int n = 0; n < 2 * multiChannelCameraNrCh_; n++) {
                  TaggedImage t = core_.getNBeforeLastTaggedImage(n);
                  int ch = t.tags.getInt(camera + "-" + CCHANNELINDEX);
                  if (images[ch] == null) {
                     numFound++;
                     images[ch] = t;
                     addTags(t, ch);
                     break;
                  }
               }
            }
            //need to add in channel order so that autoscale on window opening works properly
            for (int i = 0; i < multiChannelCameraNrCh_; i++) {
               if (images[i] != null) {
                  gui_.addImage(ACQ_NAME, images[i], i == multiChannelCameraNrCh_ - 1, true);
               }
            }

         }
         
         fpsCounter_ = 0;
         fpsTimer_ = System.currentTimeMillis();

         super.start();
         win_.liveModeEnabled(true);
         
         win_.getImagePlus().getWindow().toFront();

   }

   @Override
   public void stop() {
      stop(true);
   }
   
   private void stop(boolean firstAttempt) {
      super.stop();
      try {
         core_.stopSequenceAcquisition();
         manageShutter(false);
         if (win_ != null) {
            win_.liveModeEnabled(false);
         }
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
         //Wait 1 s and try to stop again
         if (firstAttempt) {
            final Timer delayStop = new Timer(1000, null);
            delayStop.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  stop(false);
                  delayStop.stop();
               }
            });
            delayStop.start();
         }
      }
   }

   private void updateFPS() {
      try {
      fpsCounter_++;
      long now = System.currentTimeMillis();
      long diff = now - fpsTimer_;
      if (diff > fpsInterval_) {
         double fps = fpsCounter_ / (diff / 1000.0);
         ij.IJ.showStatus("fps: " + format_.format(fps));
         fpsCounter_ = 0;
         fpsTimer_ = now;
      }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   private ActionListener singleCameraLiveAction() {
      return new ActionListener() {
         private long startTime_ = System.currentTimeMillis();         
         
         @Override
         public void actionPerformed(ActionEvent e) {
            ReportingUtils.logMessage("Live mode timer action firing at:" + (System.currentTimeMillis()-startTime_));
            if (core_.getRemainingImageCount() == 0) {
               ReportingUtils.logMessage("Core image count equals 0");
                return;
            }
            if (win_.windowClosed()) //check is user closed window             
            {
               gui_.enableLiveMode(false);
            } else {
               try {
                  TaggedImage ti = core_.getLastTaggedImage();
                  ReportingUtils.logMessage("Got tagged img at: "+ (System.currentTimeMillis()-startTime_) );
                  addTags(ti, 0);
                  ReportingUtils.logMessage("Tags added at: "+ (System.currentTimeMillis()-startTime_));
                  gui_.addImage(ACQ_NAME, ti, true, true);
                  ReportingUtils.logMessage("Image added at" + (System.currentTimeMillis()-startTime_));
                  gui_.updateLineProfile();
                  updateFPS();
                                           
                  } catch (MMScriptException ex) {
                  ReportingUtils.showError(ex);
                  gui_.enableLiveMode(false);
               } catch (JSONException exc) {
                  ReportingUtils.showError("Problem with image tags");
                  gui_.enableLiveMode(false);
               } catch (Exception excp) {
                  ReportingUtils.showError("Couldn't get tagged image from core");
                  gui_.enableLiveMode(false);
               }
            }
         }
      };
   }

   private ActionListener multiCamLiveAction() {
      return new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (core_.getRemainingImageCount() == 0) {
               return;
            }
            if (win_.windowClosed() || !gui_.acquisitionExists(MMStudioMainFrame.SIMPLE_ACQ)) {
               gui_.enableLiveMode(false);  //disable live if user closed window
            } else {
               try {
                  TaggedImage[] images = new TaggedImage[(int) multiChannelCameraNrCh_];
                  String camera = core_.getCameraDevice();

                  TaggedImage ti = core_.getLastTaggedImage();

                  int channel = ti.tags.getInt(camera + "-" + CCHANNELINDEX);
                  images[channel] = ti;
                  int numFound = 1;
                  int index = 1;
                  while (numFound < images.length && index <= 2 * images.length) {      //play with this number
                     try {
                        ti = core_.getNBeforeLastTaggedImage(index);
                     } catch (Exception ex) {
                        break;
                     }
                     channel = ti.tags.getInt(camera + "-" + CCHANNELINDEX);
                     if (images[channel] == null) {
                        numFound++;
                     }
                     images[channel] = ti;
                     index++;
                  }

                  if (numFound == images.length) {
                     for (channel = 0; channel < images.length; channel++) {
                        ti = images[channel];
                        ti.tags.put("Channel", core_.getCameraChannelName(channel));
                        addTags(ti, channel);
                     }
                     int lastChannelToAdd = win_.getHyperImage().getChannel() - 1;
                     for (int i = 0; i < images.length; i++) {
                        if (i != lastChannelToAdd) {
                           gui_.addImage(MMStudioMainFrame.SIMPLE_ACQ, images[i], false, true);
                        }
                     }
                     gui_.addImage(MMStudioMainFrame.SIMPLE_ACQ, images[lastChannelToAdd], true, true);
                     gui_.updateLineProfile();
                     updateFPS();
                  }

               } catch (MMScriptException ex) {
                  ReportingUtils.showError(ex);
                  gui_.enableLiveMode(false);
               } catch (JSONException exc) {
                  ReportingUtils.showError("Problem with image tags");
                  gui_.enableLiveMode(false);
               } catch (Exception excp) {
                  ReportingUtils.showError("Couldn't get tagged image from core");
                  gui_.enableLiveMode(false);
               }
            }
         }
      };
   }

   private void addTags(TaggedImage ti, int channel) throws JSONException {
      MDUtils.setChannelIndex(ti.tags, channel);
      MDUtils.setFrameIndex(ti.tags, 0);
      MDUtils.setPositionIndex(ti.tags, 0);
      MDUtils.setSliceIndex(ti.tags, 0);
      try {
         ti.tags.put("Summary", MMStudioMainFrame.getInstance().getAcquisition(ACQ_NAME).getSummaryMetadata());
      } catch (MMScriptException ex) {
         ReportingUtils.logError("Error adding summary metadata to tags");
      }
   }

   private void manageShutter(boolean enable) throws Exception {
      String shutterLabel = core_.getShutterDevice();
      if (shutterLabel.length() > 0) {
         if (enable) {
            shutterOriginalState_ = core_.getShutterOpen();
            autoShutterOriginalState_ = core_.getAutoShutter();
            core_.setAutoShutter(false);
            core_.setShutterOpen(true);
         } else {
            core_.setShutterOpen(shutterOriginalState_);
            core_.setAutoShutter(autoShutterOriginalState_);
         }
      }
   }
}
