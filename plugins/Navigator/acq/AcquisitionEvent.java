/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acq;

import java.awt.geom.Point2D;

/**
 * Information about the acquisition of a single image
 */
public class AcquisitionEvent {
   
   final public Acquisition acquisition_;
   final public int timeIndex_, sliceIndex_, channelIndex_, positionIndex_;
   final public double zPosition_, xPosition_, yPosition_;
   
   public AcquisitionEvent(Acquisition acq, int frameIndex, int channelIndex, int sliceIndex, int positionIndex, 
           double zPos, double xPos, double yPos) {
      timeIndex_ = frameIndex;
      sliceIndex_ = sliceIndex;
      channelIndex_ = channelIndex;
      positionIndex_ = positionIndex;    
      zPosition_ = zPos;
      xPosition_ = xPos;
      yPosition_ = yPos;
      acquisition_ = acq;
   }
   
   @Override
   public String toString() {
      return "P: " + positionIndex_ + "\t\tT: " + timeIndex_ + "\t\tZ: " + sliceIndex_ + "\t\tC: " + channelIndex_; 
   }
   
}
