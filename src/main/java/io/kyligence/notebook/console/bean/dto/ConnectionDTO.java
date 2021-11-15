package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.support.EncryptUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ConnectionDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty(value = "datasource", required = true)
    private String datasource;

    @JsonProperty(value = "driver", required = true)
    private String driver;

    @JsonProperty("name")
    private String name;

    @JsonProperty(value = "url", required = true)
    private String url;

    @JsonProperty(value = "username", required = true)
    private String userName;

    @JsonProperty("password")
    private String password;

    @JsonProperty("parameter")
    private List<ParameterMap> parameter;

    @JsonProperty("status")
    private String status;

    @Data
    @NoArgsConstructor
    public static class ParameterMap {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;
    }


    public static ConnectionDTO valueOf(ConnectionInfo connectionInfo) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setId(connectionInfo.getId());
        connectionDTO.setDriver(connectionInfo.getDriver());
        connectionDTO.setDatasource(connectionInfo.getDatasource());
        connectionDTO.setName(connectionInfo.getName());
        connectionDTO.setUrl(connectionInfo.getUrl());
        connectionDTO.setUserName(connectionInfo.getUserName());
        connectionDTO.setPassword(EncryptUtils.decrypt(connectionInfo.getPassword()));
        connectionDTO.setParameter(JacksonUtils.readJsonArray(connectionInfo.getParameter(), ParameterMap.class));
        return connectionDTO;
    }

    public static ConnectionDTO valueOf(ConnectionInfo connectionInfo, boolean status) {
        ConnectionDTO connectionDTO = valueOf(connectionInfo);
        if (status) {
            connectionDTO.setStatus("success");
        } else {
            connectionDTO.setStatus("error");
        }
        return connectionDTO;
    }

}