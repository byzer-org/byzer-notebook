#!/bin/bash

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
pid_file=${dir}/pid

function check_alive() {
    for ((i=1;i<=15;i+=1))
    do
        printf "."
        sleep 1
        killed_pid=$(ps -ef | grep $1 | grep -v grep | awk '{print $2}')
        if [[ -z ${killed_pid} ]]; then
            echo ""
            return 1
        fi
    done
    echo ""
    return 0
}

function check_after() {
    # check kill status
    check_alive "$1"
    status=$?
    if [[ ${status} -eq 1 ]]; then
        echo "Notebook service has been stopped, pid=$1."
        return
    else
        echo "Notebook service termination failed! The process [pid=$1] is about to be forcibly terminated."
    fi
    # force kill and check
    kill -9 "$1"
    check_alive "$1"
    status=$?
    if [[ ${status} -eq 1 ]]; then
        echo "Notebook service has been forcibly stopped, pid=$1."
    else
        echo -e "Notebook service didn't stopped completely. Please check the cause of the failure or contact IT support."
    fi
}

if [[ -e "${pid_file}" ]]
then
    pid=$(cat "${pid_file}")
    if [[ -n $(ps -ef | grep ${pid} | grep -v grep) ]]; then
        kill "${pid}"
    fi
    rm -f "${pid_file}"
    check_after "${pid}"
elif [[ -n `ps -ef | grep ${dir} | grep -v grep` ]]
then
    pid=$(ps -ef | grep "${dir}" | grep semantic | grep -v grep | awk '{print $2}')
    kill "${pid}"
    check_after "$pid"
else
    echo "There is no notebook service running."
fi
