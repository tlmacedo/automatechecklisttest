@echo off
REM Script to compile and run instrumented tests via Gradle for Windows.
REM Usage: run_gradle_test.bat ClassName
REM Usage: run_gradle_test.bat ClassName#MethodName

set PACKAGE_BASE=com.samsung.requirements.automatechecklisttest
set TEST_PACKAGE_PATH=%PACKAGE_BASE%.tests

set TARGET=%1

if "%TARGET%"=="" (
    echo Error: Test class name is required.
    echo Example: run_gradle_test.bat NfcTest
    exit /b 1
)

REM Check if TARGET contains a dot
echo %TARGET% | findstr /C:"." > nul
if errorlevel 1 (
    set TARGET=%TEST_PACKAGE_PATH%.%TARGET%
)

echo Compiling and running via Gradle: %TARGET%
gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=%TARGET%
