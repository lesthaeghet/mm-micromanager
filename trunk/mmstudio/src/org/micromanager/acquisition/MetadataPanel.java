/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MetadataPanel.java
 *
 * Created on Oct 20, 2010, 10:40:52 AM
 */
package org.micromanager.acquisition;

import ij.CompositeImage;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.table.AbstractTableModel;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.api.ImageFocusListener;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class MetadataPanel extends javax.swing.JPanel
        implements ImageListener, ImageFocusListener {



   private static MetadataPanel singletonViewer_ = null;

   private final MetadataTableModel imageMetadataModel_;
   private final MetadataTableModel summaryMetadataModel_;
   private ImageWindow currentWindow_ = null;
   private final String [] columnNames_ = {"Property","Value"};
   private MMImageCache cache_;
   private boolean showUnchangingKeys_;
   private boolean updatingDisplayModeCombo_ = false;
   private MMVirtualAcquisitionDisplay acq_;
   private ArrayList<ChannelControlPanel> ccpList_;

    /** Creates new form MetadataPanel */
    public MetadataPanel() {
      initComponents();
      imageMetadataModel_ = new MetadataTableModel();
      summaryMetadataModel_ = new MetadataTableModel();
      ImagePlus.addImageListener(this);
      GUIUtils.registerImageFocusListener(this);
      //update(WindowManager.getCurrentImage());
      imageMetadataTable.setModel(imageMetadataModel_);
      summaryMetadataTable.setModel(summaryMetadataModel_);

      setDisplayState(CompositeImage.COMPOSITE);
    }

    public static MetadataPanel showMetadataPanel() {
      if (singletonViewer_ == null) {
         singletonViewer_ = new MetadataPanel();
         //GUIUtils.recallPosition(singletonViewer_);
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

      tabbedPane = new javax.swing.JTabbedPane();
      ChannelsTablePanel = new javax.swing.JPanel();
      contrastScrollPane = new javax.swing.JScrollPane();
      jPanel1 = new javax.swing.JPanel();
      displayModeCombo = new javax.swing.JComboBox();
      jLabel1 = new javax.swing.JLabel();
      metadataSplitPane = new javax.swing.JSplitPane();
      imageMetadataScrollPane = new javax.swing.JPanel();
      imageMetadataTableScrollPane = new javax.swing.JScrollPane();
      imageMetadataTable = new javax.swing.JTable();
      showUnchangingPropertiesCheckbox = new javax.swing.JCheckBox();
      summaryMetadataPanel = new javax.swing.JPanel();
      summaryMetadataScrollPane = new javax.swing.JScrollPane();
      summaryMetadataTable = new javax.swing.JTable();
      CommentsSplitPane = new javax.swing.JSplitPane();
      summaryCommentsPane = new javax.swing.JPanel();
      summaryCommentsLabel = new javax.swing.JLabel();
      summaryCommentsScrollPane = new javax.swing.JScrollPane();
      summaryCommentsTextArea = new javax.swing.JTextArea();
      imageCommentsPanel = new javax.swing.JPanel();
      imageCommentsLabel = new javax.swing.JLabel();
      imageCommentsScrollPane = new javax.swing.JScrollPane();
      summaryCommentsTextArea1 = new javax.swing.JTextArea();

      tabbedPane.setFocusable(false);
      tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            tabbedPaneStateChanged(evt);
         }
      });

      contrastScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      displayModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Composite", "Color", "Grayscale" }));
      displayModeCombo.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            displayModeComboActionPerformed(evt);
         }
      });

      jLabel1.setText("Display mode:");

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jLabel1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(displayModeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(357, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(jLabel1)
            .add(displayModeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );

      org.jdesktop.layout.GroupLayout ChannelsTablePanelLayout = new org.jdesktop.layout.GroupLayout(ChannelsTablePanel);
      ChannelsTablePanel.setLayout(ChannelsTablePanelLayout);
      ChannelsTablePanelLayout.setHorizontalGroup(
         ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, contrastScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
      );
      ChannelsTablePanelLayout.setVerticalGroup(
         ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(ChannelsTablePanelLayout.createSequentialGroup()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(contrastScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
      );

      tabbedPane.addTab("Channels", ChannelsTablePanel);

      metadataSplitPane.setBorder(null);
      metadataSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

      imageMetadataTable.setModel(new javax.swing.table.DefaultTableModel(
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
      imageMetadataTable.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
      imageMetadataTable.setDoubleBuffered(true);
      imageMetadataTableScrollPane.setViewportView(imageMetadataTable);

      showUnchangingPropertiesCheckbox.setText("Show unchanging properties");
      showUnchangingPropertiesCheckbox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            showUnchangingPropertiesCheckboxActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout imageMetadataScrollPaneLayout = new org.jdesktop.layout.GroupLayout(imageMetadataScrollPane);
      imageMetadataScrollPane.setLayout(imageMetadataScrollPaneLayout);
      imageMetadataScrollPaneLayout.setHorizontalGroup(
         imageMetadataScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageMetadataScrollPaneLayout.createSequentialGroup()
            .add(showUnchangingPropertiesCheckbox)
            .addContainerGap())
         .add(imageMetadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
      );
      imageMetadataScrollPaneLayout.setVerticalGroup(
         imageMetadataScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageMetadataScrollPaneLayout.createSequentialGroup()
            .add(showUnchangingPropertiesCheckbox)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(imageMetadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
      );

      metadataSplitPane.setRightComponent(imageMetadataScrollPane);

      summaryMetadataTable.setModel(new javax.swing.table.DefaultTableModel(
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
      summaryMetadataScrollPane.setViewportView(summaryMetadataTable);

      org.jdesktop.layout.GroupLayout summaryMetadataPanelLayout = new org.jdesktop.layout.GroupLayout(summaryMetadataPanel);
      summaryMetadataPanel.setLayout(summaryMetadataPanelLayout);
      summaryMetadataPanelLayout.setHorizontalGroup(
         summaryMetadataPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryMetadataScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
      );
      summaryMetadataPanelLayout.setVerticalGroup(
         summaryMetadataPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryMetadataScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
      );

      metadataSplitPane.setLeftComponent(summaryMetadataPanel);

      tabbedPane.addTab("Metadata", metadataSplitPane);

      CommentsSplitPane.setBorder(null);
      CommentsSplitPane.setDividerLocation(200);
      CommentsSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

      summaryCommentsLabel.setText("Acquisition comments:");

      summaryCommentsTextArea.setColumns(20);
      summaryCommentsTextArea.setLineWrap(true);
      summaryCommentsTextArea.setRows(1);
      summaryCommentsTextArea.setTabSize(3);
      summaryCommentsTextArea.setWrapStyleWord(true);
      summaryCommentsTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            summaryCommentsTextAreaFocusLost(evt);
         }
      });
      summaryCommentsScrollPane.setViewportView(summaryCommentsTextArea);

      org.jdesktop.layout.GroupLayout summaryCommentsPaneLayout = new org.jdesktop.layout.GroupLayout(summaryCommentsPane);
      summaryCommentsPane.setLayout(summaryCommentsPaneLayout);
      summaryCommentsPaneLayout.setHorizontalGroup(
         summaryCommentsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(summaryCommentsPaneLayout.createSequentialGroup()
            .add(summaryCommentsLabel)
            .addContainerGap(440, Short.MAX_VALUE))
         .add(summaryCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
      );
      summaryCommentsPaneLayout.setVerticalGroup(
         summaryCommentsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(summaryCommentsPaneLayout.createSequentialGroup()
            .add(summaryCommentsLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(summaryCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
      );

      CommentsSplitPane.setLeftComponent(summaryCommentsPane);

      imageCommentsPanel.setPreferredSize(new java.awt.Dimension(500, 300));

      imageCommentsLabel.setText("Per-image comments:");

      summaryCommentsTextArea1.setColumns(20);
      summaryCommentsTextArea1.setLineWrap(true);
      summaryCommentsTextArea1.setRows(1);
      summaryCommentsTextArea1.setTabSize(3);
      summaryCommentsTextArea1.setWrapStyleWord(true);
      summaryCommentsTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            summaryCommentsTextArea1FocusLost(evt);
         }
      });
      imageCommentsScrollPane.setViewportView(summaryCommentsTextArea1);

      org.jdesktop.layout.GroupLayout imageCommentsPanelLayout = new org.jdesktop.layout.GroupLayout(imageCommentsPanel);
      imageCommentsPanel.setLayout(imageCommentsPanelLayout);
      imageCommentsPanelLayout.setHorizontalGroup(
         imageCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageCommentsPanelLayout.createSequentialGroup()
            .add(imageCommentsLabel)
            .addContainerGap(447, Short.MAX_VALUE))
         .add(imageCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
      );
      imageCommentsPanelLayout.setVerticalGroup(
         imageCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageCommentsPanelLayout.createSequentialGroup()
            .add(imageCommentsLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(imageCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
      );

      CommentsSplitPane.setRightComponent(imageCommentsPanel);

      tabbedPane.addTab("Comments", CommentsSplitPane);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void displayModeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayModeComboActionPerformed
       if (!updatingDisplayModeCombo_) {
          setDisplayState(displayModeCombo.getSelectedIndex()+1);
       }
}//GEN-LAST:event_displayModeComboActionPerformed

    private void summaryCommentsTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_summaryCommentsTextAreaFocusLost
       if (cache_ != null) {
          cache_.setComment(summaryCommentsTextArea.getText());
       }
}//GEN-LAST:event_summaryCommentsTextAreaFocusLost

    private void summaryCommentsTextArea1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_summaryCommentsTextArea1FocusLost
       // TODO add your handling code here:
}//GEN-LAST:event_summaryCommentsTextArea1FocusLost

    private void showUnchangingPropertiesCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUnchangingPropertiesCheckboxActionPerformed
       showUnchangingKeys_ = showUnchangingPropertiesCheckbox.isSelected();
       update(WindowManager.getCurrentImage());
}//GEN-LAST:event_showUnchangingPropertiesCheckboxActionPerformed

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
       try {
          update(WindowManager.getCurrentImage());
       } catch (Exception e) {}
}//GEN-LAST:event_tabbedPaneStateChanged


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel ChannelsTablePanel;
   private javax.swing.JSplitPane CommentsSplitPane;
   private javax.swing.JScrollPane contrastScrollPane;
   private javax.swing.JComboBox displayModeCombo;
   private javax.swing.JLabel imageCommentsLabel;
   private javax.swing.JPanel imageCommentsPanel;
   private javax.swing.JScrollPane imageCommentsScrollPane;
   private javax.swing.JPanel imageMetadataScrollPane;
   private javax.swing.JTable imageMetadataTable;
   private javax.swing.JScrollPane imageMetadataTableScrollPane;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JSplitPane metadataSplitPane;
   private javax.swing.JCheckBox showUnchangingPropertiesCheckbox;
   private javax.swing.JLabel summaryCommentsLabel;
   private javax.swing.JPanel summaryCommentsPane;
   private javax.swing.JScrollPane summaryCommentsScrollPane;
   private javax.swing.JTextArea summaryCommentsTextArea;
   private javax.swing.JTextArea summaryCommentsTextArea1;
   private javax.swing.JPanel summaryMetadataPanel;
   private javax.swing.JScrollPane summaryMetadataScrollPane;
   private javax.swing.JTable summaryMetadataTable;
   private javax.swing.JTabbedPane tabbedPane;
   // End of variables declaration//GEN-END:variables



   private ImagePlus getCurrentImage() {
      try {
         return WindowManager.getCurrentImage();
      } catch (Exception e) {
         return null;
      }
   }

   private void setDisplayState(int state) {
      ImagePlus imgp = getCurrentImage();
      if (imgp instanceof CompositeImage) {
         CompositeImage ci = (CompositeImage) imgp;
         ci.setMode(state);
         ci.updateAndDraw();
      }
   }


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

      public synchronized void setMetadata(JSONObject md) {
         clear();
         if (md != null) {
            String [] keys = MDUtils.getKeys(md);
            Arrays.sort(keys);
            for (String key : keys) {
               Vector<String> rowData = new Vector<String>();
               rowData.add((String) key);
               try {
                  rowData.add(md.getString(key));
               } catch (JSONException ex) {
                  ReportingUtils.logError(ex);
               }
               addRow(rowData);
            }
         }
         fireTableDataChanged();
      }
   }

   private JSONObject selectChangingTags(JSONObject md) {
      JSONObject mdChanging = new JSONObject();
      if (cache_ != null) {
         for (String key : cache_.getChangingKeys()) {
            if (md.has(key)) {
               try {
                  mdChanging.put(key, md.get(key));
               } catch (JSONException ex) {
                  ReportingUtils.logError(ex);
               }
            }
         }
      }
      return mdChanging;
   }

   private AcquisitionVirtualStack getAcquisitionStack(ImagePlus imp) {
      ImageStack stack = imp.getStack();
      if (stack instanceof AcquisitionVirtualStack) {
         return (AcquisitionVirtualStack) stack;
      } else {
         return null;
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
   }



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

   private MMImageCache getCache(ImagePlus imgp) {
      AcquisitionVirtualStack stack = getAcquisitionStack(imgp);
      if (stack != null)
         return stack.getCache();
      else
         return null;
   }

   /*
    * update(ImagePlus imp) is called every time the image is changed
    * or the sliders have moved.
    */
   public void update(ImagePlus imp) {
      if (this.isVisible()) {
         int tabSelected = tabbedPane.getSelectedIndex();
         if (imp == null) {
            imageMetadataModel_.setMetadata(null);
            summaryCommentsTextArea.setText(null);
         } else {
            if (tabSelected == 1) {
               AcquisitionVirtualStack stack = getAcquisitionStack(imp);
               if (stack != null) {
                  int slice = imp.getCurrentSlice();
                  TaggedImage taggedImg = stack.getTaggedImage(slice);
                  if (taggedImg == null) {
                     imageMetadataModel_.setMetadata(null);
                  } else {
                     JSONObject md = stack.getTaggedImage(slice).tags;
                     if (!showUnchangingKeys_)
                        md = selectChangingTags(md);
                     imageMetadataModel_.setMetadata(md);
                  }
               } else {
                  imageMetadataModel_.setMetadata(null);
               }
            } else if (tabSelected == 0) {
               updateChannelControls();
            }
         }
      }
   }

   //Implements AWTEventListener
   /*
    * This is called, in contrast to update(), only when the ImageWindow
    * in focus has changed.
    */
   public void focusReceived(ImageWindow focusedWindow) {
      if (currentWindow_ != focusedWindow) {
         ImagePlus imgp = focusedWindow.getImagePlus();
         cache_ = getCache(imgp);

         if (cache_ != null) {
            summaryCommentsTextArea.setText(cache_.getComment());
            JSONObject md = cache_.getSummaryMetadata();
            summaryMetadataModel_.setMetadata(md);
         } else {
            summaryCommentsTextArea.setText(null);
         }

         if (imgp instanceof CompositeImage) {
            CompositeImage cimp = (CompositeImage) imgp;
            updatingDisplayModeCombo_ = true;
            displayModeCombo.setSelectedIndex(cimp.getMode()-1);
            updatingDisplayModeCombo_ = false;
         }
         AcquisitionVirtualStack stack = getAcquisitionStack(imgp);
         if (stack != null) {
            acq_ = stack.getVirtualAcquisition();
            setupChannelControls(acq_);

            update(imgp);
            currentWindow_ = focusedWindow;
         }
      }
   }

   public synchronized void setupChannelControls(MMVirtualAcquisitionDisplay acq) {
      int hpHeight = 100;
      int nChannels = acq.getChannels();

      JPanel p = new JPanel();
      p.setPreferredSize(new Dimension(200,nChannels * hpHeight));
      contrastScrollPane.setViewportView(p);
      SpringLayout layout = new SpringLayout();
      p.setLayout(layout);
      ccpList_ = new ArrayList<ChannelControlPanel>();

      for (int i=0;i<nChannels;++i) {
         ChannelControlPanel ccp = new ChannelControlPanel(acq, i);

         layout.putConstraint(SpringLayout.NORTH,ccp,hpHeight*i,SpringLayout.NORTH,p);
         layout.putConstraint(SpringLayout.EAST,ccp,0,SpringLayout.EAST,p);
         layout.putConstraint(SpringLayout.WEST,ccp,0,SpringLayout.WEST,p);
         layout.putConstraint(SpringLayout.SOUTH,ccp,hpHeight * (i+1),SpringLayout.NORTH,p);

         p.add(ccp);
         ccpList_.add(ccp);
      }
   }


   private synchronized void updateChannelControls() {
      for (ChannelControlPanel ccp:ccpList_) {
         ccp.updateChannelSettings();
      }

   }

}
