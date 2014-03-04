// Micro-Manager IIDC Device Adapter
//
// AUTHOR:        Mark A. Tsuchida
//
// COPYRIGHT:     University of California, San Francisco, 2014
//
// LICENSE:       This file is distributed under the BSD license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

#include "IIDCVideoMode.h"

#include "IIDCError.h"

#include <boost/lexical_cast.hpp>


namespace IIDC {

PixelFormat
VideoMode::GetPixelFormat() const
{
   switch (libdc1394coding_)
   {
      case DC1394_COLOR_CODING_MONO8:
         return PixelFormatGray8;
         break;
      case DC1394_COLOR_CODING_MONO16:
         return PixelFormatGray16;
         break;
      default:
         return PixelFormatUnsupported;
         break;
   }
}


ConventionalVideoMode::ConventionalVideoMode(dc1394camera_t* libdc1394camera,
      dc1394video_mode_t libdc1394mode) :
   VideoMode(libdc1394mode)
{
   uint32_t width, height;
   dc1394error_t err;
   err = dc1394_get_image_size_from_video_mode(libdc1394camera, libdc1394mode, &width, &height);
   if (err != DC1394_SUCCESS)
      throw Error(err, "Cannot get image size from video mode");
   SetImageSize(width, height);
   
   dc1394color_coding_t libdc1394coding;
   err = dc1394_get_color_coding_from_video_mode(libdc1394camera, libdc1394mode, &libdc1394coding);
   if (err != DC1394_SUCCESS)
      throw Error(err, "Cannot get color coding from video mode");
   SetLibDC1394Coding(libdc1394coding);
}


Format7VideoMode::Format7VideoMode(dc1394camera_t* libdc1394camera,
      dc1394video_mode_t libdc1394mode, dc1394color_coding_t libdc1394coding) :
   VideoMode(libdc1394mode)
{
   uint32_t width, height;
   dc1394error_t err;
   err = dc1394_format7_get_max_image_size(libdc1394camera, libdc1394mode, &width, &height);
   if (err != DC1394_SUCCESS)
      throw Error(err, "Cannot get maximum image size for format 7 video mode");
   SetImageSize(width, height);

   SetLibDC1394Coding(libdc1394coding);
}


std::string
ConventionalVideoMode::ToString() const
{
   dc1394video_mode_t libdc1394mode = GetLibDC1394Mode();
   int formatNr = -1, modeNr = -1;
   if (libdc1394mode >= DC1394_VIDEO_MODE_160x120_YUV444 &&
         libdc1394mode <= DC1394_VIDEO_MODE_640x480_MONO16)
   {
      formatNr = 0;
      modeNr = libdc1394mode - DC1394_VIDEO_MODE_160x120_YUV444;
   }
   else if (libdc1394mode >= DC1394_VIDEO_MODE_800x600_YUV422 &&
         libdc1394mode <= DC1394_VIDEO_MODE_1024x768_MONO16)
   {
      formatNr = 1;
      modeNr = libdc1394mode - DC1394_VIDEO_MODE_800x600_YUV422;
   }
   else if (libdc1394mode >= DC1394_VIDEO_MODE_1280x960_YUV422 &&
         libdc1394mode <= DC1394_VIDEO_MODE_1600x1200_MONO16)
   {
      formatNr = 2;
      modeNr = libdc1394mode - DC1394_VIDEO_MODE_1280x960_YUV422;
   }
   std::string formatModeStr = (formatNr >= 0 && modeNr >= 0) ?
      "[f" + boost::lexical_cast<std::string>(formatNr) + "-m" +
      boost::lexical_cast<std::string>(modeNr) + "]" :
      "[?-?]";

   std::string colorCodingName;
   switch (GetLibDC1394Coding())
   {
      case DC1394_COLOR_CODING_MONO8:
         colorCodingName = "Mono8";
         break;
      case DC1394_COLOR_CODING_MONO16:
         colorCodingName = "Mono16";
         break;
      default:
         colorCodingName = "(unsupported)";
         break;
   }

   return formatModeStr + " " +
      boost::lexical_cast<std::string>(GetMaxWidth()) + "x" +
      boost::lexical_cast<std::string>(GetMaxHeight()) + " " +
      colorCodingName;
}


std::string
Format7VideoMode::ToString() const
{
   int modeNr = static_cast<int>(GetLibDC1394Mode()) - DC1394_VIDEO_MODE_FORMAT7_MIN;

   std::string colorCodingName;
   switch (GetLibDC1394Coding())
   {
      case DC1394_COLOR_CODING_MONO8:
         colorCodingName = "Mono8";
         break;
      case DC1394_COLOR_CODING_MONO16:
         colorCodingName = "Mono16";
         break;
      default:
         colorCodingName = "(unsupported)";
         break;
   }

   return "[f7-m" + boost::lexical_cast<std::string>(modeNr) + "] " +
      boost::lexical_cast<std::string>(GetMaxWidth()) + "x" +
      boost::lexical_cast<std::string>(GetMaxHeight()) + " " +
      colorCodingName;
}

} // namespace
