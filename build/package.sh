#!/usr/bin/env bash

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

set -e
is_skip_tar="${1:-}"

# Notebook absolute path
root_dir=$(cd -P -- "$(dirname -- "$0")/.." && pwd -P)
if [[ -n "${BYZER_NOTEBOOK_HOME}" ]]; then
   root_dir=${BYZER_NOTEBOOK_HOME}
fi

echo "frontend branch set to [${FRONTEND_BRANCH:-"main"}]"

cd ${root_dir} && echo ${root_dir}

if [[ ! -d byzer-notebook-vue/.git ]]; then
    echo "cloning byzer-notebook-vue repo..."
    if [[ -d byzer-notebook-vue ]]; then
      rm -rf ./byzer-notebook-vue
    fi
    # build front
    git clone -b "${FRONTEND_BRANCH:-"main"}" https://gitee.com/allwefantasy/byzer-notebook-vue.git
else
    echo "update byzer-notebook-vue to latest..."
    ( cd byzer-notebook-vue && git reset --hard && git checkout main && git pull -r origin main )
fi

cd ${root_dir}/byzer-notebook-vue && bash ./build/build.sh

console_static_resources_dir=${root_dir}/src/main/resources/static &&
echo ${console_static_resources_dir}
[[ ! -d ${console_static_resources_dir} ]] && mkdir -p ${console_static_resources_dir} || rm -rf "${console_static_resources_dir:?}"/*

tar -xvf ${root_dir}/byzer-notebook-vue/build/*.tar.gz -C ${console_static_resources_dir}

cd ${root_dir}

## build console fat jar
mvn_version=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
if [[ -z "${version}" ]]; then
    export version=${mvn_version}
fi

[[ -z "${mvn_profile}" ]] && profile='test' || profile=${mvn_profile}

echo "Maven Package Profile : ${profile}"

mvn clean install -DskipTests -P${profile}

# collect release package resource
cd ${root_dir}
export package_name=Byzer-Notebook-${version}

[[ ! -d "dist" ]] && mkdir -p "dist" || rm -rf dist/*
mkdir -p dist/${package_name}

cd dist/${package_name}

## 1. make changelog file
if [[ -z "${changelog}" ]]; then
    echo "changelog not set, use UNKNOWN instead."
    changelog="UNKNOWN"
fi

echo "${changelog}" > ./CHANGELOG.md

## 2. make commit_sha file
echo `git rev-parse HEAD` | tee commit_SHA1
cd ${root_dir}/byzer-notebook-vue && echo `git rev-parse HEAD` | tee ${root_dir}/dist/${package_name}/commit_SHA1.frontend
cd ${root_dir}/dist/${package_name}

## 3. make version file
echo "${version}" > VERSION


## 4. copy console jar file
mkdir lib
cp ${root_dir}/target/notebook-console.jar lib

## 5. copy config
mkdir conf
cp ${root_dir}/conf/notebook.properties.example conf/notebook.properties

## 6. copy scripts
mkdir bin
cp ${root_dir}/build/* bin

## 7. others
mkdir logs
cp -r ${root_dir}/sample .

cd ${root_dir}/dist

if [[ "${is_skip_tar}" == "skipTar" ]]; then
  echo "====================================="
  echo "Build Finished!"
  echo "Location: ${root_dir}/dist/${package_name}"
else
  tar -zcvf ${package_name}.tar.gz ${package_name}
  echo "====================================="
  echo "Build Finished!"
  echo "Location: ${root_dir}/dist/${package_name}.tar.gz"
fi