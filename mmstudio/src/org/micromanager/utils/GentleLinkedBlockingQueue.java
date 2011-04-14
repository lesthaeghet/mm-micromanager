/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.micromanager.utils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author arthur
 */
public class GentleLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

   public GentleLinkedBlockingQueue() {
      super();
   }

   @Override
   public void put(E e) throws InterruptedException {
      final int n = 1000 / 5; // Timeout after 1 second.
      final long minimumBytes = 5000000;
      for (int i=0; i<n; ++i) {
         long m = JavaUtils.getAvailableUnusedMemory();
         if (m > minimumBytes)
            break;
         ReportingUtils.logError("Running out of memory: " + minimumBytes + "left.");
         JavaUtils.sleep(5);
      }
      super.put(e);
   }

}
