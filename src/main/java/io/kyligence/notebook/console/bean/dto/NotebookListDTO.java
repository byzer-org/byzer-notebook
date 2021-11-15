package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NotebookListDTO {

    @JsonProperty("list")
    private List<NotebookDTO> notebookDTOList;


}
