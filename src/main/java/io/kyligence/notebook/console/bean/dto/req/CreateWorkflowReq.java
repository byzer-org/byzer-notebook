package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class CreateWorkflowReq {

    @Pattern(regexp = "[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "The workflow name can only contain numbers, letters, Chinese characters, and underscores")
    @JsonProperty("name")
    private String name;

    @JsonProperty("folder_id")
    private Integer folderId;
}
