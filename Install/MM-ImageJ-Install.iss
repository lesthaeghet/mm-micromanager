; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=Micro-Manager 1.2
AppVerName=Micro-Manager 1.2
AppPublisher=UCSF
AppPublisherURL=http://www.micro-manager.org
AppSupportURL=http://www.micro-manager.org
AppUpdatesURL=http://www.micro-manager.org
DefaultDirName=C:/Program Files/Micro-Manager1.2
DefaultGroupName=Micro-Manager 1.2
OutputBaseFilename=MMSetup_1_2_xx
Compression=lzma
SolidCompression=true
VersionInfoVersion=1.2
VersionInfoCompany=micro-manager.org
VersionInfoCopyright=(c)University of California San Francisco
AppCopyright=� University of California San Francisco
ShowLanguageDialog=yes
AppVersion=1.2
AppID={{B624DD17-CB0F-4134-ADC9-D2A20424DD4D}

[Languages]
Name: eng; MessagesFile: compiler:Default.isl

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
; device libraries
Source: micro-manager-1.2\mmgr_dal_DemoCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_DemoStreamingCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Hamamatsu.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\inpout32.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\libusb0.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Ludl.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\MMCoreJ_wrap.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_ParallelPort.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_PVCAM.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_SerialManager.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_SutterLambda.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_ZeissCAN.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_ZeissCAN29.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_AOTF.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Sensicam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Vincent.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_NikonTE2000.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Prior.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Andor.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\ATMCD32D.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_DTOpenLayer.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Nikon.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_ASIFW1000.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_ASIStage.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Yokogawa.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_CSUX.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_QCam.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_USBManager.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_K8055.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_K8061.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Conix.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_SpectralLMM5.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_PI_GCS.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_PI.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_Pecon.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\mmgr_dal_DAZStage.dll; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\ace.dll; DestDir: {app}; Flags: ignoreversion

; drivers
Source: micro-manager-1.2\drivers\K8055_libusb.inf; DestDir: {app}\drivers; Flags: ignoreversion
Source: micro-manager-1.2\drivers\K8055_libusb.cat; DestDir: {app}\drivers; Flags: ignoreversion
Source: micro-manager-1.2\drivers\K8061_libusb.cat; DestDir: {app}\drivers; Flags: ignoreversion
Source: micro-manager-1.2\drivers\K8061_libusb.inf; DestDir: {app}\drivers; Flags: ignoreversion
Source: micro-manager-1.2\drivers\libusb0.dll; DestDir: {app}\drivers; Flags: ignoreversion
Source: micro-manager-1.2\drivers\libusb0.sys; DestDir: {app}\drivers; Flags: ignoreversion

; configuration files
Source: micro-manager-1.2\MMConfig_demo.cfg; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\MMDeviceList.txt; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\MMStartup.bsh; DestDir: {app}; Flags: onlyifdoesntexist

; configurator help files
Source: micro-manager-1.2\conf_intro_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_comport_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_devices_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_delays_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_finish_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_labels_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_presets_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_roles_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_synchro_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\conf_preinit_page.html; DestDir: {app}; Flags: onlyifdoesntexist

; ImageJ files
Source: micro-manager-1.2\ImageJ.exe; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\ImageJ.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\ij.jar; DestDir: {app}; Flags: ignoreversion
Source: micro-manager-1.2\IJ_Prefs.txt; DestDir: {app}; Flags: onlyifdoesntexist
Source: micro-manager-1.2\jre\*; DestDir: {app}\jre; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager-1.2\macros\*; DestDir: {app}\macros; Flags: ignoreversion recursesubdirs createallsubdirs
Source: micro-manager-1.2\plugins\*; DestDir: {app}\plugins; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: {group}\Micro-Manager 1.2; Filename: {app}\ImageJ.exe; WorkingDir: {app}
Name: {group}\{cm:UninstallProgram,Micro-Manager1.2}; Filename: {uninstallexe}
Name: {commondesktop}\Micro-Manager 1.2; Filename: {app}\ImageJ.exe; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\ImageJ.exe; Description: {cm:LaunchProgram,Micro-Manager1.2}; Flags: nowait postinstall skipifsilent
