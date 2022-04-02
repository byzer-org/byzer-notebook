#!/bin/bash

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh $@

function checkFileOccupied() {
    target_file=$1
    pids="`fuser $target_file 2>&1`"
    if [[ "${pids}" == "" ]]; then
        echo false
    else
        echo true
    fi
}

function checkSizeExceedLimit() {
    target_file=$1
    file_threshold=`${NOTEBOOK_HOME}/bin/get-properties.sh notebook.env.max-keep-log-file-threshold-mb`
    file_size=`du -b "$target_file" | cut -f 1`
    let file_threshold=file_threshold*1024*1024
    if [[ ${file_size} -gt ${file_threshold} ]]; then
        echo true
    else
        echo false
    fi
}

function logRotate() {
    target_file=$1
    # keep 10 history log files
    keep_limit=`${NOTEBOOK_HOME}/bin/get-properties.sh notebook.env.max-keep-log-file-number`

    is_occupied=`checkFileOccupied ${target_file}`
    if [[ "${is_occupied}" == "true" ]]; then
        return
    fi

    is_too_large=`checkSizeExceedLimit ${target_file}`
    if [[ "${is_too_large}" == "false" ]]; then
        return
    fi

    if [[ -f $target_file ]]; then
        if [[ -f ${target_file}.${keep_limit} ]]; then
            # clean oldest log file first
            rm -f ${target_file}.${keep_limit}
        fi

        let p_cnt=keep_limit-1
        # renames logs .1 trough .${keep_limit}
        while [[ $keep_limit -gt 1 ]]; do
            if [ -f ${target_file}.${p_cnt} ] ; then
                mv -f ${target_file}.${p_cnt} ${target_file}.${keep_limit}
            fi
            let keep_limit=keep_limit-1
            let p_cnt=p_cnt-1
        done

        # rename current log to .1
        mv -f $target_file $target_file.1
    fi
}

ERR_LOG=${NOTEBOOK_HOME}/logs/shell.stderr
OUT_LOG=${NOTEBOOK_HOME}/logs/shell.stdout
NOTEBOOK_OUT=${NOTEBOOK_HOME}/logs/notebook.out

if [ "$1" == "start" ] || [ "$1" == "spawn" ]
then
    logRotate $ERR_LOG
    logRotate $OUT_LOG
    logRotate $NOTEBOOK_OUT
fi