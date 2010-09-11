import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.measure.ResultsTable;
import ij.text.*;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.IJ;

import org.apache.commons.math.analysis.*;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.optimization.direct.MultiDirectional;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;

import java.lang.Math;
import java.awt.Rectangle;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Vector;




public class GaussianTrack_ implements PlugIn {
	double[] params0_ = {16000.0, 5.0, 5.0, 1.0, 850.0};
	double[] steps_ = new double[5];
	String [] paramNames_ = {"A", "x_c", "y_c", "sigma", "b"};

   GaussianResidual gs_;
   NelderMead nm_;
   SimpleScalarValueChecker convergedChecker_;;

	private void print(String myText) {
		ij.IJ.log(myText);
	}

   public class GaussianResidual implements MultivariateRealFunction {
      short[] data_;
      int nx_;
      int ny_;
      int count_ = 0;


      public void setImage(short[] data, int width, int height) {
          data_ = data;
          nx_ = width;
          ny_ = height;
      }

      public double value(double[] params) {
          double residual = 0.0;
          for (int i = 0; i < nx_; i++) {
             for (int j = 0; j < ny_; j++) {
                residual += sqr(gaussian(params, i, j) - data_[(j*nx_) + i]);
             }
          }
          return residual;
      }

      public double sqr(double val) {
         return val*val;
      }

      public double gaussian(double[] params, int x, int y) {

         /* Gaussian function of the form:
          * A *  exp(-((x-xc)^2+(y-yc)^2)/(2 sigy^2))+b
          * A = params[0]  (total intensity)
          * xc = params[1]
          * yc = params[2]
          * sig = params[3]
          * b = params[4]  (background)
          */

         if (params.length < 5) {
                          // Problem, what do we do???
                          //MMScriptException e;
                          //e.message = "Params for Gaussian function has too few values";
                          //throw (e);
         }

         double exponent = (sqr(x - params[1])  + sqr(y - params[2])) / (2 * sqr(params[3]));
         double res = params[0] * Math.exp(-exponent) + params[4];
         return res;
      }
   }

   /**
    * Linear Regression to find the best line between a set of points
    * returns an array where [0] = slope and [1] = offset
    * Input: arrays with x and y data points
    */
   public double[] fitLine(Vector<Point2D.Double> xyPoints) {
      double[][] xWithOne = new double[xyPoints.size()][2];
      double[][] yWithOne = new double[xyPoints.size()][2];
      for (int i =0; i< xyPoints.size(); i++) {
         xWithOne[i][0] = xyPoints.get(i).getX();
         xWithOne[i][1] = 1;
         yWithOne[i][0] = xyPoints.get(i).getY();
         yWithOne[i][1] = 1;
      }

      Array2DRowRealMatrix xM = new Array2DRowRealMatrix(xWithOne);
      Array2DRowRealMatrix yM = new Array2DRowRealMatrix(yWithOne);

      QRDecompositionImpl qX = new QRDecompositionImpl(xM);
      BlockRealMatrix mX = (BlockRealMatrix) qX.getSolver().solve(yM);

      RealMatrix theY = xM.multiply(mX);
      double ansX = theY.subtract(yM).getColumnVector(0).getNorm();
      print ("Answer X: " + ansX);

      QRDecompositionImpl qY = new QRDecompositionImpl(yM);
      BlockRealMatrix mY = (BlockRealMatrix) qY.getSolver().solve(xM);

      RealMatrix theX = yM.multiply(mY);
      double ansY = theX.subtract(xM).getColumnVector(0).getNorm();
      print ("Answer Y: " + ansY);

      double[][] res = mX.getData();
      double[] ret = new double[2];
      ret[0] = res[0][0];
      ret[1] = res[1][0];

      if (ansY < ansX) {
         res = mY.getData();
         ret[0] = 1 / res[0][0];
         ret[1] = - res[1][0]/res[0][0];
      }

      return ret;
   }

   public AffineTransform computeAffineTransform(double a, double b) {
      AffineTransform T = new AffineTransform();
      T.rotate(-Math.atan(a));
      T.translate(0, -b);
      return T;
   }

   /**
    * KeyListener class for ResultsTable
    * When user selected a line in the ResulsTable and presses a key,
    * the corresponding image will move to the correct slice and draw the ROI
    * that was used to calculate the Gaussian fit
    * Works only in conjunction with appropriate column names
    * Up and down keys also work as expected
    */
   public class MyK implements KeyListener{
      ImagePlus siPlus_;
      ResultsTable res_;
      TextWindow win_;
      TextPanel tp_;
      int hBS_;
      public MyK(ImagePlus siPlus, ResultsTable res, TextWindow win, int halfBoxSize) {
         siPlus_ = siPlus;
         res_ = res;
         win_ = win;
         tp_ = win.getTextPanel();
         hBS_ = halfBoxSize;
      }
      public void keyPressed(KeyEvent e) {
         int key = e.getKeyCode();
         int row = tp_.getSelectionStart();
         if (key == KeyEvent.VK_DOWN) {
            if (row > 0) {
               row--;
               tp_.setSelection(row, row);
            }
         } else if (key == KeyEvent.VK_UP) {
            if  (row < tp_.getLineCount() - 1) {
               row++;
               tp_.setSelection(row, row);
            }
         }
         if (row >= 0 && row < tp_.getLineCount()) {
            // These two lines ensure that the Image Window is visible, but do cause flicker
            siPlus_.getWindow().toFront();
            win_.toFront();
            int frame = (int) res_.getValue("Frame", row);
            int x = (int)res_.getValue("XMax", row);
            int y = (int) res_.getValue("YMax", row);
            siPlus_.setSlice(frame);
            siPlus_.setRoi(new Roi(x - hBS_ , y - hBS_, 2 * hBS_, 2 * hBS_));
         }
     };
      public void keyReleased(KeyEvent e) {};
      public void keyTyped(KeyEvent e) {};
   }


   /**
    * Performs Gaussian Fit on a given ImageProcessor
    * Estimates initial values for the fit and send of to Apache fitting code
    */
	public double[] doGaussianFit (ImageProcessor siProc) {

      short[] imagePixels = (short[])siProc.getPixels();
		gs_.setImage((short[])siProc.getPixels(), siProc.getWidth(), siProc.getHeight());

      // Hard code estimate for sigma:
      params0_[3] = 1.115;

      // estimate background by averaging pixels at the edge
      double bg = 0.0;
      int n = 0;
      int lastRowOffset = (siProc.getHeight() - 1) * siProc.getWidth();
      for (int i =0; i < siProc.getWidth(); i++) {
         bg += imagePixels[i];
         bg += imagePixels[i + lastRowOffset];
         n += 2;
      }
      for (int i = 1; i < siProc.getHeight() - 1; i++) {
         bg += imagePixels[i * siProc.getWidth()];
         bg += imagePixels[(i + 1) *siProc.getWidth() - 1];
         n += 2;
      }
      params0_[4] = bg / n;

      // estimate signal by subtracting background from total intensity 
      double ti = 0.0;
      double mt = 0.0;
      for (int i = 0; i < siProc.getHeight() * siProc.getWidth(); i++) {
         mt += imagePixels[i];
      }
      ti = mt - ( (bg / n) * siProc.getHeight() * siProc.getWidth());
      params0_[0] = ti / (2 * Math.PI * params0_[3] * params0_[3]);
      // print("Total signal: " + ti + "Estimate: " + params0_[0]);

      // estimate center of mass
      double mx = 0.0;
      double my = 0.0;
      for (int i = 0; i < siProc.getHeight() * siProc.getWidth(); i++) {
         //mx += (imagePixels[i] - params0_[4]) * (i % siProc.getWidth() );
         //my += (imagePixels[i] - params0_[4]) * (Math.floor (i / siProc.getWidth()));
         mx += imagePixels[i]  * (i % siProc.getWidth() );
         my += imagePixels[i]  * (Math.floor (i / siProc.getWidth()));
      }
      params0_[1] = mx/mt;
      params0_[2] = my/mt;

      print("Centroid: " + mx/mt + " " + my/mt);

      // set step size during estimate
		for (int i=0;i<params0_.length;++i)
			steps_[i] = params0_[i]*0.3;

		nm_.setStartConfiguration(steps_);
		nm_.setConvergenceChecker(convergedChecker_);

		nm_.setMaxIterations(200);
		double[] paramsOut = {0.0};
		try {
			RealPointValuePair result = nm_.optimize(gs_, GoalType.MINIMIZE, params0_);
			paramsOut = result.getPoint();
		} catch (Exception e) {}

      return paramsOut;
	}

	public void run(String arg) {

      // objects used in Gaussian fitting
		gs_ = new GaussianResidual();
		nm_ = new NelderMead();
		convergedChecker_ = new SimpleScalarValueChecker(1e-5,-1);

      // Filters for results of Gaussian fit
      double intMin = 100;
      double intMax = 1E7;
      double sigmaMin = 0.8;
      double sigmaMax = 2.1;

      // half the size of the box used for Gaussian fitting in pixels
      int halfSize = 6;

      // initial setting for Maximum Finder
      int noiseTolerance = 100;

      // for now, take the active ImageJ image (this should be an image of a difraction limited spot
		ImagePlus siPlus = IJ.getImage();

      Roi originalRoi = siPlus.getRoi();
      if (null == originalRoi) { 
         IJ.error("Please draw a Roi around the spot you want to track");
         return;
      }
      int sliceN = siPlus.getSlice();

      ResultsTable rt = new ResultsTable();

      Rectangle rect = originalRoi.getBounds();
      int xc = (int) (rect.getX() + 0.5 * rect.getWidth());
      int yc = (int) (rect.getY() + 0.5 * rect.getHeight());


		long startTime = System.nanoTime();

      Vector<Point2D.Double> xyPoints = new Vector<Point2D.Double>();
      for (int i = sliceN; i <= siPlus.getNSlices(); i++) {
         // Search in next slice in same Roi for local maximum
         Roi spotRoi = new Roi(xc - halfSize, yc - halfSize, 2 * halfSize, 2*halfSize);
         siPlus.setSlice(i);
         siPlus.setRoi(spotRoi);

         // Find maximum in Roi
         IJ.run("Find Maxima...", "noise=" + noiseTolerance + " output=List");
         ResultsTable rtS = ResultsTable.getResultsTable();
         if (rtS.getCounter() >=1) {
            xc = (int) rtS.getValueAsDouble(0, 0);
            yc = (int) rtS.getValueAsDouble(1, 0);
         }

         // Set Roi for fitting centered around maximum
         spotRoi = new Roi(xc - halfSize, yc - halfSize, 2 * halfSize, 2*halfSize);
         siPlus.setRoi(spotRoi);
         ImageProcessor ip = siPlus.getProcessor().crop();
         
         double[]paramsOut = doGaussianFit(ip);
         if (paramsOut.length >= 4) {                                         
            double anormalized = paramsOut[0] * (2 * Math.PI * paramsOut[3] * paramsOut[3]);
            double x = paramsOut[1] - halfSize + xc;
            double y = paramsOut[2] - halfSize + yc;
            xyPoints.add(new Point2D.Double(x, y));
            boolean report = anormalized > intMin && anormalized < intMax &&  
                              paramsOut[3] > sigmaMin && paramsOut[3] < sigmaMax;

            rt.incrementCounter();                                         
            rt.addValue("Frame", i);                                     
            rt.addValue("Intensity", anormalized);                         
            rt.addValue("Background", paramsOut[4]);                       
            rt.addValue("X", x);     
            rt.addValue("Y", y);     
            rt.addValue("Sigma", paramsOut[3]);                            
            rt.addValue("XMax", xc);
            rt.addValue("YMax", yc);

            // rt.addValue("Residual", gs_.value(paramsOut));
            if (report) {                                                     
               // IJ.log (i + " " + xc + " " + paramsOut[1] + " " + halfSize);
               // IJ.log (xc + " ");
            } 
         }  
      }

      rt.show("Gaussian Fit Tracking Result:");

      // Attach listener to TextPanel
      TextPanel tp;
      Frame frame = WindowManager.getFrame("Gaussian Fit Tracking Result:");
      TextWindow win;
      if (frame!=null && frame instanceof TextWindow) {
         win = (TextWindow)frame;
         tp = win.getTextPanel();
         MyK myk = new MyK(siPlus, rt, win, halfSize);
         tp.addKeyListener(myk);
      }

      long endTime = System.nanoTime();
		double took = (endTime - startTime) / 1E6;

		print("Calculation took: " + took + " milli seconds"); 

      double[] line = fitLine(xyPoints);

      print(line[0] + " " + line[1]);

      AffineTransform T = computeAffineTransform(line[0], line[1]);

      Vector<Point2D.Double> xyCorrPoints = new Vector<Point2D.Double>();
      for (int i = 0; i < xyPoints.size(); i++) {
         Point2D.Double pt = new Point2D.Double();
         xyCorrPoints.add((Point2D.Double)T.transform(xyPoints.get(i), pt));
         print(pt.getX() + "\t" + pt.getY());
      }





      /*
      double[] guess = {-1.1, 467.5};

      try {
         double[] result = cf_.fit(new LinearFunction(), guess);
         print ("Results is of size: " + result.length);
         print ("Slope: " + result[0] + " Offset: " + result[1]);
      } catch (Exception e) {
         print(e.getMessage());
         e.printStackTrace();

      }
      */

   }
}
