@echo off
setlocal

set JAVA_HOME=C:\Users\1468870319121002.CIV\.gradle\jdks\eclipse_adoptium-21-amd64-windows\jdk-21.0.5+11
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using JAVA from: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

call gradlew.bat build

endlocal
