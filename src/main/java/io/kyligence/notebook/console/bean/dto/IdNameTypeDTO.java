package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdNameTypeDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    public static IdNameTypeDTO valueOf(Integer id, String name, String type) {
        IdNameTypeDTO result = new IdNameTypeDTO();
        result.setId(EntityUtils.toStr(id));
        result.setName(name);
        result.setType(type);
        return result;
    }
}
