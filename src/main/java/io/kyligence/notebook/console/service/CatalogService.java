package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.model.FileInfo;
import io.kyligence.notebook.console.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    @Autowired
    private EngineService engineService;

    public List<FileInfo> listHdfsFiles(String path) {
        path = path == null ? "/" : path;
        String sql = "!hdfs -ls -F " + path + ";";

        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        List<FileInfo> files = JacksonUtils.readJsonArray(responseBody, FileInfo.class);

        if (files == null) {
            return null;
        }

        files = files.stream()
                .filter(item -> {
                    String permission = item.getPermission();
                    if (permission == null) {
                        return false;
                    }
                    return permission.startsWith("-") || permission.startsWith("d");
                })
                .collect(Collectors.toList());

        return files;
    }

    public List<String> listHiveDatabases() {
        String sql = "!profiler sql \"show databases\";";
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        List<Map> dbs = JacksonUtils.readJsonArray(responseBody, Map.class);

        if (dbs == null) {
            return null;
        }
        List<String> dbNames = dbs.stream()
                // adaptor for spark2 and spark3
                .map(item -> item.containsKey("databaseName") ? item.get("databaseName").toString() : item.get("namespace").toString())
                .collect(Collectors.toList());
        return dbNames;
    }

    public List<String> listHiveTables(String database) {
        String sql = "!profiler sql \"use " + database + "\";\n!profiler sql \"show tables\";";
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        List<Map> tables = JacksonUtils.readJsonArray(responseBody, Map.class);

        if (tables == null) {
            return null;
        }
        List<String> tableNames = tables.stream()
                .filter(item -> item.get("isTemporary").toString().equals("false"))
                .map(item -> item.get("tableName").toString())
                .collect(Collectors.toList());
        return tableNames;
    }


    public List<String> listDeltaDatabases() {
        List<Map> dbWithTables = listDeltaDbWithTables();

        if (dbWithTables == null) {
            return null;
        }
        List<String> dbNames = dbWithTables.stream()
                .filter(item -> !StringUtils.startsWithAny(item.get("database").toString(), ".","__"))
                .map(item -> item.get("database").toString())
                .distinct()
                .collect(Collectors.toList());
        return dbNames;
    }

    public List<String> listDeltaTables(String database) {
        List<Map> dbWithTables = listDeltaDbWithTables();
        List<String> tables = dbWithTables.stream()
                .filter(item -> item.get("database").toString().equals(database))
                .map(item -> item.get("table").toString())
                .distinct()
                .collect(Collectors.toList());
        return tables;
    }

    private List<Map> listDeltaDbWithTables() {
        String sql = "!delta show tables;";
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        List<Map> dbWithTables = JacksonUtils.readJsonArray(responseBody, Map.class);
        return dbWithTables;
    }

}
