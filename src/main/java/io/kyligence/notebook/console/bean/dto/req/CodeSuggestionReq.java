package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@NoArgsConstructor
public class CodeSuggestionReq {

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("lineNum")
    private Long lineNum;

    @JsonProperty("columnNum")
    private Long columnNum;

    @JsonProperty("isDebug")
    private Boolean isDebug;

}
