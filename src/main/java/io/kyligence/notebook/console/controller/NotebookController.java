package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.dto.req.CodeSuggestionReq;
import io.kyligence.notebook.console.bean.dto.req.CreateCellReq;
import io.kyligence.notebook.console.bean.dto.req.SaveNotebookReq;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.bean.entity.NotebookCommit;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.NotebookService;
import io.kyligence.notebook.console.service.UploadFileService;
import io.kyligence.notebook.console.service.UserService;
import io.kyligence.notebook.console.service.WorkflowService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on notebook")
public class NotebookController {

    @Autowired
    private NotebookHelper notebookHelper;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserService userService;

    @Autowired
    private UploadFileService uploadFileService;

    @ApiOperation("Save Notebook Content")
    @PutMapping("/notebook/{id}")
    @Permission
    public Response<IdDTO> save(@PathVariable("id") @NotNull Integer id,
                                @RequestBody @Validated SaveNotebookReq saveNotebookReq) {
        List<CellInfoDTO> cellInfoDTOS = saveNotebookReq.getCellList();

        if (cellInfoDTOS != null) {
            updateNotebookCells(id, cellInfoDTOS);
        }

        return new Response<IdDTO>().data(IdDTO.valueOf(id));
    }

    @Transactional
    protected void updateNotebookCells(Integer notebookId, List<CellInfoDTO> cellInfoDTOs) {
        // 1. update notebook info cell list
        List<Integer> cellIds = cellInfoDTOs.stream()
                .map(cellInfoDTO -> Integer.valueOf(cellInfoDTO.getId()))
                .collect(Collectors.toList());

        List<CellInfo> cellInfos = notebookService.getCellInfos(notebookId);
        List<Integer> allCellIds = cellInfos.stream().map(CellInfo::getId).collect(Collectors.toList());
        cellIds.forEach(cellId -> {
            if (!allCellIds.contains(cellId)) {
                throw new ByzerException(ErrorCodeEnum.CELL_NOT_EXIST);
            }
        });

        String cellList = JacksonUtils.writeJson(cellIds);
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(notebookId);
        notebookInfo.setCellList(cellList);
        notebookService.updateById(notebookInfo);

        // 2. foreach update notebook cell info content
        cellInfoDTOs.forEach(cellInfoDTO -> {
            CellInfo cellInfo = new CellInfo();
            cellInfo.setId(Integer.valueOf(cellInfoDTO.getId()));
            cellInfo.setNotebookId(notebookId);
            cellInfo.setContent(cellInfoDTO.getContent());
            notebookService.updateCellContent(cellInfo);
        });
    }


    @ApiOperation("Get Notebook Content")
    @GetMapping("/notebook/{id}")
    @Permission
    public Response<NotebookDTO> get(@PathVariable("id") @NotNull Integer id,
                                     @RequestParam(value = "commit_id", required = false) String commitId) {
        String user = WebUtils.getCurrentLoginUser();
        NotebookDTO notebookDTO = notebookService.getNotebook(id, user, commitId);
        return new Response<NotebookDTO>().data(notebookDTO);
    }



    private NotebookInfo importNotebook(NotebookDTO notebookDTO, Integer folderId) {
        return notebookHelper.importNotebook(notebookDTO, folderId);
    }

    @ApiOperation("Create Notebook Cell")
    @PostMapping("/notebook/{notebookId}/cell")
    @Permission
    public Response<IdDTO> createCell(@PathVariable("notebookId") @NotNull Integer notebookId,
                                      @RequestBody @Validated CreateCellReq createCellReq) {
        NotebookInfo notebookInfo = notebookService.findById(notebookId);
        if (notebookInfo == null) {
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NOT_EXIST);
        }

        String user = WebUtils.getCurrentLoginUser();
        if (!user.equals(notebookInfo.getUser())) {
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NOT_AVAILABLE);
        }

        // create cell info
        long timestamp = System.currentTimeMillis();
        CellInfo cellInfo = new CellInfo();
        cellInfo.setNotebookId(notebookId);
        cellInfo.setCreateTime(new Timestamp(timestamp));
        cellInfo.setUpdateTime(new Timestamp(timestamp));

        cellInfo = notebookService.save(cellInfo);

        // update cell list order
        String cellList = notebookInfo.getCellList();
        if (cellList == null || cellList.isEmpty()) {
            cellList = "[]";
        }
        List<Integer> cellIds = JacksonUtils.readJsonArray(cellList, Integer.class);
        assert cellIds != null;
        cellIds.add(createCellReq.getCellIndex(), cellInfo.getId());

        notebookInfo.setCellList(JacksonUtils.writeJson(cellIds));
        notebookInfo.setUpdateTime(new Timestamp(timestamp));
        notebookService.save(notebookInfo);

        return new Response<IdDTO>().data(IdDTO.valueOf(cellInfo.getId()));
    }

    @ApiOperation("Delete Notebook Cell")
    @DeleteMapping("/notebook/{notebookId}/cell/{cellId}")
    @Permission
    public Response<IdDTO> delCell(@PathVariable("notebookId") @NotNull Integer notebookId,
                                   @PathVariable("cellId") @NotNull Integer cellId) {
        deleteCell(notebookId, cellId);
        return new Response<IdDTO>().data(IdDTO.valueOf(cellId));
    }

    @ApiOperation("Clear Notebook Result")
    @DeleteMapping("/notebook/{notebookId}/result")
    @Permission
    public Response<IdNameDTO> delCell(@PathVariable("notebookId") @NotNull Integer notebookId) {
        String user = WebUtils.getCurrentLoginUser();
        NotebookInfo notebookInfo = notebookService.findById(notebookId);
        notebookService.checkExecFileAvailable(user, notebookInfo, null);

        List<CellInfo> cells = notebookService.getCellInfos(notebookId);
        if (cells != null) {
            cells.forEach(cell -> {
                cell.setLastJobId(null);
                notebookService.save(cell);
            });
        }

        return new Response<IdNameDTO>().data(IdNameDTO.valueOf(notebookInfo.getId(), notebookInfo.getName()));
    }

    @Transactional
    protected void deleteCell(Integer notebookId, Integer cellId) {
        NotebookInfo notebookInfo = notebookService.findById(notebookId);
        if (notebookInfo == null) {
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NOT_EXIST);
        }
        String cellList = notebookInfo.getCellList();
        if (cellList == null || cellList.isEmpty()) {
            cellList = "[]";
        }
        List<Integer> cellIds = JacksonUtils.readJsonArray(cellList, Integer.class);
        assert cellIds != null;

        if (cellIds.size() <= 1) {
            throw new ByzerException(ErrorCodeEnum.CELL_CAN_NOT_DELETE);
        }

        if (!cellIds.contains(cellId)) {
            throw new ByzerException(ErrorCodeEnum.CELL_NOT_FOUND, cellId);
        }

        cellIds.remove(cellId);

        // update notebook cell list
        long timestamp = System.currentTimeMillis();
        notebookInfo.setCellList(JacksonUtils.writeJson(cellIds));
        notebookInfo.setUpdateTime(new Timestamp(timestamp));
        notebookService.save(notebookInfo);

        // delete cell info
        CellInfo cellInfo = new CellInfo();
        cellInfo.setId(cellId);
        cellInfo.setNotebookId(cellId);
        notebookService.deleteCell(cellInfo);
    }

    @ApiOperation("Get User Default Notebook")
    @GetMapping("/notebook/default")
    @Permission
    public Response<DemoInfoDTO> getDefaultNotebook() {
        NotebookCommit notebookCommit = notebookService.getDefaultDemo();

        if (notebookCommit == null) {
            return new Response<>();
        }

        return new Response<DemoInfoDTO>().data(
                DemoInfoDTO.valueOf(
                        notebookCommit.getNotebookId(),
                        notebookCommit.getName(),
                        "notebook",
                        notebookCommit.getCommitId()
                ));
    }

    @ApiOperation("Create Sample Notebook")
    @PostMapping("/notebook/sample/creation")
    @Permission
    public Response<IdNameDTO> createSampleNotebook() {
        String user = WebUtils.getCurrentLoginUser();
        NotebookInfo notebookInfo = notebookService.getDefault(user);

        if (notebookInfo == null) {
            notebookInfo = notebookHelper.createSampleDemo(user);
        }

        return new Response<IdNameDTO>().data(IdNameDTO.valueOf(notebookInfo.getId(), notebookInfo.getName()));
    }

    @ApiOperation("Code Auto Suggestion")
    @PostMapping("/notebook/code/suggestion")
    @Permission
    public Response<List<CodeSuggestDTO>> getCodeSuggestion(@RequestBody CodeSuggestionReq suggestionReq) {
        List<CodeSuggestDTO> suggestionDTOS = notebookService.getCodeSuggestion(suggestionReq);
        return new Response<List<CodeSuggestDTO>>().data(suggestionDTOS);
    }

    @ApiOperation("Commit Notebook")
    @PostMapping("/notebook/{id}/commit")
    @Permission
    public Response<String> commit(@PathVariable("id") @NotNull Integer notebookId){
        String user = WebUtils.getCurrentLoginUser();
        NotebookCommit commit = notebookService.commit(user, notebookId);
        return new Response<String>().data(commit.getCommitId());
    }
}
