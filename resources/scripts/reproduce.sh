#!/bin/bash

_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

_DOWNLOADS_DIR=${_DIR}/../../_downloads
DEFINER_LOG_DIR=${_DIR}/../file-level/output/definer
DEFINER_LEARNING_LOG_DIR=${_DIR}/../file-level/output/definer-learning
DEFINER_BASIC_LOG_DIR=${_DIR}/../file-level/output/definer-basic
TEMP_FILES_DIR=${_DIR}/../file-level/temp-files
TEMP_CONFIGS_DIR=${_DIR}/../file-level/temp-configs

# create dirs to store logs and temp files
mkdir -p ${_DOWNLOADS_DIR}
mkdir -p ${DEFINER_LOG_DIR}
mkdir -p ${DEFINER_LEARNING_LOG_DIR}
mkdir -p ${DEFINER_BASIC_LOG_DIR}
mkdir -p ${TEMP_FILES_DIR}
mkdir -p ${TEMP_CONFIGS_DIR}

# clone projects
if [ ! -d "${_DOWNLOADS_DIR}/commons-compress" ]; then
    (
        cd ${_DOWNLOADS_DIR}
        git clone https://github.com/MSR-2017/commons-compress
    )
fi
if [ ! -d "${_DOWNLOADS_DIR}/commons-net" ]; then
    (
        cd ${_DOWNLOADS_DIR}
        git clone https://github.com/MSR-2017/commons-net
    )
fi

# show config file path
echo "Config file is: ${_DIR}/../file-level/definer-configs/${1}.properties"
# show log files path
echo "Definer log file is: ${_DIR}/../file-level/output/definer/${1}.log"
echo "Definer-basic file is: ${_DIR}/../file-level/output/definer-basic/${1}.log"

# run examples
cd ${_DIR}
# run definer
./run_examples.py --definer-one "${1}"
# run basic
./run_examples.py --definer-asej-exp-one basic --example "${1}"
