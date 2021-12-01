package io.kyligence.notebook.console.scheduler;

import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;

import java.util.List;
import java.util.Map;

public interface RemoteSchedulerInterface {
    void createTask(String user, String name, String description, String entityType, Integer entityId, String entityName, ScheduleSetting scheduleSetting, Map<String, String> extraSettings);
    void deleteTask(String user, String projectName, Integer taskId);
    void updateTask(String user, Integer taskId, String name, String description, EntityModification modification, ScheduleSetting scheduleSetting, Map<String, String> extraSettings);
    TaskInfoDTO getTask(String projectName, String user, Integer taskId);
    TaskInfoDTO getTask(String projectName, String user, String entityType, Integer entityId);
    List<TaskInfoDTO> getTasks(String projectName, String user);
    void getTask(String user);
    void getTask();
    String getServiceName();

    TaskInfoDTO searchForEntity(String entityName, String entityType, Integer entityId);
}
