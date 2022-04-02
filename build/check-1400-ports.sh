#!/bin/bash

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
