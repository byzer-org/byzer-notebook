package io.kyligence.notebook.console.scheduler.dolphin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.EntityMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EntityModification {

    public interface Actions {
        String update = "update";
        String remove = "remove";
    }

    @JsonProperty("action")
    private String action;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("entity_id")
    private Integer entityId;

    @JsonProperty("attach_to")
    private List<EntityMap> attachTo;

    @JsonProperty("commit_id")
    private String commitId;

    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("task_desc")
    private String taskDesc;
}