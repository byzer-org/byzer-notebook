#!/bin/bash

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

#if [ -z $MAPR_HOME ];then
#    export MAPR_HOME="/opt/mapr"
#fi

#if [ -z ${kylin_hadoop_conf_dir} ]; then
#    export kylin_hadoop_conf_dir=$NOTEBOOK_HOME/hadoop_conf
#fi

#export KYLIN_KERBEROS_OPTS=""
#if [ -f ${NOTEBOOK_HOME}/conf/krb5.conf ];then
#    KYLIN_KERBEROS_OPTS="-Djava.security.krb5.conf=${NOTEBOOK_HOME}/conf/krb5.conf"
#fi

export SPARK_HOME=$NOTEBOOK_HOME/spark

if [[ -f ${NOTEBOOK_HOME}/conf/notebook-tools-log4j.xml ]]; then
    notebook_tools_log4j="file:${NOTEBOOK_HOME}/conf/notebook-tools-log4j.xml"
    else
    notebook_tools_log4j="file:${NOTEBOOK_HOME}/tool/conf/notebook-tools-log4j.xml"
fi

mkdir -p ${NOTEBOOK_HOME}/logs
#result=`java ${KYLIN_KERBEROS_OPTS} -Dlog4j.configurationFile=${notebook_tools_log4j} -Dkylin.hadoop.conf.dir=${kylin_hadoop_conf_dir} -Dhdp.version=current -cp "${NOTEBOOK_HOME}/lib/*" io.kyligence.notebook.console.util.NotebookConfigCLI $@ 2>>${NOTEBOOK_HOME}/logs/shell.stderr`
result=`java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -cp "${NOTEBOOK_HOME}/lib/notebook-console.jar" -Dloader.main=io.kyligence.notebook.console.util.NotebookConfigCLI -Dloader.args="$@" org.springframework.boot.loader.PropertiesLauncher 2>>${NOTEBOOK_HOME}/logs/shell.stderr`

echo "$result"