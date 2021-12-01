package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ModifyScheduleReq {


    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("modification")
    private EntityModification modification;

    @JsonProperty("scheduler_id")
    private Integer schedulerId;

    @JsonProperty("schedule")
    private ScheduleSetting schedule;

    @JsonProperty("extra")
    private Map<String, String> extra;


}
