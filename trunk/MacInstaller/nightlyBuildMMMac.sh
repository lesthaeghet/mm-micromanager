# This script builds binaries for three architectures (ppc, i386, and x86_64)
# from three repositories (which should be checked out already).
# It assumes the following directory structure:
# $REPOSITORYROOT - micro-manager1.4
#                 - micro-manager1.4-ppc
#                 - micro-manager1.4-i386
#                 - micro-manager1.4-x86_64
# contents of $BUILDDIR will be removed!!!

# Use "-f" to build a release version, no option will do nightly build (only difference is in the name of the output)
# Use "-n" to avoid a make clean, i.e. do an incremental only
while getopts ":fn" optname
do
   case "$optname" in
      "f")
      echo "Executing full build"
      FULL="full"
      ;;
      "n")
      echo "Incremental build"
      NOCLEAN="noclean"
      ;;
   esac
done


REPOSITORYROOT=/Users/MM/svn
BUILDDIR=/Users/MM/MMBuild
if [ "$FULL" != "full" ]; then
   UPLOADPLACE=valelab2.ucsf.edu:public_html/nightlyBuilds/1.4/Mac/
else
   UPLOADPLACE=valelab2.ucsf.edu:public_html/builds/1.4/Mac/
fi

# No edits should be needed below this line

TARGET=$BUILDDIR/Micro-Manager1.4

PPC=$BUILDDIR/Micro-Manager1.4-ppc
I386=$BUILDDIR/Micro-Manager1.4-i386
X86_64=$BUILDDIR/Micro-Manager1.4-x86_64

REPOSITORY=$REPOSITORYROOT/micromanager1.4
RDPARTYPUBLIC=$REPOSITORYROOT/3rdpartypublic
RPPC=$REPOSITORYROOT/micromanager1.4-ppc
RI386=$REPOSITORYROOT/micromanager1.4-i386
RX86_64=$REPOSITORYROOT/micromanager1.4-x86_64
CLASSEXT=$REPOSITORY/../3rdpartypublic/classext

test -d $BUILDDIR && rm -rf $BUILDDIR
mkdir $BUILDDIR
mkdir $TARGET

BUILDLOG=$REPOSITORY/MacInstaller/buildlog.txt
test -f $BUILDLOG && rm $BUILDLOG
BUILDLOGUPLOADPLACE=valelab2.ucsf.edu:/home/MM

cd $RDPARTYPUBLIC
svn update
cd $REPOSITORY
svn update --accept theirs-conflict
cd $REPOSITORY/SecretDeviceAdapters
svn update --accept theirs-conflict
cd $RPPC
svn update --accept theirs-conflict
cd $RPPC/SecretDeviceAdapters
svn update --accept theirs-conflict
cd $RI386
svn update --accept theirs-conflict
cd $RI386/SecretDeviceAdapters
svn update --accept theirs-conflict
cd $RX86_64
svn update --accept theirs-conflict
cd $RX86_64/SecretDeviceAdapters
svn update --accept theirs-conflict
cd $RPPC

cp -r $REPOSITORY/bindist/any-platform/* $TARGET
cp -r $REPOSITORY/bindist/MacOSX/* $TARGET
find $TARGET -name '.svn' -exec rm -fr {} \;
cp $CLASSEXT/ij.jar $TARGET
MMPLUGINDIR=$TARGET/plugins/Micro-Manager
test -d $MMPLUGINDIR && rm -rf $MMPLUGINDIR
mkdir $MMPLUGINDIR
cp $CLASSEXT/*.jar $MMPLUGINDIR
rm $TARGET/plugins/ij.jar || echo "No problem"
cp -r $TARGET $PPC
cp -r $TARGET $I386
cp -r $TARGET $X86_64


# build PPC
cd $RPPC
# set version variable and change version in java source code to include build date stamp
VERSION=`cat version.txt`
# nightly build
if [ "$FULL" != "full" ]; then
   VERSION=$VERSION-`date "+%Y%m%d"`
fi
echo $VERSION
echo $VERSION >> $BUILDLOG
sed -e "s/@VERSION_STRING@/$VERSION/" buildscripts/MMVersion.java.in > mmstudio/src/org/micromanager/MMVersion.java || exit


./mmUnixBuild.sh || exit
MACOSX_DEPLOYMENT_TARGET=10.4
./configure --with-imagej=$PPC --enable-python --enable-arch=ppc --with-boost=/usr/local/ppc --prefix=/usr/local/ppc CXX="g++-4.0" CFLAGS="-g -O2 -Wall -mmacosx-version-min=10.4 -arch ppc" CXXFLAGS="-g -O2 -mmacosx-version-min=10.4 -isysroot /Developer/SDKs/MacOSX10.4u.sdk -arch ppc"  --disable-dependency-tracking PKG_CONFIG_LIBDIR="/usr/local/ppc/lib/pkgconfig/" >> $BUILDLOG || exit
if [ "$NOCLEAN" != "noclean" ]; then
   make clean || exit
fi
make 2>> $BUILDLOG || exit
make install || exit

# build i386
cd $RI386
./mmUnixBuild.sh || exit
MACOSX_DEPLOYMENT_TARGET=10.4
./configure --with-imagej=$I386 --enable-python --enable-arch=i386 --with-boost=/usr/local/i386 --with-opencv=/usr/local/i386 --prefix=/usr/local/i386 CFLAGS="-g -O2 -Wall -mmacosx-version-min=10.4 -arch i386" CXX="g++-4.0" CXXFLAGS="-g -O2 -mmacosx-version-min=10.4 -isysroot /Developer/SDKs/MacOSX10.4u.sdk -arch i386"  --disable-dependency-tracking PKG_CONFIG_LIBDIR="/usr/local/i386/lib/pkgconfig/" >> $BUILDLOG || exit
if [ "$NOCLEAN" != "noclean" ]; then
   make clean || exit
fi
make 2>> $BUILDLOG || exit
make install || exit

# build x86_64
cd $RX86_64
./mmUnixBuild.sh || exit
export MACOSX_DEPLOYMENT_TARGET=10.5
./configure --with-imagej=$X86_64 --enable-arch=x86_64 --with-boost=/usr/local/x86_64 --with-opencv=/usr/local/x86_64 --prefix=/usr/local/x86_64 CXX="g++-4.2" CFLAGS="-g -O2 -Wall -mmacosx-version-min=10.5 -arch x86_64" CXXFLAGS="-g -O2 -mmacosx-version-min=10.5 -isysroot /Developer/SDKs/MacOSX10.5.sdk -arch x86_64" --disable-dependency-tracking PKG_CONFIG_LIBDIR="/usr/local/x86_64/lib/pkgconfig/" >> $BUILDLOG || exit
if [ "$NOCLEAN" != "noclean" ]; then
   make clean || exit
fi
make 2>> $BUILDLOG || exit
make install || exit

# Use lipo to make Universal Binaries
lipo -create $PPC/libMMCoreJ_wrap.jnilib $I386/libMMCoreJ_wrap.jnilib $X86_64/libMMCoreJ_wrap.jnilib -o $TARGET/libMMCoreJ_wrap.jnilib
lipo -create $PPC/libNativeGUI.jnilib $I386/libNativeGUI.jnilib $X86_64/libNativeGUI.jnilib -o $TARGET/libNativeGUI.jnilib
#strip -X -S $TARGET/libMMCoreJ_wrap.jnilib
cd $PPC
FILES=libmmgr*
for f in $FILES; do lipo -create $PPC/$f $I386/$f $X86_64/$f -o $TARGET/$f; done

# need to do files absent from ppc seperately
lipo -create $I386/libmmgr_dal_OpenCVgrabber  $X86_64/libmmgr_dal_OpenCVgrabber -o $TARGET/libmmgr_dal_OpenCVgrabber
lipo -create $I386/libmmgr_dal_Micropix  $X86_64/libmmgr_dal_Micropix -o $TARGET/libmmgr_dal_Micropix
lipo -create $I386/libmmgr_dal_GPhoto  $X86_64/libmmgr_dal_GPhoto -o $TARGET/libmmgr_dal_GPhoto
lipo -create $I386/libmmgr_dal_Micropix  $X86_64/libmmgr_dal_Micropix -o $TARGET/libmmgr_dal_Micropix

# Build Gphoto on I386 and X86_64 but not on PPC since p2p does not build there
mkdir $TARGET/libgphoto2
GPHOTODIR=libgphoto2/libgphoto2
mkdir $TARGET/$GPHOTODIR
cd $I386/$GPHOTODIR
GPHOTOFILES=*.so
for g in $GPHOTOFILES; do lipo -create $I386/$GPHOTODIR/$g $X86_64/$GPHOTODIR/$g -o $TARGET/$GPHOTODIR/$g; done
GPHOTOPORTDIR=libgphoto2/libgphoto2_port
mkdir $TARGET/$GPHOTOPORTDIR
cd $I386/$GPHOTOPORTDIR
GPHOTOPORTFILES=*.so
for p in $GPHOTOPORTFILES; do lipo -create $I386/$GPHOTOPORTDIR/$p $X86_64/$GPHOTOPORTDIR/$p -o $TARGET/$GPHOTOPORTDIR/$p; done
cd $I386/libgphoto2
GPHOTOLIBS=*.dylib
for l in $GPHOTOLIBS; do lipo -create $I386/libgphoto2/$l $X86_64/libgphoto2/$l -o $TARGET/libgphoto2/$l; done
cp -r $I386/libgphoto2//KillPtpCamera.app $TARGET/libgphoto2/


#for f in $FILES; do strip -X -S $TARGET/$f; done
#lipo -create $PPC/_MMCorePy.so $I386/_MMCorePy.so $X86_64/_MMCorePy.so -o $TARGET/_MMCorePy.so


# copy installed files 
cp -r $PPC/plugins/Micro-Manager $TARGET/plugins/
cp $PPC/*.cfg $TARGET/
cp $PPC/_MMCorePy.so $TARGET/
cp -r $PPC/mmplugins $TARGET/
cp -r $PPC/mmautofocus $TARGET/ 
cp -r $PPC/scripts $TARGET/

# Build devicelist using 32-bit JVM (Set using /Applications/Utilities/Java Preferences)
#cd $I386
#java -cp plugins/Micro-Manager/MMJ_.jar:plugins/Micro-Manager/MMCoreJ.jar DeviceListBuilder notDeviceDiscoveryEnabled
#cp $I386/MMDeviceList.txt $TARGET/MMDeviceList.txt
#java -cp plugins/Micro-Manager/MMJ_.jar:plugins/Micro-Manager/MMCoreJ.jar DeviceListBuilder deviceDiscoveryEnabled
#cp $I386/MMDeviceListPrime.txt $TARGET/MMDeviceListPrime.txt

#Install Python, only I386
cp $RI386/bin/MMCorePy.py $TARGET/
cp $RI386/bin/_MMCorePy.so $TARGET/

# copy over scripts from repository to installation
cp -f $REPOSITORY/scripts/*.bsh $TARGET/scripts/

cd $REPOSITORY/MacInstaller
if [ "$FULL" != "full" ]; then
   ./makemacdisk.sh -d -s $TARGET
else
   ./makemacdisk.sh -r -s $TARGET
fi


# upload to mightly build server:
scp Micro-Manager$VERSION.dmg $UPLOADPLACE

# upload the Mac build log
scp $BUILDLOG $BUILDLOGUPLOADPLACE/MacBuildLog.txt

# build and upload documentation
cd $RI386
make dox
scp -r doxygen/out/* valelab2.ucsf.edu:public_html/doc/
cd swig-doc-converter
./convert
scp -r javadoc/* valelab2.ucsf.edu:public_html/doc/mmcorej/
cd ../mmstudio
make javadoc
scp -r doc/* valelab2.ucsf.edu:public_html/doc/mmstudio/
cd ..

# tag the repository
if [ "$FULL" == "full" ]; then
   svn mkdir -m "Making tag $VERSION" https://valelab2.ucsf.edu/svn/micromanager2/tags/$VERSION
   svn copy https://valelab2.ucsf.edu/svn/micromanager2/trunk/ https://valelab2.ucsf.edu/svn/micromanager2/tags/$VERSION -m "Tagging version $VERSION"
fi
