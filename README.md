# Byzer-Notebook


## Requirements

1. jdk8
2. maven
3. scala (recommend 2.11.8)
4. npm (recommend 14.x.x)
5. Byzer-lang (startup first)

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

Test workflow2
