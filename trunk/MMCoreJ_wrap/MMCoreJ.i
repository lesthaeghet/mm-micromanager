/////////////////////////////////////////////////////////////////////////////////
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
%feature("director") MMRunnable::run;

%include std_string.i
%include std_vector.i
%include std_map.i
%include std_pair.i
%include "typemaps.i"

// output arguments
%apply double &OUTPUT { double &x };
%apply double &OUTPUT { double &y };
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
   unsigned numComponents = (arg1)->getNumberOfComponents();
   
   if ((arg1)->getBytesPerPixel() == 1)
   {
      // create a new byte[] object in Java
      jbyteArray data = JCALL1(NewByteArray, jenv, lSize * numComponents);
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
      jshortArray data = JCALL1(NewShortArray, jenv, lSize * numComponents);
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
      // create a new int[] object in Java
      jintArray data = JCALL1(NewIntArray, jenv, lSize * numComponents);
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


%pragma(java) jniclasscode=%{
  static {
    try {
        System.loadLibrary("MMCoreJ_wrap");
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
#include "../MMCore/MMRunnable.h"
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

    %template(CharVector)   vector<char>;
    %template(LongVector)   vector<long>;
    %template(StrVector)    vector<string>;
    %template(pair_ss)      pair<string, string>;
    %template(StrMap)       map<string, string>;

}

%typemap(javaimports) MMRunnable %{
import java.lang.Runnable;
%}
%typemap(javainterfaces) MMRunnable %{Runnable%}

%include "../MMDevice/MMDeviceConstants.h"
%include "../MMCore/Error.h"
%include "../MMCore/Configuration.h"
%include "../MMCore/MMCore.h"
%include "../MMDevice/ImageMetadata.h"
%include "../MMCore/MMEventCallback.h"
%include "../MMCore/MMRunnable.h"
