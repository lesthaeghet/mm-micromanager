/*
 * This class uses autocorrelation to detect the movement between a reference image
 * and the given image
 */
package edu.valelab.GaussianFit;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.FHT;
import ij.process.ImageProcessor;

/**
 *
 * @author Nico Stuurman
 */
public class JitterDetector {
   private final FHT ref_;
   
   public JitterDetector(ImageProcessor reference) {
     ref_ = new FHT(reference);
     ref_.transform();
     ref_.resetMinAndMax();
     new ImagePlus("Ref test", ref_).show();
   }
   
   public void getJitter(ImageProcessor test, double x, double y) {
      FHT t = new FHT(test);
      t.transform();
      t.resetMinAndMax();
      new ImagePlus("Ref Test2", t).show();
      
      FHT m = ref_.conjugateMultiply(t);
            
      m.inverseTransform();
      m.swapQuadrants();
      ij.IJ.showStatus("Display image");
      m.resetMinAndMax();
      int midx = m.getWidth() / 2;
      int midy = m.getHeight() / 2;
      int hw = 32;
      m.setRoi(midx - hw, midy - hw, 2* hw, 2* hw);
      ImagePlus mp = new ImagePlus("JitterTest", m);
      Analyzer al = new Analyzer(mp, ij.measure.Measurements.CENTER_OF_MASS, new ResultsTable());
      al.measure();
      al.summarize();
      
      al.displayResults();
      mp.show();
   }
   
}
