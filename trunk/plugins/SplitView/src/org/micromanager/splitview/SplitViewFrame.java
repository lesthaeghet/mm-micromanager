

/*
 * SplitViewFrame.java
 *
 * Created on Aug 28, 2011, 9:41:57 PM
 */

package org.micromanager.splitview;

import java.text.NumberFormat;
import java.util.prefs.Preferences;
import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.api.DeviceControlGUI;



/**
 *
 * @author nico
 */
public class SplitViewFrame extends javax.swing.JFrame {

   private final ScriptInterface gui_;
   private final DeviceControlGUI dGui_;
   private final CMMCore core_;
   private Preferences prefs_;
   private NumberFormat nf_;


    public SplitViewFrame(ScriptInterface gui) throws Exception {
       gui_ = gui;
       dGui_ = (DeviceControlGUI) gui_;
       core_ = gui_.getMMCore();
       nf_ = NumberFormat.getInstance();
       prefs_ = Preferences.userNodeForPackage(this.getClass());

       initComponents();
   }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 400, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 300, Short.MAX_VALUE)
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new SplitViewFrame().setVisible(true);
            }
        });
    }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   // End of variables declaration//GEN-END:variables

}
