#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

pid=${dir}/pid

export NOTEBOOK_HOME=${dir}
echo "NOTEBOOK_HOME="${NOTEBOOK_HOME}

nohup java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -Dspring.config.name=application,notebook -Dspring.config.location=classpath:/,file:${NOTEBOOK_HOME}/conf/ -jar notebook-console.jar > logs/notebook.log 2>&1 &

echo "$!" > ${pid}
echo "success"