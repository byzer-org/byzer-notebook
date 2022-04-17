package io.kyligence.notebook.console.scheduler.dolphin.dto;

import io.kyligence.notebook.console.bean.model.EntityMap;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;


@Data
@NoArgsConstructor
public class ModifyProcessDTO {

    private String id;
    private String processDefinitionJson;
    private String name;
    private String description;
    private String locations;
    private String connects;

    public static ModifyProcessDTO create(String processName, String description, String entityName,
                                          String entityType, Integer entityId, String commitId,
                                          String taskName, String taskDesc, Integer taskTimeout,
                                          String owner, String token, String callbackUrl,
                                          Integer maxRetryTimes, Integer retryInterval,
                                          Integer timeout, Integer tenantId) {
        ProcessInfo processInfo = ProcessInfo.valueOf(
                processName, description, entityName, entityType,
                entityId, commitId, taskName, taskDesc,
                owner, token, callbackUrl, maxRetryTimes, retryInterval,
                timeout, tenantId, taskTimeout
        );
        ModifyProcessDTO dto = new ModifyProcessDTO();

        dto.setName(processName);
        dto.setDescription(description);
        dto.setProcessDefinitionJson(JacksonUtils.writeJson(processInfo.getProcessDefinition()));
        dto.setConnects(JacksonUtils.writeJson(processInfo.getConnections()));
        dto.setLocations(JacksonUtils.writeJson(processInfo.getLocationMap()));
        return dto;
    }

    public static ModifyProcessDTO modify(ProcessInfo processInfo, String name, String description) {
        ModifyProcessDTO dto = new ModifyProcessDTO();

        dto.setName((Objects.nonNull(name))  ? name : processInfo.getName() );
        dto.setDescription((Objects.nonNull(description)) ? description : processInfo.getDescription());
        dto.setId(String.valueOf(processInfo.getId()));
        dto.setProcessDefinitionJson(JacksonUtils.writeJson(processInfo.getProcessDefinition()));
        dto.setConnects(JacksonUtils.writeJson(processInfo.getConnections()));
        dto.setLocations(JacksonUtils.writeJson(processInfo.getLocationMap()));
        return dto;
    }

    public static ModifyProcessDTO modify(ProcessInfo processInfo, String name, String description, String entityName,
                                          String entityType, Integer entityId, String commitId,
                                          String owner, String token, String callbackUrl,
                                          Integer maxRetryTimes, Integer retryInterval,
                                          List<EntityMap> attachTo, String taskName,
                                          String taskDesc, Integer taskTimeout) {
        processInfo.modify(entityName, entityType, entityId, commitId, taskName, taskDesc,
                owner, token, callbackUrl, maxRetryTimes, retryInterval, taskTimeout, attachTo);
        ModifyProcessDTO dto = new ModifyProcessDTO();

        dto.setName((Objects.nonNull(name))  ? name : processInfo.getName() );
        dto.setDescription((Objects.nonNull(description)) ? description : processInfo.getDescription());

        dto.setId(String.valueOf(processInfo.getId()));
        dto.setProcessDefinitionJson(JacksonUtils.writeJson(processInfo.getProcessDefinition()));
        dto.setConnects(JacksonUtils.writeJson(processInfo.getConnections()));
        dto.setLocations(JacksonUtils.writeJson(processInfo.getLocationMap()));
        return dto;
    }

    public static ModifyProcessDTO remove(ProcessInfo processInfo, String entityType, Integer entityId) {
        processInfo.remove(entityType, entityId);
        ModifyProcessDTO dto = new ModifyProcessDTO();

        dto.setName(processInfo.getName());
        dto.setDescription(processInfo.getDescription());
        dto.setId(String.valueOf(processInfo.getId()));
        dto.setProcessDefinitionJson(JacksonUtils.writeJson(processInfo.getProcessDefinition()));
        dto.setConnects(JacksonUtils.writeJson(processInfo.getConnections()));
        dto.setLocations(JacksonUtils.writeJson(processInfo.getLocationMap()));
        return dto;
    }
}
