
/**
 * StageControlFrame.java
 *
 * Created on Aug 19, 2010, 10:04:49 PM
 * Nico Stuurman, copyright UCSF, 2010
 * 
 * LICENSE:      This file is distributed under the BSD license.
 *               License text is included with the source distribution.
 *
 *               This file is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty
 *               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 */

package org.micromanager.stagecontrol;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import mmcorej.CMMCore;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import mmcorej.DeviceType;
import mmcorej.StrVector;

import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;

/**
 *
 * @author nico
 */
public class StageControlFrame extends javax.swing.JFrame 
implements MMListenerInterface {
   private final ScriptInterface gui_;
   private final CMMCore core_;
   private Preferences prefs_;

   private double smallMovement_ = 1.0;
   private double mediumMovement_ = 10.0;
   private double largeMovement_ = 100.0;
   private Map<String, Double> smallMovementZ_ = new HashMap();
   private Map<String, Double> mediumMovementZ_ = new HashMap();
   private NumberFormat nf_;
   private String currentZDrive_ = "";
   private boolean initialized_ = false;

   private int frameXPos_ = 100;
   private int frameYPos_ = 100;
   
   private LayoutManager frameLayout_;

   private static final String FRAMEXPOS = "FRAMEXPOS";
   private static final String FRAMEYPOS = "FRAMEYPOS";
   private static final String SMALLMOVEMENT = "SMALLMOVEMENT";
   private static final String MEDIUMMOVEMENT = "MEDIUMMOVEMENT";
   private static final String LARGEMOVEMENT = "LARGEMOVEMENT";
   private static final String SMALLMOVEMENTZ = "SMALLMOVEMENTZ";
   private static final String MEDIUMMOVEMENTZ = "MEDIUMMOVEMENTZ";
   private static final String CURRENTZDRIVE = "CURRENTZDRIVE";



   /**
    * Creates new form StageControlFrame
    */
   public StageControlFrame(ScriptInterface gui) {
      gui_ = gui;
      core_ = gui_.getMMCore();
      nf_ = NumberFormat.getInstance();
      prefs_ = Preferences.userNodeForPackage(this.getClass());

      // Read values from PREFS
      frameXPos_ = prefs_.getInt(FRAMEXPOS, frameXPos_);
      frameYPos_ = prefs_.getInt(FRAMEYPOS, frameYPos_);
      double pixelSize = core_.getPixelSizeUm();
      long nrPixelsX = core_.getImageWidth();
      if (pixelSize != 0) {
         smallMovement_ = pixelSize;
         mediumMovement_ = pixelSize * nrPixelsX * 0.1;
         largeMovement_ = pixelSize * nrPixelsX;
      }
      smallMovement_ = prefs_.getDouble(SMALLMOVEMENT, smallMovement_);
      mediumMovement_ = prefs_.getDouble(MEDIUMMOVEMENT, mediumMovement_);
      largeMovement_ = prefs_.getDouble(LARGEMOVEMENT, largeMovement_);
      currentZDrive_ = prefs_.get(CURRENTZDRIVE, currentZDrive_);

      initComponents();
      
      frameLayout_ = this.getContentPane().getLayout();

      setLocation(frameXPos_, frameYPos_);

   }
   
   /**
    * Initialized GUI components based on current hardware configuration
    * Can be called at any time to adjust display (for instance after hardware
    * configuration change)
    */
   public final void initialize() {
      smallXYTextField_.setText(nf_.format(smallMovement_));
      mediumXYTextField_.setText(nf_.format(mediumMovement_));
      largeXYTextField_.setText(nf_.format(largeMovement_));

      StrVector zDrives = core_.getLoadedDevicesOfType(DeviceType.StageDevice);
      StrVector xyDrives = core_.getLoadedDevicesOfType(DeviceType.XYStageDevice);
      
      // start with a clean slate
      this.getContentPane().removeAll();
      // The no-drive display changes the layout. Switch back here.
      this.getContentPane().setLayout(frameLayout_);
      
      int width = 0;
      if (!xyDrives.isEmpty()) {       
         xyPanel_.setLocation(0,0);
         xyPanel_.setVisible(true);
         this.getContentPane().add(xyPanel_);
         width += 250;
      }
      
      if (!zDrives.isEmpty()) {
         zPanel_.setLocation(width,0);
         zPanel_.setVisible(true);
         this.getContentPane().add(zPanel_);
         width += 140;
         if (zDrives.size() == 1) {
            zDriveSelect_.setVisible(false);
         }

         boolean zDriveFound = false;
         for (int i = 0; i < zDrives.size(); i++) {
            String drive = zDrives.get(i);
            smallMovementZ_.put(drive, prefs_.getDouble(SMALLMOVEMENTZ + drive, 1.0));
            mediumMovementZ_.put(drive, prefs_.getDouble(MEDIUMMOVEMENTZ + drive, 10.0));
            zDriveSelect_.addItem(drive);
            if (currentZDrive_.equals(zDrives.get(i))) {
               zDriveFound = true;
            }
         }
         if (!zDriveFound) {
            currentZDrive_ = zDrives.get(0);
         }
         updateZMovements();
      } 
      
      // Provide a friendly message when there are no drives in the device list
      if (zDrives.isEmpty() && xyDrives.isEmpty()) {
         javax.swing.JLabel noDriveLabel = new javax.swing.JLabel(
                 "No XY or Z drive found.  Nothing to control.");
         noDriveLabel.setOpaque(true);
         
         javax.swing.JPanel panel = new javax.swing.JPanel();
         panel.setLayout(new BorderLayout());        
         panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
         panel.add(noDriveLabel, BorderLayout.CENTER);
         panel.revalidate();
         
         this.getContentPane().setLayout(new BorderLayout());
         this.getContentPane().add(panel, BorderLayout.CENTER);
         this.pack();
         this.setVisible(true);
         this.repaint();
         return;
      }
      
      this.setSize(width, 349);

      initialized_ = true;
   }

   private void updateZMovements() {
      smallZTextField_.setText(nf_.format(smallMovementZ_.get(currentZDrive_)));
      mediumZTextField_.setText(nf_.format(mediumMovementZ_.get(currentZDrive_)));
   }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        zPanel_ = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        zDriveSelect_ = new javax.swing.JComboBox();
        mediumZUpButton_ = new javax.swing.JButton();
        mediumZTextField_ = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        smallZTextField_ = new javax.swing.JTextField();
        mediumZDownButton_ = new javax.swing.JButton();
        smallZDownButton_ = new javax.swing.JButton();
        zPositionLabel_ = new javax.swing.JLabel();
        smallZUpButton_ = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        xyPanel_ = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        largeXYUpButton_ = new javax.swing.JButton();
        mediumXYUpButton_ = new javax.swing.JButton();
        smallXYUpButton_ = new javax.swing.JButton();
        smallXYDownButton_ = new javax.swing.JButton();
        mediumXYDownButton_ = new javax.swing.JButton();
        largeXYDownButton_ = new javax.swing.JButton();
        largeLeftButton_ = new javax.swing.JButton();
        mediumLeftButton_ = new javax.swing.JButton();
        smallLeftButton_ = new javax.swing.JButton();
        smallRightButton_ = new javax.swing.JButton();
        mediumRightButton_ = new javax.swing.JButton();
        largeRightButton_ = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        largeXYTextField_ = new javax.swing.JTextField();
        mediumXYTextField_ = new javax.swing.JTextField();
        smallXYTextField_ = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        getFieldButton_ = new javax.swing.JButton();
        getPartFieldButton_ = new javax.swing.JButton();
        get1PixelButton_ = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0));

        setTitle("Stage Control");
        setLocationByPlatform(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosing(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Z Stage");

        zDriveSelect_.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        zDriveSelect_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zDriveSelect_ActionPerformed(evt);
            }
        });

        mediumZUpButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-du.png"))); // NOI18N
        mediumZUpButton_.setBorder(null);
        mediumZUpButton_.setBorderPainted(false);
        mediumZUpButton_.setContentAreaFilled(false);
        mediumZUpButton_.setFocusPainted(false);
        mediumZUpButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dup.png"))); // NOI18N
        mediumZUpButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumZUpButton_ActionPerformed(evt);
            }
        });

        mediumZTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumZTextField_ActionPerformed(evt);
            }
        });
        mediumZTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                mediumZTextField_focusLostHandlerJTF2(evt);
            }
        });

        jLabel8.setText("<html>&#956;m</html>");

        jLabel9.setText("<html>&#956;m</html>");

        smallZTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallZTextField_ActionPerformed(evt);
            }
        });
        smallZTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                smallZTextField_focusLostHandlerJTF1(evt);
            }
        });

        mediumZDownButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dd.png"))); // NOI18N
        mediumZDownButton_.setBorder(null);
        mediumZDownButton_.setBorderPainted(false);
        mediumZDownButton_.setContentAreaFilled(false);
        mediumZDownButton_.setFocusPainted(false);
        mediumZDownButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-ddp.png"))); // NOI18N
        mediumZDownButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumZDownButton_ActionPerformed(evt);
            }
        });

        smallZDownButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sd.png"))); // NOI18N
        smallZDownButton_.setBorder(null);
        smallZDownButton_.setBorderPainted(false);
        smallZDownButton_.setContentAreaFilled(false);
        smallZDownButton_.setFocusPainted(false);
        smallZDownButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sdp.png"))); // NOI18N
        smallZDownButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallZDownButton_ActionPerformed(evt);
            }
        });

        zPositionLabel_.setText("0.00");

        smallZUpButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-su.png"))); // NOI18N
        smallZUpButton_.setBorder(null);
        smallZUpButton_.setBorderPainted(false);
        smallZUpButton_.setContentAreaFilled(false);
        smallZUpButton_.setFocusPainted(false);
        smallZUpButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sup.png"))); // NOI18N
        smallZUpButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallZUpButton_ActionPerformed(evt);
            }
        });

        jButton23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sr.png"))); // NOI18N
        jButton23.setBorder(null);
        jButton23.setBorderPainted(false);
        jButton23.setContentAreaFilled(false);
        jButton23.setFocusPainted(false);
        jButton23.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-srp.png"))); // NOI18N
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jButton24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dr.png"))); // NOI18N
        jButton24.setBorder(null);
        jButton24.setBorderPainted(false);
        jButton24.setContentAreaFilled(false);
        jButton24.setFocusPainted(false);
        jButton24.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-drp.png"))); // NOI18N
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout zPanel_Layout = new org.jdesktop.layout.GroupLayout(zPanel_);
        zPanel_.setLayout(zPanel_Layout);
        zPanel_Layout.setHorizontalGroup(
            zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.CENTER, smallZUpButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, smallZDownButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(zPanel_Layout.createSequentialGroup()
                .add(filler3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zDriveSelect_, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(mediumZDownButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(mediumZUpButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, zPanel_Layout.createSequentialGroup()
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(zPanel_Layout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButton23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(zPanel_Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jButton24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mediumZTextField_, 0, 1, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, smallZTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .add(zPanel_Layout.createSequentialGroup()
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, zPositionLabel_)
                    .add(zPanel_Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, Short.MAX_VALUE))
        );
        zPanel_Layout.setVerticalGroup(
            zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(zPanel_Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(zDriveSelect_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filler3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mediumZUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(smallZUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(zPanel_Layout.createSequentialGroup()
                        .add(zPositionLabel_)
                        .add(10, 10, 10)
                        .add(smallZDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(mediumZDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(smallZTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(zPanel_Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jButton23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, 0)
                .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(zPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(mediumZTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jButton24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32))
        );

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("XY Stage");

        largeXYUpButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tu.png"))); // NOI18N
        largeXYUpButton_.setBorder(null);
        largeXYUpButton_.setBorderPainted(false);
        largeXYUpButton_.setContentAreaFilled(false);
        largeXYUpButton_.setFocusPainted(false);
        largeXYUpButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tup.png"))); // NOI18N
        largeXYUpButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeXYUpButton_ActionPerformed(evt);
            }
        });

        mediumXYUpButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-du.png"))); // NOI18N
        mediumXYUpButton_.setBorder(null);
        mediumXYUpButton_.setBorderPainted(false);
        mediumXYUpButton_.setContentAreaFilled(false);
        mediumXYUpButton_.setFocusPainted(false);
        mediumXYUpButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dup.png"))); // NOI18N
        mediumXYUpButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumXYUpButton_ActionPerformed(evt);
            }
        });

        smallXYUpButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-su.png"))); // NOI18N
        smallXYUpButton_.setBorder(null);
        smallXYUpButton_.setBorderPainted(false);
        smallXYUpButton_.setContentAreaFilled(false);
        smallXYUpButton_.setFocusPainted(false);
        smallXYUpButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sup.png"))); // NOI18N
        smallXYUpButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallXYUpButton_ActionPerformed(evt);
            }
        });

        smallXYDownButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sd.png"))); // NOI18N
        smallXYDownButton_.setBorder(null);
        smallXYDownButton_.setBorderPainted(false);
        smallXYDownButton_.setContentAreaFilled(false);
        smallXYDownButton_.setFocusPainted(false);
        smallXYDownButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sdp.png"))); // NOI18N
        smallXYDownButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallXYDownButton_ActionPerformed(evt);
            }
        });

        mediumXYDownButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dd.png"))); // NOI18N
        mediumXYDownButton_.setBorder(null);
        mediumXYDownButton_.setBorderPainted(false);
        mediumXYDownButton_.setContentAreaFilled(false);
        mediumXYDownButton_.setFocusPainted(false);
        mediumXYDownButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-ddp.png"))); // NOI18N
        mediumXYDownButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumXYDownButton_ActionPerformed(evt);
            }
        });

        largeXYDownButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-td.png"))); // NOI18N
        largeXYDownButton_.setBorder(null);
        largeXYDownButton_.setBorderPainted(false);
        largeXYDownButton_.setContentAreaFilled(false);
        largeXYDownButton_.setFocusPainted(false);
        largeXYDownButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tdp.png"))); // NOI18N
        largeXYDownButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeXYDownButton_ActionPerformed(evt);
            }
        });

        largeLeftButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tl.png"))); // NOI18N
        largeLeftButton_.setBorder(null);
        largeLeftButton_.setBorderPainted(false);
        largeLeftButton_.setContentAreaFilled(false);
        largeLeftButton_.setFocusPainted(false);
        largeLeftButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tlp.png"))); // NOI18N
        largeLeftButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeLeftButton_ActionPerformed(evt);
            }
        });

        mediumLeftButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dl.png"))); // NOI18N
        mediumLeftButton_.setBorder(null);
        mediumLeftButton_.setBorderPainted(false);
        mediumLeftButton_.setContentAreaFilled(false);
        mediumLeftButton_.setFocusPainted(false);
        mediumLeftButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dlp.png"))); // NOI18N
        mediumLeftButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumLeftButton_ActionPerformed(evt);
            }
        });

        smallLeftButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sl.png"))); // NOI18N
        smallLeftButton_.setBorder(null);
        smallLeftButton_.setBorderPainted(false);
        smallLeftButton_.setContentAreaFilled(false);
        smallLeftButton_.setFocusPainted(false);
        smallLeftButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-slp.png"))); // NOI18N
        smallLeftButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallLeftButton_ActionPerformed(evt);
            }
        });

        smallRightButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sr.png"))); // NOI18N
        smallRightButton_.setBorder(null);
        smallRightButton_.setBorderPainted(false);
        smallRightButton_.setContentAreaFilled(false);
        smallRightButton_.setFocusPainted(false);
        smallRightButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-srp.png"))); // NOI18N
        smallRightButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallRightButton_ActionPerformed(evt);
            }
        });

        mediumRightButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dr.png"))); // NOI18N
        mediumRightButton_.setBorder(null);
        mediumRightButton_.setBorderPainted(false);
        mediumRightButton_.setContentAreaFilled(false);
        mediumRightButton_.setFocusPainted(false);
        mediumRightButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-drp.png"))); // NOI18N
        mediumRightButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumRightButton_ActionPerformed(evt);
            }
        });

        largeRightButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tr.png"))); // NOI18N
        largeRightButton_.setBorder(null);
        largeRightButton_.setBorderPainted(false);
        largeRightButton_.setContentAreaFilled(false);
        largeRightButton_.setFocusPainted(false);
        largeRightButton_.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-trp.png"))); // NOI18N
        largeRightButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeRightButton_ActionPerformed(evt);
            }
        });

        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-sr.png"))); // NOI18N
        jButton20.setBorder(null);
        jButton20.setBorderPainted(false);
        jButton20.setContentAreaFilled(false);
        jButton20.setFocusPainted(false);
        jButton20.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-srp.png"))); // NOI18N
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-dr.png"))); // NOI18N
        jButton21.setBorder(null);
        jButton21.setBorderPainted(false);
        jButton21.setContentAreaFilled(false);
        jButton21.setFocusPainted(false);
        jButton21.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-drp.png"))); // NOI18N
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-tr.png"))); // NOI18N
        jButton22.setBorder(null);
        jButton22.setBorderPainted(false);
        jButton22.setContentAreaFilled(false);
        jButton22.setFocusPainted(false);
        jButton22.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/stagecontrol/icons/arrowhead-trp.png"))); // NOI18N
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        largeXYTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeXYTextField_ActionPerformed(evt);
            }
        });
        largeXYTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                focusLostHandlerJTF3(evt);
            }
        });

        mediumXYTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumXYTextField_ActionPerformed(evt);
            }
        });
        mediumXYTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                focusLostHandlerJTF2(evt);
            }
        });

        smallXYTextField_.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        smallXYTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallXYTextField_ActionPerformed(evt);
            }
        });
        smallXYTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                focusLostHandlerJTF1(evt);
            }
        });

        jLabel4.setText("<html>&#956;m</html>");

        jLabel5.setText("<html>&#956;m</html>");

        jLabel6.setText("<html>&#956;m</html>");

        getFieldButton_.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        getFieldButton_.setText("1 field");
        getFieldButton_.setMaximumSize(new java.awt.Dimension(35, 20));
        getFieldButton_.setMinimumSize(new java.awt.Dimension(0, 0));
        getFieldButton_.setPreferredSize(new java.awt.Dimension(35, 20));
        getFieldButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getFieldButton_ActionPerformed(evt);
            }
        });

        getPartFieldButton_.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        getPartFieldButton_.setText("0.1 field");
        getPartFieldButton_.setMaximumSize(new java.awt.Dimension(35, 20));
        getPartFieldButton_.setMinimumSize(new java.awt.Dimension(0, 0));
        getPartFieldButton_.setPreferredSize(new java.awt.Dimension(35, 20));
        getPartFieldButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getPartFieldButton_ActionPerformed(evt);
            }
        });

        get1PixelButton_.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        get1PixelButton_.setText("1 pixel");
        get1PixelButton_.setIconTextGap(6);
        get1PixelButton_.setMaximumSize(new java.awt.Dimension(0, 0));
        get1PixelButton_.setMinimumSize(new java.awt.Dimension(0, 0));
        get1PixelButton_.setPreferredSize(new java.awt.Dimension(35, 20));
        get1PixelButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                get1PixelButton_ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout xyPanel_Layout = new org.jdesktop.layout.GroupLayout(xyPanel_);
        xyPanel_.setLayout(xyPanel_Layout);
        xyPanel_Layout.setHorizontalGroup(
            xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, largeXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(mediumXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, smallXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(xyPanel_Layout.createSequentialGroup()
                .addContainerGap()
                .add(largeLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mediumLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(smallLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(smallRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mediumRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(largeRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(largeXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, smallXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(mediumXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .add(xyPanel_Layout.createSequentialGroup()
                .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(xyPanel_Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jButton20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButton21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButton22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(smallXYTextField_)
                            .add(mediumXYTextField_)
                            .add(largeXYTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(xyPanel_Layout.createSequentialGroup()
                                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(get1PixelButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(xyPanel_Layout.createSequentialGroup()
                                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(getFieldButton_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(xyPanel_Layout.createSequentialGroup()
                                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(getPartFieldButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(xyPanel_Layout.createSequentialGroup()
                        .add(78, 78, 78)
                        .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 16, Short.MAX_VALUE))
        );
        xyPanel_Layout.setVerticalGroup(
            xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(xyPanel_Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(largeXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mediumXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(smallXYUpButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(smallLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mediumLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(largeLeftButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(largeRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mediumRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(smallRightButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(smallXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mediumXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(largeXYDownButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(xyPanel_Layout.createSequentialGroup()
                        .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(get1PixelButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(smallXYTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(0, 0, 0)
                        .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(xyPanel_Layout.createSequentialGroup()
                                .add(jButton21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE)
                                .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(largeXYTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(getFieldButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jButton22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, getPartFieldButton_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(xyPanel_Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(mediumXYTextField_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(jButton20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(232, 232, 232)
                        .add(jLabel3))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(xyPanel_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zPanel_, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filler2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filler1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(xyPanel_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(zPanel_, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(0, 0, 0)
                .add(jLabel3)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(39, 39, 39)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filler2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filler1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void setRelativeXYStagePosition(double x, double y) {
      try {
         if (!core_.deviceBusy(core_.getXYStageDevice()))
            core_.setRelativeXYPosition(core_.getXYStageDevice(), x, y);
       } catch(Exception e) {
          gui_.logError(e);
       }
   }
   
   private void setRelativeStagePosition(double z) {
      try {
         if (!core_.deviceBusy(currentZDrive_)) {
            core_.setRelativePosition(currentZDrive_, z);
            updateZPosLabel();
         }
      } catch (Exception e) {
         gui_.logError(e);
      }
   }

   private void updateZPosLabel() throws Exception {
      double zPos = core_.getPosition(currentZDrive_);
      zPositionLabel_.setText(nf_.format(zPos) + " \u00B5m" );
   }

    private void onWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosing
       prefs_.putInt(FRAMEXPOS, (int) getLocation().getX());
       prefs_.putInt(FRAMEYPOS, (int) getLocation().getY());
       prefs_.putDouble(SMALLMOVEMENT, smallMovement_);
       prefs_.putDouble(MEDIUMMOVEMENT, mediumMovement_);
       prefs_.putDouble(LARGEMOVEMENT, largeMovement_);
    }//GEN-LAST:event_onWindowClosing

   private void updateSmallZMove() {
      try {
         double smallZMove = nf_.parse(smallZTextField_.getText()).doubleValue();
         smallMovementZ_.put(currentZDrive_, smallZMove);
         prefs_.putDouble(SMALLMOVEMENTZ + currentZDrive_, smallZMove);
      } catch (ParseException e) {
         // ignore if parsing fails
      }
   }

   private void updateMediumZMove() {
      try {
         double mediumZMove = nf_.parse(mediumZTextField_.getText()).doubleValue();
         mediumMovementZ_.put(currentZDrive_, mediumZMove);
         prefs_.putDouble(MEDIUMMOVEMENTZ + currentZDrive_, mediumZMove);
      } catch (ParseException e) {
         // ignore if parsing fails
      }
   }
   
   private void get1PixelButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_get1PixelButton_ActionPerformed
      double pixelSize = core_.getPixelSizeUm();
      smallXYTextField_.setText(nf_.format(pixelSize));
      smallMovement_ = pixelSize;
      nf_.setMaximumFractionDigits(3);
      smallXYTextField_.setText(nf_.format(smallMovement_));
   }//GEN-LAST:event_get1PixelButton_ActionPerformed

   private void getPartFieldButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPartFieldButton_ActionPerformed
      long nrPixelsX = core_.getImageWidth();
      double pixelSize = core_.getPixelSizeUm();
      nf_.setMaximumFractionDigits(2);
      mediumMovement_ = pixelSize * nrPixelsX * 0.1;
      mediumXYTextField_.setText(nf_.format(mediumMovement_));
   }//GEN-LAST:event_getPartFieldButton_ActionPerformed

   private void getFieldButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getFieldButton_ActionPerformed
      long nrPixelsX = core_.getImageWidth();
      double pixelSize = core_.getPixelSizeUm();
      nf_.setMaximumFractionDigits(1);
      largeMovement_ = pixelSize * nrPixelsX;
      largeXYTextField_.setText(nf_.format(largeMovement_));
   }//GEN-LAST:event_getFieldButton_ActionPerformed

   private void focusLostHandlerJTF1(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusLostHandlerJTF1
      try {
         smallMovement_ = nf_.parse(smallXYTextField_.getText()).doubleValue();
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_focusLostHandlerJTF1

   private void smallXYTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallXYTextField_ActionPerformed
      try {
         smallMovement_ = nf_.parse(smallXYTextField_.getText()).doubleValue();
         nf_.setMaximumFractionDigits(3);
         smallXYTextField_.setText(nf_.format(smallMovement_));
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_smallXYTextField_ActionPerformed

   private void focusLostHandlerJTF2(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusLostHandlerJTF2
      try {
         mediumMovement_ = nf_.parse(mediumXYTextField_.getText()).doubleValue();
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_focusLostHandlerJTF2

   private void mediumXYTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumXYTextField_ActionPerformed
      try {
         mediumMovement_ = nf_.parse(mediumXYTextField_.getText()).doubleValue();
         nf_.setMaximumFractionDigits(2);
         mediumXYTextField_.setText(nf_.format(mediumMovement_));
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_mediumXYTextField_ActionPerformed

   private void focusLostHandlerJTF3(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusLostHandlerJTF3
      try {
         largeMovement_ = nf_.parse(largeXYTextField_.getText()).doubleValue();
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_focusLostHandlerJTF3

   private void largeXYTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeXYTextField_ActionPerformed
      try {
         largeMovement_ = nf_.parse(largeXYTextField_.getText()).doubleValue();
         nf_.setMaximumFractionDigits(1);
         largeXYTextField_.setText(nf_.format(largeMovement_));
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_largeXYTextField_ActionPerformed

   private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButton22ActionPerformed

   private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButton21ActionPerformed

   private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButton20ActionPerformed

   private void largeRightButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeRightButton_ActionPerformed
      setRelativeXYStagePosition(largeMovement_, 0.0);
   }//GEN-LAST:event_largeRightButton_ActionPerformed

   private void mediumRightButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumRightButton_ActionPerformed
      setRelativeXYStagePosition(mediumMovement_, 0.0);
   }//GEN-LAST:event_mediumRightButton_ActionPerformed

   private void smallRightButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallRightButton_ActionPerformed
      setRelativeXYStagePosition(smallMovement_, 0.0);
   }//GEN-LAST:event_smallRightButton_ActionPerformed

   private void smallLeftButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallLeftButton_ActionPerformed
      setRelativeXYStagePosition(-smallMovement_, 0.0);
   }//GEN-LAST:event_smallLeftButton_ActionPerformed

   private void mediumLeftButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumLeftButton_ActionPerformed
      setRelativeXYStagePosition(-mediumMovement_, 0.0);
   }//GEN-LAST:event_mediumLeftButton_ActionPerformed

   private void largeLeftButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeLeftButton_ActionPerformed
      setRelativeXYStagePosition(-largeMovement_, 0.0);
   }//GEN-LAST:event_largeLeftButton_ActionPerformed

   private void largeXYDownButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeXYDownButton_ActionPerformed
      setRelativeXYStagePosition(0.0, largeMovement_);
   }//GEN-LAST:event_largeXYDownButton_ActionPerformed

   private void mediumXYDownButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumXYDownButton_ActionPerformed
      setRelativeXYStagePosition(0.0, mediumMovement_);
   }//GEN-LAST:event_mediumXYDownButton_ActionPerformed

   private void smallXYDownButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallXYDownButton_ActionPerformed
      setRelativeXYStagePosition(0.0, smallMovement_);
   }//GEN-LAST:event_smallXYDownButton_ActionPerformed

   private void smallXYUpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallXYUpButton_ActionPerformed
      setRelativeXYStagePosition(0.0, -smallMovement_);
   }//GEN-LAST:event_smallXYUpButton_ActionPerformed

   private void mediumXYUpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumXYUpButton_ActionPerformed
      setRelativeXYStagePosition(0.0, -mediumMovement_);
   }//GEN-LAST:event_mediumXYUpButton_ActionPerformed

   private void largeXYUpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeXYUpButton_ActionPerformed
      setRelativeXYStagePosition(0.0, -largeMovement_);
   }//GEN-LAST:event_largeXYUpButton_ActionPerformed

   private void smallZUpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallZUpButton_ActionPerformed
      try {
         setRelativeStagePosition(nf_.parse(smallZTextField_.getText()).doubleValue());
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_smallZUpButton_ActionPerformed

   private void smallZDownButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallZDownButton_ActionPerformed
      try {
         setRelativeStagePosition( - nf_.parse(smallZTextField_.getText()).doubleValue());
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_smallZDownButton_ActionPerformed

   private void mediumZDownButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumZDownButton_ActionPerformed
      try {
         setRelativeStagePosition( - nf_.parse(mediumZTextField_.getText()).doubleValue());
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_mediumZDownButton_ActionPerformed

   private void smallZTextField_focusLostHandlerJTF1(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_smallZTextField_focusLostHandlerJTF1
      updateSmallZMove();
   }//GEN-LAST:event_smallZTextField_focusLostHandlerJTF1

   private void smallZTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallZTextField_ActionPerformed
      updateSmallZMove();
   }//GEN-LAST:event_smallZTextField_ActionPerformed

   private void mediumZTextField_focusLostHandlerJTF2(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mediumZTextField_focusLostHandlerJTF2
      updateMediumZMove();
   }//GEN-LAST:event_mediumZTextField_focusLostHandlerJTF2

   private void mediumZTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumZTextField_ActionPerformed
      updateMediumZMove();
   }//GEN-LAST:event_mediumZTextField_ActionPerformed

   private void mediumZUpButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumZUpButton_ActionPerformed
      try {
         setRelativeStagePosition(nf_.parse(mediumZTextField_.getText()).doubleValue());
      } catch(ParseException e) {
         // ignore if parsing fails
      }
   }//GEN-LAST:event_mediumZUpButton_ActionPerformed

   private void zDriveSelect_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zDriveSelect_ActionPerformed
      currentZDrive_ = (String) zDriveSelect_.getSelectedItem();
      if (initialized_) {
         prefs_.put(CURRENTZDRIVE, currentZDrive_);
         updateZMovements();   // Sets the small and medium Z movement to whatever we had for this new drive
         try {
            updateZPosLabel();
         } catch (Exception ex) {
            // ignore for now...
         }
      }
   }//GEN-LAST:event_zDriveSelect_ActionPerformed

   private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButton23ActionPerformed

   private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jButton24ActionPerformed

 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JButton get1PixelButton_;
    private javax.swing.JButton getFieldButton_;
    private javax.swing.JButton getPartFieldButton_;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton largeLeftButton_;
    private javax.swing.JButton largeRightButton_;
    private javax.swing.JButton largeXYDownButton_;
    private javax.swing.JTextField largeXYTextField_;
    private javax.swing.JButton largeXYUpButton_;
    private javax.swing.JButton mediumLeftButton_;
    private javax.swing.JButton mediumRightButton_;
    private javax.swing.JButton mediumXYDownButton_;
    private javax.swing.JTextField mediumXYTextField_;
    private javax.swing.JButton mediumXYUpButton_;
    private javax.swing.JButton mediumZDownButton_;
    private javax.swing.JTextField mediumZTextField_;
    private javax.swing.JButton mediumZUpButton_;
    private javax.swing.JButton smallLeftButton_;
    private javax.swing.JButton smallRightButton_;
    private javax.swing.JButton smallXYDownButton_;
    private javax.swing.JTextField smallXYTextField_;
    private javax.swing.JButton smallXYUpButton_;
    private javax.swing.JButton smallZDownButton_;
    private javax.swing.JTextField smallZTextField_;
    private javax.swing.JButton smallZUpButton_;
    private javax.swing.JPanel xyPanel_;
    private javax.swing.JComboBox zDriveSelect_;
    private javax.swing.JPanel zPanel_;
    private javax.swing.JLabel zPositionLabel_;
    // End of variables declaration//GEN-END:variables

   public void propertiesChangedAlert() {
   }

   public void propertyChangedAlert(String device, String property, String value) {
   }

   public void configGroupChangedAlert(String groupName, String newConfig) {
   }

   public void systemConfigurationLoaded() {
      initialize();
   }

   public void pixelSizeChangedAlert(double newPixelSizeUm) {
   }

   public void stagePositionChangedAlert(String deviceName, double pos) {
      if (deviceName.equals(currentZDrive_)) {
         zPositionLabel_.setText(nf_.format(pos) + " \u00B5m" );
      }
   }

   public void xyStagePositionChanged(String deviceName, double xPos, double yPos) {
   }

   public void exposureChanged(String cameraName, double newExposureTime) {
   }

}
