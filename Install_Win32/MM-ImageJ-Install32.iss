; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=Micro-Manager-1.4
AppVerName=Micro-Manager-1.4
AppPublisher=UCSF
AppPublisherURL=http://www.micro-manager.org
AppSupportURL=http://www.micro-manager.org
AppUpdatesURL=http://www.micro-manager.org
DefaultDirName=C:/Program Files/Micro-Manager-1.4
DefaultGroupName=Micro-Manager-1.4
OutputBaseFilename=MMSetup_
Compression=lzma
PrivilegesRequired=admin
SolidCompression=true
VersionInfoVersion=1.4
VersionInfoCompany=(c)University of California San Francisco
VersionInfoCopyright=(c)University of California San Francisco, (c)100XImaging Inc
AppCopyright=University of California San Francisco, 100XImaging Inc
ShowLanguageDialog=yes
AppVersion=1.4
AppID=31830087-F23D-4198-B67D-AD4A2A69147F

[Languages]
Name: eng; MessagesFile: compiler:Default.isl

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
; the entire redistributable set
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.ATL\atl90.dll ; DestDir: {app}\Microsoft.VC90.ATL; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.ATL\Microsoft.VC90.ATL.manifest ; DestDir: {app}\Microsoft.VC90.ATL; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.CRT\msvcm90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.CRT\msvcp90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.CRT\msvcr90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.CRT\Microsoft.VC90.CRT.manifest ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFC\mfc90.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFC\mfc90u.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFC\mfcm90.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFC\mfcm90u.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFC\Microsoft.VC90.MFC.manifest ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90CHS.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90CHT.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90DEU.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90ENU.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90ESN.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90ESP.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90FRA.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90ITA.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90JPN.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\MFC90KOR.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.MFCLOC\Microsoft.VC90.MFCLOC.manifest ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.OPENMP\vcomp90.dll ; DestDir: {app}\Microsoft.VC90.OPENMP; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\x86\Microsoft.VC90.OPENMP\Microsoft.VC90.OpenMP.manifest ; DestDir: {app}\Microsoft.VC90.OPENMP; Flags: ignoreversion



; device libraries
Source: micro-manager\ATMCD32D.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\CamUsb_API.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\inpout32.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\libusb0.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\MMCoreJ_wrap.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\usb_main.bin; DestDir: {app}; Flags: ignoreversion

; Breeze stuff
Source: micro-manager\DSLRRemoteLib.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\NKRemoteLib.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\PSRemoteLib.dll; DestDir: {app}; Flags: ignoreversion

Source: micro-manager\mmgr_dal_AAAOTF.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ABSCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_AgilentLaserCombiner.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Andor.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_AndorLaserCombiner.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_AOTF.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Apogee.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Arduino.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ASIFW1000.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ASIStage.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ASIwptr.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_BaumerOptronic.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_CoherentCube.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Conix.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Corvus.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_CSUX.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_DemoCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_DSLRRemoteCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_DTOpenLayer.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_XCite120PC_Exacte.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_FreeSerialPort.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_GenericSLM.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_GigECamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Hamamatsu.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ImageProcessorChain.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_K8055.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_K8061.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_KDV.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_LeicaDMI.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_LeicaDMR.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Ludl.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_MaestroServo.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Marzhauser.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_MCL_MicroDrive.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_MCL_NanoDrive.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_MT20.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Neos.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Nikon.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_NikonAZ100.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_NikonTE2000.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_NikonTI.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_NKRemoteCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_NNLC.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Olympus.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ParallelPort.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PCO_Camera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Pecon.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PI.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PI_GCS.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Piper.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PrecisExcite.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PrincetonInstruments.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Prior.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ProgRes.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PSRemoteCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_PVCAM.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_QCam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Sapphire.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ScionCam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Sensicam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_SerialManager.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_SimpleAutofocus.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_SpectralLMM5.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_SpotCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_SutterLambda.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ThorlabsFilterWheel.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ThorlabsSC10.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_TIScam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_TwainCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_USBManager.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Utilities.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Vincent.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_Yokogawa.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ZeissCAN.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\mmgr_dal_ZeissCAN29.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\camconj.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\FireCamJ.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\MexJCam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\ProcessLib.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\FxLib.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\SysInfo.dll; DestDir: {app}; Flags: ignoreversion


; python wrapper
Source: micro-manager\_MMCorePy.pyd; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist
Source: micro-manager\MMCorePy.py; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist
Source: micro-manager\MMCoreWrapDemo.py; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist

; drivers
Source: micro-manager\drivers_Win32\*; DestDir: {app}\drivers; Flags: ignoreversion

; beanshell scripts
Source: ..\scripts\*; DestDir: {app}\scripts; Flags: ignoreversion

; configuration files
Source: micro-manager\MMConfig_demo.cfg; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\MMDeviceList.txt; DestDir: {app}; Flags: ignoreversion

; ImageJ files
Source: micro-manager\ImageJ.exe; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\ImageJ.cfg; DestDir: {app}; Flags: onlyifdoesntexist;  Permissions: users-modify
Source: ..\..\3rdpartypublic\classext\ij.jar; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\IJ_Prefs.txt; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager\macros\*; DestDir: {app}\macros; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager\plugins\*; DestDir: {app}\plugins; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager\mmplugins\*; DestDir: {app}\mmplugins; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager\mmautofocus\*; DestDir: {app}\mmautofocus; Flags: ignoreversion recursesubdirs createallsubdirs

; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[DIRS]
Name: {app}\macros; Permissions: users-modify
Name: {app}\plugins; Permissions: users-modify
Name: {app}\mmplugins; Permissions: users-modify
Name: {app}\mmautofocus; Permissions: users-modify

[Icons]
Name: {group}\Micro-Manager-1.4; Filename: {app}\ImageJ.exe; WorkingDir: {app}
Name: {group}\{cm:UninstallProgram,Micro-Manager-1.4}; Filename: {uninstallexe}
Name: {commondesktop}\Micro-Manager 1.4; Filename: {app}\ImageJ.exe; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\ImageJ.exe; Description: {cm:LaunchProgram,Micro-Manager-1.4}; Flags: nowait postinstall
