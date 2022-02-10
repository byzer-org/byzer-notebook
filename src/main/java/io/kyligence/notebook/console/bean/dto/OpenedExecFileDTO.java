package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpenedExecFileDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("is_demo")
    private Boolean isDemo;

    @JsonProperty("uniq")
    private String uniq;

    @JsonProperty("commit_id")
    private String commitId;

    @JsonProperty("active")
    private String active;

    @JsonProperty("type")
    private String type;
}
