#!/bin/bash



source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh "$@"
mkdir -p ${NOTEBOOK_HOME}/logs
ERR_LOG=${NOTEBOOK_HOME}/logs/shell.stderr
OUT_LOG=${NOTEBOOK_HOME}/logs/shell.stdout
echo "-----------------------  log start  -----------------------" >>${ERR_LOG}
echo "-----------------------  log start  -----------------------" >>${OUT_LOG}
bash -x ${NOTEBOOK_HOME}/bin/bootstrap.sh "$@" 2>>${ERR_LOG}  | tee -a ${OUT_LOG}
ret=${PIPESTATUS[0]}
echo "-----------------------  log end  -------------------------" >>${ERR_LOG}
echo "-----------------------  log end  -------------------------" >>${OUT_LOG}
exit ${ret}