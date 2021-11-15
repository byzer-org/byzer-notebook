package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import io.kyligence.notebook.console.support.EnumValid;
import io.kyligence.notebook.console.support.ParamEnums;
import io.kyligence.notebook.console.util.NodeUtils;
import io.kyligence.notebook.console.util.EntityUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class NodeInfoDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("content")
    private NodeContent content;

    @JsonProperty("position")
    private NodePosition position;

    @JsonProperty("type")
    private String type;

    @JsonProperty("input")
    private List<String> input;

    @JsonProperty("output")
    private List<String> output;

    @JsonProperty("from")
    private List<String> from;

    @JsonProperty("to")
    private List<String> to;

    @JsonProperty("sql")
    private String sql;

    @Data
    @NoArgsConstructor
    public static class NodePosition {
        @JsonProperty("left")
        private String x;

        @JsonProperty("top")
        private String y;

        public static NodePosition valueOf(Map<String, String> positionMap) {
            NodePosition position = new NodePosition();
            position.setX(positionMap.get("x"));
            position.setY(positionMap.get("y"));
            return position;
        }
    }

    @Data
    @NoArgsConstructor
    public static class NodeContent {

        @JsonProperty("sql")
        private String sql;

        @JsonProperty("et_id")
        private Integer etId;

        @JsonProperty("et_name")
        private String etName;

        @JsonProperty("et_usage")
        private String etUsage;

        @JsonProperty("input_param")
        private List<ParamValueMap> inputParam;

        @JsonProperty("output_param")
        private List<ParamValueMap> outputParam;

        @JsonProperty("param_list")
        private List<ParamValueMap> paramList;

        @JsonProperty("group")
        private List<List<ParamValueMap>> group;

        @EnumValid(enumClass = ParamEnums.DatasourceType.class, message = "ParamError: invalid datasource_type")
        @JsonProperty("datasource_type")
        private String datasourceType;

        @JsonProperty("database")
        private String database;

        @JsonProperty("header")
        private String header;

        @JsonProperty("connection")
        private Integer connection;

        @JsonProperty("connection_name")
        private String connectionName;

        @EnumValid(enumClass = ParamEnums.HDFSDataType.class, message = "ParamError: invalid data_type")
        @JsonProperty("data_type")
        private String dataType;

        @JsonProperty("source")
        private String source;

        @JsonProperty("target")
        private String target;

        @EnumValid(enumClass = ParamEnums.NodeSaveModeEnum.class, message = "ParamError: invalid save mode")
        @JsonProperty("mode")
        private String mode;

        @JsonProperty("algorithm")
        private String algorithm;

        @JsonProperty("save_path")
        private String savePath;

        @JsonProperty("train_param")
        private String trainParam;

        @JsonProperty("group_size")
        private Integer groupSize;

        @JsonProperty("model")
        private String model;

        @JsonProperty("predict_param")
        private PredictParam predictParam;

        @JsonProperty("deploy_mode_param")
        private DeployModeParam deployModeParam;

        @JsonProperty(value = "extra_params")
        private Map<String, String> extraParams;

        private List<ParamDefDTO> algParamDef;

        private String etUsageName;

        public String getModelFullPath() {
            if (getSavePath() == null || getTarget() == null) {
                return null;
            }

            String dir = getSavePath();
            if (!dir.startsWith("/")) {
                dir = "/" + dir;
            }
            if (!dir.endsWith("/")) {
                dir += "/";
            }
            return dir + getTarget();
        }
    }

    @Data
    @NoArgsConstructor
    public static class PredictParam {
        @JsonProperty(value = "auto")
        private Boolean auto;

        @JsonProperty(value = "value")
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class DeployModeParam {
        @JsonProperty("url")
        private String url;

        @JsonProperty("access_token")
        private String accessToken;
    }

    public static NodeInfoDTO valueOf(NodeInfo nodeInfo, Map<Integer, ConnectionInfo> connectionMap, Map<String, List<ParamDefDTO>> algoParam) {
        NodeInfoDTO dto = valueOf(nodeInfo, connectionMap);
        NodeContent content = dto.getContent();
        if (nodeInfo.getType().equalsIgnoreCase(NodeUtils.NodeType.TRAIN)) {
            content.setAlgParamDef(algoParam.get(content.getAlgorithm()));
        }
        dto.setSql(NodeUtils.renderSQL(nodeInfo.getType(), content));
        return dto;
    }

    public static NodeInfoDTO valueOf(NodeInfo nodeInfo, Map<Integer, ConnectionInfo> connectionMap) {
        if (nodeInfo == null) {
            return null;
        }

        NodeInfoDTO nodeInfoDTO = new NodeInfoDTO();
        nodeInfoDTO.setId(EntityUtils.toStr(nodeInfo.getId()));
        nodeInfoDTO.setType(nodeInfo.getType());
        nodeInfoDTO.setPosition(JacksonUtils.readJson(nodeInfo.getPosition(), NodePosition.class));

        nodeInfoDTO.setInput(JacksonUtils.readJsonArray(nodeInfo.getInput(), String.class));
        nodeInfoDTO.setOutput(JacksonUtils.readJsonArray(nodeInfo.getOutput(), String.class));

        NodeContent content = JacksonUtils.readJson(nodeInfo.getContent(), NodeContent.class);
        if (content != null) {
            Integer connectionId = content.getConnection();
            if (connectionId != null) {
                ConnectionInfo connection = connectionMap.getOrDefault(connectionId, null);
                if (connection != null) content.setConnectionName(connection.getName());
            }
            nodeInfoDTO.setContent(content);
        }
        return nodeInfoDTO;
    }


    public static NodeInfoDTO valueOf(NodeInfo nodeInfo, List<String> from, List<String> to,
                                      Map<Integer, ConnectionInfo> connectionMap) {
        NodeInfoDTO nodeInfoDTO = valueOf(nodeInfo, connectionMap);
        nodeInfoDTO.setFrom(from);
        nodeInfoDTO.setTo(to);
        return nodeInfoDTO;
    }
}
