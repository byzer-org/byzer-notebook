#!/bin/bash



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
    # required because the entry shell can be $NOTEBOOK_HOME/kybot/kybot.sh; must change /kybot to /bin
#    dir=$(cd "$dir" && pwd -P)

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
    
    # set NOTEBOOK_HOME with consideration for multiple instances that are on the same node
    CURRENT=`cd "${dir}/../"; pwd`
    if [[ "$CI_MODE" != "true" ]]; then
        [[ -z "$KYLIN_CONF" ]] || quit "KYLIN_CONF should not be set. Please leave it NULL, i.e. 'export KYLIN_CONF='"
        [[ -z "$NOTEBOOK_HOME" ]] || [[ "${CURRENT}" == "${NOTEBOOK_HOME}" ]] || quit "NOTEBOOK_HOME=${NOTEBOOK_HOME}, doesn't set correctly, please make sure it is set as current dir: ${CURRENT}, or leave it NULL, i.e. 'export NOTEBOOK_HOME='"
    fi
    # have a check to avoid repeating verbose message
    if [[ "${NOTEBOOK_HOME}" != "${CURRENT}" ]]; then
        export NOTEBOOK_HOME=${CURRENT}
        verbose "NOTEBOOK_HOME is ${NOTEBOOK_HOME}"
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
