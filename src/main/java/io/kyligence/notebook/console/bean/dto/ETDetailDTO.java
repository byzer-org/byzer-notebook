package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ETDetailDTO {

    @JsonProperty("usage")
    private String usage;

    @JsonProperty("params")
    private List<ETParamDTO> params;
}
