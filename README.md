![CI](https://github.com/byzer-org/byzer-notebook/actions/workflows/build.yml/badge.svg)   [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)   [![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=byzer-org_byzer-notebook&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=byzer-org_byzer-notebook)

# Byzer-Notebook

## Requirements

1. jdk8
2. maven
3. scala (recommend 2.11.8)
4. node (recommend 14.x.x)
5. npm (recommend 6.x.x)
6. python (recommend 3.10.x, must < 3.11)
7. Byzer-lang (startup first)

## Build with Byzer-Notebook-Vue
* Execute the script
```shell
./build/package.sh
```
The packaged file will be placed in `./dist/Byzer-Notebook-<version>.tar.gz`

> version is defined in pom.xml

## VM Options

```
-DNOTEBOOK_HOME=[YOUR PROJECT ROOT PATH]
-Dspring.config.name=application,notebook
-Dspring.config.location=classpath:/,file:./conf/
-Djava.io.tmpdir=./tmp
```

## Database Configuration

1. Rename notebook.example.properties to notebook.properties. (IDE startup)
2. Create Database called `notebook`


## Main class

```
io.kyligence.notebook.console.NotebookLauncher
```

Now you can start Byzer-Notebook.

## Web Console Support (IDE startup)

1. Clone https://github.com/byzer-org/byzer-notebook-vue.git
2. npm install && npm run build  (npm version v14.18.1 is tested)
3. Copy the byzer-notebook-vue dist/* to src/main/resources/static/

```
cp -r dist/* src/main/resources/static/
```

Try to visit http://127.0.0.1:9002 

**Admin Account**: admin/admin
