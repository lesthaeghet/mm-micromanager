package coordinates;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.geom.AffineTransform;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import misc.JavaUtils;
import misc.Log;
import org.micromanager.MMStudio;

/**
 *
 * @author Henry
 */
public class AffineUtils {
   
   private static TreeMap<String, AffineTransform> affineTransforms_ = new TreeMap<String,AffineTransform>();
   
   public static String transformToString(AffineTransform transform) {
      double[] matrix = new double[4];
      transform.getMatrix(matrix);
      return matrix[0] +"-"+matrix[1]+"-"+matrix[2]+"-"+matrix[3];
   }
   
   public static AffineTransform stringToTransform(String s) {
      double[] mat = new double[4];
      String[] vals = s.split("-");
      for (int i = 0; i < 4; i ++) {
         mat[i] = Double.parseDouble(vals[i]);
      }
      return new AffineTransform(mat);
   }
   
   
   //called when an affine transform is updated
   public static void transformUpdated(String pixelSizeConfig, AffineTransform transform) {
      affineTransforms_.put(pixelSizeConfig, transform);
   }
   
   //Only read from preferences one time, so that an inordinate amount of time isn't spent in native system calls
   public static AffineTransform getAffineTransform(String pixelSizeConfig, double xCenter, double yCenter) {
      try {
         AffineTransform transform = null;
         if (affineTransforms_.containsKey(pixelSizeConfig)) {
            transform = affineTransforms_.get(pixelSizeConfig);
            //copy transform so multiple referneces with different translations cause problems
            double[] newMat = new double[6];
            transform.getMatrix(newMat);
            transform = new AffineTransform(newMat);
         } else {
            //Get affine transform from prefs
            Preferences prefs = Preferences.userNodeForPackage(MMStudio.class);
            transform = JavaUtils.getObjectFromPrefs(prefs, "affine_transform_" + pixelSizeConfig, (AffineTransform) null);
            affineTransforms_.put(pixelSizeConfig, transform);
         }
         //set map origin to current stage position
         double[] matrix = new double[6];
         transform.getMatrix(matrix);
         matrix[4] = xCenter;
         matrix[5] = yCenter;
         return new AffineTransform(matrix);
      } catch (Exception ex) {
         Log.log(ex);
         Log.log("Couldnt get affine transform");
         return null;
      }
   }

 
}
