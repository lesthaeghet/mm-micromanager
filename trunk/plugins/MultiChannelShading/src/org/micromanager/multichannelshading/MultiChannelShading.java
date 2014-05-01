/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.micromanager.multichannelshading;

import org.micromanager.api.MMProcessorPlugin;

/**
 *
 * @author kthorn
 */
public class MultiChannelShading implements org.micromanager.api.MMProcessorPlugin {
   public static final String menuName = "Multi-Channel Shading";
   public static final String tooltipDescription =
      "Apply dark subtraction and flat-field correction";

   public static String versionNumber = "0.1";

   public static Class<?> getProcessorClass() {
      return BFProcessor.class;
   }

   @Override
   public String getDescription() {
      return tooltipDescription;
   }

   @Override
   public String getInfo() {
      return tooltipDescription;
   }

   @Override
   public String getVersion() {
      return versionNumber;
   }

   @Override
   public String getCopyright() {
      return "University of California, 2014";
   }
   
}
