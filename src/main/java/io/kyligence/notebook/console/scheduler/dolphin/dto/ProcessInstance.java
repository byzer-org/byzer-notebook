package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ProcessInstance {

    private Long id;
    private Integer processDefinitionId;
    private String state;
    private String recovery;
    private Date startTime;
    private Date endTime;
    private Integer runTimes;
    private String name;
    private String host;
    private String processDefinition;
    private String commandType;
    private String commandParam;
    private String taskDependType;
    private Integer maxTryTimes;
    private String failureStrategy;
    private String warningType;
    private String warningGroupId;
    private Date scheduleTime;
    private String commandStartTime;
    private String globalParams;
    private String processInstanceJson;
    private Integer executorId;
    private String executorName;
    private String tenantCode;
    private String queue;
    private String isSubProcess;
    private String locations;
    private String connects;
    private String historyCmd;
    private String dependenceScheduleTimes;
    private String duration;
    private String processInstancePriority;
    private String workerGroup;
    private Integer timeout;
    private Integer tenantId;
    private String receivers;
    private String receiversCc;
    private String cmdTypeIfComplement;
    private Boolean complementData;
    private Boolean processInstanceStop;

}
