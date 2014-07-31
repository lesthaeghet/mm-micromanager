/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acq;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import main.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ReportingUtils;


/*
 * Class to manage XY positions, their indices, and positions within a grid at
 * multiple resolution levels
 */
public class PositionManager {

   private static final String COORDINATES_KEY = "DeviceCoordinatesUm";
   private static final String ROW_KEY = "GridRowIndex";
   private static final String COL_KEY = "GridColumnIndex";
   private static final String PROPERTIES_KEY = "Properties";
   private static final String MULTI_RES_NODE_KEY = "MultiResPositionNode";
   
   
   
   private JSONArray positionList_;
   private int minRow_, maxRow_, minCol_, maxCol_; //For the lowest resolution level
   private String xyStageName_ = MMStudioMainFrame.getInstance().getCore().getXYStageDevice();
   //Map of Res level to set of nodes
   private TreeMap<Integer,TreeSet<MultiResPositionNode>> positionNodes_; 

   public PositionManager(JSONObject summaryMD) {
      positionNodes_ = new TreeMap<Integer,TreeSet<MultiResPositionNode>>();
      readRowsAndColsFromPositionList(summaryMD);
   }
   
   public String getSerializedPositionList() {
      return positionList_.toString();
   }
   
   public int getNumPositions() {
      return positionList_.length();
   }

   public double getXCoordinate(int positionIndex) throws JSONException {
      return positionList_.getJSONObject(positionIndex).getJSONObject("DeviceCoordinatesUm").getJSONArray(xyStageName_).getDouble(0);
   }

   public double getYCoordinate(int positionIndex) throws JSONException {
      return positionList_.getJSONObject(positionIndex).getJSONObject("DeviceCoordinatesUm").getJSONArray(xyStageName_).getDouble(1);
   }
   
   public int getNumRows() {
      return 1 + maxRow_ - minRow_;
   }
   
   public int getNumCols() {
      return 1 + maxCol_ - minCol_;              
   }
   
   public int getMinRow() {
      return minRow_;
   }
   
   public int getMinCol() {
      return minCol_;
   }
   
   /**
    * Return the position index of any one of the full res positions corresponding a low res position.
    * There is at least one full res position corresponding to the low res one, but this function makes no
    * guarantees about which one is returned if there are more than one.
    * @param lowResPositionIndex
    * @param resIndex - res index corresponding to the low res position
    * @return position index of full res child of node or -1 if doesn't exist (which shouldn't ever happen)
    */
   public int getFullResPositionIndex(int lowResPositionIndex, int resIndex) {
      for (MultiResPositionNode node : positionNodes_.get(resIndex)) {
         if (node.positionIndex == lowResPositionIndex) {
            while (node.child != null) {
               node = node.child;
            }
            //now we have a full res node that is a descendent
            return node.positionIndex;
         }
      }
      ReportingUtils.showError("Could't find full resolution child of node");
      return -1;
   }

   public int getLowResPositionIndex(int fullResPosIndex, int resIndex) {
      try {
         MultiResPositionNode node = (MultiResPositionNode) positionList_.getJSONObject(fullResPosIndex).getJSONObject(PROPERTIES_KEY).get(MULTI_RES_NODE_KEY);
         for (int i = 0; i < resIndex; i++) {
            node = node.parent;
         }
         return node.positionIndex;
      } catch (JSONException e) {
         ReportingUtils.showError("Couldnt read position list correctly");
         return 0;
      }
   }
   
   public long getGridRow(int fullResPosIndex, int resIndex) {
      try {
         MultiResPositionNode node = (MultiResPositionNode) positionList_.getJSONObject(fullResPosIndex).getJSONObject(PROPERTIES_KEY).get(MULTI_RES_NODE_KEY);
         for (int i = 0; i < resIndex; i++) {
            node = node.parent;
         }
         return node.gridRow;
      } catch (JSONException e) {
         ReportingUtils.showError("Couldnt read position list correctly");
         return 0;
      }
   }

   public long getGridCol(int fullResPosIndex, int resIndex) {
      try {
         MultiResPositionNode node = (MultiResPositionNode) positionList_.getJSONObject(fullResPosIndex).getJSONObject(PROPERTIES_KEY).get(MULTI_RES_NODE_KEY);
         for (int i = 0; i < resIndex; i++) {
            node = node.parent;
         }
         return node.gridCol;
      } catch (JSONException e) {
         ReportingUtils.showError("Couldnt read position list correctly");
         return 0;
      }
   }

   /**
    * 
    * @param dsIndex
    * @param rowIndex
    * @param colIndex
    * @return position index given res level or -1 if it doesn't exist
    */
   public int getPositionIndexFromTilePosition(int dsIndex, int rowIndex, int colIndex) {
      MultiResPositionNode nodeToFind = findExisitngNode(dsIndex, rowIndex, colIndex);
      if (nodeToFind != null) {
         return nodeToFind.positionIndex;
      }
      return -1;
   }

   /**
    * Return the position indices for the positions at the specified rows, cols.
    * If no position exists at this location, create one and return its index
    * @param rows
    * @param cols
    * @return
    */
   public int[] getPositionIndices(int[] rows, int[] cols) {
      try {
         int[] posIndices = new int[rows.length];
         boolean newPositionsAdded = false;

         outerloop:
         for (int h = 0; h < posIndices.length; h++) {
            //check if position is already present in list, and if so, return its index
            for (int i = 0; i < positionList_.length(); i++) {
               if (positionList_.getJSONObject(i).getLong(ROW_KEY) == rows[h]
                       && positionList_.getJSONObject(i).getLong(COL_KEY) == cols[h]) {
                  //we already have position, so return its index
                  posIndices[h] = i;
                  continue outerloop;
               }
            }
            //add this position to list
            positionList_.put(createPosition(rows[h], cols[h]));
            newPositionsAdded = true;
            
            posIndices[h] = positionList_.length() - 1;
         }
         //if size of grid wasn't expanded, return here
         if (!newPositionsAdded) {
            return posIndices;
         }

         updateMinAndMaxRowsAndCols();
         updateLowerResolutionNodes();
         return posIndices;
      } catch (JSONException e) {
         ReportingUtils.showError("Problem with position metadata");
         return null;
      }
   }
   
   private void updateMinAndMaxRowsAndCols() throws JSONException {
      //Go through all positions to update numRows and numCols
      for (int i = 0; i < positionList_.length(); i++) {
         JSONObject pos = positionList_.getJSONObject(i);
         minRow_ = (int) Math.min(pos.getLong(ROW_KEY), minRow_);
         minCol_ = (int) Math.min(pos.getLong(COL_KEY), minCol_);
         maxRow_ = (int) Math.max(pos.getLong(ROW_KEY), maxRow_);
         maxCol_ = (int) Math.max(pos.getLong(COL_KEY), maxCol_);
      }
   }

   private void readRowsAndColsFromPositionList(JSONObject summaryMD) {
      minRow_ = 0; maxRow_ = 0; minCol_ = 0; maxRow_ = 0;
      try {
         if (summaryMD.has("InitialPositionList") && !summaryMD.isNull("InitialPositionList")) {
            positionList_ = summaryMD.getJSONArray("InitialPositionList");
            updateLowerResolutionNodes(); //make sure nodes created for all preexisiting positions
            updateMinAndMaxRowsAndCols();
         } else {
            positionList_ = new JSONArray();
         }
      } catch (JSONException e) {
         ReportingUtils.showError("Couldn't read initial position list");
      }
   }

   private JSONObject createPosition(int row, int col) {
      try {
         JSONArray xy = new JSONArray();
         //TODO: change if overlap added
         int xOverlap = 0;
         int yOverlap = 0;
         Point2D.Double stageCoords = getStageCoordinates(row, col, xOverlap, yOverlap);

         JSONObject coords = new JSONObject();
         xy.put(stageCoords.x);
         xy.put(stageCoords.y);
         coords.put(MMStudioMainFrame.getInstance().getCore().getXYStageDevice(), xy);
         JSONObject pos = new JSONObject();
         pos.put(COORDINATES_KEY, coords);
         pos.put(COL_KEY, col);
         pos.put(ROW_KEY, row);
         pos.put(PROPERTIES_KEY, new JSONObject());

         return pos;
      } catch (Exception e) {
         ReportingUtils.showError("Couldn't create XY position");
         return null;
      }
   }
   
   private MultiResPositionNode[] getFullResNodes() throws JSONException {
      //Go through all base resolution positions and make a list of their multiResNodes, creating nodes when neccessary
      MultiResPositionNode[] fullResNodes = new MultiResPositionNode[positionList_.length()];
      for (int i = 0; i < positionList_.length(); i++) {
         JSONObject position = positionList_.getJSONObject(i);
         if (!position.getJSONObject(PROPERTIES_KEY).has(MULTI_RES_NODE_KEY)) {
            //make sure level 0 set exists
            if (!positionNodes_.containsKey(0)) {
               positionNodes_.put(0, new TreeSet<MultiResPositionNode>());
            }
            //add node in case its a new position
            MultiResPositionNode n = new MultiResPositionNode(0,position.getLong(ROW_KEY),position.getLong(COL_KEY));
            positionNodes_.get(0).add(n);
            n.positionIndex = i;
            position.getJSONObject(PROPERTIES_KEY).put(MULTI_RES_NODE_KEY, n);
         }
         fullResNodes[i] = (MultiResPositionNode) position.getJSONObject(PROPERTIES_KEY).get(MULTI_RES_NODE_KEY);        
      }
      return fullResNodes;
   }

   private void updateLowerResolutionNodes() throws JSONException {
      int gridLength = Math.max(maxRow_ - minRow_ + 1, maxCol_ - minCol_ + 1);
      int lowestResLevel = (int) Math.ceil(Math.log(gridLength) / Math.log(2));

      //Go through all base resolution positions and make a list of their multiResNodes, creating nodes when neccessary
      MultiResPositionNode[] fullResNodes = getFullResNodes();
      for (MultiResPositionNode node : fullResNodes) {
         //recursively link all nodes to their parents to ensure that correct
         //position indices and grid/col indices are know for all needed res levels
         linkToParentNodes(node, lowestResLevel);
      }
   }
   
   //Lower res levels are actually higher numbers: 0 is full res, 1 is factor of two, 2 facotr of 4, etc
   //lowestResLevel tells you the lowest resolution data is being downsampled to
   private void linkToParentNodes(MultiResPositionNode node, int lowestResLevel) {
      if (node.resLevel == lowestResLevel) {
         return; //lowest resLevel reached, mission complete
      } else if (node.parent == null) {
         //first figure out if the parent node already exists by using the defined
         //relationship between gridRow and gridCol at different res levels, which is
         //that 0 will be topleft child of 0 at next lowest resolution
         long parentRow, parentCol;
         if (node.gridCol >= 0) {
            parentCol = node.gridCol / 2;
         } else {
            parentCol = (node.gridCol - 1) / 2;
         }
         if (node.gridRow >= 0) {
            parentRow = node.gridRow / 2;
         } else {
            parentRow = (node.gridRow - 1) / 2;
         }
         MultiResPositionNode parentNode = findExisitngNode(node.resLevel + 1, parentRow, parentCol);
         if (parentNode == null) {
            //parent node does not exist, so create it
            parentNode = new MultiResPositionNode(node.resLevel + 1, parentRow, parentCol);
            //add to list of all nodes, creating storage level if needed
            if (!positionNodes_.containsKey(parentNode.resLevel)) {
               positionNodes_.put(parentNode.resLevel, new TreeSet<MultiResPositionNode>());
            }
            positionNodes_.get(parentNode.resLevel).add(parentNode);
            //count number of positions at this res level to get position index
            int numPositions = positionNodes_.get(parentNode.resLevel).size();
            parentNode.positionIndex = numPositions - 1;
         }
         //link together child and parent
         node.parent = parentNode;
         parentNode.child = node;
      }
      linkToParentNodes(node.parent, lowestResLevel); //keep traveling up the parent chanin
   }
   
   private MultiResPositionNode findExisitngNode(int resLevel, long gridRow, long gridCol ) {
      MultiResPositionNode nodeToFind = new MultiResPositionNode(resLevel, gridRow, gridCol);
      if (positionNodes_.containsKey(resLevel) && positionNodes_.get(resLevel).contains(nodeToFind)) {
         return positionNodes_.get(resLevel).ceiling(nodeToFind); //this should return the equal node if everything works properly
      }
      return null;
   }
   

   /**
    * Calculate the x and y stage coordinates of a new position given its row
    * and column and the existing metadata for another position
    *
    * @param row
    * @param col
    * @param existingPosition
    * @return
    */
   private Point2D.Double getStageCoordinates(int row, int col, int pixelOverlapX, int pixelOverlapY) {
      try {
         ScriptInterface app = MMStudioMainFrame.getInstance();

         JSONObject existingPosition = positionList_.getJSONObject(0);

         double exisitngX = existingPosition.getJSONObject(COORDINATES_KEY).getJSONArray(xyStageName_).getDouble(0);
         double exisitngY = existingPosition.getJSONObject(COORDINATES_KEY).getJSONArray(xyStageName_).getDouble(1);
         int existingRow = existingPosition.getInt(ROW_KEY);
         int existingColumn = existingPosition.getInt(COL_KEY);
         long height = app.getMMCore().getImageHeight();
         long width = app.getMMCore().getImageWidth();

         double xPixelOffset = (col - existingColumn) * (width - pixelOverlapX);
         double yPixelOffset = (row - existingRow) * (height - pixelOverlapY);

         AffineTransform transform = Util.getAffineTransform(exisitngX, exisitngY);
         Point2D.Double stagePos = new Point2D.Double();
         transform.transform(new Point2D.Double(xPixelOffset, yPixelOffset), stagePos);
         return stagePos;
      } catch (JSONException ex) {
         ReportingUtils.showError("Problem with current position metadata");
         return null;
      }
   }

   /*
    * This class is a data structure describing positions corresponding to one another at different
    * resolution levels.
    * Full resolution is at the bottom of the tree, with pointers going up to lower resolotion
    * forming a pyrimid shape
    */
class MultiResPositionNode  implements Comparable<MultiResPositionNode> {
   
   MultiResPositionNode parent, child; 
   //each node actually has 4 children, but only need one to trace down to full res
   long gridRow, gridCol;
   int resLevel;
   int positionIndex;
      
   public MultiResPositionNode(int rLevel, long gRow, long gCol) {
      resLevel = rLevel;
      gridRow = gRow;
      gridCol = gCol;
   }
   
   //TODO: serialize so this can be written to disk
   
   @Override
   public int compareTo(MultiResPositionNode n) {
      if (this.resLevel != n.resLevel) {
         return this.resLevel - n.resLevel;
      } else if (this.gridRow != n.gridRow) {
         return (int) (this.gridRow - n.gridRow);
      } else {
         return (int) (this.gridCol - n.gridCol);
      }
   }

}

}