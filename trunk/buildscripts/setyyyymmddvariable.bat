rem set the day-stamp variable
rem needs program yyyymmdd.exe installed in path
FOR /F "tokens=*" %%i in ('\projects\micromanager\yyyymmdd') do SET YYYYMMDD=%%i
