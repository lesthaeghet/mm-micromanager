package org.micromanager.image5d;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.StackWindow;
import ij.measure.Calibration;

import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Scrollbar;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.micromanager.PlaybackPanel;
import org.micromanager.api.AcquisitionEngine;
import org.micromanager.metadata.AcquisitionData;
import org.micromanager.metadata.DisplaySettings;
import org.micromanager.metadata.ImageKey;
import org.micromanager.metadata.ImagePropertyKeys;
import org.micromanager.metadata.MMAcqDataException;
import org.micromanager.metadata.MetadataDlg;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.ProgressBar;
import org.micromanager.utils.ReportingUtils;

import org.json.JSONObject;
import org.micromanager.utils.JavaUtils;

/*
 * Created on 28.03.2005
 */

/** ImageWindow for Image5Ds. Has two scrollbars for slice and time and a panel with controls
 * to change the current channel and its color.
 * @author Joachim Walter
 * 
 * NOTES:-extended to accomodate button panel below slice/frame scrollbars
 *       Micro-Manager project
 *       Nenad Amodaj, Feb 2006
 */
public class Image5DWindow extends StackWindow {
   private static final long serialVersionUID = -5031307205188924732L;
   protected ChannelControl channelControl;
   protected Scrollbar[] Scrollbars;
   protected Image5D i5d;

   // >>>>>>> Micro-Manager >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   ChannelSpec channelsMM_[]; // Micro-Manager channel info
   private MetadataDlg metaDlg_;
   //private JSONObject metadata_;
   private AcquisitionData acqData_;
   private AcquisitionEngine acqEng_;
   private boolean acqActive_ = false;
   private boolean autoSave_ = false;
   private Timer playTimer_;
   private Timer countdownTimer_;
   long timeToGoMs_ = 0;
   long playInterval_ = 100;
   private Preferences prefs_;
   private static final String PLAY_INTERVAL = "PlayInterval";
   protected String rootDir_ = "";
   // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   
   // Array for storing change of position in each dimension. 
   // 0: no change, 1 - dimensionSize : changed position
   protected int[] positions;
   
   protected int nDimensions = 5;
   
   protected boolean isInitialized = false;
   protected PlaybackPanel pb_;
   private int framesToGo_=0;
   private int playbackFrames_ = 0;

   private String acqSavePath_ = "";
   
   /**
    * @param imp
    */
   public Image5DWindow(Image5D imp) {
      this(imp, new Image5DCanvas(imp));
   }


   /**
    * @param imp
    * @param visible
    */
   public Image5DWindow(Image5D imp, boolean visible) {
      this(imp, new Image5DCanvas(imp), visible);
   }

   /**
    * @param imp
    * @param ic
    */
   public Image5DWindow(Image5D imp, Image5DCanvas ic) {
      this(imp, ic, true);
   }
   
   /**
    * @param imp
    * @param ic
    * @param visible
    */
   public Image5DWindow(Image5D imp, Image5DCanvas ic, boolean visible) {
      super(imp, ic);
            
      if (!visible)
         setVisible(false);
      
      i5d = imp;
      
      if (imp.getNDimensions() != nDimensions) {
         throw new IllegalArgumentException("Wrong number of dimensions.");
      }

      Scrollbars = new Scrollbar[nDimensions];
      positions = new int[nDimensions];
      
      // Remove all components and then add them with the Image5DLayout layoutmanager.
  	   remove(sliceSelector);
      remove(ic);
      
      setLayout(new Image5DLayout(ic));

      // Add ImageCanvas
      add(ic, Image5DLayout.MAIN_CANVAS);
      
      // Add channel selector
      channelControl = new ChannelControl(this);
      add(channelControl, Image5DLayout.CHANNEL_SELECTOR);
      
      addHorizontalScrollbars(imp);
      
      // >>>>>>>>>>>>>>>>>>>>>>>>>>> Micro-Manager >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      // add button panel
      pb_ = createPlaybackPanel();
      prefs_ = Preferences.userNodeForPackage(this.getClass());
      playInterval_ = prefs_.getLong(PLAY_INTERVAL, 500);
      
      ActionListener playTimerHandler = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            int curFrame = i5d.getCurrentFrame()-1; // one-based index
            int numFrames = 0;
            if (acqEng_ != null && acqEng_.isAcquisitionRunning() && acqActive_) {
               numFrames = acqEng_.getCurrentFrameCount();
            }
            else
               if (playbackFrames_ == 0)
                  numFrames = i5d.getDimensionSize(4);
               else
                  numFrames = playbackFrames_;
            
            int nextFrame = 0;
            if ( 0 < numFrames)
               nextFrame = (curFrame + 1) % numFrames;
            i5d.setCurrentPosition(4, nextFrame); // zero based index
//            if (acqEng_ != null)
//               pb_.setElapsedTime(nextFrame * acqEng_.getFrameIntervalMs());
         }
      };      
      playTimer_ = new Timer((int)playInterval_, playTimerHandler);
      playTimer_.setInitialDelay(0);
      
      ActionListener countdownTimerHandler = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            long numSec = timeToGoMs_ / 1000;
            if (numSec > 0) {
               pb_.setImageInfo(Long.toString(numSec) + " s, " + framesToGo_ + " frames remaining");
               timeToGoMs_ -= 1000;
            } else {
               stopCountdown();
            }
         }
      };      
      countdownTimer_ = new Timer((int)playInterval_, countdownTimerHandler);
      countdownTimer_.setInitialDelay(0);

                  
      // Interval
      pb_.setIntervalText(String.valueOf(1000.0 / (double)playInterval_));

      add(pb_, Image5DLayout.PANEL_SELECTOR);
      
      // add window listeners      
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {            
            ((Window)e.getSource()).setVisible(true);
            playTimer_.stop();
                        
            if (metaDlg_ != null)
               metaDlg_.dispose();
            
            // if ij == null we are running as standalone
            ImageJ ij = IJ.getInstance();
            if (ij == null)
               close();
         }
      });
      
      addWindowListener(new WindowAdapter() {
         public void windowDeactivated(WindowEvent e) {
         }
      });   
            
      // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      
      int size = imp.getNFrames();
      
      for(int i=3; i<nDimensions; ++i) {
    	 if (Scrollbars[i] != null) { 
	         Scrollbars[i].addAdjustmentListener(this);
	         Scrollbars[i].setUnitIncrement(1);
	         if (i==3) {
		         int blockIncrement = size/10;
		         if (blockIncrement<1) blockIncrement = 1;
		         Scrollbars[i].setBlockIncrement(blockIncrement); 
	         }
    	 }
      }			
      
      sliceSelector = Scrollbars[3];

      // Suppress the animation selector from StackWindow
      try {
         Component animationSelector = (Component) JavaUtils.getRestrictedFieldValue(this, StackWindow.class, "animationSelector");
         if (animationSelector != null)
            animationSelector.setVisible(false);
      } catch (NoSuchFieldException ex) {
         ReportingUtils.logError(ex);
      }
      pack();
      isInitialized = true;	
      
      updateSliceSelector();	
      i5d.updateAndRepaintWindow();
      i5d.updateImageAndDraw();
      
      thread = new Thread(this, "SliceSelector");
      thread.start();		
   }
   
   public void setPlaybackFrames(int frames) {
      playbackFrames_ = frames;
   }
   
   // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Micromanage >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   
   public void setMMChannelData(ChannelSpec[] channelInfo) {
      channelsMM_ = channelInfo;
   }
   
   public ChannelSpec[] getMMChannelData() {
      return channelsMM_;
   }
   
   /**
    * Gets the metadata from the external source and creates
    * an internal acquisition data object.
    * IMPORTANT NOTE: this metadata is always instantiated as in-memory,
    * regardless of whether the original source actually saved it.
    * TODO: Do we need to clear the potential references to files ???
    * @param metaStream - metadata
    */
   private void setMetadata(JSONObject metaData) {
      try {
         acqData_ = new AcquisitionData();
//System.out.println(metaStream);
         acqData_.createNew();
         acqData_.load(metaData);
         i5d.changes = true;
      } catch (MMAcqDataException e) {
         // TODO Auto-generated catch block
         ReportingUtils.logError(e);
      }
   }
   
   /**
    * 
    * @param acqData
    */
   public void setAcquisitionData(AcquisitionData acqData) {
      acqData_ = acqData;
   }
   
   public AcquisitionData getAcquisitionData() {
      return acqData_;
   }
   
   public void startCountdown(long timeToGoMs, int framesToGo) {
      timeToGoMs_ = timeToGoMs;
      framesToGo_ = framesToGo;
      countdownTimer_.setDelay(1000);
      countdownTimer_.start();
   }
   
   public void stopCountdown() {
      timeToGoMs_ = 0;
      countdownTimer_.stop();
      pb_.setImageInfo("");      
   }
       
   public void displayMetadata() {
      if (acqData_ == null)
         return;
      
      if (metaDlg_ != null) {
         metaDlg_.setVisible(false);
         metaDlg_.dispose();
      }
      
      metaDlg_ = new MetadataDlg(this, this);
      metaDlg_.setMetadata(acqData_);
      metaDlg_.displaySummary();
      metaDlg_.displayImageData(i5d.getCurrentFrame()-1, i5d.getCurrentChannel()-1, i5d.getCurrentSlice()-1);
      metaDlg_.displayComment();
      metaDlg_.setVisible(true);      
   }
      
   public void setAcquitionEngine(AcquisitionEngine eng) {
      playbackFrames_ = 0;
      acqEng_ = eng;
      // if the 'save files to acquisition directory' was checked, assume the data have been saved
      if (acqEng_ != null && acqEng_.getSaveFiles())
         autoSave_ = true;
      
      // save reference to the acqEngine to be able to save file after
   }
   
   public void setActive(boolean state) {
      acqActive_ = state;
      i5d.changes = true;
      if (acqEng_ != null)
         rootDir_ = acqEng_.getRootName();
  }
   
   public boolean abortAcquisition() {
      if (acqEng_ == null)
         return true;
      
      if (acqEng_.isAcquisitionRunning() && acqActive_) {
         int result = JOptionPane.showConfirmDialog(this,
               "Abort current acquisition task ?",
               "Micro-Manager", JOptionPane.YES_NO_OPTION,
               JOptionPane.INFORMATION_MESSAGE);

         try {
             if (result == JOptionPane.YES_OPTION) {
                if (acqEng_ != null && acqEng_.isPaused())
                   acqEng_.setPause(false);
                acqEng_.abortRequest();
                acqEng_.stop(true);
                // TODO: needs to clean-up properly
             } else {
                return false; // abort cancelled
             }
         } catch (NullPointerException e) {
             if (acqEng_ != null)
                 ReportingUtils.logError(e);
         }
         }
      return true;
   }
   
   public void playBack() {
      String txt = pb_.getIntervalText();
      if (txt.length() > 0) {
         double fps = Double.parseDouble(txt);
         if (fps <= 0.0)
            fps = 0.5;
         playInterval_ = (long) (1000.0 / fps);
      } else {
         playInterval_ = 500;
         pb_.setIntervalText(String.valueOf(playInterval_ / 1000.0));
      }
      
      prefs_.putLong(PLAY_INTERVAL, playInterval_); // remember the value for the next time
      playTimer_.setDelay((int)playInterval_);
      playTimer_.start();
   }
   
   public void stopPlayBack() {
      playTimer_.stop();
   }
   
   public void pause() {
      if (acqEng_ == null)
         return;
      acqEng_.setPause(true);
   }
   
   public void resume() {
      if (acqEng_ == null)
         return;   
      acqEng_.setPause(false);
   }
   
   public boolean isPlaybackRunning() {
      return playTimer_.isRunning();
   }
     
   /**
    * Saves the data set in the micro-manager format.
    */
    public void saveAs() {
        // NOTE: >> save as directory instead of image 5D

        if (acqData_ == null) {
            JOptionPane.showMessageDialog(this,
                    "This 5D-image was not generated by Micro-Manager acquisition process or data is corruped.\n" +
                    "Unable to save.");
            return;
        }

        try {
            if (metaDlg_ != null) {
                acqData_.setComment(metaDlg_.getComment());
            }
            for (int i = 0; i < acqData_.getNumberOfChannels(); i++) {
                i5d.storeChannelProperties(i + 1);
                ChannelDisplayProperties cdp = i5d.getChannelDisplayProperties(i + 1);
                DisplaySettings ds = new DisplaySettings(cdp.getMinValue(), cdp.getMaxValue());
                acqData_.setChannelDisplaySetting(i, ds);
                Color c = getColor(i + 1);
                acqData_.setChannelColor(i, c.getRGB());
            }
        } catch (MMAcqDataException e1) {
            ReportingUtils.showError(e1);
        }

        if (acqData_.getNumberOfFrames() > i5d.getNFrames()) {
            ReportingUtils.showError("Internal error: detected number of frames invalid " + acqData_.getNumberOfFrames());
            return;
        }

        // choose output directory
        boolean saveFile = true;
        File f;

        do {
           saveFile = true;
           FileDialog fc = new FileDialog(this, "Save Image5D", java.awt.FileDialog.SAVE);
           fc.setDirectory(rootDir_);
           fc.setFile(i5d.getTitle());
           fc.setVisible(true);
           if (fc.getFile() == null)
              return;
           rootDir_ = fc.getDirectory();
           f = new File(rootDir_, fc.getFile());
           // check if file already exists
           if (f.exists()) {
               JOptionPane.showMessageDialog(this,
                       f.getName() + " already exists. Existing data can not be overwritten.");
               saveFile = false;
           }
        } while (saveFile == false);

        final ProgressBar progressBar = new ProgressBar("Saving File...", 0, acqData_.getNumberOfFrames());

        acqSavePath_ = f.getAbsolutePath();
        if (!acqData_.inMemory()) {
           // this creates a new acqData_ object copied form the old one
           setMetadata(acqData_.getMetadataObject());
        }
        try {
            acqData_.save(f.getName(), f.getParent(), false, null);
        } catch (MMAcqDataException ex) {
            ReportingUtils.showError(ex);
        }

        final File f2 = f;

        Thread t = new Thread() {

            public void run() {

                for (int i = 0; i < acqData_.getNumberOfFrames(); i++) {
                    for (int j = 0; j < i5d.getNSlices(); j++) {
                        for (int k = 0; k < i5d.getNChannels(); k++) {
                            // save only images that were actually acquired, i.e. the ones recorded in the metadata
                            if (acqData_.hasImageMetadata(i, k, j)) {
                                Object img = i5d.getPixels(k + 1, j + 1, i + 1);
                                try {
                                    acqData_.attachImage(img, i, k, j);
                                } catch (MMAcqDataException ex) {
                                    ReportingUtils.logError(ex);
                                }
                            }
                        }
                    }
                    progressBar.setProgress(i);
                }
                try {
                    acqData_.saveMetadata();
                } catch (MMAcqDataException e) {
                    ReportingUtils.showError(e);
                }
                setTitle(f2.getName());

                if (progressBar != null) {
                    progressBar.setVisible(false);
                }

                i5d.changes = false;
            }
        };
        t.start();

    }
   
   
   /** Removes this window from the window list and disposes of it.
    * Overrides the original close defined in ImageWindow.
    */
   public boolean close() {
      
      boolean isRunning = running || running2;
      running = running2 = false;
      if (isRunning) IJ.wait(500);
      ImageJ ij = IJ.getInstance();

      abortAcquisition();

      if (imp.changes && (!autoSave_)) {
         int result = JOptionPane.showConfirmDialog(this,
               "Close window and discard unsaved data?",
               "Micro-Manager", JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.INFORMATION_MESSAGE);

         if (result == JOptionPane.CANCEL_OPTION)
            return false;
      }
      
      closed = true;
      WindowManager.removeWindow(this);
      setVisible(false);
      if (ij!=null && ij.quitting())  // this may help avoid thread deadlocks
         return true;
      dispose();
      imp.flush();
      
      synchronized(this) {
         done = true;
         notify();
      }
      return true;
   }
   
   // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   
   /** Handles changes in the scrollbars for z and t. 
    */
   public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
      if (!running2){
         
         for(int i=3; i<nDimensions; ++i) {
            if (e.getSource()==Scrollbars[i])
               positions[i] = Scrollbars[i].getValue();
         }

         notify();
      }
   }
   
   public int getDisplayMode() {
      if (channelControl!=null) {
         return channelControl.getDisplayMode();
      }
      else {
         return -1;
      }
   }
   
   /** Handles change in ChannelControl. 
    * Is called by ChannelControl without any events involved.
    */
   public synchronized void channelChanged() {
      if (!running2){
         positions[2] = channelControl.getCurrentChannel();
      }
      
      notify();
      
   }
   
   /** Updates the size and value of the stack and time scrollbar 
    * and the size and value and other display properties of the channel control. */
   public void updateSliceSelector() {		
      // TODO: update other display properties of channel control
      if (isInitialized) {
         int[] dimensions = imp.getDimensions();
         
         // update channel control
         channelControl.setDisplayMode(i5d.getDisplayMode());
         channelControl.updateChannelSelector();
         // update z- and time control
         int size, max;
         for(int i=3; i<nDimensions; ++i) {
        	if (Scrollbars[i]!= null) {
	            size = dimensions[i];
	            max = Scrollbars[i].getMaximum();
	            if (max!=(size+1))
	               Scrollbars[i].setMaximum(size+1);
	            Scrollbars[i].setValue(((Image5D)imp).getCurrentPosition(i)+1);
        	}
//            if (i == 4 && pb_ != null && acqEng_ != null) {
//               pb_.setElapsedTime(((Image5D)imp).getCurrentPosition(i) * acqEng_.getFrameIntervalMs());
//            }
         }
         //if (acqEng_ != null)
         //   acqEng_.updateImageGUI();
      }
   }
   
   public void drawInfo(Graphics g) {
      // TODO: add support for slice labels
      int TEXT_GAP = 0;
      String s="";
      Insets insets = super.getInsets();
      Image5D img5 = (Image5D) imp;
      int[] dimensions = imp.getDimensions();
      Calibration cal = img5.getCalibration();
      
      // current position
      for (int i=2; i<img5.getNDimensions(); ++i){
         s += (img5.getDimensionLabel(i)).trim()+":";
         s += (img5.getCurrentPosition(i)+1);
         s += "/";
         s += dimensions[i];
         s += "; ";
      }    	
      
      // Pixel Size display in header
      try {
         double pixSizeUm = Double.parseDouble(acqData_.getImageValue(i5d.getCurrentFrame()-1, i5d.getCurrentChannel()-1, i5d.getCurrentSlice()-1, ImagePropertyKeys.X_UM));
         //cal = getImagePlus().getCalibration();
         if (pixSizeUm > 0) {
            cal.setUnit("um");
            cal.pixelWidth = pixSizeUm;
            cal.pixelHeight = pixSizeUm;
         }
         img5.setCalibration(cal);
      } catch (Exception ex) {
        // N.B. intentionally ignoring this exception; in some cases there is no X stage calibration - KH
      }

      // x/y size
      if (cal.pixelWidth!=1.0 || cal.pixelHeight!=1.0)
         s += IJ.d2s(imp.getWidth()*cal.pixelWidth,2) + "x" + IJ.d2s(imp.getHeight()*cal.pixelHeight,2)
         + " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
      else
         s += imp.getWidth() + "x" + imp.getHeight() + " pixels; ";
      
      // Data type and size in kBytes
      int size = 1;
      for (int i=0; i<img5.getNDimensions(); ++i) {
         size *= dimensions[i];
      }
      size /= 1024;
      int type = imp.getType();
      switch (type) {
      case ImagePlus.GRAY8:
         s += "8-bit";
         break;
      case ImagePlus.GRAY16:
         s += "16-bit grayscale";
         size *= 2;
         break;
      case ImagePlus.GRAY32:
         s += "32-bit grayscale";
         size *= 4;
         break;
      case ImagePlus.COLOR_RGB:
         s += "8-bit RGBA";
         size *= 4;
      }
      if (size>=10000)    	
         s += "; " + (int)Math.round(size/1024.0) + "MB";
      else if (size>=1024) {
         double size2 = size/1024.0;
         s += "; " + IJ.d2s(size2,(int)size2==size2?0:1) + "MB";
      } else
         s += "; " + size + "K";
      g.drawString(s, 5, insets.top+TEXT_GAP);
      
      // micro-manager - madata display
      if (metaDlg_ != null) {
         metaDlg_.displayImageData(i5d.getCurrentFrame()-1, i5d.getCurrentChannel()-1, i5d.getCurrentSlice()-1);
      }
      

      if (pb_ != null && acqData_ != null)
         pb_.setImageInfo(ImageKey.getImageInfo(acqData_, i5d.getCurrentFrame()-1, i5d.getCurrentChannel()-1, i5d.getCurrentSlice()-1));
      try{
         pb_.setImageProcessor(imp.getProcessor());
      }
      catch( Exception e){
      }
   }
   
   public void run() {
      if (!isInitialized)
         return;
      while (!done) {
         synchronized(this) {
            try {wait(500);}
            catch(InterruptedException e) {
                ReportingUtils.logError(e);
            }
         }
         if (done) return;
         
         for(int i=2; i<nDimensions; ++i) {
            if (positions[i]>0) {
               int p = positions[i];
               positions[i] = 0;
               if (p!=i5d.getCurrentPosition(i)+1) {
                  i5d.setCurrentPosition(i, p-1);					
               }
            }
         }
         
      }
   }
   
   public ChannelControl getChannelControl() {
      return channelControl;
   }

   public String getAcqSavePath() {
      return acqSavePath_;
   }
   
   public void setAcqSavePath(String acqSavePath) {
      acqSavePath_ = acqSavePath;
   }

   public Image5D getImage5D(){
      return i5d;
   }
   
   public Color getColor(int channel) {
      ColorModel model = i5d.getProcessor(channel).getColorModel();
      int red = model.getRed(255);
      int green = model.getGreen(255);
      int blue = model.getBlue(255);

      Color c = new Color(red, green, blue);
      return c;   
   }
   
   public PlaybackPanel createPlaybackPanel () {
	   return new PlaybackPanel(this);
   }
   
   
   protected void addHorizontalScrollbars(Image5D imp) {
	      
	  int size;
       
      // Add slice selector
      ScrollbarWithLabel bar;
      size = imp.getNSlices();	
      bar = new ScrollbarWithLabel(Scrollbar.HORIZONTAL, 1, 1, 1, size+1, imp.getDimensionLabel(3));
      Scrollbars[3] = bar.getScrollbar();		
      add(bar, Image5DLayout.SLICE_SELECTOR);
      if (ij!=null) bar.getScrollbar().addKeyListener(ij);
      
      
      // Add frame selector
      size = imp.getNFrames();	
      bar = new ScrollbarWithLabel(Scrollbar.HORIZONTAL, 1, 1, 1, size+1, imp.getDimensionLabel(4));
      Scrollbars[4] = bar.getScrollbar();		
      add(bar, Image5DLayout.FRAME_SELECTOR);
      if (ij!=null) bar.getScrollbar().addKeyListener(ij);
   }
   
   
}
