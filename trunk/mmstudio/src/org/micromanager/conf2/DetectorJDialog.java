/*
 * DetectorJDialog.java
 *
 * Created on Feb 14, 2011, 9:48:28 AM
 */

package org.micromanager.conf2;

import org.micromanager.MMStudio;

/**
 *
 * @author karlhoover
 */
public class DetectorJDialog extends javax.swing.JDialog {

    public boolean CancelRequest(){
        return cancelRequest_;
    }

    public void ProgressText(final String t){

        detectionTextPane_.setText(t);

        
    }

    public String ProgressText(){
        return detectionTextPane_.getText();
    }

    private boolean cancelRequest_;


    /** Creates new form DetectorJDialog */
    public DetectorJDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        cancelRequest_ = false;
        setBackground(MMStudio.getInstance().getBackgroundColor());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      cancelButton_ = new javax.swing.JButton();
      jScrollPane2 = new javax.swing.JScrollPane();
      detectionTextPane_ = new javax.swing.JTextPane();

      addComponentListener(new java.awt.event.ComponentAdapter() {
         public void componentShown(java.awt.event.ComponentEvent evt) {
            formComponentShown(evt);
         }
      });

      cancelButton_.setText("Cancel");
      cancelButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButton_ActionPerformed(evt);
         }
      });

      jScrollPane2.setViewportView(detectionTextPane_);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(cancelButton_)
               .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(17, 17, 17)
            .addComponent(cancelButton_)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void cancelButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButton_ActionPerformed

        System.out.print("cancelButton_ActionPerformed");
        cancelRequest_ = true;

    }//GEN-LAST:event_cancelButton_ActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        cancelRequest_ = false;
    }//GEN-LAST:event_formComponentShown

 

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelButton_;
   private javax.swing.JTextPane detectionTextPane_;
   private javax.swing.JScrollPane jScrollPane2;
   // End of variables declaration//GEN-END:variables

}
