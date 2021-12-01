#!/usr/bin/env bash

set -e
is_skip_tar="${1:-}"
# Notebook absolute path
root_dir=$(cd -P -- "$(dirname -- "$0")/.." && pwd -P)
if [[ -n "${BYZER_NOTEBOOK_HOME}" ]]; then
   root_dir=${BYZER_NOTEBOOK_HOME}
fi

cd ${root_dir} && echo ${root_dir}

if [[ ! -d byzer-notebook-vue/.git ]]; then
    echo "cloning byzer-notebook-vue repo..."
    # build front
    git clone https://github.com/byzer-org/byzer-notebook-vue.git
else
    echo "update byzer-notebook-vue to latest..."
    ( cd byzer-notebook-vue && git checkout main && git pull -r origin main )
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

mvn clean install -DskipTests -Ptest

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

## 3. make version file
echo "${version}" > VERSION


## 4. copy console jar file
cp ${root_dir}/target/notebook-console.jar .

## 5. copy config
mkdir conf
cp ${root_dir}/conf/notebook.properties.example conf/notebook.properties

## 6. copy scripts
cp ${root_dir}/build/startup.sh .
cp ${root_dir}/build/shutdown.sh .

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