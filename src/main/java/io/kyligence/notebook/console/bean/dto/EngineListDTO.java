package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EngineStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class EngineListDTO {

    @JsonProperty("list")
    private List<EngineStatusDTO> list;

    public static EngineListDTO valueOf(Map<String, Pair<EngineStatus, Double>> engineStatusMap) {
        List<EngineStatusDTO> engineList = new ArrayList<>();
        engineStatusMap.forEach((k, v) -> engineList.add(EngineStatusDTO.valueOf(k, v.getFirst(), v.getSecond())));
        EngineListDTO dto = new EngineListDTO();
        dto.setList(engineList);
        return dto;
    }
}
