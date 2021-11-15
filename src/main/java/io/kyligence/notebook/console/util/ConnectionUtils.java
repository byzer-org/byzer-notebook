package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import io.kyligence.notebook.console.support.EncryptUtils;

import java.util.List;

public class ConnectionUtils {

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
                if (parameterMap.getName() != null && !parameterMap.getName().isEmpty()){
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
}
