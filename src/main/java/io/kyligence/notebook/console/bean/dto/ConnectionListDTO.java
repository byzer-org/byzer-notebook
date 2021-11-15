package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ConnectionListDTO {

    @JsonProperty("connection_list")
    private List<ConnectionDTO> connectionDTOList;

    public static ConnectionListDTO valueOf(List<ConnectionInfo> connectionInfoList, Map<Integer, Boolean> statusMap) {
        List<ConnectionDTO> connections = new ArrayList<>();
        connectionInfoList.forEach(connectionInfo ->
            connections.add(
                    ConnectionDTO.valueOf(
                            connectionInfo,
                            statusMap.getOrDefault(connectionInfo.getId(), false)
                    )
            )
        );
        ConnectionListDTO connectionListDTO = new ConnectionListDTO();
        connectionListDTO.setConnectionDTOList(connections);
        return connectionListDTO;
    }
}
