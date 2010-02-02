///////////////////////////////////////////////////////////////////////////////
//FILE:          AcqControlDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, Dec 1, 2005
//
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// CVS:          $Id$
package org.micromanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.micromanager.api.AcquisitionEngine;
import org.micromanager.api.DeviceControlGUI;
import org.micromanager.metadata.MMAcqDataException;
import org.micromanager.metadata.WellAcquisitionData;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.ColorEditor;
import org.micromanager.utils.ColorRenderer;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.DisplayMode;
import org.micromanager.utils.GUIColors;
import org.micromanager.utils.MMException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.PositionMode;
import org.micromanager.utils.SliceMode;

import com.swtdesigner.SwingResourceManager;
import java.awt.Dimension;
import org.micromanager.acquisition.ComponentTitledBorder;
import org.micromanager.utils.ReportingUtils;

/**
 * Time-lapse, channel and z-stack acquisition setup dialog.
 * This dialog specifies all parameters for the Image5D acquisition. 
 *
 */
public class AcqControlDlg extends JDialog implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    protected JButton listButton_;
    private JButton afButton_;
 
    private JSpinner afSkipInterval_;
    private JComboBox sliceModeCombo_;
    protected JComboBox posModeCombo_;
    public static final String NEW_ACQFILE_NAME = "MMAcquistion.xml";
    public static final String ACQ_SETTINGS_NODE = "AcquistionSettings";
    public static final String COLOR_SETTINGS_NODE = "ColorSettings";
    private JComboBox channelGroupCombo_;
    private JTextArea commentTextArea_;
    private JComboBox zValCombo_;
    private JTextField nameField_;
    private JTextField rootField_;
    private JTextArea summaryTextArea_;
    private JComboBox timeUnitCombo_;
    private JFormattedTextField interval_;
    private JFormattedTextField zStep_;
    private JFormattedTextField zTop_;
    private JFormattedTextField zBottom_;
    private AcquisitionEngine acqEng_;
    private JScrollPane channelTablePane_;
    private JTable channelTable_;
    private JSpinner numFrames_;
    private ChannelTableModel model_;
    private Preferences prefs_;
    private Preferences acqPrefs_;
    private Preferences colorPrefs_;
    private File acqFile_;
    private String acqDir_;
    private int zVals_ = 0;
    private JButton setBottomButton_;
    private JButton setTopButton_;
    private JComboBox displayModeCombo_;
    private DeviceControlGUI gui_;
    private GUIColors guiColors_;
    private NumberFormat numberFormat_;
    private JLabel namePrefixLabel_;
    private JLabel rootLabel_;
    private JLabel commentLabel_;
    private JButton browseRootButton_;
    private JLabel displayMode_;
    private JCheckBox stackKeepShutterOpenCheckBox_;
    private JCheckBox chanKeepShutterOpenCheckBox_;

    // persistent properties (app settings)
    private static final String ACQ_CONTROL_X = "x";
    private static final String ACQ_CONTROL_Y = "y";
    private static final String ACQ_FILE_DIR = "dir";
    private static final String ACQ_INTERVAL = "acqInterval";
    private static final String ACQ_TIME_UNIT = "acqTimeInit";
    private static final String ACQ_ZBOTTOM = "acqZbottom";
    private static final String ACQ_ZTOP = "acqZtop";
    private static final String ACQ_ZSTEP = "acqZstep";
    private static final String ACQ_ENABLE_SLICE_SETTINGS = "enableSliceSettings";
    private static final String ACQ_ENABLE_MULTI_POSITION = "enableMultiPosition";
    private static final String ACQ_ENABLE_MULTI_FRAME = "enableMultiFrame";
    private static final String ACQ_ENABLE_MULTI_CHANNEL = "enableMultiChannels";
    private static final String ACQ_SLICE_MODE = "sliceMode";
    private static final String ACQ_POSITION_MODE = "positionMode";
    private static final String ACQ_NUMFRAMES = "acqNumframes";
    private static final String ACQ_CHANNEL_GROUP = "acqChannelGroup";
    private static final String ACQ_NUM_CHANNELS = "acqNumchannels";
    private static final String ACQ_CHANNELS_KEEP_SHUTTER_OPEN = "acqChannelsKeepShutterOpen";
    private static final String ACQ_STACK_KEEP_SHUTTER_OPEN = "acqStackKeepShutterOpen";
    private static final String CHANNEL_NAME_PREFIX = "acqChannelName";
    private static final String CHANNEL_EXPOSURE_PREFIX = "acqChannelExp";
    private static final String CHANNEL_ZOFFSET_PREFIX = "acqChannelZOffset";
    private static final String CHANNEL_DOZSTACK_PREFIX = "acqChannelDoZStack";
    private static final String CHANNEL_CONTRAST8_MIN_PREFIX = "acqChannel8ContrastMin";
    private static final String CHANNEL_CONTRAST8_MAX_PREFIX = "acqChannel8ContrastMax";
    private static final String CHANNEL_CONTRAST16_MIN_PREFIX = "acqChannel16ContrastMin";
    private static final String CHANNEL_CONTRAST16_MAX_PREFIX = "acqChannel16ContrastMax";
    private static final String CHANNEL_COLOR_R_PREFIX = "acqChannelColorR";
    private static final String CHANNEL_COLOR_G_PREFIX = "acqChannelColorG";
    private static final String CHANNEL_COLOR_B_PREFIX = "acqChannelColorB";
    private static final String CHANNEL_SKIP_PREFIX = "acqSkip";
    private static final String ACQ_Z_VALUES = "acqZValues";
    private static final String ACQ_DIR_NAME = "acqDirName";
    private static final String ACQ_ROOT_NAME = "acqRootName";
    private static final String ACQ_SAVE_FILES = "acqSaveFiles";
    private static final String ACQ_DISPLAY_MODE = "acqDisplayMode";
    private static final String ACQ_AF_ENABLE = "autofocus_enabled";
    private static final String ACQ_AF_SKIP_INTERVAL = "autofocusSkipInterval";
    private static final String ACQ_COLUMN_WIDTH = "column_width";
    private static final String ACQ_COLUMN_ORDER = "column_order";
    private static final int ACQ_DEFAULT_COLUMN_WIDTH = 77;

    private int columnWidth_[];
    private int columnOrder_[];
    private CheckBoxPanel framesPanel_;
    private CheckBoxPanel channelsPanel_;
    private CheckBoxPanel slicesPanel_;
    protected CheckBoxPanel positionsPanel_;
    private JPanel acquisitionOrderPanel_;
    private CheckBoxPanel afPanel_;
    private JPanel summaryPanel_;
    private CheckBoxPanel savePanel_;
    private Border dayBorder_;
    private Border nightBorder_;
    private Vector<JPanel> panelList_;
    private boolean disableGUItoSettings_ = false;






    /**
     * File filter class for Open/Save file choosers
     */
    private class AcqFileFilter extends FileFilter {

        final private String EXT_BSH;
        final private String DESCRIPTION;

        public AcqFileFilter() {
            super();
            EXT_BSH = new String("xml");
            DESCRIPTION = new String("XML files (*.xml)");
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            if (EXT_BSH.equals(getExtension(f))) {
                return true;
            }
            return false;
        }

        public String getDescription() {
            return DESCRIPTION;
        }

        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    /**
     * Data representation class for the channels list
     */
    public class ChannelTableModel extends AbstractTableModel implements TableModelListener {

        private static final long serialVersionUID = 3290621191844925827L;
        private ArrayList<ChannelSpec> channels_;
        private AcquisitionEngine acqEng_;
        public final String[] COLUMN_NAMES = new String[]{
            "Configuration",
            "Exposure",
            "Z-offset",
            "Z-stack",
            "Skip Fr.",
            "Color"
        };

        public ChannelTableModel(AcquisitionEngine eng) {
            acqEng_ = eng;
            addTableModelListener(this);
        }

        public int getRowCount() {
            if (channels_ == null) {
                return 0;
            } else {
                return channels_.size();
            }
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (channels_ != null && rowIndex < channels_.size()) {
                if (columnIndex == 0) {
                    return channels_.get(rowIndex).config_;
                } else if (columnIndex == 1) {
                    return new Double(channels_.get(rowIndex).exposure_);
                } else if (columnIndex == 2) {
                    return new Double(channels_.get(rowIndex).zOffset_);
                } else if (columnIndex == 3) {
                    return new Boolean(channels_.get(rowIndex).doZStack_);
                } else if (columnIndex == 4) {
                    return new Integer(channels_.get(rowIndex).skipFactorFrame_);
                } else if (columnIndex == 5) {
                    return channels_.get(rowIndex).color_;
                }
            }
            return null;
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public void setValueAt(Object value, int row, int col) {
            if (row >= channels_.size() || value == null) {
                return;
            }

            ChannelSpec channel = channels_.get(row);

            if (col == 0) {
                channel.config_ = value.toString();
            } else if (col == 1) {
                channel.exposure_ = ((Double) value).doubleValue();
            } else if (col == 2) {
                channel.zOffset_ = ((Double) value).doubleValue();
            } else if (col == 3) {
                channel.doZStack_ = (Boolean) value;
            } else if (col == 4) {
                channel.skipFactorFrame_ = ((Integer) value).intValue();
            } else if (col == 5) {
                channel.color_ = (Color) value;
            }

            acqEng_.setChannel(row, channel);
            repaint();
        }

        public boolean isCellEditable(int nRow, int nCol) {
            if (nCol == 3) {
                if (!acqEng_.isZSliceSettingEnabled()) {
                    return false;
                }
            }

            return true;
        }

        /*
         * Catched events thrown by the ColorEditor
         * Will write the new color into the Color Prefs
         */
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            if (row < 0) {
                return;
            }
            int col = e.getColumn();
            if (col < 0) {
                return;
            }
            ChannelSpec channel = channels_.get(row);
            TableModel model = (TableModel) e.getSource();
            if (col == 5) {
                Color color = (Color) model.getValueAt(row, col);
                colorPrefs_.putInt("Color_" + acqEng_.getChannelGroup() + "_" + channel.config_, color.getRGB());
            }
        }

        public void setChannels(ArrayList<ChannelSpec> ch) {
            channels_ = ch;
        }

        public ArrayList<ChannelSpec> getChannels() {
            return channels_;
        }

        public void addNewChannel() {
            ChannelSpec channel = new ChannelSpec();
            if (acqEng_.getChannelConfigs().length > 0) {
                channel.config_ = acqEng_.getChannelConfigs()[0];
                channel.color_ = new Color(colorPrefs_.getInt("Color_" + acqEng_.getChannelGroup() + "_" + channel.config_, Color.white.getRGB()));
                channels_.add(channel);
            }
        }

        public void removeChannel(int chIndex) {
            if (chIndex >= 0 && chIndex < channels_.size()) {
                channels_.remove(chIndex);
            }
        }

        public int rowDown(int rowIdx) {
            if (rowIdx >= 0 && rowIdx < channels_.size() - 1) {
                ChannelSpec channel = channels_.get(rowIdx);
                channels_.add(rowIdx + 2, channel);
                channels_.remove(rowIdx);
                return rowIdx + 1;
            }
            return rowIdx;
        }

        public int rowUp(int rowIdx) {
            if (rowIdx >= 1 && rowIdx < channels_.size()) {
                ChannelSpec channel = channels_.get(rowIdx);
                channels_.add(rowIdx - 1, channel);
                channels_.remove(rowIdx + 1);
                return rowIdx - 1;
            }
            return rowIdx;
        }

        public String[] getAvailableChannels() {
            return acqEng_.getChannelConfigs();
        }

        /**
         * Remove all channels from the list which are not compatible with
         * the current acquisition settings
         */
        public void cleanUpConfigurationList() {
            String config;
            for (Iterator<ChannelSpec> it = channels_.iterator(); it.hasNext();) {
                config = it.next().config_;
                if (!config.contentEquals("") && !acqEng_.isConfigAvailable(config)) {
                    it.remove();
                }
            }
            fireTableStructureChanged();
        }

        /**
         * reports if the same channel name is used twice
         */
        public boolean duplicateChannels() {
            for (int i = 0; i < channels_.size() - 1; i++) {
                for (int j = i + 1; j < channels_.size(); j++) {
                    if (channels_.get(i).config_.equals(channels_.get(j).config_)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    /**
     * Cell editing using either JTextField or JComboBox depending on whether the
     * property enforces a set of allowed values.
     */
    public class ChannelCellEditor extends AbstractCellEditor implements TableCellEditor {

        private static final long serialVersionUID = -8374637422965302637L;
        JTextField text_ = new JTextField();
        JComboBox combo_ = new JComboBox();
        JCheckBox checkBox_ = new JCheckBox();
        JLabel colorLabel_ = new JLabel();
        int editCol_ = -1;
        int editRow_ = -1;
        ChannelSpec channel_ = null;

        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int colIndex) {

            if (isSelected) {
                // cell (and perhaps other cells) are selected
            }

            ChannelTableModel model = (ChannelTableModel) table.getModel();
            ArrayList<ChannelSpec> channels = model.getChannels();
            final ChannelSpec channel = channels.get(rowIndex);
            channel_ = channel;

            colIndex = table.convertColumnIndexToModel(colIndex);

            // Configure the component with the specified value
            editRow_ = rowIndex;
            editCol_ = colIndex;
            if (colIndex == 1 || colIndex == 2) {
                // exposure and z offset
                text_.setText(((Double) value).toString());
                return text_;
            } else if (colIndex == 3) {
                checkBox_.setSelected((Boolean) value);
                return checkBox_;
            } else if (colIndex == 4) {
                // skip
                text_.setText(((Integer) value).toString());
                return text_;
            } else if (colIndex == 0) {
                // channel
                combo_.removeAllItems();

                // remove old listeners
                ActionListener[] l = combo_.getActionListeners();
                for (int i = 0; i < l.length; i++) {
                    combo_.removeActionListener(l[i]);
                }
                combo_.removeAllItems();

                String configs[] = model.getAvailableChannels();
                for (int i = 0; i < configs.length; i++) {
                    combo_.addItem(configs[i]);
                }
                combo_.setSelectedItem(channel.config_);
                channel.color_ = new Color(colorPrefs_.getInt("Color_" + acqEng_.getChannelGroup() + "_" + channel.config_, Color.white.getRGB()));

                // end editing on selection change
                combo_.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        channel.color_ = new Color(colorPrefs_.getInt("Color_" + acqEng_.getChannelGroup() + "_" + channel.config_, Color.white.getRGB()));
                        fireEditingStopped();
                    }
                });

                // Return the configured component
                return combo_;
            } else {
                // ColorEditor takes care of this
                return colorLabel_;
            }
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            // TODO: if content of column does not match type we get an exception
            try {
                if (editCol_ == 0) {
                    // As a side effect, change to the color of the new channel
                    channel_.color_ = new Color(colorPrefs_.getInt("Color_" + acqEng_.getChannelGroup() + "_" + combo_.getSelectedItem(), Color.white.getRGB()));
                    return combo_.getSelectedItem();
                } else if (editCol_ == 1 || editCol_ == 2) {
                    return new Double(NumberUtils.displayStringToDouble(text_.getText()));
                } else if (editCol_ == 3) {
                    return new Boolean(checkBox_.isSelected());
                } else if (editCol_ == 4) {
                    return new Integer(NumberUtils.displayStringToInt(text_.getText()));
                } else if (editCol_ == 5) {
                    Color c = colorLabel_.getBackground();
                    return c;
                } else {
                    String err = new String("Internal error: unknown column");
                    return err;
                }
            } catch (ParseException p) {
                ReportingUtils.showError(p);
            }
            String err = new String("Internal error: unknown column");
            return err;
        }
    }

    /**
     * Renderer class for the channel table.
     */
    public class ChannelCellRenderer extends JLabel implements TableCellRenderer {

        private static final long serialVersionUID = -4328340719459382679L;
        private AcquisitionEngine acqEng_;

        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public ChannelCellRenderer(AcquisitionEngine acqEng) {
            super();
            acqEng_ = acqEng;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
            this.setEnabled(table.isEnabled());
            
            ChannelTableModel model = (ChannelTableModel) table.getModel();
            ArrayList<ChannelSpec> channels = model.getChannels();
            ChannelSpec channel = channels.get(rowIndex);

            if (hasFocus) {
                // this cell is the anchor and the table has the focus
            }

            colIndex = table.convertColumnIndexToModel(colIndex);

            setOpaque(false);
            if (colIndex == 0) {
                setText(channel.config_);
            } else if (colIndex == 1) {
                setText(NumberUtils.doubleToDisplayString(channel.exposure_));
            } else if (colIndex == 2) {
                setText(NumberUtils.doubleToDisplayString(channel.zOffset_));
            } else if (colIndex == 3) {
                JCheckBox check = new JCheckBox("", channel.doZStack_);
                check.setEnabled(acqEng_.isZSliceSettingEnabled() && table.isEnabled());
                if (isSelected) {
                    check.setBackground(table.getSelectionBackground());
                    check.setOpaque(true);
                } else {
                    check.setOpaque(false);
                    check.setBackground(table.getBackground());
                }
                return check;
            } else if (colIndex == 4) {
                setText(Integer.toString(channel.skipFactorFrame_));
            } else if (colIndex == 5) {
                setText("");
                setBackground(channel.color_);
                setOpaque(true);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setOpaque(true);
            } else {
                setOpaque(false);
                setBackground(table.getBackground());
            }

            // Since the renderer is a component, return itself
            return this;
        }

        // The following methods override the defaults for performance reasons
        public void validate() {
        }

        public void revalidate() {
        }

        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    public void createChannelTable() {
        channelTable_ = new JTable();
        channelTable_.setFont(new Font("Dialog", Font.PLAIN, 10));
        channelTable_.setAutoCreateColumnsFromModel(false);
        model_ = new ChannelTableModel(acqEng_);
        channelTable_.setModel(model_);
        model_.setChannels(acqEng_.getChannels());
        
        ChannelCellEditor cellEditor = new ChannelCellEditor();
        ChannelCellRenderer cellRenderer = new ChannelCellRenderer(acqEng_);
        channelTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int k = 0; k < model_.getColumnCount(); k++) {
            int colIndex = search(columnOrder_, k);
            if (colIndex < 0) {
                colIndex = k;
            }
            if (colIndex == model_.getColumnCount() - 1) {
                ColorRenderer cr = new ColorRenderer(true);
                ColorEditor ce = new ColorEditor(model_, model_.getColumnCount() - 1);
                TableColumn column = new TableColumn(model_.getColumnCount() - 1, 200, cr, ce);
                column.setPreferredWidth(columnWidth_[model_.getColumnCount() - 1]);
                channelTable_.addColumn(column);
            } else {
                TableColumn column = new TableColumn(colIndex, 200, cellRenderer, cellEditor);
                column.setPreferredWidth(columnWidth_[colIndex]);
                channelTable_.addColumn(column);
            }
        }
        channelTablePane_.setViewportView(channelTable_);
    }


    public JPanel createPanel(String text, int left, int top, int right, int bottom) {
        return createPanel(text, left, top, right, bottom, false);
    }

    public JPanel createPanel(String text, int left, int top, int right, int bottom, boolean checkBox) {
        ComponentTitledPanel thePanel;
        if (checkBox) {
           thePanel = new CheckBoxPanel(text);
        } else {
           thePanel = new LabelPanel(text);
        }

        thePanel.setTitleFont(new Font("Dialog",Font.BOLD,12));
        panelList_.add(thePanel);
        thePanel.setBounds(left, top, right - left, bottom - top);
        dayBorder_ = BorderFactory.createEtchedBorder();
        nightBorder_ = BorderFactory.createEtchedBorder(Color.gray, Color.darkGray);

        //updatePanelBorder(thePanel);
        thePanel.setLayout(null);
        getContentPane().add(thePanel);
        return thePanel;
    }



    public void updatePanelBorder(JPanel thePanel) {
        TitledBorder border = (TitledBorder) thePanel.getBorder();
        if (gui_.getBackgroundStyle().contentEquals("Day")) {
            border.setBorder(dayBorder_);
        } else {
            border.setBorder(nightBorder_);
        }
    }

    public void createEmptyPanels() {
        panelList_ = new Vector<JPanel>();

        framesPanel_ = (CheckBoxPanel) createPanel("Time points", 5, 5, 220, 91, true); // (text, left, top, right, bottom)
        positionsPanel_ = (CheckBoxPanel) createPanel("Multiple positions (XY)", 5, 93, 220, 154, true);
        slicesPanel_ = (CheckBoxPanel) createPanel("Z-stacks (slices)", 5, 156, 220, 306, true);

        acquisitionOrderPanel_ = createPanel("Acquisition order", 226, 5, 415, 91);
        summaryPanel_ = createPanel("Summary", 226, 180, 415, 306);
        afPanel_ = (CheckBoxPanel) createPanel("Autofocus", 226, 93, 415, 178, true);

        channelsPanel_ = (CheckBoxPanel) createPanel("Channels", 5, 308, 510, 451, true);
        savePanel_ = (CheckBoxPanel) createPanel("Save images", 5, 453, 510, 620, true);
    }

    /**
     * Acquisition control dialog box.
     * Specification of all parameters required for the acquisition.
     * @param acqEng - acquisition engine
     * @param prefs - application preferences node
     */
    public AcqControlDlg(AcquisitionEngine acqEng, Preferences prefs, DeviceControlGUI gui) {
        super();

        prefs_ = prefs;
        gui_ = gui;
        guiColors_ = new GUIColors();

        Preferences root = Preferences.userNodeForPackage(this.getClass());
        acqPrefs_ = root.node(root.absolutePath() + "/" + ACQ_SETTINGS_NODE);
        colorPrefs_ = root.node(root.absolutePath() + "/" + COLOR_SETTINGS_NODE);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        numberFormat_ = NumberFormat.getNumberInstance();

        addWindowListener(new WindowAdapter() {

            public void windowClosing(final WindowEvent e) {
                close();
            }
        });
        acqEng_ = acqEng;

        getContentPane().setLayout(null);
        //getContentPane().setFocusTraversalPolicyProvider(true);
        setResizable(false);
        setTitle("Multi-dimensional Acquisition");
        setBackground(guiColors_.background.get(gui_.getBackgroundStyle()));

        createEmptyPanels();

        // Frames panel

        framesPanel_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });

        final JLabel numberLabel = new JLabel();
        numberLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        numberLabel.setText("Number");
        framesPanel_.add(numberLabel);
        numberLabel.setBounds(15, 25, 54, 24);

        SpinnerModel sModel = new SpinnerNumberModel(
                new Integer(1),
                new Integer(1),
                null,
                new Integer(1));

        numFrames_ = new JSpinner(sModel);
        ((JSpinner.DefaultEditor) numFrames_.getEditor()).getTextField().setFont(new Font("Arial", Font.PLAIN, 10));

        //numFrames_.setValue((int) acqEng_.getNumFrames());
        framesPanel_.add(numFrames_);
        numFrames_.setBounds(60, 25, 70, 24);
        numFrames_.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                applySettings();
            }
        });

        final JLabel intervalLabel = new JLabel();
        intervalLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        intervalLabel.setText("Interval");
        framesPanel_.add(intervalLabel);
        intervalLabel.setBounds(15, 52, 43, 24);

        interval_ = new JFormattedTextField(numberFormat_);
        interval_.setFont(new Font("Arial", Font.PLAIN, 10));
        interval_.setValue(new Double(1.0));
        interval_.addPropertyChangeListener("value", this);
        framesPanel_.add(interval_);
        interval_.setBounds(60, 52, 55, 24);

        timeUnitCombo_ = new JComboBox();
        timeUnitCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                interval_.setText(NumberUtils.doubleToDisplayString(convertMsToTime(acqEng_.getFrameIntervalMs(), timeUnitCombo_.getSelectedIndex())));
            }
        });
        timeUnitCombo_.setModel(new DefaultComboBoxModel(new String[]{"ms", "s", "min"}));
        timeUnitCombo_.setFont(new Font("Arial", Font.PLAIN, 10));
        timeUnitCombo_.setBounds(120, 52, 67, 24);
        framesPanel_.add(timeUnitCombo_);


        // Positions (XY) panel


        listButton_ = new JButton();
        listButton_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gui_.showXYPositionList();
            }
        });
        listButton_.setToolTipText("Open XY list dialog");
        listButton_.setIcon(SwingResourceManager.getIcon(AcqControlDlg.class, "icons/application_view_list.png"));
        listButton_.setText("Edit position list...");
        listButton_.setMargin(new Insets(2, 5, 2, 5));
        listButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
        listButton_.setBounds(42, 25, 136, 26);
        positionsPanel_.add(listButton_);

        // Slices panel

        slicesPanel_.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // enable disable all related contrtols
                applySettings();
            }
        });

        final JLabel zbottomLabel = new JLabel();
        zbottomLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        zbottomLabel.setText("Z-start [um]");
        zbottomLabel.setBounds(30, 30, 69, 15);
        slicesPanel_.add(zbottomLabel);

        zBottom_ = new JFormattedTextField(numberFormat_);
        zBottom_.setFont(new Font("Arial", Font.PLAIN, 10));
        zBottom_.setBounds(95, 27, 54, 21);
        zBottom_.setValue(new Double(1.0));
        zBottom_.addPropertyChangeListener("value", this);
        slicesPanel_.add(zBottom_);

        setBottomButton_ = new JButton();
        setBottomButton_.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                setBottomPosition();
            }
        });
        setBottomButton_.setMargin(new Insets(-5, -5, -5, -5));
        setBottomButton_.setFont(new Font("", Font.PLAIN, 10));
        setBottomButton_.setText("Set");
        setBottomButton_.setBounds(150, 27, 50, 22);
        slicesPanel_.add(setBottomButton_);

        final JLabel ztopLabel = new JLabel();
        ztopLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        ztopLabel.setText("Z-end [um]");
        ztopLabel.setBounds(30, 53, 69, 15);
        slicesPanel_.add(ztopLabel);

        zTop_ = new JFormattedTextField(numberFormat_);
        zTop_.setFont(new Font("Arial", Font.PLAIN, 10));
        zTop_.setBounds(95, 50, 54, 21);
        zTop_.setValue(new Double(1.0));
        zTop_.addPropertyChangeListener("value", this);
        slicesPanel_.add(zTop_);

        setTopButton_ = new JButton();
        setTopButton_.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                setTopPosition();
            }
        });
        setTopButton_.setMargin(new Insets(-5, -5, -5, -5));
        setTopButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
        setTopButton_.setText("Set");
        setTopButton_.setBounds(150, 50, 50, 22);
        slicesPanel_.add(setTopButton_);

        final JLabel zstepLabel = new JLabel();
        zstepLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        zstepLabel.setText("Z-step [um]");
        zstepLabel.setBounds(30, 76, 69, 15);
        slicesPanel_.add(zstepLabel);

        zStep_ = new JFormattedTextField(numberFormat_);
        zStep_.setFont(new Font("Arial", Font.PLAIN, 10));
        zStep_.setBounds(95, 73, 54, 21);
        zStep_.setValue(new Double(1.0));
        zStep_.addPropertyChangeListener("value", this);
        slicesPanel_.add(zStep_);

        zValCombo_ = new JComboBox();
        zValCombo_.setFont(new Font("Arial", Font.PLAIN, 10));
        zValCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                zValCalcChanged();
            }
        });
        zValCombo_.setModel(new DefaultComboBoxModel(new String[]{"relative Z", "absolute Z"}));
        zValCombo_.setBounds(30, 97, 110, 22);
        slicesPanel_.add(zValCombo_);

        stackKeepShutterOpenCheckBox_ = new JCheckBox();
        stackKeepShutterOpenCheckBox_.setText("Keep shutter open");
        stackKeepShutterOpenCheckBox_.setFont(new Font("Arial", Font.PLAIN, 10));
        stackKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                applySettings();
            }
        });
        stackKeepShutterOpenCheckBox_.setSelected(false);
        stackKeepShutterOpenCheckBox_.setBounds(60,121,150,22);
        slicesPanel_.add(stackKeepShutterOpenCheckBox_);
        
        // Acquisition order panel

        posModeCombo_ = new JComboBox();
        posModeCombo_.setFont(new Font("", Font.PLAIN, 10));
        posModeCombo_.setBounds(15, 23, 151, 22);
        posModeCombo_.setEnabled(false);
        acquisitionOrderPanel_.add(posModeCombo_);
        posModeCombo_.addItem(new PositionMode(PositionMode.MULTI_FIELD));
        posModeCombo_.addItem(new PositionMode(PositionMode.TIME_LAPSE));

        sliceModeCombo_ = new JComboBox();
        sliceModeCombo_.setFont(new Font("", Font.PLAIN, 10));
        sliceModeCombo_.setBounds(15, 49, 151, 22);
        acquisitionOrderPanel_.add(sliceModeCombo_);
        sliceModeCombo_.addItem(new SliceMode(SliceMode.CHANNELS_FIRST));
        sliceModeCombo_.addItem(new SliceMode(SliceMode.SLICES_FIRST));

        // Summary panel

        summaryTextArea_ = new JTextArea();
        summaryTextArea_.setFont(new Font("Arial", Font.PLAIN, 10));
        summaryTextArea_.setEditable(false);
        summaryTextArea_.setBounds(4, 19, 273, 99);
        summaryTextArea_.setMargin(new Insets(2, 2, 2, 2));
        summaryTextArea_.setOpaque(false);
        summaryPanel_.add(summaryTextArea_);

        // Autofocus panel

        afPanel_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                applySettings();
            }
        });

        afButton_ = new JButton();
        afButton_.setToolTipText("Set autofocus options");
        afButton_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                afOptions();
            }
        });
        afButton_.setText("Options...");
        afButton_.setIcon(SwingResourceManager.getIcon(AcqControlDlg.class, "icons/wrench_orange.png"));
        afButton_.setMargin(new Insets(2, 5, 2, 5));
        afButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
        afButton_.setBounds(50, 21, 100, 28);
        afPanel_.add(afButton_);


        final JLabel afSkipFrame1 = new JLabel();
        afSkipFrame1.setFont(new Font("Dialog", Font.PLAIN, 10));
        afSkipFrame1.setText("Skip frame(s): ");
        afSkipFrame1.setBounds(35, 54, 70, 21);
        afPanel_.add(afSkipFrame1);


        afSkipInterval_ = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        ((JSpinner.DefaultEditor) afSkipInterval_.getEditor()).getTextField().setFont(new Font("Arial", Font.PLAIN, 10));
        afSkipInterval_.setBounds(105, 54, 55, 22);
        afSkipInterval_.setValue(new Integer(acqEng_.getAfSkipInterval()));
        afSkipInterval_.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                applySettings();
                afSkipInterval_.setValue(new Integer(acqEng_.getAfSkipInterval()));
            }
        });
        afPanel_.add(afSkipInterval_);


        // Channels panel
        channelsPanel_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });

        final JLabel channelsLabel = new JLabel();
        channelsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        channelsLabel.setBounds(90, 19, 80, 24);
        channelsLabel.setText("Channel group:");
        channelsPanel_.add(channelsLabel);


        channelGroupCombo_ = new JComboBox();
        channelGroupCombo_.setFont(new Font("", Font.PLAIN, 10));
        updateGroupsCombo();

        channelGroupCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                String newGroup = (String) channelGroupCombo_.getSelectedItem();

                if (acqEng_.setChannelGroup(newGroup)) {
                    model_.cleanUpConfigurationList();
                    if (gui_.getAutofocusManager() != null) {
                        try {
                            gui_.getAutofocusManager().refresh();
                        } catch (MMException e) {
                            ReportingUtils.showError(e);
                        }
                    }
                } else {
                    updateGroupsCombo();
                }
            }
        });
        channelGroupCombo_.setBounds(165, 20, 150, 22);
        channelsPanel_.add(channelGroupCombo_);

        channelTablePane_ = new JScrollPane();
        channelTablePane_.setFont(new Font("Arial", Font.PLAIN, 10));
        channelTablePane_.setBounds(10, 45, 414, 90);
        channelsPanel_.add(channelTablePane_);


        final JButton addButton = new JButton();
        addButton.setFont(new Font("Arial", Font.PLAIN, 10));
        addButton.setMargin(new Insets(0, 0, 0, 0));
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                model_.addNewChannel();
                model_.fireTableStructureChanged();
                // update summary
                applySettings();
            }
        });
        addButton.setText("New");
        addButton.setBounds(430, 45, 68, 22);
        channelsPanel_.add(addButton);

        final JButton removeButton = new JButton();
        removeButton.setFont(new Font("Arial", Font.PLAIN, 10));
        removeButton.setMargin(new Insets(-5, -5, -5, -5));
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ChannelTableModel model = (ChannelTableModel) channelTable_.getModel();
                model.removeChannel(channelTable_.getSelectedRow());
                model.fireTableStructureChanged();
                // update summary
                applySettings();
            }
        });
        removeButton.setText("Remove");
        removeButton.setBounds(430, 69, 68, 22);
        channelsPanel_.add(removeButton);

        final JButton upButton = new JButton();
        upButton.setFont(new Font("Arial", Font.PLAIN, 10));
        upButton.setMargin(new Insets(0, 0, 0, 0));
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int sel = channelTable_.getSelectedRow();
                if (sel > -1) {
                    int newSel = model_.rowUp(sel);
                    model_.fireTableStructureChanged();
                    channelTable_.setRowSelectionInterval(newSel, newSel);
                    applySettings();
                }
            }
        });
        upButton.setText("Up");
        upButton.setBounds(430, 93, 68, 22);
        channelsPanel_.add(upButton);

        final JButton downButton = new JButton();
        downButton.setFont(new Font("Arial", Font.PLAIN, 10));
        downButton.setMargin(new Insets(0, 0, 0, 0));
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int sel = channelTable_.getSelectedRow();
                if (sel > -1) {
                    int newSel = model_.rowDown(sel);
                    model_.fireTableStructureChanged();
                    channelTable_.setRowSelectionInterval(newSel, newSel);
                    applySettings();
                }
            }
        });
        downButton.setText("Down");
        downButton.setBounds(430, 117, 68, 22);
        channelsPanel_.add(downButton);

        chanKeepShutterOpenCheckBox_ = new JCheckBox();
        chanKeepShutterOpenCheckBox_.setText("Keep shutter open");
        chanKeepShutterOpenCheckBox_.setFont(new Font("Arial", Font.PLAIN, 10));
        chanKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                applySettings();
            }
        });
        chanKeepShutterOpenCheckBox_.setSelected(false);
        chanKeepShutterOpenCheckBox_.setBounds(330,20,150,22);
        channelsPanel_.add(chanKeepShutterOpenCheckBox_);


        // Save panel

        savePanel_.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (!savePanel_.isSelected()) {
                    displayModeCombo_.setSelectedIndex(0);
                }
                commentTextArea_.setEnabled(savePanel_.isSelected());
                applySettings();
            }
        });

        displayMode_ = new JLabel();
        displayMode_.setFont(new Font("Arial", Font.PLAIN, 10));
        displayMode_.setText("Display");
        displayMode_.setBounds(150, 15, 49, 21);
        savePanel_.add(displayMode_);

        displayModeCombo_ = new JComboBox();
        displayModeCombo_.setFont(new Font("", Font.PLAIN, 10));
        displayModeCombo_.setBounds(188, 14, 150, 24);
        displayModeCombo_.addItem(new DisplayMode(DisplayMode.ALL));
        displayModeCombo_.addItem(new DisplayMode(DisplayMode.LAST_FRAME));
        displayModeCombo_.addItem(new DisplayMode(DisplayMode.SINGLE_WINDOW));
        displayModeCombo_.setEnabled(false);
        savePanel_.add(displayModeCombo_);


        rootLabel_ = new JLabel();
        rootLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
        rootLabel_.setText("Directory root");
        rootLabel_.setBounds(10, 40, 72, 22);
        savePanel_.add(rootLabel_);

        rootField_ = new JTextField();
        rootField_.setFont(new Font("Arial", Font.PLAIN, 10));
        rootField_.setBounds(90, 40, 354, 22);
        savePanel_.add(rootField_);

        browseRootButton_ = new JButton();
        browseRootButton_.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                setRootDirectory();
            }
        });
        browseRootButton_.setMargin(new Insets(2, 5, 2, 5));
        browseRootButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
        browseRootButton_.setText("...");
        browseRootButton_.setBounds(445, 40, 47, 24);
        savePanel_.add(browseRootButton_);

        namePrefixLabel_ = new JLabel();
        namePrefixLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
        namePrefixLabel_.setText("Name prefix");
        namePrefixLabel_.setBounds(10, 65, 76, 22);
        savePanel_.add(namePrefixLabel_);

        nameField_ = new JTextField();
        nameField_.setFont(new Font("Arial", Font.PLAIN, 10));
        nameField_.setBounds(90, 65, 354, 22);
        savePanel_.add(nameField_);

        commentLabel_ = new JLabel();
        commentLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
        commentLabel_.setText("Comment");
        commentLabel_.setBounds(10, 90, 76, 22);
        savePanel_.add(commentLabel_);

        JScrollPane commentScrollPane = new JScrollPane();
        commentScrollPane.setBounds(90, 90, 354, 62);
        savePanel_.add(commentScrollPane);

        commentTextArea_ = new JTextArea();
        commentScrollPane.setViewportView(commentTextArea_);
        commentTextArea_.setFont(new Font("", Font.PLAIN, 10));
        commentTextArea_.setToolTipText("Comment for the current acquistion");
        commentTextArea_.setWrapStyleWord(true);
        commentTextArea_.setLineWrap(true);
        commentTextArea_.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        //commentTextArea_.setBounds(91, 485, 354, 62);
        //savePanel_.add(commentTextArea_);


        // Main buttons

        final JButton closeButton = new JButton();
        closeButton.setFont(new Font("Arial", Font.PLAIN, 10));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveSettings();
                saveAcqSettings();
                AcqControlDlg.this.dispose();
                gui_.makeActive();
            }
        });
        closeButton.setText("Close");
        closeButton.setBounds(430, 10, 77, 22);
        getContentPane().add(closeButton);

        final JButton acquireButton = new JButton();
        acquireButton.setMargin(new Insets(-9, -9, -9, -9));
        acquireButton.setFont(new Font("Arial", Font.BOLD, 12));
        acquireButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                AbstractCellEditor ae = (AbstractCellEditor) channelTable_.getCellEditor();
                if (ae != null) {
                    ae.stopCellEditing();
                }
                runAcquisition();
            }
        });
        acquireButton.setText("Acquire!");
        acquireButton.setBounds(430, 44, 77, 22);
        getContentPane().add(acquireButton);


        final JButton stopButton = new JButton();
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                acqEng_.stop(true);
            }
        });
        stopButton.setText("Stop");
        stopButton.setFont(new Font("Arial", Font.BOLD, 12));
        stopButton.setBounds(430, 68, 77, 22);
        getContentPane().add(stopButton);



        final JButton loadButton = new JButton();
        loadButton.setFont(new Font("Arial", Font.PLAIN, 10));
        loadButton.setMargin(new Insets(-5, -5, -5, -5));
        loadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadAcqSettingsFromFile();
            }
        });

        loadButton.setText("Load...");
        loadButton.setBounds(430, 102, 77, 22);
        getContentPane().add(loadButton);

        final JButton saveAsButton = new JButton();
        saveAsButton.setFont(new Font("Arial", Font.PLAIN, 10));
        saveAsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveAsAcqSettingsToFile();
            }
        });
        saveAsButton.setText("Save...");
        saveAsButton.setBounds(430, 126, 77, 22);
        saveAsButton.setMargin(new Insets(-5, -5, -5, -5));
        getContentPane().add(saveAsButton);


        // update GUI contentss
        // -------------------

        // load window settings
        int x = 100;
        int y = 100;
        this.setBounds(x, y, 521, 645);

        if (prefs_ != null) {
            x = prefs_.getInt(ACQ_CONTROL_X, x);
            y = prefs_.getInt(ACQ_CONTROL_Y, y);
            setLocation(x, y);

            // load override settings
            // enable/disable dependent controls
        }


        // add update event listeners
        positionsPanel_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                applySettings();
            }
        });
        displayModeCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });
        sliceModeCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });
        posModeCombo_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });




        // load acquistion settings
        loadAcqSettings();

        // create the table of channels
        createChannelTable();

        // update summary
        updateGUIContents();

    }

    /** Called when a field's "value" property changes. Causes the Summary to be updated*/
    public void propertyChange(PropertyChangeEvent e) {
        // update summary
        applySettings();
        summaryTextArea_.setText(acqEng_.getVerboseSummary());
    }

    protected void afOptions() {
        if (gui_.getAutofocusManager().getDevice() != null) {
            gui_.getAutofocusManager().showOptionsDialog();
        }
    }

    public boolean inArray(String member, String[] group) {
        for (int i = 0; i < group.length; i++) {
            if (member.equals(group[i])) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        saveSettings();
        saveAcqSettings();
        dispose();
        gui_.makeActive();
    }

    public void updateGroupsCombo() {
        String groups[] = acqEng_.getAvailableGroups();
        if (groups.length != 0) {
            channelGroupCombo_.setModel(new DefaultComboBoxModel(groups));
            if (!inArray(acqEng_.getChannelGroup(), groups)) {
                acqEng_.setChannelGroup(acqEng_.getFirstConfigGroup());
            }

            channelGroupCombo_.setSelectedItem(acqEng_.getChannelGroup());
        }
    }

    public void updateChannelAndGroupCombo() {
        updateGroupsCombo();
        model_.cleanUpConfigurationList();
    }

    public synchronized void loadAcqSettings() {
        disableGUItoSettings_ = true;

        // load acquisition engine preferences
        acqEng_.clear();
        int numFrames = acqPrefs_.getInt(ACQ_NUMFRAMES, 1);
        double interval = acqPrefs_.getDouble(ACQ_INTERVAL, 0.0);

        acqEng_.setFrames(numFrames, interval);
        acqEng_.enableFramesSetting(acqPrefs_.getBoolean(ACQ_ENABLE_MULTI_FRAME, false));

        framesPanel_.setSelected(acqEng_.isFramesSettingEnabled());
        numFrames_.setValue(acqEng_.getNumFrames());

        int unit = acqPrefs_.getInt(ACQ_TIME_UNIT, 0);
        timeUnitCombo_.setSelectedIndex(unit);

        double bottom = acqPrefs_.getDouble(ACQ_ZBOTTOM, 0.0);
        double top = acqPrefs_.getDouble(ACQ_ZTOP, 0.0);
        double step = acqPrefs_.getDouble(ACQ_ZSTEP, 1.0);
        if (Math.abs(step) < Math.abs(acqEng_.getMinZStepUm())) {
            step = acqEng_.getMinZStepUm();
        }
        zVals_ = acqPrefs_.getInt(ACQ_Z_VALUES, 0);
        acqEng_.setSlices(bottom, top, step, zVals_ == 0 ? false : true);
        acqEng_.enableZSliceSetting(acqPrefs_.getBoolean(ACQ_ENABLE_SLICE_SETTINGS, acqEng_.isZSliceSettingEnabled()));
        acqEng_.enableMultiPosition(acqPrefs_.getBoolean(ACQ_ENABLE_MULTI_POSITION, acqEng_.isMultiPositionEnabled()));
        positionsPanel_.setSelected(acqEng_.isMultiPositionEnabled());

        slicesPanel_.setSelected(acqEng_.isZSliceSettingEnabled());
        
        acqEng_.enableChannelsSetting(acqPrefs_.getBoolean(ACQ_ENABLE_MULTI_CHANNEL, false));
        channelsPanel_.setSelected(acqEng_.isChannelsSettingEnabled());

        savePanel_.setSelected(acqPrefs_.getBoolean(ACQ_SAVE_FILES, false));

        nameField_.setText(acqPrefs_.get(ACQ_DIR_NAME, "Untitled"));
        String os_name = System.getProperty("os.name", "");
        if (os_name.startsWith("Window")) {
            rootField_.setText(acqPrefs_.get(ACQ_ROOT_NAME, "C:/AcquisitionData"));
        } else {
            rootField_.setText(acqPrefs_.get(ACQ_ROOT_NAME, "AcquisitionData"));
        }

        acqEng_.setSliceMode(acqPrefs_.getInt(ACQ_SLICE_MODE, acqEng_.getSliceMode()));
        acqEng_.setDisplayMode(acqPrefs_.getInt(ACQ_DISPLAY_MODE, acqEng_.getDisplayMode()));
        acqEng_.setPositionMode(acqPrefs_.getInt(ACQ_POSITION_MODE, acqEng_.getPositionMode()));
        acqEng_.enableAutoFocus(acqPrefs_.getBoolean(ACQ_AF_ENABLE, acqEng_.isAutoFocusEnabled()));
        acqEng_.setAfSkipInterval(acqPrefs_.getInt(ACQ_AF_SKIP_INTERVAL, acqEng_.getAfSkipInterval()));
        acqEng_.setChannelGroup(acqPrefs_.get(ACQ_CHANNEL_GROUP, acqEng_.getFirstConfigGroup()));
        afPanel_.setSelected(acqEng_.isAutoFocusEnabled());
        acqEng_.keepShutterOpenForChannels(acqPrefs_.getBoolean(ACQ_CHANNELS_KEEP_SHUTTER_OPEN, false));
        acqEng_.keepShutterOpenForStack(acqPrefs_.getBoolean(ACQ_STACK_KEEP_SHUTTER_OPEN, false));


        int numChannels = acqPrefs_.getInt(ACQ_NUM_CHANNELS, 0);

        ChannelSpec defaultChannel = new ChannelSpec();

        acqEng_.getChannels().clear();
        for (int i = 0; i < numChannels; i++) {
            String name = acqPrefs_.get(CHANNEL_NAME_PREFIX + i, "Undefined");
            double exp = acqPrefs_.getDouble(CHANNEL_EXPOSURE_PREFIX + i, 0.0);
            Boolean doZStack = acqPrefs_.getBoolean(CHANNEL_DOZSTACK_PREFIX + i, true);
            double zOffset = acqPrefs_.getDouble(CHANNEL_ZOFFSET_PREFIX + i, 0.0);
            ContrastSettings s8 = new ContrastSettings();
            s8.min = acqPrefs_.getDouble(CHANNEL_CONTRAST8_MIN_PREFIX + i, defaultChannel.contrast8_.min);
            s8.max = acqPrefs_.getDouble(CHANNEL_CONTRAST8_MAX_PREFIX + i, defaultChannel.contrast8_.max);
            ContrastSettings s16 = new ContrastSettings();
            s16.min = acqPrefs_.getDouble(CHANNEL_CONTRAST16_MIN_PREFIX + i, defaultChannel.contrast16_.min);
            s16.max = acqPrefs_.getDouble(CHANNEL_CONTRAST16_MAX_PREFIX + i, defaultChannel.contrast16_.max);
            int r = acqPrefs_.getInt(CHANNEL_COLOR_R_PREFIX + i, defaultChannel.color_.getRed());
            int g = acqPrefs_.getInt(CHANNEL_COLOR_G_PREFIX + i, defaultChannel.color_.getGreen());
            int b = acqPrefs_.getInt(CHANNEL_COLOR_B_PREFIX + i, defaultChannel.color_.getBlue());
            int skip = acqPrefs_.getInt(CHANNEL_SKIP_PREFIX + i, defaultChannel.skipFactorFrame_);
            Color c = new Color(r, g, b);
            acqEng_.addChannel(name, exp, doZStack, zOffset, s8, s16, skip, c);
        }

        // Restore Column Width and Column order
        int columnCount = 6;
        columnWidth_ = new int[columnCount];
        columnOrder_ = new int[columnCount];
        for (int k = 0; k < columnCount; k++) {
            columnWidth_[k] = acqPrefs_.getInt(ACQ_COLUMN_WIDTH + k, ACQ_DEFAULT_COLUMN_WIDTH);
            columnOrder_[k] = acqPrefs_.getInt(ACQ_COLUMN_ORDER + k, k);
        }
        
        disableGUItoSettings_ = false;
    }

    public synchronized void saveAcqSettings() {
        try {
            acqPrefs_.clear();
        } catch (BackingStoreException e) {
            ReportingUtils.showError(e);
        }

        applySettings();

        acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_FRAME, acqEng_.isFramesSettingEnabled());
        acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_CHANNEL, acqEng_.isChannelsSettingEnabled());
        acqPrefs_.putInt(ACQ_NUMFRAMES, acqEng_.getNumFrames());
        acqPrefs_.putDouble(ACQ_INTERVAL, acqEng_.getFrameIntervalMs());
        acqPrefs_.putInt(ACQ_TIME_UNIT, timeUnitCombo_.getSelectedIndex());
        acqPrefs_.putDouble(ACQ_ZBOTTOM, acqEng_.getSliceZBottomUm());
        acqPrefs_.putDouble(ACQ_ZTOP, acqEng_.getZTopUm());
        acqPrefs_.putDouble(ACQ_ZSTEP, acqEng_.getSliceZStepUm());
        acqPrefs_.putBoolean(ACQ_ENABLE_SLICE_SETTINGS, acqEng_.isZSliceSettingEnabled());
        acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_POSITION, acqEng_.isMultiPositionEnabled());
        acqPrefs_.putInt(ACQ_Z_VALUES, zVals_);
        acqPrefs_.putBoolean(ACQ_SAVE_FILES, savePanel_.isSelected());
        acqPrefs_.put(ACQ_DIR_NAME, nameField_.getText());
        acqPrefs_.put(ACQ_ROOT_NAME, rootField_.getText());

        acqPrefs_.putInt(ACQ_SLICE_MODE, acqEng_.getSliceMode());
        acqPrefs_.putInt(ACQ_DISPLAY_MODE, acqEng_.getDisplayMode());
        acqPrefs_.putInt(ACQ_POSITION_MODE, acqEng_.getPositionMode());
        acqPrefs_.putBoolean(ACQ_AF_ENABLE, acqEng_.isAutoFocusEnabled());
        acqPrefs_.putInt(ACQ_AF_SKIP_INTERVAL, acqEng_.getAfSkipInterval());
        acqPrefs_.putBoolean(ACQ_CHANNELS_KEEP_SHUTTER_OPEN, acqEng_.isShutterOpenForChannels());
        acqPrefs_.putBoolean(ACQ_STACK_KEEP_SHUTTER_OPEN, acqEng_.isShutterOpenForStack());

        acqPrefs_.put(ACQ_CHANNEL_GROUP, acqEng_.getChannelGroup());
        ArrayList<ChannelSpec> channels = acqEng_.getChannels();
        acqPrefs_.putInt(ACQ_NUM_CHANNELS, channels.size());
        for (int i = 0; i < channels.size(); i++) {
            ChannelSpec channel = channels.get(i);
            acqPrefs_.put(CHANNEL_NAME_PREFIX + i, channel.config_);
            acqPrefs_.putDouble(CHANNEL_EXPOSURE_PREFIX + i, channel.exposure_);
            acqPrefs_.putBoolean(CHANNEL_DOZSTACK_PREFIX + i, channel.doZStack_);
            acqPrefs_.putDouble(CHANNEL_ZOFFSET_PREFIX + i, channel.zOffset_);
            acqPrefs_.putDouble(CHANNEL_CONTRAST8_MIN_PREFIX + i, channel.contrast8_.min);
            acqPrefs_.putDouble(CHANNEL_CONTRAST8_MAX_PREFIX + i, channel.contrast8_.max);
            acqPrefs_.putDouble(CHANNEL_CONTRAST16_MIN_PREFIX + i, channel.contrast16_.min);
            acqPrefs_.putDouble(CHANNEL_CONTRAST16_MAX_PREFIX + i, channel.contrast16_.max);
            acqPrefs_.putInt(CHANNEL_COLOR_R_PREFIX + i, channel.color_.getRed());
            acqPrefs_.putInt(CHANNEL_COLOR_G_PREFIX + i, channel.color_.getGreen());
            acqPrefs_.putInt(CHANNEL_COLOR_B_PREFIX + i, channel.color_.getBlue());
            acqPrefs_.putInt(CHANNEL_SKIP_PREFIX + i, channel.skipFactorFrame_);
        }

        // Save model column widths and order
        for (int k = 0; k < model_.getColumnCount(); k++) {
            acqPrefs_.putInt(ACQ_COLUMN_WIDTH + k, findTableColumn(channelTable_, k).getWidth());
            acqPrefs_.putInt(ACQ_COLUMN_ORDER + k, channelTable_.convertColumnIndexToView(k));
        }
        try {
            acqPrefs_.flush();
        } catch (BackingStoreException ex) {
            ReportingUtils.logError(ex);
        }
    }

    // Returns the TableColumn associated with the specified column
    // index in the model
    public TableColumn findTableColumn(JTable table, int columnModelIndex) {
        Enumeration<?> e = table.getColumnModel().getColumns();
        for (; e.hasMoreElements();) {
            TableColumn col = (TableColumn) e.nextElement();
            if (col.getModelIndex() == columnModelIndex) {
                return col;
            }
        }
        return null;
    }

    protected void enableZSliceControls(boolean state) {
        zBottom_.setEnabled(state);
        zTop_.setEnabled(state);
        zStep_.setEnabled(state);
        zValCombo_.setEnabled(state);
    }

    protected void setRootDirectory() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(acqEng_.getRootName()));
        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            rootField_.setText(fc.getSelectedFile().getAbsolutePath());
            acqEng_.setRootName(fc.getSelectedFile().getAbsolutePath());
        }
    }

    protected void setTopPosition() {
        double z = acqEng_.getCurrentZPos();
        zTop_.setText(NumberUtils.doubleToDisplayString(z));
        applySettings();
        // update summary
        summaryTextArea_.setText(acqEng_.getVerboseSummary());
    }

    protected void setBottomPosition() {
        double z = acqEng_.getCurrentZPos();
        zBottom_.setText(NumberUtils.doubleToDisplayString(z));
        applySettings();
        // update summary
        summaryTextArea_.setText(acqEng_.getVerboseSummary());
    }

    protected void loadAcqSettingsFromFile() {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new AcqFileFilter());
        acqDir_ = prefs_.get(ACQ_FILE_DIR, null);

        if (acqDir_ != null) {
            fc.setCurrentDirectory(new File(acqDir_));
        }
        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            acqFile_ = fc.getSelectedFile();
            try {
                FileInputStream in = new FileInputStream(acqFile_);
                acqPrefs_.clear();
                Preferences.importPreferences(in);
                loadAcqSettings();
                updateGUIContents();
                in.close();
                acqDir_ = acqFile_.getParent();
                prefs_.put(ACQ_FILE_DIR, acqDir_);
            } catch (Exception e) {
                ReportingUtils.showError(e);
                return;
            }
        }
    }

    public void loadAcqSettingsFromFile(String path) {
        acqFile_ = new File(path);
        try {
            FileInputStream in = new FileInputStream(acqFile_);
            acqPrefs_.clear();
            Preferences.importPreferences(in);
            loadAcqSettings();
            updateGUIContents();
            in.close();
            acqDir_ = acqFile_.getParent();
            if (acqDir_ != null) {
                prefs_.put(ACQ_FILE_DIR, acqDir_);
            }
        } catch (Exception e) {
            ReportingUtils.showError(e);
            return;
        }
    }

    protected boolean saveAsAcqSettingsToFile() {
        saveAcqSettings();
        JFileChooser fc = new JFileChooser();
        boolean saveFile = true;
        if (acqPrefs_ == null) {
            return false; //nothing to save
        }
        do {
            if (acqFile_ == null) {
                acqFile_ = new File(NEW_ACQFILE_NAME);
            }

            fc.setSelectedFile(acqFile_);
            int retVal = fc.showSaveDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                acqFile_ = fc.getSelectedFile();

                // check if file already exists
                if (acqFile_.exists()) {
                    int sel = JOptionPane.showConfirmDialog(this,
                            "Overwrite " + acqFile_.getName(),
                            "File Save",
                            JOptionPane.YES_NO_OPTION);

                    if (sel == JOptionPane.YES_OPTION) {
                        saveFile = true;
                    } else {
                        saveFile = false;
                    }
                }
            } else {
                return false;
            }
        } while (saveFile == false);

        FileOutputStream os;
        try {
            os = new FileOutputStream(acqFile_);
            acqPrefs_.exportNode(os);
        } catch (FileNotFoundException e) {
            ReportingUtils.showError(e);
            return false;
        } catch (IOException e) {
            ReportingUtils.showError(e);
            return false;
        } catch (BackingStoreException e) {
            ReportingUtils.showError(e);
            return false;
        }
        return true;
    }

    public void runAcquisition() {
        if (acqEng_.isAcquisitionRunning()) {
            JOptionPane.showMessageDialog(this, "Cannot start acquisition: previous acquisition still in progress.");
            return;
        }


        try {
            applySettings();
            //saveAcqSettings(); // This is too slow.
            ChannelTableModel model = (ChannelTableModel) channelTable_.getModel();
            if (acqEng_.isChannelsSettingEnabled() && model.duplicateChannels()) {
                JOptionPane.showMessageDialog(this, "Cannot start acquisition using the same channel twice");
                return;
            }
            acqEng_.acquire();
        } catch (MMAcqDataException e) {
            ReportingUtils.showError(e);
            return;
        } catch (MMException e) {
            ReportingUtils.showError(e);
            return;
        }
    }

    public void runAcquisition(String acqName, String acqRoot) {
        if (acqEng_.isAcquisitionRunning()) {
            JOptionPane.showMessageDialog(this, "Unable to start the new acquisition task: previous acquisition still in progress.");
            return;
        }

        try {
            applySettings();
            acqEng_.setDirName(acqName);
            acqEng_.setRootName(acqRoot);
            acqEng_.setSaveFiles(true);
            acqEng_.acquire();
        } catch (MMException e) {
            ReportingUtils.showError(e);
            return;
        } catch (MMAcqDataException e) {
            ReportingUtils.showError(e);
            return;
        }
    }

    public boolean runWellScan(WellAcquisitionData wad) {

        boolean result = true;
        if (acqEng_.isAcquisitionRunning()) {
            JOptionPane.showMessageDialog(this, "Unable to start the new acquisition task: previous acquisition still in progress.");
            result = false;
            return result;
        }

        try {
            applySettings();
            acqEng_.setSaveFiles(true);
            result = acqEng_.acquireWellScan(wad);
            if (result == false) {

                acqEng_.stop(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //acqEng_.setFinished();
                return result;

            }
            // wait until acquisition is done
            while (acqEng_.isMultiFieldRunning()) {
                try {
                    ReportingUtils.logMessage("DBG: Waiting in AcqWindow");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    ReportingUtils.logError(e);
                    return result;
                }
            }

        } catch (MMException e) {
            ReportingUtils.showError(e);
            return false;
        } catch (MMAcqDataException e) {
            ReportingUtils.showError(e);
            return false;
        }

        return true;
    }

    public boolean isAcquisitionRunning() {
        return acqEng_.isAcquisitionRunning();
    }

    public static int search(int[] numbers, int key) {
        for (int index = 0; index < numbers.length; index++) {
            if (numbers[index] == key) {
                return index;
            }
        }
        return -1;
    }

    public void updateGUIContents() {
        if (disableGUItoSettings_) {
            return;
        }
        disableGUItoSettings_ = true;
        // Disable update prevents action listener loops


        // TODO: remove setChannels()
        model_.setChannels(acqEng_.getChannels());

        double intervalMs = acqEng_.getFrameIntervalMs();
        interval_.setText(numberFormat_.format(convertMsToTime(intervalMs, timeUnitCombo_.getSelectedIndex())));

        zBottom_.setText(NumberUtils.doubleToDisplayString(acqEng_.getSliceZBottomUm()));
        zTop_.setText(NumberUtils.doubleToDisplayString(acqEng_.getZTopUm()));
        zStep_.setText(NumberUtils.doubleToDisplayString(acqEng_.getSliceZStepUm()));

        framesPanel_.setSelected(acqEng_.isFramesSettingEnabled());
        slicesPanel_.setSelected(acqEng_.isZSliceSettingEnabled());
        positionsPanel_.setSelected(acqEng_.isMultiPositionEnabled());
        afPanel_.setSelected(acqEng_.isAutoFocusEnabled());
        posModeCombo_.setEnabled(positionsPanel_.isSelected() && framesPanel_.isSelected());
        sliceModeCombo_.setEnabled(slicesPanel_.isSelected() && channelsPanel_.isSelected());
        
        afSkipInterval_.setEnabled(acqEng_.isAutoFocusEnabled());

        // These values need to be cached or we will loose them due to the Spinners OnChanged methods calling applySetting
        Integer numFrames = new Integer(acqEng_.getNumFrames());
        Integer afSkipInterval = new Integer(acqEng_.getAfSkipInterval());
        if (acqEng_.isFramesSettingEnabled())
            numFrames_.setValue(numFrames);
        
        afSkipInterval_.setValue(afSkipInterval);

        enableZSliceControls(acqEng_.isZSliceSettingEnabled());
        model_.fireTableStructureChanged();

        channelGroupCombo_.setSelectedItem(acqEng_.getChannelGroup());
        sliceModeCombo_.setSelectedIndex(acqEng_.getSliceMode());
        displayModeCombo_.setSelectedIndex(acqEng_.getDisplayMode());
        if (framesPanel_.isSelected() && positionsPanel_.isSelected())
            posModeCombo_.setSelectedIndex(acqEng_.getPositionMode());
        else
            posModeCombo_.setSelectedIndex(PositionMode.TIME_LAPSE);
        zValCombo_.setSelectedIndex(zVals_);
        stackKeepShutterOpenCheckBox_.setSelected(acqEng_.isShutterOpenForStack());
        chanKeepShutterOpenCheckBox_.setSelected(acqEng_.isShutterOpenForChannels());

        channelTable_.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        boolean selected = channelsPanel_.isSelected();
        channelTable_.setEnabled(selected);
        channelTable_.getTableHeader().setForeground(selected ? Color.black : Color.gray);

        // update summary
        summaryTextArea_.setText(acqEng_.getVerboseSummary());

        disableGUItoSettings_ = false;
    }

    private void applySettings() {
        if (disableGUItoSettings_) {
            return;
        }
        disableGUItoSettings_ = true;

        AbstractCellEditor ae = (AbstractCellEditor) channelTable_.getCellEditor();
        if (ae != null) {
            ae.stopCellEditing();
        }

        try {
            double zStep = NumberUtils.displayStringToDouble(zStep_.getText());
            if (Math.abs(zStep) < acqEng_.getMinZStepUm()) {
                zStep = acqEng_.getMinZStepUm();
            }
            acqEng_.setSlices(NumberUtils.displayStringToDouble(zBottom_.getText()), NumberUtils.displayStringToDouble(zTop_.getText()), zStep, zVals_ == 0 ? false : true);
            acqEng_.enableZSliceSetting(slicesPanel_.isSelected());
            acqEng_.enableMultiPosition(positionsPanel_.isSelected());
            acqEng_.setSliceMode(((SliceMode) sliceModeCombo_.getSelectedItem()).getID());
            acqEng_.setDisplayMode(((DisplayMode) displayModeCombo_.getSelectedItem()).getID());
            acqEng_.setPositionMode(posModeCombo_.getSelectedIndex());
            acqEng_.enableChannelsSetting(channelsPanel_.isSelected());
            acqEng_.setChannels(((ChannelTableModel) channelTable_.getModel()).getChannels());
            acqEng_.enableFramesSetting(framesPanel_.isSelected());
            acqEng_.setFrames((Integer) numFrames_.getValue(),
            convertTimeToMs(NumberUtils.displayStringToDouble(interval_.getText()), timeUnitCombo_.getSelectedIndex()));
            acqEng_.setAfSkipInterval(NumberUtils.displayStringToInt(afSkipInterval_.getValue().toString()));
            acqEng_.keepShutterOpenForChannels(chanKeepShutterOpenCheckBox_.isSelected());
            acqEng_.keepShutterOpenForStack(stackKeepShutterOpenCheckBox_.isSelected());

        } catch (ParseException p) {
            ReportingUtils.showError(p);
            // TODO: throw error
        }

        acqEng_.setSaveFiles(savePanel_.isSelected());
        acqEng_.setDirName(nameField_.getText());
        acqEng_.setRootName(rootField_.getText());

        // update summary

        acqEng_.setComment(commentTextArea_.getText());

        acqEng_.enableAutoFocus(afPanel_.isSelected());


        acqEng_.setParameterPreferences(acqPrefs_);
        disableGUItoSettings_ = false;
        updateGUIContents();
    }

    /**
     * Save settings to application properties.
     *
     */
    private void saveSettings() {
        Rectangle r = getBounds();

        if (prefs_ != null) {
            // save window position
            prefs_.putInt(ACQ_CONTROL_X, r.x);
            prefs_.putInt(ACQ_CONTROL_Y, r.y);
        }
    }

    private double convertTimeToMs(double interval, int units) {
        if (units == 1) {
            return interval * 1000; // sec
        } else if (units == 2) {
            return interval * 60.0 * 1000.0; // min
        } else if (units == 0) {
            return interval; // ms
        }
        ReportingUtils.showError("Unknown units supplied for acquisition interval!");
        return interval;
    }

    private double convertMsToTime(double intervalMs, int units) {
        if (units == 1) {
            return intervalMs / 1000; // sec
        } else if (units == 2) {
            return intervalMs / (60.0 * 1000.0); // min
        } else if (units == 0) {
            return intervalMs; // ms
        }
        ReportingUtils.showError("Unknown units supplied for acquisition interval!");
        return intervalMs;
    }

    private void zValCalcChanged() {

        if (zValCombo_.getSelectedIndex() == 0) {
            setTopButton_.setEnabled(false);
            setBottomButton_.setEnabled(false);
        } else {
            setTopButton_.setEnabled(true);
            setBottomButton_.setEnabled(true);
        }

        if (zVals_ == zValCombo_.getSelectedIndex()) {
            return;
        }

        zVals_ = zValCombo_.getSelectedIndex();
        double zBottomUm, zTopUm;
        try {
            zBottomUm = NumberUtils.displayStringToDouble(zBottom_.getText());
            zTopUm = NumberUtils.displayStringToDouble(zTop_.getText());
        } catch (ParseException e) {
            ReportingUtils.logError(e);
            return;
        }

        double curZ = acqEng_.getCurrentZPos();

        double newTop, newBottom;
        if (zVals_ == 0) {
            setTopButton_.setEnabled(false);
            setBottomButton_.setEnabled(false);
            // convert from absolute to relative
            newTop = zTopUm - curZ;
            newBottom = zBottomUm - curZ;
        } else {
            setTopButton_.setEnabled(true);
            setBottomButton_.setEnabled(true);
            // convert from relative to absolute
            newTop = zTopUm + curZ;
            newBottom = zBottomUm + curZ;
        }
        zBottom_.setText(NumberUtils.doubleToDisplayString(newBottom));
        zTop_.setText(NumberUtils.doubleToDisplayString(newTop));
    }

    /**
     * This method is called from the Options dialog, to set the background style
     */
    public void setBackgroundStyle(String style) {
        setBackground(guiColors_.background.get(style));
        for (JPanel panel : panelList_) {
            //updatePanelBorder(panel);
        }
        repaint();
    }



    public class ComponentTitledPanel extends JPanel {
        public ComponentTitledBorder compTitledBorder;
        public boolean borderSet_ = false;
        public Component titleComponent;

        public void setBorder(Border border) {
            if (compTitledBorder != null && borderSet_)
                compTitledBorder.setBorder(border);
            else
                super.setBorder(border);
        }

        public Border getBorder() {
            return compTitledBorder;
        }

        public void setTitleFont(Font font) {
            titleComponent.setFont(font);
        }
    }


    public class LabelPanel extends ComponentTitledPanel {
        LabelPanel(String title) {
            super();
            titleComponent = new JLabel(title);
            JLabel label = (JLabel) titleComponent;
            //Dimension d = label.getPreferredSize();
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //d.height = 23;
            //d.width+=10;
            //label.setPreferredSize(d);
            compTitledBorder = new ComponentTitledBorder(label, this, BorderFactory.createEtchedBorder());
            this.setBorder(compTitledBorder);
            borderSet_ = true;
        }


    }

    public class CheckBoxPanel extends ComponentTitledPanel {

        JCheckBox checkBox;

        CheckBoxPanel(String title) {
            super();
            titleComponent = new JCheckBox(title);
            checkBox = (JCheckBox) titleComponent;
            
            compTitledBorder = new ComponentTitledBorder(checkBox, this, BorderFactory.createEtchedBorder());
            this.setBorder(compTitledBorder);
            borderSet_ = true;

            final CheckBoxPanel thisPanel = this;

            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean enable = checkBox.isSelected();
                    thisPanel.setChildrenEnabled(enable);
                }
            });

        }

        public void setChildrenEnabled(boolean enabled) {
            Component comp[] = this.getComponents();
            for (int i = 0; i < comp.length; i++) {
                 comp[i].setEnabled(enabled);
            }
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public void setSelected(boolean selected) {
            checkBox.setSelected(selected);
            setChildrenEnabled(selected);
        }

        public void addActionListener(ActionListener actionListener) {
            checkBox.addActionListener(actionListener);
        }


    }
} 
