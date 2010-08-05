/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MetadataViewer.java
 *
 * Created on Jul 16, 2010, 11:18:45 AM
 */
package org.micromanager.acquisition;

import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import mmcorej.Metadata;
import mmcorej.StrMap;
import mmcorej.TaggedImage;
import org.micromanager.utils.GUIUtils;

/**
 *
 * @author arthur
 */
public class MetadataViewer extends javax.swing.JFrame
        implements ImageListener, AWTEventListener {

   private static MetadataViewer singletonViewer_ = null;
   private final MetadataTableModel model_;
   private ImageWindow currentWindow_;
   private final String [] columnNames_ = {"Property","Value"};
   
   /** Creates new form MetadataViewer */
   public MetadataViewer() {
      initComponents();
      model_ = new MetadataTableModel();

      ImagePlus.addImageListener(this);
      update(ij.IJ.getImage());
      metadataTable.setModel(model_);
      this.getToolkit().addAWTEventListener(this, AWTEvent.WINDOW_FOCUS_EVENT_MASK);
   }

   public static MetadataViewer showMetadataViewer() {
      if (singletonViewer_ == null) {
         singletonViewer_ = new MetadataViewer();
         GUIUtils.recallPosition(singletonViewer_);
      }
      singletonViewer_.setVisible(true);
      return singletonViewer_;
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jScrollPane2 = new javax.swing.JScrollPane();
      jTextArea1 = new javax.swing.JTextArea();
      jTabbedPane1 = new javax.swing.JTabbedPane();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTable1 = new javax.swing.JTable();
      jPanel1 = new javax.swing.JPanel();
      metadataTableScrollPane = new javax.swing.JScrollPane();
      metadataTable = new javax.swing.JTable();
      jCheckBox1 = new javax.swing.JCheckBox();
      jScrollPane3 = new javax.swing.JScrollPane();
      jTextArea2 = new javax.swing.JTextArea();

      jTextArea1.setColumns(20);
      jTextArea1.setRows(5);
      jScrollPane2.setViewportView(jTextArea1);

      setTitle("Metadata and Notes");

      jTabbedPane1.setFocusable(false);

      jTable1.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null},
            {null, null},
            {null, null},
            {null, null}
         },
         new String [] {
            "Property", "Value"
         }
      ) {
         boolean[] canEdit = new boolean [] {
            false, false
         };

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
         }
      });
      jScrollPane1.setViewportView(jTable1);

      jTabbedPane1.addTab("Acquisition Summary", jScrollPane1);

      jPanel1.setOpaque(false);

      metadataTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {

         },
         new String [] {
            "Property", "Value"
         }
      ) {
         Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class
         };
         boolean[] canEdit = new boolean [] {
            false, false
         };

         public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
         }
      });
      metadataTable.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
      metadataTable.setDoubleBuffered(true);
      metadataTableScrollPane.setViewportView(metadataTable);

      jCheckBox1.setText("Show unchanging properties");

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jCheckBox1)
            .addContainerGap(114, Short.MAX_VALUE))
         .add(metadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jCheckBox1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(metadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("Image", jPanel1);

      jTextArea2.setColumns(20);
      jTextArea2.setLineWrap(true);
      jTextArea2.setRows(5);
      jTextArea2.setWrapStyleWord(true);
      jScrollPane3.setViewportView(jTextArea2);

      jTabbedPane1.addTab("Notes", jScrollPane3);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   class MetadataTableModel extends AbstractTableModel {

      Vector<Vector<String>> data_;

      MetadataTableModel() {
         data_ = new Vector<Vector<String>>();
      }

      public int getRowCount() {
         return data_.size();
      }

      public void addRow(Vector<String> rowData) {
         data_.add(rowData);
      }

      public int getColumnCount() {
         return 2;
      }

      public synchronized Object getValueAt(int rowIndex, int columnIndex) {
         if (data_.size() > rowIndex) {
            Vector<String> row = data_.get(rowIndex);
            if (row.size() > columnIndex)
               return data_.get(rowIndex).get(columnIndex);
            else
               return "";
         } else {
            return "";
         }
      }

      public void clear() {
         data_.clear();
      }

      @Override
      public String getColumnName(int colIndex) {
         return columnNames_[colIndex];
      }

      public synchronized void setMetadata(Map<String,String> md) {
         clear();
         if (md != null) {
            for (String key : md.keySet()) {
               Vector<String> rowData = new Vector<String>();
               rowData.add(key);
               rowData.add(md.get(key));
               addRow(rowData);
            }
         }
         fireTableDataChanged();
      }
   }

   public void update(ImagePlus imp) {
      if (this.isVisible()) {
         if (imp == null) {
            model_.setMetadata(null);
         } else {
            ImageStack stack = imp.getStack();
            if (stack instanceof AcquisitionVirtualStack) {
               AcquisitionVirtualStack vstack = (AcquisitionVirtualStack) imp.getStack();
               int slice = imp.getCurrentSlice();
               TaggedImage taggedImg = vstack.getTaggedImage(slice);
               if (taggedImg == null) {
                  model_.setMetadata(null);
               } else {
                  Map<String,String> md = vstack.getTaggedImage(slice).md;
                  model_.setMetadata(md);
               }
            } else {
               model_.setMetadata(null);
            }
         }
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      if (visible)
         eventDispatched(null);
   }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JCheckBox jCheckBox1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JScrollPane jScrollPane3;
   private javax.swing.JTabbedPane jTabbedPane1;
   private javax.swing.JTable jTable1;
   private javax.swing.JTextArea jTextArea1;
   private javax.swing.JTextArea jTextArea2;
   private javax.swing.JTable metadataTable;
   private javax.swing.JScrollPane metadataTableScrollPane;
   // End of variables declaration//GEN-END:variables


   //Implements ImageListener
   public void imageOpened(ImagePlus imp) {
      update(imp);
   }

   //Implements ImageListener
   public void imageClosed(ImagePlus imp) {
      if (WindowManager.getCurrentWindow() == null) {
         update((ImagePlus) null);
      }
   }

   //Implements ImageListener
   public void imageUpdated(ImagePlus imp) {
      update(imp);
   }


   //Implements AWTEventListener
   public void eventDispatched(AWTEvent event) {
      ImageWindow currentWindow = WindowManager.getCurrentWindow();
      if (currentWindow_ != currentWindow) {
         ImagePlus imgp = currentWindow.getImagePlus();
         update(imgp);
      }
   }

}
