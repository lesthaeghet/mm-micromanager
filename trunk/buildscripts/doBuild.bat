@rem We reset the echo mode after any "call" below to keep it consistent.
@set ECHO_MODE=on
@echo %ECHO_MODE%

rem Parameters are:
rem doBuild.bat Win32|x64 FULL|INCREMENTAL RELEASE|NIGHTLY UPLOAD|NOUPLOAD

echo %date% - %time%

if "%1"=="x64" (
    set PLATFORM=x64
    set ARCH=64
) else (
    set PLATFORM=Win32
    set ARCH=32
)
set DO_FULL_BUILD=%2
set DO_RELEASE_BUILD=%3
set DO_UPLOAD=%4
if "%DO_FULL_BUILD%"=="FULL" set DO_REBUILD=REBUILD else set DO_REBUILD=

echo stop any instances that might already be running.
pskill javaw.exe
pskill java.exe

cd /d %~dp0\..
echo working directory is
cd

if "%DO_FULL_BUILD%"=="FULL" (
    pushd ..\3rdparty
    svn cleanup --non-interactive
    svn update --accept postpone --force --ignore-externals --non-interactive
    popd

    pushd ..\3rdpartypublic
    echo update 3rdpartypublic tree from the repository
    svn cleanup --non-interactive
    svn update --accept postpone --force --ignore-externals --non-interactive
    popd
)

echo update micromanager tree from the repository
svn cleanup --non-interactive
svn update --accept postpone --non-interactive
pushd SecretDeviceAdapters
svn cleanup --non-interactive
svn update --accept postpone --non-interactive
popd

call buildscripts\buildCpp.bat %PLATFORM% %DO_REBUILD%
@echo %ECHO_MODE%

rem MMCorePy needs to be manually staged
rem (MMCoreJ is staged by the Java build process)
copy .\bin_%PLATFORM%\MMCorePy.py .\Install_%PLATFORM%\micro-manager
copy .\bin_%PLATFORM%\_MMCorePy.pyd .\Install_%PLATFORM%\micro-manager
copy .\MMCorePy_wrap\MMCoreWrapDemo.py .\Install_%PLATFORM%\micro-manager

echo Update the version number in MMVersion.java
set mmversion=""
set YYYYMMDD=""
set TARGETNAME=""
call buildscripts\setmmversionvariable
call buildscripts\setyyyymmddvariable
@echo %ECHO_MODE%
pushd .\mmstudio\src\org\micromanager
del MMVersion.java
svn update --non-interactive
rem for nightly builds we put the version + the date-stamp
if "%DO_RELEASE_BUILD%"=="RELEASE" (
    set TARGETNAME=MMSetup%ARCH%BIT_%mmversion%.exe
    sed -i "s/\"1\.4.*/\"%mmversion%\";/"  MMVersion.java
) else (
    set TARGETNAME=MMSetup%ARCH%BIT_%mmversion%_%YYYYMMDD%.exe
    sed -i "s/\"1\.4.*/\"%mmversion%  %YYYYMMDD%\";/"  MMVersion.java
)
popd

rem remove any installer package with exactly the same name as the current output
echo trying to delete \Projects\micromanager\Install_%PLATFORM%\Output\MMSetup_.exe
del \Projects\micromanager\Install_%PLATFORM%\Output\MMSetup_.exe
echo trying to delete \Projects\micromanager\Install_%PLATFORM%\Output\%TARGETNAME%
del \Projects\micromanager\Install_%PLATFORM%\Output\%TARGETNAME%

ECHO incremental build of Java components...

set cleantarget=
IF "%DO_FULL_BUILD%"=="FULL" SET cleantarget=clean

PUSHD \projects\micromanager\mmStudio\src
echo building mmStudio with command:
echo call ant -buildfile ../build%ARCH%.xml %cleantarget% compile build buildMMReader
call ant -buildfile ../build%ARCH%.xml %cleantarget% compile build buildMMReader
@echo %ECHO_MODE%
POPD

rem haven't got to the bottom of this yet, but Pixel Calibrator and Slide Explorer need this jar file there....
copy \projects\micromanager\bin_%PLATFORM%\plugins\Micro-Manager\MMJ_.jar \projects\micromanager\bin_%PLATFORM%\

pushd buildscripts
call buildJars %DO_FULL_BUILD%
@echo %ECHO_MODE%
popd

pushd mmStudio\src
call ant -buildfile ../build%ARCH%.xml install packInstaller
@echo %ECHO_MODE%
popd

pushd \Projects\micromanager\Install_%PLATFORM%\Output
rename MMSetup_.exe  %TARGETNAME%
popd

REM -- try to install on build machine
set DO_INSTALL=YES
if "%PLATFORM%"=="x64" (
    if not "%PROCESSOR_ARCHITECTURE%"=="AMD64" (
        set DO_INSTALL=NO
    )
)
if "%DO_INSTALL%"=="YES" (
    \Projects\micromanager\Install_%PLATFORM%\Output\%TARGETNAME%  /silent
    ECHO "Done installing"
)

if "%DO_UPLOAD%"=="UPLOAD" (
    pscp -i c:\projects\MM.ppk -batch /projects/micromanager/Install_%PLATFORM%/Output/%TARGETNAME% arthur@valelab.ucsf.edu:../MM/public_html/nightlyBuilds/1.4/Windows/%TARGETNAME%
)

pushd .\mmstudio\src\org\micromanager
del MMVersion.java
svn update --non-interactive
popd

echo %date% - %time%
