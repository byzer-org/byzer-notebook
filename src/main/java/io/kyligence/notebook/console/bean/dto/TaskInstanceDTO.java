package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.scheduler.dolphin.dto.ProcessInstance;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class TaskInstanceDTO {
    private Long id;

    @JsonProperty("task_id")
    private Integer taskId;

    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("state")
    private String state;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("start_time")
    private Date startTime;

    @JsonProperty("end_time")
    private Date endTime;

    @JsonProperty("duration")
    private String duration;


    public static TaskInstanceDTO valueOf(ProcessInstance processInstance, String user, String namePrefix){
        TaskInstanceDTO dto = new TaskInstanceDTO();
        dto.setId(processInstance.getId());
        dto.setTaskId(processInstance.getProcessDefinitionId());
        dto.setDuration(processInstance.getDuration());
        dto.setState(processInstance.getState());
        dto.setStartTime(processInstance.getStartTime());
        dto.setEndTime(processInstance.getEndTime());
        dto.setOwner(user);
        dto.setTaskName(processInstance.getName().replace(namePrefix, "")
                .replaceAll("-\\d+-\\d+$", ""));
        return dto;
    }
}
