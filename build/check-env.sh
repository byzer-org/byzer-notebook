#!/bin/bash

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh
if [ "$1" == "-v" ]; then
    shift
fi

mkdir -p ${NOTEBOOK_HOME}/logs

REDIS_ENABLE=`$NOTEBOOK_HOME/bin/get-properties.sh notebook.env.redis-enable`
export NOTEBOOK_CONFIG_FILE="${NOTEBOOK_HOME}/conf/notebook.properties"

# avoid re-entering
if [[ "$CHECKENV_ING" == "" ]]; then
    export CHECKENV_ING=true

    mkdir -p ${NOTEBOOK_HOME}/logs
    LOG=${NOTEBOOK_HOME}/logs/check-env.out
    ERRORS=${NOTEBOOK_HOME}/logs/check-env.error
    BYPASS=${NOTEBOOK_HOME}/bin/check-env-bypass
    TITLE="#title"

    if [[ "$1" != "if-not-yet" || ! -f ${BYPASS} ]]; then

        echo ""
        echo `setColor 33 "Byzer Notebook is checking installation environment, log is at ${LOG}"`
        echo ""

        rm -rf ${NOTEBOOK_HOME}/logs/tmp
        rm -f ${ERRORS}
        touch ${ERRORS}

        export CHECKENV_REPORT_PFX=">   "
        export QUIT_MESSAGE_LOG=${ERRORS}


#        CHECK_FILES=
        if [[ ${REDIS_ENABLE} == "true" ]]; then
            CHECK_FILES=`ls ${NOTEBOOK_HOME}/bin/check-*.sh`
        else
          declare array CHECK_FILES=("${NOTEBOOK_HOME}/bin/check-1000-java.sh"
                         "${NOTEBOOK_HOME}/bin/check-1100-mysql.sh"
                         "${NOTEBOOK_HOME}/bin/check-1200-lang.sh"
                         "${NOTEBOOK_HOME}/bin/check-1400-ports.sh")
        fi
        for f in ${CHECK_FILES[@]}
        do
            if [[ ! $f == *check-env.sh ]]; then
                echo `getValueByKey ${TITLE} ${f}`
                echo ""                                                                             >>${LOG}
                echo "============================================================================" >>${LOG}
                echo "Checking $(basename $f)"                                                      >>${LOG}
                echo "----------------------------------------------------------------------------" >>${LOG}
                bash $f >>${LOG} 2>&1
                rtn=$?
                if [[ $rtn == 0 ]]; then
                    echo "...................................................[`setColor 32 PASS`]"
                elif [[ $rtn == 3 ]]; then
                    echo "...................................................[`setColor 33 SKIP`]"
                elif [[ $rtn == 4 ]];then
                    echo "...................................................[`setColor 33 WARN`]"
                    WARN_INFO=`tail -n 3 ${LOG}`
                    echo `setColor 33 "WARNING:"`
                    echo -e "$WARN_INFO"  | sed 's/^/    &/g'
                else
                    echo "...................................................[`setColor 31 FAIL`]"
                    cat  ${ERRORS} >> ${LOG}
                    tail ${ERRORS}
                    echo `setColor 33 "Full log is at: ${LOG}"`
                    exit 1
                fi
            fi
        done
        echo ""
        cat ${LOG} | grep "^${CHECKENV_REPORT_PFX}"
        touch ${BYPASS}
        echo `setColor 33 "Checking environment finished successfully. To check again, run 'bin/check-env.sh' manually."`
        echo ""
    fi

    export CHECKENV_ING=
fi