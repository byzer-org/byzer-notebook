package io.kyligence.notebook.console.controller;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.bean.dto.ScriptExecutionResp;
import io.kyligence.notebook.console.bean.dto.req.ScriptExecutionReq;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.service.EngineService;
import io.kyligence.notebook.console.service.FolderService;
import io.kyligence.notebook.console.service.JobService;
import io.kyligence.notebook.console.service.NotebookService;
import io.kyligence.notebook.console.support.IncludePathParser;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on script")
public class ScriptController {

    private final NotebookConfig config = NotebookConfig.getInstance();

    @Autowired
    private JobService jobService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private EngineService engineService;

    @Autowired
    private FolderService folderService;

    @ApiOperation("Execute Script")
    @PostMapping("/script/execution")
    @Permission
    public Response<ScriptExecutionResp> executeScript(@RequestBody @Validated ScriptExecutionReq scriptExecutionReq) {
        String user = WebUtils.getCurrentLoginUser();

        JobInfo jobInfo = new JobInfo();
        // TODO consider cluster mode, uuid maybe duplicate
        jobInfo.setJobId(UUID.randomUUID().toString());
        jobInfo.setContent(scriptExecutionReq.getSql());
        jobInfo.setStatus(JobInfo.JobStatus.RUNNING);
        jobInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        jobInfo.setName(user + "_untiled_202001011314121");
        jobInfo.setUser(user);

        String notebook = scriptExecutionReq.getNotebook();
        notebook = StringUtils.isEmpty(notebook) ? "untitled" : notebook;
        jobInfo.setNotebook(notebook);

        String engine = engineService.getExecutionEngine();
        jobInfo.setEngine(engine);

        jobService.insert(jobInfo);

        // 发送查询
        try {
            engineService.runScript(
                    new EngineService.RunScriptParams()
                            .withJobName(jobInfo.getJobId())
                            .withIncludeSchema(true)
                            .withLimit(config.getOutputSize())
                            .withSql(jobInfo.getContent()));
        } catch (Exception ex) {
            // update job status to FAILED if exception happened
            jobInfo.setFinishTime(new Timestamp(System.currentTimeMillis()));
            jobInfo.setStatus(JobInfo.JobStatus.FAILED);
            jobService.updateByJobId(jobInfo);
            throw ex;
        }

        // 保存 Cell 运行的 Job
        Integer cellId = scriptExecutionReq.getCellId();
        if (cellId != null) {
            CellInfo cellInfo = notebookService.getCellInfo(cellId);
            if (cellInfo != null) {
                cellInfo.setLastJobId(jobInfo.getJobId());
                cellInfo.setId(cellId);
                notebookService.updateCellJobId(cellInfo);
            }
        }


        ScriptExecutionResp resp = new ScriptExecutionResp();
        resp.setJobId(jobInfo.getJobId());

        return new Response<ScriptExecutionResp>().code("000").data(resp);
    }

    @ApiOperation("Get Script Content")
    @PostMapping("/script/include")
    public String executeScript(@RequestParam(value = "owner") String owner,
                                                       @RequestParam(value = "path") String path) {

        IncludePathParser.IncludePath includePath = IncludePathParser.parse(path);

        if (includePath == null) {
            return "include path can not be resolved.";
        }
        Integer folderId = null;
        if (includePath.getFolder() != null) {
            NotebookFolder folder = folderService.findFolder(owner, includePath.getFolder());
            if (folder == null) {
                return "can not find folder";
            }
            folderId = folder.getId();
        }

        NotebookInfo notebookInfo = notebookService.find(owner, includePath.getNotebook(), folderId);
        if (notebookInfo == null) {
            return "can not find notebook.";
        }
        List<CellInfo> cellInfoList = notebookService.getCellInfos(notebookInfo.getCellList());
        if (includePath.getCellId() != null) {
            if (cellInfoList == null || cellInfoList.size() < includePath.getCellId()) {
                return "can not find cell id.";
            }
            return cellInfoList.get(includePath.getCellId() - 1).getContent();
        }
        StringBuilder buff = new StringBuilder();
        for (CellInfo cellInfo : cellInfoList) {
            buff.append(cellInfo.getContent());
            buff.append("\n");
        }

        return buff.toString();
    }

}
