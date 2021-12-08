# Byzer-Notebook


## Requirements

1. jdk8
2. scala （recommand 2.11.8）
3. maven

## Build
* If you already downloaded project `byzer-notebook-vue`, specify the directory of project using:
```shell
export BYZER_NOTEBOOK_VUE_HOME=/path/of/byzer-notebook-vue
```
Otherwise, use ${project.dir}/../byzer-notebook-vue as default.

* If you want to update `byzer-notebook-vue` to the latest version, just set `UPDATE_NOTEBOOK_VUE` to `true`
```shell
export UPDATE_NOTEBOOK_VUE=true
```

* Execute the script
```shell
./build/build.sh
```

## VM Options

```
-DNOTEBOOK_HOME=[YOUR PROJECT ROOT PATH]
-Dspring.config.name=application,notebook
-Dspring.config.location=classpath:/,file:./conf/
-Djava.io.tmpdir=./tmp
```

## Database Configuration

1. Rename notebook.example.properties to notebook.properties.
2. Create Database called `notebook`


## Main class

```
io.kyligence.notebook.console.NotebookLauncher
```

Now you can start Byzer-Notebook.

## Web Console Support

1. Clone https://github.com/byzer-org/byzer-notebook-vue.git
2. npm install && npm run build  (npm version v14.18.1 is tested)
3. Copy the byzer-notebook-vu disk/* to src/main/resources/static/

```
cp -r disk/* src/main/resources/static/
```

Try to visit http://127.0.0.1:9002
