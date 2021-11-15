package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParamValueMap {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    public static ParamValueMap valueOf(String name, String value){
        ParamValueMap map = new ParamValueMap();
        map.setName(name);
        map.setValue(value);
        return map;
    }
}
