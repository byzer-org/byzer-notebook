#!/bin/bash

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh $@

function addCrontab() {
    logrotateCmd="${cronExpress} /usr/sbin/logrotate -s ${logrotateDir}/status ${logrotateDir}/notebook > /dev/null 2>&1"
    crontab -l | while read line
    do
        if [[ "$line" == *${logrotateDir}/notebook* ]];then
            continue
        fi
        echo "$line" >> ${logrotateDir}/cron
    done
    echo "$logrotateCmd" >> ${logrotateDir}/cron
    crontab ${logrotateDir}/cron
}

function rmCronConf() {
    if [ -f "${logrotateDir}/cron" ]; then
        rm -f ${logrotateDir}/cron
    fi
}

function creatConf(){
  cat > ${logrotateDir}/notebook <<EOL
${ERR_LOG} ${OUT_LOG} ${NOTEBOOK_OUT}  {
size ${file_threshold}M
rotate ${keep_limit}
missingok
copytruncate
nodateext
}
EOL
}

ERR_LOG=${NOTEBOOK_HOME}/logs/shell.stderr
OUT_LOG=${NOTEBOOK_HOME}/logs/shell.stdout
NOTEBOOK_OUT=${NOTEBOOK_HOME}/logs/notebook.out
logrotateDir=${NOTEBOOK_HOME}/logrotate

file_threshold=`${dir}/get-properties.sh notebook.env.max-keep-log-file-threshold-mb`
keep_limit=`${dir}/get-properties.sh notebook.env.max-keep-log-file-number`
cronExpress=`${dir}/get-properties.sh notebook.env.log-rotate-check-cron`

if [ ! -d "$logrotateDir" ]; then
    mkdir $logrotateDir
fi
creatConf
rmCronConf
addCrontab
