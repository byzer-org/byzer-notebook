package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdDTO {

    @JsonProperty("id")
    private String id;

    public static IdDTO valueOf(Integer id) {
        IdDTO idDTO = new IdDTO();
        idDTO.setId(EntityUtils.toStr(id));
        return idDTO;
    }

}
