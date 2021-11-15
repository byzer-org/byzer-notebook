package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.bean.dto.NodeInfoDTO;
import io.kyligence.notebook.console.bean.dto.ParamDefDTO;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class NodeUtils {

    public interface NodeType {
        String SELECT = "select";
        String LOAD = "load";
        String SAVE = "save";
        String TRAIN = "train";
        String PREDICT = "predict";
        String REGISTER = "register";
        String ET = "et";
    }


    public static String renderSQL(String nodeType, NodeInfoDTO.NodeContent nodeContent) {
        switch (nodeType) {
            case NodeType.SELECT:
                return renderSelectSQL(nodeContent);
            case NodeType.LOAD:
                return renderLoadSQL(nodeContent);
            case NodeType.SAVE:
                return renderSaveSQL(nodeContent);
            case NodeType.TRAIN:
                return renderTrainSQL(nodeContent);
            case NodeType.PREDICT:
                return renderPredictSQL(nodeContent);
            case NodeType.REGISTER:
                return renderRegisterSQL(nodeContent);
            case NodeType.ET:
                return renderETSQL(nodeContent);
            default:
                throw new ByzerException(ErrorCodeEnum.UNSUPPORTED_NODE_TYPE);
        }
    }

    private static String renderETSQL(NodeInfoDTO.NodeContent nodeContent) {
        return ETSQLRender.render(nodeContent);
    }

    private static String renderRegisterSQL(NodeInfoDTO.NodeContent nodeContent) {
        String sql = String.format(
                "register %1$s as %2$s;",
                nodeContent.getSource(),
                nodeContent.getTarget()
        );
        NodeInfoDTO.DeployModeParam deployParam = nodeContent.getDeployModeParam();
        if (deployParam != null && !deployParam.getAccessToken().isEmpty() && !deployParam.getUrl().isEmpty()) {
            sql = String.format(
                    "--%%deployModel\n--%%url=%1$s\n--%%access_token=%2$s\n",
                    deployParam.getUrl(),
                    deployParam.getAccessToken()
            ) + sql;
        }
        return sql;
    }

    private static String renderPredictSQL(NodeInfoDTO.NodeContent nodeContent) {
        return String.format(
                "predict %1$s as %2$s\n" +
                        "where %3$s=\"%4$s\"\n" +
                        "as %5$s;",
                nodeContent.getSource(),
                nodeContent.getModel(),
                nodeContent.getPredictParam().getAuto() ? "autoSelectByMetric" : "algIndex",
                nodeContent.getPredictParam().getValue(),
                nodeContent.getTarget()
        );
    }

    private static String renderTrainSQL(NodeInfoDTO.NodeContent nodeContent) {
        Map trainParam = JacksonUtils.readJson(nodeContent.getTrainParam(), Map.class);
        String input = nodeContent.getSource();
        String output = String.format(
                "%1$s.`%2$s`",
                nodeContent.getAlgorithm(),
                nodeContent.getModelFullPath()
        );

        String sql = new TrainNodeSQLRender(nodeContent.getAlgParamDef(), trainParam).render(input, output);
        nodeContent.setAlgParamDef(null);
        return sql;
    }

    public static Map<String, List<String>> parseInputAndOutput(String nodeType, NodeInfoDTO.NodeContent nodeContent) {
        switch (nodeType.toLowerCase()) {
            case NodeType.SELECT:
                return parseSelectNode(nodeContent);
            case NodeType.LOAD:
                return parseLoadNode(nodeContent);
            case NodeType.SAVE:
                return parseSaveNode(nodeContent);
            case NodeType.TRAIN:
                return parseTrainNode(nodeContent);
            case NodeType.PREDICT:
                return parsePredictNode(nodeContent);
            case NodeType.REGISTER:
                return parseRegisterNode(nodeContent);
            case NodeType.ET:
                return parseETNode(nodeContent);
            default:
                throw new ByzerException(ErrorCodeEnum.UNSUPPORTED_NODE_TYPE);
        }
    }

    private static Map<String, List<String>> parseETNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> input;
        List<String> output;
        if (etUsageCheck(nodeContent.getEtUsageName(), "train")) {
            input = Lists.newArrayList();
            input.add(nodeContent.getSource());
            output = Lists.newArrayList();

            output.add(String.format(
                    "%1$s.`%2$s`",
                    nodeContent.getAlgorithm(),
                    nodeContent.getModelFullPath()
            ));

        } else {
            input = nodeContent.getInputParam().stream().map(ParamValueMap::getValue).collect(Collectors.toList());
            output = nodeContent.getOutputParam().stream().map(ParamValueMap::getValue).collect(Collectors.toList());
        }
        result.put("input", input);
        result.put("output", output);
        return result;
    }

    public static String renderSelectSQL(NodeInfoDTO.NodeContent nodeContent) {
        return nodeContent.getSql();
    }

    private static String parseExtraParams(Map<String, String> extraParams) {
        if (extraParams != null) {

            List<String> extraParamStrings = Lists.newArrayList();
            extraParams.forEach((key, value) -> {
                if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                    extraParamStrings.add(
                            String.format(
                                    "%1$s=%2$s",
                                    key,
                                    value.contains("\"") ?
                                            String.format("'''%1$s'''", value) :
                                            String.format("\"%1$s\"", value)
                            )
                    );
                }
            });
            if (extraParamStrings.size() > 0) {
                return "where " + String.join(" and ", extraParamStrings);
            }
        }
        return "";
    }

    public static String renderLoadSQL(NodeInfoDTO.NodeContent nodeContent) {
        String extraParamString = parseExtraParams(nodeContent.getExtraParams());
        switch (nodeContent.getDatasourceType().toLowerCase()) {
            case "jdbc": {
                return String.format(
                        "load jdbc.`%1$s.%2$s` as %3$s;",
                        nodeContent.getConnectionName(),
                        nodeContent.getSource(),
                        nodeContent.getTarget()

                );
            }
            case "deltalake":
                return String.format(
                        "load delta.`%3$s.%1$s` as %2$s;",
                        nodeContent.getSource(),
                        nodeContent.getTarget(),
                        nodeContent.getDatabase()
                );
            case "hive": {
                return String.format(
                        "load hive.`%3$s.%1$s` as %2$s;",
                        nodeContent.getSource(),
                        nodeContent.getTarget(),
                        nodeContent.getDatabase()
                );
            }
            case "hdfs": {
                if (nodeContent.getDataType().equalsIgnoreCase("csv")) {

                    return String.format(
                            "load csv.`%1$s` %3$s as %2$s;",
                            nodeContent.getSource(),
                            nodeContent.getTarget(),
                            extraParamString
                    );
                }
                return String.format(
                        "load %1$s.`%2$s` as %3$s;",
                        nodeContent.getDataType().toLowerCase(),
                        nodeContent.getSource(),
                        nodeContent.getTarget()
                );
            }
            default:
                throw new ByzerException(ErrorCodeEnum.UNSUPPORTED_DATASOURCE);
        }
    }

    public static String renderSaveSQL(NodeInfoDTO.NodeContent nodeContent) {
        switch (nodeContent.getDatasourceType().toLowerCase()) {
            case "jdbc": {
                return String.format(
                        "save %1$s %2$s as jdbc.`%3$s.%4$s`;",
                        nodeContent.getMode().toLowerCase(),
                        nodeContent.getSource(),
                        nodeContent.getConnectionName(),
                        nodeContent.getTarget()
                );

            }
            case "deltalake":
                return String.format(
                        "save %1$s %2$s as delta.`%3$s.%4$s`;",
                        nodeContent.getMode().toLowerCase(),
                        nodeContent.getSource(),
                        nodeContent.getDatabase(),
                        nodeContent.getTarget()
                );
            case "hive": {
                return String.format(
                        "save %1$s %2$s as hive.`%3$s.%4$s`;",
                        nodeContent.getMode().toLowerCase(),
                        nodeContent.getSource(),
                        nodeContent.getDatabase(),
                        nodeContent.getTarget()
                );
            }
            case "hdfs": {
                return String.format(
                        "save %1$s %2$s as %3$s.`%4$s`;",
                        nodeContent.getMode().toLowerCase(),
                        nodeContent.getSource(),
                        nodeContent.getDataType().toLowerCase(),
                        nodeContent.getTarget()
                );
            }
            default:
                throw new ByzerException(ErrorCodeEnum.UNSUPPORTED_DATASOURCE);
        }
    }

    public static Map<String, List<String>> parseSelectNode(NodeInfoDTO.NodeContent nodeContent) {
        return SQLParser.parseSQLSelectTable(renderSQL(NodeType.SELECT, nodeContent));
    }

    public static Map<String, List<String>> parseSaveNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> input = Lists.newArrayList();
        input.add(nodeContent.getSource());
        result.put("input", input);
        result.put("output", Lists.newArrayList());
        return result;
    }

    public static Map<String, List<String>> parseLoadNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> output = Lists.newArrayList();
        output.add(nodeContent.getTarget());
        result.put("input", Lists.newArrayList());
        result.put("output", output);
        return result;
    }

    public static Map<String, List<String>> parseTrainNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> input = Lists.newArrayList();
        input.add(nodeContent.getSource());
        List<String> output = Lists.newArrayList();

        output.add(String.format(
                "%1$s.`%2$s`",
                nodeContent.getAlgorithm(),
                nodeContent.getModelFullPath()
        ));
        result.put("input", input);
        result.put("output", output);
        return result;
    }

    public static Map<String, List<String>> parsePredictNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> input = Lists.newArrayList();
        input.add(nodeContent.getSource());
        input.add(nodeContent.getModel());
        List<String> output = Lists.newArrayList();
        output.add(nodeContent.getTarget());
        result.put("input", input);
        result.put("output", output);
        return result;
    }

    public static Map<String, List<String>> parseRegisterNode(NodeInfoDTO.NodeContent nodeContent) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> input = Lists.newArrayList();
        input.add(nodeContent.getSource());
        List<String> output = Lists.newArrayList();
        output.add(nodeContent.getTarget());
        result.put("input", input);
        result.put("output", output);
        return result;
    }

    public static String renderNodeSQL(NodeInfo nodeInfo, Map<Integer, ConnectionInfo> connectionMap, Map<String, List<ParamDefDTO>> algParam) {
        NodeInfoDTO.NodeContent content = JacksonUtils.readJson(nodeInfo.getContent(), NodeInfoDTO.NodeContent.class);
        if (content != null) {
            Integer connectionId = content.getConnection();
            if (connectionId != null && connectionMap.containsKey(connectionId)) {
                content.setConnectionName(connectionMap.get(connectionId).getName());
            }
        }
        if (nodeInfo.getType().equalsIgnoreCase(NodeType.TRAIN) && content != null) {
            content.setAlgParamDef(algParam.get(content.getAlgorithm()));
        }
        return renderSQL(nodeInfo.getType(), content);
    }


    public static Boolean etUsageCheck(String usageName, String usage){
        return usageName.split("-")[0].equalsIgnoreCase(usage);
    }

}
