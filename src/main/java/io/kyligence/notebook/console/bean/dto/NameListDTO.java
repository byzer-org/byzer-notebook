package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NameListDTO {

    @JsonProperty("list")
    private List<String> nameList;

    public static NameListDTO valueOf(List<String> names) {
        NameListDTO nameListDTO = new NameListDTO();
        if (names != null && names.size() != 0) {
            nameListDTO.setNameList(names);
        }
        return nameListDTO;
    }
}
