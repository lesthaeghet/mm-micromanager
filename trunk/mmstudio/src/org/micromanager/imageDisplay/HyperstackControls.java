

/*
 * HyperstackControls.java
 *
 * Created on Jul 15, 2010, 2:54:37 PM
 */
package org.micromanager.imageDisplay;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.swtdesigner.SwingResourceManager;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.awt.Font;
import java.lang.Math;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.json.JSONException;
import org.json.JSONObject;

import org.micromanager.internalinterfaces.DisplayControls;
import org.micromanager.internalinterfaces.LiveModeListener;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;


public class HyperstackControls extends DisplayControls implements LiveModeListener {

   private final static int DEFAULT_FPS = 10;
   private final static double MAX_FPS = 5000;
   // Height in pixels of our controls, not counting scrollbars.
   private final static int CONTROLS_HEIGHT = 65;

   private final VirtualAcquisitionDisplay display_;
   private EventBus bus_;

   // Last known mouse positions.
   private int mouseX_ = -1;
   private int mouseY_ = -1;

   // JPanel that holds all controls.
   private JPanel subPanel_;
   // Controls common to both control sets
   private ScrollerPanel scrollerPanel_;
   private JLabel pixelInfoLabel_;
   // Displays information on the currently-displayed image.
   private JLabel imageInfoLabel_;
   // Displays the countdown to the next frame.
   private JLabel countdownLabel_;
   private JButton showFolderButton_;
   private JButton saveButton_;
   private JLabel fpsLabel_;

   // Standard control set
   private javax.swing.JTextField fpsField_;
   private JButton abortButton_;
   private javax.swing.JToggleButton pauseAndResumeToggleButton_;

   // Snap/live control set
   private JButton snapButton_;
   private JButton snapToAlbumButton_;
   private JButton liveButton_;

   /**
    * @param shouldUseLiveControls - indicates if we should use the buttons for 
    *        the "Snap/Live" window or the buttons for normal displays.
    */
   public HyperstackControls(VirtualAcquisitionDisplay display, 
         EventBus bus, boolean shouldUseLiveControls) {
      super(new FlowLayout(FlowLayout.LEADING));
      bus_ = bus;
      initComponents(display, shouldUseLiveControls);
      display_ = display;
      bus_.register(this);
      MMStudioMainFrame.getInstance().addLiveModeListener(this);
   }

   private void initComponents(VirtualAcquisitionDisplay display, 
         final boolean shouldUseLiveControls) {
      // This layout minimizes space between components.
      subPanel_ = new JPanel(new MigLayout("", "0[]", "0[]0[]0[]0"));

      pixelInfoLabel_ = new JLabel("                                         ");
      pixelInfoLabel_.setMinimumSize(new Dimension(150, 10));
      pixelInfoLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      subPanel_.add(pixelInfoLabel_, "span 4");
      
      imageInfoLabel_ = new JLabel("                                         ");
      imageInfoLabel_.setMinimumSize(new Dimension(150, 10));
      imageInfoLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      subPanel_.add(imageInfoLabel_, "span 2");
      
      countdownLabel_ = new JLabel("                                         ");
      countdownLabel_.setMinimumSize(new Dimension(150, 10));
      countdownLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      subPanel_.add(countdownLabel_, "span, wrap");

      int numChannels = display.getNumChannels();
      int numFrames = display.getNumFrames();
      int numSlices = display.getNumSlices();
      // Positions have to be handled specially, since our display doesn't
      // actually know about them -- it normally relies on us! 
      JSONObject tags = display.getImageCache().getSummaryMetadata();
      int numPositions = 0;
      try {
         numPositions = MDUtils.getNumPositions(tags);
      }
      catch (JSONException e) {
         // Oh well, no positions for us.
      }
      scrollerPanel_ = new ScrollerPanel(
               bus_, new String[]{"channel", "position", "time", "z"}, 
               new Integer[]{numChannels, numPositions, numFrames, numSlices}, 
               DEFAULT_FPS);
      subPanel_.add(scrollerPanel_, "span, growx, wrap 0px");

      showFolderButton_ = new JButton();
      saveButton_ = new JButton();

      subPanel_.add(showFolderButton_);
      subPanel_.add(saveButton_);

      showFolderButton_.setBackground(new java.awt.Color(255, 255, 255));
      showFolderButton_.setIcon(
            new javax.swing.ImageIcon(
               getClass().getResource("/org/micromanager/icons/folder.png")));
      showFolderButton_.setToolTipText("Show containing folder");
      showFolderButton_.setFocusable(false);
      showFolderButton_.setHorizontalTextPosition(
            javax.swing.SwingConstants.CENTER);
      showFolderButton_.setMaximumSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setMinimumSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setPreferredSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setVerticalTextPosition(
            javax.swing.SwingConstants.BOTTOM);
      showFolderButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            showFolderButtonActionPerformed(evt);
         }
      });

      saveButton_.setBackground(new java.awt.Color(255, 255, 255));
      saveButton_.setIcon(
            new javax.swing.ImageIcon(
               getClass().getResource("/org/micromanager/icons/disk.png")));
      saveButton_.setToolTipText("Save as...");
      saveButton_.setFocusable(false);
      saveButton_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      saveButton_.setMaximumSize(new java.awt.Dimension(30, 28));
      saveButton_.setMinimumSize(new java.awt.Dimension(30, 28));
      saveButton_.setPreferredSize(new java.awt.Dimension(30, 28));
      saveButton_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      saveButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveButtonActionPerformed(evt, shouldUseLiveControls);
         }
      });

      // This control is added by both Snap/Live, and Standard, but in 
      // different places on each. 
      fpsLabel_ = new JLabel("                      ", SwingConstants.RIGHT);
      fpsLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      fpsLabel_.setFocusable(false);

      if (shouldUseLiveControls) {
         makeSnapLiveControls();
      }
      else {
         makeStandardControls();
      }
      
      add(subPanel_);
   }

   /**
    * Generate the controls used for the "Snap/Live" window.
    */
   private void makeSnapLiveControls() {
      snapButton_ = new JButton();
      snapButton_.setFocusable(false);
      snapButton_.setIconTextGap(6);
      snapButton_.setText("Snap");
      snapButton_.setMinimumSize(new Dimension(90,28));
      snapButton_.setPreferredSize(new Dimension(90,28));
      snapButton_.setMaximumSize(new Dimension(90,28));
      snapButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "/org/micromanager/icons/camera.png"));
      snapButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      snapButton_.setToolTipText("Snap single image");
      snapButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            MMStudioMainFrame.getInstance().doSnap();
         }

      });

      liveButton_ = new JButton();
      liveButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/camera_go.png"));
      liveButton_.setIconTextGap(6);
      liveButton_.setText("Live");
      liveButton_.setMinimumSize(new Dimension(90,28));
      liveButton_.setPreferredSize(new Dimension(90,28));
      liveButton_.setMaximumSize(new Dimension(90,28));
      liveButton_.setFocusable(false);
      liveButton_.setToolTipText("Continuous live view");
      liveButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      liveButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            liveButtonAction();
         }
      });
    
      snapToAlbumButton_ = new JButton("Album");
      snapToAlbumButton_.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/arrow_right.png"));
      snapToAlbumButton_.setIconTextGap(6);
      snapToAlbumButton_.setToolTipText("Add current image to album");
      snapToAlbumButton_.setFocusable(false);
      snapToAlbumButton_.setMaximumSize(new Dimension(90, 28));
      snapToAlbumButton_.setMinimumSize(new Dimension(90, 28));
      snapToAlbumButton_.setPreferredSize(new Dimension(90, 28));
      snapToAlbumButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      snapToAlbumButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            snapToAlbumButtonActionPerformed();
         }
      });

      subPanel_.add(snapButton_);
      subPanel_.add(liveButton_);
      subPanel_.add(snapToAlbumButton_);
      fpsLabel_.setText("                          ");
      subPanel_.add(fpsLabel_, "span, wrap, width 120px, align right");
   }

   /**
    * Generate the controls used on a standard dataset display (i.e. not the 
    * snap/live window).
    */
   private void makeStandardControls() {
      fpsField_ = new javax.swing.JTextField(String.valueOf(DEFAULT_FPS), 4);
      abortButton_ = new JButton();
      pauseAndResumeToggleButton_ = new javax.swing.JToggleButton();
      
      subPanel_.add(abortButton_);
      subPanel_.add(pauseAndResumeToggleButton_);
      // Make a new panel to hold the FPS info, since they need to be 
      // together.
      JPanel fpsPanel = new JPanel(new MigLayout());
      fpsPanel.add(fpsLabel_);
      fpsPanel.add(fpsField_);

      subPanel_.add(fpsPanel, "span, gapleft push, wrap");

      fpsField_.setToolTipText(
            "Set the speed at which the acquisition is played back.");
      fpsField_.addFocusListener(new java.awt.event.FocusAdapter() {
         @Override
         public void focusLost(java.awt.event.FocusEvent evt) {
            fpsField_FocusLost(evt);
         }
      });
      fpsField_.addKeyListener(new java.awt.event.KeyAdapter() {
         @Override
         public void keyReleased(java.awt.event.KeyEvent evt) {
            fpsField_KeyReleased(evt);
         }
      });

      abortButton_.setBackground(new java.awt.Color(255, 255, 255));
      abortButton_.setIcon(
            new javax.swing.ImageIcon(
               getClass().getResource("/org/micromanager/icons/cancel.png")));
      abortButton_.setToolTipText("Stop acquisition");
      abortButton_.setFocusable(false);
      abortButton_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      abortButton_.setMaximumSize(new java.awt.Dimension(30, 28));
      abortButton_.setMinimumSize(new java.awt.Dimension(30, 28));
      abortButton_.setPreferredSize(new java.awt.Dimension(30, 28));
      abortButton_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      abortButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            abortButtonActionPerformed(evt);
         }
      });

      pauseAndResumeToggleButton_.setIcon(
            new javax.swing.ImageIcon(getClass().getResource(
                  "/org/micromanager/icons/control_pause.png")));
      pauseAndResumeToggleButton_.setToolTipText("Pause acquisition");
      pauseAndResumeToggleButton_.setFocusable(false);
      pauseAndResumeToggleButton_.setMargin(new java.awt.Insets(0, 0, 0, 0));
      pauseAndResumeToggleButton_.setMaximumSize(new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton_.setMinimumSize(new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton_.setPreferredSize(
            new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton_.setPressedIcon(
            new javax.swing.ImageIcon(getClass().getResource(
                  "/org/micromanager/icons/resultset_next.png")));
      pauseAndResumeToggleButton_.setSelectedIcon(
            new javax.swing.ImageIcon(getClass().getResource(
                  "/org/micromanager/icons/resultset_next.png")));
      pauseAndResumeToggleButton_.addActionListener(
            new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  pauseAndResumeToggleButtonActionPerformed(evt);
               }
            });
   }

   /**
    * User moused over the display; update our indication of pixel intensities.
    * TODO: only providing the first intensity; what about multichannel 
    * images?
    */
   @Subscribe
   public void onMouseMoved(MouseIntensityEvent event) {
      mouseX_ = event.x_;
      mouseY_ = event.y_;
      setPixelInfo(mouseX_, mouseY_, event.intensities_[0]);
   }

   /**
    * Update our pixel info text.
    */
   private void setPixelInfo(int x, int y, int intensity) {
      pixelInfoLabel_.setText(String.format("<%d, %d>: %d", x, y, intensity));
   }

   /**
    * Our ScrollerPanel is informing us that we need to display a different
    * image.
    */
   @Subscribe
   public void onSetImage(ScrollerPanel.SetImageEvent event) {
      int position = -1;
      int channel = -1;
      int frame = -1;
      int slice = -1;
      try {
         position = event.getPositionForAxis("position");
         display_.updatePosition(position);
         // Positions for ImageJ are 1-indexed but positions from the event are 
         // 0-indexed.
         channel = event.getPositionForAxis("channel") + 1;
         frame = event.getPositionForAxis("time") + 1;
         slice = event.getPositionForAxis("z") + 1;
         display_.getHyperImage().setPosition(channel, slice, frame);
      }
      catch (Exception e) {
         // This can happen, rarely, with an ArrayIndexOutOfBoundsException
         // in IJ code that draws the new image. Best guess is that we're
         // trying to set an image that the IJ code doesn't yet have access
         // to, maybe?
         // TODO: Just swallowing the error for now.
      }
   }

   /**
    * A new image has been made available. Update our pixel info, assuming
    * we have a valid mouse position.
    */
   @Subscribe
   public void onNewImage(NewImageEvent event) {
      if (mouseX_ != -1 && mouseY_ != -1) {
         try {
            int intensity = display_.getIntensityAt(mouseX_, mouseY_);
            setPixelInfo(mouseX_, mouseY_, intensity);
         }
         catch (Exception e) {
            ReportingUtils.logError(e, "Error in HyperstackControls onNewImage");
         }
      }
   }

   /**
    * Our ScrollerPanel is informing us that its layout has changed.
    */
   @Subscribe
   public void onLayoutChange(ScrollerPanel.LayoutChangedEvent event) {
      invalidate();
      validate();
   }

   private void showFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {
      display_.showFolder();
   }

   private void fpsField_FocusLost(java.awt.event.FocusEvent evt) {
      updateFPS();
   }

   private void fpsField_KeyReleased(java.awt.event.KeyEvent evt) {
      updateFPS();
   }

   private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {
      display_.abort();
   }

   private void pauseAndResumeToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
      display_.pause();
}

   private void saveButtonActionPerformed(java.awt.event.ActionEvent evt, final boolean isSimpleDisplay) {
      new Thread() {
         @Override
         public void run() {
            // We don't want to tie the Snap/Live display to a specific
            // file since its contents get overwritten regularly.
            display_.saveAs(!isSimpleDisplay);
         }
      }.start();
   }

   private void snapToAlbumButtonActionPerformed() {
      try {
         MMStudioMainFrame gui = MMStudioMainFrame.getInstance();
         gui.copyFromLiveModeToAlbum(display_);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   private void liveButtonAction() {
       MMStudioMainFrame.getInstance().enableLiveMode(!MMStudioMainFrame.getInstance().isLiveModeOn());
    }

   private void updateFPS() {
      // There's no FPS field when using the Snap/Live window
      if (fpsField_ != null) {
         try {
            double fps = NumberUtils.displayStringToDouble(fpsField_.getText());
            // Constrain the FPS to a sane range.
            fps = Math.max(1.0, Math.min(fps, MAX_FPS));
            scrollerPanel_.setFramesPerSecond(fps);
         } catch (ParseException ex) {
            // No recognizable number (e.g. because the field is empty); just
            // do nothing.
         }
      }
   }

   @Override
   public synchronized void setImageInfoLabel(String text) {
      imageInfoLabel_.setText(text);
   }

   private void updateStatusLine(JSONObject tags) {
      String status = "";
      try {
         String xyPosition;
         try {
            xyPosition = tags.getString("PositionName");
            if (xyPosition != null && !xyPosition.contentEquals("null")) {
               status += xyPosition + ", ";
            }
         } catch (Exception e) {
            //Oh well...
         }

         try {
            double seconds = tags.getDouble("ElapsedTime-ms") / 1000;
            status += elapsedTimeDisplayString(seconds);
         } catch (JSONException ex) {
            ReportingUtils.logError("MetaData did not contain ElapsedTime-ms field");
         }

         String zPosition;
         try {
            zPosition = NumberUtils.doubleStringCoreToDisplay(tags.getString("ZPositionUm"));
            status += ", z: " + zPosition + " um";
         } catch (Exception e) {
            try {
               zPosition = NumberUtils.doubleStringCoreToDisplay(tags.getString("Z-um"));
               status += ", z: " + zPosition + " um";
            } catch (Exception e1) {
               // Do nothing...
            }
         }
         String chan;
         try {
            chan = MDUtils.getChannelName(tags);
            if (chan != null && !chan.contentEquals("null")) {
               status += ", " + chan;
            }
         } catch (Exception ex) {
         }

         setImageInfoLabel(status);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }

   }

   public static String elapsedTimeDisplayString(double seconds) {
      // Use "12.34s" up to 60 s; "12m 34.56s" up to 1 h, and
      // "1h 23m 45s" beyond that.

      long wholeSeconds = (long) Math.floor(seconds);
      double fraction = seconds - wholeSeconds;

      long hours = TimeUnit.SECONDS.toHours(wholeSeconds);
      wholeSeconds -= TimeUnit.HOURS.toSeconds(hours);
      String hoursString = "";
      if (hours > 0) {
         hoursString = hours + "h ";
      }

      long minutes = TimeUnit.SECONDS.toMinutes(wholeSeconds);
      wholeSeconds -= TimeUnit.MINUTES.toSeconds(minutes);
      String minutesString = "";
      if (minutes > 0) {
         minutesString = minutes + "m ";
      }

      String secondsString;
      if (hours == 0 && fraction > 0.01) {
         secondsString = String.format("%.2fs", wholeSeconds + fraction);
      }
      else {
         secondsString = wholeSeconds + "s";
      }

      return hoursString + minutesString + secondsString;
   }

   @Override
   public void newImageUpdate(JSONObject tags) {
      if (tags == null) {
         return;
      }
      updateStatusLine(tags);
      if (!display_.acquisitionIsRunning() || display_.getNextWakeTime() <= 0) {
         // No acquisition to display a countdown for.
         return;
      }
      final long nextImageTime = display_.getNextWakeTime();
      if (System.nanoTime() / 1000000 >= nextImageTime) {
         // Already past the next image time.
         return;
      }
      // TODO: why the try/catch block here?
      try {
         final Timer timer = new Timer("Next frame display");
         TimerTask task = new TimerTask() {

            @Override
            public void run() {
               double timeRemainingS = (nextImageTime - System.nanoTime() / 1000000.0) / 1000;
               if (timeRemainingS > 0 && 
                     display_.acquisitionIsRunning()) {
                  countdownLabel_.setText(
                        String.format("Next frame: %.2fs", timeRemainingS));
               } else {
                  timer.cancel();
                  countdownLabel_.setText("");
               }
            }
         };
         timer.schedule(task, 500, 100);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }


   @Override
   public void acquiringImagesUpdate(boolean state) {
      // NB currently there's no situation in which one of these will be null
      // when the other isn't, but on the other hand who knows what the future
      // will bring?
      if (abortButton_ != null) {
         abortButton_.setEnabled(state);
      }
      if (pauseAndResumeToggleButton_ != null) {
         pauseAndResumeToggleButton_.setEnabled(state);
      }
   }

   @Override
   public void imagesOnDiskUpdate(boolean enabled) {
      showFolderButton_.setEnabled(enabled);
   }

   public void prepareForClose() {
      scrollerPanel_.prepareForClose();
      bus_.unregister(this);
   }

   public void setPosition(int p) {
      scrollerPanel_.setPosition("position", p);
   }

   public int getPosition() {
      return scrollerPanel_.getPosition("position");
   }

   /**
    * New information on our FPS; update a label.
    */
   @Subscribe
   public void onFPSUpdate(FPSEvent event) {
      // Default to assuming we'll be blanking the label.
      String newLabel = "";
      if (event.getDataFPS() != 0) {
         newLabel = String.format("FPS: %.1f (display %.1f)", 
               event.getDataFPS(), event.getDisplayFPS());
      }
      else if (fpsField_ != null) {
         // No new data, but we do have an FPS text field for animations, so
         // switch fpsLabel_ to being an indicator for that. 
         newLabel = "Playback FPS:";
      }
      fpsLabel_.setText(newLabel);
   }

   /**
    * Live mode was toggled; if we have a "live mode" button, it needs to be 
    * toggled on/off; likewise, the Snap button should be disabled/enabled.
    */
   public void liveModeEnabled(boolean isEnabled) {
      if (liveButton_ == null) {
         return;
      }
      String label = isEnabled ? "Stop Live" : "Live";
      String iconPath = isEnabled ? "/org/micromanager/icons/cancel.png" : "/org/micromanager/icons/camera_go.png";
      liveButton_.setIcon(
            SwingResourceManager.getIcon(MMStudioMainFrame.class, iconPath));
      liveButton_.setText(label);
      if (snapButton_ != null) {
         snapButton_.setEnabled(!isEnabled);
      }
   }

   public int getNumPositions() {
      return scrollerPanel_.getMaxPosition("position");
   }
}
