package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.dto.req.*;
import io.kyligence.notebook.console.bean.entity.*;
import io.kyligence.notebook.console.service.WorkflowService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @ApiOperation("Create Workflow")
    @PostMapping("/workflow")
    @Permission
    public Response<IdDTO> create(@RequestBody @Validated CreateWorkflowReq createWorkflowReq) {
        String user = WebUtils.getCurrentLoginUser();
        WorkflowInfo workflowInfo = workflowService.create(
                user,
                createWorkflowReq.getName(),
                createWorkflowReq.getFolderId()
        );
        return new Response<IdDTO>().data(IdDTO.valueOf(workflowInfo.getId()));
    }

    @ApiOperation("Rename Workflow")
    @PutMapping("/workflow")
    @Permission
    public Response<IdDTO> rename(@RequestBody @Validated RenameWorkflowReq renameWorkflowReq) {
        String user = WebUtils.getCurrentLoginUser();
        WorkflowInfo workflowInfo = workflowService.rename(
                Integer.valueOf(renameWorkflowReq.getId()),
                user,
                renameWorkflowReq.getName()
        );
        return new Response<IdDTO>().data(IdDTO.valueOf(workflowInfo.getId()));
    }

    @ApiOperation("Create Workflow Node")
    @PostMapping("/workflow/{id}/node")
    @Permission
    public Response<IdDTO> createNode(@PathVariable("id") @NotNull Integer id,
                                      @RequestBody @Validated CUNodeReq createNodeReq) {
        String user = WebUtils.getCurrentLoginUser();
        NodeInfo nodeInfo = workflowService.createNode(
                id,
                user,
                createNodeReq.getNodeType(),
                createNodeReq.getContent(),
                createNodeReq.getPosition()
        );
        return new Response<IdDTO>().data(IdDTO.valueOf(nodeInfo.getId()));
    }


    @ApiOperation("Get Workflow Content")
    @GetMapping("/workflow/{id}")
    @Permission
    public Response<WorkflowDTO> getWorkflow(@PathVariable("id") @NotNull Integer id) {
        String user = WebUtils.getCurrentLoginUser();
        WorkflowInfo workflowInfo = workflowService.findById(id);
        if (workflowInfo == null) {
            return new Response<WorkflowDTO>().data(null);
        }
        List<NodeInfo> nodeInfoList = workflowService.findNodeByWorkflow(id);
        Map<Integer, ConnectionInfo> connectionInfoMap = workflowService.getUserConnectionMap(user);
        return new Response<WorkflowDTO>().data(WorkflowDTO.valueOf(workflowInfo, nodeInfoList, connectionInfoMap));
    }

    @ApiOperation("Delete Node")
    @DeleteMapping("/workflow/{workflowId}/node/{nodeId}")
    @Permission
    public Response<IdDTO> deleteNode(@PathVariable("workflowId") @NotNull Integer workflowId,
                                      @PathVariable("nodeId") @NotNull Integer nodeId) {
        String user = WebUtils.getCurrentLoginUser();
        workflowService.deleteNode(workflowId, nodeId, user);
        return new Response<IdDTO>().data(IdDTO.valueOf(nodeId));
    }

    @ApiOperation("Get Workflow Script Content")
    @GetMapping("/workflow/{id}/script")
    @Permission
    public Response<WorkflowContentDTO> getWorkflowContent(@PathVariable("id") @NotNull Integer id) {
        String user = WebUtils.getCurrentLoginUser();
        WorkflowContentDTO content = workflowService.getWorkflowContent(user, id);
        return new Response<WorkflowContentDTO>().data(content);
    }

    @ApiOperation("Workflow to Notebook")
    @PostMapping("/workflow/{id}/notebook")
    @Permission
    public Response<IdDTO> toNotebook(@PathVariable("id") @NotNull Integer id,
                                      @RequestBody @Validated WorkflowToNotebookReq workflowToNotebookReq) {
        String user = WebUtils.getCurrentLoginUser();
        NotebookInfo notebookInfo = workflowService.workflowToNotebook(id, user, workflowToNotebookReq.getName());
        return new Response<IdDTO>().data(IdDTO.valueOf(notebookInfo.getId()));
    }

    @ApiOperation("Check SQL")
    @PostMapping("/sql/validation")
    @Permission
    public Response<SQLValidationDTO> sqlCheck(@RequestBody @Validated SQLValidationReq sqlValidationReq) {
        SQLValidationDTO sqlValidationDTO = workflowService.checkSQL(sqlValidationReq.getContent());
        return new Response<SQLValidationDTO>().data(sqlValidationDTO);
    }

    @ApiOperation("List of Workflow Outputs")
    @GetMapping("/workflow/{id}/output")
    @Permission
    public Response<NodeOutputDTO> getNodeOutputs(@PathVariable("id") @NotNull Integer id) {
        Set<String> outputList = workflowService.listOutput(id);
        return new Response<NodeOutputDTO>().data(NodeOutputDTO.valueOf(outputList));
    }

    @ApiOperation("List of Workflow defined Models")
    @GetMapping("/workflow/{id}/model")
    @Permission
    public Response<ModelInfoListDTO> getModels(@PathVariable("id") @NotNull Integer id) {
        List<ModelInfo> models = workflowService.getWorkflowModels(id);
        return new Response<ModelInfoListDTO>().data(ModelInfoListDTO.valueOf(models));
    }

    @ApiOperation("List of User Defined Models")
    @GetMapping("/workflow/model")
    @Permission
    public Response<ModelInfoListDTO> getAllModels() {
        String user = WebUtils.getCurrentLoginUser();
        List<ModelInfo> models = workflowService.getAllModels(user);
        return new Response<ModelInfoListDTO>().data(ModelInfoListDTO.valueOf(models));
    }

    @ApiOperation("Get Node Content")
    @GetMapping("/workflow/{workflowId}/node/{nodeId}")
    @Permission
    public Response<NodeInfoDTO> getNode(
            @PathVariable("workflowId") @NotNull Integer workflowId,
            @PathVariable("nodeId") @NotNull Integer nodeId) {
        String user = WebUtils.getCurrentLoginUser();
        NodeInfo nodeInfo = workflowService.findNodeById(nodeId);
        Map<Integer, ConnectionInfo> connectionInfoMap = workflowService.getUserConnectionMap(user);

        Map<String, List<ParamDefDTO>> algoParam = workflowService.getAlgoParamSettings();
        return new Response<NodeInfoDTO>().data(NodeInfoDTO.valueOf(nodeInfo, connectionInfoMap, algoParam));
    }

    @ApiOperation("Get Node Content")
    @PutMapping("/workflow/{workflowId}/node/{nodeId}")
    @Permission
    public Response<IdDTO> updateNode(@PathVariable("workflowId") @NotNull Integer workflowId,
                                      @PathVariable("nodeId") @NotNull Integer nodeId,
                                      @RequestBody @Validated CUNodeReq updateNodeReq) {
        String user = WebUtils.getCurrentLoginUser();
        NodeInfo nodeInfo = workflowService.updateNode(
                workflowId,
                nodeId,
                user,
                updateNodeReq.getNodeType(),
                updateNodeReq.getContent()
        );
        return new Response<IdDTO>().data(IdDTO.valueOf(nodeInfo.getId()));
    }

    @ApiOperation("Update Node Position ")
    @PutMapping("/workflow/{workflowId}/node/{nodeId}/position")
    @Permission
    public Response<IdDTO> updateNodePosition(@PathVariable("workflowId") @NotNull Integer workflowId,
                                              @PathVariable("nodeId") @NotNull Integer nodeId,
                                              @RequestBody @Validated UpdateNodePositionReq updateNodePositionReq) {
        String user = WebUtils.getCurrentLoginUser();
        NodeInfo nodeInfo = workflowService.updateNodePosition(
                workflowId,
                nodeId,
                user,
                updateNodePositionReq.getPosition()
        );
        return new Response<IdDTO>().data(IdDTO.valueOf(nodeInfo.getId()));
    }

    @ApiOperation("Delete Workflow")
    @DeleteMapping("/workflow/{workflowId}")
    @Permission
    public Response<IdDTO> deleteWorkflow(@PathVariable("workflowId") @NotNull Integer workflowId) {
        String user = WebUtils.getCurrentLoginUser();
        WorkflowInfo workflowInfo = workflowService.findById(workflowId);
        workflowService.checkExecFileAvailable(user, workflowInfo);
        workflowService.delete(workflowId);
        return new Response<IdDTO>().data(IdDTO.valueOf(workflowId));
    }
}
