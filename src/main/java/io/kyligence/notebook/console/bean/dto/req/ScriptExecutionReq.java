package io.kyligence.notebook.console.bean.dto.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import javax.validation.constraints.NotBlank;
import java.io.UnsupportedEncodingException;

@Data
@NoArgsConstructor
public class ScriptExecutionReq {

    @NotBlank
    @JsonProperty("sql")
    private String sql;

    @JsonProperty("notebook")
    private String notebook;

    @JsonProperty("cell_id")
    private Integer cellId;

    @JsonProperty("encType")
    private String encType;

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("timeout")
    private Integer timeout;

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
