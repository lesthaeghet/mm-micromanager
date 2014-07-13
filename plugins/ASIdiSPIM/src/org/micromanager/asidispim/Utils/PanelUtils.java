///////////////////////////////////////////////////////////////////////////////
//FILE:          PanelUtils.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
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

package org.micromanager.asidispim.Utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.api.ScriptInterface;
import org.micromanager.asidispim.ASIdiSPIM;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Joystick;
import org.micromanager.asidispim.Data.Positions;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;

/**
 *
 * @author nico
 * @author Jon
 */
public class PanelUtils {
   private final ScriptInterface gui_;
   private final Prefs prefs_;
   
   public PanelUtils(ScriptInterface gui, Prefs prefs) {
      gui_ = gui;
      prefs_ = prefs;
   }
   
   /**
    * makes JSlider for double values where the values are multiplied by a scale factor
    * before internal representation as integer (as JSlider requires)
    * @return
    */
   public JSlider makeSlider(double min, double max, final int scalefactor, Properties props, Devices devs,
         Devices.Keys devKey, Properties.Keys propKey) {
      
      class sliderListener implements ChangeListener, UpdateFromPropertyListenerInterface, DevicesListenerInterface {
         private final JSlider js_;
         private final int scalefactor_;
         private final Properties props_;
         private final Devices.Keys devKey_;
         private final Properties.Keys propKey_;
         
         public sliderListener(JSlider js, int scalefactor, Properties props, 
                 Devices.Keys devKey, Properties.Keys propKey) {
            js_ = js;
            scalefactor_ = scalefactor;
            props_ = props;
            devKey_ = devKey;
            propKey_ = propKey;
         }
         
         @Override
         public void stateChanged(ChangeEvent ce) {
            if (!((JSlider)ce.getSource()).getValueIsAdjusting()) {  // only change when user releases
               props_.setPropValue(devKey_, propKey_, (float)js_.getValue()/(float)scalefactor_, true);
            }
         }
         
         @Override
         public void updateFromProperty() {
            js_.setValue((int)(scalefactor_*props_.getPropValueFloat(devKey_, propKey_, true)));
         }
         
         @Override
         public void devicesChangedAlert() {
            // TODO refresh limits
            updateFromProperty();
         }
                
      }
      
      int intmin = (int)(min*scalefactor);
      int intmax = (int)(max*scalefactor);
      
      JSlider js = new JSlider(JSlider.HORIZONTAL, intmin, intmax, intmin);  // initialize with min value, will set to current value shortly 
      ChangeListener l = new sliderListener(js, scalefactor, props, devKey, propKey);
      ((UpdateFromPropertyListenerInterface) l).updateFromProperty();  // set to value of property at present
      js.addChangeListener(l);
      devs.addListener((DevicesListenerInterface) l);
      props.addListener((UpdateFromPropertyListenerInterface) l);

      js.setMajorTickSpacing(intmax-intmin);
      js.setMinorTickSpacing(scalefactor);
      
      //Create the label table
      Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
      labelTable.put( new Integer(intmax), new JLabel(Double.toString(max)) );
      labelTable.put( new Integer(intmin), new JLabel(Double.toString(min)) );
      
      js.setLabelTable( labelTable );
      js.setPaintTicks(true);
      js.setPaintLabels(true);
      
      return js;
   }
   
   /**
    * Creates spinner for integers in the GUI.
    * Implements UpdateFromPropertyListenerInterface, causing updates in the model
    * that were generated by changes in the device to be propagated back to the UI.
    * @param min - minimum value for the spinner
    * @param max - maximum value for the spinner
    * @param step - stepsize for the spinner
    * @param props - the singleton properties class instance that holds all our property info
    * @param devs - singleton device class instance holding device info
    * @param devKeys - array of device keys, use inline constructor "new Devices.Keys[]{<list of devices>}"
    * @param propKey - property key for this spinner
    * @param saveToPrefs - will be saved to prefs using the supplied propKey; not needed if being saved on controller
    * @param prefNode - preference node to save under, usually supplied as panelName_
    * @return the created JSpinner
    */
   public JSpinner makeSpinnerInteger(int min, int max, Properties props, Devices devs, 
         Devices.Keys [] devKeys, Properties.Keys propKey, int defaultVal) {

      class SpinnerListenerInt implements ChangeListener, UpdateFromPropertyListenerInterface, DevicesListenerInterface {
         private final JSpinner sp_;
         private final Properties props_;
         private final Devices.Keys [] devKeys_;
         private final Properties.Keys propKey_;
         
         public SpinnerListenerInt(JSpinner sp, Properties props, 
               Devices.Keys [] devKeys, Properties.Keys propKey) {
            sp_ = sp;
            props_ = props;
            devKeys_ = devKeys;
            propKey_ = propKey;
         }
         
         private int getSpinnerValue() {
            return ((Integer)sp_.getValue());
         }

         @Override
         public void stateChanged(ChangeEvent ce) {
            int spinnerValue = getSpinnerValue();
            for (Devices.Keys devKey : devKeys_) {
               // property reads (core calls) are inexpensive compared to 
               //   property writes (serial comm) so only write if needed
               // however, this doesn't solve problem of properties that are really
               //    card-specific (not axis-specific) because other devices on same
               //    card may have been changed but not refreshed in micro-Manager
               if (props_.getPropValueInteger(devKey, propKey_) != spinnerValue) {
                  props_.setPropValue(devKey, propKey_, spinnerValue, true);
                  // ignore error for sake of missing device assignment
               }
            }
         }

         @Override
         public void updateFromProperty() {
            sp_.setValue(props_.getPropValueInteger(devKeys_[0], propKey_, true));
            stateChanged(new ChangeEvent(sp_));  // fire manually to set all the devices is devKeys
         }
         
         @Override
         public void devicesChangedAlert() {
            updateFromProperty();
         }
      }
      
      // read the existing value of 1st device and make sure it is within our min/max limits
      int origVal = props.getPropValueInteger(devKeys[0], propKey, true);
      if (origVal == 0) {
      // if getPropValue returned 0 (sign no value existed) then use default
         origVal = defaultVal;
         props.setPropValue(devKeys[0], propKey, defaultVal, true);
      }
      if (origVal < min) {
         origVal = min;
         props.setPropValue(devKeys[0], propKey, min, true);
         // ignore error for sake of missing device assignment
      }
      if (origVal > max) {
         origVal = max;
         props.setPropValue(devKeys[0], propKey, max, true);
         // ignore error for sake of missing device assignment
      }

      SpinnerModel jspm = new SpinnerNumberModel(origVal, min, max, 1);
      JSpinner jsp = new JSpinner(jspm);
      SpinnerListenerInt ispl = new SpinnerListenerInt(jsp, props, devKeys, propKey);
      jsp.addChangeListener(ispl);
      devs.addListener(ispl);
      props.addListener(ispl);
      JComponent editor = jsp.getEditor();
      JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
      tf.setColumns(4);
      return jsp;
   }
   
   /**
    * Utility function to access the spinner value.
    * @param sp
    * @return
    */
   public static float getSpinnerFloatValue(JSpinner sp) {
      // TODO figure out why the type of value in the numbermodel is 
      // changing type to float which necessitates this code
      float f;
      try {
         f = (float) ((Double) sp.getValue()).doubleValue();
      } catch (Exception ex) {
         f = ((Float) sp.getValue()).floatValue();
      }
      return f;
   }
   
   /**
    * Utility function to write to the spinner value.
    * @param sp
    * @return
    */
   public static void setSpinnerFloatValue(JSpinner sp, float f) {
      // TODO figure out why the type of value in the numbermodel is 
      // changing type to float which necessitates this code
      try {
         sp.setValue((Double)((double)f));
      } catch (Exception ex) {
         sp.setValue((Float)f);
      }
   }
   
   /**
    * Utility function to write to the spinner value.
    * @param sp
    * @return
    */
   public static void setSpinnerFloatValue(JSpinner sp, double d) {
      // TODO figure out why the type of value in the numbermodel is 
      // changing type to float which necessitates this code
      try {
         sp.setValue((Double)d);
      } catch (Exception ex) {
         sp.setValue((Float)((float)d));
      }
   }
   
   /**
    * Creates spinner for floats in the GUI.
    * Implements UpdateFromPropertyListenerInterface, causing updates in the model
    * that were generated by changes in the device to be propagated back to the UI.
    * @param min - minimum value for the spinner
    * @param max - maximum value for the spinner
    * @param step - stepsize for the spinner
    * @param props - the singleton properties class instance that holds all our property info
    * @param devs - singleton device class instance holding device info
    * @param devKeys - array of device keys, use inline constructor "new Devices.Keys[]{<list of devices>}"
    * @param propKey - property key for this spinner
    * @param saveToPrefs - will be saved to prefs using the supplied propKey; not needed if being saved on controller
    * @param prefNode - preference node to save under, usually supplied as panelName_
    * @return the created JSpinner
    */
   public JSpinner makeSpinnerFloat(double min, double max, double step, Properties props, Devices devs, 
         Devices.Keys [] devKeys, Properties.Keys propKey, double defaultVal) {
      // same as IntSpinnerListener except
      //  - different getSpinnerValue() implementation
      //  - getPropValueFloat in stateChanged()
      class SpinnerListenerFloat implements ChangeListener, UpdateFromPropertyListenerInterface, DevicesListenerInterface {
         private final JSpinner sp_;
         private final Properties props_;
         private final Devices.Keys [] devKeys_;
         private final Properties.Keys propKey_;

         public SpinnerListenerFloat(JSpinner sp, Properties props, Devices.Keys [] devKeys, 
               Properties.Keys propKey) {
            sp_ = sp;
            props_ = props;
            devKeys_ = devKeys;
            propKey_ = propKey;
         }
                
         @Override
         public void stateChanged(ChangeEvent ce) {
            float spinnerValue = getSpinnerFloatValue(sp_);
            for (Devices.Keys devKey : devKeys_) {
               // property reads (core calls) are inexpensive compared to 
               //   property writes (serial comm) so only write if needed
               // however, this doesn't solve problem of properties that are really
               //    for the card (not for the axis) because other devices on same
               //    card may have been changed but not refreshed in micro-Manager
               if (!PanelUtils.floatsEqual(props_.getPropValueFloat(devKey, propKey_), spinnerValue)) {
                  props_.setPropValue(devKey, propKey_, spinnerValue, true);
               // ignore error for sake of missing device assignment
               }
            }
         }

         @Override
         public void updateFromProperty() {
            sp_.setValue(props_.getPropValueFloat(devKeys_[0], propKey_, true));
            stateChanged(new ChangeEvent(sp_));  // fire manually to set all the devices is devKeys
         }
         
         @Override
         public void devicesChangedAlert() {
            updateFromProperty();
         }
      }
      
      // read the existing value of 1st device and make sure it is within our min/max limits
      double origVal = (double)props.getPropValueFloat(devKeys[0], propKey, true);
      if (floatsEqual((float) origVal, (float) 0)) {
         // if getPropValue returned 0 (sign no value existed) then use default
         origVal = defaultVal;
         props.setPropValue(devKeys[0], propKey, (float)defaultVal, true);
      }
      if (origVal < min) {
         origVal = min;
         props.setPropValue(devKeys[0], propKey, (float)min, true);
         // ignore error for sake of missing device assignment
      }
      if (origVal > max) {
         origVal = max;
         props.setPropValue(devKeys[0], propKey, (float)max, true);
         // ignore error for sake of missing device assignment
      }
      
      // all devices' properties will be set on tab's gotSelected which calls updateFromProperty
      
      SpinnerModel jspm = new SpinnerNumberModel(origVal, min, max, step);
      JSpinner jsp = new JSpinner(jspm);
      SpinnerListenerFloat ispl = new SpinnerListenerFloat(jsp, props, devKeys, propKey);
      jsp.addChangeListener(ispl);
      devs.addListener(ispl);
      props.addListener(ispl);
      JComponent editor = jsp.getEditor();
      JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
      tf.setColumns(4);
      return jsp;
   }

   
   /**
    * Constructs a DropDown box selecting between multiple strings.
    * Sets selection based on property value and attaches a Listener.
    * 
    * @param propKey - property key as known in the params
    * @param props
    * @param devs
    * @param vals - array of strings, each one is a different option in the dropdown 
    * @param devKeys 
    * @return constructed JComboBox
    */
   public JComboBox makeDropDownBox(String[] vals, Properties props, Devices devs,
         Devices.Keys [] devKeys, Properties.Keys propKey) {
      /**
       * Listener for the string-based dropdown boxes
       * Updates the model in the params class with any GUI changes
       */
      class StringBoxListener implements ActionListener, UpdateFromPropertyListenerInterface, DevicesListenerInterface {
         private final JComboBox box_;
         private final Properties props_;
         private final Devices.Keys [] devKeys_;
         private final Properties.Keys propKey_;

         public StringBoxListener(JComboBox box, Properties props, Devices.Keys [] devKeys, Properties.Keys propKey) {
            box_ = box;
            props_ = props;
            devKeys_ = devKeys;
            propKey_ = propKey;
         }
         
         private String getBoxValue() {
            return (String) box_.getSelectedItem();
         }

         @Override
         public void actionPerformed(ActionEvent ae) {
            // unlike analogous int/float functions, this handler is called on any setSelectedItem 
            String boxValue = getBoxValue();
            for (Devices.Keys devKey : devKeys_) {
               // property reads (core calls) are inexpensive compared to 
               //   property writes (serial comm) so only write if needed
               // however, this doesn't solve problem of properties that are really
               //    for the card (not for the axis) because other devices on same
               //    card may have been changed but not refreshed in micro-Manager
               if (!props_.getPropValueString(devKey, propKey_).equals(boxValue)) {
                  props_.setPropValue(devKey, propKey_, boxValue, true);
               }
            }
         }
         
         @Override
         public void updateFromProperty() {
            box_.setSelectedItem(props_.getPropValueString(devKeys_[0], propKey_, true));
         }
         
         @Override
         public void devicesChangedAlert() {
            updateFromProperty();
         }
      }
      
      String origVal = props.getPropValueString(devKeys[0], propKey);
      JComboBox jcb = new JComboBox(vals);
      jcb.setSelectedItem(origVal);
      StringBoxListener l = new StringBoxListener(jcb, props, devKeys, propKey);
      jcb.addActionListener(l);
      devs.addListener(l);
      props.addListener(l);
      return jcb;
   }
   
   
  /**
   * Creates formatted text field for user to enter decimal (double) values.
   * @param prefNode - String identifying preference node where this variable 
   *                    be store such that its value can be retrieved on restart
   * @param prefKey - String used to identify this preference
   * @param defaultValue - initial (default) value.  Will be overwritten by
   *                       value in Preferences
   * @param numColumns - width of the GUI element
   * @return - JFormattedTextField element
   */
   public JFormattedTextField makeFloatEntryField(String prefNode, String prefKey, 
           double defaultValue, int numColumns) {
      
      class FieldListener implements PropertyChangeListener {
         private final JFormattedTextField tf_;
         private final String prefNode_;
         private final String prefKey_;

         @Override
         public void propertyChange(PropertyChangeEvent evt) {
            try {
               prefs_.putFloat(prefNode_, prefKey_, ((Double)tf_.getValue()).floatValue());
            } catch (Exception e) {
               gui_.showError(e);
            }
         }
         
         public FieldListener(JFormattedTextField tf, String prefNode, String prefKey) {
            prefNode_ = prefNode;
            prefKey_ = prefKey;
            tf_ = tf;
         }
      }
      
      JFormattedTextField tf = new JFormattedTextField();
      tf.setValue((double) prefs_.getFloat(prefNode, prefKey, (float)defaultValue));
      tf.setColumns(numColumns);
      PropertyChangeListener listener = new FieldListener(tf, prefNode, prefKey);
      tf.addPropertyChangeListener("value", listener);
      return tf;
   }
   
   
        
   
   
   /**
    * Creates field for user to type in new position for an axis, with default value of 0.
    * @param key
    * @param dir
    * @param positions
    * @return
    */
   public JFormattedTextField makeSetPositionField(Devices.Keys key, Joystick.Directions dir, Positions positions) {

      class setPositionListener implements PropertyChangeListener { 
         private final Devices.Keys key_;
         private final Joystick.Directions dir_;
         private final Positions positions_;

         @Override
         public void propertyChange(PropertyChangeEvent evt) {
            try {
               positions_.setPosition(key_, dir_, ((Number)evt.getNewValue()).doubleValue());
            } catch (Exception e) {
               gui_.showError(e);
            }
         }

         setPositionListener(Devices.Keys key, Joystick.Directions dir, Positions positions) {
            key_ = key;
            dir_ = dir;
            positions_ = positions;
         }
      }
      
      // this is an attempt to allow enter presses to register as "changes" too
      // useful when the desired value is already in the field but the position is elsewhere
      // now enter works but only if the field has been edited, for instance if 0 is there you
      // can delete the 0 and then retype it and enter will register as an action and set the position
      class setPositionListener2 implements ActionListener {
         private final Devices.Keys key_;
         private final Joystick.Directions dir_;
         private final Positions positions_;
         private final JFormattedTextField tf_;

         @Override
         public void actionPerformed(ActionEvent evt) {
            try {
               positions_.setPosition(key_, dir_, ((Number)tf_.getValue()).doubleValue());
            } catch (Exception e) {
               gui_.showError(e);
            }
         }
         
         setPositionListener2(Devices.Keys key, Joystick.Directions dir, Positions positions, JFormattedTextField tf) {
            key_ = key;
            dir_ = dir;
            positions_ = positions;
            tf_ = tf;
         }
      }

      JFormattedTextField tf = new JFormattedTextField();
      
      tf.setValue(0.0);
      tf.setColumns(4);
      PropertyChangeListener pc = new setPositionListener(key, dir, positions);
      ActionListener al = new setPositionListener2(key, dir, positions, tf);
      tf.addPropertyChangeListener("value", pc);
      tf.addActionListener(al);
      return tf;
   }
  
   
   /**
    * takes a JSpinner and adds a listener that is guaranteed to be called 
    * after the other listeners.
    * Modifies the JSpinner!!
    * @param js - spinner to which listener will be added
    * @param lastListener - listener that will be added at the end
    */
   public void addListenerLast(JSpinner js, final ChangeListener lastListener) {
      final ChangeListener [] origListeners = js.getChangeListeners();
      for (ChangeListener list : origListeners) {
         js.removeChangeListener(list);
      }

      ChangeListener newListener = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            for (ChangeListener list : origListeners) {
               list.stateChanged(e);
            }
            lastListener.stateChanged(e);
         }
      };
      js.addChangeListener(newListener);
   }
   
   /**
    * makes border with centered title text
    * @param title
    * @return
    */
   public static TitledBorder makeTitledBorder(String title) {
      TitledBorder myBorder = BorderFactory.createTitledBorder(
              BorderFactory.createLineBorder(ASIdiSPIM.borderColor), title);
      myBorder.setTitleJustification(TitledBorder.CENTER);
      return myBorder;
   }
   
   /**
    * Does "equality" test on floats, using locally-defined epsilon (1e-12)
    * @param f1
    * @param f2
    * @return
    */
   public static boolean floatsEqual(float f1, float f2) {
      final float EPS = (float) 1e-12;
      float diff = f2 - f1;
      return((diff < EPS) && (diff > -EPS));
   }
   
   /**
    * "rounds up" to nearest increment of 0.25, e.g. 0 goes to 0 but 0.01 goes to 0.25
    * @param f
    * @return
    */
   public static float ceilToQuarterMs(float f) {
      return (float) (Math.ceil(f*4)/4);
   }
   
   /**
    * "rounds up" to nearest increment of 0.25
    * @param f
    * @return
    */
   public static float roundToQuarterMs(float f) {
      return (float) (((float) Math.round(f*4))/4);
   }
   
   
}
