package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.entity.WorkflowInfo;
import io.kyligence.notebook.console.util.EntityUtils;
import io.kyligence.notebook.console.util.NodeChainParser;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class WorkflowDTO extends ExecFileDTO{
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user")
    private String user;

    @JsonProperty("node_list")
    private List<NodeInfoDTO> nodeList;


    public static WorkflowDTO valueOf(WorkflowInfo workflow, List<NodeInfo> nodeInfos,
                                      Map<Integer, ConnectionInfo> connectionMap) {

        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setId(EntityUtils.toStr(workflow.getId()));
        workflowDTO.setName(workflow.getName());
        workflowDTO.setUser(workflow.getUser());

        NodeChainParser chainParser = new NodeChainParser(nodeInfos);
        Set<NodeChainParser.Node> allNodes = chainParser.visitAll();
        workflowDTO.setNodeList(allNodes.stream().map(node -> {
            NodeInfo nodeInfo = node.getInfo();
            return NodeInfoDTO.valueOf(
                    nodeInfo,
                    node.getSortedNodeIdList(node.getPreviousNode()),
                    node.getSortedNodeIdList(node.getNextNode()),
                    connectionMap
            );
        }).collect(Collectors.toList()));

        chainParser.checkCycle();

        return workflowDTO;
    }
}
