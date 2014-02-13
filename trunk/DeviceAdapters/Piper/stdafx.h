// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN		// Exclude rarely-used stuff from Windows headers
#endif

// Modify the following defines if you have to target a platform prior to the ones specified below.
// Refer to MSDN for the latest info on corresponding values for different platforms.
#ifndef WINVER				// Allow use of features specific to Windows XP or later.
#define WINVER 0x0501		// Change this to the appropriate value to target other versions of Windows.
#endif

#ifndef _WIN32_WINNT		// Allow use of features specific to Windows XP or later.                   
#define _WIN32_WINNT 0x0501	// Change this to the appropriate value to target other versions of Windows.
#endif						

#ifndef _WIN32_WINDOWS		// Allow use of features specific to Windows 98 or later.
#define _WIN32_WINDOWS 0x0410 // Change this to the appropriate value to target Windows Me or later.
#endif

#ifndef _WIN32_IE			// Allow use of features specific to IE 6.0 or later.
#define _WIN32_IE 0x0600	// Change this to the appropriate value to target other versions of IE.
#endif

#include <Windows.h>

#define snprintf _snprintf

#define __PIPER_API_EXPORT __declspec(dllimport)

#include "PiperApiErrors.h"
#include "PiperApiCbCmds.h"
#include "PiperApiLib.h"
#include "PiperApiUtils.h"


#include <vector>
#include <string>
#include <map>
#include <math.h>
#include <sstream>

#include "MMDevice.h"
#include "DeviceBase.h"
#include "ModuleInterface.h"
using namespace std;

extern LONG gs_nInstanceCount;
