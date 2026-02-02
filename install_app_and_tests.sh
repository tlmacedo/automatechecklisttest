#!/bin/bash

# This script compiles and installs the debug version of the app
# and the instrumented tests on a connected device.

echo "Compiling and installing the app and tests..."
./gradlew installDebug installDebugAndroidTest
echo "Installation complete."
