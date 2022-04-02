#!/bin/bash

#title=Checking Mysql Availability & Version

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking Mysql..."

function version_lt() {
  test "$(echo "$@" | tr " " "\n" | sort -rV | head -n 1)" != "$1";
}

mkdir -p ${NOTEBOOK_HOME}/logs
result=`java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -cp "${NOTEBOOK_HOME}/lib/notebook-console.jar" -Dloader.main=io.kyligence.notebook.console.util.MysqlCheckCLI org.springframework.boot.loader.PropertiesLauncher 2>>${NOTEBOOK_HOME}/logs/shell.stderr`

if [[ -n `echo $result |grep ERROR` ]]; then
    quit "$result"
fi

if version_lt $result "5.7.0"; then
    quit "ERROR: Mysql 5.7.0 or above is required for Byzer-Notebook"
fi