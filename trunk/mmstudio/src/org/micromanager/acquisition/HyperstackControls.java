/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HyperstackControls.java
 *
 * Created on Jul 15, 2010, 2:54:37 PM
 */

package org.micromanager.acquisition;

import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageWindow;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class HyperstackControls extends java.awt.Panel implements ImageListener {
   private final VirtualAcquisitionDisplay acq_;
   
    /** Creates new form HyperstackControls */
    public HyperstackControls(VirtualAcquisitionDisplay acq) {
        initComponents();
        acq_ = acq;
        fpsField.setText(NumberUtils.doubleToDisplayString(acq_.getPlaybackFPS()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      showFolderButton = new javax.swing.JButton();
      saveButton = new javax.swing.JButton();
      fpsField = new javax.swing.JTextField();
      fpsLabel = new javax.swing.JLabel();
      abortButton = new javax.swing.JButton();
      statusLineLabel = new javax.swing.JLabel();
      pauseAndResumeToggleButton = new javax.swing.JToggleButton();

      setPreferredSize(new java.awt.Dimension(512, 30));

      showFolderButton.setBackground(new java.awt.Color(255, 255, 255));
      showFolderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/folder.png"))); // NOI18N
      showFolderButton.setToolTipText("Show containing folder");
      showFolderButton.setFocusable(false);
      showFolderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      showFolderButton.setMaximumSize(new java.awt.Dimension(30, 28));
      showFolderButton.setMinimumSize(new java.awt.Dimension(30, 28));
      showFolderButton.setPreferredSize(new java.awt.Dimension(30, 28));
      showFolderButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      showFolderButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            showFolderButtonActionPerformed(evt);
         }
      });

      saveButton.setBackground(new java.awt.Color(255, 255, 255));
      saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/disk.png"))); // NOI18N
      saveButton.setToolTipText("Save as...");
      saveButton.setFocusable(false);
      saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      saveButton.setMaximumSize(new java.awt.Dimension(30, 28));
      saveButton.setMinimumSize(new java.awt.Dimension(30, 28));
      saveButton.setPreferredSize(new java.awt.Dimension(30, 28));
      saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      saveButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveButtonActionPerformed(evt);
         }
      });

      fpsField.setToolTipText("Set the speed at which the acquisition is played back.");
      fpsField.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            fpsFieldFocusLost(evt);
         }
      });
      fpsField.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyReleased(java.awt.event.KeyEvent evt) {
            fpsFieldKeyReleased(evt);
         }
      });

      fpsLabel.setText("playback fps:");
      fpsLabel.setFocusable(false);

      abortButton.setBackground(new java.awt.Color(255, 255, 255));
      abortButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/cancel.png"))); // NOI18N
      abortButton.setToolTipText("Stop acquisition");
      abortButton.setFocusable(false);
      abortButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      abortButton.setMaximumSize(new java.awt.Dimension(30, 28));
      abortButton.setMinimumSize(new java.awt.Dimension(30, 28));
      abortButton.setPreferredSize(new java.awt.Dimension(30, 28));
      abortButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      abortButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            abortButtonActionPerformed(evt);
         }
      });

      statusLineLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      statusLineLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

      pauseAndResumeToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/control_pause.png"))); // NOI18N
      pauseAndResumeToggleButton.setToolTipText("Pause acquisition");
      pauseAndResumeToggleButton.setFocusable(false);
      pauseAndResumeToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
      pauseAndResumeToggleButton.setMaximumSize(new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton.setMinimumSize(new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton.setPreferredSize(new java.awt.Dimension(30, 28));
      pauseAndResumeToggleButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/resultset_next.png"))); // NOI18N
      pauseAndResumeToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/resultset_next.png"))); // NOI18N
      pauseAndResumeToggleButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            pauseAndResumeToggleButtonActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(showFolderButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(layout.createSequentialGroup()
                  .add(30, 30, 30)
                  .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(abortButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(0, 0, 0)
            .add(pauseAndResumeToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(fpsLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(fpsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(statusLineLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 303, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
               .add(org.jdesktop.layout.GroupLayout.LEADING, statusLineLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, showFolderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, saveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                  .add(fpsLabel)
                  .add(fpsField))
               .add(org.jdesktop.layout.GroupLayout.LEADING, abortButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, pauseAndResumeToggleButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(20, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents

   private void showFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFolderButtonActionPerformed
      acq_.showFolder();
   }//GEN-LAST:event_showFolderButtonActionPerformed

   private void fpsFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fpsFieldFocusLost
      updateFPS();
   }//GEN-LAST:event_fpsFieldFocusLost

   private void fpsFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fpsFieldKeyReleased
      updateFPS();
   }//GEN-LAST:event_fpsFieldKeyReleased

   private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
      acq_.abort();
   }//GEN-LAST:event_abortButtonActionPerformed

   private void pauseAndResumeToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseAndResumeToggleButtonActionPerformed
      acq_.pause();
}//GEN-LAST:event_pauseAndResumeToggleButtonActionPerformed

   private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
      acq_.saveAs();
   }//GEN-LAST:event_saveButtonActionPerformed

   private void updateFPS() {
      try {
         double fps = NumberUtils.displayStringToDouble(fpsField.getText());
         acq_.setPlaybackFPS(fps);
      } catch (ParseException ex) {}
   }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton abortButton;
   private javax.swing.JTextField fpsField;
   private javax.swing.JLabel fpsLabel;
   private javax.swing.JToggleButton pauseAndResumeToggleButton;
   private javax.swing.JButton saveButton;
   private javax.swing.JButton showFolderButton;
   private javax.swing.JLabel statusLineLabel;
   // End of variables declaration//GEN-END:variables

   public void imageOpened(ImagePlus ip) {}

   public void imageClosed(ImagePlus ip) {}

   public synchronized void setStatusLabel(String text) {
      statusLineLabel.setText(text);
   }

   private void updateStatusLine(TaggedImage taggedImg) {
      if (taggedImg != null) {
         String status = "";
         try {
            String xyPosition;
            try {
               xyPosition = taggedImg.tags.getString("PositionName");
               status += xyPosition + ", ";
            } catch (Exception e) {
               //Oh well...
            }

            try {
               String time = NumberUtils.doubleToDisplayString(taggedImg.tags.getDouble("ElapsedTime-ms"));
               status += time + " ms";
            } catch (JSONException ex) {
               ReportingUtils.logError("MetaData did not contain ElapsedTime-ms field");
            }
            
            String zPosition;
            try {
               zPosition = NumberUtils.doubleStringCoreToDisplay(taggedImg.tags.getString("ZPositionUm"));
               status += ", z: " + zPosition + " um";
            } catch (Exception e) {
               try {
                  zPosition = NumberUtils.doubleStringCoreToDisplay(taggedImg.tags.getString("Z-um"));
                                 status += ", z: " + zPosition + " um";
               } catch (Exception e1) {
                  // Do nothing...
               }
            }
            String chan;
            try {
               chan = MDUtils.getChannelName(taggedImg.tags);
               status += ", " + chan;
            } catch (Exception ex) {
            }

            setStatusLabel(status);
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
         }
      }
   }

   public void imageUpdated(ImagePlus imp) {
      if (imp == acq_.getHyperImage()) {
         ImageStack stack = imp.getStack();
         if (stack instanceof AcquisitionVirtualStack) {
            AcquisitionVirtualStack vstack = (AcquisitionVirtualStack) imp.getStack();
            int slice = imp.getCurrentSlice();
            final TaggedImage taggedImg = vstack.getTaggedImage(slice);
            updateStatusLine(taggedImg);
            try {
               if (acq_.acquisitionIsRunning() && acq_.getNextWakeTime() > 0) {
                  final long nextImageTime = acq_.getNextWakeTime();
                  if (System.nanoTime() / 1000000 < nextImageTime) {
                     final Timer timer = new Timer();
                     TimerTask task = new TimerTask() {

                        public void run() {
                           double timeRemainingS = (nextImageTime - System.nanoTime() / 1000000) / 1000;
                           if (timeRemainingS > 0 && acq_.acquisitionIsRunning()) {
                              setStatusLabel("Next frame: " + NumberUtils.doubleToDisplayString(1+timeRemainingS) + " s");
                           } else {
                              timer.cancel();
                              setStatusLabel("");
                           }
                        }
                     };
                     timer.schedule(task, 2000, 100);
                  }
               }
               
            } catch (Exception ex) {
               ReportingUtils.logError(ex);
            }
         }
      }

   }

   public void enableAcquisitionControls(boolean state) {
      abortButton.setEnabled(state);
      pauseAndResumeToggleButton.setEnabled(state);
   }

   void enableShowFolderButton(boolean enabled) {
      showFolderButton.setEnabled(enabled);
   }

}
