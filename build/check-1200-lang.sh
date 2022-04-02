#!/bin/bash

#title=Checking Byzer-lang

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking Byzer-lang..."
Byzer_lang_address="`${NOTEBOOK_HOME}/bin/get-properties.sh notebook.mlsql.engine-url`/health/liveness"
http_code=000
status="false"
for i in {1..3} ; do
    sleep 1
    echo "Try to connect Byzer-lang, address : ${Byzer_lang_address}"

    http_code=`curl -I -m 10 -o /dev/null -s -w %{http_code} ${Byzer_lang_address}`
    if [[ "${http_code}" -eq 200 ]]; then
      status="true"
      echo "Success: connect to Byzer-lang"
      break;
    fi
done

if [[ "${status}" == "false" ]]; then
  quit "Error: cannot connect to Byzer-lang, please start up Byzer-lang first."
fi
