package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ScheduleCallbackReq {

    private Map<String, Object> userParams = new HashMap<>();

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

    @JsonProperty("timeout")
    private Integer timeout;

    @JsonAnyGetter
    public Map<String, Object> getUserParams() {
        return this.userParams;
    }

    @JsonAnySetter
    public void setUserParam(String name, Object value) {
        this.userParams.put(name, value);
    }
}
