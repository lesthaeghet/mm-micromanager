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
SolidCompression=true
VersionInfoVersion=1.4
VersionInfoCompany=(c)University of California San Francisco
VersionInfoCopyright=(c)University of California San Francisco, (c)100XImaging Inc
AppCopyright=University of California San Francisco, 100XImaging Inc
ShowLanguageDialog=yes
AppVersion=1.4
AppID=31830087-F23D-4198-B67D-AD4A2A69147F
ArchitecturesAllowed=x64
; "ArchitecturesInstallIn64BitMode=x64" requests that the install be
; done in "64-bit mode" on x64, meaning it should use the native
; 64-bit Program Files directory and the 64-bit view of the registry.
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: eng; MessagesFile: compiler:Default.isl

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
; the entire redistributable set
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.ATL\atl90.dll ; DestDir: {app}\Microsoft.VC90.ATL; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.ATL\Microsoft.VC90.ATL.manifest ; DestDir: {app}\Microsoft.VC90.ATL; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.CRT\msvcm90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.CRT\msvcp90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.CRT\msvcr90.dll ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.CRT\Microsoft.VC90.CRT.manifest ; DestDir: {app}\Microsoft.VC90.CRT; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFC\mfc90.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFC\mfc90u.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFC\mfcm90.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFC\mfcm90u.dll ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFC\Microsoft.VC90.MFC.manifest ; DestDir: {app}\Microsoft.VC90.MFC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90CHS.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90CHT.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90DEU.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90ENU.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90ESN.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90ESP.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90FRA.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90ITA.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90JPN.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\MFC90KOR.dll ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.MFCLOC\Microsoft.VC90.MFCLOC.manifest ; DestDir: {app}\Microsoft.VC90.MFCLOC; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.OPENMP\vcomp90.dll ; DestDir: {app}\Microsoft.VC90.OPENMP; Flags: ignoreversion
Source: ..\..\3rdparty\Microsoft\VisualC++\lib\amd64\Microsoft.VC90.OPENMP\Microsoft.VC90.OpenMP.manifest ; DestDir: {app}\Microsoft.VC90.OPENMP; Flags: ignoreversion


;
Source: ..\..\3rdparty\jre\* ; DestDir: {app}\jre; Flags: ignoreversion recursesubdirs createallsubdirs


; device libraries
Source: micro-manager\atmcd64d.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\inpoutx64.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\MMCoreJ_wrap.dll; DestDir: {app}; Flags: ignoreversion
Source: ..\drivers\K8061\amd64\libusb0.dll; DestDir: {app}; DestName: libusb0.dll; Flags: ignoreversion
Source: micro-manager\opencv_highgui231.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\opencv_core231.dll; DestDir: {app}; Flags: ignoreversion

; device adapters
Source: micro-manager\mmgr_dal_*.dll; DestDir: {app}; Flags: ignoreversion

; python wrapper
; NOTE: not available for 64-bit version
;Source: micro-manager\_MMCorePy.pyd; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist
;Source: micro-manager\MMCorePy.py; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist
;Source: micro-manager\MMCoreWrapDemo.py; DestDir: {app}; Flags: ignoreversion skipifsourcedoesntexist

; drivers
Source: ..\drivers\*; DestDir: {app}\drivers; Flags: ignoreversion recursesubdirs

; beanshell scripts
Source: ..\scripts\*; DestDir: {app}\scripts; Flags: ignoreversion

; configuration files
Source: micro-manager\MMConfig_demo.cfg; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\MMDeviceList*.txt; DestDir: {app}; Flags: ignoreversion

; ImageJ files
Source: ..\..\3rdpartypublic\JavaLauncher\ImageJ.exe; DestDir: {app}; Flags: ignoreversion
;Source: micro-manager\ImageJ.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: ..\..\3rdpartypublic\classext\ij.jar; DestDir: {app}; Flags: ignoreversion
Source: micro-manager\IJ_Prefs.txt; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager\macros\*; DestDir: {app}\macros; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager\plugins\*; DestDir: {app}\plugins; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ..\Install_AllPlatforms\micro-manager\mmplugins\*; DestDir: {app}\mmplugins; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ..\Install_AllPlatforms\micro-manager\mmautofocus\*; DestDir: {app}\mmautofocus; Flags: ignoreversion recursesubdirs createallsubdirs

; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[DIRS]
Name: {app}; Permissions: users-modify

[Icons]
Name: {group}\Micro-Manager-1.4; Filename: {app}\ImageJ.exe; WorkingDir: {app}
Name: {group}\{cm:UninstallProgram,Micro-Manager-1.4}; Filename: {uninstallexe}
Name: {commondesktop}\Micro-Manager 1.4; Filename: {app}\ImageJ.exe; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\ImageJ.exe; Description: {cm:LaunchProgram,Micro-Manager-1.4}; Flags: nowait postinstall
