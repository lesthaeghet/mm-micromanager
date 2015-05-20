/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Dialog;
import java.awt.Window;
import propsandcovariants.DeviceControlChooserTableModel;
import java.util.prefs.Preferences;
import javax.swing.JDialog;

/**
 *
 * @author Henry
 */
public class PickPropertiesGUI extends javax.swing.JFrame {

   private DeviceControlChooserTableModel propChooserModel_;
   private GUI parentGUI_;
   
   /**
    * Creates new form PickPropertiesGUI
    */
   public PickPropertiesGUI(Preferences prefs, GUI parentGUI) {
      propChooserModel_ = new DeviceControlChooserTableModel(prefs);
      initComponents();
      this.setVisible(true);
      //set column sizes
      propertiesTable_.getColumnModel().getColumn(0).setMaxWidth(60);
      this.setLocationRelativeTo(null);
      parentGUI_ = parentGUI;
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jScrollPane1 = new javax.swing.JScrollPane();
      propertiesTable_ = new javax.swing.JTable();
      okButton_ = new javax.swing.JButton();
      cancelButton_ = new javax.swing.JButton();
      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Select properties");

      propertiesTable_.setModel( propChooserModel_
      );
      jScrollPane1.setViewportView(propertiesTable_);

      okButton_.setText("Ok");
      okButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            okButton_ActionPerformed(evt);
         }
      });

      cancelButton_.setText("Cancel");
      cancelButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButton_ActionPerformed(evt);
         }
      });

      jPanel1.setBackground(new java.awt.Color(200, 255, 200));

      jLabel1.setText("Entries will appear in the Device status/control panel in the order that they are selected here");

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jLabel1)
      );

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cancelButton_)
            .addGap(47, 47, 47)
            .addComponent(okButton_)
            .addGap(271, 271, 271))
         .addGroup(layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 665, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
         .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(cancelButton_)
               .addComponent(okButton_))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   private void okButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButton_ActionPerformed
      propChooserModel_.storeProperties();
      this.setVisible(false);
      this.dispose();     
      parentGUI_.updatePropertiesTable();
   }//GEN-LAST:event_okButton_ActionPerformed

   private void cancelButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButton_ActionPerformed
      this.setVisible(false);
      this.dispose();
   }//GEN-LAST:event_cancelButton_ActionPerformed


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelButton_;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JButton okButton_;
   private javax.swing.JTable propertiesTable_;
   // End of variables declaration//GEN-END:variables
}
