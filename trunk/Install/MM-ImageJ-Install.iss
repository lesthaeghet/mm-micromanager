; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=Micro-Manager
AppVerName=Micro-Manager 1.0 (beta)
AppPublisher=Vale Lab, UCSF
AppPublisherURL=http://www.micro-manager.org
AppSupportURL=http://www.micro-manager.org
AppUpdatesURL=http://www.micro-manager.org
DefaultDirName=C:/Program Files/Micro-Manager
DefaultGroupName=Micro-Manager
OutputBaseFilename=MMSetup_1_0_xx
Compression=lzma
SolidCompression=true
VersionInfoVersion=1.0
VersionInfoCompany=micro-manager.org
VersionInfoCopyright=University of California San Francisco

[Languages]
Name: eng; MessagesFile: compiler:Default.isl

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}; Flags: unchecked

[Files]
; device libraries
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_DemoCamera.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Hamamatsu.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\inpout32.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Ludl.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMCoreJ_wrap.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_ParallelPort.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_PVCAM.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_SerialManager.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_SutterLambda.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_ZeissMTB.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_AOTF.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Sensicam.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Vincent.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_NikonTE2000.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Prior.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Andor.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\ATMCD32D.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_StanfordPhotonics.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_DTOpenLayer.dll; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\mmgr_dal_Nikon.dll; DestDir: {app}; Flags: ignoreversion
; configuration files
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_demo.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_zeiss.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_uberzeiss.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_PVCAM.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_NikonTE2000.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_prior.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_Andor.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_DTOL.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMConfig_StanfordPhotonics.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\MMDeviceList.txt; DestDir: {app}; Flags: ignoreversion

; configurator help files
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_intro_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_comport_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_devices_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_delays_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_finish_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_labels_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_presets_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_roles_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_synchro_page.html; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\conf_preinit_page.html; DestDir: {app}; Flags: onlyifdoesntexist

; ImageJ files
Source: C:\projects\MicroManage\Install\micro-manager-1.0\ImageJ.exe; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\ImageJ.cfg; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\ij.jar; DestDir: {app}; Flags: ignoreversion
Source: C:\projects\MicroManage\Install\micro-manager-1.0\IJ_Prefs.txt; DestDir: {app}; Flags: onlyifdoesntexist
Source: C:\projects\MicroManage\Install\micro-manager-1.0\jre\*; DestDir: {app}\jre; Flags: ignoreversion recursesubdirs createallsubdirs
Source: C:\projects\MicroManage\Install\micro-manager-1.0\macros\*; DestDir: {app}\macros; Flags: ignoreversion recursesubdirs createallsubdirs
Source: C:\projects\MicroManage\Install\micro-manager-1.0\plugins\*; DestDir: {app}\plugins; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
[Icons]
Name: {group}\Micro-Manager + ImageJ; Filename: {app}\ImageJ.exe; WorkingDir: {app}
Name: {group}\{cm:UninstallProgram,Micro-Manager}; Filename: {uninstallexe}
Name: {commondesktop}\Micro-Manager + ImageJ; Filename: {app}\ImageJ.exe; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\ImageJ.exe; Description: {cm:LaunchProgram,Micro-Manager}; Flags: nowait postinstall skipifsilent
