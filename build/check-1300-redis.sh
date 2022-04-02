#!/bin/bash

#title=Checking Redis

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking Redis..."

mkdir -p ${NOTEBOOK_HOME}/logs
result=`java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -cp "${NOTEBOOK_HOME}/lib/notebook-console.jar" -Dloader.main=io.kyligence.notebook.console.util.RedisCheckCLI org.springframework.boot.loader.PropertiesLauncher 2>>${NOTEBOOK_HOME}/logs/shell.stderr`

if [[ -n `echo $result |grep ERROR` ]]; then
    quit "$result"
fi