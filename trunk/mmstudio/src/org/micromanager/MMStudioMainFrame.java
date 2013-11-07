///////////////////////////////////////////////////////////////////////////////
//FILE:          MMStudioMainFrame.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//AUTHOR:        Nenad Amodaj, nenad@amodaj.com, Jul 18, 2005
//               Modifications by Arthur Edelstein, Nico Stuurman, Henry Pinkard
//COPYRIGHT:     University of California, San Francisco, 2006-2012
//               100X Imaging Inc, www.100ximaging.com, 2008
//LICENSE:       This file is distributed under the BSD license.
//               License text is included with the source distribution.
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//CVS:          $Id$
//
package org.micromanager;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.MMCoreJ;
import mmcorej.MMEventCallback;
import mmcorej.StrVector;

import org.json.JSONObject;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.api.Autofocus;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.conf2.ConfiguratorDlg2;
import org.micromanager.conf2.MMConfigFileException;
import org.micromanager.conf2.MicroscopeModel;
import org.micromanager.graph.GraphData;
import org.micromanager.graph.GraphFrame;
import org.micromanager.navigation.CenterAndDragListener;
import org.micromanager.navigation.PositionList;
import org.micromanager.navigation.XYZKeyListener;
import org.micromanager.navigation.ZWheelListener;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.GUIColors;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.JavaUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.TextUtils;
import org.micromanager.utils.TooltipTextMaker;
import org.micromanager.utils.WaitDialog;







import bsh.EvalError;
import bsh.Interpreter;

import com.swtdesigner.SwingResourceManager;

import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Toolbar;

import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.AncestorListener;

import mmcorej.TaggedImage;

import org.json.JSONException;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.acquisition.LiveModeTimer;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.ImageCache;
import org.micromanager.acquisition.AcquisitionEngine;
import org.micromanager.acquisition.MetadataPanel;
import org.micromanager.acquisition.ProcessorStack;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.acquisition.TaggedImageStorageDiskDefault;
import org.micromanager.acquisition.TaggedImageStorageMultipageTiff;
import org.micromanager.acquisition.VirtualAcquisitionDisplay;
import org.micromanager.api.IAcquisitionEngine2010;
import org.micromanager.graph.HistogramSettings;
import org.micromanager.utils.DragDropUtil;
import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.FileDialogs.FileType;
import org.micromanager.utils.HotKeysDialog;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMKeyDispatcher;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.UIMonitor;



/*
 * Main panel and application class for the MMStudio.
 */
public class MMStudioMainFrame extends JFrame implements ScriptInterface {

   private static final String MICRO_MANAGER_TITLE = "Micro-Manager";
   private static final long serialVersionUID = 3556500289598574541L;
   private static final String MAIN_FRAME_X = "x";
   private static final String MAIN_FRAME_Y = "y";
   private static final String MAIN_FRAME_WIDTH = "width";
   private static final String MAIN_FRAME_HEIGHT = "height";
   private static final String MAIN_FRAME_DIVIDER_POS = "divider_pos";
   private static final String MAIN_EXPOSURE = "exposure";
   private static final String MAIN_SAVE_METHOD = "saveMethod";
   private static final String SYSTEM_CONFIG_FILE = "sysconfig_file";
   private static final String OPEN_ACQ_DIR = "openDataDir";
   private static final String SCRIPT_CORE_OBJECT = "mmc";
   private static final String SCRIPT_ACQENG_OBJECT = "acq";
   private static final String SCRIPT_GUI_OBJECT = "gui";
   private static final String AUTOFOCUS_DEVICE = "autofocus_device";
   private static final String MOUSE_MOVES_STAGE = "mouse_moves_stage";
   private static final String EXPOSURE_SETTINGS_NODE = "MainExposureSettings";
   private static final String CONTRAST_SETTINGS_NODE = "MainContrastSettings";
   private static final int TOOLTIP_DISPLAY_DURATION_MILLISECONDS = 15000;
   private static final int TOOLTIP_DISPLAY_INITIAL_DELAY_MILLISECONDS = 2000;


   // cfg file saving
   private static final String CFGFILE_ENTRY_BASE = "CFGFileEntry"; // + {0, 1, 2, 3, 4}
   // GUI components
   private JComboBox comboBinning_;
   private JComboBox shutterComboBox_;
   private JTextField textFieldExp_;
   private JLabel labelImageDimensions_;
   private JToggleButton toggleButtonLive_;
   private JButton toAlbumButton_;
   private JCheckBox autoShutterCheckBox_;
   private MMOptions options_;
   private boolean runsAsPlugin_;
   private JCheckBoxMenuItem centerAndDragMenuItem_;
   private JButton buttonSnap_;
   private JButton autofocusNowButton_;
   private JButton autofocusConfigureButton_;
   private JToggleButton toggleButtonShutter_;
   private GUIColors guiColors_;
   private GraphFrame profileWin_;
   private PropertyEditor propertyBrowser_;
   private CalibrationListDlg calibrationListDlg_;
   private AcqControlDlg acqControlWin_;
   private ReportProblemDialog reportProblemDialog_;
   
   private JMenu pluginMenu_;
   private ArrayList<PluginItem> plugins_;
   private List<MMListenerInterface> MMListeners_
           = (List<MMListenerInterface>)
           Collections.synchronizedList(new ArrayList<MMListenerInterface>());
   private List<Component> MMFrames_
           = (List<Component>)
           Collections.synchronizedList(new ArrayList<Component>());
   private AutofocusManager afMgr_;
   private final static String DEFAULT_CONFIG_FILE_NAME = "MMConfig_demo.cfg";
   private ArrayList<String> MRUConfigFiles_;
   private static final int maxMRUCfgs_ = 5;
   private String sysConfigFile_;
   private String startupScriptFile_;
   private String sysStateFile_ = "MMSystemState.cfg";
   private ConfigGroupPad configPad_;
   private LiveModeTimer liveModeTimer_;
   private GraphData lineProfileData_;
   // labels for standard devices
   private String cameraLabel_;
   private String zStageLabel_;
   private String shutterLabel_;
   private String xyStageLabel_;
   // applications settings
   private Preferences mainPrefs_;
   private Preferences systemPrefs_;
   private Preferences colorPrefs_;
   private Preferences exposurePrefs_;
   private Preferences contrastPrefs_;

   // MMcore
   private CMMCore core_;
   private AcquisitionWrapperEngine engine_;
   private PositionList posList_;
   private PositionListDlg posListDlg_;
   private String openAcqDirectory_ = "";
   private boolean running_;
   private boolean configChanged_ = false;
   private StrVector shutters_ = null;

   private JButton saveConfigButton_;
   private ScriptPanel scriptPanel_;
   private org.micromanager.utils.HotKeys hotKeys_;
   private CenterAndDragListener centerAndDragListener_;
   private ZWheelListener zWheelListener_;
   private XYZKeyListener xyzKeyListener_;
   private AcquisitionManager acqMgr_;
   private static VirtualAcquisitionDisplay simpleDisplay_;
   private Color[] multiCameraColors_ = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN};
   private int snapCount_ = -1;
   private boolean liveModeSuspended_;
   public Font defaultScriptFont_ = null;
   public static final String SIMPLE_ACQ = "Snap/Live Window";
   public static FileType MM_CONFIG_FILE
            = new FileType("MM_CONFIG_FILE",
                           "Micro-Manager Config File",
                           "./MyScope.cfg",
                           true, "cfg");

   // Our instance
   private static MMStudioMainFrame gui_;
   // Callback
   private CoreEventCallback cb_;
   // Lock invoked while shutting down
   private final Object shutdownLock_ = new Object();

   private JMenuBar menuBar_;
   private ConfigPadButtonPanel configPadButtonPanel_;
   private final JMenu switchConfigurationMenu_;
   private final MetadataPanel metadataPanel_;
   public static FileType MM_DATA_SET 
           = new FileType("MM_DATA_SET",
                 "Micro-Manager Image Location",
                 System.getProperty("user.home") + "/Untitled",
                 false, (String[]) null);
   private Thread acquisitionEngine2010LoadingThread = null;
   private Class acquisitionEngine2010Class = null;
   private IAcquisitionEngine2010 acquisitionEngine2010 = null;
   private final JSplitPane splitPane_;
   private volatile boolean ignorePropertyChanges_; 

   private JButton setRoiButton_;
   private  JButton clearRoiButton_;
   
   private DropTarget dt_;

   public ImageWindow getImageWin() {
      return getSnapLiveWin();
   }

   public ImageWindow getSnapLiveWin() {
      return simpleDisplay_.getHyperImage().getWindow();
   }

   public static VirtualAcquisitionDisplay getSimpleDisplay() {
      return simpleDisplay_;
   }

   public static void createSimpleDisplay(String name, ImageCache cache) throws MMScriptException {
      simpleDisplay_ = new VirtualAcquisitionDisplay(cache, name); 
   }
   
 public void checkSimpleAcquisition() {
      if (core_.getCameraDevice().length() == 0) {
         ReportingUtils.showError("No camera configured");
         return;
      }
      int width = (int) core_.getImageWidth();
      int height = (int) core_.getImageHeight();
      int depth = (int) core_.getBytesPerPixel();
      int bitDepth = (int) core_.getImageBitDepth();
      int numCamChannels = (int) core_.getNumberOfCameraChannels();

      try {
         if (acquisitionExists(SIMPLE_ACQ)) {
            if ((getAcquisitionImageWidth(SIMPLE_ACQ) != width)
                    || (getAcquisitionImageHeight(SIMPLE_ACQ) != height)
                    || (getAcquisitionImageByteDepth(SIMPLE_ACQ) != depth)
                    || (getAcquisitionImageBitDepth(SIMPLE_ACQ) != bitDepth)
                    || (getAcquisitionMultiCamNumChannels(SIMPLE_ACQ) != numCamChannels)) {  //Need to close and reopen simple window
               closeAcquisitionWindow(SIMPLE_ACQ);
            }
         }
         if (!acquisitionExists(SIMPLE_ACQ)) {
            openAcquisition(SIMPLE_ACQ, "", 1, numCamChannels, 1, true);
            if (numCamChannels > 1) {
               for (long i = 0; i < numCamChannels; i++) {
                  String chName = core_.getCameraChannelName(i);
                  int defaultColor = multiCameraColors_[(int) i % multiCameraColors_.length].getRGB();
                  setChannelColor(SIMPLE_ACQ, (int) i, getChannelColor(chName, defaultColor));
                  setChannelName(SIMPLE_ACQ, (int) i, chName);
               }
            }
            initializeSimpleAcquisition(SIMPLE_ACQ, width, height, depth, bitDepth, numCamChannels);
            getAcquisition(SIMPLE_ACQ).promptToSave(false);
            getAcquisition(SIMPLE_ACQ).toFront();
            this.updateCenterAndDragListener();
         }
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
      }

   }


 public void checkSimpleAcquisition(TaggedImage image) {
      try {
         JSONObject tags = image.tags;
         int width = MDUtils.getWidth(tags);
         int height = MDUtils.getHeight(tags);
         int depth = MDUtils.getDepth(tags);
         int bitDepth = MDUtils.getBitDepth(tags);
         int numCamChannels = (int) core_.getNumberOfCameraChannels();

         if (acquisitionExists(SIMPLE_ACQ)) {
            if ((getAcquisitionImageWidth(SIMPLE_ACQ) != width)
                    || (getAcquisitionImageHeight(SIMPLE_ACQ) != height)
                    || (getAcquisitionImageByteDepth(SIMPLE_ACQ) != depth)
                    || (getAcquisitionImageBitDepth(SIMPLE_ACQ) != bitDepth)
                    || (getAcquisitionMultiCamNumChannels(SIMPLE_ACQ) != numCamChannels)) {  //Need to close and reopen simple window
               closeAcquisitionWindow(SIMPLE_ACQ);
               // Seems that closeAcquisitionWindow also closes the acquisition...
               //closeAcquisition(SIMPLE_ACQ);
            }
         }
         if (!acquisitionExists(SIMPLE_ACQ)) {
            openAcquisition(SIMPLE_ACQ, "", 1, numCamChannels, 1, true);
            if (numCamChannels > 1) {
               for (long i = 0; i < numCamChannels; i++) {
                  String chName = core_.getCameraChannelName(i);
                  int defaultColor = multiCameraColors_[(int) i % multiCameraColors_.length].getRGB();
                  setChannelColor(SIMPLE_ACQ, (int) i, getChannelColor(chName, defaultColor));
                  setChannelName(SIMPLE_ACQ, (int) i, chName);
               }
            }
            initializeSimpleAcquisition(SIMPLE_ACQ, width, height, depth, bitDepth, numCamChannels);
            getAcquisition(SIMPLE_ACQ).promptToSave(false);
            getAcquisition(SIMPLE_ACQ).toFront();
            this.updateCenterAndDragListener();
         }
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
      }

   }

   public void saveChannelColor(String chName, int rgb)
   {
      if (colorPrefs_ != null) {
         colorPrefs_.putInt("Color_" + chName, rgb);      
      }          
   }
   
   public Color getChannelColor(String chName, int defaultColor)
   {  
      if (colorPrefs_ != null) {
         defaultColor = colorPrefs_.getInt("Color_" + chName, defaultColor);
      }
      return new Color(defaultColor);
   }
  
   public void enableRoiButtons(final boolean enabled) {
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               setRoiButton_.setEnabled(enabled);
               clearRoiButton_.setEnabled(enabled);
           }
       });
   }

   public void copyFromLiveModeToAlbum(VirtualAcquisitionDisplay display) throws MMScriptException, JSONException {
      ImageCache ic = display.getImageCache();
      int channels = ic.getSummaryMetadata().getInt("Channels");
      if (channels == 1) {
         //RGB or monchrome
         addToAlbum(ic.getImage(0, 0, 0, 0), ic.getDisplayAndComments());
      } else {
         //multicamera
         for (int i = 0; i < channels; i++) {
            addToAlbum(ic.getImage(i, 0, 0, 0), ic.getDisplayAndComments());
         }
      }
   }
   
    private void initializeHelpMenu() {
        // add help menu item
        final JMenu helpMenu = new JMenu();
        helpMenu.setText("Help");
        menuBar_.add(helpMenu);
        final JMenuItem usersGuideMenuItem = new JMenuItem();
        usersGuideMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ij.plugin.BrowserLauncher.openURL("http://micro-manager.org/wiki/Micro-Manager_User%27s_Guide");
                } catch (IOException e1) {
                    ReportingUtils.showError(e1);
                }
            }
        });
        usersGuideMenuItem.setText("User's Guide");
        helpMenu.add(usersGuideMenuItem);
        final JMenuItem configGuideMenuItem = new JMenuItem();
        configGuideMenuItem.addActionListener(new ActionListener() {

            @Override
             public void actionPerformed(ActionEvent e) {
                try {
                    ij.plugin.BrowserLauncher.openURL("http://micro-manager.org/wiki/Micro-Manager_Configuration_Guide");
                } catch (IOException e1) {
                    ReportingUtils.showError(e1);
                }
            }
        });
        configGuideMenuItem.setText("Configuration Guide");
        helpMenu.add(configGuideMenuItem);
        if (!systemPrefs_.getBoolean(RegistrationDlg.REGISTRATION, false)) {
            final JMenuItem registerMenuItem = new JMenuItem();
            registerMenuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        RegistrationDlg regDlg = new RegistrationDlg(systemPrefs_);
                        regDlg.setVisible(true);
                    } catch (Exception e1) {
                        ReportingUtils.showError(e1);
                    }
                }
            });
            registerMenuItem.setText("Register your copy of Micro-Manager...");
            helpMenu.add(registerMenuItem);
        }
        final MMStudioMainFrame thisFrame = this;
        final JMenuItem reportProblemMenuItem = new JMenuItem();
        reportProblemMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null == reportProblemDialog_) {
                    reportProblemDialog_ = new ReportProblemDialog(core_, thisFrame, options_);
                    thisFrame.addMMBackgroundListener(reportProblemDialog_);
                    reportProblemDialog_.setBackground(guiColors_.background.get(options_.displayBackground_));
                }
                reportProblemDialog_.setVisible(true);
            }
        });
        reportProblemMenuItem.setText("Report Problem...");
        helpMenu.add(reportProblemMenuItem);
        final JMenuItem aboutMenuItem = new JMenuItem();
        aboutMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MMAboutDlg dlg = new MMAboutDlg();
                String versionInfo = "MM Studio version: " + MMVersion.VERSION_STRING;
                versionInfo += "\n" + core_.getVersionInfo();
                versionInfo += "\n" + core_.getAPIVersionInfo();
                versionInfo += "\nUser: " + core_.getUserId();
                versionInfo += "\nHost: " + core_.getHostName();
                dlg.setVersionInfo(versionInfo);
                dlg.setVisible(true);
            }
        });
        aboutMenuItem.setText("About Micromanager");
        helpMenu.add(aboutMenuItem);
        menuBar_.validate();
    }

   private void updateSwitchConfigurationMenu() {
      switchConfigurationMenu_.removeAll();
      for (final String configFile : MRUConfigFiles_) {
         if (! configFile.equals(sysConfigFile_)) {
            JMenuItem configMenuItem = new JMenuItem();
            configMenuItem.setText(configFile);
            configMenuItem.addActionListener(new ActionListener() {
               String theConfigFile = configFile;
               public void actionPerformed(ActionEvent e) {
                  sysConfigFile_ = theConfigFile;
                  loadSystemConfiguration();
                  mainPrefs_.put(SYSTEM_CONFIG_FILE, sysConfigFile_);
               }
            });
            switchConfigurationMenu_.add(configMenuItem);
         }
      }
   }

   /**
    * Allows MMListeners to register themselves
    */
   public void addMMListener(MMListenerInterface newL) {
      if (MMListeners_.contains(newL))
         return;
      MMListeners_.add(newL);
   }

   /**
    * Allows MMListeners to remove themselves
    */
   public void removeMMListener(MMListenerInterface oldL) {
      if (!MMListeners_.contains(oldL))
         return;
      MMListeners_.remove(oldL);
   }

   /**
    * Lets JComponents register themselves so that their background can be
    * manipulated
    */
   public void addMMBackgroundListener(Component comp) {
      if (MMFrames_.contains(comp))
         return;
      MMFrames_.add(comp);
   }

   /**
    * Lets JComponents remove themselves from the list whose background gets
    * changes
    */
   public void removeMMBackgroundListener(Component comp) {
      if (!MMFrames_.contains(comp))
         return;
      MMFrames_.remove(comp);
   }

   /**
    * Part of ScriptInterface
    * Manipulate acquisition so that it looks like a burst
    */
   public void runBurstAcquisition() throws MMScriptException {
      double interval = engine_.getFrameIntervalMs();
      int nr = engine_.getNumFrames();
      boolean doZStack = engine_.isZSliceSettingEnabled();
      boolean doChannels = engine_.isChannelsSettingEnabled();
      engine_.enableZSliceSetting(false);
      engine_.setFrames(nr, 0);
      engine_.enableChannelsSetting(false);
      try {
         engine_.acquire();
      } catch (MMException e) {
         throw new MMScriptException(e);
      }
      engine_.setFrames(nr, interval);
      engine_.enableZSliceSetting(doZStack);
      engine_.enableChannelsSetting(doChannels);
   }

   public void runBurstAcquisition(int nr) throws MMScriptException {
      int originalNr = engine_.getNumFrames();
      double interval = engine_.getFrameIntervalMs();
      engine_.setFrames(nr, 0);
      this.runBurstAcquisition();
      engine_.setFrames(originalNr, interval);
   }

   public void runBurstAcquisition(int nr, String name, String root) throws MMScriptException {
      //String originalName = engine_.getDirName();
      String originalRoot = engine_.getRootName();
      engine_.setDirName(name);
      engine_.setRootName(root);
      this.runBurstAcquisition(nr);
      engine_.setRootName(originalRoot);
      //engine_.setDirName(originalDirName);
   }

   /**
    * Inserts version info for various components in the Corelog
    */
   public void logStartupProperties() {
      core_.enableDebugLog(options_.debugLogEnabled_);
      core_.logMessage("MM Studio version: " + getVersion());
      core_.logMessage(core_.getVersionInfo());
      core_.logMessage(core_.getAPIVersionInfo());
      core_.logMessage("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
      core_.logMessage("JVM: " + System.getProperty("java.vm.name") + ", version " + System.getProperty("java.version") + "; " + System.getProperty("sun.arch.data.model") + " bit");
   }

   /**
    * @deprecated
    * @throws MMScriptException
    */
   public void startBurstAcquisition() throws MMScriptException {
      runAcquisition();
   }

   public boolean isBurstAcquisitionRunning() throws MMScriptException {
      if (engine_ == null)
         return false;
      return engine_.isAcquisitionRunning();
   }

   private void startLoadingPipelineClass() {
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      acquisitionEngine2010LoadingThread = new Thread("Pipeline Class loading thread") {
         @Override
         public void run() {
            try {
               acquisitionEngine2010Class  = Class.forName("org.micromanager.AcquisitionEngine2010");
            } catch (Exception ex) {
               ReportingUtils.logError(ex);
               acquisitionEngine2010Class = null;
            }
         }
      };
      acquisitionEngine2010LoadingThread.start();
   }

   public ImageCache getAcquisitionImageCache(String acquisitionName) throws MMScriptException {
      return getAcquisition(acquisitionName).getImageCache();
   }

   
   /*
    * Shows images as they appear in the default display window. Uses
    * the default processor stack to process images as they arrive on
    * the rawImageQueue.
    */
    public void runDisplayThread(BlockingQueue rawImageQueue, 
            final DisplayImageRoutine displayImageRoutine) {
        final BlockingQueue processedImageQueue = 
                ProcessorStack.run(rawImageQueue, getAcquisitionEngine().getImageProcessors());
        new Thread("Display thread") {
         @Override
            public void run() {
                try {
                    TaggedImage image;
                    do {
                        image = (TaggedImage) processedImageQueue.take();
                        if (image != TaggedImageQueue.POISON) {
                            displayImageRoutine.show(image);
                        }
                    } while (image != TaggedImageQueue.POISON);
                } catch (InterruptedException ex) {
                    ReportingUtils.logError(ex);
                }
            }
        }.start();
    }

  

   public interface DisplayImageRoutine {
      public void show(TaggedImage image);
   }
 
   /**
    * Callback to update GUI when a change happens in the MMCore.
    */
   public class CoreEventCallback extends MMEventCallback {

      public CoreEventCallback() {
         super();
      }

      @Override
      public void onPropertiesChanged() {
         // TODO: remove test once acquisition engine is fully multithreaded
         if (engine_ != null && engine_.isAcquisitionRunning()) {
            core_.logMessage("Notification from MMCore ignored because acquistion is running!", true);
         } else {
            if (ignorePropertyChanges_) {
               core_.logMessage("Notification from MMCore ignored since the system is still loading", true);
            } else {
               core_.updateSystemStateCache();
               updateGUI(true);
               // update all registered listeners 
               for (MMListenerInterface mmIntf : MMListeners_) {
                  mmIntf.propertiesChangedAlert();
               }
               core_.logMessage("Notification from MMCore!", true);
            }
         }
      }

      @Override
      public void onPropertyChanged(String deviceName, String propName, String propValue) {
         core_.logMessage("Notification for Device: " + deviceName + " Property: " +
               propName + " changed to value: " + propValue, true);
         // update all registered listeners
         for (MMListenerInterface mmIntf:MMListeners_) {
            mmIntf.propertyChangedAlert(deviceName, propName, propValue);
         }
      }

      @Override
      public void onConfigGroupChanged(String groupName, String newConfig) {
         try {
            configPad_.refreshGroup(groupName, newConfig);
            for (MMListenerInterface mmIntf:MMListeners_) {
               mmIntf.configGroupChangedAlert(groupName, newConfig);
            }
         } catch (Exception e) {
         }
      }
      
      @Override
      public void onSystemConfigurationLoaded() {
         for (MMListenerInterface mmIntf:MMListeners_) {
            mmIntf.systemConfigurationLoaded();
         }
      }

      @Override
      public void onPixelSizeChanged(double newPixelSizeUm) {
         updatePixSizeUm (newPixelSizeUm);
         for (MMListenerInterface mmIntf:MMListeners_) {
            mmIntf.pixelSizeChangedAlert(newPixelSizeUm);
         }
      }

      @Override
      public void onStagePositionChanged(String deviceName, double pos) {
         if (deviceName.equals(zStageLabel_)) {
            updateZPos(pos);
            for (MMListenerInterface mmIntf:MMListeners_) {
               mmIntf.stagePositionChangedAlert(deviceName, pos);
            }
         }
      }

      @Override
      public void onStagePositionChangedRelative(String deviceName, double pos) {
         if (deviceName.equals(zStageLabel_))
            updateZPosRelative(pos);
      }

      @Override
      public void onXYStagePositionChanged(String deviceName, double xPos, double yPos) {
         if (deviceName.equals(xyStageLabel_)) {
            updateXYPos(xPos, yPos);
            for (MMListenerInterface mmIntf:MMListeners_) {
               mmIntf.xyStagePositionChanged(deviceName, xPos, yPos);
            }
         }
      }

      @Override
      public void onXYStagePositionChangedRelative(String deviceName, double xPos, double yPos) {
         if (deviceName.equals(xyStageLabel_))
            updateXYPosRelative(xPos, yPos);
      }
      
      @Override
      public void onExposureChanged(String deviceName, double exposure) {
         if (deviceName.equals(cameraLabel_)){
            // update exposure in gui
            textFieldExp_.setText(NumberUtils.doubleToDisplayString(exposure));  
         }
         for (MMListenerInterface mmIntf:MMListeners_) {
            mmIntf.exposureChanged(deviceName, exposure);
         }
      }

   }

   private class PluginItem {

      public Class<?> pluginClass = null;
      public String menuItem = "undefined";
      public MMPlugin plugin = null;
      public String className = "";

      public void instantiate() {

         try {
            if (plugin == null) {
               plugin = (MMPlugin) pluginClass.newInstance();
            }
         } catch (InstantiationException e) {
            ReportingUtils.logError(e);
         } catch (IllegalAccessException e) {
            ReportingUtils.logError(e);
         }
         plugin.setApp(MMStudioMainFrame.this);
      }
   }

   /*
    * Simple class used to cache static info
    */
   private class StaticInfo {

      public long width_;
      public long height_;
      public long bytesPerPixel_;
      public long imageBitDepth_;
      public double pixSizeUm_;
      public double zPos_;
      public double x_;
      public double y_;
   }
   private StaticInfo staticInfo_ = new StaticInfo();

   /**
    * Main procedure for stand alone operation.
    */
   public static void main(String args[]) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         MMStudioMainFrame frame = new MMStudioMainFrame(false);
         frame.setVisible(true);
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      } catch (Throwable e) {
         ReportingUtils.showError(e, "A java error has caused Micro-Manager to exit.");
         System.exit(1);
      }
   }

   public MMStudioMainFrame(boolean pluginStatus) {
      super();

      startLoadingPipelineClass();

      options_ = new MMOptions();
      try {
         options_.loadSettings();
      } catch (NullPointerException ex) {
         ReportingUtils.logError(ex);
      }

      UIMonitor.enable(options_.debugLogEnabled_);
      
      guiColors_ = new GUIColors();

      plugins_ = new ArrayList<PluginItem>();

      gui_ = this;

      runsAsPlugin_ = pluginStatus;
      setIconImage(SwingResourceManager.getImage(MMStudioMainFrame.class,
            "icons/microscope.gif"));
      running_ = true;

      acqMgr_ = new AcquisitionManager();
      
      sysConfigFile_ = System.getProperty("user.dir") + "/"
            + DEFAULT_CONFIG_FILE_NAME;

      if (options_.startupScript_.length() > 0) {
         startupScriptFile_ = System.getProperty("user.dir") + "/"
                 + options_.startupScript_;
      } else {
         startupScriptFile_ = "";
      }

      ReportingUtils.SetContainingFrame(gui_);

      // set the location for app preferences
      try {
         mainPrefs_ = Preferences.userNodeForPackage(this.getClass());
      } catch (Exception e) {
         ReportingUtils.logError(e);
      }
      systemPrefs_ = mainPrefs_;
      
      colorPrefs_ = mainPrefs_.node(mainPrefs_.absolutePath() + "/" + 
              AcqControlDlg.COLOR_SETTINGS_NODE);
      exposurePrefs_ = mainPrefs_.node(mainPrefs_.absolutePath() + "/" + 
              EXPOSURE_SETTINGS_NODE);
      contrastPrefs_ = mainPrefs_.node(mainPrefs_.absolutePath() + "/" +
              CONTRAST_SETTINGS_NODE);
      
      // check system preferences
      try {
         Preferences p = Preferences.systemNodeForPackage(this.getClass());
         if (null != p) {
            // if we can not write to the systemPrefs, use AppPrefs instead
            if (JavaUtils.backingStoreAvailable(p)) {
               systemPrefs_ = p;
            }
         }
      } catch (Exception e) {
         ReportingUtils.logError(e);
      }

      // show registration dialog if not already registered
      // first check user preferences (for legacy compatibility reasons)
      boolean userReg = mainPrefs_.getBoolean(RegistrationDlg.REGISTRATION,
            false) || mainPrefs_.getBoolean(RegistrationDlg.REGISTRATION_NEVER, false);

      if (!userReg) {
         boolean systemReg = systemPrefs_.getBoolean(
               RegistrationDlg.REGISTRATION, false) || systemPrefs_.getBoolean(RegistrationDlg.REGISTRATION_NEVER, false);
         if (!systemReg) {
            // prompt for registration info
            RegistrationDlg dlg = new RegistrationDlg(systemPrefs_);
            dlg.setVisible(true);
         }
      }

      // load application preferences
      // NOTE: only window size and position preferences are loaded,
      // not the settings for the camera and live imaging -
      // attempting to set those automatically on startup may cause problems
      // with the hardware
      int x = mainPrefs_.getInt(MAIN_FRAME_X, 100);
      int y = mainPrefs_.getInt(MAIN_FRAME_Y, 100);
      int width = mainPrefs_.getInt(MAIN_FRAME_WIDTH, 644);
      int height = mainPrefs_.getInt(MAIN_FRAME_HEIGHT, 570);
      int dividerPos = mainPrefs_.getInt(MAIN_FRAME_DIVIDER_POS, 200);    
      openAcqDirectory_ = mainPrefs_.get(OPEN_ACQ_DIR, "");
      try {
         ImageUtils.setImageStorageClass(Class.forName (mainPrefs_.get(MAIN_SAVE_METHOD,
                 ImageUtils.getImageStorageClass().getName()) ) );
      } catch (ClassNotFoundException ex) {
         ReportingUtils.logError(ex, "Class not found error.  Should never happen");
      }

      ToolTipManager ttManager = ToolTipManager.sharedInstance();
      ttManager.setDismissDelay(TOOLTIP_DISPLAY_DURATION_MILLISECONDS);
      ttManager.setInitialDelay(TOOLTIP_DISPLAY_INITIAL_DELAY_MILLISECONDS);
      
      setBounds(x, y, width, height);
      setExitStrategy(options_.closeOnExit_);
      setTitle(MICRO_MANAGER_TITLE + " " + MMVersion.VERSION_STRING);
      setBackground(guiColors_.background.get((options_.displayBackground_)));
      SpringLayout topLayout = new SpringLayout();
      
      this.setMinimumSize(new Dimension(605,480));
      JPanel topPanel = new JPanel();
      topPanel.setLayout(topLayout);
      topPanel.setMinimumSize(new Dimension(580, 195));

      class ListeningJPanel extends JPanel implements AncestorListener {

         public void ancestorMoved(AncestorEvent event) {
            //System.out.println("moved!");
         }

         public void ancestorRemoved(AncestorEvent event) {}
         public void ancestorAdded(AncestorEvent event) {}

      }

      ListeningJPanel bottomPanel = new ListeningJPanel();
      bottomPanel.setLayout(topLayout);
      
      splitPane_ = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
              topPanel, bottomPanel);
      splitPane_.setBorder(BorderFactory.createEmptyBorder());
      splitPane_.setDividerLocation(dividerPos);
      splitPane_.setResizeWeight(0.0);
      splitPane_.addAncestorListener(bottomPanel);
      getContentPane().add(splitPane_);


      // Snap button
      // -----------
      buttonSnap_ = new JButton();
      buttonSnap_.setIconTextGap(6);
      buttonSnap_.setText("Snap");
      buttonSnap_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "/org/micromanager/icons/camera.png"));
      buttonSnap_.setFont(new Font("Arial", Font.PLAIN, 10));
      buttonSnap_.setToolTipText("Snap single image");
      buttonSnap_.setMaximumSize(new Dimension(0, 0));
      buttonSnap_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            doSnap();
         }
      });
      topPanel.add(buttonSnap_);
      topLayout.putConstraint(SpringLayout.SOUTH, buttonSnap_, 25,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, buttonSnap_, 4,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, buttonSnap_, 95,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, buttonSnap_, 7,
            SpringLayout.WEST, topPanel);

      // Initialize
      // ----------

      // Exposure field
      // ---------------
      final JLabel label_1 = new JLabel();
      label_1.setFont(new Font("Arial", Font.PLAIN, 10));
      label_1.setText("Exposure [ms]");
      topPanel.add(label_1);
      topLayout.putConstraint(SpringLayout.EAST, label_1, 198,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, label_1, 111,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, label_1, 39,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, label_1, 23,
            SpringLayout.NORTH, topPanel);

      textFieldExp_ = new JTextField();
      textFieldExp_.addFocusListener(new FocusAdapter() {
         @Override
         public void focusLost(FocusEvent fe) {
            synchronized(shutdownLock_) {
            if (core_ != null)
               setExposure();
            }
         }
      });
      textFieldExp_.setFont(new Font("Arial", Font.PLAIN, 10));
      textFieldExp_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            setExposure();
         }
      });
      topPanel.add(textFieldExp_);
      topLayout.putConstraint(SpringLayout.SOUTH, textFieldExp_, 40,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, textFieldExp_, 21,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, textFieldExp_, 276,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, textFieldExp_, 203,
            SpringLayout.WEST, topPanel);

      // Live button
      // -----------
      toggleButtonLive_ = new JToggleButton();
      toggleButtonLive_.setName("Live");
      toggleButtonLive_.setMargin(new Insets(2, 2, 2, 2));
      toggleButtonLive_.setIconTextGap(1);
      toggleButtonLive_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/camera_go.png"));
      toggleButtonLive_.setIconTextGap(6);
      toggleButtonLive_.setToolTipText("Continuous live view");
      toggleButtonLive_.setFont(new Font("Arial", Font.PLAIN, 10));
      toggleButtonLive_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {            
            enableLiveMode(!isLiveModeOn());
         }
      });

      toggleButtonLive_.setText("Live");
      topPanel.add(toggleButtonLive_);
      topLayout.putConstraint(SpringLayout.SOUTH, toggleButtonLive_, 47,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, toggleButtonLive_, 26,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, toggleButtonLive_, 95,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, toggleButtonLive_, 7,
            SpringLayout.WEST, topPanel);

      // Acquire button
      // -----------
      toAlbumButton_ = new JButton();
      toAlbumButton_.setMargin(new Insets(2, 2, 2, 2));
      toAlbumButton_.setIconTextGap(1);
      toAlbumButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/camera_plus_arrow.png"));
      toAlbumButton_.setIconTextGap(6);
      toAlbumButton_.setToolTipText("Acquire single frame and add to an album");
      toAlbumButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      toAlbumButton_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            snapAndAddToImage5D();
         }
      });

      toAlbumButton_.setText("Album");
      topPanel.add(toAlbumButton_);
      topLayout.putConstraint(SpringLayout.SOUTH, toAlbumButton_, 69,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, toAlbumButton_, 48,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, toAlbumButton_, 95,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, toAlbumButton_, 7,
            SpringLayout.WEST, topPanel);

      // Shutter button
      // --------------

      toggleButtonShutter_ = new JToggleButton();
      toggleButtonShutter_.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            toggleShutter();
         }


      });
      toggleButtonShutter_.setToolTipText("Open/close the shutter");
      toggleButtonShutter_.setIconTextGap(6);
      toggleButtonShutter_.setFont(new Font("Arial", Font.BOLD, 10));
      toggleButtonShutter_.setText("Open");
      topPanel.add(toggleButtonShutter_);
      topLayout.putConstraint(SpringLayout.EAST, toggleButtonShutter_,
            275, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, toggleButtonShutter_,
            203, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, toggleButtonShutter_,
            138 - 21, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, toggleButtonShutter_,
            117 - 21, SpringLayout.NORTH, topPanel);

      // Active shutter label
      final JLabel activeShutterLabel = new JLabel();
      activeShutterLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      activeShutterLabel.setText("Shutter");
      topPanel.add(activeShutterLabel);
      topLayout.putConstraint(SpringLayout.SOUTH, activeShutterLabel,
            108 - 22, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, activeShutterLabel,
            95 - 22, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, activeShutterLabel,
            160 - 2, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, activeShutterLabel,
            113 - 2, SpringLayout.WEST, topPanel);

      // Active shutter Combo Box
      shutterComboBox_ = new JComboBox();
      shutterComboBox_.setName("Shutter");
      shutterComboBox_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent arg0) {
            try {
               if (shutterComboBox_.getSelectedItem() != null) {
                  core_.setShutterDevice((String) shutterComboBox_.getSelectedItem());
               }
            } catch (Exception e) {
               ReportingUtils.showError(e);
            }
         }
      });
      
      topPanel.add(shutterComboBox_);
      topLayout.putConstraint(SpringLayout.SOUTH, shutterComboBox_,
            114 - 22, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, shutterComboBox_,
            92 - 22, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, shutterComboBox_, 275,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, shutterComboBox_, 170,
            SpringLayout.WEST, topPanel);

      menuBar_ = new JMenuBar();
      setJMenuBar(menuBar_);
      
      
      // File menu

      final JMenu fileMenu = new JMenu();
      fileMenu.setText("File");
      menuBar_.add(fileMenu);

      final JMenuItem openMenuItem = new JMenuItem();
      openMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            new Thread() {
               @Override
               public void run() {
                  openAcquisitionData(false);
               }
            }.start();
         }
      });
      openMenuItem.setText("Open (Virtual)...");
      fileMenu.add(openMenuItem);

      final JMenuItem openInRamMenuItem = new JMenuItem();
      openInRamMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            new Thread() {
               @Override
               public void run() {
                  openAcquisitionData(true);
               }
            }.start();
         }
      });
      openInRamMenuItem.setText("Open (RAM)...");
      fileMenu.add(openInRamMenuItem);

      fileMenu.addSeparator();

      final JMenuItem exitMenuItem = new JMenuItem();
      exitMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            closeSequence(false);
         }
      });
      fileMenu.add(exitMenuItem);
      exitMenuItem.setText("Exit");

      
      // Tools menu
      
      final JMenu toolsMenu = new JMenu();
      toolsMenu.setText("Tools");
      menuBar_.add(toolsMenu);

      final JMenuItem refreshMenuItem = new JMenuItem();
      refreshMenuItem.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "icons/arrow_refresh.png"));
      refreshMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            core_.updateSystemStateCache();
            updateGUI(true);
         }
      });
      refreshMenuItem.setText("Refresh GUI");
      refreshMenuItem.setToolTipText("Refresh all GUI controls directly from the hardware");
      toolsMenu.add(refreshMenuItem);

      final JMenuItem rebuildGuiMenuItem = new JMenuItem();
      rebuildGuiMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            initializeGUI();
            core_.updateSystemStateCache(); 
         }
      });
      rebuildGuiMenuItem.setText("Rebuild GUI");
      rebuildGuiMenuItem.setToolTipText("Regenerate micromanager user interface");
      toolsMenu.add(rebuildGuiMenuItem);

      toolsMenu.addSeparator();

      final JMenuItem scriptPanelMenuItem = new JMenuItem();
      toolsMenu.add(scriptPanelMenuItem);
      scriptPanelMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            scriptPanel_.setVisible(true);
         }
      });
      scriptPanelMenuItem.setText("Script Panel...");
      scriptPanelMenuItem.setToolTipText("Open micromanager script editor window");
      
      final JMenuItem hotKeysMenuItem = new JMenuItem();
      toolsMenu.add(hotKeysMenuItem);
      hotKeysMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            HotKeysDialog hk = new HotKeysDialog
                    (guiColors_.background.get((options_.displayBackground_)));
            //hk.setBackground(guiColors_.background.get((options_.displayBackground_)));
         }
      });
      hotKeysMenuItem.setText("Shortcuts...");
      hotKeysMenuItem.setToolTipText("Create keyboard shortcuts to activate image acquisition, mark positions, or run custom scripts");

      final JMenuItem propertyEditorMenuItem = new JMenuItem();
      toolsMenu.add(propertyEditorMenuItem);
      propertyEditorMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            createPropertyEditor();
         }
      });
      propertyEditorMenuItem.setText("Device/Property Browser...");
      propertyEditorMenuItem.setToolTipText("Open new window to view and edit property values in current configuration");
      
      toolsMenu.addSeparator();

      final JMenuItem xyListMenuItem = new JMenuItem();
      xyListMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0) {
            showXYPositionList();
         }
      });
      xyListMenuItem.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "icons/application_view_list.png"));
      xyListMenuItem.setText("XY List...");
      toolsMenu.add(xyListMenuItem);
      xyListMenuItem.setToolTipText("Open position list manager window");

      final JMenuItem acquisitionMenuItem = new JMenuItem();
      acquisitionMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            openAcqControlDialog();
         }
      });
      acquisitionMenuItem.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "icons/film.png"));
      acquisitionMenuItem.setText("Multi-Dimensional Acquisition...");
      toolsMenu.add(acquisitionMenuItem);
      acquisitionMenuItem.setToolTipText("Open multi-dimensional acquisition window");
      
      
      centerAndDragMenuItem_ = new JCheckBoxMenuItem();     
      
      centerAndDragMenuItem_.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            updateCenterAndDragListener();
            IJ.setTool(Toolbar.HAND);
            mainPrefs_.putBoolean(MOUSE_MOVES_STAGE, centerAndDragMenuItem_.isSelected());
         }
      });

      centerAndDragMenuItem_.setText("Mouse Moves Stage (use Hand Tool)");
      centerAndDragMenuItem_.setSelected(mainPrefs_.getBoolean(MOUSE_MOVES_STAGE, false));
      centerAndDragMenuItem_.setToolTipText("When enabled, double clicking or dragging in the snap/live\n"
                                          + "window moves the XY-stage. Requires the hand tool.");
      toolsMenu.add(centerAndDragMenuItem_);

      
      final JMenuItem calibrationMenuItem = new JMenuItem();
      toolsMenu.add(calibrationMenuItem);
      calibrationMenuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            createCalibrationListDlg();
         }
      });
      calibrationMenuItem.setText("Pixel Size Calibration...");
      toolsMenu.add(calibrationMenuItem);
      
      String calibrationTooltip = "Define size calibrations specific to each objective lens.  " +
    		  "When the objective in use has a calibration defined, " +
    		  "micromanager will automatically use it when " +
    		  "calculating metadata";
      
      String mrjProp = System.getProperty("mrj.version");
      if (mrjProp != null && !mrjProp.equals("")) {// running on a mac
         calibrationMenuItem.setToolTipText(calibrationTooltip);
      }
      else {
         calibrationMenuItem.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip(calibrationTooltip));
      }
      toolsMenu.addSeparator();

      final JMenuItem configuratorMenuItem = new JMenuItem();
      configuratorMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0) {
            runHardwareWizard();
         }
      });
      
      configuratorMenuItem.setText("Hardware Configuration Wizard...");
      toolsMenu.add(configuratorMenuItem);
      configuratorMenuItem.setToolTipText("Open wizard to create new hardware configuration");      
      
      final JMenuItem loadSystemConfigMenuItem = new JMenuItem();
      toolsMenu.add(loadSystemConfigMenuItem);
      loadSystemConfigMenuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            loadConfiguration();
            initializeGUI();
         }
      });
      loadSystemConfigMenuItem.setText("Load Hardware Configuration...");
      loadSystemConfigMenuItem.setToolTipText("Un-initialize current configuration and initialize new one");

      switchConfigurationMenu_ = new JMenu();
      for (int i=0; i<5; i++)
      {
         JMenuItem configItem = new JMenuItem();
         configItem.setText(Integer.toString(i));
         switchConfigurationMenu_.add(configItem);
      }

      final JMenuItem reloadConfigMenuItem = new JMenuItem();
      toolsMenu.add(reloadConfigMenuItem);
      reloadConfigMenuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            loadSystemConfiguration();
            initializeGUI();
         }
      });
      reloadConfigMenuItem.setText("Reload Hardware Configuration");
      reloadConfigMenuItem.setToolTipText("Un-initialize current configuration and initialize most recently loaded configuration");

      switchConfigurationMenu_.setText("Switch Hardware Configuration");
      toolsMenu.add(switchConfigurationMenu_);
      switchConfigurationMenu_.setToolTipText("Switch between recently used configurations");

      final JMenuItem saveConfigurationPresetsMenuItem = new JMenuItem();
      saveConfigurationPresetsMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0) {
            saveConfigPresets();
            updateChannelCombos();
         }
      });
      saveConfigurationPresetsMenuItem.setText("Save Configuration Settings as...");
      toolsMenu.add(saveConfigurationPresetsMenuItem);
      saveConfigurationPresetsMenuItem.setToolTipText("Save current configuration settings as new configuration file");


      toolsMenu.addSeparator();

      final MMStudioMainFrame thisInstance = this;
      final JMenuItem optionsMenuItem = new JMenuItem();
      optionsMenuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            int oldBufsize = options_.circularBufferSizeMB_;

            OptionsDlg dlg = new OptionsDlg(options_, core_, mainPrefs_,
                  thisInstance, sysConfigFile_);
            dlg.setVisible(true);
            // adjust memory footprint if necessary
            if (oldBufsize != options_.circularBufferSizeMB_) {
               try {
                  core_.setCircularBufferMemoryFootprint(options_.circularBufferSizeMB_);
               } catch (Exception exc) {
                  ReportingUtils.showError(exc);
               }
            }
         }
      });
      optionsMenuItem.setText("Options...");
      toolsMenu.add(optionsMenuItem);

      final JLabel binningLabel = new JLabel();
      binningLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      binningLabel.setText("Binning");
      topPanel.add(binningLabel);
      topLayout.putConstraint(SpringLayout.SOUTH, binningLabel, 64,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, binningLabel, 43,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, binningLabel, 200 - 1,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, binningLabel, 112 - 1,
            SpringLayout.WEST, topPanel);

      metadataPanel_ = new MetadataPanel();
      bottomPanel.add(metadataPanel_);
      topLayout.putConstraint(SpringLayout.SOUTH, metadataPanel_, 0,
            SpringLayout.SOUTH, bottomPanel);
      topLayout.putConstraint(SpringLayout.NORTH, metadataPanel_, 0,
            SpringLayout.NORTH, bottomPanel);
      topLayout.putConstraint(SpringLayout.EAST, metadataPanel_, 0,
            SpringLayout.EAST, bottomPanel);
      topLayout.putConstraint(SpringLayout.WEST, metadataPanel_, 0,
            SpringLayout.WEST, bottomPanel);
      metadataPanel_.setBorder(BorderFactory.createEmptyBorder());



      comboBinning_ = new JComboBox();
      comboBinning_.setName("Binning");
      comboBinning_.setFont(new Font("Arial", Font.PLAIN, 10));
      comboBinning_.setMaximumRowCount(4);
      comboBinning_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            changeBinning();
         }
      });
      topPanel.add(comboBinning_);
      topLayout.putConstraint(SpringLayout.EAST, comboBinning_, 275,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, comboBinning_, 200,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, comboBinning_, 66,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, comboBinning_, 43,
            SpringLayout.NORTH, topPanel);



      final JLabel cameraSettingsLabel = new JLabel();
      cameraSettingsLabel.setFont(new Font("Arial", Font.BOLD, 11));
      cameraSettingsLabel.setText("Camera settings");
      topPanel.add(cameraSettingsLabel);
      topLayout.putConstraint(SpringLayout.EAST, cameraSettingsLabel,
            211, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, cameraSettingsLabel, 6,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, cameraSettingsLabel,
            109, SpringLayout.WEST, topPanel);

      
      labelImageDimensions_ = new JLabel();
      labelImageDimensions_.setFont(new Font("Arial", Font.PLAIN, 10));
      topPanel.add(labelImageDimensions_);
      topLayout.putConstraint(SpringLayout.SOUTH, labelImageDimensions_,
            0, SpringLayout.SOUTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, labelImageDimensions_,
            -20, SpringLayout.SOUTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, labelImageDimensions_,
            0, SpringLayout.EAST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, labelImageDimensions_,
            5, SpringLayout.WEST, topPanel);
      
      
      configPad_ = new ConfigGroupPad();
      configPadButtonPanel_ = new ConfigPadButtonPanel();
      configPadButtonPanel_.setConfigPad(configPad_);
      configPadButtonPanel_.setGUI(MMStudioMainFrame.getInstance());
      
      configPad_.setFont(new Font("", Font.PLAIN, 10));
      topPanel.add(configPad_);
      topLayout.putConstraint(SpringLayout.EAST, configPad_, -4,
            SpringLayout.EAST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, configPad_, 5,
            SpringLayout.EAST, comboBinning_);
      topLayout.putConstraint(SpringLayout.SOUTH, configPad_, -4,
            SpringLayout.NORTH, configPadButtonPanel_);
      topLayout.putConstraint(SpringLayout.NORTH, configPad_, 21,
            SpringLayout.NORTH, topPanel);


      topPanel.add(configPadButtonPanel_);
      topLayout.putConstraint(SpringLayout.EAST, configPadButtonPanel_, -4,
            SpringLayout.EAST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, configPadButtonPanel_, 5,
            SpringLayout.EAST, comboBinning_);
      topLayout.putConstraint(SpringLayout.NORTH, configPadButtonPanel_, -40,
            SpringLayout.SOUTH, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, configPadButtonPanel_, -20,
            SpringLayout.SOUTH, topPanel);


      final JLabel stateDeviceLabel = new JLabel();
      stateDeviceLabel.setFont(new Font("Arial", Font.BOLD, 11));
      stateDeviceLabel.setText("Configuration settings");
      topPanel.add(stateDeviceLabel);
      topLayout.putConstraint(SpringLayout.SOUTH, stateDeviceLabel, 0,
            SpringLayout.SOUTH, cameraSettingsLabel);
      topLayout.putConstraint(SpringLayout.NORTH, stateDeviceLabel, 0,
            SpringLayout.NORTH, cameraSettingsLabel);
      topLayout.putConstraint(SpringLayout.EAST, stateDeviceLabel, 150,
            SpringLayout.WEST, configPad_);
      topLayout.putConstraint(SpringLayout.WEST, stateDeviceLabel, 0,
            SpringLayout.WEST, configPad_);


      final JButton buttonAcqSetup = new JButton();
      buttonAcqSetup.setMargin(new Insets(2, 2, 2, 2));
      buttonAcqSetup.setIconTextGap(1);
      buttonAcqSetup.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "/org/micromanager/icons/film.png"));
      buttonAcqSetup.setToolTipText("Open multi-dimensional acquisition window");
      buttonAcqSetup.setFont(new Font("Arial", Font.PLAIN, 10));
      buttonAcqSetup.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            openAcqControlDialog();
         }
      });
      buttonAcqSetup.setText("Multi-D Acq.");
      topPanel.add(buttonAcqSetup);
      topLayout.putConstraint(SpringLayout.SOUTH, buttonAcqSetup, 91,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, buttonAcqSetup, 70,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, buttonAcqSetup, 95,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, buttonAcqSetup, 7,
            SpringLayout.WEST, topPanel);

      autoShutterCheckBox_ = new JCheckBox();
      autoShutterCheckBox_.setFont(new Font("Arial", Font.PLAIN, 10));
      autoShutterCheckBox_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
             shutterLabel_ = core_.getShutterDevice();
             if (shutterLabel_.length() == 0) {
                toggleButtonShutter_.setEnabled(false);
                return;
             }
            if (autoShutterCheckBox_.isSelected()) {
               try {
                  core_.setAutoShutter(true);
                  core_.setShutterOpen(false);
                  toggleButtonShutter_.setSelected(false);
                  toggleButtonShutter_.setText("Open");
                  toggleButtonShutter_.setEnabled(false);
               } catch (Exception e2) {
                  ReportingUtils.logError(e2);
               }
            } else {
               try {
               core_.setAutoShutter(false);
               core_.setShutterOpen(false);
               toggleButtonShutter_.setEnabled(true);
               toggleButtonShutter_.setText("Open");
               } catch (Exception exc) {
                  ReportingUtils.logError(exc);
               }
            }
          
         }
      });
      autoShutterCheckBox_.setIconTextGap(6);
      autoShutterCheckBox_.setHorizontalTextPosition(SwingConstants.LEADING);
      autoShutterCheckBox_.setText("Auto shutter");
      topPanel.add(autoShutterCheckBox_);
      topLayout.putConstraint(SpringLayout.EAST, autoShutterCheckBox_,
            202 - 3, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, autoShutterCheckBox_,
            110 - 3, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, autoShutterCheckBox_,
            141 - 22, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, autoShutterCheckBox_,
            118 - 22, SpringLayout.NORTH, topPanel);

    
      final JButton refreshButton = new JButton();
      refreshButton.setMargin(new Insets(2, 2, 2, 2));
      refreshButton.setIconTextGap(1);
      refreshButton.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/arrow_refresh.png"));
      refreshButton.setFont(new Font("Arial", Font.PLAIN, 10));
      refreshButton.setToolTipText("Refresh all GUI controls directly from the hardware");
      refreshButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            core_.updateSystemStateCache(); 
            updateGUI(true);
         }
      });
      refreshButton.setText("Refresh");
      topPanel.add(refreshButton);
      topLayout.putConstraint(SpringLayout.SOUTH, refreshButton, 113,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, refreshButton, 92,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, refreshButton, 95,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, refreshButton, 7,
            SpringLayout.WEST, topPanel);

      JLabel citePleaLabel = new JLabel("<html>Please <a href=\"http://micro-manager.org\">cite Micro-Manager</a> so funding will continue!</html>");
      topPanel.add(citePleaLabel);
      citePleaLabel.setFont(new Font("Arial", Font.PLAIN, 11));
      topLayout.putConstraint(SpringLayout.SOUTH, citePleaLabel, 139,
              SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, citePleaLabel, 119,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, citePleaLabel, 270,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, citePleaLabel, 7,
            SpringLayout.WEST, topPanel);

      class Pleader extends Thread{
         Pleader(){
            super("pleader");
         }
         @Override
         public void run(){
          try {
               ij.plugin.BrowserLauncher.openURL("https://micro-manager.org/wiki/Citing_Micro-Manager");
            } catch (IOException e1) {
               ReportingUtils.showError(e1);
            }
         }

      }
      citePleaLabel.addMouseListener(new MouseAdapter() {
         @Override
          public void mousePressed(MouseEvent e) {
             Pleader p = new Pleader();
             p.start();
          }
      });

      // add a listener to the main ImageJ window to catch it quitting out on us
      if (ij.IJ.getInstance() != null) {
         ij.IJ.getInstance().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
               closeSequence(true);
            };
         });
      }


      // add window listeners
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            closeSequence(false);
         }

         @Override
         public void windowOpened(WindowEvent e) {
            // -------------------
            // initialize hardware
            // -------------------
            try {
               core_ = new CMMCore();
            } catch(UnsatisfiedLinkError ex) {
               ReportingUtils.showError(ex, "Failed to open libMMCoreJ_wrap.jnilib");
               return;
            }
            ReportingUtils.setCore(core_);
            logStartupProperties();
                    
            cameraLabel_ = "";
            shutterLabel_ = "";
            zStageLabel_ = "";
            xyStageLabel_ = "";
            engine_ = new AcquisitionWrapperEngine(acqMgr_);

            // register callback for MMCore notifications, this is a global
            // to avoid garbage collection
            cb_ = new CoreEventCallback();
            core_.registerCallback(cb_);

            try {
               core_.setCircularBufferMemoryFootprint(options_.circularBufferSizeMB_);
            } catch (Exception e2) {
               ReportingUtils.showError(e2);
            }

            MMStudioMainFrame parent = (MMStudioMainFrame) e.getWindow();
            if (parent != null) {
               engine_.setParentGUI(parent);
            }

            loadMRUConfigFiles();
            afMgr_ = new AutofocusManager(gui_);
            Thread pluginLoader = initializePlugins();

            toFront();
            
            if (!options_.doNotAskForConfigFile_) {
               MMIntroDlg introDlg = new MMIntroDlg(MMVersion.VERSION_STRING, MRUConfigFiles_);
               introDlg.setConfigFile(sysConfigFile_);
               introDlg.setBackground(guiColors_.background.get((options_.displayBackground_)));
               introDlg.setVisible(true);
               sysConfigFile_ = introDlg.getConfigFile();
            }
            saveMRUConfigFiles();

            mainPrefs_.put(SYSTEM_CONFIG_FILE, sysConfigFile_);

            paint(MMStudioMainFrame.this.getGraphics());

            engine_.setCore(core_, afMgr_);
            posList_ = new PositionList();
            engine_.setPositionList(posList_);
            // load (but do no show) the scriptPanel
            createScriptPanel();

            // Create an instance of HotKeys so that they can be read in from prefs
            hotKeys_ = new org.micromanager.utils.HotKeys();
            hotKeys_.loadSettings();
            
            // before loading the system configuration, we need to wait 
            // until the plugins are loaded
            try {  
               pluginLoader.join(2000);
            } catch (InterruptedException ex) {
               ReportingUtils.logError(ex, "Plugin loader thread was interupted");
            }
            
            // if an error occurred during config loading, 
            // do not display more errors than needed
            if (!loadSystemConfiguration())
               ReportingUtils.showErrorOn(false);

            executeStartupScript();


            // Create Multi-D window here but do not show it.
            // This window needs to be created in order to properly set the "ChannelGroup"
            // based on the Multi-D parameters
            acqControlWin_ = new AcqControlDlg(engine_, mainPrefs_, MMStudioMainFrame.this, options_);
            addMMBackgroundListener(acqControlWin_);

            configPad_.setCore(core_);
            if (parent != null) {
               configPad_.setParentGUI(parent);
            }

            configPadButtonPanel_.setCore(core_);

            // initialize controls
            // initializeGUI();  Not needed since it is already called in loadSystemConfiguration
            initializeHelpMenu();
            
            String afDevice = mainPrefs_.get(AUTOFOCUS_DEVICE, "");
            if (afMgr_.hasDevice(afDevice)) {
               try {
                  afMgr_.selectDevice(afDevice);
               } catch (MMException e1) {
                  // this error should never happen
                  ReportingUtils.showError(e1);
               }
            }
                        
            centerAndDragListener_ = new CenterAndDragListener(gui_);
             
            // switch error reporting back on
            ReportingUtils.showErrorOn(true);
         }

         private Thread initializePlugins() {
            pluginMenu_ = new JMenu();
            pluginMenu_.setText("Plugins");
            menuBar_.add(pluginMenu_);
            Thread myThread = new ThreadPluginLoading("Plugin loading");
            myThread.start();
            return myThread;
         }

         class ThreadPluginLoading extends Thread {

            public ThreadPluginLoading(String string) {
               super(string);
            }

            @Override
            public void run() {
               // Needed for loading clojure-based jars:
               Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
               loadPlugins();
            }
         }

        
      });

      setRoiButton_ = new JButton();
      setRoiButton_.setName("setRoiButton");
      setRoiButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/shape_handles.png"));
      setRoiButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      setRoiButton_.setToolTipText("Set Region Of Interest to selected rectangle");
      setRoiButton_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            setROI();
         }
      });
      topPanel.add(setRoiButton_);
      topLayout.putConstraint(SpringLayout.EAST, setRoiButton_, 37,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, setRoiButton_, 7,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, setRoiButton_, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, setRoiButton_, 154,
            SpringLayout.NORTH, topPanel);

      clearRoiButton_ = new JButton();
      clearRoiButton_.setName("clearRoiButton");
      clearRoiButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/arrow_out.png"));
      clearRoiButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      clearRoiButton_.setToolTipText("Reset Region of Interest to full frame");
      clearRoiButton_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            clearROI();
         }
      });
      topPanel.add(clearRoiButton_);
      topLayout.putConstraint(SpringLayout.EAST, clearRoiButton_, 70,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, clearRoiButton_, 40,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.SOUTH, clearRoiButton_, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, clearRoiButton_, 154,
            SpringLayout.NORTH, topPanel);

      final JLabel regionOfInterestLabel = new JLabel();
      regionOfInterestLabel.setFont(new Font("Arial", Font.BOLD, 11));
      regionOfInterestLabel.setText("ROI");
      topPanel.add(regionOfInterestLabel);
      topLayout.putConstraint(SpringLayout.SOUTH, regionOfInterestLabel,
            154, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, regionOfInterestLabel,
            140, SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, regionOfInterestLabel,
            71, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, regionOfInterestLabel,
            8, SpringLayout.WEST, topPanel);
   

      final JLabel regionOfInterestLabel_1 = new JLabel();
      regionOfInterestLabel_1.setFont(new Font("Arial", Font.BOLD, 11));
      regionOfInterestLabel_1.setText("Zoom");
      topPanel.add(regionOfInterestLabel_1);
      topLayout.putConstraint(SpringLayout.SOUTH,
            regionOfInterestLabel_1, 154, SpringLayout.NORTH,
            topPanel);
      topLayout.putConstraint(SpringLayout.NORTH,
            regionOfInterestLabel_1, 140, SpringLayout.NORTH,
            topPanel);
      topLayout.putConstraint(SpringLayout.EAST, regionOfInterestLabel_1,
            139, SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, regionOfInterestLabel_1,
            81, SpringLayout.WEST, topPanel);

      final JButton zoomInButton = new JButton();
      zoomInButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            zoomIn();
         }
      });
      zoomInButton.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
            "/org/micromanager/icons/zoom_in.png"));
      zoomInButton.setName("zoomInButton");
      zoomInButton.setToolTipText("Zoom in");
      zoomInButton.setFont(new Font("Arial", Font.PLAIN, 10));
      topPanel.add(zoomInButton);
      topLayout.putConstraint(SpringLayout.SOUTH, zoomInButton, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, zoomInButton, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, zoomInButton, 110,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, zoomInButton, 80,
            SpringLayout.WEST, topPanel);

      final JButton zoomOutButton = new JButton();
      zoomOutButton.setName("zoomOutButton");
      zoomOutButton.addActionListener(new ActionListener() {

            @Override
         public void actionPerformed(final ActionEvent e) {
            zoomOut();
         }
      });
      zoomOutButton.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
            "/org/micromanager/icons/zoom_out.png"));
      zoomOutButton.setToolTipText("Zoom out");
      zoomOutButton.setFont(new Font("Arial", Font.PLAIN, 10));
      topPanel.add(zoomOutButton);
      topLayout.putConstraint(SpringLayout.SOUTH, zoomOutButton, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, zoomOutButton, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, zoomOutButton, 143,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, zoomOutButton, 113,
            SpringLayout.WEST, topPanel);

      // Profile
      // -------

      final JLabel profileLabel_ = new JLabel();
      profileLabel_.setFont(new Font("Arial", Font.BOLD, 11));
      profileLabel_.setText("Profile");
      topPanel.add(profileLabel_);
      topLayout.putConstraint(SpringLayout.SOUTH, profileLabel_, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, profileLabel_, 140,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, profileLabel_, 217,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, profileLabel_, 154,
            SpringLayout.WEST, topPanel);

      final JButton lineProfileButton = new JButton();
      lineProfileButton.setName("lineProfileButton");
      lineProfileButton.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/chart_curve.png"));
      lineProfileButton.setFont(new Font("Arial", Font.PLAIN, 10));
      lineProfileButton.setToolTipText("Open line profile window (requires line selection)");
      lineProfileButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            openLineProfileWindow();
         }
      });
      // buttonProf.setText("Profile");
      topPanel.add(lineProfileButton);
      topLayout.putConstraint(SpringLayout.SOUTH, lineProfileButton, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, lineProfileButton, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, lineProfileButton, 183,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, lineProfileButton, 153,
            SpringLayout.WEST, topPanel);

      // Autofocus
      // -------

      final JLabel autofocusLabel_ = new JLabel();
      autofocusLabel_.setFont(new Font("Arial", Font.BOLD, 11));
      autofocusLabel_.setText("Autofocus");
      topPanel.add(autofocusLabel_);
      topLayout.putConstraint(SpringLayout.SOUTH, autofocusLabel_, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, autofocusLabel_, 140,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, autofocusLabel_, 274,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, autofocusLabel_, 194,
            SpringLayout.WEST, topPanel);

      autofocusNowButton_ = new JButton();
      autofocusNowButton_.setName("autofocusNowButton");
      autofocusNowButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/find.png"));
      autofocusNowButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      autofocusNowButton_.setToolTipText("Autofocus now");
      autofocusNowButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (afMgr_.getDevice() != null) {
               new Thread() {
                  @Override
                  public void run() {
                     try {
                        boolean lmo = isLiveModeOn();
                        if (lmo) {
                           enableLiveMode(false);
                        }
                        afMgr_.getDevice().fullFocus();
                        if (lmo) {
                           enableLiveMode(true);
                        }
                     } catch (MMException ex) {
                        ReportingUtils.logError(ex);
                     }
                  }
               }.start(); // or any other method from Autofocus.java API
            }
         }
      });
      topPanel.add(autofocusNowButton_);
      topLayout.putConstraint(SpringLayout.SOUTH, autofocusNowButton_, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, autofocusNowButton_, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, autofocusNowButton_, 223,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, autofocusNowButton_, 193,
            SpringLayout.WEST, topPanel);

      autofocusConfigureButton_ = new JButton();
      autofocusConfigureButton_.setName("autofocusConfigureButton_");
      autofocusConfigureButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/wrench_orange.png"));
      autofocusConfigureButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      autofocusConfigureButton_.setToolTipText("Set autofocus options");
      autofocusConfigureButton_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            showAutofocusDialog();
         }
      });
      topPanel.add(autofocusConfigureButton_);
      topLayout.putConstraint(SpringLayout.SOUTH, autofocusConfigureButton_, 174,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, autofocusConfigureButton_, 154,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, autofocusConfigureButton_, 256,
            SpringLayout.WEST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, autofocusConfigureButton_, 226,
            SpringLayout.WEST, topPanel);
      
  

      saveConfigButton_ = new JButton();
      saveConfigButton_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0) {
            saveConfigPresets();
         }
      });
      saveConfigButton_.setToolTipText("Save current presets to the configuration file");
      saveConfigButton_.setText("Save");
      saveConfigButton_.setEnabled(false);
      topPanel.add(saveConfigButton_);
      topLayout.putConstraint(SpringLayout.SOUTH, saveConfigButton_, 20,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.NORTH, saveConfigButton_, 2,
            SpringLayout.NORTH, topPanel);
      topLayout.putConstraint(SpringLayout.EAST, saveConfigButton_, -5,
            SpringLayout.EAST, topPanel);
      topLayout.putConstraint(SpringLayout.WEST, saveConfigButton_, -80,
            SpringLayout.EAST, topPanel);

      // Add our own keyboard manager that handles Micro-Manager shortcuts
      MMKeyDispatcher mmKD = new MMKeyDispatcher(gui_);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(mmKD);

      dt_ = new DropTarget(this, new DragDropUtil());
      
      overrideImageJMenu();
   }
   
   private void overrideImageJMenu() {
//      final Menu colorMenu = ((Menu) Menus.getMenuBar().getMenu(2).getItem(5));
//      MenuItem stackToRGB = colorMenu.getItem(4);
//      final ActionListener ij = stackToRGB.getActionListeners()[0];
//      stackToRGB.removeActionListener(ij);
//      stackToRGB.addActionListener(new ActionListener() {
//         @Override
//         public void actionPerformed(ActionEvent e) {
//            ImagePlus img = WindowManager.getCurrentImage();
//            if (img != null && img instanceof VirtualAcquisitionDisplay.MMCompositeImage) {
//               int selection = JOptionPane.showConfirmDialog(img.getWindow(),
//                       "Because of the way Micro-Manager internally handles Color images, this command may\n"
//                       + "not work correctly.  An RGB Image/Stack can be created by splitting all channels\n"
//                       + "and then merging them.  Would you like Micro-Manager to perform this operation\n"
//                       + "automatically?\n\n"
//                       + "Yes--Split and merge channels to create RGB image/stack\n"
//                       + "No--Proceed with Stack to RGB command (may have unexpected effects on pixel data) ",
//                       "Convert Stack to RGB", JOptionPane.YES_NO_CANCEL_OPTION);
//               if (selection == 0) { //yes
//                  IJ.run("Split Channels");
//                  WindowManager.getCurrentImage().setTitle("BlueChannel");
//                  WindowManager.getImage(WindowManager.getNthImageID(2)).setTitle("GreenChannel");
//                  WindowManager.getImage(WindowManager.getNthImageID(1)).setTitle("RedChannel");
//                  
//                  IJ.run("Merge Channels...", "red=[RedChannel] green=[GreenChannel] blue=[BlueChannel] gray=*None*");
//               } else if (selection == 1) { //no
//                   ij.actionPerformed(e);
//               }
//            } else {
//               //If not an MM CompositImage, do the normal command
//               ij.actionPerformed(e);
//            }           
//         }});
   }

   private void handleException(Exception e, String msg) {
      String errText = "Exception occurred: ";
      if (msg.length() > 0) {
         errText += msg + " -- ";
      }
      if (options_.debugLogEnabled_) {
         errText += e.getMessage();
      } else {
         errText += e.toString() + "\n";
         ReportingUtils.showError(e);
      }
      handleError(errText);
   }

   private void handleException(Exception e) {
      handleException(e, "");
   }

   private void handleError(String message) {
      if (isLiveModeOn()) {
         // Should we always stop live mode on any error?
         enableLiveMode(false);
      }
      JOptionPane.showMessageDialog(this, message);
      core_.logMessage(message);
   }

   @Override
   public void makeActive() {
      toFront();
   }
   
   /**
    * used to store contrast settings to be later used for initialization of contrast of new windows.
    *  Shouldn't be called by loaded data sets, only
    * ones that have been acquired
    */
   public void saveChannelHistogramSettings(String channelGroup, String channel, boolean mda, 
           HistogramSettings settings) {
      String type = mda ? "MDA_" : "SnapLive_";
      if (options_.syncExposureMainAndMDA_) {
         type = "";  //only one group of contrast settings
      }
      contrastPrefs_.putInt("ContrastMin_" + channelGroup + "_" + type + channel, settings.min_);
      contrastPrefs_.putInt("ContrastMax_" + channelGroup + "_" + type + channel, settings.max_);
      contrastPrefs_.putDouble("ContrastGamma_" + channelGroup + "_" + type + channel, settings.gamma_);
      contrastPrefs_.putInt("ContrastHistMax_" + channelGroup + "_" + type + channel, settings.histMax_);
      contrastPrefs_.putInt("ContrastHistDisplayMode_" + channelGroup + "_" + type + channel, settings.displayMode_);
   }

   public HistogramSettings loadStoredChannelHisotgramSettings(String channelGroup, String channel, boolean mda) {
      String type = mda ? "MDA_" : "SnapLive_";
      if (options_.syncExposureMainAndMDA_) {
         type = "";  //only one group of contrast settings
      }
      return new HistogramSettings(
      contrastPrefs_.getInt("ContrastMin_" + channelGroup + "_" + type + channel,0),
      contrastPrefs_.getInt("ContrastMax_" + channelGroup + "_" + type + channel, 65536),
      contrastPrefs_.getDouble("ContrastGamma_" + channelGroup + "_" + type + channel, 1.0),
      contrastPrefs_.getInt("ContrastHistMax_" + channelGroup + "_" + type + channel, -1),
      contrastPrefs_.getInt("ContrastHistDisplayMode_" + channelGroup + "_" + type + channel, 1) );
   }

   private void setExposure() {
      try {
         if (!isLiveModeOn()) {
            core_.setExposure(NumberUtils.displayStringToDouble(
                    textFieldExp_.getText()));
         } else {
            liveModeTimer_.stop();
            core_.setExposure(NumberUtils.displayStringToDouble(
                    textFieldExp_.getText()));
            try {
               liveModeTimer_.begin();
            } catch (Exception e) {
               ReportingUtils.showError("Couldn't restart live mode");
               liveModeTimer_.stop();
            }
         }
        

         // Display the new exposure time
         double exposure = core_.getExposure();
         textFieldExp_.setText(NumberUtils.doubleToDisplayString(exposure));
         
         // update current channel in MDA window with this exposure
         String channelGroup = core_.getChannelGroup();
         String channel = core_.getCurrentConfigFromCache(channelGroup);
         if (!channel.equals("") ) {
            exposurePrefs_.putDouble("Exposure_" + channelGroup + "_"
                 + channel, exposure);
            if (options_.syncExposureMainAndMDA_) {
               getAcqDlg().setChannelExposureTime(channelGroup, channel, exposure);
            }
         }
         

      } catch (Exception exp) {
         // Do nothing.
      }
   }
   
   /**
    * Returns exposure time for the desired preset in the given channelgroup
    * Acquires its info from the preferences
    * Same thing is used in MDA window, but this class keeps its own copy
    * 
    * @param channelGroup
    * @param channel - 
    * @param defaultExp - default value
    * @return exposure time
    */
   public double getChannelExposureTime(String channelGroup, String channel,
           double defaultExp) {
      return exposurePrefs_.getDouble("Exposure_" + channelGroup
              + "_" + channel, defaultExp);
   }

   /**
    * Updates the exposure time in the given preset 
    * Will also update current exposure if it the given channel and channelgroup
    * are the current one
    * 
    * @param channelGroup - 
    * 
    * @param channel - preset for which to change exposure time
    * @param exposure - desired exposure time
    */
   public void setChannelExposureTime(String channelGroup, String channel,
           double exposure) {
      try {
         exposurePrefs_.putDouble("Exposure_" + channelGroup + "_"
                 + channel, exposure);
         if (channelGroup != null && channelGroup.equals(core_.getChannelGroup())) {
            if (channel != null && !channel.equals("") && 
                    channel.equals(core_.getCurrentConfigFromCache(channelGroup))) {
               textFieldExp_.setText(NumberUtils.doubleToDisplayString(exposure));
               setExposure();
            }
         }
      } catch (Exception ex) {
         ReportingUtils.logError("Failed to set Exposure prefs using Channelgroup: "
                 + channelGroup + ", channel: " + channel + ", exposure: " + exposure);
      }
   }

   @Override
   public boolean getAutoreloadOption() {
      return options_.autoreloadDevices_;
   }

   public double getPreferredWindowMag() {
      return options_.windowMag_;
   }

   public boolean getMetadataFileWithMultipageTiff() {
      return options_.mpTiffMetadataFile_;
   }

   public boolean getSeparateFilesForPositionsMPTiff() {
      return options_.mpTiffSeparateFilesForPositions_;
   }
   
   public boolean getHideMDADisplayOption() {
      return options_.hideMDADisplay_;
   }
   
   public boolean getFastStorageOption() {
      return options_.fastStorage_;
   }

   private void updateTitle() {
      this.setTitle(MICRO_MANAGER_TITLE + " " + MMVersion.VERSION_STRING + " - " + sysConfigFile_);
   }

   public void updateLineProfile() {
      if (WindowManager.getCurrentWindow() == null || profileWin_ == null
            || !profileWin_.isShowing()) {
         return;
      }

      calculateLineProfileData(WindowManager.getCurrentImage());
      profileWin_.setData(lineProfileData_);
   }

   private void openLineProfileWindow() {
      if (WindowManager.getCurrentWindow() == null || WindowManager.getCurrentWindow().isClosed()) {
         return;
      }
      calculateLineProfileData(WindowManager.getCurrentImage());
      if (lineProfileData_ == null) {
         return;
      }
      profileWin_ = new GraphFrame();
      profileWin_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      profileWin_.setData(lineProfileData_);
      profileWin_.setAutoScale();
      profileWin_.setTitle("Live line profile");
      profileWin_.setBackground(guiColors_.background.get((options_.displayBackground_)));
      addMMBackgroundListener(profileWin_);
      profileWin_.setVisible(true);
   }

   @Override
   public Rectangle getROI() throws MMScriptException {
      // ROI values are given as x,y,w,h in individual one-member arrays (pointers in C++):
      int[][] a = new int[4][1];
      try {
         core_.getROI(a[0], a[1], a[2], a[3]);
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
      // Return as a single array with x,y,w,h:
      return new Rectangle(a[0][0], a[1][0], a[2][0], a[3][0]);
   }

   private void calculateLineProfileData(ImagePlus imp) {
      // generate line profile
      Roi roi = imp.getRoi();
      if (roi == null || !roi.isLine()) {

         // if there is no line ROI, create one
         Rectangle r = imp.getProcessor().getRoi();
         int iWidth = r.width;
         int iHeight = r.height;
         int iXROI = r.x;
         int iYROI = r.y;
         if (roi == null) {
            iXROI += iWidth / 2;
            iYROI += iHeight / 2;
         }

         roi = new Line(iXROI - iWidth / 4, iYROI - iWidth / 4, iXROI
               + iWidth / 4, iYROI + iHeight / 4);
         imp.setRoi(roi);
         roi = imp.getRoi();
      }

      ImageProcessor ip = imp.getProcessor();
      ip.setInterpolate(true);
      Line line = (Line) roi;

      if (lineProfileData_ == null) {
         lineProfileData_ = new GraphData();
      }
      lineProfileData_.setData(line.getPixels());
   }

   @Override
   public void setROI(Rectangle r) throws MMScriptException {
      boolean liveRunning = false;
      if (isLiveModeOn()) {
         liveRunning = true;
         enableLiveMode(false);
      }
      try {
         core_.setROI(r.x, r.y, r.width, r.height);
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
      updateStaticInfo();
      if (liveRunning) {
         enableLiveMode(true);
      }

   }

   private void setROI() {
      ImagePlus curImage = WindowManager.getCurrentImage();
      if (curImage == null) {
         return;
      }

      Roi roi = curImage.getRoi();
      
      try {
         if (roi == null) {
            // if there is no ROI, create one
            Rectangle r = curImage.getProcessor().getRoi();
            int iWidth = r.width;
            int iHeight = r.height;
            int iXROI = r.x;
            int iYROI = r.y;
            if (roi == null) {
               iWidth /= 2;
               iHeight /= 2;
               iXROI += iWidth / 2;
               iYROI += iHeight / 2;
            }

            curImage.setRoi(iXROI, iYROI, iWidth, iHeight);
            roi = curImage.getRoi();
         }

         if (roi.getType() != Roi.RECTANGLE) {
            handleError("ROI must be a rectangle.\nUse the ImageJ rectangle tool to draw the ROI.");
            return;
         }

         Rectangle r = roi.getBoundingRect();
         // if we already had an ROI defined, correct for the offsets
         Rectangle cameraR =  getROI();
         r.x += cameraR.x;
         r.y += cameraR.y;
         // Stop (and restart) live mode if it is running
         setROI(r);

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

   private void clearROI() {
      try {
         boolean liveRunning = false;
         if (isLiveModeOn()) {
            liveRunning = true;
            enableLiveMode(false);
         }
         core_.clearROI();
         updateStaticInfo();
         if (liveRunning) {
            enableLiveMode(true);
         }

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

   /**
    * Returns instance of the core uManager object;
    */
   @Override
   public CMMCore getMMCore() {
      return core_;
   }

   /**
    * Returns singleton instance of MMStudioMainFrame
    */
   public static MMStudioMainFrame getInstance() {
      return gui_;
   }

   public MetadataPanel getMetadataPanel() {
      return metadataPanel_;
   }

   public final void setExitStrategy(boolean closeOnExit) {
      if (closeOnExit) {
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      }
      else {
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      }
   }

   @Override
   public void saveConfigPresets() {
      MicroscopeModel model = new MicroscopeModel();
      try {
         model.loadFromFile(sysConfigFile_);
         model.createSetupConfigsFromHardware(core_);
         model.createResolutionsFromHardware(core_);
         File f = FileDialogs.save(this, "Save the configuration file", MM_CONFIG_FILE);
         if (f != null) {
            model.saveToFile(f.getAbsolutePath());
            sysConfigFile_ = f.getAbsolutePath();
            mainPrefs_.put(SYSTEM_CONFIG_FILE, sysConfigFile_);
            configChanged_ = false;
            setConfigSaveButtonStatus(configChanged_);
            updateTitle();
         }
      } catch (MMConfigFileException e) {
         ReportingUtils.showError(e);
      }
   }

   protected void setConfigSaveButtonStatus(boolean changed) {
      saveConfigButton_.setEnabled(changed);
   }

   public String getAcqDirectory() {
      return openAcqDirectory_;
   }
   
   /**
    * Get currently used configuration file
    * @return - Path to currently used configuration file
    */
   public String getSysConfigFile() {
      return sysConfigFile_;
   }

   public void setAcqDirectory(String dir) {
      openAcqDirectory_ = dir;
   }

    /**
    * Open an existing acquisition directory and build viewer window.
    *
    */
   public void openAcquisitionData(boolean inRAM) {

      // choose the directory
      // --------------------
      File f = FileDialogs.openDir(this, "Please select an image data set", MM_DATA_SET);
      if (f != null) {
         if (f.isDirectory()) {
            openAcqDirectory_ = f.getAbsolutePath();
         } else {
            openAcqDirectory_ = f.getParent();
         }
         String acq = null;
         try {
            acq = openAcquisitionData(openAcqDirectory_, inRAM);
         } catch (MMScriptException ex) {
            ReportingUtils.showError(ex);
         } finally {
            try {
               acqMgr_.closeAcquisition(acq);
            } catch (MMScriptException ex) {
               ReportingUtils.logError(ex);
            }
         }
         
      }
   }

   public String openAcquisitionData(String dir, boolean inRAM, boolean show) 
           throws MMScriptException {
      String rootDir = new File(dir).getAbsolutePath();
      String name = new File(dir).getName();
      rootDir = rootDir.substring(0, rootDir.length() - (name.length() + 1));
      acqMgr_.openAcquisition(name, rootDir, show, !inRAM, true);
      try {
         getAcquisition(name).initialize();
      } catch (MMScriptException mex) {
         acqMgr_.closeAcquisition(name);
         throw (mex);
      }
     
      return name;
   }

   /**
    * Opens an existing data set. Shows the acquisition in a window.
    * @return The acquisition object.
    */
   public String openAcquisitionData(String dir, boolean inRam) throws MMScriptException {
      return openAcquisitionData(dir, inRam, true);
   }

   protected void zoomOut() {
      ImageWindow curWin = WindowManager.getCurrentWindow();
      if (curWin != null) {
         ImageCanvas canvas = curWin.getCanvas();
         Rectangle r = canvas.getBounds();
         canvas.zoomOut(r.width / 2, r.height / 2);

         VirtualAcquisitionDisplay vad = VirtualAcquisitionDisplay.getDisplay(curWin.getImagePlus());
         if (vad != null) {
            vad.storeWindowSizeAfterZoom(curWin);
            vad.updateWindowTitleAndStatus();
         }
      }
   }

   protected void zoomIn() {
      ImageWindow curWin = WindowManager.getCurrentWindow();
      if (curWin != null) {
         ImageCanvas canvas = curWin.getCanvas();
         Rectangle r = canvas.getBounds();
         canvas.zoomIn(r.width / 2, r.height / 2);
         
         VirtualAcquisitionDisplay vad = VirtualAcquisitionDisplay.getDisplay(curWin.getImagePlus());
         if (vad != null) {
            vad.storeWindowSizeAfterZoom(curWin);
            vad.updateWindowTitleAndStatus();
         }        
      }
   }

   protected void changeBinning() {
      try {
         boolean liveRunning = false;
         if (isLiveModeOn() ) {
            liveRunning = true;
            enableLiveMode(false);
        } 
         
         if (isCameraAvailable()) {
            Object item = comboBinning_.getSelectedItem();
            if (item != null) {
               core_.setProperty(cameraLabel_, MMCoreJ.getG_Keyword_Binning(), item.toString());
            }
         }
         updateStaticInfo();

         if (liveRunning) {
            enableLiveMode(true);
         }

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }

      
   }

   private void createPropertyEditor() {
      if (propertyBrowser_ != null) {
         propertyBrowser_.dispose();
      }

      propertyBrowser_ = new PropertyEditor();
      propertyBrowser_.setGui(this);
      propertyBrowser_.setVisible(true);
      propertyBrowser_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      propertyBrowser_.setCore(core_);
   }

   private void createCalibrationListDlg() {
      if (calibrationListDlg_ != null) {
         calibrationListDlg_.dispose();
      }

      calibrationListDlg_ = new CalibrationListDlg(core_);
      calibrationListDlg_.setVisible(true);
      calibrationListDlg_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      calibrationListDlg_.setParentGUI(this);
   }

   public CalibrationListDlg getCalibrationListDlg() {
      if (calibrationListDlg_ == null) {
         createCalibrationListDlg();
      }
      return calibrationListDlg_;
   }

   private void createScriptPanel() {
      if (scriptPanel_ == null) {
         scriptPanel_ = new ScriptPanel(core_, options_, this);
         scriptPanel_.insertScriptingObject(SCRIPT_CORE_OBJECT, core_);
         scriptPanel_.insertScriptingObject(SCRIPT_ACQENG_OBJECT, engine_);
         scriptPanel_.setParentGUI(this);
         scriptPanel_.setBackground(guiColors_.background.get((options_.displayBackground_)));
         addMMBackgroundListener(scriptPanel_);

      }
   }

   /**
    * Updates Status line in main window from cached values
    */
   private void updateStaticInfoFromCache() {
      String dimText = "Image info (from camera): " + staticInfo_.width_ + " X " + staticInfo_.height_ + " X "
            + staticInfo_.bytesPerPixel_ + ", Intensity range: " + staticInfo_.imageBitDepth_ + " bits";
      dimText += ", " + TextUtils.FMT0.format(staticInfo_.pixSizeUm_ * 1000) + "nm/pix";
      if (zStageLabel_.length() > 0) {
         dimText += ", Z=" + TextUtils.FMT2.format(staticInfo_.zPos_) + "um";
      }
      if (xyStageLabel_.length() > 0) {
         dimText += ", XY=(" + TextUtils.FMT2.format(staticInfo_.x_) + "," + TextUtils.FMT2.format(staticInfo_.y_) + ")um";
      }

      labelImageDimensions_.setText(dimText);
   }

   public void updateXYPos(double x, double y) {
      staticInfo_.x_ = x;
      staticInfo_.y_ = y;

      updateStaticInfoFromCache();
   }

   public void updateZPos(double z) {
      staticInfo_.zPos_ = z;

      updateStaticInfoFromCache();
   }

   public void updateXYPosRelative(double x, double y) {
      staticInfo_.x_ += x;
      staticInfo_.y_ += y;

      updateStaticInfoFromCache();
   }

   public void updateZPosRelative(double z) {
      staticInfo_.zPos_ += z;

      updateStaticInfoFromCache();
   }

   public void updateXYStagePosition(){

      double x[] = new double[1];
      double y[] = new double[1];
      try {
         if (xyStageLabel_.length() > 0) 
            core_.getXYPosition(xyStageLabel_, x, y);
      } catch (Exception e) {
          ReportingUtils.showError(e);
      }

      staticInfo_.x_ = x[0];
      staticInfo_.y_ = y[0];
      updateStaticInfoFromCache();
   }

   private void updatePixSizeUm (double pixSizeUm) {
      staticInfo_.pixSizeUm_ = pixSizeUm;

      updateStaticInfoFromCache();
   }

   private void updateStaticInfo() {
      double zPos = 0.0;
      double x[] = new double[1];
      double y[] = new double[1];

      try {
         if (zStageLabel_.length() > 0) {
            zPos = core_.getPosition(zStageLabel_);
         }
         if (xyStageLabel_.length() > 0) {
            core_.getXYPosition(xyStageLabel_, x, y);
         }
      } catch (Exception e) {
         handleException(e);
      }

      staticInfo_.width_ = core_.getImageWidth();
      staticInfo_.height_ = core_.getImageHeight();
      staticInfo_.bytesPerPixel_ = core_.getBytesPerPixel();
      staticInfo_.imageBitDepth_ = core_.getImageBitDepth();
      staticInfo_.pixSizeUm_ = core_.getPixelSizeUm();
      staticInfo_.zPos_ = zPos;
      staticInfo_.x_ = x[0];
      staticInfo_.y_ = y[0];

      updateStaticInfoFromCache();
   }

   public void toggleShutter() {
      try {
         if (!toggleButtonShutter_.isEnabled())
            return;
         toggleButtonShutter_.requestFocusInWindow();
         if (toggleButtonShutter_.getText().equals("Open")) {
            setShutterButton(true);
            core_.setShutterOpen(true);
         } else {
            core_.setShutterOpen(false);
            setShutterButton(false);
         }
      } catch (Exception e1) {
         ReportingUtils.showError(e1);
      }
   }

   private void updateCenterAndDragListener() {
      if (centerAndDragMenuItem_.isSelected()) {
         centerAndDragListener_.start();
      } else {
         centerAndDragListener_.stop();
      }
   }
   
   private void setShutterButton(boolean state) {
      if (state) {
         toggleButtonShutter_.setText("Close");
      } else {
         toggleButtonShutter_.setText("Open");
      }
   }

   // //////////////////////////////////////////////////////////////////////////
   // public interface available for scripting access
   // //////////////////////////////////////////////////////////////////////////
   public void snapSingleImage() {
      doSnap();
   }

   public Object getPixels() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null) {
         return ip.getProcessor().getPixels();
      }

      return null;
   }

   public void setPixels(Object obj) {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip == null) {
         return;
      }
      ip.getProcessor().setPixels(obj);
   }

   public int getImageHeight() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null)
         return ip.getHeight();
      return 0;
   }

   public int getImageWidth() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null)
         return ip.getWidth();
      return 0;
   }

   public int getImageDepth() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null)
         return ip.getBitDepth();
      return 0;
   }

   public ImageProcessor getImageProcessor() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip == null)
         return null;
      return ip.getProcessor();
   }

   private boolean isCameraAvailable() {
      return cameraLabel_.length() > 0;
   }

   /**
    * Part of ScriptInterface API
    * Opens the XYPositionList when it is not opened
    * Adds the current position to the list (same as pressing the "Mark" button)
    */
   public void markCurrentPosition() {
      if (posListDlg_ == null) {
         showXYPositionList();
      }
      if (posListDlg_ != null) {
         posListDlg_.markPosition();
      }
   }

   /**
    * Implements ScriptInterface
    */
   public AcqControlDlg getAcqDlg() {
      return acqControlWin_;
   }

   /**
    * Implements ScriptInterface
    */
   public PositionListDlg getXYPosListDlg() {
      if (posListDlg_ == null)
         posListDlg_ = new PositionListDlg(core_, this, posList_, options_);
      return posListDlg_;
   }

   /**
    * Implements ScriptInterface
    */
   public boolean isAcquisitionRunning() {
      if (engine_ == null)
         return false;
      return engine_.isAcquisitionRunning();
   }

   /**
    * Implements ScriptInterface
    */
   public boolean versionLessThan(String version) throws MMScriptException {
      try {
         String[] v = MMVersion.VERSION_STRING.split(" ", 2);
         String[] m = v[0].split("\\.", 3);
         String[] v2 = version.split(" ", 2);
         String[] m2 = v2[0].split("\\.", 3);
         for (int i=0; i < 3; i++) {
            if (Integer.parseInt(m[i]) < Integer.parseInt(m2[i])) {
               ReportingUtils.showError("This code needs Micro-Manager version " + version + " or greater");
               return true;
            }
            if (Integer.parseInt(m[i]) > Integer.parseInt(m2[i])) {
               return false;
            }
         }
         if (v2.length < 2 || v2[1].equals("") )
            return false;
         if (v.length < 2 ) {
            ReportingUtils.showError("This code needs Micro-Manager version " + version + " or greater");
            return true;
         }
         if (Integer.parseInt(v[1]) < Integer.parseInt(v2[1])) {
            ReportingUtils.showError("This code needs Micro-Manager version " + version + " or greater");
            return false;
         }
         return true;

      } catch (Exception ex) {
         throw new MMScriptException ("Format of version String should be \"a.b.c\"");
      }
   } 

    public boolean isLiveModeOn() {
        return liveModeTimer_ != null && liveModeTimer_.isRunning();
   }
    
   public LiveModeTimer getLiveModeTimer() {
      if (liveModeTimer_ == null) {
         liveModeTimer_ = new LiveModeTimer();
      }
      return liveModeTimer_;
   }
   
   public void enableLiveMode(boolean enable) {
      if (core_ == null) {
         return;
      }
      if (enable == isLiveModeOn()) {
         return;
      }
      if (enable) {
         try {
            if (core_.getCameraDevice().length() == 0) {
               ReportingUtils.showError("No camera configured");
               updateButtonsForLiveMode(false);
               return;
            }
            if (liveModeTimer_ == null) {
               liveModeTimer_ = new LiveModeTimer();
            }
            liveModeTimer_.begin();
            enableLiveModeListeners(enable);
         } catch (Exception e) {
            ReportingUtils.showError(e);
            liveModeTimer_.stop();
            enableLiveModeListeners(false);
            updateButtonsForLiveMode(false);
            return;
         }
      } else {
         liveModeTimer_.stop();
         enableLiveModeListeners(enable);
      }
      updateButtonsForLiveMode(enable);
   }

   public void updateButtonsForLiveMode(boolean enable) {
      autoShutterCheckBox_.setEnabled(!enable);
      if (core_.getAutoShutter()) {
         toggleButtonShutter_.setText(enable ? "Close" : "Open" );
      }
      buttonSnap_.setEnabled(!enable);
      //toAlbumButton_.setEnabled(!enable);
      toggleButtonLive_.setIcon(enable ? SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/cancel.png")
              : SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/camera_go.png"));
      toggleButtonLive_.setSelected(false);
      toggleButtonLive_.setText(enable ? "Stop Live" : "Live");
      
   }

   private void enableLiveModeListeners(boolean enable) {
      if (enable) {
         // attach mouse wheel listener to control focus:
         if (zWheelListener_ == null) 
            zWheelListener_ = new ZWheelListener(core_, this);         
         zWheelListener_.start(getImageWin());
         // attach key listener to control the stage and focus:
         if (xyzKeyListener_ == null) 
            xyzKeyListener_ = new XYZKeyListener(core_, this);
         xyzKeyListener_.start(getImageWin());
      } else {
         if (zWheelListener_ != null) 
            zWheelListener_.stop();
         if (xyzKeyListener_ != null) 
            xyzKeyListener_.stop();
      }
   }

   public boolean getLiveMode() {
      return isLiveModeOn();
   }

   public boolean updateImage() {
      try {
         if (isLiveModeOn()) {
               enableLiveMode(false);
               return true; // nothing to do, just show the last image
         }

         if (WindowManager.getCurrentWindow() == null) {
            return false;
         }

         ImagePlus ip = WindowManager.getCurrentImage();
         
         core_.snapImage();
         Object img = core_.getImage();

         ip.getProcessor().setPixels(img);
         ip.updateAndRepaintWindow();

         if (!isCurrentImageFormatSupported()) {
            return false;
         }
       
         updateLineProfile();
      } catch (Exception e) {
         ReportingUtils.showError(e);
         return false;
      }

      return true;
   }

   public boolean displayImage(final Object pixels) {
      if (pixels instanceof TaggedImage) {
         return displayTaggedImage((TaggedImage) pixels, true);
      } else {
         return displayImage(pixels, true);
      }
   }


   public boolean displayImage(final Object pixels, boolean wait) {
      checkSimpleAcquisition();
      try {   
            int width = getAcquisition(SIMPLE_ACQ).getWidth();
            int height = getAcquisition(SIMPLE_ACQ).getHeight();
            int byteDepth = getAcquisition(SIMPLE_ACQ).getByteDepth();          
            TaggedImage ti = ImageUtils.makeTaggedImage(pixels, 0, 0, 0,0, width, height, byteDepth);
            simpleDisplay_.getImageCache().putImage(ti);
            simpleDisplay_.showImage(ti, wait);
            return true;
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
         return false;
      }
   }

   public boolean displayImageWithStatusLine(Object pixels, String statusLine) {
      boolean ret = displayImage(pixels);
      simpleDisplay_.displayStatusLine(statusLine);
      return ret;
   }

   public void displayStatusLine(String statusLine) {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (!(ip.getWindow() instanceof VirtualAcquisitionDisplay.DisplayWindow)) {
         return;
      }
      VirtualAcquisitionDisplay.getDisplay(ip).displayStatusLine(statusLine);
   }

   private boolean isCurrentImageFormatSupported() {
      boolean ret = false;
      long channels = core_.getNumberOfComponents();
      long bpp = core_.getBytesPerPixel();

      if (channels > 1 && channels != 4 && bpp != 1) {
         handleError("Unsupported image format.");
      } else {
         ret = true;
      }
      return ret;
   }

   public void doSnap() {
      doSnap(false);
   }

   public void doSnap(final boolean album) {
      if (core_.getCameraDevice().length() == 0) {
         ReportingUtils.showError("No camera configured");
         return;
      }

      BlockingQueue snapImageQueue = new LinkedBlockingQueue();
      
      try {
         core_.snapImage();
         long c = core_.getNumberOfCameraChannels();
         runDisplayThread(snapImageQueue, new DisplayImageRoutine() {
            public void show(final TaggedImage image) {
                  if (album) {
                     try {
                        addToAlbum(image);
                     } catch (MMScriptException ex) {
                        ReportingUtils.showError(ex);
                     }
                  } else {
                     displayImage(image);
                  }
            }

         });
         
         for (int i = 0; i < c; ++i) {
            TaggedImage img = core_.getTaggedImage(i);
            img.tags.put("Channels", c);
            snapImageQueue.put(img);
         }
         
         snapImageQueue.put(TaggedImageQueue.POISON);

         if (simpleDisplay_ != null) {
            ImagePlus imgp = simpleDisplay_.getImagePlus();
            if (imgp != null) {
               ImageWindow win = imgp.getWindow();
               if (win != null) {
                  win.toFront();
               }
            }
         }
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
      }
   }

   /**
    * Is this function still needed?  It does some magic with tags. I found 
    * it to do harmful thing with tags when a Multi-Camera device is
    * present (that issue is now fixed).
    */
   public void normalizeTags(TaggedImage ti) {
      if (ti != TaggedImageQueue.POISON) {
      int channel = 0;
      try {

         if (ti.tags.has("ChannelIndex")) {
            channel = MDUtils.getChannelIndex(ti.tags);
         }
         MDUtils.setChannelIndex(ti.tags, channel);
         MDUtils.setPositionIndex(ti.tags, 0);
         MDUtils.setSliceIndex(ti.tags, 0);
         MDUtils.setFrameIndex(ti.tags, 0);
         
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
      }
      }
   }


   public boolean displayImage(TaggedImage ti) {
      normalizeTags(ti);
      return displayTaggedImage(ti, true);
   }

   private boolean displayTaggedImage(TaggedImage ti, boolean update) {
      try {
         checkSimpleAcquisition(ti);
         setCursor(new Cursor(Cursor.WAIT_CURSOR));
         ti.tags.put("Summary", getAcquisition(SIMPLE_ACQ).getSummaryMetadata());
         addStagePositionToTags(ti);
         addImage(SIMPLE_ACQ, ti, update, true);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
         return false;
      }
      if (update) {
         setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         updateLineProfile();
      }
      return true;
   }
   
   public void addStagePositionToTags(TaggedImage ti) throws JSONException {
      if (gui_.xyStageLabel_.length() > 0) {
         ti.tags.put("XPositionUm", gui_.staticInfo_.x_);
         ti.tags.put("YPositionUm", gui_.staticInfo_.y_);
      }
      if (gui_.zStageLabel_.length() > 0) {
         ti.tags.put("ZPositionUm", gui_.staticInfo_.zPos_);
      }
   }

    private void configureBinningCombo() throws Exception {
        if (cameraLabel_.length() > 0) {
            ActionListener[] listeners;

            // binning combo
            if (comboBinning_.getItemCount() > 0) {
                comboBinning_.removeAllItems();
            }
            StrVector binSizes = core_.getAllowedPropertyValues(
                    cameraLabel_, MMCoreJ.getG_Keyword_Binning());
            listeners = comboBinning_.getActionListeners();
            for (int i = 0; i < listeners.length; i++) {
                comboBinning_.removeActionListener(listeners[i]);
            }
            for (int i = 0; i < binSizes.size(); i++) {
                comboBinning_.addItem(binSizes.get(i));
            }

            comboBinning_.setMaximumRowCount((int) binSizes.size());
            if (binSizes.isEmpty()) {
                comboBinning_.setEditable(true);
            } else {
                comboBinning_.setEditable(false);
            }

            for (int i = 0; i < listeners.length; i++) {
                comboBinning_.addActionListener(listeners[i]);
            }
        }
    }

   public void initializeGUI() {
      try {

         // establish device roles
         cameraLabel_ = core_.getCameraDevice();
         shutterLabel_ = core_.getShutterDevice();
         zStageLabel_ = core_.getFocusDevice();
         xyStageLabel_ = core_.getXYStageDevice();
         engine_.setZStageDevice(zStageLabel_);  
  
         configureBinningCombo();

         // active shutter combo
         try {
            shutters_ = core_.getLoadedDevicesOfType(DeviceType.ShutterDevice);
         } catch (Exception e) {
            ReportingUtils.logError(e);
         }

         if (shutters_ != null) {
            String items[] = new String[(int) shutters_.size()];
            for (int i = 0; i < shutters_.size(); i++) {
               items[i] = shutters_.get(i);
            }

            GUIUtils.replaceComboContents(shutterComboBox_, items);
            String activeShutter = core_.getShutterDevice();
            if (activeShutter != null) {
               shutterComboBox_.setSelectedItem(activeShutter);
            } else {
               shutterComboBox_.setSelectedItem("");
            }
         }

         // Autofocus
         autofocusConfigureButton_.setEnabled(afMgr_.getDevice() != null);
         autofocusNowButton_.setEnabled(afMgr_.getDevice() != null);

         // Rebuild stage list in XY PositinList
         if (posListDlg_ != null) {
            posListDlg_.rebuildAxisList();
         }

         updateGUI(true);
      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

   public String getVersion() {
      return MMVersion.VERSION_STRING;
   }

   private void addPluginToMenu(final PluginItem plugin, Class<?> cl) {
      // add plugin menu items

      final JMenuItem newMenuItem = new JMenuItem();
      newMenuItem.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            ReportingUtils.logMessage("Plugin command: "
                  + e.getActionCommand());
                  plugin.instantiate();
                  plugin.plugin.show();
         }
      });
      newMenuItem.setText(plugin.menuItem);
      
      
      String toolTipDescription = "";
      try {
          // Get this static field from the class implementing MMPlugin.
    	  toolTipDescription = (String) cl.getDeclaredField("tooltipDescription").get(null);
       } catch (SecurityException e) {
          ReportingUtils.logError(e);
          toolTipDescription = "Description not available";
       } catch (NoSuchFieldException e) {
          toolTipDescription = "Description not available";
          ReportingUtils.logMessage(cl.getName() + " fails to implement static String tooltipDescription.");
       } catch (IllegalArgumentException e) {
          ReportingUtils.logError(e);
       } catch (IllegalAccessException e) {
          ReportingUtils.logError(e);
       }
      
      String mrjProp = System.getProperty("mrj.version");
      if (mrjProp != null) // running on a mac
          newMenuItem.setToolTipText(toolTipDescription);
      else      
          newMenuItem.setToolTipText( TooltipTextMaker.addHTMLBreaksForTooltip(toolTipDescription) );
      
    	  
      pluginMenu_.add(newMenuItem);
      pluginMenu_.validate();
      menuBar_.validate();
   }
   
   public void updateGUI(boolean updateConfigPadStructure) {
      updateGUI(updateConfigPadStructure, false);
   }

   public void updateGUI(boolean updateConfigPadStructure, boolean fromCache) {

      try {
         // establish device roles
         cameraLabel_ = core_.getCameraDevice();
         shutterLabel_ = core_.getShutterDevice();
         zStageLabel_ = core_.getFocusDevice();
         xyStageLabel_ = core_.getXYStageDevice();

         afMgr_.refresh();

         // camera settings
         if (isCameraAvailable()) {
            double exp = core_.getExposure();
            textFieldExp_.setText(NumberUtils.doubleToDisplayString(exp));
            configureBinningCombo();
            String binSize;
            if (fromCache) {
               binSize = core_.getPropertyFromCache(cameraLabel_, MMCoreJ.getG_Keyword_Binning());
            } else {
               binSize = core_.getProperty(cameraLabel_, MMCoreJ.getG_Keyword_Binning());
            }
            GUIUtils.setComboSelection(comboBinning_, binSize);
         }

         if (liveModeTimer_ == null || !liveModeTimer_.isRunning()) {
            autoShutterCheckBox_.setSelected(core_.getAutoShutter());
            boolean shutterOpen = core_.getShutterOpen();
            setShutterButton(shutterOpen);
            if (autoShutterCheckBox_.isSelected()) {
               toggleButtonShutter_.setEnabled(false);
            } else {
               toggleButtonShutter_.setEnabled(true);
            }
         }

         // active shutter combo
         if (shutters_ != null) {
            String activeShutter = core_.getShutterDevice();
            if (activeShutter != null) {
               shutterComboBox_.setSelectedItem(activeShutter);
            } else {
               shutterComboBox_.setSelectedItem("");
            }
         }

         // state devices
         if (updateConfigPadStructure && (configPad_ != null)) {
            configPad_.refreshStructure(fromCache);
            // Needed to update read-only properties.  May slow things down...
            if (!fromCache)
               core_.updateSystemStateCache();
         }

         // update Channel menus in Multi-dimensional acquisition dialog
         updateChannelCombos();

         // update list of pixel sizes in pixel size configuration window
         if (calibrationListDlg_ != null) {
            calibrationListDlg_.refreshCalibrations();
         }
         if (propertyBrowser_ != null) {
            propertyBrowser_.refresh();
         }

      } catch (Exception e) {
         ReportingUtils.logError(e);
      }

      updateStaticInfo();
      updateTitle();

   }

   //TODO: deprecated @Override
   public boolean okToAcquire() {
      return !isLiveModeOn();
   }

   //TODO: deprecated @Override
   public void stopAllActivity() {
        if (this.acquisitionEngine2010 != null) {
            this.acquisitionEngine2010.stop();
        }
      enableLiveMode(false);
   }

   /**
    * Cleans up resources while shutting down 
    * 
    * @param calledByImageJ
    * @return flag indicating success.  Shut down should abort when flag is false 
    */
   private boolean cleanupOnClose(boolean calledByImageJ) {
      // Save config presets if they were changed.
      if (configChanged_) {
         Object[] options = {"Yes", "No"};
         int n = JOptionPane.showOptionDialog(null,
               "Save Changed Configuration?", "Micro-Manager",
               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
               null, options, options[0]);
         if (n == JOptionPane.YES_OPTION) {
            saveConfigPresets();
            // if the configChanged_ flag did not become false, the user 
            // must have cancelled the configuration saving and we should cancel
            // quitting as well
            if (configChanged_) {
               return false;
            }
         }
      }
      if (liveModeTimer_ != null)
         liveModeTimer_.stop();
      
       // check needed to avoid deadlock
       if (!calledByImageJ) {
           if (!WindowManager.closeAllWindows()) {
               core_.logMessage("Failed to close some windows");
           }
       }

      if (profileWin_ != null) {
         removeMMBackgroundListener(profileWin_);
         profileWin_.dispose();
      }

      if (scriptPanel_ != null) {
         removeMMBackgroundListener(scriptPanel_);
         scriptPanel_.closePanel();
      }

      if (propertyBrowser_ != null) {
         removeMMBackgroundListener(propertyBrowser_);
         propertyBrowser_.dispose();
      }

      if (acqControlWin_ != null) {
         removeMMBackgroundListener(acqControlWin_);
         acqControlWin_.close();
      }

      if (engine_ != null) {
         engine_.shutdown();
      }

      if (afMgr_ != null) {
         afMgr_.closeOptionsDialog();
      }

      // dispose plugins
      for (int i = 0; i < plugins_.size(); i++) {
         MMPlugin plugin = (MMPlugin) plugins_.get(i).plugin;
         if (plugin != null) {
            plugin.dispose();
         }
      }

      synchronized (shutdownLock_) {
         try {
            if (core_ != null) {
               ReportingUtils.setCore(null);
               core_.delete();
               core_ = null;
            }
         } catch (Exception err) {
            ReportingUtils.showError(err);
         }
      }
      return true;
   }

   private void saveSettings() {
      Rectangle r = this.getBounds();

      mainPrefs_.putInt(MAIN_FRAME_X, r.x);
      mainPrefs_.putInt(MAIN_FRAME_Y, r.y);
      mainPrefs_.putInt(MAIN_FRAME_WIDTH, r.width);
      mainPrefs_.putInt(MAIN_FRAME_HEIGHT, r.height);
      mainPrefs_.putInt(MAIN_FRAME_DIVIDER_POS, this.splitPane_.getDividerLocation());
      
      mainPrefs_.put(OPEN_ACQ_DIR, openAcqDirectory_);
      mainPrefs_.put(MAIN_SAVE_METHOD, 
              ImageUtils.getImageStorageClass().getName());

      // save field values from the main window
      // NOTE: automatically restoring these values on startup may cause
      // problems
      mainPrefs_.put(MAIN_EXPOSURE, textFieldExp_.getText());

      // NOTE: do not save auto shutter state

      if (afMgr_ != null && afMgr_.getDevice() != null) {
         mainPrefs_.put(AUTOFOCUS_DEVICE, afMgr_.getDevice().getDeviceName());
      }
   }

   private void loadConfiguration() {
      File f = FileDialogs.openFile(this, "Load a config file",MM_CONFIG_FILE);
      if (f != null) {
         sysConfigFile_ = f.getAbsolutePath();
         configChanged_ = false;
         setConfigSaveButtonStatus(configChanged_);
         mainPrefs_.put(SYSTEM_CONFIG_FILE, sysConfigFile_);
         loadSystemConfiguration();
      }
   }


   public synchronized boolean closeSequence(boolean calledByImageJ) {

      if (!this.isRunning()) {
         if (core_ != null) {
            core_.logMessage("MMStudioMainFrame::closeSequence called while running_ is false");
         }
         this.dispose();
         return true;
      }
      
      if (engine_ != null && engine_.isAcquisitionRunning()) {
         int result = JOptionPane.showConfirmDialog(
               this,
               "Acquisition in progress. Are you sure you want to exit and discard all data?",
               "Micro-Manager", JOptionPane.YES_NO_OPTION,
               JOptionPane.INFORMATION_MESSAGE);

         if (result == JOptionPane.NO_OPTION) {
            return false;
         }
      }

      stopAllActivity();
      
      try {
         // Close all image windows associated with MM.  Canceling saving of 
         // any of these should abort shutdown
         if (!acqMgr_.closeAllImageWindows()) {
            return false;
         }
      } catch (MMScriptException ex) {
         // Not sure what to do here...
      }

      if (!cleanupOnClose(calledByImageJ)) {
         return false;
      }

      running_ = false;

      saveSettings();
      try {
         configPad_.saveSettings();
         options_.saveSettings();
         hotKeys_.saveSettings();
      } catch (NullPointerException e) {
         if (core_ != null)
            this.logError(e);
      }     
      // disposing sometimes hangs ImageJ!
      // this.dispose();
      if (options_.closeOnExit_) {
         if (!runsAsPlugin_) {
            System.exit(0);
         } else {
            ImageJ ij = IJ.getInstance();
            if (ij != null) {
               ij.quit();
            }
         }
      } else {
         this.dispose();
      }
      
      return true;
   }

   public void applyContrastSettings(ContrastSettings contrast8,
         ContrastSettings contrast16) {
      ImagePlus img = WindowManager.getCurrentImage();
      if (img == null|| VirtualAcquisitionDisplay.getDisplay(img) == null )
         return;
      if (img.getBytesPerPixel() == 1)     
         VirtualAcquisitionDisplay.getDisplay(img).setChannelContrast(0,
                 contrast8.min, contrast8.max, contrast8.gamma);
      else
         VirtualAcquisitionDisplay.getDisplay(img).setChannelContrast(0, 
                 contrast16.min, contrast16.max, contrast16.gamma);
   }

   //TODO: deprecated @Override
   public ContrastSettings getContrastSettings() {
      ImagePlus img = WindowManager.getCurrentImage();
      if (img == null || VirtualAcquisitionDisplay.getDisplay(img) == null )
         return null;
      return VirtualAcquisitionDisplay.getDisplay(img).getChannelContrastSettings(0);
   }

   public boolean is16bit() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null && ip.getProcessor() instanceof ShortProcessor) {
         return true;
      }
      return false;
   }

   public boolean isRunning() {
      return running_;
   }

   /**
    * Executes the beanShell script. This script instance only supports
    * commands directed to the core object.
    */
   private void executeStartupScript() {
      // execute startup script
      File f = new File(startupScriptFile_);

      if (startupScriptFile_.length() > 0 && f.exists()) {
         WaitDialog waitDlg = new WaitDialog(
               "Executing startup script, please wait...");
         waitDlg.showDialog();
         Interpreter interp = new Interpreter();
         try {
            // insert core object only
            interp.set(SCRIPT_CORE_OBJECT, core_);
            interp.set(SCRIPT_ACQENG_OBJECT, engine_);
            interp.set(SCRIPT_GUI_OBJECT, this);

            // read text file and evaluate
            interp.eval(TextUtils.readTextFile(startupScriptFile_));
         } catch (IOException exc) {
            ReportingUtils.logError(exc, "Unable to read the startup script (" + startupScriptFile_ + ").");
         } catch (EvalError exc) {
            ReportingUtils.logError(exc);
         } finally {
            waitDlg.closeDialog();
         }
      } else {
         if (startupScriptFile_.length() > 0)
            ReportingUtils.logMessage("Startup script file ("+startupScriptFile_+") not present.");
      }
   }

   /**
    * Loads system configuration from the cfg file.
    */
   private boolean loadSystemConfiguration() {
      boolean result = true;

      saveMRUConfigFiles();

      final WaitDialog waitDlg = new WaitDialog(
              "Loading system configuration, please wait...");

      waitDlg.setAlwaysOnTop(true);
      waitDlg.showDialog();
      this.setEnabled(false);

      try {
         if (sysConfigFile_.length() > 0) {
            GUIUtils.preventDisplayAdapterChangeExceptions();
            core_.waitForSystem();
            ignorePropertyChanges_ = true;
            core_.loadSystemConfiguration(sysConfigFile_);
            ignorePropertyChanges_ = false;
            GUIUtils.preventDisplayAdapterChangeExceptions();

         }
      } catch (final Exception err) {
         GUIUtils.preventDisplayAdapterChangeExceptions();

         ReportingUtils.showError(err);
         result = false;
      } finally {
         waitDlg.closeDialog();
      }
      setEnabled(true);
      initializeGUI();

      updateSwitchConfigurationMenu();

      FileDialogs.storePath(MM_CONFIG_FILE, new File(sysConfigFile_));

      return result;
   }

   private void saveMRUConfigFiles() {
      if (0 < sysConfigFile_.length()) {
         if (MRUConfigFiles_.contains(sysConfigFile_)) {
            MRUConfigFiles_.remove(sysConfigFile_);
         }
         if (maxMRUCfgs_ <= MRUConfigFiles_.size()) {
            MRUConfigFiles_.remove(maxMRUCfgs_ - 1);
         }
         MRUConfigFiles_.add(0, sysConfigFile_);
         // save the MRU list to the preferences
         for (Integer icfg = 0; icfg < MRUConfigFiles_.size(); ++icfg) {
            String value = "";
            if (null != MRUConfigFiles_.get(icfg)) {
               value = MRUConfigFiles_.get(icfg).toString();
            }
            mainPrefs_.put(CFGFILE_ENTRY_BASE + icfg.toString(), value);
         }
      }
   }

   private void loadMRUConfigFiles() {
      sysConfigFile_ = mainPrefs_.get(SYSTEM_CONFIG_FILE, sysConfigFile_);
      // startupScriptFile_ = mainPrefs_.get(STARTUP_SCRIPT_FILE,
      // startupScriptFile_);
      MRUConfigFiles_ = new ArrayList<String>();
      for (Integer icfg = 0; icfg < maxMRUCfgs_; ++icfg) {
         String value = "";
         value = mainPrefs_.get(CFGFILE_ENTRY_BASE + icfg.toString(), value);
         if (0 < value.length()) {
            File ruFile = new File(value);
            if (ruFile.exists()) {
               if (!MRUConfigFiles_.contains(value)) {
                  MRUConfigFiles_.add(value);
               }
            }
         }
      }
      // initialize MRU list from old persistant data containing only SYSTEM_CONFIG_FILE
      if (0 < sysConfigFile_.length()) {
         if (!MRUConfigFiles_.contains(sysConfigFile_)) {
            // in case persistant data is inconsistent
            if (maxMRUCfgs_ <= MRUConfigFiles_.size()) {
               MRUConfigFiles_.remove(maxMRUCfgs_ - 1);
            }
            MRUConfigFiles_.add(0, sysConfigFile_);
         }
      }
   }

   /**
    * Opens Acquisition dialog.
    */
   private void openAcqControlDialog() {
      try {
         if (acqControlWin_ == null) {
            acqControlWin_ = new AcqControlDlg(engine_, mainPrefs_, this, options_);
         }
         if (acqControlWin_.isActive()) {
            acqControlWin_.setTopPosition();
         }

         acqControlWin_.setVisible(true);
         
         acqControlWin_.repaint();

         // TODO: this call causes a strange exception the first time the
         // dialog is created
         // something to do with the order in which combo box creation is
         // performed

         // acqControlWin_.updateGroupsCombo();
      } catch (Exception exc) {
         ReportingUtils.showError(exc,
               "\nAcquistion window failed to open due to invalid or corrupted settings.\n"
               + "Try resetting registry settings to factory defaults (Menu Tools|Options).");
      }
   }
   
   /**
    * Opens a dialog to record stage positions
    */
   @Override
   public void showXYPositionList() {
      if (posListDlg_ == null) {
         posListDlg_ = new PositionListDlg(core_, this, posList_, options_);
      }
      posListDlg_.setVisible(true);
   }

   private void updateChannelCombos() {
      if (this.acqControlWin_ != null) {
         this.acqControlWin_.updateChannelAndGroupCombo();
      }
   }

   @Override
   public void setConfigChanged(boolean status) {
      configChanged_ = status;
      setConfigSaveButtonStatus(configChanged_);
   }


   /**
    * Returns the current background color
    * @return current background color
    */
   @Override
   public Color getBackgroundColor() {
      return guiColors_.background.get((options_.displayBackground_));
   }

   /*
    * Changes background color of this window and all other MM windows
    */
   @Override
   public void setBackgroundStyle(String backgroundType) {
      setBackground(guiColors_.background.get((backgroundType)));
      paint(MMStudioMainFrame.this.getGraphics());
      
      // sets background of all registered Components
      for (Component comp:MMFrames_) {
         if (comp != null)
            comp.setBackground(guiColors_.background.get(backgroundType));
       }
   }

   @Override
   public String getBackgroundStyle() {
      return options_.displayBackground_;
   }

   // //////////////////////////////////////////////////////////////////////////
   // Scripting interface
   // //////////////////////////////////////////////////////////////////////////
   private class ExecuteAcq implements Runnable {

      public ExecuteAcq() {
      }

      @Override
      public void run() {
         if (acqControlWin_ != null) {
            acqControlWin_.runAcquisition();
         }
      }
   }

   private void testForAbortRequests() throws MMScriptException {
      if (scriptPanel_ != null) {
         if (scriptPanel_.stopRequestPending()) {
            throw new MMScriptException("Script interrupted by the user!");
         }
      }
   }

   /**
    * @deprecated - used to be in api/AcquisitionEngine
    */
   public void startAcquisition() throws MMScriptException {
      testForAbortRequests();
      SwingUtilities.invokeLater(new ExecuteAcq());
   }

   @Override
   public String runAcquisition() throws MMScriptException {
      if (SwingUtilities.isEventDispatchThread()) {
         throw new MMScriptException("Acquisition can not be run from this (EDT) thread");
      }
      testForAbortRequests();
      if (acqControlWin_ != null) {
         String name = acqControlWin_.runAcquisition();
         try {
            while (acqControlWin_.isAcquisitionRunning()) {
               Thread.sleep(50);
            }
         } catch (InterruptedException e) {
            ReportingUtils.showError(e);
         }
         return name;
      } else {
         throw new MMScriptException(
               "Acquisition setup window must be open for this command to work.");
      }
   }

   @Override
   public String runAcquisition(String name, String root)
         throws MMScriptException {
      testForAbortRequests();
      if (acqControlWin_ != null) {
         String acqName = acqControlWin_.runAcquisition(name, root);
         try {
            while (acqControlWin_.isAcquisitionRunning()) {
               Thread.sleep(100);
            }
            // ensure that the acquisition has finished.
            // This does not seem to work, needs something better
            MMAcquisition acq = acqMgr_.getAcquisition(acqName);
            boolean finished = false;
            while (!finished) {
               ImageCache imCache = acq.getImageCache();
               if (imCache != null) {
                  if (imCache.isFinished()) {
                     finished = true;
                  } else {
                     Thread.sleep(100);
                  }
               }
            }

         } catch (InterruptedException e) {
            ReportingUtils.showError(e);
         }
         return acqName;
      } else {
         throw new MMScriptException(
               "Acquisition setup window must be open for this command to work.");
      }
   }

   /**
    * @deprecated used to be part of api
    */
   public String runAcqusition(String name, String root) throws MMScriptException {
      return runAcquisition(name, root);
   }

   /**
    * Loads acquisition settings from file
    * @param path file containing previously saved acquisition settings
    * @throws MMScriptException 
    */
   @Override
   public void loadAcquisition(String path) throws MMScriptException {
      testForAbortRequests();
      try {
         engine_.shutdown();

         // load protocol
         if (acqControlWin_ != null) {
            acqControlWin_.loadAcqSettingsFromFile(path);
         }
      } catch (Exception ex) {
         throw new MMScriptException(ex.getMessage());
      }

   }

   @Override
   public void setPositionList(PositionList pl) throws MMScriptException {
      testForAbortRequests();
      // use serialization to clone the PositionList object
      posList_ = pl; // PositionList.newInstance(pl);
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            if (posListDlg_ != null)
               posListDlg_.setPositionList(posList_);
            
            if (engine_ != null)
               engine_.setPositionList(posList_);
            
            if (acqControlWin_ != null)
               acqControlWin_.updateGUIContents();
         }
      });
   }

   @Override
   public PositionList getPositionList() throws MMScriptException {
      testForAbortRequests();
      // use serialization to clone the PositionList object
      return posList_; //PositionList.newInstance(posList_);
   }

   @Override
   public void sleep(long ms) throws MMScriptException {
      if (scriptPanel_ != null) {
         if (scriptPanel_.stopRequestPending()) {
            throw new MMScriptException("Script interrupted by the user!");
         }
         scriptPanel_.sleep(ms);
      }
   }

   @Override
   public String getUniqueAcquisitionName(String stub) {
      return acqMgr_.getUniqueAcquisitionName(stub);
   }
   
   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices, int nrPositions) throws MMScriptException {
      this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices,
              nrPositions, true, false);
   }

   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices) throws MMScriptException {
      openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0);
   }
   
   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices, int nrPositions, boolean show)
         throws MMScriptException {
      this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, nrPositions, show, false);
   }


   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices, boolean show)
         throws MMScriptException {
      this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0, show, false);
   }   

   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices, int nrPositions, boolean show, boolean virtual)
         throws MMScriptException {
      acqMgr_.openAcquisition(name, rootDir, show, virtual);
      MMAcquisition acq = acqMgr_.getAcquisition(name);
      acq.setDimensions(nrFrames, nrChannels, nrSlices, nrPositions);
   }

   @Override
   public void openAcquisition(String name, String rootDir, int nrFrames,
         int nrChannels, int nrSlices, boolean show, boolean virtual)
         throws MMScriptException {
      this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0, show, virtual);
   }

   public String createAcquisition(JSONObject summaryMetadata, boolean diskCached) {
      return createAcquisition(summaryMetadata, diskCached, false);
   }
   
   public String createAcquisition(JSONObject summaryMetadata, boolean diskCached, boolean displayOff) {
      return acqMgr_.createAcquisition(summaryMetadata, diskCached, engine_, displayOff);
   }


   private void openAcquisitionSnap(String name, String rootDir, boolean show)
         throws MMScriptException {
      /*
       MMAcquisition acq = acqMgr_.openAcquisitionSnap(name, rootDir, this,
            show);
      acq.setDimensions(0, 1, 1, 1);
      try {
         // acq.getAcqData().setPixelSizeUm(core_.getPixelSizeUm());
         acq.setProperty(SummaryKeys.IMAGE_PIXEL_SIZE_UM, String.valueOf(core_.getPixelSizeUm()));

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
       *
       */
   }

   @Override
   public void initializeSimpleAcquisition(String name, int width, int height,
         int byteDepth, int bitDepth, int multiCamNumCh) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(name);
      acq.setImagePhysicalDimensions(width, height, byteDepth, bitDepth, multiCamNumCh);
      acq.initializeSimpleAcq();
   }
   
   @Override
   public void initializeAcquisition(String name, int width, int height,
         int depth) throws MMScriptException {
      initializeAcquisition(name,width,height,depth,8*depth);
   }
   
   @Override
   public void initializeAcquisition(String name, int width, int height,
         int byteDepth, int bitDepth) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(name);
      //number of multi-cam cameras is set to 1 here for backwards compatibility
      //might want to change this later
      acq.setImagePhysicalDimensions(width, height, byteDepth, bitDepth,1);
      acq.initialize();
   }

   @Override
   public int getAcquisitionImageWidth(String acqName) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      return acq.getWidth();
   }

   @Override
   public int getAcquisitionImageHeight(String acqName) throws MMScriptException{
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      return acq.getHeight();
   }

   @Override
   public int getAcquisitionImageBitDepth(String acqName) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      return acq.getBitDepth();
   }
   
   @Override
   public int getAcquisitionImageByteDepth(String acqName) throws MMScriptException{
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      return acq.getByteDepth();
   }

   @Override public int getAcquisitionMultiCamNumChannels(String acqName) throws MMScriptException{
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      return acq.getMultiCameraNumChannels();
   }
   
   @Override
   public Boolean acquisitionExists(String name) {
      return acqMgr_.acquisitionExists(name);
   }

   @Override
   public void closeAcquisition(String name) throws MMScriptException {
      acqMgr_.closeAcquisition(name);
   }

   /**
    * @deprecated  use closeAcquisitionWindow instead
    * @deprecated - used to be in api/AcquisitionEngine
    */
   public void closeAcquisitionImage5D(String acquisitionName) throws MMScriptException {
      acqMgr_.closeImageWindow(acquisitionName);
   }

   @Override
   public void closeAcquisitionWindow(String acquisitionName) throws MMScriptException {
      acqMgr_.closeImageWindow(acquisitionName);
   }

   /**
    * @deprecated - used to be in api/AcquisitionEngine
    * Since Burst and normal acquisition are now carried out by the same engine,
    * loadBurstAcquistion simply calls loadAcquisition
    * t
    * @param path - path to file specifying acquisition settings
    */
   public void loadBurstAcquisition(String path) throws MMScriptException {
      this.loadAcquisition(path);
   }

   @Override
   public void refreshGUI() {
      updateGUI(true);
   }
   
   @Override
   public void refreshGUIFromCache() {
      updateGUI(true, true);
   }

   public void setAcquisitionProperty(String acqName, String propertyName,
         String value) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      acq.setProperty(propertyName, value);
   }

   public void setAcquisitionSystemState(String acqName, JSONObject md) throws MMScriptException {
//      acqMgr_.getAcquisition(acqName).setSystemState(md);
      setAcquisitionSummary(acqName, md);
   }

   public void setAcquisitionSummary(String acqName, JSONObject md) throws MMScriptException {
      acqMgr_.getAcquisition(acqName).setSummaryProperties(md);
   }

   public void setImageProperty(String acqName, int frame, int channel,
         int slice, String propName, String value) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(acqName);
      acq.setProperty(frame, channel, slice, propName, value);
   }


   public void snapAndAddImage(String name, int frame, int channel, int slice)
           throws MMScriptException {
      snapAndAddImage(name, frame, channel, slice, 0);
   }

   public void snapAndAddImage(String name, int frame, int channel, int slice, int position)
           throws MMScriptException {
      TaggedImage ti;
      try {
         if (core_.isSequenceRunning()) {
            ti = core_.getLastTaggedImage();
         } else {
            core_.snapImage();
            ti = core_.getTaggedImage();
         }
         MDUtils.setChannelIndex(ti.tags, channel);
         MDUtils.setFrameIndex(ti.tags, frame);
         MDUtils.setSliceIndex(ti.tags, slice);
         MDUtils.setPositionIndex(ti.tags, position);
         
         MMAcquisition acq = acqMgr_.getAcquisition(name);
         if (!acq.isInitialized()) {
            long width = core_.getImageWidth();
            long height = core_.getImageHeight();
            long depth = core_.getBytesPerPixel();
            long bitDepth = core_.getImageBitDepth();
            int multiCamNumCh = (int) core_.getNumberOfCameraChannels();

            acq.setImagePhysicalDimensions((int) width, (int) height, (int) depth, (int) bitDepth, multiCamNumCh);
            acq.initialize();
         }
         
         if (acq.getPositions() > 1) {
            MDUtils.setPositionName(ti.tags, "Pos" + position);
         }

         addImage(name, ti, true);
      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

   public String getCurrentAlbum() {
      return acqMgr_.getCurrentAlbum();
   }

   public String createNewAlbum() {
      return acqMgr_.createNewAlbum();
   }

   public void appendImage(String name, TaggedImage taggedImg) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(name);
      int f = 1 + acq.getLastAcquiredFrame();
      try {
         MDUtils.setFrameIndex(taggedImg.tags, f);
         } catch (JSONException e) {
            throw new MMScriptException("Unable to set the frame index.");
         }
      acq.insertTaggedImage(taggedImg, f, 0, 0);
   }

   public void addToAlbum(TaggedImage taggedImg) throws MMScriptException {
      addToAlbum(taggedImg, null);
   }
   
   public void addToAlbum(TaggedImage taggedImg, JSONObject displaySettings) throws MMScriptException {
      normalizeTags(taggedImg);
      acqMgr_.addToAlbum(taggedImg,displaySettings);
   }

   public void addImage(String name, Object img, int frame, int channel,
         int slice) throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(name);
      acq.insertImage(img, frame, channel, slice);
   }

   public void addImage(String name, TaggedImage taggedImg) throws MMScriptException {
      acqMgr_.getAcquisition(name).insertImage(taggedImg);
   }

   public void addImage(String name, TaggedImage taggedImg, boolean updateDisplay) throws MMScriptException {
      acqMgr_.getAcquisition(name).insertImage(taggedImg, updateDisplay);
   }
   
   public void addImage(String name, TaggedImage taggedImg, 
           boolean updateDisplay,
           boolean waitForDisplay) throws MMScriptException {
   acqMgr_.getAcquisition(name).insertImage(taggedImg, updateDisplay, waitForDisplay);
}
   
   public void addImage(String name, TaggedImage taggedImg, int frame, int channel, 
           int slice, int position) throws MMScriptException {
      try {
         acqMgr_.getAcquisition(name).insertImage(taggedImg, frame, channel, slice, position);
      } catch (JSONException ex) {
         ReportingUtils.showError(ex);
      }
   }

   public void addImage(String name, TaggedImage taggedImg, int frame, int channel, 
           int slice, int position, boolean updateDisplay) throws MMScriptException {
      try {
         acqMgr_.getAcquisition(name).insertImage(taggedImg, frame, channel, slice, position, updateDisplay);
      } catch (JSONException ex) {
         ReportingUtils.showError(ex);
      }  
   }

   public void addImage(String name, TaggedImage taggedImg, int frame, int channel,
           int slice, int position, boolean updateDisplay, boolean waitForDisplay) throws MMScriptException {
      try {
         acqMgr_.getAcquisition(name).insertImage(taggedImg, frame, channel, slice, position, updateDisplay, waitForDisplay);
      } catch (JSONException ex) {
         ReportingUtils.showError(ex);
      }
   }

   /**
    * Closes all acquisitions
    */
   public void closeAllAcquisitions() {
      acqMgr_.closeAll();
   }

   public String[] getAcquisitionNames()
   {
      return acqMgr_.getAcqusitionNames();
   }
   
   public MMAcquisition getAcquisition(String name) throws MMScriptException {
      return acqMgr_.getAcquisition(name);
   }

   private class ScriptConsoleMessage implements Runnable {

      String msg_;

      public ScriptConsoleMessage(String text) {
         msg_ = text;
      }

      public void run() {
         if (scriptPanel_ != null)
            scriptPanel_.message(msg_);
      }
   }

   public void message(String text) throws MMScriptException {
      if (scriptPanel_ != null) {
         if (scriptPanel_.stopRequestPending()) {
            throw new MMScriptException("Script interrupted by the user!");
         }

         SwingUtilities.invokeLater(new ScriptConsoleMessage(text));
      }
   }

   public void clearMessageWindow() throws MMScriptException {
      if (scriptPanel_ != null) {
         if (scriptPanel_.stopRequestPending()) {
            throw new MMScriptException("Script interrupted by the user!");
         }
         scriptPanel_.clearOutput();
      }
   }

   public void clearOutput() throws MMScriptException {
      clearMessageWindow();
   }

   public void clear() throws MMScriptException {
      clearMessageWindow();
   }

   public void setChannelContrast(String title, int channel, int min, int max)
         throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(title);
      acq.setChannelContrast(channel, min, max);
   }

   public void setChannelName(String title, int channel, String name)
         throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(title);
      acq.setChannelName(channel, name);

   }

   public void setChannelColor(String title, int channel, Color color)
         throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(title);
      acq.setChannelColor(channel, color.getRGB());
   }

   public void setContrastBasedOnFrame(String title, int frame, int slice)
         throws MMScriptException {
      MMAcquisition acq = acqMgr_.getAcquisition(title);
      acq.setContrastBasedOnFrame(frame, slice);
   }

   public void setStagePosition(double z) throws MMScriptException {
      try {
         core_.setPosition(core_.getFocusDevice(),z);
         core_.waitForDevice(core_.getFocusDevice());
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
   }

   public void setRelativeStagePosition(double z) throws MMScriptException {
      try {
         core_.setRelativePosition(core_.getFocusDevice(), z);
         core_.waitForDevice(core_.getFocusDevice());
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
   }


   public void setXYStagePosition(double x, double y) throws MMScriptException {
      try {
         core_.setXYPosition(core_.getXYStageDevice(), x, y);
         core_.waitForDevice(core_.getXYStageDevice());
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
   }

      public void setRelativeXYStagePosition(double x, double y) throws MMScriptException {
      try {
         core_.setRelativeXYPosition(core_.getXYStageDevice(), x, y);
         core_.waitForDevice(core_.getXYStageDevice());
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
   }

   public Point2D.Double getXYStagePosition() throws MMScriptException {
      String stage = core_.getXYStageDevice();
      if (stage.length() == 0) {
         throw new MMScriptException("XY Stage device is not available");
      }

      double x[] = new double[1];
      double y[] = new double[1];
      try {
         core_.getXYPosition(stage, x, y);
         Point2D.Double pt = new Point2D.Double(x[0], y[0]);
         return pt;
      } catch (Exception e) {
         throw new MMScriptException(e.getMessage());
      }
   }

   public String getXYStageName() {
      return core_.getXYStageDevice();
   }

   public void setXYOrigin(double x, double y) throws MMScriptException {
      String xyStage = core_.getXYStageDevice();
      try {
         core_.setAdapterOriginXY(xyStage, x, y);
      } catch (Exception e) {
         throw new MMScriptException(e);
      }
   }

   public AcquisitionWrapperEngine getAcquisitionEngine() {
      return engine_;
   }

   public String installPlugin(Class<?> cl) {
      String className = cl.getSimpleName();
      String msg = className + " module loaded.";
      try {
         for (PluginItem plugin : plugins_) {
            if (plugin.className.contentEquals(className)) {
               return className + " already loaded.";
            }
         }

         PluginItem pi = new PluginItem();
         pi.className = className;
         try {
            // Get this static field from the class implementing MMPlugin.
            pi.menuItem = (String) cl.getDeclaredField("menuName").get(null);
         } catch (SecurityException e) {
            ReportingUtils.logError(e);
            pi.menuItem = className;
         } catch (NoSuchFieldException e) {
            pi.menuItem = className;
            ReportingUtils.logMessage(className + " fails to implement static String menuName.");
         } catch (IllegalArgumentException e) {
            ReportingUtils.logError(e);
         } catch (IllegalAccessException e) {
            ReportingUtils.logError(e);
         }

         if (pi.menuItem == null) {
            pi.menuItem = className;
         }
         pi.menuItem = pi.menuItem.replace("_", " ");
         pi.pluginClass = cl;
         plugins_.add(pi);
         final PluginItem pi2 = pi;
         final Class<?> cl2 = cl;
         SwingUtilities.invokeLater(
            new Runnable() {
               public void run() {
                  addPluginToMenu(pi2, cl2);
               }
            });

      } catch (NoClassDefFoundError e) {
         msg = className + " class definition not found.";
         ReportingUtils.logError(e, msg);

      }

      return msg;

   }

   public String installPlugin(String className, String menuName) {
      String msg = "installPlugin(String className, String menuName) is deprecated. Use installPlugin(String className) instead.";
      core_.logMessage(msg);
      installPlugin(className);
      return msg;
   }

   public String installPlugin(String className) {
      try {
         Class clazz = Class.forName(className);
         return installPlugin(clazz);
      } catch (ClassNotFoundException e) {
         String msg = className + " plugin not found.";
         ReportingUtils.logError(e, msg);
         return msg;
      }
   }

   public String installAutofocusPlugin(String className) {
      try {
         return installAutofocusPlugin(Class.forName(className));
      } catch (ClassNotFoundException e) {
         String msg = "Internal error: AF manager not instantiated.";
         ReportingUtils.logError(e, msg);
         return msg;
      }
   }

   public String installAutofocusPlugin(Class<?> autofocus) {
      String msg = autofocus.getSimpleName() + " module loaded.";
      if (afMgr_ != null) {
         try {
            afMgr_.refresh();
         } catch (MMException e) {
            msg = e.getMessage();
            ReportingUtils.logError(e);
         }
         afMgr_.setAFPluginClassName(autofocus.getSimpleName());
      } else {
         msg = "Internal error: AF manager not instantiated.";
      }
      return msg;
   }

   public CMMCore getCore() {
      return core_;
   }

   @Override
   public IAcquisitionEngine2010 getAcquisitionEngine2010() {
      try {
         acquisitionEngine2010LoadingThread.join();
         if (acquisitionEngine2010 == null) {
            acquisitionEngine2010 = (IAcquisitionEngine2010) acquisitionEngine2010Class.getConstructor(ScriptInterface.class).newInstance(this);
         }
         return acquisitionEngine2010;
      } catch (Exception e) {
         ReportingUtils.logError(e);
         return null;
      }
   }
   
   @Override
   public void addImageProcessor(DataProcessor<TaggedImage> processor) {
	   getAcquisitionEngine().addImageProcessor(processor);
   }

   @Override
   public void removeImageProcessor(DataProcessor<TaggedImage> processor) {
	   getAcquisitionEngine().removeImageProcessor(processor);
   }

   @Override
   public void setPause(boolean state) {
	   getAcquisitionEngine().setPause(state);
   }

   @Override
   public boolean isPaused() {
	   return getAcquisitionEngine().isPaused();
   }
   
   @Override
   public void attachRunnable(int frame, int position, int channel, int slice, Runnable runnable) {
	   getAcquisitionEngine().attachRunnable(frame, position, channel, slice, runnable);
   }

   @Override
   public void clearRunnables() {
	   getAcquisitionEngine().clearRunnables();
   }
   
   @Override
   public SequenceSettings getAcqusitionSettings() {
	   if (engine_ == null)
		   return new SequenceSettings();
	   return engine_.getSequenceSettings();
   }
   
   @Override
   public String getAcquisitionPath() {
	   if (engine_ == null)
		   return null;
	   return engine_.getImageCache().getDiskLocation();
   }
   
   @Override
   public void promptToSaveAcqusition(String name, boolean prompt) throws MMScriptException {
	   MMAcquisition acq = getAcquisition(name);
	   getAcquisition(name).promptToSave(prompt);
   }


   public void snapAndAddToImage5D() {
      if (core_.getCameraDevice().length() == 0) {
         ReportingUtils.showError("No camera configured");
         return;
      }
      try {
         if (this.isLiveModeOn()) {
            copyFromLiveModeToAlbum(simpleDisplay_);
         } else {
            doSnap(true);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   public void setAcquisitionEngine(AcquisitionWrapperEngine eng) {
      engine_ = eng;
   }
   
   public void suspendLiveMode() {
      liveModeSuspended_ = isLiveModeOn();
      enableLiveMode(false);
   }

   public void resumeLiveMode() {
      if (liveModeSuspended_) {
         enableLiveMode(true);
      }
   }

   public Autofocus getAutofocus() {
      return afMgr_.getDevice();
   }

   public void showAutofocusDialog() {
      if (afMgr_.getDevice() != null) {
         afMgr_.showOptionsDialog();
      }
   }

   public AutofocusManager getAutofocusManager() {
      return afMgr_;
   }

   public void selectConfigGroup(String groupName) {
      configPad_.setGroup(groupName);
   }

   public String regenerateDeviceList()   {
            Cursor oldc = Cursor.getDefaultCursor();
            Cursor waitc = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(waitc);
            StringBuffer resultFile = new StringBuffer();
            MicroscopeModel.generateDeviceListFile(resultFile, core_);
            //MicroscopeModel.generateDeviceListFile();
            setCursor(oldc);
            return resultFile.toString();
   }
   
   public void setImageSavingFormat(Class imageSavingClass) throws MMScriptException {
      if (! (imageSavingClass.equals(TaggedImageStorageDiskDefault.class) || 
              imageSavingClass.equals(TaggedImageStorageMultipageTiff.class))) {
         throw new MMScriptException("Unrecognized saving class");
      }
      ImageUtils.setImageStorageClass(imageSavingClass);
      if (acqControlWin_ != null) {
         acqControlWin_.updateSavingTypeButtons();
      }
   }




   private void loadPlugins() {
      
      ArrayList<Class<?>> pluginClasses = new ArrayList<Class<?>>();
      ArrayList<Class<?>> autofocusClasses = new ArrayList<Class<?>>();
      List<Class<?>> classes;

      try {
         long t1 = System.currentTimeMillis();
         classes = JavaUtils.findClasses(new File("mmplugins"), 2);
         //System.out.println("findClasses: " + (System.currentTimeMillis() - t1));
         //System.out.println(classes.size());
         for (Class<?> clazz : classes) {
            for (Class<?> iface : clazz.getInterfaces()) {
               //core_.logMessage("interface found: " + iface.getName());
               if (iface == MMPlugin.class) {
                  pluginClasses.add(clazz);
               }
            }

         }

         classes = JavaUtils.findClasses(new File("mmautofocus"), 2);
         for (Class<?> clazz : classes) {
            for (Class<?> iface : clazz.getInterfaces()) {
               //core_.logMessage("interface found: " + iface.getName());
               if (iface == Autofocus.class) {
                  autofocusClasses.add(clazz);
               }
            }
         }

      } catch (ClassNotFoundException e1) {
         ReportingUtils.logError(e1);
      }

      for (Class<?> plugin : pluginClasses) {
         try {
            ReportingUtils.logMessage("Attempting to install plugin " + plugin.getName());
            installPlugin(plugin);
         } catch (Exception e) {
            ReportingUtils.logError(e, "Failed to install the \"" + plugin.getName() + "\" plugin .");
         }
      }

      for (Class<?> autofocus : autofocusClasses) {
         try {
            ReportingUtils.logMessage("Attempting to install autofocus plugin " + autofocus.getName());
            installAutofocusPlugin(autofocus.getName());
         } catch (Exception e) {
            ReportingUtils.logError("Failed to install the \"" + autofocus.getName() + "\" autofocus plugin.");
         }
      }

   }

   public void logMessage(String msg) {
      ReportingUtils.logMessage(msg);
   }

   public void showMessage(String msg) {
      ReportingUtils.showMessage(msg);
   }

   public void logError(Exception e, String msg) {
      ReportingUtils.logError(e, msg);
   }

   public void logError(Exception e) {
      ReportingUtils.logError(e);
   }

   public void logError(String msg) {
      ReportingUtils.logError(msg);
   }

   public void showError(Exception e, String msg) {
      ReportingUtils.showError(e, msg);
   }

   public void showError(Exception e) {
      ReportingUtils.showError(e);
   }

   public void showError(String msg) {
      ReportingUtils.showError(msg);
   }

   private void runHardwareWizard() {
      try {
         if (configChanged_) {
            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(null,
                  "Save Changed Configuration?", "Micro-Manager",
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE, null, options,
                  options[0]);
            if (n == JOptionPane.YES_OPTION) {
               saveConfigPresets();
            }
            configChanged_ = false;
         }

         boolean liveRunning = false;
         if (isLiveModeOn()) {
            liveRunning = true;
            enableLiveMode(false);
         }

         // unload all devices before starting configurator
         core_.reset();
         GUIUtils.preventDisplayAdapterChangeExceptions();

         // run Configurator
         ConfiguratorDlg2 cfg2 = null;
         try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            cfg2 = new ConfiguratorDlg2(core_, sysConfigFile_);
         } finally {
            setCursor(Cursor.getDefaultCursor());        		 
         }

         if (cfg2 == null)
         {
            ReportingUtils.showError("Failed to launch Hardware Configuration Wizard");
            return;
         }
         cfg2.setVisible(true);
         GUIUtils.preventDisplayAdapterChangeExceptions();

         // re-initialize the system with the new configuration file
         sysConfigFile_ = cfg2.getFileName();

         mainPrefs_.put(SYSTEM_CONFIG_FILE, sysConfigFile_);
         loadSystemConfiguration();
         GUIUtils.preventDisplayAdapterChangeExceptions();

         if (liveRunning) {
            enableLiveMode(liveRunning);
         }

      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

}

class BooleanLock extends Object {

   private boolean value;

   public BooleanLock(boolean initialValue) {
      value = initialValue;
   }

   public BooleanLock() {
      this(false);
   }

   public synchronized void setValue(boolean newValue) {
      if (newValue != value) {
         value = newValue;
         notifyAll();
      }
   }

   public synchronized boolean waitToSetTrue(long msTimeout)
         throws InterruptedException {

      boolean success = waitUntilFalse(msTimeout);
      if (success) {
         setValue(true);
      }

      return success;
   }

   public synchronized boolean waitToSetFalse(long msTimeout)
         throws InterruptedException {

      boolean success = waitUntilTrue(msTimeout);
      if (success) {
         setValue(false);
      }

      return success;
   }

   public synchronized boolean isTrue() {
      return value;
   }

   public synchronized boolean isFalse() {
      return !value;
   }

   public synchronized boolean waitUntilTrue(long msTimeout)
         throws InterruptedException {

      return waitUntilStateIs(true, msTimeout);
   }

   public synchronized boolean waitUntilFalse(long msTimeout)
         throws InterruptedException {

      return waitUntilStateIs(false, msTimeout);
   }

   public synchronized boolean waitUntilStateIs(
         boolean state,
         long msTimeout) throws InterruptedException {

      if (msTimeout == 0L) {
         while (value != state) {
            wait();
         }

         return true;
      }

      long endTime = System.currentTimeMillis() + msTimeout;
      long msRemaining = msTimeout;

      while ((value != state) && (msRemaining > 0L)) {
         wait(msRemaining);
         msRemaining = endTime - System.currentTimeMillis();
      }

      return (value == state);
   }


 

}

