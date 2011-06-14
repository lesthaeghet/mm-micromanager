//////////////////////////////////////////////////////////////////////////////////
// FILE:          MMCoreJ.i
// PROJECT:       Micro-Manager
// SUBSYSTEM:     MMCoreJ
//-----------------------------------------------------------------------------
// DESCRIPTION:   SWIG generator for the Java interface wrapper.
//              
// COPYRIGHT:     University of California, San Francisco, 2006,
//                All Rights reserved
//
// LICENSE:       This file is distributed under the "Lesser GPL" (LGPL) license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Nenad Amodaj, nenad@amodaj.com, 06/07/2005
// 
// CVS:           $Id$
//

%module (directors="1") MMCoreJ
%feature("director") MMEventCallback;

%include std_string.i
%include std_vector.i
%include std_map.i
%include std_pair.i
%include "typemaps.i"

// output arguments
%apply double &OUTPUT { double &x_stage };
%apply double &OUTPUT { double &y_stage };
%apply int &OUTPUT { int &x };
%apply int &OUTPUT { int &y };
%apply int &OUTPUT { int &xSize };
%apply int &OUTPUT { int &ySize };


// Java typemap
// change deafult SWIG mapping of unsigned char* return values
// to byte[]
//
// Assumes that class has the following method defined:
// long GetImageBufferSize()
//


%typemap(jni) unsigned char*        "jbyteArray"
%typemap(jtype) unsigned char*      "byte[]"
%typemap(jstype) unsigned char*     "byte[]"
%typemap(out) unsigned char*
{
   long lSize = (arg1)->getImageBufferSize();
   
   // create a new byte[] object in Java
   jbyteArray data = JCALL1(NewByteArray, jenv, lSize);
   
   // copy pixels from the image buffer
   JCALL4(SetByteArrayRegion, jenv, data, 0, lSize, (jbyte*)result);

   $result = data;
}

// Map input argument: java byte[] -> C++ unsigned char *
%typemap(in) unsigned char*
{
   // Assume that we are sending an image to an SLM device, one byte per pixel (monochrome grayscale).
   
   long expectedLength = (arg1)->getSLMWidth(arg2) * (arg1)->getSLMHeight(arg2);
   long receivedLength = JCALL1(GetArrayLength, jenv, $input);
   
   if (receivedLength != expectedLength && receivedLength != expectedLength*4)
   {
      jclass excep = jenv->FindClass("java/lang/Exception");
      if (excep)
         jenv->ThrowNew(excep, "Image dimensions are wrong for this SLM.");
      return;
   }
   
   $1 = (unsigned char *) JCALL2(GetByteArrayElements, jenv, $input, 0);
}

%typemap(freearg) unsigned char* {
   // Allow the Java byte array to be garbage collected.
   JCALL3(ReleaseByteArrayElements, jenv, $input, (jbyte *) $1, JNI_ABORT); // JNI_ABORT = Don't alter the original array.
}

// change Java wrapper output mapping for unsigned char*
%typemap(javaout) unsigned char* {
    return $jnicall;
 }

%typemap(javain) unsigned char* "$javainput" 


 
// Java typemap
// change deafult SWIG mapping of void* return values
// to return CObject containing array of pixel values
//
// Assumes that class has the following methods defined:
// unsigned GetImageWidth()
// unsigned GetImageHeight()
// unsigned GetImageDepth()
// unsigned GetNumberOfComponents


%typemap(jni) void*        "jobject"
%typemap(jtype) void*      "Object"
%typemap(jstype) void*     "Object"
%typemap(javaout) void* {
   return $jnicall;
}
%typemap(out) void*
{
   long lSize = (arg1)->getImageWidth() * (arg1)->getImageHeight();
   
   if ((arg1)->getBytesPerPixel() == 1)
   {
      // create a new byte[] object in Java
      jbyteArray data = JCALL1(NewByteArray, jenv, lSize);
      if (data == 0)
      {
         jclass excep = jenv->FindClass("java/lang/Exception");
         if (excep)
            jenv->ThrowNew(excep, "The system ran out of memory!");

         $result = 0;
         return $result;
      }
   
      // copy pixels from the image buffer
      JCALL4(SetByteArrayRegion, jenv, data, 0, lSize, (jbyte*)result);

      $result = data;
   }
   else if ((arg1)->getBytesPerPixel() == 2)
   {
      // create a new short[] object in Java
      jshortArray data = JCALL1(NewShortArray, jenv, lSize);
      if (data == 0)
      {
         jclass excep = jenv->FindClass("java/lang/Exception");
         if (excep)
            jenv->ThrowNew(excep, "The system ran out of memory!");
         $result = 0;
         return $result;
      }
  
      // copy pixels from the image buffer
      JCALL4(SetShortArrayRegion, jenv, data, 0, lSize, (jshort*)result);

      $result = data;
   }
   else if ((arg1)->getBytesPerPixel() == 4)
   {
      // create a new byte[] object in Java
      jbyteArray data = JCALL1(NewByteArray, jenv, lSize * 4);
      if (data == 0)
      {
         jclass excep = jenv->FindClass("java/lang/Exception");
         if (excep)
            jenv->ThrowNew(excep, "The system ran out of memory!");

         $result = 0;
         return $result;
      }
   
      // copy pixels from the image buffer
      JCALL4(SetByteArrayRegion, jenv, data, 0, lSize * 4, (jbyte*)result);

      $result = data;
   }
   else if ((arg1)->getBytesPerPixel() == 8)
   {
      // create a new short[] object in Java
      jshortArray data = JCALL1(NewShortArray, jenv, lSize * 4);
      if (data == 0)
      {
         jclass excep = jenv->FindClass("java/lang/Exception");
         if (excep)
            jenv->ThrowNew(excep, "The system ran out of memory!");
         $result = 0;
         return $result;
      }
  
      // copy pixels from the image buffer
      JCALL4(SetShortArrayRegion, jenv, data, 0, lSize * 4, (jshort*)result);

      $result = data;
   }

   else
   {
      // don't know how to map
      // TODO: throw exception?
      $result = 0;
   }
}

// Java typemap
// change deafult SWIG mapping of void* return values
// to return CObject containing array of pixel values
//
// Assumes that class has the following methods defined:
// unsigned GetImageWidth()
// unsigned GetImageHeight()
// unsigned GetImageDepth()
// unsigned GetNumberOfComponents


%typemap(jni) unsigned int* "jobject"
%typemap(jtype) unsigned int*      "Object"
%typemap(jstype) unsigned int*     "Object"
%typemap(javaout) unsigned int* {
   return $jnicall;
}
%typemap(out) unsigned int*
{
   long lSize = (arg1)->getImageWidth() * (arg1)->getImageHeight();
   unsigned numComponents = (arg1)->getNumberOfComponents();
   
   if ((arg1)->getBytesPerPixel() == 1 && numComponents == 4)
   {
      // assuming RGB32 format
      // create a new int[] object in Java
      jintArray data = JCALL1(NewIntArray, jenv, lSize);
      if (data == 0)
      {
         jclass excep = jenv->FindClass("java/lang/Exception");
         if (excep)
            jenv->ThrowNew(excep, "The system ran out of memory!");
         $result = 0;
         return $result;
      }
  
      // copy pixels from the image buffer
      JCALL4(SetIntArrayRegion, jenv, data, 0, lSize, (jint*)result);

      $result = data;
   }
   else
   {
      // don't know how to map
      // TODO: thow exception?
      $result = 0;
   }
}


%typemap(jni) imgRGB32 "jintArray"
%typemap(jtype) imgRGB32      "int[]"
%typemap(jstype) imgRGB32     "int[]"
%typemap(javain) imgRGB32     "$javainput"
%typemap(in) imgRGB32
{
   // Assume that we are sending an image to an SLM device, one int (four bytes) per pixel.
   
   if  ((arg1)->getSLMBytesPerPixel(arg2) != 4)
   {
      jclass excep = jenv->FindClass("java/lang/Exception");
      if (excep)
         jenv->ThrowNew(excep, "32-bit array received but not expected for this SLM.");
      return;
   }
   
   long expectedLength = (arg1)->getSLMWidth(arg2) * (arg1)->getSLMHeight(arg2);
   long receivedLength = JCALL1(GetArrayLength, jenv, (jarray) $input);
   
   if (receivedLength != expectedLength)
   {
      jclass excep = jenv->FindClass("java/lang/Exception");
      if (excep)
         jenv->ThrowNew(excep, "Image dimensions are wrong for this SLM.");
      return;
   }
   
   $1 = (imgRGB32) JCALL2(GetIntArrayElements, jenv, (jintArray) $input, 0);
}

%typemap(freearg) imgRGB32 {
   // Allow the Java int array to be garbage collected.
   JCALL3(ReleaseIntArrayElements, jenv, $input, (jint *) $1, JNI_ABORT); // JNI_ABORT = Don't alter the original array.
}


//
// Map all exception objects coming from C++ level
// generic Java Exception
//
%rename(eql) operator=;

// CMMError used by MMCore
%typemap(throws, throws="java.lang.Exception") CMMError {
   jclass excep = jenv->FindClass("java/lang/Exception");
   if (excep)
     jenv->ThrowNew(excep, $1.getMsg().c_str());
   return $null;
}

// MetadataKeyError used by Metadata class
%typemap(throws, throws="java.lang.Exception") MetadataKeyError {
   jclass excep = jenv->FindClass("java/lang/Exception");
   if (excep)
     jenv->ThrowNew(excep, $1.getMsg().c_str());
   return $null;
}

// MetadataIndexError used by Metadata class
%typemap(throws, throws="java.lang.Exception") MetadataIndexError {
   jclass excep = jenv->FindClass("java/lang/Exception");
   if (excep)
     jenv->ThrowNew(excep, $1.getMsg().c_str());
   return $null;
}

%typemap(javabase) CMMError "java.lang.Exception"
//%typemap(javabase) MetadataKeyError "java.lang.Exception"
//%typemap(javabase) MetadataIndexError "java.lang.Exception"

%typemap(javaimports) CMMCore %{
   import org.json.JSONObject;
%}

%typemap(javacode) CMMCore %{
   private JSONObject metadataToMap(Metadata md) {
      JSONObject tags = new JSONObject();
      for (String key:md.getFrameKeys())
         try {
            tags.put(key,md.get(key));
         } catch (Exception e) {} // Ignore	
      return tags;
    }

   private TaggedImage createTaggedImage(Object pixels, Metadata md) {
      return new TaggedImage(pixels, metadataToMap(md));	
   }

   public TaggedImage getLastTaggedImage() throws java.lang.Exception {
      Metadata md = new Metadata();
      Object pixels = getLastImageMD(md);
      return createTaggedImage(pixels, md);
   }
   
   public TaggedImage popNextTaggedImage() throws java.lang.Exception {
      Metadata md = new Metadata();
      Object pixels = popNextImageMD(md);
     return createTaggedImage(pixels, md);
   }

%}


%typemap(javacode) CMMError %{
   public String getMessage() {
      return getMsg();
   }
%}

%typemap(javacode) MetadataKeyError %{
   public String getMessage() {
      return getMsg();
   }
%}

%typemap(javacode) MetadataIndexError %{
   public String getMessage() {
      return getMsg();
   }
%}

%pragma(java) jniclassimports=%{
   import java.io.File;

   import java.util.ArrayList;
   import java.util.List;
%}

%pragma(java) jniclasscode=%{
  private static String getJarPath() {
    String classFile = "/mmcorej/CMMCore.class";
    String path = CMMCore.class.getResource(classFile).getFile();
    if (path.startsWith("file:"))
      path = path.substring(5);
    int bang = path.indexOf('!');
    if (bang > 0)
      path = path.substring(0, bang);
    return path;
  }

  private static String getPlatformString() {
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    return osName.startsWith("Mac") ? "macosx" :
      (osName.startsWith("Win") ? "win" : osName.toLowerCase()) +
      (osArch.indexOf("64") < 0 ? "32" : "64");
  }

  public static void loadLibrary(List<File> searchPaths, String name) {
    String libraryName = System.mapLibraryName(name);
    for (File path : searchPaths)
        if (new File(path, libraryName).exists()) {
            System.load(new File(path, libraryName).getAbsolutePath());
            return;
        }
    String paths = System.getProperty("java.library.path");
    if (paths != null)
        for (String path : paths.split(File.pathSeparator))
        if (new File(path, libraryName).exists()) {
            System.load(new File(path, libraryName).getAbsolutePath());
            return;
      }
    System.loadLibrary(name);
  }

  static {
    List<File> searchPaths = new ArrayList<File>();
    File directory = new File(getJarPath()).getParentFile();
    searchPaths.add(directory);
    directory = directory.getParentFile();
    searchPaths.add(directory);
    directory = new File(new File(directory, "mm"), getPlatformString());
    searchPaths.add(directory);

    try {
        loadLibrary(searchPaths, "MMCoreJ_wrap");
        /*
         * NOTE: it is positively _dangerous_ to rely on the
         * java.library.path!
         *
         * For one, The assumption that Java respects that setting
         * when changed after starting up Java is _wrong_, as
         * explained in this Sun Java-specific hack:
         * http://forums.sun.com/thread.jspa?threadID=707176
         *
         * But there is an even more serious reason:
         * getDeviceLibraries() is supposed to list the drivers
         * which can later be loaded. But they are loaded via
         * dlopen(), which does not respect the java.library.path
         * property at all!
         *
         * Indeed, it is very easy for the search paths of Java and
         * of dlopen() to become unsynchronized, and it does not
         * even take malice to do so!
         *
         * However, we cannot easily get the search path for
         * dlopen(), and we cannot modify it at runtime at all!
         *
         * In the interest of the law of the least surprise,
         * therefore, we parse java.library.path ourselves and
         * respect that search path both for the discovery of the
         * drivers as well as for the actual loading.
         *
         * As libMMCoreJ_wrap lives in the same directory as the
         * device drivers, let's add those paths to the search
         * paths, too.
         *
         * ======
         * 6/14/2011
         * I'm commenting out the java.library.path code because it is
         * causing us to experience DLL hell. Discussion welcome.
         * -- Arthur
         */
        for (File path : searchPaths)
          CMMCore.addSearchPath(path.getAbsolutePath());
//        String libPath = System.getProperty("java.library.path");
//        if (libPath != null)
//            for (String path : libPath.split(File.pathSeparator))
//                CMMCore.addSearchPath(path);
    } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library failed to load. \n" + e);
        // do not exit here, loadLibrary does not work on all platforms in the same way,
        // perhaps the library is already loaded.
        //System.exit(1);
    }
  }
%}

%{
#include "../MMDevice/MMDeviceConstants.h"
#include "../MMCore/Error.h"
#include "../MMCore/Configuration.h"
#include "../MMDevice/ImageMetadata.h"
#include "../MMCore/MMEventCallback.h"
#include "../MMCore/MMCore.h"
%}


// instantiate STL mappings

namespace std {

	%typemap(javaimports) vector<string> %{
		import java.lang.Iterable;
		import java.util.Iterator;
		import java.util.NoSuchElementException;
		import java.lang.UnsupportedOperationException;
	%}
	
	%typemap(javainterfaces) vector<string> %{ Iterable<String>%}
	
	%typemap(javacode) vector<string> %{
	
		public Iterator<String> iterator() {
			return new Iterator<String>() {
			
				private int i_=0;
			
				public boolean hasNext() {
					return (i_<size());
				}
				
				public String next() throws NoSuchElementException {
					if (hasNext()) {
						++i_;
						return get(i_-1);
					} else {
					throw new NoSuchElementException();
					}
				}
					
				public void remove() throws UnsupportedOperationException {
					throw new UnsupportedOperationException();
				}		
			};
		}
		
		public String[] toArray() {
			if (0==size())
				return new String[0];
			
			String strs[] = new String[(int) size()];
			for (int i=0; i<size(); ++i) {
				strs[i] = get(i);
			}
			return strs;
		}
		
	%}
	
   

	%typemap(javaimports) vector<bool> %{
		import java.lang.Iterable;
		import java.util.Iterator;
		import java.util.NoSuchElementException;
		import java.lang.UnsupportedOperationException;
	%}
	
	%typemap(javainterfaces) vector<bool> %{ Iterable<Boolean>%}
	
	%typemap(javacode) vector<bool> %{
	
		public Iterator<Boolean> iterator() {
			return new Iterator<Boolean>() {
			
				private int i_=0;
			
				public boolean hasNext() {
					return (i_<size());
				}
				
				public Boolean next() throws NoSuchElementException {
					if (hasNext()) {
						++i_;
						return get(i_-1);
					} else {
					throw new NoSuchElementException();
					}
				}
					
				public void remove() throws UnsupportedOperationException {
					throw new UnsupportedOperationException();
				}		
			};
		}
		
		public Boolean[] toArray() {
			if (0==size())
				return new Boolean[0];
			
			Boolean strs[] = new Boolean[(int) size()];
			for (int i=0; i<size(); ++i) {
				strs[i] = get(i);
			}
			return strs;
		}
		
	%}
	





    %template(CharVector)   vector<char>;
    %template(LongVector)   vector<long>;
    %template(DoubleVector) vector<double>;
    %template(StrVector)    vector<string>;
    %template(BooleanVector)    vector<bool>;
    %template(pair_ss)      pair<string, string>;
    %template(StrMap)       map<string, string>;





}


%include "../MMDevice/MMDeviceConstants.h"
%include "../MMCore/Error.h"
%include "../MMCore/Configuration.h"
%include "../MMCore/MMCore.h"
%include "../MMDevice/ImageMetadata.h"
%include "../MMCore/MMEventCallback.h"


