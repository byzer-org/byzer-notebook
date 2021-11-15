package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdNameDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    public static IdNameDTO valueOf(Integer id, String name) {
        IdNameDTO idNameDTO = new IdNameDTO();
        idNameDTO.setId(EntityUtils.toStr(id));
        idNameDTO.setName(name);
        return idNameDTO;
    }
}
