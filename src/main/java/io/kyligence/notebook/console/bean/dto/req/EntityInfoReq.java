package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EntityInfoReq {
    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_id")
    private Integer entityId;

    @JsonProperty("entity_name")
    private String entityName;

}

