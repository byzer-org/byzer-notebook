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

notebook_port=`$NOTEBOOK_HOME/bin/get-properties.sh notebook.port`
if [[ -z ${notebook_port} ]]; then
    notebook_port=9002
fi
if [[ $MACHINE_OS == "Linux" ]]; then
    notebook_port_in_use=`netstat -anvp tcp | grep "\b${notebook_port}\b"`
fi
if [[ $MACHINE_OS == "Mac" ]]; then
    notebook_port_in_use=`lsof -nP -iTCP:${notebook_port} -sTCP:LISTEN`
fi

[[ -z ${notebook_port_in_use} ]] || quit "ERROR: Port ${notebook_port} is in use, another Byzer Notebook is running?"
