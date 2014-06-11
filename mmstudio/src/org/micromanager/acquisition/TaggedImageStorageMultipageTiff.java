///////////////////////////////////////////////////////////////////////////////
//FILE:          TaggedImageStorageMultipageTiff.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//
// COPYRIGHT:    University of California, San Francisco, 2012
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package org.micromanager.acquisition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.imageDisplay.DisplaySettings;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.*;




public final class TaggedImageStorageMultipageTiff implements TaggedImageStorage {
   
   private JSONObject summaryMetadata_;
   private JSONObject displayAndComments_;
   private boolean newDataSet_;
   private int lastFrameOpenedDataSet_ = -1;
   private String directory_;
   private int numPositions_;
   final public boolean omeTiff_;
   final private boolean separateMetadataFile_;
   private boolean splitByXYPosition_ = true;
   private volatile boolean finished_ = false;
   private boolean expectedImageOrder_ = true;
   private int numChannels_, numSlices_;
   private String omeXML_ = null;
   private OMEMetadata omeMetadata_;
   private int lastFrame_ = 0;
   private boolean fixIndexMap_ = false;
   private final boolean fastStorageMode_;
   private int lastAcquiredPosition_ = 0;
  
   //used for estimating total length of ome xml
   private int totalNumImagePlanes_ = 0;
      
   //map of position indices to objects associated with each
   private HashMap<Integer, FileSet> fileSets_;
   
   //Map of image labels to file 
   private TreeMap<String, MultipageTiffReader> tiffReadersByLabel_;
  
   public TaggedImageStorageMultipageTiff(String dir, Boolean newDataSet, JSONObject summaryMetadata) throws IOException {            
      this(dir, newDataSet, summaryMetadata, MMStudioMainFrame.getInstance().getMetadataFileWithMultipageTiff(),
              MMStudioMainFrame.getInstance().getSeparateFilesForPositionsMPTiff(),
              true);
   }
   
   /*
    * Constructor that doesn't make reference to MMStudioMainFrame so it can be used independently of MM GUI
    */
   public TaggedImageStorageMultipageTiff(String dir, boolean newDataSet, JSONObject summaryMetadata, 
         boolean separateMDFile, boolean separateFilesForPositions, boolean fastStorageMode) throws IOException {
      fastStorageMode_ = fastStorageMode;
      omeTiff_ = true;
      separateMetadataFile_ = separateMDFile;
      splitByXYPosition_ = separateFilesForPositions;

      newDataSet_ = newDataSet;
      directory_ = dir;
      tiffReadersByLabel_ = new TreeMap<String, MultipageTiffReader>(new ImageLabelComparator());
      setSummaryMetadata(summaryMetadata);

      // TODO: throw error if no existing dataset
      if (!newDataSet_) {       
         openExistingDataSet();
      }    
      
   }
   
   private void processSummaryMD() {
      try {
         displayAndComments_ = DisplaySettings.getDisplaySettingsFromSummary(summaryMetadata_);    
      } catch (Exception ex) {
         ReportingUtils.logError(ex, "Problems setting displaySettings from Summery");
      }
      try {
         numPositions_ = MDUtils.getNumPositions(summaryMetadata_);
         if (numPositions_ <= 0) {
            numPositions_ = 1;
         }
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
         numPositions_ = 1;
      }
      if (newDataSet_) {
         try {
            //Estimate of max number of image planes
            numChannels_ = MDUtils.getNumChannels(summaryMetadata_);
            numSlices_ = MDUtils.getNumSlices(summaryMetadata_);
            totalNumImagePlanes_ = numChannels_ * MDUtils.getNumFrames(summaryMetadata_)
                    * numPositions_ * numSlices_;
         } catch (Exception ex) {
            ReportingUtils.logError("Error estimating total number of image planes");
            totalNumImagePlanes_ = 1;
         }
      }
   }
   
   boolean slicesFirst() {
      return ((ImageLabelComparator) tiffReadersByLabel_.comparator()).getSlicesFirst();
   }
   
   boolean timeFirst() {
      return ((ImageLabelComparator) tiffReadersByLabel_.comparator()).getTimeFirst();
   }
   
   public boolean getFixIndexMap() {
      return fixIndexMap_;
   }
   
   public void setFixIndexMap() {
      fixIndexMap_ = true;
   }

   private void openExistingDataSet() {
      //Need to throw error if file not found
      MultipageTiffReader reader = null;
      File dir = new File(directory_);

      ProgressBar progressBar = new ProgressBar("Reading " + directory_, 0, dir.listFiles().length);
      int numRead = 0;
      progressBar.setProgress(numRead);
      progressBar.setVisible(true);
      for (File f : dir.listFiles()) {
         if (f.getName().endsWith(".tif") || f.getName().endsWith(".TIF")) {
            try {
               //this is where fixing dataset code occurs
               reader = new MultipageTiffReader(f);
               Set<String> labels = reader.getIndexKeys();
               for (String label : labels) {
                  tiffReadersByLabel_.put(label, reader);
                  int frameIndex = Integer.parseInt(label.split("_")[2]);
                  lastFrameOpenedDataSet_ = Math.max(frameIndex, lastFrameOpenedDataSet_);
               }
            } catch (IOException ex) {
               ReportingUtils.showError("Couldn't open file: " + f.toString());
            }
         }
         numRead++;
         progressBar.setProgress(numRead);
      }
      progressBar.setVisible(false);
      //reset this static variable to false so the prompt is delivered if a new data set is opened
      reader.fixIndexMapWithoutPrompt_ = false;


      try {
         setSummaryMetadata(reader.getSummaryMetadata(),true);
         numPositions_ = MDUtils.getNumPositions(summaryMetadata_);
         displayAndComments_ = reader.getDisplayAndComments();
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
      }
      progressBar.setProgress(1);
      progressBar.setVisible(false);

   }

   @Override
   public TaggedImage getImage(int channelIndex, int sliceIndex, int frameIndex, int positionIndex) {
      String label = MDUtils.generateLabel(channelIndex, sliceIndex, frameIndex, positionIndex);
      if (!tiffReadersByLabel_.containsKey(label)) {
         return null;
      }

      //DEbugging code for a strange exception found in core log
      try {
         return tiffReadersByLabel_.get(label).readImage(label);
      } catch (NullPointerException e) {
         ReportingUtils.logError("Couldn't find image that TiffReader is supposed to contain");
         if (tiffReadersByLabel_ == null) {
            ReportingUtils.logError("Tiffreadersbylabel is null");
         }
         if (tiffReadersByLabel_.get(label) == null) {
            ReportingUtils.logError("Specific reader is null " + label);
         }
      }
      return null;
   }

   @Override
   public JSONObject getImageTags(int channelIndex, int sliceIndex, int frameIndex, int positionIndex) {
      String label = MDUtils.generateLabel(channelIndex, sliceIndex, frameIndex, positionIndex);
      if (!tiffReadersByLabel_.containsKey(label)) {
         return null;
      }
      return tiffReadersByLabel_.get(label).readImage(label).tags;   
   }

   /*
    * Method that allows overwrting of pixels but not MD or TIFF tags
    * so that low res stitched images can be written tile by tile
    */
   public void overwritePixels(Object pix, int channel, int slice, int frame) throws IOException {
      //asumes only one position
      fileSets_.get(0).overwritePixels(pix, channel, slice, frame); 
   }
   
   @Override
   public void putImage(TaggedImage taggedImage) throws MMException {
      if (!newDataSet_) {
         ReportingUtils.showError("Tried to write image to a finished data set");
         throw new MMException("This ImageFileManager is read-only.");
      }
      int fileSetIndex = 0;
      if (splitByXYPosition_) {
         try {
            fileSetIndex = MDUtils.getPositionIndex(taggedImage.tags);
         } catch (JSONException ex) {
            ReportingUtils.logError(ex);
         }
      }
      String label = MDUtils.getLabel(taggedImage.tags);
      if (fileSets_ == null) {
         try {
            fileSets_ = new HashMap<Integer, FileSet>();
            JavaUtils.createDirectory(directory_);
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
         }
      }
          
      if (omeTiff_) {
         if (omeMetadata_ == null) {
            omeMetadata_ = new OMEMetadata(this);
         }
      }
      
      if (fileSets_.get(fileSetIndex) == null) {
         fileSets_.put(fileSetIndex, new FileSet(taggedImage.tags, this));
      }
      FileSet set = fileSets_.get(fileSetIndex);
      try {
         set.writeImage(taggedImage);
         tiffReadersByLabel_.put(label, set.getCurrentReader());
      } catch (IOException ex) {
        ReportingUtils.showError("problem writing image to file");
      }

         
      int frame;
      try {
         frame = MDUtils.getFrameIndex(taggedImage.tags);
      } catch (JSONException ex) {
         frame = 0;
      }
      lastFrameOpenedDataSet_ = Math.max(frame, lastFrameOpenedDataSet_);
   }

   @Override
   public Set<String> imageKeys() {
      return tiffReadersByLabel_.keySet();
   }

   /**
    * Call this function when no more images are expected
    * Finishes writing the metadata file and closes it.
    * After calling this function, the imagestorage is read-only
    */
   @Override
   public synchronized void finished() {
      if (finished_) {
         return;
      }
      newDataSet_ = false;
      try {
         if (fileSets_ != null) {
              
            int count = 0;
            ProgressBar progressBar = new ProgressBar("Finishing Files", 0, fileSets_.size());
            progressBar.setProgress(count);
            progressBar.setVisible(true);
            for (FileSet p : fileSets_.values()) {
               p.finishAbortedAcqIfNeeded();
            }
            for (FileSet p : fileSets_.values()) {
               p.finished();
               count++;
               progressBar.setProgress(count);
            }
            progressBar.setVisible(false);
         }
      } catch (IOException ex) {
         ReportingUtils.logError(ex);
      }
      finished_ = true;
   }

   @Override
   public boolean isFinished() {
      return !newDataSet_;
   }

   @Override
   public void setSummaryMetadata(JSONObject md) {
      setSummaryMetadata(md, false);
   }
   
   private void setSummaryMetadata(JSONObject md, boolean showProgress) {
      summaryMetadata_ = md;
      if (summaryMetadata_ != null) {
         // try {
            boolean slicesFirst = summaryMetadata_.optBoolean("SlicesFirst", true);
            boolean timeFirst = summaryMetadata_.optBoolean("TimeFirst", false);
            TreeMap<String, MultipageTiffReader> oldImageMap = tiffReadersByLabel_;
            tiffReadersByLabel_ = new TreeMap<String, MultipageTiffReader>(new ImageLabelComparator(slicesFirst, timeFirst));
            if (showProgress) {
               ProgressBar progressBar = new ProgressBar("Building image location map", 0, oldImageMap.keySet().size());
               progressBar.setProgress(0);
               progressBar.setVisible(true);
               int i = 1;
               for (String label : oldImageMap.keySet()) {
                  tiffReadersByLabel_.put(label, oldImageMap.get(label));
                  progressBar.setProgress(i);
                  i++;
               }
               progressBar.setVisible(false);
            } else {
               tiffReadersByLabel_.putAll(oldImageMap);
            }
        //  } catch (JSONException ex) {
        //    ReportingUtils.logError(ex, "Couldn't find SlicesFirst or TimeFirst in summary metadata");
        //  }
         if (summaryMetadata_ != null && summaryMetadata_.length() > 0) {
            processSummaryMD();
         }
      }
   }

   @Override
   public JSONObject getSummaryMetadata() {
      return summaryMetadata_;
   }
   
   @Override
   public JSONObject getDisplayAndComments() {
      return displayAndComments_;
   }

   @Override
   public void setDisplayAndComments(JSONObject settings) {
      displayAndComments_ = settings;
   }
          
   @Override   
   public void writeDisplaySettings() {
      for (MultipageTiffReader r : new HashSet<MultipageTiffReader>(tiffReadersByLabel_.values())) {
         try {
            r.rewriteDisplaySettings(displayAndComments_.getJSONArray("Channels"));
            r.rewriteComments(displayAndComments_.getJSONObject("Comments"));
         } catch (JSONException ex) {
            ReportingUtils.logError("Error writing display settings");
         } catch (IOException ex) {
            ReportingUtils.logError(ex);
         }
      }
   }
   
   /**
    * Disposes of the tagged images in the imagestorage
    */
   @Override
   public void close() {
      for (MultipageTiffReader r : new HashSet<MultipageTiffReader>(tiffReadersByLabel_.values())) {
         try {
            r.close();
         } catch (IOException ex) {
            ReportingUtils.logError(ex);
         }
      }              
   }

   @Override
   public String getDiskLocation() {
      return directory_;
   }

   @Override
   public int lastAcquiredFrame() {
      if (newDataSet_) {
         return lastFrame_;
      } else {
         return lastFrameOpenedDataSet_;
      }
   }

   @Override
   public long getDataSetSize() {
      File dir = new File (directory_);
      LinkedList<File> list = new LinkedList<File>();
      for (File f : dir.listFiles()) {
         if (f.isDirectory()) {
            for (File fi : f.listFiles()) {
               list.add(f);
            }
         } else {
            list.add(f);
         }
      }
      long size = 0;
      for (File f : list) {
         size += f.length();
      }
      return size;
   }
   
   public boolean hasExpectedImageOrder() {
      return false;
//      return expectedImageOrder_;
   }

   //Class encapsulating a single File (or series of files)
   //Default is one file series per xy posititon
   private class FileSet {
      private LinkedList<MultipageTiffWriter> tiffWriters_;
      private FileWriter mdWriter_;
      private String baseFilename_;
      private String currentTiffFilename_;
      private String currentTiffUUID_;;
      private String metadataFileFullPath_;
      private boolean finished_ = false;
      private int ifdCount_ = 0;
      private TaggedImageStorageMultipageTiff mpTiff_;
      int nextExpectedChannel_ = 0, nextExpectedSlice_ = 0, nextExpectedFrame_ = 0;
      int currentFrame_ = 0;

      
      public FileSet(JSONObject firstImageTags, TaggedImageStorageMultipageTiff mpt) {
         tiffWriters_ = new LinkedList<MultipageTiffWriter>();  
         mpTiff_ = mpt;
         
         //get file path and name
         baseFilename_ = createBaseFilename(firstImageTags);
         currentTiffFilename_ = baseFilename_ + (omeTiff_ ? ".ome.tif" : ".tif");
         currentTiffUUID_ = "urn:uuid:" + UUID.randomUUID().toString();
         //make first writer
         tiffWriters_.add(new MultipageTiffWriter(directory_, currentTiffFilename_, summaryMetadata_, mpt,
                 fastStorageMode_, splitByXYPosition_));
   
         try {
            if (separateMetadataFile_) {
               startMetadataFile();
            }
         } catch (JSONException ex) {
            ReportingUtils.showError("Problem with summary metadata");
         }
      }

      public void finished() throws IOException {
         if (finished_) {
            return;
         }
         try {
            //fill in missing tiffdata tags for OME
            for (int p = 0; p <= lastAcquiredPosition_; p++) {
               //set sizeT in case of aborted acq
               omeMetadata_.setNumFrames(p, currentFrame_ + 1);
               omeMetadata_.fillInMissingTiffDatas(lastAcquiredFrame(), p);
            }
         } catch (Exception e) {
            //don't want errors in this code to trip up correct file finishing
            ReportingUtils.logError("Couldn't fill in missing frames in OME");
         }


         if (separateMetadataFile_) {
            try {
               finishMetadataFile();

            } catch (JSONException ex) {
               ReportingUtils.logError("Problem finishing metadata.txt");
            }
         }
         
         try {
            if (omeXML_ == null) {
               omeXML_ = omeMetadata_.toString();
            }
         } catch (Exception e) {
            omeXML_ = " ";
         }
         //only need to finish last one here because previous ones in set are finished as they fill up with images
         tiffWriters_.getLast().finish();
         for (MultipageTiffWriter w : tiffWriters_) {
            w.close(omeXML_);
         }
         finished_ = true;
      }

      public MultipageTiffReader getCurrentReader() {
         return tiffWriters_.getLast().getReader();
      }
      
      public void overwritePixels(Object pixels, int channel, int slice, int frame) throws IOException {
         for (MultipageTiffWriter w : tiffWriters_) {
            if (w.getIndexMap().containsKey(MDUtils.generateLabel(channel, slice, frame, 0))) {
               w.overwritePixels(pixels, channel, slice, frame);
            }
         }
      }
      
      public void writeImage(TaggedImage img) throws IOException {
         //check if current writer is out of space, if so, make a new one
         if (!tiffWriters_.getLast().hasSpaceToWrite(img, omeTiff_ ? estimateOMEMDSize(): 0  )) {
            //write index map here but still need to call close() at end of acq
            tiffWriters_.getLast().finish();          
            
            currentTiffFilename_ = baseFilename_ + "_" + tiffWriters_.size() + (omeTiff_ ? ".ome.tif" : ".tif");
            currentTiffUUID_ = "urn:uuid:" + UUID.randomUUID().toString();
            ifdCount_ = 0;
            tiffWriters_.add(new MultipageTiffWriter(directory_ ,currentTiffFilename_, summaryMetadata_, mpTiff_,
                    fastStorageMode_, splitByXYPosition_));
         }      

         //Add filename to image tags
         try {
            img.tags.put("FileName", currentTiffFilename_);
         } catch (JSONException ex) {
            ReportingUtils.logError("Error adding filename to metadata");
         }

         //write image
         tiffWriters_.getLast().writeImage(img);  
                         
         if (expectedImageOrder_) {
            if (splitByXYPosition_) {
               checkForExpectedImageOrder(img.tags);
            } else {
               expectedImageOrder_ = false;
            }
         }

         //write metadata
         if (omeTiff_) {
            try {
               //Check if missing planes need to be added OME metadata
               int frame = MDUtils.getFrameIndex(img.tags);
               int position;
               try {
                  position = MDUtils.getPositionIndex(img.tags);
               } catch (Exception e) {
                  position = 0;
               }
               if (frame > currentFrame_) {
                  //check previous frame for missing IFD's in OME metadata
                  omeMetadata_.fillInMissingTiffDatas(currentFrame_, position);
               }
               //reset in case acquisitin order is position then time and all files not split by position
               currentFrame_ = frame;
               
               omeMetadata_.addImageTagsToOME(img.tags, ifdCount_, baseFilename_, currentTiffFilename_, currentTiffUUID_);
            } catch (Exception ex) {
               ReportingUtils.logError("Problem writing OME metadata");
            }
         }
         
         try {
            int frame = MDUtils.getFrameIndex(img.tags);
            lastFrame_ = Math.max(frame, lastFrame_);
         } catch (JSONException ex) {
            ReportingUtils.showError("Couldn't find frame index in image tags");
         }   
         try {
            int pos = MDUtils.getPositionIndex(img.tags);
            lastAcquiredPosition_ = Math.max(pos, lastAcquiredPosition_);
         } catch (JSONException ex) {
            ReportingUtils.showError("Couldn't find position index in image tags");
         }  
         
         
         try {
            if (separateMetadataFile_) {
               writeToMetadataFile(img.tags);
            }
         } catch (JSONException ex) {
            ReportingUtils.logError("Problem with image metadata");
         }
         ifdCount_++;
      }

      private int estimateOMEMDSize() {
         return totalNumImagePlanes_ * omeMetadata_.getOMEMetadataImageLength()
                 + numPositions_ * omeMetadata_.getOMEMetadataBaseLenght();
      }

      private void writeToMetadataFile(JSONObject md) throws JSONException {
         try {
            mdWriter_.write(",\r\n\"FrameKey-" + MDUtils.getFrameIndex(md)
                    + "-" + MDUtils.getChannelIndex(md) + "-" + MDUtils.getSliceIndex(md) + "\": ");
            mdWriter_.write(md.toString(2));
         } catch (IOException ex) {
            ReportingUtils.logError("Problem writing to metadata.txt file");
         }
      }

      private void startMetadataFile() throws JSONException {
            metadataFileFullPath_ = directory_ + "/" + baseFilename_ + "_metadata.txt";
            try {
               mdWriter_ = new FileWriter(metadataFileFullPath_);
               mdWriter_.write("{" + "\r\n");
               mdWriter_.write("\"Summary\": ");
               mdWriter_.write(summaryMetadata_.toString(2));
            } catch (IOException ex) {
               ReportingUtils.logError("Problem creating metadata.txt file");
            }
      }

      private void finishMetadataFile() throws JSONException {
         try {
            mdWriter_.write("\r\n}\r\n");
            mdWriter_.close();
         } catch (IOException ex) {
            ReportingUtils.logError("Problem creating metadata.txt file");
         }
      }

      private String createBaseFilename(JSONObject firstImageTags) {
         String baseFilename = "";
         try {
            String prefix = summaryMetadata_.getString("Prefix");
            if (prefix.length() == 0) {
               baseFilename = "MMStack";
            } else {
               baseFilename = prefix + "_MMStack";
            }
         } catch (JSONException ex) {
            ReportingUtils.logError("Can't find Prefix in summary metadata");
            baseFilename = "MMStack";
         }

         if (numPositions_ > 1 && splitByXYPosition_) {
            String positionName;
            try {
               positionName = MDUtils.getPositionName(firstImageTags);
            } catch (JSONException ex) {
               ReportingUtils.logError("Couldn't find position name in image metadata");
               try {
                  positionName = "pos" + MDUtils.getPositionIndex(firstImageTags);
               } catch (JSONException ex1) {
                  positionName = "pos" + 0;
                  ReportingUtils.showError("Couldnt find position index in image metadata");
               }
            }
            baseFilename += "_" + positionName;
         }
         return baseFilename;
      }

      /**
       * Generate all expected labels for the last frame, and write dummy images for ones 
       * that weren't written. Modify ImageJ and OME max number of frames as appropriate.
       * This method only works if xy positions are split across separate files
       */
      private void finishAbortedAcqIfNeeded() {
         if (expectedImageOrder_ && splitByXYPosition_ && !timeFirst()) {
            try {
               //One position may be on the next frame compared to others. Complete each position
               //with blank images to fill this frame
               completeFrameWithBlankImages(lastAcquiredFrame());
            } catch (Exception e) {
               ReportingUtils.logError("Problem finishing aborted acq with blank images");
            }
         }
      }

      /*
       * Completes the current time point of an aborted acquisition with blank images, 
       * so that it can be opened correctly by ImageJ/BioForamts
       */
      private void completeFrameWithBlankImages(int frame) throws JSONException, MMScriptException {
         
         int numFrames = MDUtils.getNumFrames(summaryMetadata_);
         int numSlices = MDUtils.getNumSlices(summaryMetadata_);
         int numChannels = MDUtils.getNumChannels(summaryMetadata_);
         if (numFrames > frame + 1 ) {
            TreeSet<String> writtenImages = new TreeSet<String>();
            for (MultipageTiffWriter w : tiffWriters_) {
               writtenImages.addAll(w.getIndexMap().keySet());
               w.setAbortedNumFrames(frame + 1);
            }
            int positionIndex = MDUtils.getIndices(writtenImages.first())[3];
            if (omeTiff_) {
               omeMetadata_.setNumFrames(positionIndex, frame + 1);
            }
            TreeSet<String> lastFrameLabels = new TreeSet<String>();
            for (int c = 0; c < numChannels; c++) {
               for (int z = 0; z < numSlices; z++) {
                  lastFrameLabels.add(MDUtils.generateLabel(c, z, frame, positionIndex));
               }
            }
            lastFrameLabels.removeAll(writtenImages);
            try {
               for (String label : lastFrameLabels) {
                  tiffWriters_.getLast().writeBlankImage(label);
                  if (omeTiff_) {
                     JSONObject dummyTags = new JSONObject();
                     int channel = Integer.parseInt(label.split("_")[0]);
                     int slice = Integer.parseInt(label.split("_")[1]);
                     MDUtils.setChannelIndex(dummyTags, channel);
                     MDUtils.setFrameIndex(dummyTags, frame);
                     MDUtils.setSliceIndex(dummyTags, slice);
                     omeMetadata_.addImageTagsToOME(dummyTags, ifdCount_, baseFilename_, currentTiffFilename_, currentTiffUUID_);
                  }
               }
            } catch (IOException ex) {
               ReportingUtils.logError("problem writing dummy image");
            }
         }
      }
      
      void checkForExpectedImageOrder(JSONObject tags) {
         try {
            //Determine next expected indices
            int channel = MDUtils.getChannelIndex(tags), frame = MDUtils.getFrameIndex(tags),
                    slice = MDUtils.getSliceIndex(tags);
            if (slice != nextExpectedSlice_ || channel != nextExpectedChannel_ ||
                    frame != nextExpectedFrame_) {
               expectedImageOrder_ = false;
            }
            //Figure out next expected indices
            if (slicesFirst()) {
               nextExpectedSlice_ = slice + 1;
               if (nextExpectedSlice_ == numSlices_) {
                  nextExpectedSlice_ = 0;
                  nextExpectedChannel_ = channel + 1;
                  if (nextExpectedChannel_ == numChannels_) {
                     nextExpectedChannel_ = 0;
                     nextExpectedFrame_ = frame + 1;
                  }
               }
            } else {
               nextExpectedChannel_ = channel + 1;
               if (nextExpectedChannel_ == numChannels_) {
                  nextExpectedChannel_ = 0;
                  nextExpectedSlice_ = slice + 1;
                  if (nextExpectedSlice_ == numSlices_) {
                     nextExpectedSlice_ = 0;
                     nextExpectedFrame_ = frame + 1;
                  }
               }
            }
         } catch (JSONException ex) {
            ReportingUtils.logError("Couldnt find channel, slice, or frame index in Image tags");
            expectedImageOrder_ = false;
         }
      }
 
   }    
}
