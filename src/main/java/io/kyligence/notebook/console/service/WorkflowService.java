package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.entity.*;
import io.kyligence.notebook.console.dao.ModelInfoRepository;
import io.kyligence.notebook.console.exception.EngineAccessException;
import io.kyligence.notebook.console.scalalib.hint.HintManager;
import io.kyligence.notebook.console.support.ETEnum;
import io.kyligence.notebook.console.util.*;
import io.kyligence.notebook.console.dao.NodeInfoRepository;
import io.kyligence.notebook.console.dao.WorkflowRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Query;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowService implements FileInterface {
    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private NodeInfoRepository nodeInfoRepository;

    @Autowired
    private ModelInfoRepository modelInfoRepository;

    @Autowired
    private ETService etService;

    @Autowired
    private NodeDefService nodeDefService;

    @Autowired
    private EngineService engineService;

    @Autowired
    private CriteriaQueryBuilder criteriaQueryBuilder;

    private static final NotebookConfig config = NotebookConfig.getInstance();

    public WorkflowInfo findById(Integer id) {
        return workflowRepository.findById(id).orElse(null);
    }

    public NodeInfo findNodeById(Integer id) {
        return nodeInfoRepository.findById(id).orElse(null);
    }

    public WorkflowInfo find(String user, String name, Integer folderId) {
        Map<String, String> filters = new HashMap<>();
        filters.put("user", user);
        filters.put("name", name);
        filters.put("folderId", EntityUtils.toStr(folderId));
        Query query = criteriaQueryBuilder.getAll(WorkflowInfo.class, true, null, 1, 0, null, null, filters, null);
        List<WorkflowInfo> workflows = query.getResultList();
        if (workflows == null || workflows.size() == 0) {
            return null;
        }
        return workflows.get(0);
    }

    @Transactional
    public WorkflowInfo create(String user, String name, Integer folderId) {
        if (isWorkflowExist(user, name, folderId)) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_WORKFLOW_NAME);
        }

        long currentTimeStamp = System.currentTimeMillis();
        WorkflowInfo workflowInfo = new WorkflowInfo();
        workflowInfo.setName(name);
        workflowInfo.setUser(user);
        workflowInfo.setCreateTime(new Timestamp(currentTimeStamp));
        workflowInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        workflowInfo.setFolderId(folderId);
        return workflowRepository.save(workflowInfo);
    }

    @Transactional
    public WorkflowInfo rename(Integer workflowId, String user, String name) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);
        workflowRepository.rename(workflowId, name);
        workflowInfo.setName(name);
        return workflowInfo;
    }

    @Transactional
    public NodeInfo createNode(Integer workflowId, String user, String nodeType,
                               NodeInfoDTO.NodeContent nodeContent,
                               NodeInfoDTO.NodePosition nodePosition) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);

        long currentTimeStamp = System.currentTimeMillis();

        if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.ET)) {
            modifyETPreHandler(nodeContent);
        }

        Set<String> existOutputNames = getExistOutputNames(workflowId, nodeType, nodeContent);
        Map<String, List<String>> inputAndOutput = NodeUtils.parseInputAndOutput(nodeType, nodeContent);
        for (String name : inputAndOutput.get("output")) {
            if (name != null && existOutputNames.contains(name)) {
                throw new ByzerException(ErrorCodeEnum.NODE_OUTPUT_ALREADY_EXIST, name);
            }
        }
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setType(nodeType);
        nodeInfo.setContent(JacksonUtils.writeJson(nodeContent));
        nodeInfo.setUser(user);
        nodeInfo.setWorkflowId(workflowId);
        nodeInfo.setInput(JacksonUtils.writeJson(inputAndOutput.get("input")));
        nodeInfo.setOutput(JacksonUtils.writeJson(inputAndOutput.get("output")));

        nodeInfo.setId(-1);

        List<NodeInfo> nodeInfoList = nodeInfoRepository.findByWorkflow(workflowId);
        nodeInfoList.add(nodeInfo);

        NodeChainParser chainParser = new NodeChainParser(nodeInfoList);
        chainParser.checkCycle();

        nodeInfo.setId(null);
        nodeInfo.setCreateTime(new Timestamp(currentTimeStamp));
        nodeInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        nodeInfo.setPosition(JacksonUtils.writeJson(nodePosition));
        nodeInfo = nodeInfoRepository.save(nodeInfo);

        if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.TRAIN)) {
            modifyTrainNodePostHandler(workflowId, nodeInfo.getId(), user, nodeContent);
        } else if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.ET)) {
            modifyAlgorithmETPostHandler(workflowId, nodeInfo.getId(), user, nodeContent);
        }

        return nodeInfo;
    }

    public NodeInfo updateNode(Integer workflowId, Integer nodeId, String user, String nodeType, NodeInfoDTO.NodeContent nodeContent) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);
        NodeInfo nodeInfo = findNodeById(nodeId);

        if (nodeInfo == null) {
            throw new ByzerException(ErrorCodeEnum.NODE_NOT_EXIST);
        }
        if (!nodeType.equalsIgnoreCase(nodeInfo.getType())) {
            throw new ByzerException(ErrorCodeEnum.CAN_NOT_CHANGE_NODE_TYPE);
        }

        if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.ET)) {
            modifyETPreHandler(nodeContent);
        }

        Set<String> existOutputNames = getExistOutputNames(workflowId, nodeType, nodeContent);
        List<String> originalOutput = JacksonUtils.readJsonArray(nodeInfo.getOutput(), String.class);
        Map<String, List<String>> inputAndOutput = NodeUtils.parseInputAndOutput(nodeType, nodeContent);
        for (String name : inputAndOutput.get("output")) {
            if (name == null || (originalOutput != null && originalOutput.contains(name))) continue;
            if (existOutputNames.contains(name)) {
                throw new ByzerException(ErrorCodeEnum.NODE_OUTPUT_ALREADY_EXIST, name);
            }
        }
        NodeInfo newNodeInfo = new NodeInfo();
        newNodeInfo.setType(nodeType);
        newNodeInfo.setContent(JacksonUtils.writeJson(nodeContent));
        newNodeInfo.setUser(user);
        newNodeInfo.setWorkflowId(workflowId);
        newNodeInfo.setInput(JacksonUtils.writeJson(inputAndOutput.get("input")));
        newNodeInfo.setOutput(JacksonUtils.writeJson(inputAndOutput.get("output")));
        newNodeInfo.setId(nodeId);

        List<NodeInfo> nodeInfoList = nodeInfoRepository.findByWorkflow(workflowId);
        nodeInfoList.remove(nodeInfo);
        nodeInfoList.add(newNodeInfo);

        NodeChainParser chainParser = new NodeChainParser(nodeInfoList);
        chainParser.checkCycle();

        nodeInfoRepository.updateNode(
                newNodeInfo.getId(),
                newNodeInfo.getWorkflowId(),
                newNodeInfo.getContent(),
                newNodeInfo.getInput(),
                newNodeInfo.getOutput()
        );

        if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.TRAIN)) {
            modelInfoRepository.deleteByNodeId(nodeId);
            modifyTrainNodePostHandler(workflowId, nodeInfo.getId(), user, nodeContent);
        } else if (nodeType.equalsIgnoreCase(NodeUtils.NodeType.ET)) {
            modelInfoRepository.deleteByNodeId(nodeId);
            modifyAlgorithmETPostHandler(workflowId, nodeInfo.getId(), user, nodeContent);
        }

        return newNodeInfo;
    }

    @Transactional
    public void deleteNode(Integer workflowId, Integer nodeId, String user) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);

        nodeInfoRepository.delete(nodeId, workflowId);
        modelInfoRepository.deleteByNodeId(nodeId);

    }

    public NodeInfo updateNodePosition(Integer workflowId, Integer nodeId, String user, NodeInfoDTO.NodePosition position) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);
        NodeInfo nodeInfo = findNodeById(nodeId);

        if (nodeInfo == null) {
            throw new ByzerException(ErrorCodeEnum.NODE_NOT_EXIST);
        }
        String positionString = JacksonUtils.writeJson(position);
        nodeInfoRepository.updateNodePosition(nodeId, positionString);
        nodeInfo.setPosition(positionString);
        return nodeInfo;
    }


    public List<NodeInfo> findNodeByWorkflow(Integer workflowId) {
        return nodeInfoRepository.findByWorkflow(workflowId);
    }

    public Map<Integer, ConnectionInfo> getUserConnectionMap(String user) {
        List<ConnectionInfo> connectionInfoList = connectionService.getConnectionList(user);
        Map<Integer, ConnectionInfo> connectionMap = new HashMap<>();
        connectionInfoList.forEach(connectionInfo -> connectionMap.put(connectionInfo.getId(), connectionInfo));
        return connectionMap;
    }

    @Transactional
    public NotebookInfo workflowToNotebook(Integer workflowId, String user, String notebookName) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);
        notebookService.checkResourceLimit(user, 1);

        // 1. create notebook
        long timestamp = System.currentTimeMillis();

        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setUser(user);

        if (notebookService.isNotebookExist(user, notebookName, workflowInfo.getFolderId())) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_NOTEBOOK_NAME);
        }
        notebookInfo.setName(notebookName);

        notebookInfo.setCreateTime(new Timestamp(timestamp));
        notebookInfo.setUpdateTime(new Timestamp(timestamp));
        notebookInfo.setFolderId(workflowInfo.getFolderId());

        notebookInfo = notebookService.save(notebookInfo);
        Integer notebookId = notebookInfo.getId();

        // 2. render cell contents
        List<NodeInfo> nodeInfoList = nodeInfoRepository.findByWorkflow(workflowId);
        if (nodeInfoList.size() == 0) {
            return notebookInfo;
        }

        WorkflowContentDTO contents = WorkflowContentDTO.valueOf(
                workflowInfo,
                nodeInfoList,
                getUserConnectionMap(user),
                getAlgoParamSettings()
        );
        List<WorkflowContentDTO.WorkflowCellContent> cellContents = contents.getCellList();

        // 3. create notebook cells
        List<Integer> cellIds = new ArrayList<>();

        cellContents.forEach(cellContent -> {
            CellInfo cellInfo = new CellInfo();
            cellInfo.setNotebookId(notebookId);
            cellInfo.setContent(cellContent.getContent());
            cellInfo.setCreateTime(new Timestamp(timestamp));
            cellInfo.setUpdateTime(new Timestamp(timestamp));
            cellInfo = notebookService.save(cellInfo);
            cellIds.add(cellInfo.getId());
        });

        // 4. update notebook cell ids
        notebookInfo.setCellList(JacksonUtils.writeJson(cellIds));
        return notebookService.save(notebookInfo);
    }

    public SQLValidationDTO checkSQL(String sql) {
        try {
            engineService.runAnalyze(
                    new EngineService.RunScriptParams()
                            .withSql(sql.trim())
                            .with("skipAuth", "true")
                            .withAsync("false")
            );
            return SQLValidationDTO.valueOf(true, null);
        } catch (EngineAccessException exception) {
            Throwable baseException = exception.getCause();
            if (baseException instanceof ByzerException) {
                String msg = baseException.getMessage();
                return SQLValidationDTO.valueOf(false, msg.substring(0, msg.indexOf("]") + 1));
            }
            throw exception;
        }
    }

    public Set<String> listOutput(Integer workflowId) {
        Set<String> outputList = new HashSet<>();

        List<NodeInfo> nodeInfoList = nodeInfoRepository.findByWorkflow(workflowId);
        for (NodeInfo node : nodeInfoList) {
            if (node.getType().equalsIgnoreCase(NodeUtils.NodeType.TRAIN)) continue;
            else if (node.getType().equalsIgnoreCase(NodeUtils.NodeType.ET)) {
                NodeInfoDTO.NodeContent content = JacksonUtils.readJson(node.getContent(), NodeInfoDTO.NodeContent.class);
                if (Objects.nonNull(content.getEtId()) && etOutputIsModel(content.getEtId())) continue;
            }
            List<String> outputs = JacksonUtils.readJsonArray(node.getOutput(), String.class);
            if (outputs != null) outputList.addAll(outputs);
        }
        return outputList;
    }

    public Set<String> getExistOutputNames(Integer workflowId, String nodeType, NodeInfoDTO.NodeContent content) {
        if (outputIsModel(nodeType, content)) {
            return listAllModels();
        } else {
            return listOutput(workflowId);
        }
    }

    private boolean isWorkflowExist(String user, String name, Integer folderId) {
        WorkflowInfo workflowInfo = find(user, name, folderId);
        return workflowInfo != null && workflowInfo.getName().equals(name);
    }

    public void checkExecFileAvailable(String user, ExecFileInfo execFileInfo) {
        if (execFileInfo == null) {
            throw new ByzerException(ErrorCodeEnum.WORKFLOW_NOT_EXIST);
        }
        if (!user.equalsIgnoreCase(execFileInfo.getUser()) && !user.equalsIgnoreCase("admin")) {
            throw new ByzerException(ErrorCodeEnum.WORKFLOW_NOT_AVAILABLE);
        }
    }

    @Override
    @Transactional
    public void delete(Integer workflowId) {
        workflowRepository.deleteById(workflowId);
        nodeInfoRepository.deleteByWorkflow(workflowId);
        modelInfoRepository.deleteByWorkflowId(workflowId);
    }

    public List<WorkflowInfo> find(String user) {
        return workflowRepository.find(user);
    }

    @Override
    public void updateById(ExecFileInfo execFileInfo) {
        workflowRepository.save((WorkflowInfo) execFileInfo);
    }

    @Override
    public boolean isExecFileExist(String user, String name, Integer folderId) {
        return isWorkflowExist(user, name, folderId);
    }

    @Override
    public ExecFileDTO analyzeFile(MultipartFile file) throws IOException {
        return JacksonUtils.readJson(file.getBytes(), WorkflowDTO.class);
    }

    @Override
    public ExecFileDTO getFile(Integer id, String user) {
        return getWorkflow(id, user);
    }

    @Override
    public ExecFileInfo importExecFile(ExecFileDTO execFileDTO, Integer folderId) {
        return importWorkflow((WorkflowDTO) execFileDTO, folderId);
    }

    @Transactional
    public WorkflowInfo importWorkflow(WorkflowDTO workflowDTO, Integer folderId, String type) {
        // create notebook
        String user = WebUtils.getCurrentLoginUser();
        long timestamp = System.currentTimeMillis();
        WorkflowInfo workflowInfo = new WorkflowInfo();
        workflowInfo.setUser(WebUtils.getCurrentLoginUser());

        if (isWorkflowExist(user, workflowDTO.getName(), folderId)) {
            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault(Locale.Category.FORMAT)).format(new Date());
            workflowInfo.setName(workflowDTO.getName() + "_imported_" + time);
        } else {
            workflowInfo.setName(workflowDTO.getName());
        }

        workflowInfo.setCreateTime(new Timestamp(timestamp));
        workflowInfo.setUpdateTime(new Timestamp(timestamp));
        workflowInfo.setType(type);
        workflowInfo.setFolderId(folderId);

        workflowInfo = workflowRepository.save(workflowInfo);
        Integer workflowId = workflowInfo.getId();

        // 2. create nodes
        if (workflowDTO.getNodeList() == null || workflowDTO.getNodeList().isEmpty()) {
            return workflowInfo;
        }

        for (NodeInfoDTO nodeInfoDTO : workflowDTO.getNodeList()) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setWorkflowId(workflowId);
            nodeInfo.setUser(user);
            nodeInfo.setType(nodeInfoDTO.getType());
            nodeInfo.setInput(JacksonUtils.writeJson(nodeInfoDTO.getInput()));
            nodeInfo.setOutput(JacksonUtils.writeJson(nodeInfoDTO.getOutput()));
            nodeInfo.setPosition(JacksonUtils.writeJson(nodeInfoDTO.getPosition()));
            nodeInfo.setContent(JacksonUtils.writeJson(nodeInfoDTO.getContent()));
            nodeInfo.setCreateTime(new Timestamp(timestamp));
            nodeInfo.setUpdateTime(new Timestamp(timestamp));
            nodeInfo = nodeInfoRepository.save(nodeInfo);

            if (nodeInfo.getType().equalsIgnoreCase(NodeUtils.NodeType.TRAIN)) {
                modifyTrainNodePostHandler(workflowId, nodeInfo.getId(), user, nodeInfoDTO.getContent());
            } else if (nodeInfo.getType().equalsIgnoreCase(NodeUtils.NodeType.ET)) {
                modifyAlgorithmETPostHandler(workflowId, nodeInfo.getId(), user, nodeInfoDTO.getContent());
            }

        }

        return workflowRepository.save(workflowInfo);
    }

    public ExecFileInfo importWorkflow(WorkflowDTO workflowDTO, Integer folderId) {
        return importWorkflow(workflowDTO, folderId, null);
    }

    public ExecFileInfo importWorkflow(WorkflowDTO workflowDTO) {
        return importWorkflow(workflowDTO, null, null);
    }

    public ExecFileDTO getWorkflow(Integer execFileId, String user) {
        WorkflowInfo workflowInfo = findById(execFileId);
        checkExecFileAvailable(user, workflowInfo);

        List<NodeInfo> nodeInfos = findNodeByWorkflow(execFileId);
        Map<Integer, ConnectionInfo> map = getUserConnectionMap(user);

        return WorkflowDTO.valueOf(workflowInfo, nodeInfos, map);
    }

    public Map<String, List<ParamDefDTO>> getAlgoParamSettings() {
        return nodeDefService.getAlgoParamSettings();
    }

    public List<ModelInfo> getAllModels(String user) {
        return modelInfoRepository.findByUser(user);
    }

    public List<ModelInfo> getWorkflowModels(Integer workflowId) {
        return modelInfoRepository.findByWorkflowId(workflowId);
    }

    private void modifyTrainNodePostHandler(Integer workflowId, Integer nodeId,
                                            String user, NodeInfoDTO.NodeContent nodeContent) {
        ModelInfo modelInfo = new ModelInfo();
        if (nodeContent.getAlgorithm() == null || nodeContent.getSavePath() == null || nodeContent.getTarget() == null) {
            return;
        }
        modelInfo.setAlgorithm(nodeContent.getAlgorithm());
        modelInfo.setWorkflowId(workflowId);
        modelInfo.setPath(nodeContent.getModelFullPath());
        modelInfo.setGroupSize(nodeContent.getGroupSize());
        modelInfo.setNodeId(nodeId);
        modelInfo.setUserName(user);
        modelInfoRepository.save(modelInfo);
    }

    private void modifyAlgorithmETPostHandler(Integer workflowId, Integer nodeId,
                                              String user, NodeInfoDTO.NodeContent nodeContent) {
        if (!etOutputIsModel(nodeContent.getEtId())) return;
        ETNodeDTO etNodeDTO = etService.getETById(nodeContent.getEtId());
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setWorkflowId(workflowId);
        modelInfo.setAlgorithm(etNodeDTO.getName());
        modelInfo.setGroupSize(nodeContent.getGroup() != null ? nodeContent.getGroup().size() : 0);
        modelInfo.setNodeId(nodeId);
        modelInfo.setUserName(user);
        modelInfo.setPath(nodeContent.getModelFullPath());
        modelInfoRepository.save(modelInfo);

    }

    public WorkflowContentDTO getWorkflowContent(String user, Integer workflowId) {
        WorkflowInfo workflowInfo = findById(workflowId);
        checkExecFileAvailable(user, workflowInfo);
        List<NodeInfo> nodeInfoList = findNodeByWorkflow(workflowId);
        Map<Integer, ConnectionInfo> connectionInfoMap = getUserConnectionMap(workflowInfo.getUser());
        Map<String, List<ParamDefDTO>> algoParams = getAlgoParamSettings();
        return WorkflowContentDTO.valueOf(workflowInfo, nodeInfoList, connectionInfoMap, algoParams);
    }

    public String getWorkflowScripts(String user, Integer workflowId, Map<String, String> options) {
        WorkflowContentDTO content = getWorkflowContent(user, workflowId);
        List<String> scripts = content.getCellList().stream().map(WorkflowContentDTO.WorkflowCellContent::getContent)
                .filter(Objects::nonNull).map(sql -> HintManager.applyHintRewrite(sql, options))
                .collect(Collectors.toList());
        return String.join("\n", scripts);
    }

    public void checkResourceLimit(String user, Integer newResourceNum) {
        Integer limit = config.getUserWorkflowNumLimit();
        if (limit > 0 && workflowRepository.getUserWorkflowCount(user) + newResourceNum > limit) {
            if (config.getIsTrial()) {
                throw new ByzerException(ErrorCodeEnum.WORKFLOW_NUM_REACH_LIMIT,
                        String.format(
                                ("The online trial version supports up to %1$s workflows. " +
                                        "Please contact us if you need more help."),
                                limit
                        )
                );
            }
            throw new ByzerException(ErrorCodeEnum.WORKFLOW_NUM_REACH_LIMIT);
        }
    }

    private Set<String> listAllModels() {
        Set<String> outputList = new HashSet<>();

        List<ModelInfo> modelInfoList = modelInfoRepository.findAll();
        modelInfoList.forEach(modelInfo -> outputList.add(
                String.format("%1$s.`%2$s`", modelInfo.getAlgorithm(), modelInfo.getPath())
        ));
        return outputList;
    }

    private Boolean outputIsModel(String nodeType, NodeInfoDTO.NodeContent content) {
        switch (nodeType.toLowerCase()) {
            case NodeUtils.NodeType.TRAIN:
                return true;
            case NodeUtils.NodeType.ET:
                return etOutputIsModel(content.getEtId());
            default:
                return false;
        }
    }

    private Boolean etOutputIsModel(Integer etId) {
        if (etId == null || !etService.etIsEnabled(etId)) return false;
        return NodeUtils.etUsageCheck(etService.getETById(etId).getEtUsage(), ETEnum.UsageTemplate.TRAIN.getName());
    }

    private void modifyETPreHandler(NodeInfoDTO.NodeContent nodeContent) {
        nodeContent.setEtUsage(etService.getETUsage(nodeContent.getEtId()));
        ETNodeDTO etNodeDTO = etService.getETById(nodeContent.getEtId());
        nodeContent.setEtName(etNodeDTO.getName());
        nodeContent.setEtUsageName(etNodeDTO.getEtUsage());
        if (NodeUtils.etUsageCheck(etNodeDTO.getEtUsage(), ETEnum.UsageTemplate.TRAIN.getName())) {
            nodeContent.setSource(nodeContent.getInputParam().get(0).getValue());
            nodeContent.setAlgorithm(etService.getETName(nodeContent.getEtId()));
            nodeContent.setTarget(nodeContent.getOutputParam().get(1).getValue());
            nodeContent.setSavePath(nodeContent.getOutputParam().get(0).getValue());
        }
    }
}
