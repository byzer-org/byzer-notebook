package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.EntityMap;
import io.kyligence.notebook.console.scheduler.dolphin.dto.TaskInstance;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TaskNodeInfoDTO {
    private Long id;
    private String name;

    @JsonProperty("entity")
    private EntityMap entityMap;

    private String state;
    private String log;
    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    private String duration;

    public static TaskNodeInfoDTO valueOf(TaskInstance task) {
        TaskNodeInfoDTO dto = new TaskNodeInfoDTO();
        dto.setId(task.getId());
        dto.setName(task.getName().split("#_#", 2)[0]);
        dto.setEntityMap(TaskInstance.getEntityMap(task));
        dto.setState(task.getState());
        dto.setDuration(task.getDuration());
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        return dto;
    }
}
