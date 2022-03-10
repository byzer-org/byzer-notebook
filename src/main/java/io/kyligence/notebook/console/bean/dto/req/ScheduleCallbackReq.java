package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class ScheduleCallbackReq {

    @NotBlank
    @JsonProperty("token")
    private String token;

    @NotBlank
    @JsonProperty("owner")
    private String user;

    @NotBlank
    @JsonProperty("entity_type")
    private String entityType;

    @NotBlank
    @JsonProperty("entity_id")
    private String entityId;

    @JsonProperty("commit_id")
    private String commitId;
}
