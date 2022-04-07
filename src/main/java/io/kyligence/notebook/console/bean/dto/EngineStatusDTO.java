package io.kyligence.notebook.console.bean.dto;

import io.kyligence.notebook.console.util.EngineStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EngineStatusDTO {
    private String name;

    private Integer status;

    private Double usage;

    public static EngineStatusDTO valueOf(String engineName, EngineStatus engineStatus, Double engineUsage) {
        EngineStatusDTO dto = new EngineStatusDTO();
        dto.setName(engineName);
        dto.setStatus(engineStatus.getCode());
        dto.setUsage(engineUsage);
        return dto;
    }
}
