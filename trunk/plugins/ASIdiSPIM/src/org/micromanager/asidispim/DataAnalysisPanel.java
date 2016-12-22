
package org.micromanager.asidispim;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.json.JSONObject;
import org.micromanager.api.MMWindow;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.MyStrings;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.MyDialogUtils;
import org.micromanager.asidispim.Utils.PanelUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.utils.ReportingUtils;


/**
 * Panel in ASIdiSPIM plugin specifically for data analysis/processing
 * For now, we provide a way to export Micro-Manager datasets into 
 * a mipav compatible format 
 * mipav likes data in a folder as follows:
 * folder - SPIMA - name_SPIMA-0.tif, name_SPIMA-x.tif, name_SPIMA-n.tif
 *        - SPIMB - name_SPIMB-0.tif, name_SPIMB-x.tif, name_SPIMB-n.tif
 * @author Nico
 * @author Jon
 */
@SuppressWarnings("serial")
public class DataAnalysisPanel extends ListeningJPanel {
   private final Prefs prefs_;
   private final JPanel exportPanel_;
   private final JPanel deskewPanel_;
   private final JTextField saveDestinationField_;
   private final JTextField baseNameField_;
   private final JFormattedTextField deskewFactor_;
   private final JCheckBox deskewInvert_;
   private final JCheckBox deskewInterpolate_;
   private final JCheckBox deskewAutoTest_;
   
   public static final String[] TRANSFORMOPTIONS = 
      {"None", "Rotate Right 90\u00B0", "Rotate Left 90\u00B0", "Rotate outward"};
   public static final String[] EXPORTFORMATS = 
      {"mipav GenerateFusion", "Multiview Reconstruction (deprecated)"};
   public static FileDialogs.FileType EXPORT_DATA_SET 
           = new FileDialogs.FileType("EXPORT_DATA_SET",
                 "Export to Location",
                 System.getProperty("user.home") + "/Untitled",
                 false, (String[]) null);
   
   /**
    * 
    * @param gui
    * @param prefs - Plugin-wide preferences
    */
   public DataAnalysisPanel(Prefs prefs, Properties props, Devices devices) {    
      super(MyStrings.PanelNames.DATAANALYSIS.toString(),
              new MigLayout(
              "",
              "[right]",
              "[]16[]"));
      prefs_ = prefs;
      PanelUtils pu = new PanelUtils(prefs_, props, devices);
      final DataAnalysisPanel dataAnalysisPanel = this;
            
      int textFieldWidth = 35;

      // start export sub-panel
      exportPanel_ = new JPanel(new MigLayout(
              "",
              "[right]4[center]4[left]",
              "[]8[]"));
      
      exportPanel_.setBorder(PanelUtils.makeTitledBorder("Export diSPIM data"));
      
      exportPanel_.add(new JLabel("Export directory:"), "");
      
      saveDestinationField_ = new JTextField();
      saveDestinationField_.setText(prefs_.getString(panelName_,
              Properties.Keys.PLUGIN_EXPORT_DATA_DIR, ""));
      saveDestinationField_.setColumns(textFieldWidth);
      saveDestinationField_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
             prefs_.putString(panelName_, Properties.Keys.PLUGIN_EXPORT_DATA_DIR,
                    saveDestinationField_.getText());
         }
      });
      exportPanel_.add(saveDestinationField_);
      
      JButton browseToSaveDestinationButton = new JButton();
      browseToSaveDestinationButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            setSaveDestinationDirectory(saveDestinationField_);
            prefs_.putString(panelName_, Properties.Keys.PLUGIN_EXPORT_DATA_DIR,
                    saveDestinationField_.getText());
         }
      });
      
      browseToSaveDestinationButton.setMargin(new Insets(2, 5, 2, 5));
      browseToSaveDestinationButton.setText("...");
      exportPanel_.add(browseToSaveDestinationButton, "wrap");
      
      exportPanel_.add(new JLabel("Base Name:"), "");
      baseNameField_ = new JTextField();
      proposeBaseFieldText();
      baseNameField_.setColumns(textFieldWidth);
      exportPanel_.add(baseNameField_, "wrap");
      
      
      // row with transform options
      JLabel transformLabel = new JLabel("Transform:");
      exportPanel_.add(transformLabel);
      final JComboBox transformSelect = new JComboBox();
      for (String item : TRANSFORMOPTIONS) {
         transformSelect.addItem(item);
      }
      String transformOption = prefs_.getString(
              panelName_, Properties.Keys.PLUGIN_EXPORT_TRANSFORM_OPTION, 
              TRANSFORMOPTIONS[1]);
      transformSelect.setSelectedItem(transformOption);
      transformSelect.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            prefs_.putString(panelName_, 
                    Properties.Keys.PLUGIN_EXPORT_TRANSFORM_OPTION, 
                    (String)transformSelect.getSelectedItem());
         }
      });
      exportPanel_.add(transformSelect, "left, wrap");
      
      // row with output options
      JLabel exportFormatLabel = new JLabel("Export for:");
      exportPanel_.add(exportFormatLabel);
      final JComboBox exportFormatSelect = new JComboBox();
      for (String item : EXPORTFORMATS) {
         exportFormatSelect.addItem(item);
      }
      String exportFormatOption = prefs_.getString(
              panelName_, Properties.Keys.PLUGIN_EXPORT_FORMAT, 
              EXPORTFORMATS[1]);
      exportFormatSelect.setSelectedItem(exportFormatOption);
      exportFormatSelect.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            prefs_.putString(panelName_, 
                    Properties.Keys.PLUGIN_EXPORT_FORMAT, 
                    (String)exportFormatSelect.getSelectedItem());
         }
      });
      exportPanel_.add(exportFormatSelect, "left, wrap");
      
      final JProgressBar progBar = new JProgressBar();
      progBar.setStringPainted(true);
      progBar.setVisible(false);
      final JLabel infoLabel = new JLabel("");
     
      JButton exportButton = new JButton("Export");
      exportButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ExportTask task = new ExportTask(saveDestinationField_.getText(),
                    baseNameField_.getText(),
                    transformSelect.getSelectedIndex(), 
                    exportFormatSelect.getSelectedIndex() );
            task.addPropertyChangeListener(new PropertyChangeListener() {

               @Override
               public void propertyChange(PropertyChangeEvent evt) {
                  if ("progress".equals(evt.getPropertyName())) {
                     int progress = (Integer) evt.getNewValue();
                     if (!progBar.isVisible()) {
                        progBar.setVisible(true);
                        infoLabel.setText("Saving...");
                        infoLabel.setVisible(true);
                     }
                     progBar.setValue(progress);
                     if (progress == 100) {
                        progBar.setVisible(false);
                        infoLabel.setText("Done Saving...");
                     }
                  }
               }
            });
            task.execute();
         }
      });
      exportPanel_.add(exportButton, "span 3, center, wrap");
      exportPanel_.add(infoLabel,"");
      exportPanel_.add(progBar, "span3, center, wrap");    
      
      // end export sub-panel
      
      this.add(exportPanel_);
      
      // start deskew sub-panel
      deskewPanel_ = new JPanel(new MigLayout(
              "",
              "[right]4[center]",
              "[]8[]"));
      
      deskewPanel_.setBorder(PanelUtils.makeTitledBorder("Deskew stage scanning data"));
      
      deskewPanel_.add(new JLabel("Deskew fudge factor:"));
      deskewFactor_ = pu.makeFloatEntryField(panelName_, 
            Properties.Keys.PLUGIN_DESKEW_FACTOR.toString(), 1.0, 5);
      deskewPanel_.add(deskewFactor_, "wrap");
      
      deskewInvert_ = pu.makeCheckBox("Invert direction",
            Properties.Keys.PLUGIN_DESKEW_INVERT, panelName_, false);
      deskewPanel_.add(deskewInvert_, "left, span 2, wrap");
        
      deskewInterpolate_ = pu.makeCheckBox("Interpolate",
            Properties.Keys.PLUGIN_DESKEW_INTERPOLATE, panelName_, false);
      deskewPanel_.add(deskewInterpolate_, "left, span 2, wrap");
      
      deskewAutoTest_ = pu.makeCheckBox("Auto-deskew test acquisitions",
            Properties.Keys.PLUGIN_DESKEW_AUTO_TEST, panelName_, false);
      deskewPanel_.add(deskewAutoTest_, "left, span 2, wrap");
      
      JButton deskewButton = new JButton("Deskew Open Dataset");
      deskewButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            runDeskew(dataAnalysisPanel);
         }
      });
      deskewPanel_.add(deskewButton, "span 2, wrap");
      
      this.add(deskewPanel_);
   }
   
   public void runDeskew(final ListeningJPanel caller) {
      
      /**
       * Worker thread to execute deskew.
       * Patterned after Nico's ExportTask SwingWorker code but updating progress bar wasn't working
       *   and task is pretty quick so I removed that code.
       * @author Jon
       *
       */
      class DeskewTask extends SwingWorker<Void, Void> {

         DeskewTask() {
            // empty constructor for now 
         };

         @Override
         protected Void doInBackground() throws Exception {
            setProgress(0);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            long startTime = System.currentTimeMillis();
            final ImagePlus ip = IJ.getImage();
            final MMWindow mmW = new MMWindow(ip);
            boolean firstSideIsA = true;
            String windowTitle = "";
            if (mmW.isMMWindow()) {  // have Micro-Manager dataset
               final JSONObject metadata = mmW.getSummaryMetaData();
               if (!metadata.getString("SPIMmode").equals("Stage scan")) {
                  throw new Exception("Can only deskew stage scanning data; support for stage scanning interleaved needs to be added");
               }
               if (metadata.getString("FirstSide").equals("B")) {
                  firstSideIsA = false;
               }
               if (metadata.has("AcquisitionName")) {
                  windowTitle = metadata.getString("AcqusitionName");
               }
            }
            
            if (windowTitle.equals("")) {
               windowTitle = ip.getTitle();
            }

            if (deskewInvert_.isSelected()) {
               // "spread" the stack in the other direction 
               firstSideIsA = ! firstSideIsA;
            }

            // for 45 degrees we shift the same amount as the interplane spacing, so factor of 1.0
            // assume diSPIM unless marked specifically otherwise
            // I don't understand why mathematically but it seems that for oSPIM the factor is 1.0
            //   too instead of being tan(60 degrees) due to the rotation
            final double zStepPx = ip.getCalibration().pixelDepth / ip.getCalibration().pixelWidth;
            final double dx = zStepPx * (Double) deskewFactor_.getValue();

            final int sc = ip.getNChannels();
            final int sx = ip.getWidth();
            final int sy = ip.getHeight();
            final int ss = ip.getNSlices();
            final String title = windowTitle + "-deskewed";
            final int sx_new = sx + (int) Math.abs(Math.ceil(dx*ss));

            if (sc > 1) {
               IJ.run("Duplicate...", "title=" + title + " duplicate");
               IJ.run("Split Channels");
            } else {
               IJ.run("Duplicate...", "title=C1-" + title + " duplicate");  // make it named as 1st channel would be
            }
            String mergeCmd = "";
            for (int c=0; c<sc; c++) {  // loop over channels
               IJ.selectWindow("C" + (c+1) + "-" + title);
               int dir = (c % 2) * 2 - 1;  // -1 for path A which are odd channels, -1 for path B
               if (!firstSideIsA) {
                  dir *= -1;
               }
               IJ.run("Canvas Size...", "width=" + sx_new + " height=" + sy + " position=Center-" 
                     + (dir < 0 ? "Right" : "Left") + " zero");
               for (int s=0; s<ss; s++) {  // loop over slices in stack
                  IJ.setSlice(s+1);
                  IJ.run("Translate...", "x=" + (dx*s*dir) + " y=0 interpolation="
                        + (deskewInterpolate_.isSelected() ? "Bilinear slice" : "None"));
               }
               mergeCmd += ("c" + (c+1) + "=C" + (c+1) + "-" + title + " ");  
            }
            if (sc > 1) {
               IJ.run("Merge Channels...", mergeCmd + "create");
            } else {
               IJ.run("Rename...", "title=" + title);
            }
            long finishTime = System.currentTimeMillis();
            ReportingUtils.logDebugMessage("Deskew operation took " + (finishTime - startTime) + 
                  " milliseconds with total of " + (sc*ss) + " images");

            return null;
         }

         @Override
         public void done() {
            setCursor(null);
            try {
               get();
               setProgress(100);
            } catch (ExecutionException ex) {
               Throwable cause = ex.getCause();
               if (!cause.getMessage().equals("Macro canceled")) {
                  if (cause instanceof SaveTaskException) {
                     MyDialogUtils.showError(cause, "Deskew error");
                  } else {
                     MyDialogUtils.showError(ex);
                  }
               }
            } catch (InterruptedException ex) {
               MyDialogUtils.showError(ex, "Interrupted while deskewing data");
            }
         }

      }
      
      // runDeskew() code goes here
      (new DeskewTask()).execute();

   }
   
   @Override
   public void gotSelected() {
      proposeBaseFieldText();
   }
   
   private void proposeBaseFieldText() {
      ImagePlus ip = WindowManager.getCurrentImage();
      if (ip != null) {
         String baseName = ip.getShortTitle();
         baseName = baseName.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
         baseNameField_.setText(baseName);
      }
   }
   
   
   /**
    * Worker thread that executes file saving.  Updates the progress bar
    * using the setProgress method, which results in a PropertyChangedEvent
    * in attached listeners
    */
   class ExportTask extends SwingWorker<Void, Void> {
      final String targetDirectory_;
      final String baseName_;
      final int transformIndex_;
      final int exportFormat_;
      ExportTask (String targetDirectory, String baseName, 
              int transformIndex, int exportFormat) {
         targetDirectory_ = targetDirectory;
         baseName_ = baseName.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
         transformIndex_ = transformIndex;
         exportFormat_ = exportFormat;
      }
   
      @Override
      protected Void doInBackground() throws Exception {
         setProgress(0);
         ImagePlus ip = IJ.getImage();
         MMWindow mmW = new MMWindow(ip);

         if (!mmW.isMMWindow()) {
            throw new SaveTaskException("Can only convert Micro-Manager data set ");
         }

         if (exportFormat_ == 0) { // mipav
            
            boolean multiPosition = false;
            if (mmW.getNumberOfPositions() > 1) {
               multiPosition = true;
            }
            
            boolean firstSideIsA = true;
            if (mmW.getSummaryMetaData().getString("FirstSide").equals("B")) {
               firstSideIsA = false;
            }
            
            for (int position = 0; position < mmW.getNumberOfPositions(); position++) {
               
               ImageProcessor iProc = ip.getProcessor();
               int nrSides = 0;
               if (mmW.getSummaryMetaData().getString("NumberOfSides").equals("2")) {
                  nrSides = 2;
               } else if (mmW.getSummaryMetaData().getString("NumberOfSides").equals("1")) {
                  nrSides = 1;
               } else {
                  throw new SaveTaskException("unsupported number of sides");
               }

               boolean usesChannels = (mmW.getNumberOfChannels()/nrSides) > 1;  // if have channels besides two cameras
               String [] channelDirArray = new String[mmW.getNumberOfChannels()];
               if (usesChannels) {
                  for (int c = 0; c < mmW.getNumberOfChannels(); c++) {
                     //ChNames are always in order, which means
                     String chName = (String)mmW.getSummaryMetaData().getJSONArray("ChNames").get(c);
                     String colorName = chName.substring(chName.indexOf("-")+1);  // matches with AcquisitionPanel naming convention
                     channelDirArray[c] = targetDirectory_ + File.separator + baseName_ + File.separator
                           + (multiPosition ? ("Pos" + position + File.separator) : "")
                           + "SPIM" + (((c % nrSides) == 0) ? (firstSideIsA ? "A" : "B") : (firstSideIsA ? "B" : "A"))
                           + File.separator + colorName;
                  }
               } else {  // two channels are from two views, no need for separate folders for each channel
                  channelDirArray[0] = targetDirectory_ + File.separator + baseName_ + File.separator
                        + (multiPosition ? ("Pos" + position + File.separator) : "")
                        + "SPIM" + (firstSideIsA ? "A" : "B");
                  if (nrSides > 1) {
                     channelDirArray[1] = targetDirectory_ + File.separator + baseName_ + File.separator
                           + (multiPosition ? ("Pos" + position + File.separator) : "")
                           + "SPIM" + (firstSideIsA ? "B" : "A");
                  }
               }

               for (String dir : channelDirArray) {
                  if (new File(dir).exists()) {
                     throw new SaveTaskException("Output directory already exists");
                  }
               }

               for (String dir : channelDirArray) {
                  new File(dir).mkdirs();
               }

               int totalNr = mmW.getNumberOfChannels() * mmW.getNumberOfFrames() * mmW.getNumberOfSlices();
               int counter = 0;

               for (int c = 0; c < mmW.getNumberOfChannels(); c++) {  // for each channel
                  for (int t = 0; t < mmW.getNumberOfFrames(); t++) {  // for each timepoint
                     ImageStack stack = new ImageStack(iProc.getWidth(), iProc.getHeight());
                     for (int i = 0; i < mmW.getNumberOfSlices(); i++) {
                        ImageProcessor iProc2;
                        iProc2 = mmW.getImageProcessor(c, i, t, 1);

                        // optional transformation
                        switch (transformIndex_) {
                        case 1: {
                           iProc2.rotate(90);
                           break;
                        }
                        case 2: {
                           iProc2.rotate(-90);
                           break;
                        }
                        case 3: {
                           iProc2.rotate(((c % 2) == 1) ? 90 : -90);
                           break;
                        }
                        }

                        stack.addSlice(iProc2);
                        counter++;
                        double rate = ((double) counter / (double) totalNr) * 100.0;
                        setProgress((int) Math.round(rate));
                     }
                     ImagePlus ipN = new ImagePlus("tmp", stack);
                     ipN.setCalibration(ip.getCalibration());
                     ij.IJ.save(ipN, channelDirArray[c] + File.separator + "SPIM"
                           + (((c % nrSides) == 0) ? (firstSideIsA ? "A" : "B") : (firstSideIsA ? "B" : "A"))
                           + "-" + t + ".tif");
                  }
               }
            }
            
         } else 
         if (exportFormat_ == 1) {  // Multiview reconstruction
            throw new SaveTaskException("Should import Micro-Manager datasets "
                  + "directly into Fiji Multiview reconstruction as of April 2015.");
         }
      return null;
      }
      
      
      @Override
      public void done() {
         setCursor(null);
         try {
            get();
            setProgress(100);
         } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (!cause.getMessage().equals("Macro canceled")) {
               if (cause instanceof SaveTaskException) {
                  MyDialogUtils.showError(cause, "Data Export Error");
               } else {
                  MyDialogUtils.showError(ex);
               }
            }
         } catch (InterruptedException ex) {
            MyDialogUtils.showError(ex, "Interrupted while exporting data");
         }
      }
   }
   
   /**
    * Since java 1.6 does not seem to have this functionality....
    * @param folder folder to be deleted
    */
   public static void deleteFolder(File folder) {
      File[] files = folder.listFiles();
      if (files != null) { 
         for (File f : files) {
            if (f.isDirectory()) {
               deleteFolder(f);
            } else {
               f.delete();
            }
         }
      }
      folder.delete();
   }

   private void setSaveDestinationDirectory(JTextField rootField) {
      File result = FileDialogs.openDir(null,
              "Please choose a directory root for image data",
              EXPORT_DATA_SET);
      if (result != null) {
         rootField.setText(result.getAbsolutePath());
      }
   }

   public class SaveTaskException extends Exception {

      private static final long serialVersionUID = -8472323699461107823L;
      private Throwable cause;

      public SaveTaskException(String message) {
         super(message);
      }

      public SaveTaskException(Throwable t) {
         super(t.getMessage());
         this.cause = t;
      }

      @Override
      public Throwable getCause() {
         return this.cause;
      }
   }
   
}
