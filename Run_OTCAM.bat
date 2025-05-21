setlocal

rem === Java JDK path ===
set "JAVA_HOME=C:\Users\1468870319121002.CIV\.gradle\jdks\eclipse_adoptium-21-amd64-windows\jdk-21.0.5+11"
set "JavaExe=%JAVA_HOME%\bin\java.exe"

rem === Path to this script ===
set "ScriptPath=%~dp0"

rem === Path to the built JAR ===
set "JarPath=%ScriptPath%build\libs\3DVis-2.0.2.20.jar"

rem === Native libraries if needed ===
set "NativeLibraries=%ScriptPath%bin;%ScriptPath%bin\gdal;%ScriptPath%bin\tena;%ScriptPath%bin\vlc"
set "Path=%NativeLibraries%;%JAVA_HOME%\bin;%Path%"

rem === Print info and run ===
echo Using Java: %JavaExe%
echo Launching JAR: %JarPath%
echo.

"%JavaExe%" -DTENA_PLATFORM=w10-vs2019-64 -DTENA_VERSION=6.0.8 -jar "%JarPath%"
echo.
pause
