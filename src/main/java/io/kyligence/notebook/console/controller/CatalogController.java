package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.FileListDTO;
import io.kyligence.notebook.console.bean.dto.NameListDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.bean.model.FileInfo;
import io.kyligence.notebook.console.service.CatalogService;
import io.kyligence.notebook.console.support.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/catalog")
@Api("The documentation about operations on catalog")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @ApiOperation("list hdfs files")
    @GetMapping("/hdfs/file_list")
    @Permission
    public Response<FileListDTO> listHdfsFiles(@RequestParam(value = "path", required = false) String path) {
        List<FileInfo> files = catalogService.listHdfsFiles(path);
        return new Response<FileListDTO>().data(FileListDTO.valueOf(files));
    }


    @ApiOperation("list databases")
    @GetMapping("/{dbType}/databases")
    @Permission
    public Response<NameListDTO> listDatabases(@PathVariable("dbType") @NotNull String dbType) {
        List<String> databases = new ArrayList<>();

        if (dbType.equals("hive")) {
            databases = catalogService.listHiveDatabases();
        }

        if (dbType.equals("delta")) {
            databases = catalogService.listDeltaDatabases();
        }

        return new Response<NameListDTO>().data(NameListDTO.valueOf(databases));
    }

    @ApiOperation("list databases")
    @GetMapping("/{dbType}/{database}/tables")
    @Permission
    public Response<NameListDTO> listTables(@PathVariable("dbType") @NotNull String dbType,
                                            @PathVariable("database") @NotNull String database) {
        List<String> tables = new ArrayList<>();

        if (dbType.equals("hive")) {
            tables = catalogService.listHiveTables(database);
        }

        if (dbType.equals("delta")) {
            tables = catalogService.listDeltaTables(database);
        }

        return new Response<NameListDTO>().data(NameListDTO.valueOf(tables));
    }

}
