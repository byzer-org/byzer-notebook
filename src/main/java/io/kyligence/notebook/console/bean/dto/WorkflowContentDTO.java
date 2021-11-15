package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.entity.WorkflowInfo;
import io.kyligence.notebook.console.util.NodeUtils;
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
public class WorkflowContentDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user")
    private String user;

    @JsonProperty("cell_list")
    private List<WorkflowCellContent> cellList;

    @Data
    @NoArgsConstructor
    public static class WorkflowCellContent{
        @JsonProperty("content")
        private String content;
    }

    public static WorkflowContentDTO valueOf(WorkflowInfo workflow, List<NodeInfo> nodeInfos,
                                             Map<Integer,ConnectionInfo> connectionInfoMap,
                                             Map<String, List<ParamDefDTO>> algParam) {

        WorkflowContentDTO workflowDTO = new WorkflowContentDTO();
        workflowDTO.setId(EntityUtils.toStr(workflow.getId()));
        workflowDTO.setName(workflow.getName());
        workflowDTO.setUser(workflow.getUser());

        NodeChainParser chainParser = new NodeChainParser(nodeInfos);
        chainParser.checkCycle();

        Set<NodeChainParser.Node> allNodes = chainParser.visitAll();
        workflowDTO.setCellList(allNodes.stream().map(node -> {
            WorkflowCellContent cellContent = new WorkflowCellContent();
            cellContent.setContent(NodeUtils.renderNodeSQL(node.getInfo(), connectionInfoMap, algParam));
            return cellContent;
        }).collect(Collectors.toList()));

        return workflowDTO;
    }
}
