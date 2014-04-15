/*
 * Master stitched window to display real time stitched images, allow navigating of XY more easily
 */
import ij.CompositeImage;
import ij.IJ;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.acquisition.AcquisitionEngine;

import org.micromanager.acquisition.VirtualAcquisitionDisplay;
import org.micromanager.api.ImageCache;
import org.micromanager.api.ImageCacheListener;


import org.micromanager.internalinterfaces.DisplayControls;
import org.micromanager.utils.*;

public class DisplayPlus implements ImageCacheListener {

   private static final Color TRANSPARENT_BLUE = new Color(0, 0, 255, 100);
   private static final Color TRANSPARENT_MAGENTA = new Color(255, 0, 255, 100);
   //VirtualAcquisitionDisplay on top of which this display is built
   private VirtualAcquisitionDisplay vad_;
   private DynamicStitchingImageStorage storage_;
   private ImageCanvas canvas_;
   private Controls controls_;
   private CustomAcqEngine eng_;
   private JSpinner gridXSpinner_, gridYSpinner_;
   private Point clickStart_;
   private Point gridStart_, mouseDragStartPoint_;
   private boolean suspendUpdates_ = false;
   private int mouseRowIndex_ = -1, mouseColIndex_ = -1;
   private ArrayList<Point> selectedPositions_ = new ArrayList<Point>();
   private ScrollbarWithLabel tSelector_;
   private boolean gotoMode_ = false, newGridMode_ = false,
           zoomMode_ = false, zoomAreaSelectMode_ = false;
   private ZoomableVirtualStack zoomableStack_;
   private final boolean exploreMode_;

   public DisplayPlus(final ImageCache stitchedCache, CustomAcqEngine eng, JSONObject summaryMD, 
           DynamicStitchingImageStorage storage, boolean exploreMode) {
      exploreMode_ = exploreMode;
      //Set parameters for tile dimensions, num rows and columns, overlap, and image dimensions
      eng_ = eng;

      String name = "Untitled";
      try {
         String pre = summaryMD.getString("Prefix");
         if (pre != null && pre.length() > 0) {
            name = pre;
         }
      } catch (Exception e) {
      }
      
      storage_ = storage;
      vad_ = new VirtualAcquisitionDisplay(stitchedCache, null, name);
      controls_ = new Controls();

      //Add in custom controls
      try {
         JavaUtils.setRestrictedFieldValue(vad_, VirtualAcquisitionDisplay.class, "controls_", controls_);
      } catch (NoSuchFieldException ex) {
         ReportingUtils.showError("Couldn't create display controls");
      }

      //add in customized zoomable acquisition virtual stack
      try {
         int nSlices = MDUtils.getNumChannels(summaryMD) * MDUtils.getNumFrames(summaryMD) * MDUtils.getNumSlices(summaryMD);
         zoomableStack_ = new ZoomableVirtualStack(MDUtils.getIJType(summaryMD), stitchedCache, nSlices, vad_, storage);
         vad_.show(zoomableStack_);
      } catch (Exception e) {
         ReportingUtils.showError("Problem with initialization due to missing summary metadata tags");
         return;
      }

      try {
         //get reference to tSelector so it can be updated without showing latest images         
         tSelector_ = (ScrollbarWithLabel) JavaUtils.getRestrictedFieldValue(
                 vad_, VirtualAcquisitionDisplay.class, "tSelector_");
      } catch (NoSuchFieldException ex) {
         ReportingUtils.showError("Couldnt get refernce to t Selctor");
      }
      
      canvas_ = vad_.getImagePlus().getCanvas();
      
      //Zoom to 100%
      canvas_.unzoom();

      setupMouseListeners();

      IJ.setTool(Toolbar.SPARE6);

      stitchedCache.addImageCacheListener(this);
   }
//
//   public void readGridInfoFromPositionList(JSONArray posList) {
//      try {
//         //get grid parameters
//         for (int i = 0; i < posList.length(); i++) {
//            long colInd = posList.getJSONObject(i).getLong("GridColumnIndex");
//            long rowInd = posList.getJSONObject(i).getLong("GridRowIndex");
//            if (colInd >= numCols_) {
//               numCols_ = (int) (colInd + 1);
//            }
//            if (rowInd >= numRows_) {
//               numRows_ = (int) (rowInd + 1);
//            }
//
//         }
//      } catch (Exception e) {
//         ReportingUtils.showError("Couldnt get grid info");
//      }
//      fullResHeight_ = numRows_ * tileHeight_ - (numRows_ - 1) * yOverlap_;
//      fullResWidth_ = numCols_ * tileWidth_ - (numCols_ - 1) * xOverlap_;
//   }

   private Roi makeROIRect(int rowIndex, int colIndex) {
//      int y, x;
//      int width = tileWidth_ - xOverlap_ / 2, height = tileHeight_ - yOverlap_ / 2;
//      int canvasWidth = (int) (canvas_.getWidth() / canvas_.getMagnification()),
//              canvasHeight = (int) (canvas_.getHeight() / canvas_.getMagnification());
//
//      if (rowIndex == 0) {
//         y = 0;
//      } else if (rowIndex == numRows_ - 1) {
//         y = canvasHeight - (tileHeight_ - yOverlap_ / 2);
//      } else {
//         height = tileHeight_ - yOverlap_;
//         y = (tileHeight_ - yOverlap_ / 2) + (rowIndex - 1) * (tileHeight_ - yOverlap_);
//      }
//      if (colIndex == 0) {
//         x = 0;
//      } else if (colIndex == numCols_ - 1) {
//         x = canvasWidth - (tileWidth_ - xOverlap_ / 2);
//      } else {
//         width = tileWidth_ - xOverlap_;
//         x = (tileWidth_ - xOverlap_ / 2) + (colIndex - 1) * (tileWidth_ - xOverlap_);
//      }
//      return new Roi(x, y, width, height);
      
      return null;
   }
   
   private TextRoi makeTextRoi(int rowIndex, int colIndex, int offset) {
//      int y, x;
//      int width = tileWidth_ - xOverlap_ / 2, height = tileHeight_ - yOverlap_ / 2;
//      ImageCanvas canvas = canvas_;
//      int canvasWidth = (int) (canvas.getWidth() / canvas.getMagnification()),
//              canvasHeight = (int) (canvas.getHeight() / canvas.getMagnification());
//
//      if (rowIndex == 0) {
//         y = 0;
//      } else if (rowIndex == numRows_ - 1) {
//         y = canvasHeight - (tileHeight_ - yOverlap_ / 2);
//      } else {
//         height = tileHeight_ - yOverlap_;
//         y = (tileHeight_ - yOverlap_ / 2) + (rowIndex - 1) * (tileHeight_ - yOverlap_);
//      }
//      if (colIndex == 0) {
//         x = 0;
//      } else if (colIndex == numCols_ - 1) {
//         x = canvasWidth - (tileWidth_ - xOverlap_ / 2);
//      } else {
//         width = tileWidth_ - xOverlap_;
//         x = (tileWidth_ - xOverlap_ / 2) + (colIndex - 1) * (tileWidth_ - xOverlap_);
//      }
//      double mag = canvas_.getMagnification();
//      TextRoi tr = new TextRoi(x + width / 2, y + height / 2, "Offset: " + offset + " um");
//      tr.setCurrentFont(tr.getCurrentFont().deriveFont((float) (tr.getCurrentFont().getSize() / mag)));
//      tr.setJustification(TextRoi.CENTER);
//      tr.setLocation(x + width / 2, y + height / 2 - (height / 40 / mag));
//      return tr;
      return null;
   }

   private void drawDepthListOverlay() {
      Overlay overlay = new Overlay();
      if (mouseRowIndex_ != -1 && mouseColIndex_ != -1) {
         Roi rect = makeROIRect(mouseRowIndex_, mouseColIndex_);
         rect.setFillColor(TRANSPARENT_BLUE);
         overlay.add(rect);
      }

      if (!selectedPositions_.isEmpty()) {
         for (Point p : selectedPositions_) {
            Roi selectionRect = makeROIRect(p.x, p.y);
            selectionRect.setStrokeWidth(10f);
            overlay.add(selectionRect);
         }
      }

      canvas_.setOverlay(overlay);
   }
   
   private void drawZoomIndicatorOverlay() {
      //draw zoom indicator
      Overlay overlay = new Overlay();
      Point zoomPos = zoomableStack_.getZoomPosition();      
      int outerWidth = 100;
      int outerHeight = (int) ((double) storage_.getFullResHeight() / (double) storage_.getFullResWidth() * outerWidth);
      //draw outer rectangle representing full image
      Roi outerRect = new Roi(10, 10, outerWidth, outerHeight);
      outerRect.setStrokeColor(new Color(255, 0, 255)); //magenta
      overlay.add(outerRect);
      int innerX = (int) Math.round(( (double) outerWidth / (double) storage_.getFullResWidth() ) * zoomPos.x);
      int innerY = (int) Math.round(( (double) outerHeight / (double) storage_.getFullResHeight() ) * zoomPos.y);
      int innerWidth = (int) Math.round(((double) outerWidth / (double) storage_.getFullResWidth() ) * 
              (storage_.getFullResWidth() / storage_.getDSFactor()));
      int innerHeight = (int) Math.round(((double) outerHeight / (double) storage_.getFullResHeight() ) * 
              (storage_.getFullResHeight() / storage_.getDSFactor()));
      Roi innerRect = new Roi(10+innerX,10+innerY,innerWidth,innerHeight );
      innerRect.setStrokeColor(new Color(255, 0, 255)); 
      overlay.add(innerRect);
      canvas_.setOverlay(overlay);
   }

   private void zoomIn(Point p) {
      zoomMode_ = true;
      zoomAreaSelectMode_ = false;
      //This assumes 100% display of tiled image
      zoomableStack_.activateZoomMode(p.x, p.y);
      vad_.getHyperImage().setOverlay(null);
      redrawPixels();
      drawZoomIndicatorOverlay();   
   }
   
   private void zoomOut() {
      zoomMode_ = false;
      zoomableStack_.activateFullImageMode();
      redrawPixels();
      canvas_.setOverlay(null);
   }

   private void addExploreOverlay(int x, int y, int width, int height, Color color) {
      //need to convert from canvas coordinates to pixel coordinates
      final ImageCanvas canvas = vad_.getImagePlus().getCanvas();
      Roi rect = new Roi(x, y, width, height);
      rect.setFillColor(color);
      Overlay overlay = new Overlay();
      overlay.add(rect);
      //      Arrow arrow = new Arrow(40, 10, 10, 10);
//      overlay.add(arrow);
      canvas.setOverlay(overlay);
   }
   

   //TODO account for overlap?
    private void highlightTiles(int row1, int row2, int col1, int col2, Color color) {
      //need to convert from canvas coordinates to pixel coordinates
      final ImageCanvas canvas = vad_.getImagePlus().getCanvas();
       double tileHeight = storage_.getTileHeight() / (double) storage_.getDSFactor(),
               tileWidth = storage_.getTileWidth() / (double) storage_.getDSFactor();
       int x = (int) Math.round(col1 * tileWidth);
       int y = (int) Math.round(row1 * tileHeight);
       int width = (int) Math.round(tileWidth * (col2 - col1 + 1));
       int height = (int) Math.round(tileHeight * (row2 - row1 + 1));
      Roi rect = new Roi(x, y, width, height);
      rect.setFillColor(color);
      Overlay overlay = new Overlay();
      overlay.add(rect);
      canvas.setOverlay(overlay);
   }
   
   private Point getExploreIndicesFromLoResPixel(Point p) {
      //first get tile indices
      int row = storage_.tileIndicesFromLoResPixel(p.x, p.y).x;
      int col = storage_.tileIndicesFromLoResPixel(p.x, p.y).y;
      
      //now check if its on the edges
      if (row == 0 && p.y < storage_.getTileHeight() / storage_.getDSFactor() / 3) {
         row--;
      } else if (row == storage_.getNumRows() - 1 && p.y > zoomableStack_.getHeight() 
              - storage_.getTileHeight() / storage_.getDSFactor() / 3 ) {
         row++;
      }
      if (col == 0 && p.x < storage_.getTileWidth() / storage_.getDSFactor() / 3) {
         col--;
      } else if (col == storage_.getNumCols()  - 1 && p.x > zoomableStack_.getWidth() 
              - storage_.getTileWidth() / storage_.getDSFactor() / 3 ) {
         col++;
      }
      return new Point(row,col);
   }
   
   private void setupMouseListeners() {  
      //remove channel switching scroll wheel listener
      vad_.getImagePlus().getWindow().removeMouseWheelListener(
              vad_.getImagePlus().getWindow().getMouseWheelListeners()[0]);
      //remove canvas mouse listener and virtualacquisitiondisplay as mouse listener
      vad_.getImagePlus().getCanvas().removeMouseListener(
              vad_.getImagePlus().getCanvas().getMouseListeners()[0]);
      vad_.getImagePlus().getCanvas().removeMouseListener(
              vad_.getImagePlus().getCanvas().getMouseListeners()[0]);
      
      canvas_.addMouseMotionListener(new MouseMotionListener() {

         @Override
         public void mouseDragged(MouseEvent e) {               
            Point currentPoint = e.getPoint();
            if (newGridMode_) {
               int dx = (int) ((currentPoint.x - clickStart_.x) / canvas_.getMagnification());
               int dy = (int) ((currentPoint.y - clickStart_.y) / canvas_.getMagnification());
               vad_.getImagePlus().getOverlay().get(0).setLocation(gridStart_.x + dx, gridStart_.y + dy);
               if (!CanvasPaintPending.isMyPaintPending(canvas_, this)) {
                  canvas_.setPaintPending(true);
                  canvas_.paint(canvas_.getGraphics());
               }
            } else if  (zoomMode_) {
               zoomableStack_.translateZoomPosition(mouseDragStartPoint_.x - currentPoint.x, 
                       mouseDragStartPoint_.y - currentPoint.y);
               redrawPixels();
               mouseDragStartPoint_ = currentPoint;
               drawZoomIndicatorOverlay();               
            } else if (exploreMode_) {
               Point p2 = e.getPoint();
               //find top left row and column and number of columns spanned by drage event
               Point topLeftTile = storage_.tileIndicesFromLoResPixel(Math.min(p2.x, mouseDragStartPoint_.x),
                       Math.min(p2.y, mouseDragStartPoint_.y));
               Point bottomRightTile = storage_.tileIndicesFromLoResPixel(Math.max(p2.x, mouseDragStartPoint_.x),
                       Math.max(p2.y, mouseDragStartPoint_.y));
               highlightTiles(topLeftTile.x, bottomRightTile.x, topLeftTile.y, bottomRightTile.y, TRANSPARENT_MAGENTA);              
            }
         }

         @Override
         public void mouseMoved(MouseEvent e) {
            if (zoomAreaSelectMode_) {
               //draw rectangle of area that will be zoomed in on
               Overlay overlay = new Overlay();
               int width = zoomableStack_.getWidth() / storage_.getDSFactor();
               int height = zoomableStack_.getHeight() / storage_.getDSFactor();
               Point center = e.getPoint();
               Roi rect = new Roi(Math.min(Math.max(0,center.x - width / 2), canvas_.getWidth() - width),
                       Math.min(Math.max(0,center.y - height / 2), canvas_.getHeight() - height),
                       width, height);
               rect.setFillColor(TRANSPARENT_BLUE);
               overlay.add(rect);
               canvas_.setOverlay(overlay);
            } else if (exploreMode_ && !zoomMode_) {
               Point coords = getExploreIndicesFromLoResPixel(e.getPoint());
               int row = coords.x, col = coords.y;
               double tileHeight = storage_.getTileHeight() / (double) storage_.getDSFactor(), 
                       tileWidth = storage_.getTileWidth()/ (double) storage_.getDSFactor();
               int numCols = storage_.getNumCols(), numRows = storage_.getNumRows(),
                       totalWidth = zoomableStack_.getWidth(), totalHeight = zoomableStack_.getHeight();               
               if (col == -1) {
                  //left
                  addExploreOverlay(0, (int)Math.round(row * tileHeight), (int)Math.round(tileWidth / 3),
                          (int)Math.round(tileHeight), TRANSPARENT_BLUE);
               } else if (col == numCols) {
                  //right
                  addExploreOverlay((int)Math.round(totalWidth - tileWidth / 3),
                          (int) Math.round(row * tileHeight),
                          (int) Math.round(tileWidth / 3), (int)Math.round(tileHeight), TRANSPARENT_BLUE);
               } else if (row == -1) {
                  //top                                     
                  addExploreOverlay((int)Math.round(col*tileWidth), 0,(int) Math.round(tileWidth),
                          (int) Math.round(tileHeight / 3), TRANSPARENT_BLUE);                  
               } else if (row == numRows) {
                  //bottom      
                  addExploreOverlay((int)Math.round(col * tileWidth),(int)Math.round(totalHeight - tileHeight / 3),
                          (int)Math.round(tileWidth), (int)Math.round(tileHeight / 3), TRANSPARENT_BLUE);
               } else {
                  //highlight a tile
                  highlightTiles(row, row, col, col, TRANSPARENT_MAGENTA);
               }
            }
         }
      });

      canvas_.addMouseListener(new MouseListener() {

         @Override
         public void mouseClicked(MouseEvent e) {
            if (gotoMode_) {
//               //translate point into stage coordinates and move there
//               Point p = e.getPoint();
//               double xPixelDisp = (p.x / canvas_.getMagnification())
//                       + canvas_.getSrcRect().x - vad_.getImagePlus().getWidth() / 2;
//               double yPixelDisp = (p.y / canvas_.getMagnification())
//                       + canvas_.getSrcRect().y - vad_.getImagePlus().getHeight() / 2;
//
//               Point2D stagePos = stagePositionFromPixelPosition(xPixelDisp, yPixelDisp);
//               try {
//                  MMStudioMainFrame.getInstance().setXYStagePosition(stagePos.getX(), stagePos.getY());
//               } catch (MMScriptException ex) {
//                  ReportingUtils.showError("Couldn't move xy stage");
//               }
//               controls_.clearSelectedButtons();
            } else if (zoomAreaSelectMode_) {
               zoomIn(e.getPoint());
            } else {
               if (e.getClickCount() > 1) {
                  if (zoomMode_) {
                     zoomOut();
                  } else {
                     zoomIn(e.getPoint());
                  }
               }  
            }
         }

         @Override
         public void mousePressed(MouseEvent e) {
            if (zoomMode_ || exploreMode_) {
               mouseDragStartPoint_ = e.getPoint();
            } else if (newGridMode_) {
               clickStart_ = e.getPoint();
               Roi rect = vad_.getImagePlus().getOverlay().get(0);
               Rectangle2D bounds = rect.getFloatBounds();
               gridStart_ = new Point((int) bounds.getX(), (int) bounds.getY());
            } 
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            if (exploreMode_ && !e.isShiftDown()) {
               Point exploreCoordinates = getExploreIndicesFromLoResPixel(e.getPoint());
               if (exploreCoordinates.x == -1 || exploreCoordinates.y == -1
                       || exploreCoordinates.x == storage_.getNumRows() || exploreCoordinates.y == storage_.getNumCols()) {
                  //explore outside exisitn area
                  eng_.acquireTile(exploreCoordinates.x, exploreCoordinates.y);
               } else {

                  Point p2 = e.getPoint();
                  //find top left row and column and number of columns spanned by drage event
                  final Point topLeftTile = storage_.tileIndicesFromLoResPixel(Math.min(p2.x, mouseDragStartPoint_.x),
                          Math.min(p2.y, mouseDragStartPoint_.y));
                  final Point bottomRightTile = storage_.tileIndicesFromLoResPixel(Math.max(p2.x, mouseDragStartPoint_.x),
                          Math.max(p2.y, mouseDragStartPoint_.y));
                  new Thread(new Runnable() {

                     @Override
                     public void run() {
                        for (int row = topLeftTile.x; row <= bottomRightTile.x; row++) {
                           for (int col = topLeftTile.y; col <= bottomRightTile.y; col++) {
                              eng_.acquireTile(row, col);
                           }
                        }
                     }
                  }).start();
               }
               //redraw overlay to potentially reflect new grid
               
            }
         }

         @Override
         public void mouseEntered(MouseEvent e) {
         }

         @Override
         public void mouseExited(MouseEvent e) {
            if (!gotoMode_) {
               return;
            }
            mouseRowIndex_ = -1;
            mouseColIndex_ = -1;
            drawDepthListOverlay();
         }
      });
   }

   private void createGrid() {
//      try {
//         //get displacements of center of rectangle from center of stitched image
//         double rectCenterXDisp = vad_.getImagePlus().getOverlay().get(0).getFloatBounds().getCenterX()
//                 - vad_.getImagePlus().getWidth() / 2;
//         double rectCenterYDisp = vad_.getImagePlus().getOverlay().get(0).getFloatBounds().getCenterY()
//                 - vad_.getImagePlus().getHeight() / 2;
//
//         Point2D.Double stagePos = stagePositionFromPixelPosition(rectCenterXDisp, rectCenterYDisp);
//
//         int xOverlap = SettingsDialog.getXOverlap(), yOverlap = SettingsDialog.getYOverlap();
//         Util.createGrid(stagePos.x, stagePos.y,
//                 (Integer) gridXSpinner_.getValue(), (Integer) gridYSpinner_.getValue(),
//                 xOverlap, yOverlap);
//         controls_.clearSelectedButtons();
//
//      } catch (Exception e) {
//         ReportingUtils.showError("Couldnt create grid");
//      }
   }

   private Point2D.Double stagePositionFromPixelPosition(double xPixelDispFromCenter, double yPixelDispFromCenter) {
//      try {
//         //get coordinates of center of exisitng grid
//         String xyStage = MMStudioMainFrame.getInstance().getCore().getXYStageDevice();
//
//         //row column map to coordinates for exisiting stage positiions
//         Point2D.Double[][] coordinates = new Point2D.Double[numCols_][numRows_];
//         for (int i = 0; i < positionList_.length(); i++) {
//            int colInd = (int) positionList_.getJSONObject(i).getLong("GridColumnIndex");
//            int rowInd = (int) positionList_.getJSONObject(i).getLong("GridRowIndex");
//            JSONArray coords = positionList_.getJSONObject(i).getJSONObject("DeviceCoordinatesUm").getJSONArray(xyStage);
//            coordinates[colInd][rowInd] = new Point2D.Double(coords.getDouble(0), coords.getDouble(1));
//         }
//
//         //find stage coordinate of center of existing grid
//         double currentCenterX, currentCenterY;
//         if (coordinates.length % 2 == 0 && coordinates[0].length % 2 == 0) {
//            //even number of tiles in both directions
//            currentCenterX = 0.25 * coordinates[numCols_ / 2 - 1][numRows_ / 2 - 1].x + 0.25 * coordinates[numCols_ / 2 - 1][numRows_ / 2].x
//                    + 0.25 * coordinates[numCols_ / 2][numRows_ / 2 - 1].x + 0.25 * coordinates[numCols_ / 2][numRows_ / 2].x;
//            currentCenterY = 0.25 * coordinates[numCols_ / 2 - 1][numRows_ / 2 - 1].y + 0.25 * coordinates[numCols_ / 2 - 1][numRows_ / 2].y
//                    + 0.25 * coordinates[numCols_ / 2][numRows_ / 2 - 1].y + 0.25 * coordinates[numCols_ / 2][numRows_ / 2].y;
//         } else if (coordinates.length % 2 == 0) {
//            //even number of columns
//            currentCenterX = 0.5 * coordinates[numCols_ / 2 - 1][numRows_ / 2].x + 0.5 * coordinates[numCols_ / 2][numRows_ / 2].x;
//            currentCenterY = 0.5 * coordinates[numCols_ / 2 - 1][numRows_ / 2].y + 0.5 * coordinates[numCols_ / 2][numRows_ / 2].y;
//         } else if (coordinates[0].length % 2 == 0) {
//            //even number of rows
//            currentCenterX = 0.5 * coordinates[numCols_ / 2][numRows_ / 2 - 1].x + 0.5 * coordinates[numCols_ / 2][numRows_ / 2].x;
//            currentCenterY = 0.5 * coordinates[numCols_ / 2][numRows_ / 2 - 1].y + 0.5 * coordinates[numCols_ / 2][numRows_ / 2].y;
//         } else {
//            //odd number of both
//            currentCenterX = coordinates[numCols_ / 2][numRows_ / 2].x;
//            currentCenterY = coordinates[numCols_ / 2][numRows_ / 2].y;
//         }
//
//         //use affine transform to convert to stage coordinate of center of new grid
//         AffineTransform transform = null;
//         Preferences prefs = Preferences.userNodeForPackage(MMStudioMainFrame.class);
//         try {
//            transform = (AffineTransform) JavaUtils.getObjectFromPrefs(prefs, "affine_transform_"
//                    + MMStudioMainFrame.getInstance().getCore().getCurrentPixelSizeConfig(), null);
//            //set map origin to current stage position
//            double[] matrix = new double[6];
//            transform.getMatrix(matrix);
//            matrix[4] = currentCenterX;
//            matrix[5] = currentCenterY;
//            transform = new AffineTransform(matrix);
//         } catch (Exception ex) {
//            ReportingUtils.logError(ex);
//            ReportingUtils.showError("Couldnt get affine transform");
//         }
//
//         //convert pixel displacement of center of new grid to new center stage position
//         Point2D.Double pixelPos = new Point2D.Double(xPixelDispFromCenter, yPixelDispFromCenter);
//         Point2D.Double stagePos = new Point2D.Double();
//         transform.transform(pixelPos, stagePos);
//         return stagePos;
//      } catch (Exception e) {
//         ReportingUtils.showError("Couldn't convert pixel coordinates to stage coordinates");
         return null;
//      }
   }

   private void makeGridOverlay(int centerX, int centerY) {
      Overlay overlay = vad_.getImagePlus().getOverlay();
      if (overlay == null || overlay.size() == 0) {
         overlay = new Overlay();
      } else {
         overlay.clear();
      }

//      int gridWidth = (Integer) gridXSpinner_.getValue();
//      int gridHeight = (Integer) gridYSpinner_.getValue();
//      int xOverlap = SettingsDialog.getXOverlap();
//      int yOverlap = SettingsDialog.getYOverlap();
//      int roiWidth = (gridWidth * tileWidth_) - (gridWidth - 1) * xOverlap;
//      int roiHeight = gridHeight * tileHeight_ - (gridHeight - 1) * yOverlap;
//
//      Roi rectangle = new Roi(centerX - roiWidth / 2, centerY - roiHeight / 2, roiWidth, roiHeight);
//      rectangle.setStrokeWidth(20f);
//      overlay.add(rectangle);
//      vad_.getImagePlus().setOverlay(overlay);
   }

   private void gridSizeChanged() {
      //resize exisiting grid but keep centered on same area
      Overlay overlay = vad_.getImagePlus().getOverlay();
      if (overlay == null || overlay.get(0) == null) {
         return;
      }
      Rectangle2D oldBounds = overlay.get(0).getFloatBounds();
      int centerX = (int) oldBounds.getCenterX();
      int centerY = (int) oldBounds.getCenterY();
      makeGridOverlay(centerX, centerY);
   }

   @Override
   public void imageReceived(TaggedImage taggedImage) {
      try {
         //duplicate so image storage doesnt see incorrect tags
         JSONObject newTags = new JSONObject(taggedImage.tags.toString());
         MDUtils.setPositionIndex(newTags, 0);
         taggedImage = new TaggedImage(taggedImage.pix, newTags);
      } catch (JSONException ex) {
         ReportingUtils.showError("Couldn't manipulate image tags for display");
      }

      if (!suspendUpdates_) {
         vad_.imageReceived(taggedImage);
      } else {
         try {
            //tSelector will be null on first frame
            if (tSelector_ != null) {
               int frame = MDUtils.getFrameIndex(taggedImage.tags);
               if (tSelector_.getMaximum() <= (1 + frame)) {
                  ((VirtualAcquisitionDisplay.IMMImagePlus) vad_.getHyperImage()).setNFramesUnverified(frame + 1);
                  tSelector_.setMaximum(frame + 2);
                  tSelector_.invalidate();
                  tSelector_.validate();
               }
            }
         } catch (Exception ex) {
            ReportingUtils.showError("Couldn't suspend updates");
         }
      }

   }

   @Override
   public void imagingFinished(String path) {
      vad_.imagingFinished(path);
   }

   private void redrawPixels() {
      if (!vad_.getHyperImage().isComposite()) {    
         Object pixels = zoomableStack_.getPixels(vad_.getHyperImage().getCurrentSlice());
         if (pixels != null) { 
            //for some reason when zooming on a part of image still to be acquired pixels get set to null here
            vad_.getHyperImage().getProcessor().setPixels(pixels);
         }
      } else {
         CompositeImage ci = (CompositeImage) vad_.getHyperImage();
         if (ci.getMode() == CompositeImage.COMPOSITE) {
            for (int i = 0; i < ((VirtualAcquisitionDisplay.MMCompositeImage) ci).getNChannelsUnverified(); i++) {
               //Dont need to set pixels if processor is null because it will get them from stack automatically  
               Object pixels = zoomableStack_.getPixels(ci.getCurrentSlice() - ci.getChannel() + i + 1);
               if (ci.getProcessor(i + 1) != null && pixels != null) {
                  ci.getProcessor(i + 1).setPixels(pixels);
               }
            }
         }
         Object pixels = zoomableStack_.getPixels(vad_.getHyperImage().getCurrentSlice());
         if (pixels != null) {
            ci.getProcessor().setPixels(pixels);
         }
      }
      if (CanvasPaintPending.isMyPaintPending(canvas_, this)) {
         return;
      }
      CanvasPaintPending.setPaintPending(canvas_, this);
      vad_.updateAndDraw(true);
   }

   public void newGridButtonAction(boolean selected) {
      if (selected) {
         newGridMode_ = true;
         makeGridOverlay(vad_.getImagePlus().getWidth() / 2, vad_.getImagePlus().getHeight() / 2);
      } else {
         newGridMode_ = false;
         vad_.getImagePlus().setOverlay(null);
         canvas_.repaint();
      }
   }

   class Controls extends DisplayControls {

      private JButton pauseButton_, abortButton_, createGridButton_;
      private JTextField fpsField_;
      private JLabel zPosLabel_, timeStampLabel_, nextFrameLabel_, posNameLabel_,
              byLabel_, gridLabel_;
      private JToggleButton dlOffsetsButton_, newGridButton_, gotoButton_, suspendUpdatesButton_,
              zoomButton_;
      private Timer nextFrameTimer_;

      public Controls() {
         initComponents();
         nextFrameTimer_ = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               long nextImageTime = 0;
               try {
                  nextImageTime = vad_.getNextWakeTime();
               } catch (NullPointerException ex) {
                  nextFrameTimer_.stop();
               }
               if (!vad_.acquisitionIsRunning()) {
                  nextFrameTimer_.stop();
               }
               double timeRemainingS = (nextImageTime - System.nanoTime() / 1000000) / 1000;
               if (timeRemainingS > 0 && vad_.acquisitionIsRunning()) {
                  nextFrameLabel_.setText("Next frame: " + NumberUtils.doubleToDisplayString(1 + timeRemainingS) + " s");
                  nextFrameTimer_.setDelay(100);
               } else {
                  nextFrameTimer_.setDelay(1000);
                  nextFrameLabel_.setText("");
               }

            }
         });
         nextFrameTimer_.start();
      }



      private void gotoButtonAction() {
         if (gotoButton_.isSelected()) {
            gotoButton_.setSelected(true);
            gotoMode_ = true;
            canvas_.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), 0);
         } else {
            gotoMode_ = false;
            canvas_.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR), 0);
         }
      }




      public void acquiringImagesUpdate(boolean state) {
         abortButton_.setEnabled(state);
         pauseButton_.setEnabled(state);
      }

      private void updateFPS() {
         try {
            double fps = NumberUtils.displayStringToDouble(fpsField_.getText());
            vad_.setPlaybackFPS(fps);
         } catch (ParseException ex) {
         }
      }

      public void updateSelectedPosition(String posName) {
         posNameLabel_.setText(posName);
      }

      @Override
      public void imagesOnDiskUpdate(boolean bln) {
//         abortButton_.setEnabled(bln);
//         pauseButton_.setEnabled(bln);
      }

      @Override
      public void setStatusLabel(String string) {
      }

      private void updateLabels(JSONObject tags) {
         //Z position label
         String zPosition = "";
         try {
            zPosition = NumberUtils.doubleStringCoreToDisplay(tags.getString("ZPositionUm"));
         } catch (Exception e) {
            try {
               zPosition = NumberUtils.doubleStringCoreToDisplay(tags.getString("Z-um"));
            } catch (Exception e1) {
               // Do nothing...
            }
         }
         zPosLabel_.setText("Z Position: " + zPosition + " um ");

         //time label
         try {
            int ms = (int) tags.getDouble("ElapsedTime-ms");
            int s = ms / 1000;
            int min = s / 60;
            int h = min / 60;

            String time = twoDigitFormat(h) + ":" + twoDigitFormat(min % 60)
                    + ":" + twoDigitFormat(s % 60) + "." + threeDigitFormat(ms % 1000);
            timeStampLabel_.setText("Elapsed time: " + time + " ");
         } catch (JSONException ex) {
//            ReportingUtils.logE  rror("MetaData did not contain ElapsedTime-ms field");
         }
      }

      private String twoDigitFormat(int i) {
         String ret = i + "";
         if (ret.length() == 1) {
            ret = "0" + ret;
         }
         return ret;
      }

      private String threeDigitFormat(int i) {
         String ret = i + "";
         if (ret.length() == 1) {
            ret = "00" + ret;
         } else if (ret.length() == 2) {
            ret = "0" + ret;
         }
         return ret;
      }

      @Override
      public void newImageUpdate(JSONObject tags) {
         if (tags == null) {
            return;
         }
         updateLabels(tags);
      }



      private void initComponents() {
         setPreferredSize(new java.awt.Dimension(700, 110));
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT)),
                 row2 = new JPanel(new FlowLayout(FlowLayout.LEFT)),
                 row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
         this.add(row1);
         this.add(row2);
         this.add(row3);

         gridXSpinner_ = new JSpinner();
         gridXSpinner_.setModel(new SpinnerNumberModel(2, 1, 1000, 1));
         gridXSpinner_.setPreferredSize(new Dimension(35, 24));
         gridYSpinner_ = new JSpinner();
         gridYSpinner_.setModel(new SpinnerNumberModel(2, 1, 1000, 1));
         gridYSpinner_.setPreferredSize(new Dimension(35, 24));
         gridXSpinner_.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
               gridSizeChanged();
            }
         });
         gridYSpinner_.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
               gridSizeChanged();
            }
         });
         gridLabel_ = new JLabel(" grid");
         byLabel_ = new JLabel("by");
         gridLabel_.setEnabled(false);
         byLabel_.setEnabled(false);
         gridXSpinner_.setEnabled(false);
         gridYSpinner_.setEnabled(false);

         createGridButton_ = new JButton("Create");
         createGridButton_.setEnabled(false);
         createGridButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               createGrid();
            }
         });


         newGridButton_ = new JToggleButton("New grid");
         newGridButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
         });

         gotoButton_ = new JToggleButton("Goto");
         gotoButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               gotoButtonAction();
            }
         });

         dlOffsetsButton_ = new JToggleButton("Set depth list offsets");
         dlOffsetsButton_.setPreferredSize(new Dimension(133, 23));
         posNameLabel_ = new JLabel() {

            @Override
            public void setText(String s) {
               Controls.this.invalidate();
               super.setText(s);
               Controls.this.validate();
            }
         };
         dlOffsetsButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               if (dlOffsetsButton_.isSelected()) {
                  dlOffsetsButton_.setSelected(true);
                  dlOffsetsButton_.setText("Select XY position");
               } else {
                  posNameLabel_.setText("");
                  dlOffsetsButton_.setText("Set depth list offsets");
                  canvas_.setOverlay(null);
                  selectedPositions_.clear();
               }
               drawDepthListOverlay();
            }
         });

         suspendUpdatesButton_ = new JToggleButton("Suspend updates");
         suspendUpdatesButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               if (suspendUpdatesButton_.isSelected()) {
                  suspendUpdatesButton_.setText("Resume updates");
                  suspendUpdates_ = true;
               } else {
                  suspendUpdatesButton_.setText("Suspend updates");
                  suspendUpdates_ = false;
               }
            }
         });


         //button area
         abortButton_ = new JButton();
         abortButton_.setBackground(new java.awt.Color(255, 255, 255));
         abortButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/cancel.png"))); // NOI18N
         abortButton_.setToolTipText("Abort acquisition");
         abortButton_.setFocusable(false);
         abortButton_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         abortButton_.setMaximumSize(new java.awt.Dimension(25, 25));
         abortButton_.setMinimumSize(new java.awt.Dimension(25, 25));
         abortButton_.setPreferredSize(new java.awt.Dimension(25, 25));
         abortButton_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         abortButton_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
               try {
                  JavaUtils.invokeRestrictedMethod(vad_, VirtualAcquisitionDisplay.class, "abort");
               } catch (Exception ex) {
                  ReportingUtils.showError("Couldn't abort. Try pressing stop on Multi-Dimensional acquisition Window");
               }
            }
         });

         pauseButton_ = new JButton();
         pauseButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/control_pause.png"))); // NOI18N
         pauseButton_.setToolTipText("Pause acquisition");
         pauseButton_.setFocusable(false);
         pauseButton_.setMargin(new java.awt.Insets(0, 0, 0, 0));
         pauseButton_.setMaximumSize(new java.awt.Dimension(25, 25));
         pauseButton_.setMinimumSize(new java.awt.Dimension(25, 25));
         pauseButton_.setPreferredSize(new java.awt.Dimension(25, 25));
         pauseButton_.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
               try {
                  JavaUtils.invokeRestrictedMethod(vad_, VirtualAcquisitionDisplay.class, "pause");
               } catch (Exception ex) {
                  ReportingUtils.showError("Couldn't pause");
               }
//               if (eng_.isPaused()) {
//                  pauseButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/resultset_next.png"))); // NOI18N
//               } else {
//                  pauseButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/control_pause.png"))); // NOI18N
//               }
            }
         });

         //text area
         zPosLabel_ = new JLabel("Z position:") {

            @Override
            public void setText(String s) {
               Controls.this.invalidate();
               super.setText(s);
               Controls.this.validate();
            }
         };
         timeStampLabel_ = new JLabel("Elapsed time:") {

            @Override
            public void setText(String s) {
               Controls.this.invalidate();
               super.setText(s);
               Controls.this.validate();
            }
         };
         nextFrameLabel_ = new JLabel("Next frame: ") {

            @Override
            public void setText(String s) {
               Controls.this.invalidate();
               super.setText(s);
               Controls.this.validate();
            }
         };
         fpsField_ = new JTextField();
         fpsField_.setText("7");
         fpsField_.setToolTipText("Set the speed at which the acquisition is played back.");
         fpsField_.setPreferredSize(new Dimension(25, 18));
         fpsField_.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
               updateFPS();
            }
         });
         fpsField_.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyReleased(java.awt.event.KeyEvent evt) {
               updateFPS();
            }
         });
         JLabel fpsLabel = new JLabel("Animation playback FPS: ");


         zoomButton_ = new JToggleButton("Full res");
         zoomButton_.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
            }
         });


         row1.add(newGridButton_);
         row1.add(gridXSpinner_);
         row1.add(byLabel_);
         row1.add(gridYSpinner_);
         row1.add(gridLabel_);
         row1.add(createGridButton_);
         row1.add(gotoButton_);

         row1.add(dlOffsetsButton_);
         row1.add(posNameLabel_);

         row1.add(suspendUpdatesButton_);


         row2.add(abortButton_);
         row2.add(pauseButton_);
         row2.add(fpsLabel);
         row2.add(fpsField_);
         row2.add(zPosLabel_);
         row2.add(timeStampLabel_);
         row2.add(nextFrameLabel_);

         row3.add(zoomButton_);
      }
   }
}
