package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.dto.ETParamDTO;
import lombok.Data;

@Data
public class ParamConstraint {

    @JsonProperty("min")
    private Double min;

    @JsonProperty("max")
    private Double max;

    @JsonProperty("max_length")
    private Integer maxLength;

    @JsonProperty("array_value_type")
    private ETParamDTO.ArrayValueType arrayValueType;
}
