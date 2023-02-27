package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ExpandIncludeDTO {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("sql")
    private String sql;

}
