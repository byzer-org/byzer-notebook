package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import io.kyligence.notebook.console.support.EncryptUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class ConnectionUtils {
    public static final String PROBE_SQL_PARAM_NAME = "probeSQL";

    public static String renderSQL(String url, String driver, String user,
                                   String password, String name,
                                   List<ConnectionDTO.ParameterMap> parameter) {

        String template = "connect jdbc where\n" +
                "url=\"%1$s\"\n" +
                "and driver=\"%2$s\"\n" +
                "and user=\"%3$s\"\n" +
                "and password=\"%4$s\"\n";

        String formatted = String.format(template, url, driver, user, EncryptUtils.decrypt(password));
        StringBuilder builder = new StringBuilder(formatted);

        if (parameter != null) {
            parameter.forEach(parameterMap -> {
                if (StringUtils.isNotBlank(parameterMap.getName()) &&
                        !parameterMap.getName().equalsIgnoreCase(PROBE_SQL_PARAM_NAME) &&
                        StringUtils.isNotBlank(parameterMap.getValue())) {
                    builder.append(
                            String.format("and %1$s=\"%2$s\"\n", parameterMap.getName(), parameterMap.getValue())
                    );
                }
            });
        }

        builder.append(String.format("as %1$s;", name));
        return builder.toString();
    }

    public static String renderSQL(ConnectionDTO content) {
        String connectionName = content.getName();
        return renderSQL(
                content.getUrl(),
                content.getDriver(),
                content.getUserName(),
                content.getPassword(),
                connectionName,
                content.getParameter()
        );
    }

    public static String parseProbeSql(List<ConnectionDTO.ParameterMap> userParameters) {
        if (Objects.nonNull(userParameters)) {
            for (ConnectionDTO.ParameterMap parameterMap : userParameters) {
                if (StringUtils.isNotBlank(parameterMap.getName()) &&
                        parameterMap.getName().equalsIgnoreCase(PROBE_SQL_PARAM_NAME) &&
                        StringUtils.isNotBlank(parameterMap.getValue())) {
                    return parameterMap.getValue();
                }
            }
        }
        return "select 1";
    }
}
