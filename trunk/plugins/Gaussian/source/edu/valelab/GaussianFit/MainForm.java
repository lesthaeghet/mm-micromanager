/*
 * MainForm.java
 *
 * Form showing the UI controlling tracking of single molecules using
 * Gaussian Fitting
 *
 * The real work is done in class GaussianTrackThread
 *
 * Created on Sep 15, 2010, 9:29:05 PM
 */

package edu.valelab.GaussianFit;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import java.awt.Polygon;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



/**
 *
 * @author nico
 */
public class MainForm extends javax.swing.JFrame implements ij.ImageListener{
   private static final String NOISETOLERANCE = "NoiseTolerance";
   private static final String PCF = "PhotonConversionFactor";
   private static final String GAIN = "Gain";
   private static final String PIXELSIZE = "PixelSize";
   private static final String TIMEINTERVALMS = "TimeIntervalMs";
   private static final String SIGMAMAX = "SigmaMax";
   private static final String SIGMAMIN = "SigmaMin";
   private static final String USEFILTER = "UseFilter";
   private static final String NRPHOTONSMIN = "NrPhotonsMin";
   private static final String NRPHOTONSMAX = "NrPhotonsMax";
   private static final String USENRPHOTONSFILTER = "UseNrPhotonsFilter";
   private static final String MAXITERATIONS = "MaxIterations";
   private static final String BOXSIZE = "BoxSize";
   private static final String FRAMEXPOS = "XPos";
   private static final String FRAMEYPOS = "YPos";
   private static final String FRAMEWIDTH = "Width";
   private static final String FRAMEHEIGHT = "Height";
   private static final String FITMODE = "FitMode";
   private static final String ENDTRACKBOOL = "EndTrackBoolean";
   private static final String ENDTRACKINT = "EndTrackAfterN";
   private static final String PREFILTER = "PreFilterType";

   // we are a singleton with only one window
   public static boolean WINDOWOPEN = false;

   Preferences prefs_;

   // Store values of dropdown menus:
   private int shape_ = 1;
   private int fitMode_ = 2;
   private FindLocalMaxima.FilterType preFilterType_ = FindLocalMaxima.FilterType.NONE;

   private FitAllThread ft_;

   private int lastFrame_ = -1;

    /**
     * Creates new form MainForm
     * 
     * @param gt - Gaussian Track plugin from which this form was invoked
     */
    public MainForm() {
       initComponents();

       if (prefs_ == null)
            prefs_ = Preferences.userNodeForPackage(this.getClass());
       noiseToleranceTextField_.setText(Integer.toString(prefs_.getInt(NOISETOLERANCE,100)));
       photonConversionTextField.setText(Double.toString(prefs_.getDouble(PCF, 10.41)));
       emGainTextField.setText(Double.toString(prefs_.getDouble(GAIN, 50)));
       pixelSizeTextField.setText(Double.toString(prefs_.getDouble(PIXELSIZE, 107.0)));
       baseLevelTextField.setText(Double.toString(prefs_.getDouble(TIMEINTERVALMS, 100)));
       minSigmaTextField.setText(Double.toString(prefs_.getDouble(SIGMAMIN, 100)));
       maxSigmaTextField.setText(Double.toString(prefs_.getDouble(SIGMAMAX, 200)));
       minNrPhotonsTextField.setText(Double.toString(prefs_.getDouble(NRPHOTONSMIN, 500)));
       maxNrPhotonsTextField.setText(Double.toString(prefs_.getDouble(NRPHOTONSMAX, 50000)));
       filterDataCheckBoxNrPhotons.setSelected(prefs_.getBoolean(USENRPHOTONSFILTER, false));
       fitMethodComboBox1.setSelectedIndex(prefs_.getInt(FITMODE, 0));
       maxIterationsTextField.setText(Integer.toString(prefs_.getInt(MAXITERATIONS, 250)));
       boxSizeTextField.setText(Integer.toString(prefs_.getInt(BOXSIZE, 16)));
       filterDataCheckBoxWidth.setSelected(prefs_.getBoolean(USEFILTER, false));
       preFilterComboBox_.setSelectedIndex(prefs_.getInt(PREFILTER, 0));
       endTrackCheckBox_.setSelected(prefs_.getBoolean(ENDTRACKBOOL, false));
       endTrackSpinner_.setValue(prefs_.getInt(ENDTRACKINT, 0));
       
       DocumentListener updateNoiseOverlay = new DocumentListener() {

                  public void changedUpdate(DocumentEvent documentEvent) {
                     updateDisplay();
                  }

                  public void insertUpdate(DocumentEvent documentEvent) {
                     updateDisplay();
                  }

                  public void removeUpdate(DocumentEvent documentEvent) {
                     updateDisplay();
                  }
                  private void updateDisplay() {
                     if (readParmsButton_.isSelected()) {
                        showNoiseTolerance();
                     }
                  }
               };
       
       noiseToleranceTextField_.getDocument().addDocumentListener(updateNoiseOverlay);
       boxSizeTextField.getDocument().addDocumentListener(updateNoiseOverlay);
               

       setTitle("Localization Microscopy");
       setBounds(prefs_.getInt(FRAMEXPOS, 100), prefs_.getInt(FRAMEYPOS, 100), 250, 550);
              // prefs_.getInt(FRAMEWIDTH, 247), prefs_.getInt(FRAMEHEIGHT, 367));
       setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        filterDataCheckBoxWidth = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        photonConversionTextField = new javax.swing.JTextField();
        emGainTextField = new javax.swing.JTextField();
        baseLevelTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        minSigmaTextField = new javax.swing.JTextField();
        trackButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        noiseToleranceTextField_ = new javax.swing.JTextField();
        pixelSizeTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        fitAllButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        preFilterComboBox_ = new javax.swing.JComboBox();
        fitDimensionsComboBox1 = new javax.swing.JComboBox();
        timeIntervalTextField1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        maxIterationsTextField = new javax.swing.JTextField();
        maxSigmaTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        boxSizeTextField = new javax.swing.JTextField();
        stopButton = new javax.swing.JButton();
        filterDataCheckBoxNrPhotons = new javax.swing.JCheckBox();
        minNrPhotonsTextField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        maxNrPhotonsTextField = new javax.swing.JTextField();
        showButton = new javax.swing.JButton();
        endTrackCheckBox_ = new javax.swing.JCheckBox();
        endTrackSpinner_ = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        readParmsButton_ = new javax.swing.JToggleButton();
        jLabel20 = new javax.swing.JLabel();
        fitMethodComboBox1 = new javax.swing.JComboBox();
        showOverlay_1 = new javax.swing.JToggleButton();

        setBounds(new java.awt.Rectangle(0, 22, 250, 550));
        setMaximumSize(new java.awt.Dimension(250, 550));
        setMinimumSize(new java.awt.Dimension(250, 550));
        setPreferredSize(new java.awt.Dimension(250, 550));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(null);

        jLabel1.setText("Fit Parameters...");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(20, 230, 101, 16);

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel3.setText("Fitter");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(50, 270, 26, 13);

        jLabel4.setText("Filter Data...");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(30, 340, 87, 20);

        filterDataCheckBoxWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterDataCheckBoxWidthActionPerformed(evt);
            }
        });
        getContentPane().add(filterDataCheckBoxWidth);
        filterDataCheckBoxWidth.setBounds(20, 360, 28, 20);
        getContentPane().add(jLabel5);
        jLabel5.setBounds(0, 0, 0, 0);

        jLabel6.setText("Imaging parameters...");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(20, 10, 142, 16);

        photonConversionTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        photonConversionTextField.setText("10.41");
        getContentPane().add(photonConversionTextField);
        photonConversionTextField.setBounds(170, 50, 67, 20);

        emGainTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        emGainTextField.setText("50");
        getContentPane().add(emGainTextField);
        emGainTextField.setBounds(170, 70, 67, 19);

        baseLevelTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        baseLevelTextField.setText("100");
        baseLevelTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseLevelTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(baseLevelTextField);
        baseLevelTextField.setBounds(170, 130, 67, 20);

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel7.setText("Photon Conversion factor");
        getContentPane().add(jLabel7);
        jLabel7.setBounds(40, 50, 123, 13);

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel8.setText("Linear (EM) Gain");
        getContentPane().add(jLabel8);
        jLabel8.setBounds(40, 70, 78, 13);

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel9.setText("Base Level(counts)");
        getContentPane().add(jLabel9);
        jLabel9.setBounds(40, 130, 122, 13);
        getContentPane().add(jSeparator4);
        jSeparator4.setBounds(496, 191, 1, 9);

        minSigmaTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        minSigmaTextField.setText("100");
        minSigmaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minSigmaTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(minSigmaTextField);
        minSigmaTextField.setBounds(50, 360, 40, 30);

        trackButton.setText("Track");
        trackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackButtonActionPerformed(evt);
            }
        });
        getContentPane().add(trackButton);
        trackButton.setBounds(170, 460, 75, 29);
        getContentPane().add(jSeparator1);
        jSeparator1.setBounds(20, 220, 210, 10);
        getContentPane().add(jSeparator2);
        jSeparator2.setBounds(20, 150, 220, 10);
        getContentPane().add(jSeparator3);
        jSeparator3.setBounds(20, 330, 220, 10);

        noiseToleranceTextField_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        noiseToleranceTextField_.setText("2000");
        noiseToleranceTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseToleranceTextField_ActionPerformed(evt);
            }
        });
        noiseToleranceTextField_.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                noiseToleranceTextField_FocusLost(evt);
            }
        });
        noiseToleranceTextField_.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                noiseToleranceTextField_PropertyChange(evt);
            }
        });
        noiseToleranceTextField_.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                noiseToleranceTextField_KeyTyped(evt);
            }
        });
        getContentPane().add(noiseToleranceTextField_);
        noiseToleranceTextField_.setBounds(180, 200, 60, 20);

        pixelSizeTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pixelSizeTextField.setText("0.8");
        getContentPane().add(pixelSizeTextField);
        pixelSizeTextField.setBounds(170, 90, 67, 20);

        jLabel13.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel13.setText("PixelSize(nm)");
        getContentPane().add(jLabel13);
        jLabel13.setBounds(40, 90, 122, 13);

        fitAllButton.setText("Fit");
        fitAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitAllButtonActionPerformed(evt);
            }
        });
        getContentPane().add(fitAllButton);
        fitAllButton.setBounds(10, 460, 80, 30);

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel10.setText("nm < Width <");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(90, 370, 80, 10);
        getContentPane().add(jSeparator5);
        jSeparator5.setBounds(30, 450, 220, 10);

        jLabel11.setText("Find Maxima Settings...");
        getContentPane().add(jLabel11);
        jLabel11.setBounds(20, 160, 147, 16);

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel12.setText("Pre-Filter");
        getContentPane().add(jLabel12);
        jLabel12.setBounds(90, 180, 60, 13);

        jLabel14.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel14.setText("Dimensions");
        getContentPane().add(jLabel14);
        jLabel14.setBounds(50, 250, 56, 13);

        preFilterComboBox_.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        preFilterComboBox_.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Gaussian1-5" }));
        preFilterComboBox_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preFilterComboBox_ActionPerformed(evt);
            }
        });
        getContentPane().add(preFilterComboBox_);
        preFilterComboBox_.setBounds(150, 180, 90, 20);

        fitDimensionsComboBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        fitDimensionsComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3" }));
        fitDimensionsComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitDimensionsComboBox1ActionPerformed(evt);
            }
        });
        getContentPane().add(fitDimensionsComboBox1);
        fitDimensionsComboBox1.setBounds(150, 240, 90, 27);

        timeIntervalTextField1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        timeIntervalTextField1.setText("0.8");
        getContentPane().add(timeIntervalTextField1);
        timeIntervalTextField1.setBounds(170, 110, 67, 20);

        jLabel15.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel15.setText("Time Interval (ms)");
        getContentPane().add(jLabel15);
        jLabel15.setBounds(40, 110, 122, 13);

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel17.setText("Max Iterations");
        getContentPane().add(jLabel17);
        jLabel17.setBounds(50, 290, 90, 13);

        maxIterationsTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        maxIterationsTextField.setText("250");
        getContentPane().add(maxIterationsTextField);
        maxIterationsTextField.setBounds(170, 290, 70, 20);

        maxSigmaTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        maxSigmaTextField.setText("200");
        maxSigmaTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxSigmaTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(maxSigmaTextField);
        maxSigmaTextField.setBounds(160, 360, 50, 30);

        jLabel18.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel18.setText("nm");
        getContentPane().add(jLabel18);
        jLabel18.setBounds(210, 370, 20, 10);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel2.setText("Box Size (pixels)");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(50, 310, 90, 10);

        boxSizeTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boxSizeTextField.setText("16");
        getContentPane().add(boxSizeTextField);
        boxSizeTextField.setBounds(170, 310, 70, 25);

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        getContentPane().add(stopButton);
        stopButton.setBounds(140, 490, 80, 30);

        filterDataCheckBoxNrPhotons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterDataCheckBoxNrPhotonsActionPerformed(evt);
            }
        });
        getContentPane().add(filterDataCheckBoxNrPhotons);
        filterDataCheckBoxNrPhotons.setBounds(20, 390, 28, 20);

        minNrPhotonsTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        minNrPhotonsTextField.setText("100");
        minNrPhotonsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minNrPhotonsTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(minNrPhotonsTextField);
        minNrPhotonsTextField.setBounds(50, 390, 50, 30);

        jLabel16.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel16.setText("< # photons <");
        getContentPane().add(jLabel16);
        jLabel16.setBounds(100, 400, 80, 10);

        maxNrPhotonsTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        maxNrPhotonsTextField.setText("200");
        maxNrPhotonsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxNrPhotonsTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(maxNrPhotonsTextField);
        maxNrPhotonsTextField.setBounds(180, 390, 60, 30);

        showButton.setText("Show");
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });
        getContentPane().add(showButton);
        showButton.setBounds(50, 490, 80, 30);

        endTrackCheckBox_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        endTrackCheckBox_.setText("End track when missing");
        endTrackCheckBox_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endTrackCheckBox_ActionPerformed(evt);
            }
        });
        getContentPane().add(endTrackCheckBox_);
        endTrackCheckBox_.setBounds(20, 420, 150, 23);

        endTrackSpinner_.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        getContentPane().add(endTrackSpinner_);
        endTrackSpinner_.setBounds(160, 420, 50, 28);

        jLabel19.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel19.setText("frames");
        getContentPane().add(jLabel19);
        jLabel19.setBounds(210, 420, 40, 30);

        readParmsButton_.setText("read");
        readParmsButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readParmsButton_ActionPerformed(evt);
            }
        });
        getContentPane().add(readParmsButton_);
        readParmsButton_.setBounds(170, 20, 60, 20);

        jLabel20.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel20.setText("Noise tolerance");
        getContentPane().add(jLabel20);
        jLabel20.setBounds(90, 200, 76, 20);

        fitMethodComboBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        fitMethodComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Simplex", "LevenBerg-Marq" }));
        fitMethodComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitMethodComboBox1ActionPerformed(evt);
            }
        });
        getContentPane().add(fitMethodComboBox1);
        fitMethodComboBox1.setBounds(150, 260, 90, 27);

        showOverlay_1.setText("show");
        showOverlay_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOverlay_1ActionPerformed(evt);
            }
        });
        getContentPane().add(showOverlay_1);
        showOverlay_1.setBounds(20, 200, 60, 20);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void filterDataCheckBoxWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterDataCheckBoxWidthActionPerformed

    }//GEN-LAST:event_filterDataCheckBoxWidthActionPerformed

    private void noiseToleranceTextField_PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_noiseToleranceTextField_PropertyChange
       
       
    }//GEN-LAST:event_noiseToleranceTextField_PropertyChange

    private void trackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackButtonActionPerformed
       GaussianTrackThread tT = new GaussianTrackThread(FindLocalMaxima.FilterType.NONE);
       updateValues(tT);
       
       // Execute on another thread,
       // use tT.trackGaussians to run it on the same thread
       tT.init();
    }//GEN-LAST:event_trackButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
       WINDOWOPEN = false;
    }//GEN-LAST:event_formWindowClosed

    private void fitAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitAllButtonActionPerformed
       
       if (ft_ == null || !ft_.isRunning()) {
          ft_ = new FitAllThread(shape_, fitMode_, preFilterType_);
          updateValues(ft_);
          ft_.init();
       } else {
          JOptionPane.showMessageDialog(null, "Already running fitting analysis");
       }
    }//GEN-LAST:event_fitAllButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
       prefs_.put(NOISETOLERANCE, noiseToleranceTextField_.getText());
       prefs_.putDouble(PCF, Double.parseDouble(photonConversionTextField.getText()));
       prefs_.putDouble(GAIN, Double.parseDouble(emGainTextField.getText()));
       prefs_.putDouble(PIXELSIZE, Double.parseDouble(pixelSizeTextField.getText()));
       prefs_.putDouble(TIMEINTERVALMS, Double.parseDouble(baseLevelTextField.getText()));
       prefs_.putBoolean(USEFILTER, filterDataCheckBoxWidth.isSelected());
       prefs_.putDouble(SIGMAMIN, Double.parseDouble(minSigmaTextField.getText()));
       prefs_.putDouble(SIGMAMAX, Double.parseDouble(maxSigmaTextField.getText()));
       prefs_.putBoolean(USENRPHOTONSFILTER, filterDataCheckBoxNrPhotons.isSelected());
       prefs_.putDouble(NRPHOTONSMIN, Double.parseDouble(minNrPhotonsTextField.getText()));
       prefs_.putDouble(NRPHOTONSMAX, Double.parseDouble(maxNrPhotonsTextField.getText()));
       prefs_.putInt(MAXITERATIONS, Integer.parseInt(maxIterationsTextField.getText()));
       prefs_.putInt(BOXSIZE, Integer.parseInt(boxSizeTextField.getText()));
       prefs_.putInt(PREFILTER, preFilterComboBox_.getSelectedIndex());
       prefs_.putInt(FRAMEXPOS, getX());
       prefs_.putInt(FRAMEYPOS, getY());
       prefs_.putInt(FRAMEWIDTH, getWidth());
       prefs_.putInt(FRAMEHEIGHT, this.getHeight());
       prefs_.putBoolean(ENDTRACKBOOL, endTrackCheckBox_.isSelected() );
       prefs_.putInt(ENDTRACKINT, (Integer) endTrackSpinner_.getValue() );
       prefs_.putInt(FITMODE, fitMethodComboBox1.getSelectedIndex());
              
       this.setVisible(false);
    }//GEN-LAST:event_formWindowClosing

   @Override
    public void dispose() {
       formWindowClosing(null);
    }

    private void preFilterComboBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preFilterComboBox_ActionPerformed
       String item = (String) preFilterComboBox_.getSelectedItem();
       if (item.equals("None"))
          preFilterType_ = FindLocalMaxima.FilterType.NONE;
       if (item.equals("Gaussian1-5"))
          preFilterType_ = FindLocalMaxima.FilterType.GAUSSIAN1_5;
       if (readParmsButton_.isSelected())
         showNoiseTolerance();
    }//GEN-LAST:event_preFilterComboBox_ActionPerformed

    private void fitDimensionsComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitDimensionsComboBox1ActionPerformed
       shape_ = fitDimensionsComboBox1.getSelectedIndex() + 1;
    }//GEN-LAST:event_fitDimensionsComboBox1ActionPerformed

    private void baseLevelTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseLevelTextFieldActionPerformed
    }//GEN-LAST:event_baseLevelTextFieldActionPerformed

    private void maxSigmaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxSigmaTextFieldActionPerformed
       if (Double.parseDouble(maxSigmaTextField.getText()) <=
               Double.parseDouble(minSigmaTextField.getText() ))
          minSigmaTextField.setText( Double.toString
                  (Double.parseDouble(maxSigmaTextField.getText()) - 1));
    }//GEN-LAST:event_maxSigmaTextFieldActionPerformed

    private void minSigmaTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minSigmaTextFieldActionPerformed
       if (Double.parseDouble(minSigmaTextField.getText()) >=
               Double.parseDouble(maxSigmaTextField.getText() ))
          maxSigmaTextField.setText( Double.toString
                  (Double.parseDouble(minSigmaTextField.getText()) + 1));
    }//GEN-LAST:event_minSigmaTextFieldActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
       if (ft_ != null && ft_.isRunning())
          ft_.stop();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void filterDataCheckBoxNrPhotonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterDataCheckBoxNrPhotonsActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_filterDataCheckBoxNrPhotonsActionPerformed

    private void minNrPhotonsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minNrPhotonsTextFieldActionPerformed
        if (Double.parseDouble(minNrPhotonsTextField.getText()) >=
               Double.parseDouble(maxNrPhotonsTextField.getText() ))
          minNrPhotonsTextField.setText( Double.toString
                  (Double.parseDouble(maxNrPhotonsTextField.getText()) - 1));
    }//GEN-LAST:event_minNrPhotonsTextFieldActionPerformed

    private void maxNrPhotonsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxNrPhotonsTextFieldActionPerformed
        if (Double.parseDouble(maxNrPhotonsTextField.getText()) <=
           Double.parseDouble(minNrPhotonsTextField.getText() ))
        maxNrPhotonsTextField.setText( Double.toString
           (Double.parseDouble(minNrPhotonsTextField.getText()) + 1));
    }//GEN-LAST:event_maxNrPhotonsTextFieldActionPerformed

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
       DataCollectionForm dcForm = DataCollectionForm.getInstance();
       dcForm.setVisible(true);
    }//GEN-LAST:event_showButtonActionPerformed

   private void endTrackCheckBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endTrackCheckBox_ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_endTrackCheckBox_ActionPerformed

   private void noiseToleranceTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noiseToleranceTextField_ActionPerformed
      //if (showOverlay_.isSelected())
      //   showNoiseTolerance();
   }//GEN-LAST:event_noiseToleranceTextField_ActionPerformed

   private void showNoiseTolerance() {
       ImagePlus siPlus;
       try {
          siPlus = IJ.getImage();
       } catch (Exception e) {
          return;
       }

       // Roi originalRoi = siPlus.getRoi();
       // Find maximum in Roi, might not be needed....
      try {
         int val = Integer.parseInt(noiseToleranceTextField_.getText());
         int halfSize = (int) Integer.parseInt(boxSizeTextField.getText()) / 2;
         Polygon pol = FindLocalMaxima.FindMax(siPlus, halfSize, val, preFilterType_);
         // pol = FindLocalMaxima.noiseFilter(siPlus.getProcessor(), pol, val);
         Overlay ov = new Overlay();
         for (int i = 0; i < pol.npoints; i++) {
            int x = pol.xpoints[i];
            int y = pol.ypoints[i];
            ov.add(new Roi(x - halfSize, y - halfSize, 2 * halfSize, 2 * halfSize));
         }
         siPlus.setOverlay(ov);
         siPlus.setHideOverlay(false);
         ImagePlus.addImageListener(this);
      } catch (NumberFormatException nfEx) {
         // nothing to do
      }
   }
   
   private void noiseToleranceTextField_FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_noiseToleranceTextField_FocusLost
      // if (showOverlay_.isSelected())
      //    showNoiseTolerance();
   }//GEN-LAST:event_noiseToleranceTextField_FocusLost

   private void readParmsButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readParmsButton_ActionPerformed
      if (readParmsButton_.isSelected()) {
         showNoiseTolerance();
         readParmsButton_.setText("hide");
      } else {
         ImagePlus siPlus;
       try {
          siPlus = IJ.getImage();
       } catch (Exception e) {
          return;
       }
       siPlus.setHideOverlay(true);
       readParmsButton_.setText("show");
      }
   }//GEN-LAST:event_readParmsButton_ActionPerformed

   private void noiseToleranceTextField_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_noiseToleranceTextField_KeyTyped

   }//GEN-LAST:event_noiseToleranceTextField_KeyTyped

   private void fitMethodComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitMethodComboBox1ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_fitMethodComboBox1ActionPerformed

   private void showOverlay_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showOverlay_1ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_showOverlay_1ActionPerformed

    public void updateValues(GaussianInfo tT) {
       tT.setNoiseTolerance(Integer.parseInt(noiseToleranceTextField_.getText()));
       tT.setPhotonConversionFactor(Double.parseDouble(photonConversionTextField.getText()));
       tT.setGain(Double.parseDouble(emGainTextField.getText()));
       tT.setPixelSize(Float.parseFloat(pixelSizeTextField.getText()));
       tT.setTimeIntervalMs(Double.parseDouble(timeIntervalTextField1.getText()));
       tT.setBaseLevel(Double.parseDouble(baseLevelTextField.getText()));
       tT.setUseWidthFilter(filterDataCheckBoxWidth.isSelected());
       tT.setSigmaMin(Double.parseDouble(minSigmaTextField.getText()));
       tT.setSigmaMax(Double.parseDouble(maxSigmaTextField.getText()));
       tT.setUseNrPhotonsFilter(filterDataCheckBoxNrPhotons.isSelected());
       tT.setNrPhotonsMin(Double.parseDouble(minNrPhotonsTextField.getText()));
       tT.setNrPhotonsMax(Double.parseDouble(maxNrPhotonsTextField.getText()));
       tT.setMaxIterations(Integer.parseInt(maxIterationsTextField.getText()));
       tT.setBoxSize(Integer.parseInt(boxSizeTextField.getText()));
       tT.setShape(fitDimensionsComboBox1.getSelectedIndex() + 1);
       tT.setFitMode(fitMethodComboBox1.getSelectedIndex() + 1);
       tT.setEndTrackBool(endTrackCheckBox_.isSelected());
       tT.setEndTrackAfterNFrames((Integer)endTrackSpinner_.getValue());      
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField baseLevelTextField;
    private javax.swing.JTextField boxSizeTextField;
    private javax.swing.JTextField emGainTextField;
    private javax.swing.JCheckBox endTrackCheckBox_;
    private javax.swing.JSpinner endTrackSpinner_;
    private javax.swing.JCheckBox filterDataCheckBoxNrPhotons;
    private javax.swing.JCheckBox filterDataCheckBoxWidth;
    private javax.swing.JButton fitAllButton;
    private javax.swing.JComboBox fitDimensionsComboBox1;
    private javax.swing.JComboBox fitMethodComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextField maxIterationsTextField;
    private javax.swing.JTextField maxNrPhotonsTextField;
    private javax.swing.JTextField maxSigmaTextField;
    private javax.swing.JTextField minNrPhotonsTextField;
    private javax.swing.JTextField minSigmaTextField;
    private javax.swing.JTextField noiseToleranceTextField_;
    private javax.swing.JTextField photonConversionTextField;
    private javax.swing.JTextField pixelSizeTextField;
    private javax.swing.JComboBox preFilterComboBox_;
    private javax.swing.JToggleButton readParmsButton_;
    private javax.swing.JButton showButton;
    private javax.swing.JToggleButton showOverlay_1;
    private javax.swing.JButton stopButton;
    private javax.swing.JTextField timeIntervalTextField1;
    private javax.swing.JButton trackButton;
    // End of variables declaration//GEN-END:variables

   public void imageOpened(ImagePlus ip) {
      //throw new UnsupportedOperationException("Not supported yet.");
   }

   public void imageClosed(ImagePlus ip) {
      //throw new UnsupportedOperationException("Not supported yet.");
   }

   public void imageUpdated(ImagePlus ip) {
      if (readParmsButton_.isSelected()) {
         
         // note that there is confusion about frames versus slices
         int frame = 1;
         if (ip.getNFrames() > 1)
            frame = ip.getFrame();
         else if (ip.getNSlices() > 1)
            frame = ip.getSlice();
         
         if (lastFrame_ != frame) {
            lastFrame_ = frame;
            showNoiseTolerance();
         }
      }
   }

}
