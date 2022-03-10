package io.kyligence.notebook.console.scheduler.dolphin.dto;

import io.kyligence.notebook.console.bean.model.EntityMap;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class TaskInstance {
    private Long id;
    private String name;
    private String taskType;
    private Integer processDefinitionId;
    private Long processInstanceId;
    private String processInstanceName;
    private String taskJson;
    private String state;
    private String submitTime;
    private String startTime;
    private String endTime;
    private String host;
    private String executePath;
    private String logPath;
    private Integer retryTimes;
    private String alertFlag;
    private String processInstance;
    private String processDefine;
    private Integer pid;
    private String appLink;
    private String flag;
    private String dependency;
    private String duration;
    private Integer maxRetryTimes;
    private Integer retryInterval;
    private String taskInstancePriority;
    private String processInstancePriority;
    private String dependentResult;
    private String workerGroup;
    private Integer executorId;
    private String executorName;
    private String resources;
    private Boolean subProcess;
    private Boolean taskSuccess;
    private Boolean taskComplete;
    private Boolean dependTask;
    private Boolean conditionsTask;

    public static EntityMap getEntityMap(TaskInstance task){
        Map taskJson = JacksonUtils.readJson(task.getTaskJson(), Map.class);
        String typeAndId = (String) taskJson.get("id");
        String[] l = typeAndId.split("-");
        EntityMap r = new EntityMap();
        r.setEntityId(Integer.parseInt(l[1]));
        r.setEntityType(l[0]);

        ProcessInfo.Params params = JacksonUtils.readJson((String) taskJson.get("params"), ProcessInfo.Params.class);
        if (Objects.nonNull(params)) {
            for (ProcessInfo.HttpParam p: params.getHttpParams()) {
                if (p.getProp().equalsIgnoreCase("commit_id")){
                    r.setCommitId(p.getValue());
                    break;
                }
            }
        }
        return r;
    }
}
