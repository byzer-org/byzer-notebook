package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class EntityMap {
    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_id")
    private Integer entityId;

    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("commit_id")
    private String commitId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;
}
