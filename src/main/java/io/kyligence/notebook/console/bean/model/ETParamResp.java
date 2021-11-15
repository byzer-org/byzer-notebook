package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.ETParamUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ETParamResp {

    @JsonProperty("param")
    private String param;

    @JsonProperty("description")
    private String description;

    @JsonProperty("value")
    private String value;

    @JsonProperty("extra")
    private String extra;

    public ETParam getParamDef() {
        if (extra == null) return null;
        ETParam etParam = JacksonUtils.readJson(extra, ETParam.class);
        return ETParamUtils.handle(this, etParam);
    }
}
