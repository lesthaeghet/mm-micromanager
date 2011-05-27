///////////////////////////////////////////////////////////////////////////////
// FILE:          Apogee.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Apogee camera module
//                
// AUTHOR:        Bob Dougherty <bob@vischeck.com> 
// COPYRIGHT:     Bob Dougherty and Apogee Instruments, California, 2008
// LICENSE:       This file is distributed under the LGPL license.
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
//
// TO DO: 
// 1. When anisotropic binning is activated in the property browser, change the 
//    'binning' pull-down to reflect this. Maybe add a new allowed value on-the-fly?
// 2. Implement sequence mode for fast image acquisition.
//
//
// CVS:           $Id: $
//


#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#define snprintf _snprintf 
#endif

#include "Apogee.h"
#include "../../MMDevice/ModuleInterface.h"
#pragma warning(disable : 4996) // disable warning for deperecated CRT functions on Windows 

#include <string>
#include <sstream>
#include <cmath>

using namespace std;

// constants for naming pixel types (allowed values of the "PixelType" property)
const char* g_PixelType_8bit = "8bit";
const char* g_PixelType_12bit = "12bit";
const char* g_PixelType_16bit = "16bit";

// TODO: linux entry code?

// Windows DLL entry routine
#ifdef WIN32
BOOL APIENTRY DllMain( HANDLE /*hModule*/, 
                      DWORD  ul_reason_for_call, 
                      LPVOID /*lpReserved*/
                      )
{
    switch(ul_reason_for_call)
    {
        case DLL_PROCESS_ATTACH:
        case DLL_THREAD_ATTACH:
        case DLL_THREAD_DETACH:
        case DLL_PROCESS_DETACH:
            break;
    }
    return TRUE;
}
#endif


///////////////////////////////////////////////////////////////////////////////
// Exported MMDevice API
///////////////////////////////////////////////////////////////////////////////
MODULE_API void InitializeModuleData()
{
    AddAvailableDeviceName(g_CameraDeviceName,    "Apogee Alta camera adapter");
}

MODULE_API MM::Device* CreateDevice(const char* deviceName)
{
    if(deviceName == 0)
        return 0;
    
    // decide which device class to create based on the deviceName parameter
    if(strcmp(deviceName, g_CameraDeviceName) == 0){
        // create camera
        return new CApogeeCamera();
    }
    
    // ...supplied name not recognized
    return 0;
}

MODULE_API void DeleteDevice(MM::Device* pDevice)
{
    delete pDevice;
}

///////////////////////////////////////////////////////////////////////////////
// CApogeeCamera implementation
// ~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
     * CApogeeCamera constructor.
     * Setup variables and device properties needed before intialization (if any).
     * All other properties will be created in the Initialize() method.
     *
     * As a general guideline Micro-Manager devices do not access hardware in the
     * the constructor. We should do as little as possible in the constructor and
     * perform most of the initialization in the Initialize() method.
     */
CApogeeCamera::CApogeeCamera() : 
    m_bInitialized(false),
    m_bBusy(false),
    m_nLightImgMode(true),
    pixelDepth_(1),
    m_dExposure(100.0),
    m_nInterfaceType(999),
    m_nCamIdOne(-1),
    m_nCamIdTwo(0),
	 m_sequenceCount(0)
{
    // call the base class method to set-up default error codes/messages
    InitializeDefaultErrorMessages();
    
    // Pre-initialization stuff goes here- things that we need to set up before
    // we can even access the hardware. Here we set up the camera interface
    // (0=network vs. 1=USB). If we want to use the Alta 'Discover' dialog, we
    // set m_nInterfaceType to an invalid value (eg. 999) and/or set the 
    // m_nCamIdOne to a negative value.
    CPropertyAction *pAct = new CPropertyAction (this, &CApogeeCamera::OnCameraInterface);
    int nRet = CreateProperty("Interface", "Discover", MM::String, false, pAct, true);
    assert(nRet == DEVICE_OK);
    vector<string> interfaceValues;
    interfaceValues.push_back("Discover");
    interfaceValues.push_back("USB");
    interfaceValues.push_back("Net");
    nRet = SetAllowedValues("Interface", interfaceValues);
    assert(nRet == DEVICE_OK);
    
    pAct = new CPropertyAction (this, &CApogeeCamera::OnCameraIdOne);
    nRet = CreateProperty("CameraIdOne", "-1", MM::Integer, false, pAct, true);
    assert(nRet == DEVICE_OK);
    
    pAct = new CPropertyAction (this, &CApogeeCamera::OnCameraIdTwo);
    nRet = CreateProperty("CameraIdTwo", "0", MM::Integer, false, pAct, true);
    assert(nRet == DEVICE_OK);

	// Create sequence thread
	m_acqSequenceThread = new AcqSequenceThread(this);

}

/**
 * CApogeeCamera destructor.
 * Within the Micro-Manager system, Shutdown() will be always called before 
 * the destructor. But we try to make sure that all resources are properly 
 * released even if Shutdown() was not called.
 */
CApogeeCamera::~CApogeeCamera()
{
   delete m_acqSequenceThread;
}

/**
 * Intializes the hardware.
 * (Required by the MM::Device API.)
 * Typically we access and initialize hardware at this point.
 * Device properties are typically created here as well, except
 * the ones we need to use for defining initialization parameters.
 * Such pre-initialization properties are created in the constructor.
 * (This device does not have any pre-initialization properties)
 */
int CApogeeCamera::Initialize()
{
    if(m_bInitialized)
        return DEVICE_OK;
    
    ICamDiscoverPtr Discover;       // Discovery interface
    HRESULT hr;                     // Return code 
    
    //
    // Instantiate the vendor's camera class 
    //
    
    CoInitialize( NULL );           // Initialize COM library
    
    // Create the ICamera2 object
    hr = AltaCamera.CreateInstance( __uuidof( Camera2 ) );
    if(SUCCEEDED(hr)){
        printf("Successfully created the ICamera2 object\n" );
    }else{
        printf("Failed to create the ICamera2 object\n" );
        CoUninitialize();           // Close the COM library
        return DEVICE_ERR;
    }

    // Try to open the pre-configured device
	if(m_nInterfaceType!=999 && m_nCamIdOne!=-1){
        // Initialize camera using the ICamDiscover properties
        hr = AltaCamera->Init((Apn_Interface)m_nInterfaceType, m_nCamIdOne, m_nCamIdTwo, 0x0 );
        if(!SUCCEEDED(hr)){
            assert(!"Failed to connect to pre-configured camera" );
            m_nCamIdOne = -1;
        }
    }
    
    if(m_nInterfaceType==999 || m_nCamIdOne==-1){
		// Create the ICamDiscover object
		hr = Discover.CreateInstance( __uuidof( CamDiscover ) );
		if(!SUCCEEDED(hr)){
			printf( "Failed to create the ICamDiscover object\n" );
			AltaCamera = NULL;      // Release ICamera2 COM object
			CoUninitialize();       // Close the COM library
			return DEVICE_ERR;
		}
        // Set the checkboxes to default to searching both USB and 
        // ethernet interfaces for Alta cameras
        if(m_nInterfaceType==0){
            Discover->DlgCheckEthernet = true;
            Discover->DlgCheckUsb = false;
        }else if(m_nInterfaceType==1){
            Discover->DlgCheckEthernet = false;
            Discover->DlgCheckUsb = true;
        }else{
            Discover->DlgCheckEthernet = true;
            Discover->DlgCheckUsb = true;
        }
        // Display the dialog box for finding an Alta camera
        Discover->ShowDialog( true );
        
        // If a camera was not selected, then release objects and exit
        if(!Discover->ValidSelection){
            printf( "No valid camera selection made\n" );
            Discover = NULL;        // Release ICamDiscover COM object
            AltaCamera = NULL;      // Release ICamera2 COM object
            CoUninitialize();       // Close the COM library
            return DEVICE_ERR;
        }
        
        // Initialize camera using the ICamDiscover properties
        hr = AltaCamera->Init( Discover->SelectedInterface,
                              Discover->SelectedCamIdOne,
                              Discover->SelectedCamIdTwo,
                              0x0 );
        
        if(SUCCEEDED(hr)){
            printf( "Connection to camera succeeded.\n" );
        }else{
            printf( "Failed to connect to camera" );
            Discover = NULL;        // Release Discover COM object
            AltaCamera = NULL;      // Release ICamera2 COM object
            CoUninitialize();       // Close the COM library
            return DEVICE_ERR;
        }
        m_nInterfaceType = (long)Discover->SelectedInterface;
        m_nCamIdOne = Discover->SelectedCamIdOne;
        m_nCamIdTwo = Discover->SelectedCamIdTwo;
    }
    
    AltaCamera->ResetSystem();
    
    // Display the camera model and version
    _bstr_t szCamModel( AltaCamera->CameraModel );
    char *camModelStr = (char*)szCamModel;
    _bstr_t szDriverVer( AltaCamera->DriverVersion );
    char *camVersionStr = (char*)szDriverVer;
	char camIdStr[64];
	if(m_nInterfaceType==Apn_Interface_NET)
		sprintf(camIdStr, "NET %d, %d", m_nCamIdOne, m_nCamIdTwo);
	else
		sprintf(camIdStr, "USB %d, %d", m_nCamIdOne, m_nCamIdTwo);
    
    printf("Camera model: '%s'; version: '%s'.\n",camModelStr,camVersionStr);
    
    // set property list
    // -----------------
    
	CPropertyAction *pAct;

    // Name
    int nRet = CreateProperty(MM::g_Keyword_Name, g_CameraDeviceName, MM::String, true);
    if(DEVICE_OK != nRet)
        return nRet;
    
    // Description
    nRet = CreateProperty(MM::g_Keyword_Description, "Apogee Camera Device Adapter", MM::String, true);
    if(DEVICE_OK != nRet)
        return nRet;
    
    // CameraName
    nRet = CreateProperty(MM::g_Keyword_CameraName, camModelStr, MM::String, true);
    assert(nRet == DEVICE_OK);
    
    // CameraID
    nRet = CreateProperty(MM::g_Keyword_CameraID, camIdStr, MM::String, true);
    assert(nRet == DEVICE_OK);

	// name was "MM::g_Keyword_CameraID"
    nRet = CreateProperty("FirmwareVersion", camVersionStr, MM::String, true);
    assert(nRet == DEVICE_OK);
	
	// Shutter Mode
	pAct = new CPropertyAction (this, &CApogeeCamera::OnShutterMode);
    nRet = CreateProperty("ShutterMode", "Internal Auto", MM::String, false, pAct);
    assert(nRet == DEVICE_OK);
    vector<string> shutterModeValues;
    shutterModeValues.push_back("Internal Auto");
    shutterModeValues.push_back("Internal Open");
	shutterModeValues.push_back("Internal Disabled");
	shutterModeValues.push_back("External");
	shutterModeValues.push_back("External IO Readout");
    nRet = SetAllowedValues("ShutterMode", shutterModeValues);
    if(nRet != DEVICE_OK) return nRet;

	// Pixel size
	pAct = new CPropertyAction(this, &CApogeeCamera::OnCCDPixSizeX);
	nRet = CreateProperty("PixelWidthMicrometers", "0", MM::Float, true, pAct);
	assert(nRet == DEVICE_OK);
    pAct = new CPropertyAction(this, &CApogeeCamera::OnCCDPixSizeY);
	nRet = CreateProperty("PixelHeightMicrometers", "0", MM::Float, true, pAct);
	assert(nRet == DEVICE_OK);

    // Main GUI binning (isotropic binning)
    pAct = new CPropertyAction (this, &CApogeeCamera::OnBinning);
    nRet = CreateProperty(MM::g_Keyword_Binning, "1", MM::Integer, false, pAct);
    assert(nRet == DEVICE_OK);
    vector<string> binValues;
    long maxBin = min(AltaCamera->MaxBinningH, AltaCamera->MaxBinningV);
	maxBin = min(maxBin, 8);
    char tmp[8];
	sprintf(tmp, "%d", 1); binValues.push_back(tmp);
    for(int ii=2; ii<=maxBin; ii+=2){
        sprintf(tmp, "%d", ii); binValues.push_back(tmp);
    }
    nRet = SetAllowedValues(MM::g_Keyword_Binning, binValues);
    if(nRet != DEVICE_OK) return nRet;
    
    // X binning (will only show up in Property Browser)
    pAct = new CPropertyAction (this, &CApogeeCamera::OnXBinning);
    nRet = CreateProperty("BinningX", "1", MM::Integer, false, pAct);
    assert(nRet == DEVICE_OK);
    SetPropertyLimits("BinningX", 1, AltaCamera->MaxBinningH);

	// Y binning (will only show up in Property Browser)
	// *** Alta supports Y-binning of up to the height of the CCD- should we allow this?
    pAct = new CPropertyAction (this, &CApogeeCamera::OnYBinning);
    nRet = CreateProperty("BinningY", "1", MM::Integer, false, pAct);
    assert(nRet == DEVICE_OK);
    SetPropertyLimits("BinningY", 1, AltaCamera->MaxBinningV);

    // pixel type
    pAct = new CPropertyAction (this, &CApogeeCamera::OnPixelType);
    nRet = CreateProperty(MM::g_Keyword_PixelType, g_PixelType_16bit, MM::String, false, pAct);
    assert(nRet == DEVICE_OK);
    vector<string> pixelTypeValues;
    pixelTypeValues.push_back(g_PixelType_12bit);
    pixelTypeValues.push_back(g_PixelType_16bit);
    nRet = SetAllowedValues(MM::g_Keyword_PixelType, pixelTypeValues);
    if(nRet != DEVICE_OK) return nRet;
    
    // exposure
	// *** Gary prefers "exposure-msec"
    pAct = new CPropertyAction (this, &CApogeeCamera::OnExposure);
    nRet = CreateProperty(MM::g_Keyword_Exposure, "100.0", MM::Float, false, pAct);
    assert(nRet == DEVICE_OK);
    SetPropertyLimits(MM::g_Keyword_Exposure, AltaCamera->MinExposure*1000, AltaCamera->MaxExposure*1000);
    //SetPropertyLimits(MM::g_Keyword_Exposure, .1, 100000);

    // light/dark image mode
    pAct = new CPropertyAction (this, &CApogeeCamera::OnLightMode);
    nRet = CreateProperty("AcquisitionMode", "Light", MM::String, false, pAct);
    assert(nRet == DEVICE_OK);
    vector<string> lightModeValues;
    lightModeValues.push_back("Light");
    lightModeValues.push_back("Dark");
    nRet = SetAllowedValues("AcquisitionMode", lightModeValues);
    if(nRet != DEVICE_OK) return nRet;

	// *** ADD BIAS
    
    // camera gain
	// *** Gary prefers "Gain-12bit" 
    pAct = new CPropertyAction (this, &CApogeeCamera::OnGain);
    nRet = CreateProperty(MM::g_Keyword_Gain, "0", MM::Integer, false, pAct);
    assert(nRet == DEVICE_OK);
    SetPropertyLimits(MM::g_Keyword_Gain, -5, 8);
    
    // camera offset
	// *** Gary prefers "Offset-12bit"
    pAct = new CPropertyAction (this, &CApogeeCamera::OnOffset);
    nRet = CreateProperty(MM::g_Keyword_Offset, "0", MM::Integer, false, pAct);
    assert(nRet == DEVICE_OK);

	// Only set up cooler properties if the camera supports cooling
	if(AltaCamera->CoolerControl){
		// cooler enable
		pAct = new CPropertyAction (this, &CApogeeCamera::OnCoolerEnable);
		nRet = CreateProperty("CoolerEnable", "On", MM::String, false, pAct);
		assert(nRet == DEVICE_OK);
		vector<string> modeValues;
		modeValues.push_back("Off");
		modeValues.push_back("On");
		nRet = SetAllowedValues("CoolerEnable", modeValues);
		assert(nRet == DEVICE_OK);

		// camera temperature
		pAct = new CPropertyAction(this, &CApogeeCamera::OnCCDTemperature);
		nRet = CreateProperty(MM::g_Keyword_CCDTemperature, "0", MM::Float, true, pAct);
		assert(nRet == DEVICE_OK);

		// heatsink temperature
		pAct = new CPropertyAction(this, &CApogeeCamera::OnHeatsinkTemperature);
		nRet = CreateProperty("HeatsinkTemperature", "0", MM::Float, true, pAct);
		assert(nRet == DEVICE_OK);

		// Cooler drive
		pAct = new CPropertyAction(this, &CApogeeCamera::OnCoolerDriveLevel);
		nRet = CreateProperty("CoolerDriveLevel", "0", MM::Float, true, pAct);
		assert(nRet == DEVICE_OK);
        
		// Only set up these if the camera supports regulated cooling
		if(AltaCamera->CoolerRegulated){
			pAct = new CPropertyAction(this, &CApogeeCamera::OnCameraTemperatureSetPoint);
			nRet = CreateProperty(MM::g_Keyword_CCDTemperatureSetPoint, "0", MM::Float, false, pAct);
			assert(nRet == DEVICE_OK);
			SetPropertyLimits(MM::g_Keyword_CCDTemperatureSetPoint, -100, 10);

			pAct = new CPropertyAction(this, &CApogeeCamera::OnCameraTemperatureBackoffPoint);
			nRet = CreateProperty("CameraTemperatureBackoffPoint", "0", MM::Float, false, pAct);
			assert(nRet == DEVICE_OK);
			SetPropertyLimits("CameraTemperatureBackoffPoint", 0, 20);
		}

		// cooler status
		pAct = new CPropertyAction(this, &CApogeeCamera::OnCoolerStatus);
		nRet = CreateProperty("CoolerStatus", "Off", MM::String, true, pAct);
		assert(nRet == DEVICE_OK);

		// cooler fan mode
		pAct = new CPropertyAction (this, &CApogeeCamera::OnCoolerFanMode);
		nRet = CreateProperty("FanSpeed", "Medium", MM::String, false, pAct);
		assert(nRet == DEVICE_OK);
		vector<string> fanModeValues;
		fanModeValues.push_back("Off");
		fanModeValues.push_back("Low");
		fanModeValues.push_back("Medium");
		fanModeValues.push_back("High");
		nRet = SetAllowedValues("FanSpeed", fanModeValues);
		assert(nRet == DEVICE_OK);
	} // end if(AltaCamera->CoolerControl){

	// I/O pin 1 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin1Mode);
	nRet = CreateProperty("IoSignal_1", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io1ModeValues;
	io1ModeValues.push_back("User Input");
	io1ModeValues.push_back("User Output");
	io1ModeValues.push_back("Trigger input");
	nRet = SetAllowedValues("IoSignal_1", io1ModeValues);
	assert(nRet == DEVICE_OK);

	// I/O pin 2 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin2Mode);
	nRet = CreateProperty("IoSignal_2", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io2ModeValues;
	io2ModeValues.push_back("User Input");
	io2ModeValues.push_back("User Output");
	io2ModeValues.push_back("Shutter output");
	nRet = SetAllowedValues("IoSignal_2", io2ModeValues);
	assert(nRet == DEVICE_OK);

	// I/O pin 3 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin3Mode);
	nRet = CreateProperty("IoSignal_3", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io3ModeValues;
	io3ModeValues.push_back("User Input");
	io3ModeValues.push_back("User Output");
	io3ModeValues.push_back("Shutter strobe output");
	nRet = SetAllowedValues("IoSignal_3", io3ModeValues);
	assert(nRet == DEVICE_OK);
    
	// I/O pin 4 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin4Mode);
	nRet = CreateProperty("IoSignal_4", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io4ModeValues;
	io4ModeValues.push_back("User Input");
	io4ModeValues.push_back("User Output");
	io4ModeValues.push_back("External shutter input");
	nRet = SetAllowedValues("IoSignal_4", io4ModeValues);
	assert(nRet == DEVICE_OK);

	// I/O pin 5 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin5Mode);
	nRet = CreateProperty("IoSignal_5", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io5ModeValues;
	io5ModeValues.push_back("User Input");
	io5ModeValues.push_back("User Output");
	io5ModeValues.push_back("Readout start input");
	nRet = SetAllowedValues("IoSignal_5", io5ModeValues);
	assert(nRet == DEVICE_OK);

	// I/O pin 6 assignment
	pAct = new CPropertyAction (this, &CApogeeCamera::OnIoPin6Mode);
	nRet = CreateProperty("IoSignal_6", "User Input", MM::String, false, pAct);
	assert(nRet == DEVICE_OK);
	vector<string> io6ModeValues;
	io6ModeValues.push_back("User Input");
	io6ModeValues.push_back("User Output");
	io6ModeValues.push_back("Input timer pulse");
	nRet = SetAllowedValues("IoSignal_6", io6ModeValues);
	assert(nRet == DEVICE_OK);

    // synchronize all properties
    // --------------------------
    nRet = UpdateStatus();
    if(nRet != DEVICE_OK)
        return nRet;
    
    // setup the buffer
    // ----------------
	SetROI(0, 0, 0, 0);
    nRet = ResizeImageBuffer();
    if(nRet != DEVICE_OK)
        return nRet;
    
    m_bInitialized = true;
    
	return DEVICE_OK;
}

/**
 * Shuts down (unloads) the device.
 * Required by the MM::Device API.
 * Ideally this method will completely unload the device and release all resources.
 * Shutdown() may be called multiple times in a row.
 * After Shutdown() we should be allowed to call Initialize() again to load the device
 * without causing problems.
 */
int CApogeeCamera::Shutdown()
{
    m_bInitialized = false;
    // *** WORK HERE- send proper call to hardware ***
    AltaCamera	= NULL;		// Release ICamera2 COM object
    CoUninitialize();		// Close the COM library
    return DEVICE_OK;
}

/**
 * Returns the current exposure setting in milliseconds.
 * (Required by the MM::Camera API.)
 */
double CApogeeCamera::GetExposure() const
{
    char buf[MM::MaxStrLength];
    int ret = GetProperty(MM::g_Keyword_Exposure, buf);
    if(ret != DEVICE_OK)
        return 0.0;
    return atof(buf);
}

/**
 * Sets exposure in milliseconds.
 * (Required by the MM::Camera API.)
 */
void CApogeeCamera::SetExposure(double exp)
{
    SetProperty(MM::g_Keyword_Exposure, CDeviceUtils::ConvertToString(exp));
}

/**
 * Returns the current binning factor.
 * Required by the MM::Camera API.
 */
int CApogeeCamera::GetBinning() const
{
    char buf[MM::MaxStrLength];
    int ret = GetProperty(MM::g_Keyword_Binning, buf);
    if(ret != DEVICE_OK)
        return 1;
    return atoi(buf);
}

/**
 * Sets binning factor.
 * Required by the MM::Camera API.
 */
int CApogeeCamera::SetBinning(int binFactor)
{
    return SetProperty(MM::g_Keyword_Binning, CDeviceUtils::ConvertToString(binFactor));
}


///////////////////////////////////////////////////////////////////////////////
// CApogeeCamera Action handlers
///////////////////////////////////////////////////////////////////////////////

/**
 * Handles camera interface type property
 */
int CApogeeCamera::OnCameraInterface(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet){
        if(m_nInterfaceType==0)
            pProp->Set("Net");
        else if(m_nInterfaceType==1)
            pProp->Set("USB");
        else
            pProp->Set("Discover");
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
        if(mode.compare("Discover")==0)
            m_nInterfaceType = 999;
        else if(mode.compare("Net")==0)
            m_nInterfaceType = 0;
        else if(mode.compare("USB")==0)
            m_nInterfaceType = 1;
        else
            assert(!"Unsupported interface mode");
    }
    return DEVICE_OK;
}

/**
 * Handles camera Id One property
 */
int CApogeeCamera::OnCameraIdOne(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet){
        pProp->Set((long)m_nCamIdOne);
    }else if(eAct == MM::AfterSet){
        long id;
        pProp->Get(id);
        m_nCamIdOne = id;
    }
    return DEVICE_OK;
}
/**
 * Handles camera Id Two property
 */
int CApogeeCamera::OnCameraIdTwo(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet){
        pProp->Set((long)m_nCamIdTwo);
    }else if(eAct == MM::AfterSet){
        long id;
        pProp->Get(id);
        m_nCamIdTwo = id;
    }
    return DEVICE_OK;
}

int CApogeeCamera::OnCCDPixSizeX(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet)
        pProp->Set(AltaCamera->PixelSizeX);
    return DEVICE_OK;
}
int CApogeeCamera::OnCCDPixSizeY(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet)
        pProp->Set(AltaCamera->PixelSizeY);
    return DEVICE_OK;
}

/*
 * Handles "ShutterMode" property.
 */
int CApogeeCamera::OnShutterMode(MM::PropertyBase* pProp, MM::ActionType eAct)
{ 
    if(eAct == MM::BeforeGet){
		bool disableVal = AltaCamera->DisableShutter==1;
		bool readoutVal = AltaCamera->ExternalIoReadout==1;
        bool externVal = AltaCamera->ExternalShutter==1;
		bool openVal = AltaCamera->ForceShutterOpen==1;
		if(externVal){
			// Allow two external modes- one with external shutter readout control
			// and the other with readout controlled by a separate IO pin.
			if(readoutVal)
				pProp->Set("External IO Readout");
            else
				pProp->Set("External");
		}else{
			// Internal shutter modes
			if(openVal)
				pProp->Set("Internal Open");
            else if(disableVal) 
				pProp->Set("Internal Disabled");
            else
				pProp->Set("Internal Auto");
		}
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
		if(mode.compare("External")==0){
			AltaCamera->ExternalShutter = true;
			AltaCamera->ExternalIoReadout = false;
			AltaCamera->ForceShutterOpen = true;
		}else if(mode.compare("External IO Readout")==0){
			AltaCamera->ExternalShutter = true;
			AltaCamera->ExternalIoReadout = true;
			AltaCamera->ForceShutterOpen = true;
		}else if(mode.compare("Internal Auto")==0){
			AltaCamera->ExternalShutter = false;
			AltaCamera->ExternalIoReadout = false;
			AltaCamera->ForceShutterOpen = false;
			AltaCamera->DisableShutter = false;
		}else if(mode.compare("Internal Open")==0){
			AltaCamera->ExternalShutter = false;
			AltaCamera->ExternalIoReadout = false;
			AltaCamera->ForceShutterOpen = true;
			AltaCamera->DisableShutter = false;
		}else if(mode.compare("Internal Disabled")==0){
			AltaCamera->ExternalShutter = false;
			AltaCamera->ExternalIoReadout = false;
			AltaCamera->ForceShutterOpen = false;
			AltaCamera->DisableShutter = true;
		}else
            assert(!"Unsupported shutter mode");
    }
    return DEVICE_OK;
}

/*
 * Handles "CoolerEnable" property.
 */
int CApogeeCamera::OnCoolerEnable(MM::PropertyBase* pProp, MM::ActionType eAct)
{ 
    if(eAct == MM::BeforeGet){
        bool modeVal = AltaCamera->CoolerEnable==1;
        if(modeVal)
            pProp->Set("On");
        else
            pProp->Set("Off");
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
        if(mode.compare("Off")==0)
            AltaCamera->CoolerEnable = false;
        else if(mode.compare("On")==0)
            AltaCamera->CoolerEnable = true;
        else
            assert(!"Unsupported cooler enable mode");
    }
    return DEVICE_OK;
}

/**
 * Handles Camera Temperature Set-point property
 */
int CApogeeCamera::OnCameraTemperatureSetPoint(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet){
        pProp->Set(AltaCamera->CoolerSetPoint);
    }else if(eAct == MM::AfterSet){
        double temp;
        pProp->Get(temp);
        AltaCamera->CoolerSetPoint = temp;
        // Read it back out to get the actual set value
        pProp->Set(AltaCamera->CoolerSetPoint);
    }
    return DEVICE_OK;
}

int CApogeeCamera::OnCameraTemperatureBackoffPoint(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::BeforeGet){
        pProp->Set(AltaCamera->CoolerBackoffPoint);
    }else if(eAct == MM::AfterSet){
        double temp;
        pProp->Get(temp);
        AltaCamera->CoolerBackoffPoint = temp;
        // Read it back out to get the actual set value
        pProp->Set(AltaCamera->CoolerBackoffPoint);
    }
    return DEVICE_OK;
}

/**
 * Handles Camera Temperature properties
 */
int CApogeeCamera::OnCCDTemperature(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    // Read-only
    if(eAct == MM::BeforeGet)
        pProp->Set(AltaCamera->TempCCD);
    return DEVICE_OK;
}

int CApogeeCamera::OnHeatsinkTemperature(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    // Read-only
    if(eAct == MM::BeforeGet)
        pProp->Set(AltaCamera->TempHeatsink);
    return DEVICE_OK;
}
int CApogeeCamera::OnCoolerDriveLevel(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    // Read-only
    if(eAct == MM::BeforeGet)
        pProp->Set(AltaCamera->CoolerDrive);
    return DEVICE_OK;
}
int CApogeeCamera::OnCoolerStatus(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    // Read-only
    if(eAct == MM::BeforeGet){
        long modeVal = (long)AltaCamera->CoolerStatus;
        if(modeVal==Apn_CoolerStatus_Off)
            pProp->Set("Off");
        else if(modeVal==Apn_CoolerStatus_RampingToSetPoint)
            pProp->Set("Ramping to set point");
        else if(modeVal==Apn_CoolerStatus_AtSetPoint)
            pProp->Set("At set point");
        else if(modeVal==Apn_CoolerStatus_Revision)
            pProp->Set("Cooler-generated temperature revision");
        else
            assert(!"Unexpected cooler status mode returned from camera");
	}
    return DEVICE_OK;
}


/*
 * Handles "CoolerFanMode" property.
 * Changes allowed Binning values to test whether the UI updates properly
 */
int CApogeeCamera::OnCoolerFanMode(MM::PropertyBase* pProp, MM::ActionType eAct)
{ 
    if(eAct == MM::BeforeGet){
        long modeVal = (long)AltaCamera->FanMode;
        if(modeVal==Apn_FanMode_Off)
            pProp->Set("Off");
        else if(modeVal==Apn_FanMode_Low)
            pProp->Set("Low");
        else if(modeVal==Apn_FanMode_Medium)
            pProp->Set("Medium");
        else if(modeVal==Apn_FanMode_High)
            pProp->Set("High");
        else
            assert(!"Unexpected fan mode returned from camera");
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
        if(mode.compare("Off")==0)
            AltaCamera->FanMode = Apn_FanMode_Off;
        else if(mode.compare("Low")==0)
            AltaCamera->FanMode = Apn_FanMode_Low;
        else if(mode.compare("Medium")==0)
            AltaCamera->FanMode = Apn_FanMode_Medium;
        else if(mode.compare("High")==0)
            AltaCamera->FanMode = Apn_FanMode_High;
        else
            assert(!"Unsupported fan mode");
    }
    return DEVICE_OK;
}

/**
 * Handles "Exposure" property.
 */
int CApogeeCamera::OnExposure(MM::PropertyBase* pProp, MM::ActionType eAct)
{
	if(eAct == MM::BeforeGet){
        pProp->Set(m_dExposure);
	}else if(eAct == MM::AfterSet){
		double exp;
        pProp->Get(exp);
		m_dExposure = exp;
	}
    // We set the exposure time on the camera only when we are ready to acquire  
    return DEVICE_OK;
}

/**
 * Handles "PixelType" property.
 */
int CApogeeCamera::OnPixelType(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        string pixelType;
        pProp->Get(pixelType);
        if(pixelType.compare(g_PixelType_12bit)==0){
            AltaCamera->DataBits = Apn_Resolution_TwelveBit;
        }else if(pixelType.compare(g_PixelType_16bit)==0){
            AltaCamera->DataBits = Apn_Resolution_SixteenBit;
        }else{
            assert(!"Unsupported pixel type");
            // on error switch to default pixel type
            pProp->Set(g_PixelType_12bit);
            return DEVICE_ERR;
        }
    }
    else if(eAct == MM::BeforeGet){
        if(AltaCamera->DataBits == Apn_Resolution_TwelveBit)
            pProp->Set(g_PixelType_12bit);
        else if(AltaCamera->DataBits == Apn_Resolution_SixteenBit)
            pProp->Set(g_PixelType_16bit);
        else{
            assert(!"Unsupported pixel depth");
            // on error switch to default pixel type
            pProp->Set(g_PixelType_12bit);
            return DEVICE_ERR;
        }
    }
    return DEVICE_OK;
}

int CApogeeCamera::OnLightMode(MM::PropertyBase* pProp, MM::ActionType eAct)
{ 
    if(eAct == MM::BeforeGet){
        if(m_nLightImgMode)
            pProp->Set("Light");
        else
            pProp->Set("Dark");
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
        if(mode.compare("Light")==0)
            m_nLightImgMode = true;
        else if(mode.compare("Dark")==0)
            m_nLightImgMode = false;
        else
            assert(!"Unsupported light/dark mode");
    }
    return DEVICE_OK;
}

/*
 * Handle gain/offset. Note that these values only have an effect in 12-bit mode.
 */
int CApogeeCamera::OnGain(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        long gain;
        pProp->Get(gain);
        AltaCamera->GainTwelveBit = gain;
    }else if(eAct == MM::BeforeGet){
        pProp->Set(AltaCamera->GainTwelveBit);
    }
    return DEVICE_OK;
}

int CApogeeCamera::OnOffset(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        long offset;
        pProp->Get(offset);
        AltaCamera->OffsetTwelveBit = offset;
    }else if(eAct == MM::BeforeGet){
        pProp->Set(AltaCamera->OffsetTwelveBit);
    }
    return DEVICE_OK;
}

/*
 * I/O Pin assigmnet handlers
 */
int CApogeeCamera::OnIoPinMode(MM::PropertyBase* pProp, MM::ActionType eAct, long mask, const char *altStr)
{
    if(eAct == MM::BeforeGet){
        long modeVal = (long)AltaCamera->IoPortAssignment;
		long dirVal = (long)AltaCamera->IoPortDirection;
		if(modeVal & mask){
            pProp->Set(altStr);
		}else{
			if(dirVal & mask)
				pProp->Set("User Output");
			else 
				pProp->Set("User Input");
		}
    }else if(eAct == MM::AfterSet){
        string mode;
        pProp->Get(mode);
        if(mode.compare(altStr)==0)
            AltaCamera->IoPortAssignment |= mask;
		else{
			AltaCamera->IoPortAssignment &= ~mask;
			if(mode.compare("User Output")==0)
				AltaCamera->IoPortDirection |= mask;
			else if(mode.compare("User Input")==0)
				AltaCamera->IoPortDirection &= ~mask;
			else
				assert(!"Unsupported IO port direction mode");
		}
    }
    return DEVICE_OK;
}

int CApogeeCamera::OnIoPin1Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 1, "Trigger input");
}
int CApogeeCamera::OnIoPin2Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 2, "Shutter output");
}
int CApogeeCamera::OnIoPin3Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 4, "Shutter strobe output");
}
int CApogeeCamera::OnIoPin4Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 8, "External shutter input");
}
int CApogeeCamera::OnIoPin5Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 16, "Readout start input");
}
int CApogeeCamera::OnIoPin6Mode(MM::PropertyBase* pProp, MM::ActionType eAct){
	return OnIoPinMode(pProp, eAct, 32, "Input timer pulse");
}


///////////////////////////////////////////////////////////////////////////////
// Performs exposure and grabs a single image to the camera's internal buffer.
// Required by the MM::Camera API. 
//
int CApogeeCamera::SnapImage()
{
	// DEBUG: MM::MMTime startTime = GetCurrentMMTime();

	// We'll sleep for this long after triggering the exposure and
	// before we begin polling the camera to see if it thinks the
	// exposure has ended. We subtract 2ms to make sure that we get
	// to start polling just before the exposure finishes.
	long sleepMs = (long) (m_dExposure - 1.5);
	if (sleepMs < 0) sleepMs = 0;

    AltaCamera->Expose(m_dExposure/1000, m_nLightImgMode);

	// DEBUG: LogTimeDiff(startTime, GetCurrentMMTime(), "AltaCamera->Expose time: ", false);

	// Sleep until just before exposure is supposed to end
	if(sleepMs>0)
		CDeviceUtils::SleepMs(sleepMs);
	// Start polling to accurately catch the end of exposure
    while(AltaCamera->ImagingStatus == Apn_Status_Exposing);

	// DEBUG: LogTimeDiff(startTime, GetCurrentMMTime(), "SnapImage total time: ", false);

    return DEVICE_OK;
}

/**
 * Returns pixel data.
 * Required by the MM::Camera API.
 * The calling program will assume the size of the buffer based on the values
 * obtained from GetImageBufferSize(), which in turn should be consistent with
 * values returned by GetImageWidth(), GetImageHight() and GetImageBytesPerPixel().
 * The calling program allso assumes that camera never changes the size of
 * the pixel buffer on its own. In other words, the buffer can change only if
 * appropriate properties are set (such as binning, pixel type, etc.)
 */

///////////////////////////////////////////////////////////////////////////////
// Function name   : char* CApogeeCamera::GetImageBuffer
// Description     : Returns the raw image buffer
// Return type     : const unsigned 

const unsigned char* CApogeeCamera::GetImageBuffer()
{
    // Get the image data from the camera
    
    if(img_.Depth() != 2) assert(!"Unsupported pixel depth.");
    if(img_.Width()!=GetImageWidth() || img_.Height()!=GetImageHeight()) 
        assert(!"Image buffer size does not match camera buffer.");

	unsigned short* pBuf = (unsigned short*) const_cast<unsigned char*>(img_.GetPixels());
    // The Apogee API insists on a 'long', so that's what we have to give it.
    // Not sure how to prevent the warning that this generates.

    // Make sure the image data are ready:
    while(AltaCamera->ImagingStatus != Apn_Status_ImageReady)
		CDeviceUtils::SleepMs(1);

    // Get the image
    AltaCamera->GetImage((long)pBuf);
    
    return img_.GetPixels();
}

/**
 * Returns image buffer X-size in pixels.
 * Required by the MM::Camera API.
 */
unsigned CApogeeCamera::GetImageWidth() const
{
    
    return (unsigned)AltaCamera->RoiPixelsH;
}

/**
 * Returns image buffer Y-size in pixels.
 * Required by the MM::Camera API.
 */
unsigned CApogeeCamera::GetImageHeight() const
{
    return (unsigned)AltaCamera->RoiPixelsV;
}

/**
 * Returns image buffer pixel depth in bytes.
 * Required by the MM::Camera API.
 */
unsigned CApogeeCamera::GetImageBytesPerPixel() const
{
    return 2;
} 


/**
 * Returns the bit depth (dynamic range) of the pixel.
 * This does not affect the buffer size, it just gives the client application
 * a guideline on how to interpret pixel values.
 * Required by the MM::Camera API.
 */
unsigned CApogeeCamera::GetBitDepth() const
{
    if(AltaCamera->DataBits == Apn_Resolution_TwelveBit)
        return 12;
    else if(AltaCamera->DataBits == Apn_Resolution_SixteenBit)
        return 16;
    else{
        assert(!"unsupported bits per pixel count");
        return 0; // should not happen
    }
}

/**
 * Returns the size in bytes of the image buffer.
 * Required by the MM::Camera API.
 */
long CApogeeCamera::GetImageBufferSize() const
{
    return img_.Width() * img_.Height() * GetImageBytesPerPixel();
}

/**
 * Handles "Binning" property.
 */
int CApogeeCamera::OnBinning(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        // the user just set the new value for the property, so we have to
        // apply this value to the hardware.
        long bin;
        pProp->Get(bin);
		double binXDelta = (double)AltaCamera->RoiBinningH/(double)bin;
		double binYDelta = (double)AltaCamera->RoiBinningV/(double)bin;
		AltaCamera->RoiBinningH = bin;
        AltaCamera->RoiBinningV = bin;
		// The Alta driver requires us to adjust RoiPixels after a binning change.
		SetROI(0, 0, (unsigned)(binXDelta*m_roiH+0.5), (unsigned)(binYDelta*m_roiV+0.5));
        return ResizeImageBuffer();
    }
    else if(eAct == MM::BeforeGet){
        // the user is requesting the current value for the property, so
        // ask the hardware for it's current setting. 
        pProp->Set((long)AltaCamera->RoiBinningH);
    }
    return DEVICE_OK;
}

/**
 * Handles "XBinning" property.
 */
int CApogeeCamera::OnXBinning(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        long bin;
        pProp->Get(bin);
		double binXDelta = (double)AltaCamera->RoiBinningH/(double)bin;
		AltaCamera->RoiBinningH = bin;
		// The Alta driver requires us to adjust RoiPixels after a binning change.
		SetROI(0, 0, (unsigned)(binXDelta*m_roiH+0.5), (unsigned)m_roiV);
        return ResizeImageBuffer();
    }
    else if(eAct == MM::BeforeGet){
        pProp->Set((long)AltaCamera->RoiBinningH);
    }
    return DEVICE_OK;
}

/**
 * Handles "YBinning" property.
 */
int CApogeeCamera::OnYBinning(MM::PropertyBase* pProp, MM::ActionType eAct)
{
    if(eAct == MM::AfterSet){
        long bin;
        pProp->Get(bin);
		double binYDelta = (double)AltaCamera->RoiBinningV/(double)bin;
        AltaCamera->RoiBinningV = bin;
		// The Alta driver requires us to adjust RoiPixels after a binning change.
		SetROI(0, 0, (unsigned)m_roiH, (unsigned)(binYDelta*m_roiV+0.5));
        return ResizeImageBuffer();
    }
    else if(eAct == MM::BeforeGet){
        pProp->Set((long)AltaCamera->RoiBinningV);
    }
    return DEVICE_OK;
}

/**
 * Sets the camera Region Of Interest.
 * Required by the MM::Camera API.
 * This command will change the dimensions of the image.
 * Depending on the hardware capabilities the camera may not be able to configure the
 * exact dimensions requested - but should try do as close as possible.
 * If the hardware does not have this capability the software should simulate the ROI by
 * appropriately cropping each frame.
 * @param x - top-left corner coordinate
 * @param y - top-left corner coordinate
 * @param xSize - width
 * @param ySize - height
 */
int CApogeeCamera::SetROI(unsigned x, unsigned y, unsigned xSize, unsigned ySize)
{
    // The Alta Camera expects the ROI size to be specified in binned pixels,
    // but the start position to be in unbinned pixels. All inputs are specified
	// in binned pixels. The start point is relative to the current ROI.
    unsigned int maxH = (unsigned)(AltaCamera->ImagingColumns/AltaCamera->RoiBinningH);
    unsigned int maxV = (unsigned)(AltaCamera->ImagingRows/AltaCamera->RoiBinningV);
	// *** FIXME: When we create an ROI within an ROI, this gets confused. We need something like: 
	//x = x+m_roiX;
	//y = y+m_roiY;
    if(xSize == 0 && ySize == 0){
        // Clear ROI
		ClearROI();
    }else if(x<maxH && y<maxV){
		// We always save the current ROI start point in unbinned, absolute coordinates: 
		m_roiX = (long)x * AltaCamera->RoiBinningH + m_roiX;
		m_roiY = (long)y * AltaCamera->RoiBinningV + m_roiY;
        AltaCamera->RoiStartX = m_roiX;
        AltaCamera->RoiStartY = m_roiY;
		if(xSize>(maxH-x)) xSize = maxH-x;
        if(ySize>(maxV-y)) ySize = maxV-y;
        m_roiH = (long)xSize;
        m_roiV = (long)ySize;
		AltaCamera->RoiPixelsH = (long)xSize;
        AltaCamera->RoiPixelsV = (long)ySize;
        ResizeImageBuffer();
	}else
		assert(!"ROI start point out-of-range");
    return DEVICE_OK;
}

/**
 * Returns the actual dimensions of the current ROI.
 * Required by the MM::Camera API.
 */
int CApogeeCamera::GetROI(unsigned& x, unsigned& y, unsigned& xSize, unsigned& ySize)
{
    x = (unsigned)AltaCamera->RoiStartX * AltaCamera->RoiBinningH;
    y = (unsigned)AltaCamera->RoiStartY * AltaCamera->RoiBinningV;
    xSize = (unsigned)AltaCamera->RoiPixelsH;
    ySize = (unsigned)AltaCamera->RoiPixelsV;
    return DEVICE_OK;
}

/**
 * Resets the Region of Interest to full frame.
 * Required by the MM::Camera API.
 */
int CApogeeCamera::ClearROI()
{
	m_roiX = 0;
	m_roiY = 0;
	m_roiH = AltaCamera->ImagingColumns;
	m_roiV = AltaCamera->ImagingRows;
    AltaCamera->RoiStartX = m_roiX;
    AltaCamera->RoiStartY = m_roiY;
	// The ROI is specified in binned pixels
    AltaCamera->RoiPixelsH = m_roiH/AltaCamera->RoiBinningH;
    AltaCamera->RoiPixelsV = m_roiV/AltaCamera->RoiBinningV;
    ResizeImageBuffer();
    return DEVICE_OK;
}


int CApogeeCamera::ResizeImageBuffer()
{
    // get image size
    int nWidth = GetImageWidth();
    int nHeight = GetImageHeight();
    img_.Resize(nWidth, nHeight, 2);
    return DEVICE_OK;
}

// Sequence acquisition methods

void CApogeeCamera::SequenceCheckImageBuffer()
{
	// Get the image data from the camera and place in the circular buffer 
   if(img_.Depth() != 2) assert(!"Unsupported pixel depth.");
	m_sequenceWidth = (unsigned) GetImageWidth();
	m_sequenceHeight = (unsigned) GetImageHeight();
   if(img_.Width()!=m_sequenceWidth || img_.Height()!=m_sequenceHeight) 
      assert(!"Image buffer size does not match camera buffer.");
}

int CApogeeCamera::StartSequenceAcquisition(double interval)
{
   // Set to the maximum value, 65534. Note that 65535 doesn't work!
	return StartSequenceAcquisition(65534, interval, false);
}

int CApogeeCamera::StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow)
{
	SequenceCheckImageBuffer();
	m_sequenceLengthRequested = numImages;
   m_stopOnOverflow = stopOnOverflow;
	AltaCamera->SequenceBulkDownload = false; 
	AltaCamera->ImageCount = numImages;
   AltaCamera->SequenceDelay = interval_ms / 1000.0;
   AltaCamera->Expose(m_dExposure/1000, m_nLightImgMode);

   m_sequenceCount = 0;
	m_acqSequenceThread->Start();
	return DEVICE_OK;
}

int CApogeeCamera::StopSequenceAcquisition()
{
	m_acqSequenceThread->Stop();
	m_acqSequenceThread->wait();
	return DEVICE_OK;
}

bool CApogeeCamera::IsCapturing()
{
	return m_acqSequenceThread->IsRunning();
}

int CApogeeCamera::TransferImage()
{
	if (m_sequenceCount >= m_sequenceLengthRequested)
		return -1;

	if (AltaCamera->SequenceCounter >= m_sequenceCount)
	{
      ++m_sequenceCount;
      unsigned short* pBuf = (unsigned short*) const_cast<unsigned char*>(img_.GetPixels());
	   HRESULT hr = AltaCamera->GetImage((long) pBuf);
	   if (SUCCEEDED(hr))
	   {
	      int ret = GetCoreCallback()->InsertImage(this, (const unsigned char *) pBuf, m_sequenceWidth, m_sequenceHeight, 2);
         if (!m_stopOnOverflow && ret == DEVICE_BUFFER_OVERFLOW)
         {
            // do not stop on overflow - just reset the buffer
            GetCoreCallback()->ClearImageBuffer(this);
         } else
            return ret;
	      }
      else
      {
         return DEVICE_ERR;
      }
	}
	return DEVICE_OK;
}

int CApogeeCamera::CleanupAfterSequence()
{
   AltaCamera->StopExposure(true); 
   AltaCamera->ImageCount = 1;
	return DEVICE_OK;
}

// Acquisition thread

int AcqSequenceThread::Run()
{
	int ret = DEVICE_OK;
	while (! StopRequested() && (ret == DEVICE_OK))
	{  
		ret = camera_->TransferImage();
      CDeviceUtils::SleepMs(5);
	}
	if (ret == -1)
		ret = DEVICE_OK;
	camera_->CleanupAfterSequence();
	return ret;
}