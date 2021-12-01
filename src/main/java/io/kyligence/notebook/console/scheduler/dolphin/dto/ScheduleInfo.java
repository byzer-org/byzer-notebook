package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ScheduleInfo {

    private int id;
    private int processDefinitionId;
    private String processDefinitionName;
    private String projectName;
    private String definitionDescription;
    private String startTime;
    private String endTime;
    private String crontab;
    private String failureStrategy;
    private String warningType;
    private String createTime;
    private String updateTime;
    private int userId;
    private String userName;
    private String releaseState;
    private int warningGroupId;
    private String processInstancePriority;
    private String workerGroup;
}
