# Byzer-Notebook


## Requirements

1. jdk8
2. scala （recommand 2.11.8）
3. maven

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

**Admin Account**: admin/admin
