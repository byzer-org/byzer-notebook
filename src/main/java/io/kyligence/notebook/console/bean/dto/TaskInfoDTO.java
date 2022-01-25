package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.bean.model.EntityMap;
import io.kyligence.notebook.console.scheduler.dolphin.dto.ProcessInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TaskInfoDTO {
    private Integer id;
    private String name;
    private String description;
    private List<EntityMap> entities;
    private ScheduleSetting schedule;
    @JsonProperty("release_state")
    private String releaseState;
    @JsonProperty("extra")
    private Map<String, String> extraSettings;

    private List<ProcessInfo.Connects> connects;
    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;

    public static TaskInfoDTO valueOf(ProcessInfo processInfo, ScheduleSetting schedule, Map<String, String> mails) {
        TaskInfoDTO dto = new TaskInfoDTO();

        dto.setId(processInfo.getId());
        dto.setName(processInfo.getName());
        dto.setReleaseState(processInfo.getReleaseState());
        dto.setDescription(processInfo.getDescription());
        dto.setExtraSettings(mails);
        dto.setSchedule(schedule);
        dto.setEntities(
                processInfo.getProcessDefinition().getTasks()
                        .stream().map(ProcessInfo::getTaskEntityMap)
                        .collect(Collectors.toList())
        );
        dto.setConnects(processInfo.getConnections());
        dto.setCreateTime(processInfo.getCreateTime());
        dto.setUpdateTime(processInfo.getUpdateTime());
        return dto;
    }
}
