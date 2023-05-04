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

#title=Checking Ports Availability

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

function checkRestPort() {
    echo "Checking rest port on ${MACHINE_OS}"
    if [[ $MACHINE_OS == "Linux" ]]; then
        used=$(netstat -tpln | grep -w "$NOTEBOOK_PORT" | awk '{print $7}' | sed "s/\// /g")
    elif [[ $MACHINE_OS == "Mac" ]]; then
        used=$(lsof -nP -iTCP:$NOTEBOOK_PORT -sTCP:LISTEN | grep $NOTEBOOK_PORT | awk '{print $2}')
    fi
    if [ ! -z "$used" ]; then
        quit "ERROR: Port ${NOTEBOOK_PORT} is in use, another Byzer Notebook is running?"
    fi
    echo "${NOTEBOOK_PORT} is available"
}



echo "Byzer Notebook port has been set as ${NOTEBOOK_PORT}"

checkRestPort ${NOTEBOOK_PORT}
