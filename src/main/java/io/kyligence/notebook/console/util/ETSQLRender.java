package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.bean.dto.NodeInfoDTO;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ETSQLRender {
    final static String paramPlaceHolder = "{{parameters}}";

    final static String normalFormat = "%1$s=\"%2$s\"";

    final static String groupFormat = "`fitParam.%3$s.%1$s`=\"%2$s\"";

    public static String renderKey(String template, List<ParamValueMap> values) {
        String result = template;
        for (ParamValueMap p : values) {
            if (p.getName() != null && p.getValue() != null) {
                result = result.replace("$" + p.getName(), p.getValue());
            }
        }
        return result;
    }

    public static String render(NodeInfoDTO.NodeContent nodeContent) {
        String template = renderKey(nodeContent.getEtUsage(), nodeContent.getInputParam());
        template = renderKey(template, nodeContent.getOutputParam());
        String normalString = renderNormal(nodeContent.getParamList());
        String groupString = renderGroup(nodeContent.getGroup());
        List<String> paramString = Lists.newArrayList();

        if (normalString != null && !normalString.isEmpty()) paramString.add(normalString);
        if (groupString != null && !groupString.isEmpty()) paramString.add(groupString);

        if (paramString.isEmpty()){
            template = template.replace(" where {{parameters}}", "");
        }
        return template.replace(paramPlaceHolder, String.join("\n\nand ", paramString));
    }

    public static String renderNormal(List<ParamValueMap> paramList) {
        if (paramList == null) return null;
        List<String> stringList = paramList.stream().map(pair -> {
            if (pair.getName() != null && pair.getValue() != null) {
                return String.format(
                        pair.getValue().contains("\"") ? normalFormat.replace("\"", "'''") : normalFormat,
                        pair.getName(),
                        pair.getValue()
                );
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return String.join("\nand ", stringList);
    }

    public static String renderGroup(List<List<ParamValueMap>> groups) {
        if (groups == null) return null;
        List<String> groupStringList = Lists.newArrayList();
        for (int i = 0; i < groups.size(); i++) {
            List<String> groupString = Lists.newArrayList();
            for (int j = 0; j < groups.get(i).size(); j++) {
                ParamValueMap pair = groups.get(i).get(j);
                if (pair.getValue() != null && pair.getName() != null) {
                    groupString.add(String.format(
                            pair.getValue().contains("\"") ? groupFormat.replace("\"", "'''") : groupFormat,
                            pair.getName(),
                            pair.getValue(),
                            i
                    ));
                }
            }
            groupStringList.add(String.join("\nand ", groupString));
        }
        return String.join("\nand ", groupStringList);
    }
}
