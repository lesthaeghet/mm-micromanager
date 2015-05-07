/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdvcompatibility;

import acq.MultiResMultipageTiffStorage;
import bdv.img.cache.CacheArrayLoader;
import mmcorej.TaggedImage;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;

/**
 * From Tobias:
 * Your CacheArrayLoader will be queried for blocks by the BDV cache (VolatileGlobalCellCache<VolatileIntArray>) and the loaded blocks will be cached in RAM.
 * The BDV cache is set up by your ImgLoader with your CacheArrayLoader.
 */
public class MultiResMPTiffVolatileByteArrayLoader implements CacheArrayLoader<VolatileByteArray> {

   private VolatileByteArray theEmptyArray_;
   private final MultiResMultipageTiffStorage tiffStorage_;

   public MultiResMPTiffVolatileByteArrayLoader(MultiResMultipageTiffStorage tiffStorage) {      
      theEmptyArray_ = new VolatileByteArray(tiffStorage.getTileWidth() * tiffStorage.getTileHeight(), false);
      tiffStorage_ = tiffStorage;
   }
   
   
   @Override
   public int getBytesPerElement() {
      return 1;
   }

   /**
    * 
    * @param timepoint
    * @param setup Setup = channel in our context
    * @param level resolution level
    * @param dimensions dimensions of the block to load  for your tiles this will be XxYx1
    * @param min starting coordinate of block in stack
    * @return
    * @throws InterruptedException 
    */
   @Override
   public VolatileByteArray loadArray(final int timepoint, final int setup, final int level, int[] dimensions, long[] min) throws InterruptedException {
      //From Tobias: To clarify that a bit better: 
      //You do not need to be able to load arbitrary blocks here. Just the ones that you will use from the images returned by your ImgLoader.
      //So this is the only “shape” of block that your CacheArrayLoader needs to be able to load (plus they will be aligned at multiples of tileWidth, tileHeight, 1).
      TaggedImage img = tiffStorage_.getImageForDisplay(setup, (int)min[2], timepoint, level, min[0], min[1], tiffStorage_.getTileWidth(), tiffStorage_.getTileHeight());
      return new VolatileByteArray((byte[])img.pix, true);
   }

   @Override
   public VolatileByteArray emptyArray(final int[] dimensions) {
      int numEntities = 1;
      for (int i = 0; i < dimensions.length; ++i) {
         numEntities *= dimensions[ i];
      }
      if (theEmptyArray_.getCurrentStorageArray().length < numEntities) {
         theEmptyArray_ = new VolatileByteArray(numEntities, false);
      }
      return theEmptyArray_;
   }
   
}
