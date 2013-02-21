///////////////////////////////////////////////////////////////////////////////
// MODULE:			Debayer.h
// SYSTEM:        ImageBase subsystem
// AUTHOR:			Jennifer West, jennifer_west@umanitoba.ca,
//                Nenad Amodaj, nenad@amodaj.com
//
// DESCRIPTION:	Debayer algorithms, adapted from:
//                http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html
//                
//
// COPYRIGHT:     Jennifer West (University of Manitoba),
//                Exploratorium http://www.exploratorium.edu
//
// LICENSE:       This file is free for use, modification and distribution and
//                is distributed under terms specified in the BSD license
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
///////////////////////////////////////////////////////////////////////////////

#if !defined(_DEBAYER_)
#define _DEBAYER_

#include "ImgBuffer.h"

/**
 * Utility class to build color image from the Bayer grayscale image
 * Based on the Debayer_Image plugin for ImageJ, by Jennifer West, University of Manitoba
 */
class Debayer
{
public:
   Debayer();
   ~Debayer();

   int Process(ImgBuffer& out, const ImgBuffer& in, int bitDepth);
   int Process(ImgBuffer& out, const unsigned short* in, int width, int height, int bitDepth);

private:
   void ReplicateDecode(const unsigned short* input, int* out, int width, int height, int bitDepth, int rowOrder);
   void SmoothDecode(const unsigned short* input, int* output, int width, int height, int bitDepth, int rowOrder);
   int Convert(const unsigned short* input, int* output, int width, int height, int bitDepth, int rowOrder, int algorithm);
   unsigned short GetPixel(const unsigned short* v, int x, int y, int width, int height);
   void SetPixel(std::vector<unsigned short>& v, unsigned short val, int x, int y, int width, int height);

   std::vector<unsigned short> r;
   std::vector<unsigned short> g;
   std::vector<unsigned short> b;
};

#endif // !defined(_DEBAYER_)
