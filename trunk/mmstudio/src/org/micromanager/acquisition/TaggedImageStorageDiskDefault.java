/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import org.micromanager.api.TaggedImageStorage;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.JavaUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.TextUtils;

/**
 *
 * @author arthur
 */
public class TaggedImageStorageDiskDefault implements TaggedImageStorage {

   private final String dir_;
   private boolean firstElement_;
   private HashMap<String,Writer> metadataStreams_;
   private boolean newDataSet_;
   private JSONObject summaryMetadata_;
   private HashMap<String,String> filenameTable_;
   private HashMap<String, JSONObject> metadataTable_ = null;
   private JSONObject displaySettings_;
   
   public TaggedImageStorageDiskDefault(String dir) {
      this(dir, false, null);
   }

   public TaggedImageStorageDiskDefault(String dir, Boolean newDataSet,
           JSONObject summaryMetadata) {
      summaryMetadata_ = summaryMetadata;
      dir_ = dir;
      newDataSet_ = newDataSet;
      filenameTable_ = new HashMap<String,String>();
      metadataStreams_ = new HashMap<String,Writer>();
      metadataTable_ = new HashMap<String, JSONObject>();
      displaySettings_ = new JSONObject();
      
      try {
         if (!newDataSet_) {
            openExistingDataSet();
         }
      } catch (Exception e) {
         ReportingUtils.logError(e);
      }
   }

   private String getPosition(TaggedImage taggedImg) {
      return getPosition(taggedImg.tags);
   }
   
   private String getPosition(JSONObject tags) {
      try {
         String pos =  MDUtils.getPositionName(tags);
         if (pos == null)
            return "";
         else
            return pos;
      } catch (Exception e) {
         return "";
      }
   }

   public String putImage(TaggedImage taggedImg) throws MMException {
      try {
         if (!newDataSet_) {
            throw new MMException("This ImageFileManager is read-only.");
         }
         if (!metadataStreams_.containsKey(getPosition(taggedImg))) {
            try {
               openNewDataSet(taggedImg);
            } catch (Exception ex) {
               ReportingUtils.logError(ex);
            }
         }
         JSONObject md = taggedImg.tags;
         Object img = taggedImg.pix;
         String tiffFileName = createFileName(md);
         MDUtils.setFileName(md, tiffFileName);
         String posName = "";
         String fileName = tiffFileName;
         try {
            posName = getPosition(md);
            if (posName != null && posName.length() > 0 && !posName.contentEquals("null")) {
               JavaUtils.createDirectory(dir_ + "/" + posName);
               fileName = posName + "/" + tiffFileName;
            } else {
               fileName = tiffFileName;
            }
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
         }

         saveImageFile(img, md, dir_, fileName);
         writeFrameMetadata(md);
         String label = MDUtils.getLabel(md);
         filenameTable_.put(label, fileName);
         return MDUtils.getLabel(md);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
         return null;
      }
   }

   public TaggedImage getImage(int channel, int slice, int frame, int position) {
      String label = MDUtils.generateLabel(channel, slice, frame, position);
      ImagePlus imp = new Opener().openImage(dir_ + "/" + filenameTable_.get(label));
      if (imp != null) {
         try {
            ImageProcessor proc = imp.getProcessor();
            JSONObject md;
            try {
               md = new JSONObject((String) imp.getProperty("Info"));
            } catch (Exception e) {
               md = metadataTable_.get(label);
            }
            String pixelType = MDUtils.getPixelType(md);
            Object img;
            if (pixelType.contentEquals("GRAY8") || pixelType.contentEquals("GRAY16")) {
               img = proc.getPixels();
            } else if (pixelType.contentEquals("RGB32")) {
               img = proc.getPixels();
               img = ImageUtils.convertRGB32IntToBytes((int[]) img);
            } else if (pixelType.contentEquals("RGB64")) {
               ImageStack stack = ((CompositeImage) imp).getStack();
               short[] r = (short[]) stack.getProcessor(1).getPixels();
               short[] g = (short[]) stack.getProcessor(2).getPixels();
               short[] b = (short[]) stack.getProcessor(3).getPixels();
               short[][] planes = {r, g, b};
               img = ImageUtils.getRGB64PixelsFromColorPlanes(planes);
            } else {
               return null;
            }
            TaggedImage taggedImg = new TaggedImage(img, md);
            return taggedImg;
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
            return null;
         }
      } else {
         return null;
      }
   }

   private String createFileName(JSONObject md) {
      try {
         int frame;
         int slice;
         String channel;
         try {
            frame = MDUtils.getFrameIndex(md);
         } catch (Exception e) {
            frame = 0;
         }
         try {
            channel = MDUtils.getChannelName(md);
         } catch (Exception e) {
            channel = "";
         }
         try {
            slice = MDUtils.getSliceIndex(md);
         } catch (Exception e) {
            slice = 0;
         }


         return String.format("img_%09d_%s_%03d.tif",
                 frame,
                 channel,
                 slice);
      } catch (Exception e) {
         ReportingUtils.logError(e);
         return "";
      }
   }

   private void writeFrameMetadata(JSONObject md) {
      try {    
         String title = "FrameKey-" + MDUtils.getFrameIndex(md) + "-" + MDUtils.getChannelIndex(md) + "-" + MDUtils.getSliceIndex(md);
         String pos = getPosition(md);
         writeMetadata(pos, md, title);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   private void writeMetadata(String pos, JSONObject md, String title) {
      try {
         Writer metadataStream = metadataStreams_.get(pos);
         if (!firstElement_) {
            metadataStream.write(",\r\n");
         }
         metadataStream.write("\"" + title + "\": ");
         metadataStream.write(md.toString(2));
         metadataStream.flush();
         firstElement_ = false;
      } catch (Exception e) {
         ReportingUtils.logError(e);
      }
   }

   private void saveImageFile(Object img, JSONObject md, String path, String tiffFileName) {
      ImagePlus imp;
      try {
         ImageProcessor ip = null;
         int width = MDUtils.getWidth(md);
         int height = MDUtils.getHeight(md);
         String pixelType = MDUtils.getPixelType(md);
         if (pixelType.equals("GRAY8")) {
            ip = new ByteProcessor(width, height);
            ip.setPixels((byte[]) img);
            saveImageProcessor(ip, md, path, tiffFileName);
         } else if (pixelType.equals("GRAY16")) {
            ip = new ShortProcessor(width, height);
            ip.setPixels((short[]) img);
            saveImageProcessor(ip, md, path, tiffFileName);
         } else if (pixelType.equals("RGB32")) {
            byte[][] planes = ImageUtils.getColorPlanesFromRGB32((byte []) img);
            ColorProcessor cp = new ColorProcessor(width, height);
            cp.setRGB(planes[0],planes[1],planes[2]);
            saveImageProcessor(cp, md, path, tiffFileName);
         } else if (pixelType.equals("RGB64")) {
            short[][] planes = ImageUtils.getColorPlanesFromRGB64((short []) img);
            ImageStack stack = new ImageStack(width, height);
				stack.addSlice("Red", planes[0]);
				stack.addSlice("Green", planes[1]);
				stack.addSlice("Blue", planes[2]);
        		imp = new ImagePlus(path + "/" + tiffFileName, stack);
        		imp.setDimensions(3, 1, 1);
            imp = new CompositeImage(imp, CompositeImage.COLOR);
            saveImagePlus(imp, md, path, tiffFileName);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }


   private void saveImageProcessor(ImageProcessor ip, JSONObject md, String path, String tiffFileName) {
      if (ip != null) {
         ImagePlus imp = new ImagePlus(path + "/" + tiffFileName, ip);
         saveImagePlus(imp, md, path, tiffFileName);
      }
   }


   public void saveImagePlus(ImagePlus imp, JSONObject md, String path, String tiffFileName) {
      try {
         imp.setProperty("Info", md.toString(2));
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
      }
      FileSaver fs = new FileSaver(imp);
      fs.saveAsTiff(path + "/" + tiffFileName);
   }

   private void openNewDataSet(TaggedImage firstImage) throws Exception, IOException {
      String time = firstImage.tags.getString("Time");
      String pos = getPosition(firstImage);
      if (pos == null)
         pos = "";
      JavaUtils.createDirectory(dir_ + "/" + pos);
      firstElement_ = true;
      Writer metadataStream = new BufferedWriter(new FileWriter(dir_ + "/" + pos + "/metadata.txt"));
      metadataStreams_.put(pos, metadataStream);
      metadataStream.write("{" + "\r\n");
      JSONObject summaryMetadata = getSummaryMetadata();
      summaryMetadata.put("Time", time);
      summaryMetadata.put("Date", time.split(" ")[0]);
      summaryMetadata.put("PositionIndex", MDUtils.getPositionIndex(firstImage.tags));
      writeMetadata(pos, summaryMetadata, "Summary");
   }

   public void finished() {
      closeMetadataStreams();
      newDataSet_ = false;
   }

   private void closeMetadataStreams() {
      if (newDataSet_) {
         try {
            for (Writer metadataStream:metadataStreams_.values()) {
               metadataStream.write("\r\n}\r\n");
               metadataStream.close();
            }
         } catch (IOException ex) {
            ReportingUtils.logError(ex);
         }
      }
   }

   private void openExistingDataSet() {
      File metadataFile = new File(dir_ + "/metadata.txt");
      ArrayList<String> positions = new ArrayList<String>();
      if (metadataFile.exists()) {
         positions.add("");
      } else {
         for (File f : new File(dir_).listFiles()) {
            if (f.isDirectory()) {
               positions.add(f.getName());
            }
         }
      }

      for (int positionIndex = 0; positionIndex < positions.size(); ++positionIndex) {
         String position = positions.get(positionIndex);
         JSONObject data = readJsonMetadata(position);
         if (data != null) {
            try {
               summaryMetadata_ = jsonToMetadata(data.getJSONObject("Summary"));
               int metadataVersion = summaryMetadata_.getInt("MetadataVersion");
               for (String key:makeJsonIterableKeys(data)) {
                  JSONObject chunk = data.getJSONObject(key);
                  if (key.startsWith("FrameKey")) {
                     JSONObject md = jsonToMetadata(chunk);
                     try {
                        if (!md.has("ChannelIndex"))
                           md.put("ChannelIndex", getChannelIndex(MDUtils.getChannelName(md)));
                        if (!md.has("PositionIndex"))
                           md.put("PositionIndex", positionIndex);
                        if (!md.has("PixelType") && !md.has("IJType")) {
                           md.put("PixelType", MDUtils.getPixelType(summaryMetadata_));
                        }
                        String fileName = MDUtils.getFileName(md);
                        if (position.length() > 0)
                           fileName = position + "/" + fileName;
                        filenameTable_.put(MDUtils.getLabel(md), fileName);
                        if (metadataVersion < 10)
                           metadataTable_.put(MDUtils.getLabel(md), md);
                     } catch (Exception ex) {
                        ReportingUtils.showError(ex);
                     }
                  }
               }
            } catch (JSONException ex) {
               ReportingUtils.logError(ex);
            }
         }
         try {
            summaryMetadata_.put("Positions", positions.size());
         } catch (JSONException ex) {
            ReportingUtils.logError(ex);
         }
      }
      readDisplaySettings();
   }

   private int getChannelIndex(String channelName) {
      try {
         JSONArray channelNames;
         Object tmp = getSummaryMetadata().get("ChNames");
         if (tmp instanceof String) {
            channelNames = new JSONArray((String) tmp);
         } else {
            channelNames = (JSONArray) tmp;
         }
         for (int i=0;i<channelNames.length();++i) {
            if (channelNames.getString(i).contentEquals(channelName))
               return i;
         }
         return 0;
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
         return 0;
      }
   }

   private Iterable<String> makeJsonIterableKeys(final JSONObject data) {
      return new Iterable<String>() {
            public Iterator<String> iterator() {
               return data.keys();
            }
         };
   }

   private JSONObject jsonToMetadata(final JSONObject data) {
      JSONObject md = new JSONObject();
      try {
         Iterable<String> keys = makeJsonIterableKeys(data);
         for (String key:keys) {
            md.put((String) key, (String) data.getString(key));
         }
      } catch (JSONException ex) {
         ReportingUtils.showError(ex);
      }

      return md;

   }

   private JSONObject readJsonMetadata(String pos) {
      try {
         String fileStr;
         fileStr = TextUtils.readTextFile(dir_ + "/" + pos + "/metadata.txt");
         try {
            return new JSONObject(fileStr);
         } catch (JSONException ex) {
            return new JSONObject(fileStr.concat("}"));
         }
      } catch (IOException ex) {
         ReportingUtils.showError(ex, "Unable to open metadata.txt");
         return null;
      } catch (Exception ex) {
         ReportingUtils.showError(ex, "Unable to read metadata.txt");
         return null;
      }
   }

   /**
    * @return the summaryMetadata_
    */
   public JSONObject getSummaryMetadata() {
      return summaryMetadata_;
   }

   /**
    * @param summaryMetadata the summaryMetadata to set
    */
   public void setSummaryMetadata(JSONObject summaryMetadata) {
      this.summaryMetadata_ = summaryMetadata;
   }

   public void setDisplayAndComments(JSONObject settings) {
      boolean newSettings
               = (displaySettings_ == null)
              || displaySettings_.isNull("Channels")
              || displaySettings_.isNull("Comments");
      displaySettings_ = settings;
      if (newSettings) {
         this.writeDisplaySettings();
      }
   }

   public JSONObject getDisplayAndComments() {
      return displaySettings_;
   }

   private void writeDisplaySettings() {
      if (displaySettings_ == null)
         return;
      if (! new File(dir_).exists()) {
         try {
            JavaUtils.createDirectory(dir_);
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
         }
      }
      File displayFile = new File(dir_ + "/" + "display_and_comments.txt");
      try {
         Writer displayFileWriter = new FileWriter(displayFile);
         displayFileWriter.append(displaySettings_.toString(2));
         displayFileWriter.close();
      } catch (Exception e) {
         ReportingUtils.showError(e);
      }
   }

   private void readDisplaySettings() {
      displaySettings_ = null;
      String path = dir_ + "/" + "display_and_comments.txt";
      try {
         String jsonText = JavaUtils.readTextFile(path);
         if (jsonText != null) {
            JSONObject tmp = new JSONObject(jsonText);
            JSONArray channels = (JSONArray) tmp.get("Channels");
            if (channels != null)
               displaySettings_ = tmp;
            else
               displaySettings_.put("Comments", tmp.get("Comments"));
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   public void close() {
      try {
         writeDisplaySettings();
      } catch (Exception e) {
         ReportingUtils.logError(e);
      }
      /*
      this.summaryMetadata_ = null;
      this.metadataStreams_ = null;
      this.metadataTable_ = null;
      this.filenameTable_ = null;
      this.displaySettings_ = null;
       */
   }

   public String getDiskLocation() {
      return dir_;
   }

}
