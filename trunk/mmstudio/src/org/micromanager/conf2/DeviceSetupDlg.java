package org.micromanager.conf2;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import mmcorej.CMMCore;
import mmcorej.DeviceDetectionStatus;
import mmcorej.DeviceType;
import mmcorej.MMCoreJ;
import mmcorej.StrVector;

import org.micromanager.utils.MMDialog;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.PropertyNameCellRenderer;
import org.micromanager.utils.PropertyValueCellEditor;
import org.micromanager.utils.PropertyValueCellRenderer;
import org.micromanager.utils.ReportingUtils;

public class DeviceSetupDlg extends MMDialog {
   private static final long serialVersionUID = 1L;
   private final JPanel contentPanel = new JPanel();
   private CMMCore core;
   private Device portDev;
   private MicroscopeModel model;
   private Device dev;
   private JTable propTable;
   private JButton detectButton;
   private boolean requestCancel;
   private DetectorJDialog progressDialog;
   private DetectionTask dt;
   private final String DETECT_PORTS = "Scan";
   private JTable comTable;
   private JTextField devLabel;

   /**
    * Create the dialog.
    */
   public DeviceSetupDlg(MicroscopeModel mod, CMMCore c, Device d) {
      setModal(true);
      setBounds(100, 100, 478, 528);
      loadPosition(100, 100);
      model = mod;
      core = c;
      portDev = null;
      dev = d;
      
      getContentPane().setLayout(new BorderLayout());
      contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      getContentPane().add(contentPanel, BorderLayout.CENTER);
      contentPanel.setLayout(null);
      {
         JLabel lblNewLabel = new JLabel("Label");
         lblNewLabel.setBounds(10, 11, 35, 14);
         contentPanel.add(lblNewLabel);
      }
      
      devLabel = new JTextField(dev.getName());
      devLabel.setBounds(47, 8, 165, 20);
      contentPanel.add(devLabel);
      devLabel.setColumns(10);
                 
      {
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
         getContentPane().add(buttonPane, BorderLayout.SOUTH);
         {
            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  onOK();
               }
            });
            okButton.setActionCommand("OK");
            buttonPane.add(okButton);
            getRootPane().setDefaultButton(okButton);
         }
         {
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  onCancel();
               }
            });
            cancelButton.setActionCommand("Cancel");
            buttonPane.add(cancelButton);
         }
      }
      
      addWindowListener(new WindowAdapter() {
         public void windowClosing(final WindowEvent e) {
            savePosition();
         }
      });
      
      setTitle("Device: " + dev.getAdapterName() + " | Library: " + dev.getLibrary());
      
      JScrollPane scrollPaneProp = new JScrollPane();
      scrollPaneProp.setBounds(10, 64, 442, 164);
      contentPanel.add(scrollPaneProp);
      
      propTable = new JTable();
      propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      propTable.setAutoCreateColumnsFromModel(false);
      scrollPaneProp.setViewportView(propTable);
      
      detectButton = new JButton(DETECT_PORTS);
      detectButton.setEnabled(false);
      detectButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            if (detectButton.getText().equalsIgnoreCase(DETECT_PORTS)) {
               requestCancel = false;
               progressDialog = new DetectorJDialog(DeviceSetupDlg.this, false);
               progressDialog.setTitle("\u00B5" + "Manager device detection");
               progressDialog.setLocationRelativeTo(DeviceSetupDlg.this);
               progressDialog.setSize(483, 288);
               progressDialog.setVisible(true);
               dt = new DetectionTask("serial_detect");
               dt.start();
               detectButton.setText("Cancel");
           } else {
               requestCancel = true;
               dt.finish();
               detectButton.setText(DETECT_PORTS);
           }

         }
      });
      detectButton.setToolTipText("Scan COM ports to detect this device");
      detectButton.setBounds(359, 247, 93, 23);
      contentPanel.add(detectButton);
      
      JLabel portLbl = new JLabel("Port Properties (RS 232 settings)");
      portLbl.setBounds(10, 251, 442, 14);
      contentPanel.add(portLbl);
      
      JScrollPane scrollPaneCOM = new JScrollPane();
      scrollPaneCOM.setBounds(10, 281, 442, 169);
      contentPanel.add(scrollPaneCOM);
      
      comTable = new JTable();
      scrollPaneCOM.setViewportView(comTable);
      comTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      comTable.setAutoCreateColumnsFromModel(false);
      
      JLabel lblNewLabel_2 = new JLabel("Initialization Properties");
      lblNewLabel_2.setBounds(10, 49, 442, 14);
      contentPanel.add(lblNewLabel_2);
      
      JLabel parentHub = new JLabel(dev.getParentHub().isEmpty() ? "" : "Parent: " + dev.getParentHub());
      parentHub.setBounds(227, 11, 225, 14);
      contentPanel.add(parentHub);
      
      loadSettings();
   }

   protected void onCancel() {
      savePosition();
      dispose();
   }

   protected void onOK() {
      savePosition();
      String oldName = dev.getName();
      String newName = devLabel.getText();
      
      if (dev.getName().compareTo(devLabel.getText()) != 0) {
         if (model.findDevice(devLabel.getText()) != null) {
            showMessage("Device name " + devLabel.getText() + " is already in use.\nPress Cancel and try again.");
            return;            
         }
          
         try {
            core.unloadDevice(dev.getName());
            dev.setInitialized(false);
            core.loadDevice(devLabel.getText(), dev.getLibrary(), dev.getAdapterName()); 
            core.setParentLabel(devLabel.getText(), dev.getParentHub());
         } catch (Exception e) {
            showMessage("Device failed to re-load with changed name.");
            return;
         }
         
         dev.setName(devLabel.getText());
      }
      
      Device d = model.findDevice(devLabel.getText());
      if (d==null) {
         showMessage("Device " + devLabel.getText() + " is not loaded properly.\nPress Cancel and try again.");
         return;
      }
      
      if (d.isInitialized()) {
         try {
            core.unloadDevice(d.getName());
            core.loadDevice(d.getName(), d.getLibrary(), d.getAdapterName());
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      if (initializeDevice()) {
         dispose();
         if (portDev != null)
            model.useSerialPort(portDev, true);
         model.setModified(true);
      } else {
         // initialization failed
         dev.setInitialized(false);
         return;
      }
      
      // make sure parent refs are updated
      if (!oldName.contentEquals(newName)) {
         Device devs[] = model.getDevices();
         for (int i=0; i<devs.length; i++) {
            if (devs[i].getParentHub().contentEquals(oldName)) {
               devs[i].setParentHub(newName);
            }
         }
      }
   }  

   private void loadSettings() {

      rebuildPropTable();

      // setup com ports
      ArrayList<Device> ports = new ArrayList<Device>();
      Device avPorts[] = model.getAvailableSerialPorts();
      for(int i=0; i<avPorts.length; i++) {
//         if (!model.isPortInUse(avPorts[i]))
//            ports.add(avPorts[i]);
//         else if (dev.getPort().compareTo(avPorts[i].getName()) == 0)
//            ports.add(avPorts[i]);
         // NOTE: commented out code was intended to exclude ports
         // that were already used by other devices.
         // But, at this point we have to list all ports (used or not)
         // to provide compatibility with older device adapters that share the same port
         ports.add(avPorts[i]);
      }
      
      // identify "port" properties and assign available com ports declared for use
      boolean anyPorts = false;
      boolean anyProps = false;
      for (int i=0; i<dev.getNumberOfProperties(); i++) {
         PropertyItem p = dev.getProperty(i);
         if (p.preInit)
            anyProps = true;
         
         if (p.name.compareTo(MMCoreJ.getG_Keyword_Port()) == 0) {
            anyPorts = true;
            if (ports.size() == 0) {
               // no ports available, tell user and return
               JOptionPane.showMessageDialog(null, "There are no unused ports available!");
               return;
            }
            String allowed[] = new String[ports.size()];
            for (int k=0; k<ports.size(); k++)
               allowed[k] = ports.get(k).getName();
            p.allowed = allowed;
            
            rebuildComTable(p.value);
         }
      }
      
      // resize dialog based on the properties
      if (anyProps && !anyPorts) {
         Rectangle r = getBounds();
         r.height = 300;
         setBounds(r);
      } else if (!anyProps && !anyPorts) {
         Rectangle r = getBounds();
         r.height = 112;
         setBounds(r);
      }
      
   }
   
   private void rebuildPropTable() {
      
      PropertyTableModel tm = new PropertyTableModel(model, dev, this);
      propTable.setModel(tm);
      PropertyValueCellEditor propValueEditor = new PropertyValueCellEditor();
      PropertyValueCellRenderer propValueRenderer = new PropertyValueCellRenderer();
      PropertyNameCellRenderer propNameRenderer = new PropertyNameCellRenderer();
      if (propTable.getColumnCount() == 0) {
          TableColumn column;
          column = new TableColumn(0, 200, propNameRenderer, null);
          propTable.addColumn(column);
          column = new TableColumn(1, 200, propNameRenderer, null);
          propTable.addColumn(column);
          column = new TableColumn(2, 200, propValueRenderer, propValueEditor);
          propTable.addColumn(column);
      }
      tm.fireTableStructureChanged();
      tm.fireTableDataChanged();
      boolean any = false;
      Device devices[] = model.getDevices();
      //  build list of devices to look for on the serial ports
      for (int i = 0; i < devices.length; i++) {
          for (int j = 0; j < devices[i].getNumberOfProperties(); j++) {
              PropertyItem p = devices[i].getProperty(j);
              if (p.name.compareTo(MMCoreJ.getG_Keyword_Port()) == 0) {
                  any = true;
                  break;
              }
          }
          if (any) {
              break;
          }
      }
      detectButton.setEnabled(any);
      propTable.repaint();
   }

   public void rebuildComTable(String portName) {
      if (portName == null)
         return;
      
      portDev = model.findSerialPort(portName);
      if (portDev == null)
         return;
      
      // load port if necessary
      StrVector loadedPorts = core.getLoadedDevicesOfType(DeviceType.SerialDevice);
      Iterator<String> lp = loadedPorts.iterator();
      boolean loaded = false;
      while (lp.hasNext()) {
         lp.next().compareTo(portName);
         loaded = true;
      }
      if (!loaded) {
         try {
            core.loadDevice(portDev.getName(), portDev.getLibrary(), portDev.getAdapterName());
            portDev.loadDataFromHardware(core);
         } catch (Exception e) {
            ReportingUtils.logError(e);
         }
      }
      
      try {
         System.out.println("rebuild " + portDev.getPropertyValue("BaudRate"));
      } catch (MMConfigFileException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      
      ComPropTableModel tm = new ComPropTableModel(model, portDev);
      comTable.setModel(tm);
      PropertyValueCellEditor propValueEditor = new PropertyValueCellEditor();
      PropertyValueCellRenderer propValueRenderer = new PropertyValueCellRenderer();
      PropertyNameCellRenderer propNameRenderer = new PropertyNameCellRenderer();
      if (comTable.getColumnCount() == 0) {
          TableColumn column;
          column = new TableColumn(0, 200, propNameRenderer, null);
          comTable.addColumn(column);
          column = new TableColumn(1, 200, propNameRenderer, null);
          comTable.addColumn(column);
          column = new TableColumn(2, 200, propValueRenderer, propValueEditor);
          comTable.addColumn(column);
      }
      tm.fireTableStructureChanged();
      tm.fireTableDataChanged();
      comTable.repaint();
   }

   private boolean initializeDevice() {
      try {
         if (dev.isInitialized()) {
            // device was initialized before so now we have to re-set it
            core.unloadDevice(dev.getName());
            core.loadDevice(dev.getName(), dev.getLibrary(), dev.getAdapterName());
         }

         // transfer properties to device
         PropertyTableModel ptm = (PropertyTableModel)propTable.getModel();
         for (int i=0; i<ptm.getRowCount(); i++) {
            Setting s = ptm.getSetting(i);
            core.setProperty(dev.getName(), s.propertyName_, s.propertyValue_);
         }
         dev.loadDataFromHardware(core);

         // first initialize port...
         if (initializePort()) {
            // ...then device
            dev.setName(devLabel.getText());
            core.initializeDevice(dev.getName());
            dev.loadDataFromHardware(core);
            dev.setInitialized(true);
            dev.updateSetupProperties();
            dev.discoverPeripherals(core);
            return true;
         }            
         return false; // port failed

      } catch (Exception e) {
         showMessage(e.getMessage());
         // reset device, just in case it does not know how to handle repeated initializations
         try {
            core.unloadDevice(dev.getName());
            core.loadDevice(dev.getName(), dev.getLibrary(), dev.getAdapterName());
         } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         return false;
      }
   }
   
   private boolean initializePort() {
      if (portDev != null) {
         try {
            core.unloadDevice(portDev.getName());
            Thread.sleep(1000);
            core.loadDevice(portDev.getName(), portDev.getLibrary(), portDev.getAdapterName());
            for (int j = 0; j < portDev.getNumberOfProperties(); j++) {
               PropertyItem prop = portDev.getProperty(j);
               if (prop.preInit) {
                  core.setProperty(portDev.getName(), prop.name, prop.value);
                  if (portDev.findSetupProperty(prop.name) == null)
                     portDev.addSetupProperty(new PropertyItem(prop.name, prop.value, true));
                  else
                     portDev.setSetupPropertyValue(prop.name, prop.value);
               }
            }
            core.initializeDevice(portDev.getName());
            Thread.sleep(1000);
            portDev.loadDataFromHardware(core);
            model.useSerialPort(portDev, true);
            
         } catch (Exception e) {
            showMessage(e.getMessage());
            return false;
         }
      }
      return true;
   }
   
   public void showMessage(String msg) {
      JOptionPane.showMessageDialog(this, msg);
   }
   
   private class DetectionTask extends Thread {
      
      private String foundPorts[];
      private String selectedPort;

      DetectionTask(String id) {
         super(id);
         foundPorts = new String[0];
         selectedPort = new String();
      }

      public void run() {
         boolean currentDebugLogSetting = core.debugLogEnabled();
         try {
            ArrayList<Device> ports = new ArrayList<Device>();
            model.removeDuplicateComPorts();
            Device availablePorts[] = model.getAvailableSerialPorts();
            String portsInModel = "Serial ports available in configuration: ";
            for (int ip = 0; ip < availablePorts.length; ++ip) {
//               if (!model.isPortInUse(availablePorts[ip])) {
//                  ports.add(availablePorts[ip]);
//               }
               // NOTE: commented out code was intended to avoid checking ports
               // that were already used by other devices.
               // But, at this point we have to check all ports (used or not)
               // to provide compatibility with older device adapters that share the same port
               
               ports.add(availablePorts[ip]);
            }
            for (Device p1 : ports) {
               if (0 < portsInModel.length()) {
                  portsInModel += " ";
               }
               portsInModel += p1.getName();
            }
            class Detector extends Thread {
               Detector(String deviceName, String portName) {
                  super(deviceName);
                  portName_ = portName;
                  st0_ = DeviceDetectionStatus.Misconfigured;
               }
               private DeviceDetectionStatus st0_;
               private String portName_;
               public void run() {
                  st0_ = core.detectDevice(getName());
               }
               public DeviceDetectionStatus getStatus() {
                  return st0_;
               }
               public String PortName() {
                  return portName_;
               }
               public void finish() {
                  try {
                     join();
                  } catch (InterruptedException ex) {
                     //ReportingUtils.showError(ex);
                  }
               }
            }
            ArrayList<Device> devicesToSearch = new ArrayList<Device>();
            devicesToSearch.add(dev);
            ArrayList<Detector> detectors = new ArrayList<Detector>();
            // if the device does respond on any port, only communicating ports are allowed in the drop down
            Map<String, ArrayList<String>> portsFoundCommunicating = new HashMap<String, ArrayList<String>>();
            // if the device does not respond on any port, let the user pick any port that was setup with a valid serial port name, etc.
            Map<String, ArrayList<String>> portsOtherwiseCorrectlyConfigured = new HashMap<String, ArrayList<String>>();
            if (0 == ports.size() ) {
               JOptionPane.showMessageDialog(null, "Could not find any unused ports.");
               return;
            }
            // now simply start a thread for each permutation of port and device, taking care to keep the threads working
            // on unique combinations of device and port
            String looking = "";
            // no devices need to configure serial ports
            if (devicesToSearch.size() < 1) {
               return;
            }
            // during detection we'll generate lots of spurious error messages.
            core.enableDebugLog(false);
            if (devicesToSearch.size() <= ports.size()) {
               // for case where there are more serial ports than devices
               for (int iteration = 0; iteration < ports.size(); ++iteration) {
                  detectors.clear();
                  looking = "";
                  for (int diterator = 0; diterator < devicesToSearch.size(); ++diterator) {
                     int portOffset = (diterator + iteration) % ports.size();
                     try {
                        core.setProperty(devicesToSearch.get(diterator).getName(), MMCoreJ.getG_Keyword_Port(), ports.get(portOffset).getName());
                        detectors.add(new Detector(devicesToSearch.get(diterator).getName(), ports.get(portOffset).getName()));
                        if (0 < looking.length()) {
                           looking += "\n";
                        }
                        looking += devicesToSearch.get(diterator).getName() + " on " + ports.get(portOffset).getName();
                     } catch (Exception e) {
                        // USB devices will try to open the interface and return an error on failure
                        // so do not show, but only log the error
                        ReportingUtils.logError(e);
                     }
                  }
                  progressDialog.ProgressText("Looking for:\n" + looking);
                  for (Detector d : detectors) {
                     d.start();
                  }
                  for (Detector d : detectors) {
                     d.finish();
                     if (progressDialog.CancelRequest()| requestCancel) {
                        System.out.print("cancel request");
                        return; //
                     }
                  }
                  // now the detection at this iteration is complete
                  for (Detector d : detectors) {
                     DeviceDetectionStatus st = d.getStatus();
                     if (DeviceDetectionStatus.CanCommunicate == st) {
                        ArrayList<String> llist = portsFoundCommunicating.get(d.getName());
                        if (null == llist) {
                           portsFoundCommunicating.put(d.getName(), llist = new ArrayList<String>());
                        }
                        llist.add(d.PortName());
                     } else {
                        ArrayList<String> llist = portsOtherwiseCorrectlyConfigured.get(d.getName());
                        if (null == llist) {
                           portsOtherwiseCorrectlyConfigured.put(d.getName(), llist = new ArrayList<String>());
                        }
                        llist.add(d.PortName());
                     }
                  }
               }
               //// ****** complete this detection iteration
            } else { // there are more devices than serial ports...
               for (int iteration = 0; iteration < devicesToSearch.size(); ++iteration) {
                  detectors.clear();
                  looking = "";
                  for (int piterator = 0; piterator < ports.size(); ++piterator) {
                     int dOffset = (piterator + iteration) % devicesToSearch.size();
                     try {
                        core.setProperty(devicesToSearch.get(dOffset).getName(), MMCoreJ.getG_Keyword_Port(), ports.get(piterator).getName());
                        detectors.add(new Detector(devicesToSearch.get(dOffset).getName(), ports.get(piterator).getName()));
                        if (0 < looking.length()) {
                           looking += "\n";
                        }
                        looking += devicesToSearch.get(dOffset).getName() + " on " + ports.get(piterator).getName();
                     } catch (Exception e) {
                        ReportingUtils.showError(e);
                     }
                  }
                  progressDialog.ProgressText("Looking for:\n" + looking);
                  for (Detector d : detectors) {
                     d.start();
                  }
                  for (Detector d : detectors) {
                     d.finish();
                     if (progressDialog.CancelRequest() || requestCancel) {
                        System.out.print("cancel request");
                        return; //
                     }                        }
                  // the detection at this iteration is complete
                  for (Detector d : detectors) {
                     DeviceDetectionStatus st = d.getStatus();
                     if (DeviceDetectionStatus.CanCommunicate == st) {
                        ArrayList<String> llist = portsFoundCommunicating.get(d.getName());
                        if (null == llist) {
                           portsFoundCommunicating.put(d.getName(), llist = new ArrayList<String>());
                        }
                        llist.add(d.PortName());
                     } else {
                        ArrayList<String> llist = portsOtherwiseCorrectlyConfigured.get(d.getName());
                        if (null == llist) {
                           portsOtherwiseCorrectlyConfigured.put(d.getName(), llist = new ArrayList<String>());
                        }
                        llist.add(d.PortName());
                     }
                  }
               }
            }
            String foundem = "";
            // show the user the result and populate the drop down data
            for (Device dd : devicesToSearch) {
               ArrayList<String> communicating = portsFoundCommunicating.get(dd.getName());
               ArrayList<String> onlyConfigured = portsOtherwiseCorrectlyConfigured.get(dd.getName());
               foundPorts = new String[0];
               boolean any = false;
               if (null != communicating) {
                  if (0 < communicating.size()) {
                     any = true;
                     foundPorts = new String[communicating.size()];
                     int aiterator = 0;
                     foundem += dd.getName() + " on ";
                     for (String ss : communicating) {
                        foundem += (ss + "\n");
                        foundPorts[aiterator++] = ss;
                     }
                  }
               }
               // all this ugliness  because no multimap in Java...
               if (!any) {
                  if (null != onlyConfigured) {
                     if (0 < onlyConfigured.size()) {
                        Collections.sort(onlyConfigured);
                        foundPorts = new String[onlyConfigured.size()];
                        int i2 = 0;
                        for (String ss : onlyConfigured) {
                           foundPorts[i2++] = ss;
                        }
                     }
                  }
               }
               PropertyItem p = dd.findProperty(MMCoreJ.getG_Keyword_Port());
               p.allowed = foundPorts;
               p.value = "";
               selectedPort = "";
               if (0 < foundPorts.length) {
                  if (foundPorts.length > 1) {
                     String selectedValue = (String)JOptionPane.showInputDialog(null, "Multiple ports found, choose one", "Port",
                                            JOptionPane.INFORMATION_MESSAGE, null, foundPorts, foundPorts[0]);
                     // select the last found port
                     p.value = selectedValue;
                  }
                  else {
                     p.value = foundPorts[0];
                  }
                  selectedPort = p.value;
               }
            }
            progressDialog.ProgressText("Found:\n " + foundem);
            try {
               Thread.sleep(900);
            } catch (InterruptedException ex) {
            }
         } finally { // matches try at entry
            progressDialog.setVisible(false);
            core.enableDebugLog(currentDebugLogSetting);
            rebuildPropTable();
            if (!selectedPort.isEmpty()) {
               Device pd = model.findSerialPort(selectedPort);
               if (pd != null)
                  try {
                     pd.loadDataFromHardware(core);
                  } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               rebuildComTable(selectedPort);
            }
            // restore normal operation of the Detect button
            detectButton.setText(DETECT_PORTS);
         }
      }
      public void finish() {
         try {
            join();
         } catch (InterruptedException ex) {
         }
      }
   }

   public String getDeviceName() {
      return devLabel.getText();
   }
}
