/* -*- C++ -*-
 * File: libraw_const.h
 * Copyright 2008-2010 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  8 , 2008
 *
 * LibRaw error codes
LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

 */

#ifndef _LIBRAW_ERRORS_H
#define _LIBRAW_ERRORS_H

#define LIBRAW_DEFAULT_ADJUST_MAXIMUM_THRESHOLD 0.75
#define LIBRAW_DEFAULT_AUTO_BRIGHTNESS_THRESHOLD 0.01

enum LibRaw_constructor_flags
{
    LIBRAW_OPTIONS_NONE         =0,
    LIBRAW_OPIONS_NO_MEMERR_CALLBACK=1,
    LIBRAW_OPIONS_NO_DATAERR_CALLBACK=1<<1
};

enum LibRaw_warnings
{
    LIBRAW_WARN_NONE            =0,
    LIBRAW_WARN_FOVEON_NOMATRIX =1,
    LIBRAW_WARN_FOVEON_INVALIDWB =1<<1,
    LIBRAW_WARN_BAD_CAMERA_WB   =1<<2,
    LIBRAW_WARN_NO_METADATA     =1<<3,
    LIBRAW_WARN_NO_JPEGLIB     = 1<<4,
    LIBRAW_WARN_NO_EMBEDDED_PROFILE = 1<<5,
    LIBRAW_WARN_NO_INPUT_PROFILE = 1<<6,
    LIBRAW_WARN_BAD_OUTPUT_PROFILE= 1<<7,
    LIBRAW_WARN_NO_BADPIXELMAP=1<<8,
    LIBRAW_WARN_BAD_DARKFRAME_FILE=1<<9,
    LIBRAW_WARN_BAD_DARKFRAME_DIM=1<<10
};

enum LibRaw_exceptions
{
    LIBRAW_EXCEPTION_NONE       =0,
    LIBRAW_EXCEPTION_ALLOC      =1,
    LIBRAW_EXCEPTION_DECODE_RAW =2,
    LIBRAW_EXCEPTION_DECODE_JPEG=3,
    LIBRAW_EXCEPTION_IO_EOF     =4,
    LIBRAW_EXCEPTION_IO_CORRUPT =5,
    LIBRAW_EXCEPTION_CANCELLED_BY_CALLBACK=6,
    LIBRAW_EXCEPTION_BAD_CROP =7,
    LIBRAW_EXCEPTION_IO_BADFILE =8
};


enum LibRaw_colorstate
{
    LIBRAW_COLORSTATE_UNKNOWN   =0,
    LIBRAW_COLORSTATE_INIT      =1,
    LIBRAW_COLORSTATE_CONST     =2,
    LIBRAW_COLORSTATE_LOADED    =3,
    LIBRAW_COLORSTATE_CALCULATED=4,
    LIBRAW_COLORSTATE_RESERVED1 =5,
    LIBRAW_COLORSTATE_RESERVED2 =6,
    LIBRAW_COLORSTATE_RESERVED3 =7
};

enum LibRaw_filtering
{
    LIBRAW_FILTERING_DEFAULT            =0,
    LIBRAW_FILTERING_NOZEROES           =1,  
    LIBRAW_FILTERING_DEPRECATED1        =2, 
    LIBRAW_FILTERING_NORAWCURVE         =4,
    LIBRAW_FILTERING_NONE               =7,
    LIBRAW_FILTERING_LIBRAWOWN          =(8 | LIBRAW_FILTERING_NONE),
    LIBRAW_FILTERING_AUTOMATIC_BIT      =16,
    LIBRAW_FILTERING_AUTOMATIC          = (LIBRAW_FILTERING_LIBRAWOWN | LIBRAW_FILTERING_AUTOMATIC_BIT)
};


enum LibRaw_progress
{
    LIBRAW_PROGRESS_START               = 0,
    LIBRAW_PROGRESS_OPEN                = 1,
    LIBRAW_PROGRESS_IDENTIFY            = 1<<1,
    LIBRAW_PROGRESS_SIZE_ADJUST         = 1<<2,
    LIBRAW_PROGRESS_LOAD_RAW            = 1<<3,
    LIBRAW_PROGRESS_REMOVE_ZEROES       = 1<<4,
    LIBRAW_PROGRESS_BAD_PIXELS          = 1<<5,
    LIBRAW_PROGRESS_DARK_FRAME          = 1<<6,
    LIBRAW_PROGRESS_FOVEON_INTERPOLATE  = 1<<7,
    LIBRAW_PROGRESS_SCALE_COLORS        = 1<<8,
    LIBRAW_PROGRESS_PRE_INTERPOLATE     = 1<<9,
    LIBRAW_PROGRESS_INTERPOLATE         = 1<<10,
    LIBRAW_PROGRESS_MIX_GREEN           = 1<<11,
    LIBRAW_PROGRESS_MEDIAN_FILTER       = 1<<12,
    LIBRAW_PROGRESS_HIGHLIGHTS          = 1<<13,
    LIBRAW_PROGRESS_FUJI_ROTATE         = 1<<14,
    LIBRAW_PROGRESS_FLIP                = 1<<15,
    LIBRAW_PROGRESS_APPLY_PROFILE       = 1<<16,
    LIBRAW_PROGRESS_CONVERT_RGB         = 1<<17,
    LIBRAW_PROGRESS_STRETCH             = 1<<18,
/* reserved */
    LIBRAW_PROGRESS_STAGE19             = 1<<19,
    LIBRAW_PROGRESS_STAGE20             = 1<<20,
    LIBRAW_PROGRESS_STAGE21             = 1<<21,
    LIBRAW_PROGRESS_STAGE22             = 1<<22,
    LIBRAW_PROGRESS_STAGE23             = 1<<23,
    LIBRAW_PROGRESS_STAGE24             = 1<<24,
    LIBRAW_PROGRESS_STAGE25             = 1<<25,
    LIBRAW_PROGRESS_STAGE26             = 1<<26,
    LIBRAW_PROGRESS_STAGE27             = 1<<27,

    LIBRAW_PROGRESS_THUMB_LOAD          = 1<<28,
    LIBRAW_PROGRESS_TRESERVED1          = 1<<29,
    LIBRAW_PROGRESS_TRESERVED2          = 1<<30,
    LIBRAW_PROGRESS_TRESERVED3          = 1<<31
};
#define LIBRAW_PROGRESS_THUMB_MASK 0x0fffffff

enum LibRaw_errors
{
    LIBRAW_SUCCESS = 0,
    LIBRAW_UNSPECIFIED_ERROR=-1,
    LIBRAW_FILE_UNSUPPORTED = -2,
    LIBRAW_REQUEST_FOR_NONEXISTENT_IMAGE=-3,
    LIBRAW_OUT_OF_ORDER_CALL=-4,
    LIBRAW_NO_THUMBNAIL=-5,
    LIBRAW_UNSUPPORTED_THUMBNAIL=-6,
    LIBRAW_CANNOT_ADDMASK=-7,
    LIBRAW_UNSUFFICIENT_MEMORY=-100007,
    LIBRAW_DATA_ERROR=-100008,
    LIBRAW_IO_ERROR=-100009,
    LIBRAW_CANCELLED_BY_CALLBACK=-100010,
    LIBRAW_BAD_CROP=-100011
};

#define LIBRAW_FATAL_ERROR(ec) ((ec)<-100000)

enum LibRaw_thumbnail_formats
{
    LIBRAW_THUMBNAIL_UNKNOWN=0,
    LIBRAW_THUMBNAIL_JPEG=1,
    LIBRAW_THUMBNAIL_BITMAP=2,
    LIBRAW_THUMBNAIL_LAYER=4,
    LIBRAW_THUMBNAIL_ROLLEI=5
};

enum LibRaw_image_formats
{
    LIBRAW_IMAGE_JPEG=1,
    LIBRAW_IMAGE_BITMAP=2
};

#endif
