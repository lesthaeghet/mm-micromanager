/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import ij.IJ;
import main.Magellan;

/**
 *
 * @author bidc
 */
public class Log {
   
   public static void log(String message) {
      log(message, true);
   }

   public static void log(String message, boolean show) {
      Magellan.getCore().logMessage(message);
       if (show) {
          IJ.log(message);
       }
   }

   public static void log(Exception e) {
      log(e, true);
   }

   public static void log(Exception e, boolean show) {
      Magellan.getCore().logMessage(getStackTraceAsString(e));
      Magellan.getCore().logMessage(e.getMessage());
      if (show) {
         IJ.log(getStackTraceAsString(e));
      }
   }
   
      private static String getStackTraceAsString(Throwable aThrowable) {
      String result = "";
      for (StackTraceElement line : aThrowable.getStackTrace()) {
         result += "  at " + line.toString() + "\n";
      }
      Throwable cause = aThrowable.getCause();
      if (cause != null) {
         return result + "Caused by: " + cause.toString() + "\n" + getStackTraceAsString(cause);
      } else {
         return result;
      }
   }

}
