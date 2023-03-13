package io.kyligence.notebook.console.scheduler;

import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.dto.TaskInstanceDTO;
import io.kyligence.notebook.console.bean.dto.TaskNodeInfoDTO;
import io.kyligence.notebook.console.bean.dto.UserParamsDTO;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;

import java.util.List;
import java.util.Map;

public interface RemoteSchedulerInterface {
    void createTask(String user, String name, String description, String entityType, Integer entityId,
                    String commitId, String taskName, String taskDesc, String entityName,
                    Integer taskTimeout, ScheduleSetting scheduleSetting, Map<String, String> extraSettings, List<UserParamsDTO> userParams);

    void deleteTask(String user, String projectName, Integer taskId);

    void updateTask(String user, Integer taskId, String name, String description, EntityModification modification, ScheduleSetting scheduleSetting, Map<String, String> extraSettings);

    void runTask(String projectName, String user, Integer taskId);

    TaskInfoDTO getTask(String projectName, String user, Integer taskId);

    TaskInfoDTO getTask(String projectName, String user, String entityType, Integer entityId);

    List<TaskInfoDTO> getTasks(String projectName, String user);

    List<TaskInstanceDTO> getTaskInstances(String projectName, String user);

    List<TaskInstanceDTO> getTaskInstances(String projectName, String user, Integer taskId);

    void sendCommand(String projectName, String user, Long taskInstanceId, Integer commandCode);

    void onlineTask(String user, Integer taskId, String projectName);

    void offlineTask(String user, Integer taskId, String projectName);

    List<TaskNodeInfoDTO> getTaskInstanceNodes(String projectName, String user, Long taskInstanceId);

    String getTaskInstanceStatus(String projectName, String user, Long taskInstanceId);

    void getTask(String user);

    void getTask();

    String getServiceName();

    TaskInfoDTO searchForEntity(String entityName, String entityType, Integer entityId);
}
