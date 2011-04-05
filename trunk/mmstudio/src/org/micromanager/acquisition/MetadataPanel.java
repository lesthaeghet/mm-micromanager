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
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.table.AbstractTableModel;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.utils.ImageFocusListener;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.HistogramUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.ScaleBar;

/**
 *
 * @author arthur
 */
public class MetadataPanel extends javax.swing.JPanel
        implements ImageListener, ImageFocusListener {

   private static MetadataPanel singletonViewer_ = null;

   private final MetadataTableModel imageMetadataModel_;
   private final MetadataTableModel summaryMetadataModel_;
   private final String [] columnNames_ = {"Property","Value"};
   private boolean showUnchangingKeys_;
   private boolean updatingDisplayModeCombo_ = false;
   private ArrayList<ChannelControlPanel> ccpList_;
   private Color overlayColor_ = Color.white;
   private double fractionOutliersToReject_;

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
      
      HistogramUtils defaultHistogram = new HistogramUtils(null);
      // start out with a fraction that represents 3 sigma in normal distribution
      fractionOutliersToReject_ = defaultHistogram.getFractionToReject();
      SpinnerModel smodel = new SpinnerNumberModel(100.*fractionOutliersToReject_,0.,1.,.01);
      rejectPercentSpinner_.setModel(smodel);
      rejectOutliersCB_.setEnabled(autostretchCheckBox.isSelected());
      rejectPercentSpinner_.setEnabled(rejectOutliersCB_.isSelected() && autostretchCheckBox.isSelected());



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
      autostretchCheckBox = new javax.swing.JCheckBox();
      rejectOutliersCB_ = new javax.swing.JCheckBox();
      rejectPercentSpinner_ = new javax.swing.JSpinner();
      sizeBarCheckBox = new javax.swing.JCheckBox();
      sizeBarComboBox = new javax.swing.JComboBox();
      overlayColorComboBox_ = new javax.swing.JComboBox();
      logScaleCheckBox = new javax.swing.JCheckBox();
      metadataSplitPane = new javax.swing.JSplitPane();
      imageMetadataScrollPane = new javax.swing.JPanel();
      imageMetadataTableScrollPane = new javax.swing.JScrollPane();
      imageMetadataTable = new javax.swing.JTable();
      showUnchangingPropertiesCheckbox = new javax.swing.JCheckBox();
      jLabel2 = new javax.swing.JLabel();
      summaryMetadataPanel = new javax.swing.JPanel();
      summaryMetadataScrollPane = new javax.swing.JScrollPane();
      summaryMetadataTable = new javax.swing.JTable();
      jLabel3 = new javax.swing.JLabel();
      CommentsSplitPane = new javax.swing.JSplitPane();
      summaryCommentsPane = new javax.swing.JPanel();
      summaryCommentsLabel = new javax.swing.JLabel();
      summaryCommentsScrollPane = new javax.swing.JScrollPane();
      summaryCommentsTextArea = new javax.swing.JTextArea();
      imageCommentsPanel = new javax.swing.JPanel();
      imageCommentsLabel = new javax.swing.JLabel();
      imageCommentsScrollPane = new javax.swing.JScrollPane();
      imageCommentsTextArea = new javax.swing.JTextArea();

      tabbedPane.setToolTipText("Examine and adjust display settings, metadata, and comments for the multi-dimensional acquisition in the frontmost window.");
      tabbedPane.setFocusable(false);
      tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            tabbedPaneStateChanged(evt);
         }
      });

      contrastScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      displayModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Composite", "Color", "Grayscale" }));
      displayModeCombo.setToolTipText("<html>Choose display mode:<br> - Composite = Multicolor overlay<br> - Color = Single channel color view<br> - Grayscale = Single channel grayscale view</li></ul></html>");
      displayModeCombo.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            displayModeComboActionPerformed(evt);
         }
      });

      jLabel1.setText("Display mode:");

      autostretchCheckBox.setText("Autostretch");
      autostretchCheckBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            autostretchCheckBoxActionPerformed(evt);
         }
      });

      rejectOutliersCB_.setText("ignore %");
      rejectOutliersCB_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            rejectOutliersCB_ActionPerformed(evt);
         }
      });

      rejectPercentSpinner_.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            rejectPercentSpinner_StateChanged(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(24, 24, 24)
            .add(jLabel1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(displayModeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(autostretchCheckBox)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(rejectOutliersCB_)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(rejectPercentSpinner_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(36, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
            .add(autostretchCheckBox)
            .add(rejectOutliersCB_)
            .add(rejectPercentSpinner_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         .add(displayModeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         .add(jPanel1Layout.createSequentialGroup()
            .add(4, 4, 4)
            .add(jLabel1))
      );

      sizeBarCheckBox.setText("Scale Bar");
      sizeBarCheckBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            sizeBarCheckBoxActionPerformed(evt);
         }
      });

      sizeBarComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right" }));
      sizeBarComboBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            sizeBarComboBoxActionPerformed(evt);
         }
      });

      overlayColorComboBox_.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "White", "Black", "Yellow", "Gray" }));
      overlayColorComboBox_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            overlayColorComboBox_ActionPerformed(evt);
         }
      });

      logScaleCheckBox.setText("Log hist");
      logScaleCheckBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            logScaleCheckBoxActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout ChannelsTablePanelLayout = new org.jdesktop.layout.GroupLayout(ChannelsTablePanel);
      ChannelsTablePanel.setLayout(ChannelsTablePanelLayout);
      ChannelsTablePanelLayout.setHorizontalGroup(
         ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(ChannelsTablePanelLayout.createSequentialGroup()
            .add(sizeBarCheckBox)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(sizeBarComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(overlayColorComboBox_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(214, 214, 214))
         .add(ChannelsTablePanelLayout.createSequentialGroup()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(logScaleCheckBox)
            .add(134, 134, 134))
         .add(org.jdesktop.layout.GroupLayout.TRAILING, contrastScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
      );
      ChannelsTablePanelLayout.setVerticalGroup(
         ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(ChannelsTablePanelLayout.createSequentialGroup()
            .add(ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(ChannelsTablePanelLayout.createSequentialGroup()
                  .add(ChannelsTablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(sizeBarCheckBox)
                     .add(sizeBarComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(overlayColorComboBox_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                  .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(logScaleCheckBox))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(contrastScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))
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
      imageMetadataTable.setToolTipText("Metadata tags for each individual image");
      imageMetadataTable.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
      imageMetadataTable.setDoubleBuffered(true);
      imageMetadataTableScrollPane.setViewportView(imageMetadataTable);

      showUnchangingPropertiesCheckbox.setText("Show unchanging properties");
      showUnchangingPropertiesCheckbox.setToolTipText("Show/hide properties that are the same for all images in the acquisition");
      showUnchangingPropertiesCheckbox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            showUnchangingPropertiesCheckboxActionPerformed(evt);
         }
      });

      jLabel2.setText("Per-image properties");

      org.jdesktop.layout.GroupLayout imageMetadataScrollPaneLayout = new org.jdesktop.layout.GroupLayout(imageMetadataScrollPane);
      imageMetadataScrollPane.setLayout(imageMetadataScrollPaneLayout);
      imageMetadataScrollPaneLayout.setHorizontalGroup(
         imageMetadataScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageMetadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, imageMetadataScrollPaneLayout.createSequentialGroup()
            .add(jLabel2)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 410, Short.MAX_VALUE)
            .add(showUnchangingPropertiesCheckbox))
      );
      imageMetadataScrollPaneLayout.setVerticalGroup(
         imageMetadataScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageMetadataScrollPaneLayout.createSequentialGroup()
            .add(imageMetadataScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(showUnchangingPropertiesCheckbox)
               .add(jLabel2))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(imageMetadataTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
      );

      metadataSplitPane.setRightComponent(imageMetadataScrollPane);

      summaryMetadataPanel.setMinimumSize(new java.awt.Dimension(0, 100));
      summaryMetadataPanel.setPreferredSize(new java.awt.Dimension(539, 100));

      summaryMetadataScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
      summaryMetadataScrollPane.setPreferredSize(new java.awt.Dimension(454, 80));

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
      summaryMetadataTable.setToolTipText("Metadata tags for the whole acquisition");
      summaryMetadataScrollPane.setViewportView(summaryMetadataTable);

      jLabel3.setText("Acquisition properties");

      org.jdesktop.layout.GroupLayout summaryMetadataPanelLayout = new org.jdesktop.layout.GroupLayout(summaryMetadataPanel);
      summaryMetadataPanel.setLayout(summaryMetadataPanelLayout);
      summaryMetadataPanelLayout.setHorizontalGroup(
         summaryMetadataPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryMetadataScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
         .add(summaryMetadataPanelLayout.createSequentialGroup()
            .add(jLabel3)
            .addContainerGap())
      );
      summaryMetadataPanelLayout.setVerticalGroup(
         summaryMetadataPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryMetadataPanelLayout.createSequentialGroup()
            .add(jLabel3)
            .add(4, 4, 4)
            .add(summaryMetadataScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
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
      summaryCommentsTextArea.setToolTipText("Enter your comments for the whole acquisition here");
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
            .addContainerGap(565, Short.MAX_VALUE))
         .add(summaryCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
      );
      summaryCommentsPaneLayout.setVerticalGroup(
         summaryCommentsPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(summaryCommentsPaneLayout.createSequentialGroup()
            .add(summaryCommentsLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(summaryCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
      );

      CommentsSplitPane.setLeftComponent(summaryCommentsPane);

      imageCommentsPanel.setPreferredSize(new java.awt.Dimension(500, 300));

      imageCommentsLabel.setText("Per-image comments:");

      imageCommentsTextArea.setColumns(20);
      imageCommentsTextArea.setLineWrap(true);
      imageCommentsTextArea.setRows(1);
      imageCommentsTextArea.setTabSize(3);
      imageCommentsTextArea.setToolTipText("Comments for each image may be entered here.");
      imageCommentsTextArea.setWrapStyleWord(true);
      imageCommentsTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            imageCommentsTextAreaFocusLost(evt);
         }
      });
      imageCommentsScrollPane.setViewportView(imageCommentsTextArea);

      org.jdesktop.layout.GroupLayout imageCommentsPanelLayout = new org.jdesktop.layout.GroupLayout(imageCommentsPanel);
      imageCommentsPanel.setLayout(imageCommentsPanelLayout);
      imageCommentsPanelLayout.setHorizontalGroup(
         imageCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageCommentsPanelLayout.createSequentialGroup()
            .add(imageCommentsLabel)
            .add(400, 400, 400))
         .add(imageCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
      );
      imageCommentsPanelLayout.setVerticalGroup(
         imageCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(imageCommentsPanelLayout.createSequentialGroup()
            .add(imageCommentsLabel)
            .add(0, 0, 0)
            .add(imageCommentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
      );

      CommentsSplitPane.setRightComponent(imageCommentsPanel);

      tabbedPane.addTab("Comments", CommentsSplitPane);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(tabbedPane)
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void displayModeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayModeComboActionPerformed
       if (!updatingDisplayModeCombo_) {
          setDisplayState(displayModeCombo.getSelectedIndex()+1);
       }
}//GEN-LAST:event_displayModeComboActionPerformed

    private void summaryCommentsTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_summaryCommentsTextAreaFocusLost
      writeSummaryComments(WindowManager.getCurrentImage());
}//GEN-LAST:event_summaryCommentsTextAreaFocusLost

    private void imageCommentsTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imageCommentsTextAreaFocusLost
      writeImageComments(WindowManager.getCurrentImage());
}//GEN-LAST:event_imageCommentsTextAreaFocusLost

    private void showUnchangingPropertiesCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUnchangingPropertiesCheckboxActionPerformed
       showUnchangingKeys_ = showUnchangingPropertiesCheckbox.isSelected();
       update(WindowManager.getCurrentImage());
}//GEN-LAST:event_showUnchangingPropertiesCheckboxActionPerformed

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
       try {
          update(WindowManager.getCurrentImage());
       } catch (Exception e) {
       }
}//GEN-LAST:event_tabbedPaneStateChanged

    private void sizeBarCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeBarCheckBoxActionPerformed
       showSizeBar();
    }//GEN-LAST:event_sizeBarCheckBoxActionPerformed

    private void sizeBarComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeBarComboBoxActionPerformed
       showSizeBar();
    }//GEN-LAST:event_sizeBarComboBoxActionPerformed

    private void overlayColorComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overlayColorComboBox_ActionPerformed
       if ((overlayColorComboBox_.getSelectedItem()).equals("Black")) {
          overlayColor_ = Color.black;
       } else  if ((overlayColorComboBox_.getSelectedItem()).equals("White")) {
          overlayColor_ = Color.white;
       } else if ((overlayColorComboBox_.getSelectedItem()).equals("Yellow")) {
          overlayColor_ = Color.yellow;
       } else if ((overlayColorComboBox_.getSelectedItem()).equals("Gray")) {
          overlayColor_ = Color.gray;
       }
       showSizeBar();
       
    }//GEN-LAST:event_overlayColorComboBox_ActionPerformed

    private void autostretchCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autostretchCheckBoxActionPerformed
       rejectOutliersCB_.setEnabled(autostretchCheckBox.isSelected());
       boolean rejectem = rejectOutliersCB_.isSelected() && autostretchCheckBox.isSelected();
       rejectPercentSpinner_.setEnabled(rejectem);
       fractionOutliersToReject_ = 0.01*(Double)rejectPercentSpinner_.getValue();
       for (ChannelControlPanel ccp:ccpList_) {
          ccp.setFractionToReject(fractionOutliersToReject_);
          ccp.setRejectOutliers(rejectem);
          ccp.setAutostretch(autostretchCheckBox.isSelected());
          ccp.drawDisplaySettings();
       }
       

    }//GEN-LAST:event_autostretchCheckBoxActionPerformed

    private void logScaleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logScaleCheckBoxActionPerformed
       for (ChannelControlPanel ccp:ccpList_) {
          ccp.setLogScale(logScaleCheckBox.isSelected());
       }
    }//GEN-LAST:event_logScaleCheckBoxActionPerformed

    private void rejectOutliersCB_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectOutliersCB_ActionPerformed
      JCheckBox cb = (JCheckBox)evt.getSource();
       rejectPercentSpinner_.setEnabled(cb.isSelected() && autostretchCheckBox.isSelected());
    }//GEN-LAST:event_rejectOutliersCB_ActionPerformed

    private void rejectPercentSpinner_StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rejectPercentSpinner_StateChanged
       JSpinner sp = (JSpinner)evt.getSource();
        Double currentValue = (Double)sp.getValue(); //

    }//GEN-LAST:event_rejectPercentSpinner_StateChanged



   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel ChannelsTablePanel;
   private javax.swing.JSplitPane CommentsSplitPane;
   private javax.swing.JCheckBox autostretchCheckBox;
   private javax.swing.JScrollPane contrastScrollPane;
   private javax.swing.JComboBox displayModeCombo;
   private javax.swing.JLabel imageCommentsLabel;
   private javax.swing.JPanel imageCommentsPanel;
   private javax.swing.JScrollPane imageCommentsScrollPane;
   private javax.swing.JTextArea imageCommentsTextArea;
   private javax.swing.JPanel imageMetadataScrollPane;
   private javax.swing.JTable imageMetadataTable;
   private javax.swing.JScrollPane imageMetadataTableScrollPane;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JCheckBox logScaleCheckBox;
   private javax.swing.JSplitPane metadataSplitPane;
   private javax.swing.JComboBox overlayColorComboBox_;
   private javax.swing.JCheckBox rejectOutliersCB_;
   private javax.swing.JSpinner rejectPercentSpinner_;
   private javax.swing.JCheckBox showUnchangingPropertiesCheckbox;
   private javax.swing.JCheckBox sizeBarCheckBox;
   private javax.swing.JComboBox sizeBarComboBox;
   private javax.swing.JLabel summaryCommentsLabel;
   private javax.swing.JPanel summaryCommentsPane;
   private javax.swing.JScrollPane summaryCommentsScrollPane;
   private javax.swing.JTextArea summaryCommentsTextArea;
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

   void setAutostretch(boolean state) {
      autostretchCheckBox.setSelected(state);
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
                  //ReportingUtils.logError(ex);
               }
               addRow(rowData);
            }
         }
         fireTableDataChanged();
      }
   }

   private JSONObject selectChangingTags(ImagePlus imgp, JSONObject md) {
      JSONObject mdChanging = new JSONObject();
      MMImageCache cache = getCache(imgp);
      if (cache != null) {
         for (String key : cache.getChangingKeys()) {
            if (md.has(key)) {
               try {
                  mdChanging.put(key, md.get(key));
               } catch (JSONException ex) {
                  try {
                     mdChanging.put(key, "");
                     //ReportingUtils.logError(ex);
                  } catch (JSONException ex1) {
                     ReportingUtils.logError(ex1);
                  }
               }
            }
         }
      }
      return mdChanging;
   }

   private boolean isHyperImage(ImagePlus imgp) {
      return VirtualAcquisitionDisplay.getDisplay(imgp) != null;
   }

   private AcquisitionVirtualStack getAcquisitionStack(ImagePlus imp) {
      VirtualAcquisitionDisplay display
              = VirtualAcquisitionDisplay.getDisplay(imp);
      if (display != null) {
         return display.virtualStack_;
      } else{
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
      writeSummaryComments(imp);
      if (WindowManager.getCurrentWindow() == null) {
         update((ImagePlus) null);
      } else {
         imageUpdated(WindowManager.getCurrentImage());
      }
   }

   private void writeSummaryComments(ImagePlus imp) {
       VirtualAcquisitionDisplay acq = getVirtualAcquisitionDisplay(imp);
       if (acq != null) {
          acq.setSummaryComment(summaryCommentsTextArea.getText());
       }
   }

   private void writeImageComments(ImagePlus imgp) {
       VirtualAcquisitionDisplay acq = getVirtualAcquisitionDisplay(imgp);
       if (acq != null) {
          acq.setImageComment(imageCommentsTextArea.getText());
       }
   }


   //Implements ImageListener
   public void imageUpdated(ImagePlus imp) {
      ImageWindow win = imp.getWindow();
      if (win instanceof StackWindow) {
         if (((StackWindow) win).getAnimate())
            return;
      }
      if (isHyperImage(imp))
         update(imp);
   }

   private MMImageCache getCache(ImagePlus imgp) {
      if ( VirtualAcquisitionDisplay.getDisplay(imgp) != null) {
         return VirtualAcquisitionDisplay.getDisplay(imgp).imageCache_;
      } else {
         return null;
      }
   }

   /*
    * update(ImagePlus imp) is called every time the image is changed
    * or the sliders have moved.
    */
   public void update(ImagePlus imp) {
      int tabSelected = tabbedPane.getSelectedIndex();
      if (imp == null) {
         imageMetadataModel_.setMetadata(null);
         summaryMetadataModel_.setMetadata(null);
         summaryCommentsTextArea.setText(null);
         contrastScrollPane.setViewportView(null);
         ccpList_ = null;
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
                     md = selectChangingTags(imp, md);
                  imageMetadataModel_.setMetadata(md);
               }
               summaryMetadataModel_.setMetadata(stack.getCache()
                       .getSummaryMetadata());
            } else {
               imageMetadataModel_.setMetadata(null);
            }
         } else if (tabSelected == 0) {
            updateChannelControls();
         } else if (tabSelected == 2) {
            VirtualAcquisitionDisplay acq = getVirtualAcquisitionDisplay(imp);
            if (acq != null) {
               imageCommentsTextArea.setText(acq.getImageComment());
            }
         }
      }
      
   }

   private void showSizeBar() {
      boolean show = sizeBarCheckBox.isSelected();
      ImagePlus ip = WindowManager.getCurrentImage();
      if (show) {
         ScaleBar sizeBar = new ScaleBar(ip);

         if (sizeBar != null) {
            Overlay ol = new Overlay();
            //ol.setFillColor(Color.white); // this causes the text to get a white background!
            ol.setStrokeColor(overlayColor_);
            String selected = (String) sizeBarComboBox.getSelectedItem();
            if (selected.equals("Top-Right"))
               sizeBar.setPosition(ScaleBar.Position.TOPRIGHT);
            if (selected.equals("Top-Left"))
               sizeBar.setPosition(ScaleBar.Position.TOPLEFT);
            if (selected.equals("Bottom-Right"))
               sizeBar.setPosition(ScaleBar.Position.BOTTOMRIGHT);
            if (selected.equals("Bottom-Left"))
               sizeBar.setPosition(ScaleBar.Position.BOTTOMLEFT);
            sizeBar.addToOverlay(ol);
            ol.setStrokeColor(overlayColor_);
            ip.setOverlay(ol);
         }
      }
      ip.setHideOverlay(!show);
   }

   //Implements AWTEventListener
   /*
    * This is called, in contrast to update(), only when the ImageWindow
    * in focus has changed.
    */
   public void focusReceived(ImageWindow focusedWindow) {
      if (focusedWindow == null) {
         return;
      }

      ImagePlus imgp = focusedWindow.getImagePlus();
      MMImageCache cache = getCache(imgp);
      VirtualAcquisitionDisplay acq = getVirtualAcquisitionDisplay(imgp);
      sizeBarCheckBox.setSelected(imgp.getOverlay()!= null && !imgp.getHideOverlay());

      if (acq != null) {
         summaryCommentsTextArea.setText(acq.getSummaryComment());
         JSONObject md = cache.getSummaryMetadata();
         summaryMetadataModel_.setMetadata(md);
      } else {
         summaryCommentsTextArea.setText(null);
      }

      if (imgp instanceof CompositeImage) {
         CompositeImage cimp = (CompositeImage) imgp;
         updatingDisplayModeCombo_ = true;
         displayModeCombo.setSelectedIndex(cimp.getMode() - 1);
         updatingDisplayModeCombo_ = false;
      }
      if (acq != null) {
         setupChannelControls(acq);
         update(imgp);
      }

   }

   private VirtualAcquisitionDisplay getVirtualAcquisitionDisplay(ImagePlus imgp) {
      if (imgp == null)
         return null;
      return VirtualAcquisitionDisplay.getDisplay(imgp);
   }

   public synchronized void setupChannelControls(VirtualAcquisitionDisplay acq) {
      int hpHeight = 110;
      int nChannels = acq.getNumGrayChannels();

      JPanel p = new JPanel();
      p.setPreferredSize(new Dimension(200,nChannels * hpHeight));
      contrastScrollPane.setViewportView(p);
      SpringLayout layout = new SpringLayout();
      p.setLayout(layout);
      ccpList_ = new ArrayList<ChannelControlPanel>();

      for (int i=0;i<nChannels;++i) {
         ChannelControlPanel ccp = new ChannelControlPanel(acq, i, this);

         layout.putConstraint(SpringLayout.NORTH,ccp,hpHeight*i,SpringLayout.NORTH,p);
         layout.putConstraint(SpringLayout.EAST,ccp,0,SpringLayout.EAST,p);
         layout.putConstraint(SpringLayout.WEST,ccp,0,SpringLayout.WEST,p);
         layout.putConstraint(SpringLayout.SOUTH,ccp,hpHeight * (i+1),SpringLayout.NORTH,p);

          ccp.setFractionToReject(fractionOutliersToReject_);
          ccp.setRejectOutliers(rejectOutliersCB_.isSelected() && autostretchCheckBox.isSelected());



         p.add(ccp);
         ccpList_.add(ccp);
      }
   }


   private synchronized void updateChannelControls() {
      if (ccpList_ == null)
         return;
      
      for (ChannelControlPanel ccp:ccpList_) {
         ccp.updateChannelSettings();
      }

   }

}
