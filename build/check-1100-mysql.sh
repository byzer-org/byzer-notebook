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

#title=Checking MySQL Availability & Version

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
    quit "ERROR: Mysql 5.7.0 or above is required for Byzer Notebook"
fi