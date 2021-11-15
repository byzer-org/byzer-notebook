package io.kyligence.notebook.console.bean.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScriptExecutionResp {

    @JsonProperty("job_id")
    private String jobId;

}
