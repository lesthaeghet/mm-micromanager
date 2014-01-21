
/*
 * ReportProblemDialog.java
 *
 * Created on Feb 3, 2011, 11:49:27 AM
 */
package org.micromanager;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author karlhoover
 */
public class ReportProblemDialog extends javax.swing.JDialog {

   String reportPreamble_;
   CMMCore core_;
   private ScriptInterface parent_;
   MMOptions mmoptions_;

   /** Creates new form ReportProblemDialog */
   public ReportProblemDialog(CMMCore c, ScriptInterface parentMMGUI, MMOptions mmoptions) {
      super();
      initComponents();
      reportPreamble_ = "";

      core_ = c;
      parent_ = parentMMGUI;
      mmoptions_ = mmoptions;
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
      descriptionPane_ = new javax.swing.JEditorPane();
      cancelButton_ = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      stepInstructions_ = new javax.swing.JTextPane();
      jLabel1 = new javax.swing.JLabel();
      name_ = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      organization_ = new javax.swing.JTextField();
      jLabel4 = new javax.swing.JLabel();
      emailText_ = new javax.swing.JTextField();
      clearButton_ = new javax.swing.JButton();
      sendButton_ = new javax.swing.JButton();

      setTitle("Report Problem Dialog");
      addComponentListener(new java.awt.event.ComponentAdapter() {
         public void componentShown(java.awt.event.ComponentEvent evt) {
            formComponentShown(evt);
         }
         public void componentHidden(java.awt.event.ComponentEvent evt) {
            formComponentHidden(evt);
         }
      });

      descriptionPane_.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 10));
      jScrollPane2.setViewportView(descriptionPane_);

      cancelButton_.setText("Cancel");
      cancelButton_.setToolTipText("Quit the Problem Report procedure.");
      cancelButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButton_ActionPerformed(evt);
         }
      });

      stepInstructions_.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
      stepInstructions_.setEditable(false);
      stepInstructions_.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12));
      jScrollPane1.setViewportView(stepInstructions_);

      jLabel1.setText("Problem Description");

      name_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            name_ActionPerformed(evt);
         }
      });
      name_.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
         public void propertyChange(java.beans.PropertyChangeEvent evt) {
            name_PropertyChange(evt);
         }
      });

      jLabel2.setText("Name");

      jLabel3.setText("Organization");

      organization_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            organization_ActionPerformed(evt);
         }
      });
      organization_.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
         public void propertyChange(java.beans.PropertyChangeEvent evt) {
            organization_PropertyChange(evt);
         }
      });

      jLabel4.setText("e-mail");

      emailText_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            emailText_ActionPerformed(evt);
         }
      });
      emailText_.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
         public void propertyChange(java.beans.PropertyChangeEvent evt) {
            emailText_PropertyChange(evt);
         }
      });

      clearButton_.setText("Restart Log!");
      clearButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            clearButton_ActionPerformed(evt);
         }
      });

      sendButton_.setText("Send");
      sendButton_.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            sendButton_ActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel2)
                  .add(102, 102, 102)
                  .add(name_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE))
               .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE))
               .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel3)
                     .add(jLabel4))
                  .add(57, 57, 57)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(emailText_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                     .add(organization_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)))
               .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel1)
                  .add(14, 14, 14)
                  .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
               .add(layout.createSequentialGroup()
                  .add(88, 88, 88)
                  .add(cancelButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(52, 52, 52)
                  .add(clearButton_)
                  .add(69, 69, 69)
                  .add(sendButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel2)
               .add(name_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(organization_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel4)
               .add(emailText_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 153, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(layout.createSequentialGroup()
                  .add(69, 69, 69)
                  .add(jLabel1)))
            .add(18, 18, 18)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(sendButton_)
               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                  .add(cancelButton_)
                  .add(clearButton_)))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown

       InitializeDialog();

    }//GEN-LAST:event_formComponentShown
/*
   private boolean userInputValid() {
      boolean result = false;
      String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
      if (emailText_.getText().matches(EMAIL_REGEX)) {
         if (0 < name_.getText().length()) {
            if (0 < organization_.getText().length()) {
               result = true;
            }
         }
      }

      return result;
   }

 */

   private void buildAndSendReport() {
      reportPreamble_ = "#User Name: " + name_.getText();
      reportPreamble_ += ("\n#Organization: " + organization_.getText());
      reportPreamble_ += ("\n#User e-mail: " + emailText_.getText());
      reportPreamble_ += ("\n#User Description: " + descriptionPane_.getText());
      descriptionPane_.setEnabled(false);
      descriptionPane_.setEditable(false);
      sendButton_.setEnabled(false);
      stepInstructions_.setText("Sending...");

      core_.logMessage("User requested to send problem report");
      try {
         logExtraInformation(false);
      }
      catch (Exception e) {
         core_.logMessage("Exception while logging info");
      }
      core_.logMessage("Now sending problem report, after a delay of 200 ms");

      // Hack: The end of the logged info gets truncated, presumably due to the
      // behavior of the Core logger. For now, just wait a bit to remedy.
      for (;;) {
         try {
            Thread.sleep(200);
            break;
         }
         catch (InterruptedException e) {
            // In the unlikely event of being interrupted, we wait again.
         }
      }

      ProblemReportSender p = new ProblemReportSender(reportPreamble_, core_);
      String result = p.Send();
      if (0 < result.length()) {
         ReportingUtils.logError(result);
         stepInstructions_.setText("There was a problem sending the report, please verify your internet connection.");
      } else {
         stepInstructions_.setText("The report was successfully submitted to micro-manager.org");
      }

   }

   private void logExtraInformation(boolean includeConstantInfo) {
      // I'm a little tempted to suppress the system properties when
      // !includeConstantInfo, but they can be changed, and we don't want to
      // miss it if they do. The best solution would be to record the values
      // before the start and check for differences at appropriate check points
      // (including just before sending report).
      core_.logMessage("Java system properties:");
      String pathSep = System.getProperty("path.separator");
      java.util.Properties sysProps = System.getProperties();
      java.util.List<String> propKeys = new java.util.ArrayList<String>();
      java.util.Enumeration<Object> e = sysProps.keys();
      while (e.hasMoreElements()) {
         propKeys.add((String) e.nextElement());
      }
      java.util.Collections.sort(propKeys);
      for (String k : propKeys) {
         if ((k.equals("java.class.path") || k.equals("java.library.path") ||
               k.equals("sun.boot.class.path") || k.equals("sun.boot.library.path") &&
               pathSep != null && !pathSep.equals(""))) {
            core_.logMessage("  " + k  + " (split at \'" + pathSep + "\') =");
            String[] paths = sysProps.getProperty(k).split(pathSep);
            for (String path : paths) {
               core_.logMessage("    " + path);
            }
         }
         else {
            core_.logMessage("  " + k + " = " + sysProps.getProperty(k));
         }
      }
      core_.logMessage("(End Java system properties)");

      if (includeConstantInfo) {
         java.lang.management.RuntimeMXBean rtMXB = ManagementFactory.getRuntimeMXBean();
         core_.logMessage("JVM arguments:");
         java.util.List<String> args = rtMXB.getInputArguments();
         for (String a : args) {
            core_.logMessage("  " + a);
         }
         core_.logMessage("(End JVM args)");
      }

      java.lang.management.OperatingSystemMXBean osMXB = ManagementFactory.getOperatingSystemMXBean();
      if (includeConstantInfo) {
         core_.logMessage("Processors available to JVM: " + osMXB.getAvailableProcessors());
      }
      try { // Use HotSpot extensions if available
         Class<?> sunOSMXBClass = Class.forName("com.sun.management.OperatingSystemMXBean");

         java.lang.reflect.Method totalMemMethod = sunOSMXBClass.getMethod("getTotalPhysicalMemorySize");
         if (includeConstantInfo) {
            long totalRAM = ((Long) totalMemMethod.invoke(osMXB)).longValue();
            core_.logMessage("Total physical memory (4 GB max if JVM is 32-bit): " +
                    totalRAM + " (" + (totalRAM / 1024 / 1024) + "M)");
         }

         java.lang.reflect.Method freeMemMethod = sunOSMXBClass.getMethod("getFreePhysicalMemorySize");
         long freeRAM = ((Long) freeMemMethod.invoke(osMXB)).longValue();
         core_.logMessage("Free physical memory (4 GB max if JVM is 32-bit): " +
                 freeRAM + " (" + (freeRAM / 1024 / 1024) + "M)");
      } catch (ClassNotFoundException exc) {
      } catch (NoSuchMethodException exc) {
      } catch (IllegalAccessException exc) {
      } catch (java.lang.reflect.InvocationTargetException exc) {
      }

      java.lang.management.MemoryMXBean memMXB = ManagementFactory.getMemoryMXBean();
      core_.logMessage("JVM heap memory usage: " + memMXB.getHeapMemoryUsage());
      core_.logMessage("JVM non-heap memory usage: " + memMXB.getNonHeapMemoryUsage());

      java.lang.management.ThreadMXBean threadMXB = ManagementFactory.getThreadMXBean();

      core_.logMessage("All Java threads:");
      long[] tids = threadMXB.getAllThreadIds();
      java.util.Arrays.sort(tids);
      ThreadInfo[] threadInfos = threadMXB.getThreadInfo(tids);
      for (ThreadInfo tInfo : threadInfos) {
         core_.logMessage("  id " + tInfo.getThreadId() +
                 " (\"" + tInfo.getThreadName() + "\"): " +
                 tInfo.getThreadState().name());
      }
      core_.logMessage("(End all Java threads)");

      /* Uncomment to see an example of a pair of deadlocked threads
       * which will be detected below. TODO Remove this test code.
      final Object a = new Object();
      final Object b = new Object();
      Thread th0 = new Thread("DeadLockTestThread0") {
         public void run() {
            try {
               synchronized (a) {
                  Thread.sleep(100);
                  synchronized (b) {
                     Thread.sleep(1);
                  }
               }
            } catch (InterruptedException e) {
            }
         }
      };
      Thread th1 = new Thread("DeadLockTestThread1") {
         public void run() {
            try {
               synchronized (b) {
                  Thread.sleep(100);
                  synchronized (a) {
                     Thread.sleep(1);
                  }
               }
            } catch (InterruptedException e) {
            }
         }
      };
      th0.start();
      th1.start();

      try {
         Thread.sleep(200);
      } catch (InterruptedException exc) {
      }
      */

      // Analyze deadlocked threads. TODO This is generally useful and should
      // be put in its own method somewhere. It should also be dumped just
      // before sending the report.
      long[] deadlockedThreadIds = threadMXB.findDeadlockedThreads();

      if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
         java.util.Arrays.sort(deadlockedThreadIds);
         ThreadInfo[] deadlockedInfos = threadMXB.getThreadInfo(deadlockedThreadIds, true, true);
         for (ThreadInfo tInfo : deadlockedInfos) {
            core_.logMessage("Deadlocked thread " + tInfo.getThreadId() +
                    " (\"" + tInfo.getThreadName() + "\"):");
            java.lang.management.LockInfo blockingLock = tInfo.getLockInfo();
            core_.logMessage("  Blocked waiting to lock " + blockingLock.getClassName() +
                    " " + blockingLock.getIdentityHashCode());

            java.lang.management.MonitorInfo[] monitors = tInfo.getLockedMonitors();
            java.lang.management.LockInfo[] synchronizers = tInfo.getLockedSynchronizers();
            StackTraceElement[] trace = tInfo.getStackTrace();
            for (StackTraceElement frame : trace) {
               core_.logMessage("    at " + frame.toString());
               for (java.lang.management.MonitorInfo monitor : monitors) {
                  if (monitor.getLockedStackFrame().equals(frame)) {
                     core_.logMessage("      where monitor was locked: " +
                             monitor.getClassName() + " " +
                             monitor.getIdentityHashCode());
                  }
               }
            }
            for (java.lang.management.LockInfo sync : synchronizers) {
               core_.logMessage("  Ownable synchronizer is locked: " +
                       sync.getClassName() + " " + sync.getIdentityHashCode());
            }
         }
      }
      else {
         core_.logMessage("No deadlocked threads detected");
      }
   }

    private void cancelButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButton_ActionPerformed
       FinishDialog();
       this.setVisible(false);
    }//GEN-LAST:event_cancelButton_ActionPerformed

    private void name_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_name_ActionPerformed
    }//GEN-LAST:event_name_ActionPerformed

    private void name_PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_name_PropertyChange
      
    }//GEN-LAST:event_name_PropertyChange

    private void clearButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButton_ActionPerformed
       core_.clearLog();
       core_.logMessage("CoreLog cleared by problem reporter");
       try {
          parent_.logStartupProperties();
          logExtraInformation(true);
       }
       catch (Exception e) {
          core_.logMessage("Exception while logging info");
       }

       stepInstructions_.setText("The system is now capturing a 'debug' level log file. Operate the system until you've duplicated the problem. When you're successful, "
               + " press Send to send your information, problem description, system configuration, and log to micro-manager.org.");
       core_.logMessage("User has been propmted to reproduce problem");

       descriptionPane_.setEnabled(true);
       descriptionPane_.setEditable(true);
       sendButton_.setEnabled(true);

    }//GEN-LAST:event_clearButton_ActionPerformed

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
       FinishDialog();
    }//GEN-LAST:event_formComponentHidden

    private void sendButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButton_ActionPerformed

       // This regex does not allow addresses with dashes in them
       //String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
       // simpler, and seems to work:
       //String EMAIL_REGEX = "[\\w-]+@([\\w-]+\\.)+[\\w-]+";
       // this one deals with dots in name (from http://www.regular-expressions.info/email.html)
       String EMAIL_REGEX = "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
       if (emailText_.getText().matches(EMAIL_REGEX)) {
          if (0 < name_.getText().length()) {
             if (0 < organization_.getText().length()) {
                int result = JOptionPane.showConfirmDialog(null, "Did you restart the log and reproduce the problem?",
                        "Really send?", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                   buildAndSendReport();
                }
             } else {
                ReportingUtils.showMessage("Please provide your Organization name.");
                organization_.requestFocus();
             }
          } else {
             ReportingUtils.showMessage("Please provide your name.");
             name_.requestFocus();
          }
       } else {
          ReportingUtils.showMessage("Please provide a valid e-mail.");
          emailText_.requestFocus();
       }

}//GEN-LAST:event_sendButton_ActionPerformed

    private void emailText_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailText_ActionPerformed
       
    }//GEN-LAST:event_emailText_ActionPerformed

    private void organization_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_organization_ActionPerformed
    }//GEN-LAST:event_organization_ActionPerformed

    private void organization_PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_organization_PropertyChange
      
    }//GEN-LAST:event_organization_PropertyChange

    private void emailText_PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_emailText_PropertyChange
       
    }//GEN-LAST:event_emailText_PropertyChange
   void FinishDialog() {
      sendButton_.setEnabled(true);
      descriptionPane_.setEnabled(true);
      descriptionPane_.setEditable(true);
      core_.enableDebugLog(mmoptions_.debugLogEnabled_);
   }

   void InitializeDialog() {

      this.setLocation(100, 100);

      stepInstructions_.setText("Please press the \"Restart Log!\" button first.  Reproduce the problem, "
              + "then press \"Send\". \n\n ");
      stepInstructions_.setBackground(this.getBackground());
      MMStudioMainFrame.getInstance().addMMBackgroundListener(stepInstructions_);


      descriptionPane_.setText("");
      descriptionPane_.setVisible(true);
      descriptionPane_.setEditable(true);
      descriptionPane_.setEnabled(true);
      descriptionPane_.requestFocus();
      core_.enableDebugLog(true);
      cancelButton_.setVisible(true);
      sendButton_.setEnabled(true);

   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelButton_;
   private javax.swing.JButton clearButton_;
   private javax.swing.JEditorPane descriptionPane_;
   private javax.swing.JTextField emailText_;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTextField name_;
   private javax.swing.JTextField organization_;
   private javax.swing.JButton sendButton_;
   private javax.swing.JTextPane stepInstructions_;
   // End of variables declaration//GEN-END:variables
}
