#!/bin/bash

dir=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
pid=${dir}/pid

export NOTEBOOK_HOME=${dir}
echo "NOTEBOOK_HOME="${NOTEBOOK_HOME}

HANG_UP_FLAG=${1:-nohup}
if [[ "${HANG_UP_FLAG}" == "nohup" ]]; then
  echo "====================================="
  echo "Nohup startup byzer notebook."
  nohup java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -Dspring.config.name=application,notebook -Dspring.config.location=classpath:/,file:${NOTEBOOK_HOME}/conf/ -jar ${NOTEBOOK_HOME}/notebook-console.jar > ${NOTEBOOK_HOME}/logs/notebook.log 2>&1 &
  echo "$!" > ${pid}
  echo "success"
elif [[ "${HANG_UP_FLAG}" == "hangup" ]];then
  echo "====================================="
  echo "startup byzer notebook."
  java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -Dspring.config.name=application,notebook -Dspring.config.location=classpath:/,file:${NOTEBOOK_HOME}/conf/ -jar ${NOTEBOOK_HOME}/notebook-console.jar
fi