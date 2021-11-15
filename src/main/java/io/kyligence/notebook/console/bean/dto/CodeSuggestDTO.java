package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CodeSuggestDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("metaTable")
    private String metaTable;

    // for now, extra structure like "{"desc":""}"
    @JsonProperty("extra")
    private Map<String, String> extra;
}
