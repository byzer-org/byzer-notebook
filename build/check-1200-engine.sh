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

#title=Checking Byzer engine

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking Byzer engine..."
Byzer_engine_url="`${NOTEBOOK_HOME}/bin/get-properties.sh notebook.mlsql.engine-url`"
check_api="${Byzer_engine_url}/health/liveness"


http_code=000
status="false"
for i in {1..3} ; do
    sleep 1
    echo "Try to connect Byzer-lang, address : ${Byzer_engine_url}"

    http_code=`curl -I -m 10 -o /dev/null -s -w %{http_code} ${check_api}`
    if [[ "${http_code}" -eq 200 ]]; then
      status="true"
      echo "Byzer engine connected."
      break;
    fi
done

if [[ "${status}" == "false" ]]; then
  quit "Error: Unable to connect to Byzer engine, please make sure ${byzer-engine-address} is available."
fi
