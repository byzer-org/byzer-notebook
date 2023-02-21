package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;

@Data
@NoArgsConstructor
public class CodeSuggestionReq {

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("encType")
    private String encType;

    @JsonProperty("lineNum")
    private Long lineNum;

    @JsonProperty("columnNum")
    private Long columnNum;

    @JsonProperty("isDebug")
    private Boolean isDebug;

    public String getSql() {
        if (encType != null && encType.equals("base64")) {
            try {
                return new String(Base64.decodeBase64(sql), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return sql;
            }
        }
        return sql;
    }

}
