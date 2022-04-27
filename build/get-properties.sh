#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ $# != 1 ]
then
    if [[ $# < 2 || $2 != 'DEC' ]]
        then
            echo 'invalid input'
            exit 1
    fi
fi

if [ -z $NOTEBOOK_HOME ];then
    export NOTEBOOK_HOME=$(cd -P -- "$(dirname -- "$0")"/../ && pwd -P)
fi

if [[ -f ${NOTEBOOK_HOME}/conf/notebook-tools-log4j.xml ]]; then
    notebook_tools_log4j="file:${NOTEBOOK_HOME}/conf/notebook-tools-log4j.xml"
    else
    notebook_tools_log4j="file:${NOTEBOOK_HOME}/tool/conf/notebook-tools-log4j.xml"
fi

mkdir -p ${NOTEBOOK_HOME}/logs
result=`java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -cp "${NOTEBOOK_HOME}/lib/notebook-console.jar" -Dloader.main=io.kyligence.notebook.console.util.NotebookConfigCLI -Dloader.args="$@" org.springframework.boot.loader.PropertiesLauncher 2>>${NOTEBOOK_HOME}/logs/shell.stderr`

echo "$result"