package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class CloneFolderReq {

    @JsonProperty("id")
    private Integer id;

    @Pattern(regexp = "[a-zA-Z0-9_.\\u4e00-\\u9fa5]+$", message = "The folder name can only contain numbers, letters, Chinese characters, and underscores")
    @JsonProperty("name")
    private String name;
}
