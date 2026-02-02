@echo off
REM This script compiles and installs the debug version of the app
REM and the instrumented tests on a connected device for Windows.

echo Compiling and installing the app and tests...
gradlew.bat installDebug installDebugAndroidTest
echo Installation complete.
