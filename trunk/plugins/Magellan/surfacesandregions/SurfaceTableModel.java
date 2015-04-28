/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package surfacesandregions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import misc.Log;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author Henry
 */
class SurfaceTableModel extends AbstractTableModel  {

   private final String[] COLUMNS = {"Name", "XY padding (µm)", "# Positions"};
   private SurfaceManager manager_;
   
   public SurfaceTableModel(SurfaceManager manager) {
      manager_ = manager;
   }
   
   @Override
   public int getRowCount() {
      return manager_.getNumberOfSurfaces();
   }
   
   @Override
   public String getColumnName(int index) {
      return COLUMNS[index];
   }

   @Override
   public int getColumnCount() {
      return COLUMNS.length;
   }

   @Override
   public boolean isCellEditable(int rowIndex, int colIndex) {
      if (colIndex == 0 || colIndex == 1) {
         return true;
      }
      return false;
   }
   
   @Override   
   public void setValueAt(Object value, int row, int col) {
      if (col == 0) {
         try {
            manager_.renameSurface(row,(String) value);
         } catch (Exception ex) {
            Log.log("Surface name already taken");
         }
      } else if (col == 1) {
         manager_.getSurface(row).setXYPadding(Double.parseDouble((String) value));
      }  
   }
   
   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      SurfaceInterpolator surface = manager_.getSurface(rowIndex);
      if (columnIndex == 0) {
         return manager_.getSurface(rowIndex).getName();
      } else if (columnIndex == 1) {
         return surface.getXYPadding();
      } else {
         int numPositions = surface.getNumPositions();
         return numPositions == -1 ? "" : numPositions;
      }
   }
   
}
