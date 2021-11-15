package io.kyligence.notebook.console.bean.dto.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class ScriptExecutionReq {

    @NotBlank
    @JsonProperty("sql")
    private String sql;

    @JsonProperty("notebook")
    private String notebook;

    @JsonProperty("cell_id")
    private Integer cellId;

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("timeout")
    private Integer timeout;

}
