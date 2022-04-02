#!/bin/bash

#title=Checking Java Version

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking Java version..."

$JAVA -version 2>&1 || quit "ERROR: Detect java version failed. Please set JAVA_HOME."

if [[ `isValidJavaVersion` == "false" ]]; then
    quit "ERROR: Java 1.8 or above is required for Byzer-Notebook"
fi
