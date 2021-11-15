package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class MoveExecFileReq {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("folder_id")
    private Integer folderId;

    @NotNull
    @JsonProperty("type")
    private String type;
}
