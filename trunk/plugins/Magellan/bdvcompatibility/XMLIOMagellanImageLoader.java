/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdvcompatibility;

import acq.MultiResMultipageTiffStorage;
import ij.IJ;
import java.io.File;
import java.io.IOException;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

@ImgLoaderIo( format = "catmaid", type = MagellanImgLoader.class)
public class XMLIOMagellanImageLoader implements XmlIoBasicImgLoader<MagellanImgLoader> {

   @Override
   public org.jdom2.Element toXml(MagellanImgLoader loader, File basePath) {
      throw new UnsupportedOperationException("not implmented");      
   }

   @Override
   public MagellanImgLoader fromXml(org.jdom2.Element elmnt, File file, AbstractSequenceDescription<?, ?, ?> asd) { 
      try {
         return new MagellanImgLoader(new MultiResMultipageTiffStorage(file.getParent()));
      } catch (IOException ex) {
         IJ.log("Couldn't open dataset");
         return null;
      }
   }

}
