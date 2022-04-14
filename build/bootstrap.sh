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

function clearCrontab() {
    if ! isCrontabUseable;then
        return 1
    fi
    logrotateDir=${NOTEBOOK_HOME}/logrotate
    if [ -f "${logrotateDir}/cron" ]; then
        rm -f ${logrotateDir}/cron
    fi
    touch ${logrotateDir}/cron
    crontab -l | while read line
    do
        if [[ "$line" == *${logrotateDir}/ke* ]];then
            continue
        fi
        echo "$line" >> ${logrotateDir}/cron
    done
    crontab ${logrotateDir}/cron
}

function isCrontabUseable() {
    crontab -l >/dev/null 2>&1 || (echo "This user don't have permission to run crontab." && return 1)
}

function setLogRotate() {
    auto_log_rotate_enabled=`$NOTEBOOK_HOME/bin/get-properties.sh notebook.env.log-rotate-enabled`
    # linux
    if [ -d "/etc/logrotate.d" -a ${auto_log_rotate_enabled} == "true" ] && isCrontabUseable; then
        $NOTEBOOK_HOME/bin/log-rotate-cron.sh
    else
        $NOTEBOOK_HOME/bin/rotate-logs.sh
    fi
}

function recordStartOrStop() {
    currentIp=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)
    serverPort=`$NOTEBOOK_HOME/bin/get-properties.sh notebook.port`
    echo `date '+%Y-%m-%d %H:%M:%S '`"INFO : [Operation: $1] user:`whoami`, start time:$2, ip and port:${currentIp}:${serverPort}" >> ${NOTEBOOK_HOME}/logs/security.log
}

function prepareEnv {
    export NOTEBOOK_CONFIG_FILE="${NOTEBOOK_HOME}/conf/notebook.properties"

    echo "NOTEBOOK_HOME is:${NOTEBOOK_HOME}"
    echo "NOTEBOOK_CONFIG_FILE is:${NOTEBOOK_CONFIG_FILE}"

    mkdir -p ${NOTEBOOK_HOME}/logs
}

function checkRestPort() {
    if [[ $MACHINE_OS == "Linux" ]]; then
        used=`netstat -tpln | grep "\<$port\>" | awk '{print $7}' | sed "s/\// /g"`
    fi
    if [[ $MACHINE_OS == "Mac" ]]; then
        used=`lsof -nP -iTCP:$port -sTCP:LISTEN | grep $port | awk '{print $2}'`
    fi
    if [ ! -z "$used" ]; then
        echo "<$used> already listen on $port"
        exit -1
    fi
}

function checkIfStopUserSameAsStartUser() {
    startUser=`ps -p $1 -o user=`
    currentUser=`whoami`

    if [ ${startUser} != ${currentUser} ]; then
        echo `setColor 33 "Warning: You started Byzer Notebook as user [${startUser}], please stop the instance as the same user."`
    fi
}

function clearRedundantProcess {
    if [ -f "${NOTEBOOK_HOME}/pid" ]
    then
        pidKeep=0
        pidRedundant=0
        for pid in `cat ${NOTEBOOK_HOME}/pid`
        do
            pidActive=`ps -ef | grep $pid | grep ${NOTEBOOK_HOME} | wc -l`
            if [ "$pidActive" -eq 1 ]
            then
                if [ "$pidKeep" -eq 0 ]
                then
                    pidKeep=$pid
                else
                    echo "Redundant Byzer Notebook process $pid to running process $pidKeep, stop it."
                    bash ${NOTEBOOK_HOME}/bin/kill-process-tree.sh $pid
                    ((pidRedundant+=1))
                fi
            fi
        done
        if [ "$pidKeep" -ne 0 ]
        then
            echo $pidKeep > ${NOTEBOOK_HOME}/pid
        else
            rm ${NOTEBOOK_HOME}/pid
        fi
        if [ "$pidRedundant" -ne 0 ]
        then
            quit "Byzer Notebook is redundant, start canceled."
        fi
    fi
}

function start(){
    setLogRotate
    clearRedundantProcess

    if [ -f "${NOTEBOOK_HOME}/pid" ]; then
        PID=`cat ${NOTEBOOK_HOME}/pid`
        if ps -p $PID > /dev/null; then
          quit "Notebook is running, stop it first, PID is $PID"
        fi
    fi

    ${NOTEBOOK_HOME}/bin/check-env.sh "if-not-yet" || exit 1

    START_TIME=$(date "+%Y-%m-%d %H:%M:%S")

    recordStartOrStop "start" "${START_TIME}"

    prepareEnv

    port=`$NOTEBOOK_HOME/bin/get-properties.sh notebook.port`
    if [[ -f ${NOTEBOOK_HOME}/bin/check-env-bypass ]]; then
        checkRestPort
    fi

    nohup java -DNOTEBOOK_HOME=${NOTEBOOK_HOME} -Dspring.config.name=application,notebook -Dspring.config.location=classpath:/,file:${NOTEBOOK_HOME}/conf/ -jar ${NOTEBOOK_HOME}/lib/notebook-console.jar >> ${NOTEBOOK_HOME}/logs/notebook.out 2>&1 < /dev/null & echo $! >> ${NOTEBOOK_HOME}/pid &
    sleep 3
    clearRedundantProcess

    PID=`cat ${NOTEBOOK_HOME}/pid`
    CUR_DATE=$(date "+%Y-%m-%d %H:%M:%S")
    echo $CUR_DATE" new Byzer Notebook process pid is "$PID >> ${NOTEBOOK_HOME}/logs/notebook.log

    echo "Byzer Notebook is starting. It may take a while. For status, please visit http://`hostname`:$port."
    echo "You may also check status via: PID:`cat ${NOTEBOOK_HOME}/pid`, or Log: ${NOTEBOOK_HOME}/logs/notebook.log."
    recordStartOrStop "start success" "${START_TIME}"
}

function stop(){
    clearCrontab

    STOP_TIME=$(date "+%Y-%m-%d %H:%M:%S")
    if [ -f "${NOTEBOOK_HOME}/pid" ]; then
        PID=`cat ${NOTEBOOK_HOME}/pid`
        if ps -p $PID > /dev/null; then

           checkIfStopUserSameAsStartUser $PID

           echo `date '+%Y-%m-%d %H:%M:%S '`"Stopping Byzer Notebook: $PID"
           kill $PID
           for i in {1..10}; do
              sleep 3
              if ps -p $PID -f | grep notebook > /dev/null; then
                echo "loop $i"
                 if [ "$i" == "10" ]; then
                    echo `date '+%Y-%m-%d %H:%M:%S '`"Killing Notebook: $PID"
                    kill -9 $PID
                 fi
                 continue
              fi
              break
           done
           rm ${NOTEBOOK_HOME}/pid

           recordStartOrStop "stop" "${STOP_TIME}"
           return 0
        else
           return 1
        fi

    else
        return 1
    fi
}

# start command
if [ "$1" == "start" ]; then
    echo "Starting Byzer Notebook..."
    start
# stop command
elif [ "$1" == "stop" ]; then
    echo `date '+%Y-%m-%d %H:%M:%S '`"Stopping Byzer Notebook..."
    stop
    if [[ $? == 0 ]]; then
        exit 0
    else
        quit "Byzer Notebook is not running"
    fi
# restart command
elif [ "$1" == "restart" ]; then
    echo "Restarting Byzer Notebook..."
    echo "--> Stopping Byzer Notebook first if it's running..."
    stop
    if [[ $? != 0 ]]; then
        echo "    Byzer Notebook is not running, now start it"
    fi
    echo "--> Starting Byzer Notebook..."
    start
else
    quit "Usage: 'notebook.sh [-v] start' or 'notebook.sh [-v] stop' or 'notebook.sh [-v] restart'"
fi