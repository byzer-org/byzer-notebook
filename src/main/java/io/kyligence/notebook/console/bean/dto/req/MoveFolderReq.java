package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoveFolderReq {

    @JsonProperty("current_folder_id")
    private Integer currentFolderId;

    @JsonProperty("target_folder_id")
    private Integer targetFolderId;
}
