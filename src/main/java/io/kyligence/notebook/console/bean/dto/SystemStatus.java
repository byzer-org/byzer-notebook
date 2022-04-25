package io.kyligence.notebook.console.bean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SystemStatus {
    private String status;
    private String msg;
    private List<EngineStatusDTO> engineStatus;
}
