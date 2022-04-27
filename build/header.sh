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

# source me

function isValidJavaVersion() {
    version=`java -version 2>&1 | awk -F\" '/version/ {print $2}'`
    version_first_part="$(echo ${version} | cut -d '.' -f1)"
    version_second_part="$(echo ${version} | cut -d '.' -f2)"

    if [[ "$version_first_part" -eq "1" ]] && [[ "$version_second_part" -lt "8" ]]; then
        echo "false"
        exit 0
    fi

    echo "true"
}

# avoid re-entering
if [[ "$dir" == "" ]]
then
    dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
    # misc functions
    function quit {
        echo "$@"
        if [[ -n "${QUIT_MESSAGE_LOG}" ]]; then
            echo `setColor 31 "$@"` >> ${QUIT_MESSAGE_LOG}
        fi
        exit 1
    }

    function verbose {
        if [[ -n "$verbose" ]]; then
            echo "$@"
        fi
    }

    function setColor() {
        echo -e "\033[$1m$2\033[0m"
    }

    function getValueByKey() {
        while read line
        do key=${line%=*} val=${line#*=}
        if [ "${key}" == "$1" ]; then
            echo $val
            break
        fi
        done<$2
    }

    # setup verbose
    verbose=${verbose:-""}
    while getopts ":v" opt; do
        case $opt in
            v)
                echo "Turn on verbose mode." >&2
                export verbose=true
                ;;
            \?)
                echo "Invalid option: -$OPTARG" >&2
                ;;
        esac
    done
    
    # set NOTEBOOK_HOME
    if [ -z $NOTEBOOK_HOME ];then
    export NOTEBOOK_HOME=$(cd -P -- "$(dirname -- "$0")"/../ && pwd -P)
    fi

    # set JAVA
    if [[ "${JAVA}" == "" ]]; then
        if [[ -z "$JAVA_HOME" ]]; then
            if [[ `isValidJavaVersion` == "true" ]]; then
                JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
            else
                quit "Java 1.8 or above is required."
            fi
            [[ -z "$JAVA_HOME" ]] && quit "Please set JAVA_HOME"
            export JAVA_HOME
        fi
        export JAVA=$JAVA_HOME/bin/java
        [[ -e "${JAVA}" ]] || quit "${JAVA} does not exist. Please set JAVA_HOME correctly."
        verbose "java is ${JAVA}" 
    fi

    # set BYZER NOTEBOOK IP
    if [ -z $BYZER_NOTEBOOK_IP ];then
    export BYZER_NOTEBOOK_IP=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)
    fi



    # set BYZER NOTEBOOK PORT
    export NOTEBOOK_PORT=$($NOTEBOOK_HOME/bin/get-properties.sh notebook.port)

    if [[ -z ${NOTEBOOK_PORT} ]]; then
        export NOTEBOOK_PORT=9002
    fi

    # check Machine
    unameOut="$(uname -s)"
    case "${unameOut}" in
        Linux*)     os=Linux;;
        Darwin*)    os=Mac;;
        CYGWIN*)    os=Cygwin;;
        MINGW*)     os=MinGw;;
        *)          os="UNKNOWN:${unameOut}"
    esac
    export MACHINE_OS=$os

fi
