package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CreateScheduleReq {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("scheduler_id")
    private Integer schedulerId;

    @JsonProperty("entity_id")
    private Integer entityId;

    @JsonProperty("schedule")
    private ScheduleSetting schedule;

    @JsonProperty("extra")
    private Map<String, String> extra;


}
