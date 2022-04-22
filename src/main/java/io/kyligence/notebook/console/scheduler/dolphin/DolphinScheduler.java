package io.kyligence.notebook.console.scheduler.dolphin;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.dto.TaskInstanceDTO;
import io.kyligence.notebook.console.bean.dto.TaskNodeInfoDTO;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.scheduler.RemoteScheduler;
import io.kyligence.notebook.console.scheduler.RemoteSchedulerInterface;
import io.kyligence.notebook.console.scheduler.SchedulerConfig;
import io.kyligence.notebook.console.scheduler.dolphin.dto.*;
import io.kyligence.notebook.console.util.JacksonUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DolphinScheduler extends RemoteScheduler implements RemoteSchedulerInterface {

    // DolphinScheduler's http task socket timeout limit is 9999 seconds,
    // set callback timeout limit to 9600 seconds
    private static final Integer TASK_TIMEOUT_LIMIT = 9600;

    interface APIMapping {
        String getUserInfo = "/users/get-user-info";

        String searchProject = "/projects/list-paging?pageNo=$page&pageSize=10000&searchVal=$projectName";
        String createProject = "/projects/create";

        String createProcess = "/projects/$projectName/process/save";
        String updateProcess = "/projects/$projectName/process/update";
        String searchProcess = "/projects/$projectName/process/list-paging?pageSize=10000&pageNo=$page&searchVal=$processName";
        String processDetail = "/projects/$projectName/process/select-by-id?processId=$processId";
        String onlineProcess = "/projects/$projectName/process/release";
        String deleteProcess = "/projects/$projectName/process/delete?processDefinitionId=$processId";
        String runProcess = "/projects/$projectName/executors/start-process-instance";

        String stopProcessInstance = "/projects/$projectName/executors/execute";
        String createSchedule = "/projects/$projectName/schedule/create";
        String updateSchedule = "/projects/$projectName/schedule/update";
        String scheduleList = "/projects/$projectName/schedule/list-paging?processDefinitionId=$processId&pageNo=1&pageSize=10";
        String offlineSchedule = "/projects/$projectName/schedule/offline?id=$scheduleId";
        String onlineSchedule = "/projects/$projectName/schedule/online?id=$scheduleId";
        String deleteSchedule = "/projects/$projectName/schedule/delete?scheduleId=$scheduleId";
        String mailList = "/projects/$projectName/executors/get-receiver-cc?processDefinitionId=$processId";

        String getProcessInstanceList = "/projects/$projectName/instance/list-paging?searchVal=$prefix&pageSize=10000&pageNo=$page";
        String getProcessInstance = "/projects/$projectName/instance/select-by-id?processInstanceId=$processInstanceId";
        String getProcessInstanceTasks = "/projects/$projectName/instance/task-list-by-process-id?processInstanceId=$processInstanceId";
        String getTaskInstanceLog = "/log/detail?taskInstanceId=$taskInstanceId&skipLineNum=0&limit=10000";
    }

    final private String authToken;
    final private String defaultProject;
    final private SchedulerConfig config;
    private Integer defaultTenantId;

    public DolphinScheduler(Integer id, SchedulerConfig conf) {
        super(id, conf.getSchedulerUrl(), new RestTemplate(), "DolphinScheduler", conf.getCallbackUrl(), conf.getCallbackToken());
        config = conf;
        authToken = conf.getAuthToken();
        defaultProject = conf.getDefaultProjectName();
        setup();
    }

    @Override
    public TaskInfoDTO searchForEntity(String entityName, String entityType, Integer entityId) {
        for (String project : getProjectList()) {
            ProcessInfo process = searchProcessByEntity(project, null, entityType, entityId);
            if (Objects.nonNull(process)) {
                Map<String, String> mails = getScheduleMailList(project, process.getId());
                ScheduleSetting schedule = ScheduleSetting.valueOf(findSchedule(project, process.getId()));
                return TaskInfoDTO.valueOf(process, schedule, mails);
            }
        }
        return null;
    }

    @Override
    public void createTask(String user, String name, String description, String entityType, Integer entityId,
                           String commitId, String taskName, String taskDesc, String entityName,
                           Integer taskTimeout, ScheduleSetting scheduleSetting, Map<String, String> extraSettings) {
        String project = Objects.isNull(extraSettings) ? null : extraSettings.get("project_name");
        project = Objects.isNull(project) ? defaultProject : project;
        ensureProject(project);
        taskTimeout = Objects.isNull(taskTimeout) || taskTimeout > TASK_TIMEOUT_LIMIT ? TASK_TIMEOUT_LIMIT : taskTimeout;
        Integer taskId = createProcess(
                project, user, name, description, entityName, entityType, entityId, commitId,
                taskName, taskDesc, taskTimeout, extraSettings);
        onlineProcess(project, taskId);
        if (!ScheduleSetting.isNull(scheduleSetting)) {
            createSchedule(project, taskId, scheduleSetting, extraSettings);
        }
        offlineProcess(project, taskId);
    }

    @Override
    public void deleteTask(String user, String projectName, Integer taskId) {
        if (Objects.isNull(projectName)) projectName = defaultProject;
        ProcessInfo processInfo = findProcess(user, taskId, projectName);
        if (processInfo.getReleaseState().equalsIgnoreCase("ONLINE")) {
            throw new ByzerException(
                    MessageFormat.format(
                            "Task: {0} is currently online.",
                            processInfo.getName().replace(genTaskNamePrefix(user), "")
                    ));
        }
        deleteProcess(projectName, taskId);
    }

    @Override
    public void updateTask(String user, Integer taskId, String name, String description, EntityModification modification,
                           ScheduleSetting scheduleSetting, Map<String, String> extraSettings) {
        String project = Objects.isNull(extraSettings) ? null : extraSettings.get("project_name");
        project = Objects.isNull(project) ? defaultProject : project;
        ensureProject(project);

        // update process definition
        ProcessInfo processInfo = findProcess(user, taskId, project);
        if (Objects.nonNull(modification)) {
            ProcessInfo exist = searchProcessByEntity(project, user, modification.getEntityType(), modification.getEntityId());
            if (Objects.nonNull(exist) && exist.getId() != processInfo.getId()) {
                throw new ByzerException(
                        MessageFormat.format("{0}: {1} already used in schedule {3}",
                                modification.getEntityType(), modification.getEntityName(),
                                exist.getName()
                        )
                );
            }
        }

        if (Objects.nonNull(name)) {
            Integer exist = searchProcessByName(project, genTaskNamePrefix(user) + name);
            if (Objects.nonNull(exist) && exist != processInfo.getId()) {
                throw new ByzerException(
                        MessageFormat.format("Schedule name: [{0}] already exist.", name)
                );
            }
        }

        offlineProcess(project, taskId);
        updateProcess(project, user, processInfo, name, description, modification, extraSettings);
        onlineProcess(project, taskId);

        ScheduleInfo scheduleInfo = findSchedule(project, taskId);


        // handle schedule update
        if (Objects.isNull(scheduleInfo) && !ScheduleSetting.isNull(scheduleSetting)) {
            createSchedule(project, taskId, scheduleSetting, extraSettings);
        } else if (Objects.nonNull(scheduleInfo) && !ScheduleSetting.isNull(scheduleSetting)) {

            Integer scheduleId = scheduleInfo.getId();
            if (scheduleInfo.getReleaseState().equalsIgnoreCase("ONLINE")) {
                offlineSchedule(project, scheduleId);
            }
            updateSchedule(project, scheduleId, taskId, scheduleSetting, extraSettings);
        }
        offlineProcess(project, taskId);
    }

    @Override
    public void runTask(String projectName, String user, Integer taskId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;

        findProcess(user, taskId, project);
        runProcess(project, taskId);
    }

    @Override
    public void sendCommand(String projectName, String user, Long taskInstanceId, Integer commandCode) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        ProcessInstance instance = getProcessInstance(project, taskInstanceId);

        findProcess(user, instance.getProcessDefinitionId(), project);
        sendCommandToProcessInstance(project, taskInstanceId,
                DolphinInstanceCommandEnum.valueOf(commandCode).getValue());
    }

    private ProcessInfo findProcess(String user, Integer taskId, String projectName) {
        ProcessInfo processInfo = getProcessDetail(projectName, taskId);
        if (Objects.isNull(processInfo)) throw new ByzerException("Target schedule don't exist");
        if (!processInfo.getName().startsWith(genTaskNamePrefix(user))) {
            throw new ByzerException(
                    MessageFormat.format(
                            "User: {0} is not the owner of schedule: {1}",
                            user,
                            processInfo.getName().replace(genTaskNamePrefix(user), "")
                    )
            );
        }
        return processInfo;
    }

    @Override
    public TaskInfoDTO getTask(String projectName, String user, Integer taskId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;

        ProcessInfo process = getProcessDetail(project, taskId);
        if (Objects.isNull(process)) throw new ByzerException("Schedule do not exist");
        Map<String, String> mails = getScheduleMailList(project, process.getId());
        ScheduleSetting schedule = ScheduleSetting.valueOf(findSchedule(project, process.getId()));
        return TaskInfoDTO.valueOf(process, schedule, mails);
    }

    @Override
    public TaskInfoDTO getTask(String projectName, String user, String entityType, Integer entityId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;

        ProcessInfo process = searchProcessByEntity(project, user, entityType, entityId);
        if (Objects.isNull(process)) {
            return null;
        }

        process.setName(process.getName().replace(genTaskNamePrefix(user), ""));
        Map<String, String> mails = getScheduleMailList(project, process.getId());
        ScheduleSetting schedule = ScheduleSetting.valueOf(findSchedule(project, process.getId()));
        return TaskInfoDTO.valueOf(process, schedule, mails);
    }

    @Override
    public List<TaskInfoDTO> getTasks(String projectName, String user) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        ensureProject(project);

        List<Integer> ids = searchProcessByUser(project, user);
        return ids.stream().map(id -> getProcessDetail(project, id))
                .map(process -> {
                    process.setName(process.getName().replace(genTaskNamePrefix(user), ""));
                    ScheduleSetting schedule = ScheduleSetting.valueOf(findSchedule(project, process.getId()));
                    Map<String, String> mails = getScheduleMailList(project, process.getId());
                    return TaskInfoDTO.valueOf(process, schedule, mails);
                }).collect(Collectors.toList());
    }

    @Override
    public void getTask() {

    }

    @Override
    public List<TaskInstanceDTO> getTaskInstances(String projectName, String user) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        String namePrefix = genTaskNamePrefix(user);
        return getProcessInstanceList(project, namePrefix).stream()
                .map(p -> TaskInstanceDTO.valueOf(p, user, namePrefix)).collect(Collectors.toList());
    }

    @Override
    public List<TaskInstanceDTO> getTaskInstances(String projectName, String user, Integer taskId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        String namePrefix = genTaskNamePrefix(user);
        ProcessInfo processInfo = getProcessDetail(project, taskId);
        String prefix = processInfo.getName();
        return getProcessInstanceList(project, prefix).stream()
                .map(p -> TaskInstanceDTO.valueOf(p, user, namePrefix)).collect(Collectors.toList());
    }

    @Override
    public void onlineTask(String user, Integer taskId, String projectName) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;

        ProcessInfo processInfo = findProcess(user, taskId, project);

        if (processInfo.getReleaseState().equalsIgnoreCase("ONLINE")) {
            throw new ByzerException(
                    MessageFormat.format(
                            "Task: {0} already online",
                            processInfo.getName().replace(genTaskNamePrefix(user), "")
                    )
            );
        }

        onlineProcess(project, taskId);
        ScheduleInfo scheduleInfo = findSchedule(project, taskId);
        if (Objects.nonNull(scheduleInfo) && !scheduleInfo.getReleaseState().equalsIgnoreCase("ONLINE")) {
            onlineSchedule(project, scheduleInfo.getId());
        }
    }

    @Override
    public void offlineTask(String user, Integer taskId, String projectName) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;

        ProcessInfo processInfo = findProcess(user, taskId, project);

        if (processInfo.getReleaseState().equalsIgnoreCase("OFFLINE")) {
            throw new ByzerException(
                    MessageFormat.format(
                            "Task: {0} already offline",
                            processInfo.getName().replace(genTaskNamePrefix(user), "")
                    )
            );
        }

        offlineProcess(project, taskId);
    }

    @Override
    public List<TaskNodeInfoDTO> getTaskInstanceNodes(String projectName, String user, Long taskInstanceId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        ProcessInstance processInstance = getProcessInstance(project, taskInstanceId);
        if (!processInstance.getName().startsWith(genTaskNamePrefix(user))) {
            throw new ByzerException(MessageFormat.format(
                    "User: {0} is not the owner of task instance: {1}",
                    user,
                    taskInstanceId.toString()
            ));
        }
        List<TaskNodeInfoDTO> taskNodeInfos = Lists.newArrayList();
        List<TaskInstance> instanceList = getTaskInstance(project, taskInstanceId);
        instanceList.sort(Comparator.comparingLong(TaskInstance::getId));
        for (TaskInstance task : instanceList) {
            TaskNodeInfoDTO taskInfo = TaskNodeInfoDTO.valueOf(task);
            taskNodeInfos.add(taskInfo);
            if (task.getState().equalsIgnoreCase("FAILURE")) {
                taskInfo.setLog(getTaskInstanceLog(task.getId()));
                break;
            }
        }
        Collections.reverse(taskNodeInfos);
        return taskNodeInfos;
    }

    @Override
    public String getTaskInstanceStatus(String projectName, String user, Long taskInstanceId) {
        String project = Objects.isNull(projectName) ? defaultProject : projectName;
        ProcessInstance processInstance = getProcessInstance(project, taskInstanceId);
        if (!processInstance.getName().startsWith(genTaskNamePrefix(user))) {
            throw new ByzerException(MessageFormat.format(
                    "User: {0} is not the owner of task instance: {1}",
                    user,
                    taskInstanceId.toString()
            ));
        }
        return processInstance.getState();
    }

    @Override
    public void getTask(String user) {

    }

    @Override
    public String getServiceName() {
        return getName();
    }

    private HttpHeaders prepareHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("token", authToken);
        return headers;
    }

    private void setup() {
        try {
            ensureProject(defaultProject);
            log.info("Successfully connect to DolphinScheduler: {}", config.getSchedulerUrl());
            defaultTenantId = Integer.parseInt(getUserInfo().get("tenantId").toString());
        } catch (Exception ex) {
            log.error(String.format("Unable to communicate with DolphinScheduler: %s with Exception: %s; " +
                    "Skip initialize DolphinScheduler!", config.getSchedulerUrl(), ex.getMessage()), ex);

            throw ex;
        }
    }

    private void ensureProject(String projectName) {
        if (!projectExist(projectName)) createProject(projectName);
    }

    private Map<String, Object> getUserInfo() {
        BasicDataAck ack = request(APIMapping.getUserInfo, HttpMethod.GET, prepareHeader(), null);
        return ack.getData();
    }


    private List<String> getProjectList() {
        List<String> projects = Lists.newArrayList();
        int page = 1;
        int totalPage = 1;
        while (page <= totalPage) {
            String uri = APIMapping.searchProject
                    .replace("$page", Integer.toString(page))
                    .replace("$projectName", "");
            ProjectInfoDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ProjectInfoDTO.class);
            totalPage = ack.getData().getTotalPage();
            page++;
            ack.getData().getTotalList().forEach(p -> projects.add(p.getName()));
        }
        return projects;
    }

    private Boolean projectExist(String projectName) {
        int page = 1;
        int totalPage = 1;
        while (page <= totalPage) {
            String uri = APIMapping.searchProject
                    .replace("$page", Integer.toString(page))
                    .replace("$projectName", projectName);
            ProjectInfoDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ProjectInfoDTO.class);
            totalPage = ack.getData().getTotalPage();
            for (ProjectInfo info : ack.getData().getTotalList()) {
                // DolphinScheduler project name is case-insensitive
                if (info.getName().equalsIgnoreCase(projectName)) return true;
            }
            page++;
        }
        return false;
    }

    private void createProject(String projectName) {
        String projectDesc = "Byzer Schedule Project";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("description", projectDesc);
        body.add("projectName", projectName);
        request(APIMapping.createProject, HttpMethod.POST, prepareHeader(), body);
    }


    private BasicDataAck request(String uri, HttpMethod method, HttpHeaders headers, MultiValueMap<String, String> body) {
        BasicDataAck ack;
        if (method.equals(HttpMethod.GET)) {
            ack = JacksonUtils.readJson(get(uri, headers), BasicDataAck.class);
        } else if (method.equals(HttpMethod.POST)) {
            ack = JacksonUtils.readJson(post(uri, headers, body), BasicDataAck.class);
        } else {
            throw new ByzerException(method.name() + " method not support.");
        }

        if (Objects.nonNull(ack) && ack.getCode() != 0) {
            throw new ByzerException(ack.getMsg());
        }
        return ack;
    }

    private <T> T request(String uri, HttpMethod method, HttpHeaders headers, MultiValueMap<String, String> body, Class<T> clazz) {
        String respBody;
        if (method.equals(HttpMethod.GET)) {
            respBody = get(uri, headers);
        } else if (method.equals(HttpMethod.POST)) {
            respBody = post(uri, headers, body);
        } else {
            throw new ByzerException(method.name() + " method not support.");
        }
        BasicAck ack = JacksonUtils.readJson(respBody, BasicAck.class);
        if (Objects.nonNull(ack) && ack.getCode() != 0) {
            throw new ByzerException(ack.getMsg());
        }
        return JacksonUtils.readJson(respBody, clazz);
    }

    private ProcessInfo searchProcessByEntity(String projectName, String userName, String entityType, Integer entityId) {
        List<Integer> userTaskIds = searchProcessByUser(projectName, userName);
        for (Integer id : userTaskIds) {
            ProcessInfo info = getProcessDetail(projectName, id);
            if (info.contains(entityType, entityId)) return info;
        }
        return null;
    }

    private List<Integer> searchProcessByUser(String projectName, String userName) {
        int page = 1;
        int totalPage = 1;
        List<Integer> taskIds = Lists.newArrayList();
        String namePrefix = genTaskNamePrefix(userName);
        while (page <= totalPage) {
            String uri = APIMapping.searchProcess
                    .replace("$page", Integer.toString(page))
                    .replace("$projectName", projectName)
                    .replace("$processName", namePrefix);
            BasicDataAck ack = request(uri, HttpMethod.GET, prepareHeader(), null);
            totalPage = (Integer) ack.getData().getOrDefault("totalPage", 0);
            for (Object process : (List<Object>) ack.getData().get("totalList")) {
                if (((Map<String, Object>) process).get("name").toString().startsWith(namePrefix))
                    taskIds.add(Integer.parseInt(((Map<String, Object>) process).get("id").toString()));
            }
            page++;
        }
        return taskIds;
    }


    private Integer searchProcessByName(String projectName, String processName) {
        int page = 1;
        int totalPage = 1;
        while (page <= totalPage) {
            String uri = APIMapping.searchProcess
                    .replace("$page", Integer.toString(page))
                    .replace("$projectName", projectName)
                    .replace("$processName", processName);
            BasicDataAck ack = request(uri, HttpMethod.GET, prepareHeader(), null);
            totalPage = (Integer) ack.getData().getOrDefault("totalPage", 0);
            for (Object process : (List<Object>) ack.getData().get("totalList")) {
                if (((Map<String, Object>) process).get("name").toString().equals(processName))
                    return Integer.parseInt(((Map<String, Object>) process).get("id").toString());
            }
            page++;
        }
        return null;
    }

    private Integer createProcess(String projectName, String user, String name,
                                  String description, String entityName, String entityType,
                                  Integer entityId, String commitId, String taskName, String taskDesc,
                                  Integer taskTimeout, Map<String, String> extraSettings) {
        ProcessInfo exist = searchProcessByEntity(projectName, user, entityType, entityId);
        if (Objects.nonNull(exist)) {
            throw new ByzerException(
                    MessageFormat.format("{0}: {1} already used in schedule {2}",
                            entityType, entityName, exist.getName())
            );
        }

        String processName;
        if (Objects.isNull(name) || name.isEmpty()) {
            processName = genTaskName(user, entityType, entityId);
        } else {
            processName = genTaskNamePrefix(user) + name;
        }

        if (Objects.nonNull(searchProcessByName(projectName, processName))) {
            throw new ByzerException(
                    MessageFormat.format("Schedule name: [{0}] already exist.", name)
            );
        }

        if (Objects.isNull(description) || description.isEmpty())
            description = genTaskDesc(user, entityType, entityName);

        TaskTimeoutDTO timeouts = TaskTimeoutDTO.parseFrom(extraSettings);
        ModifyProcessDTO dto = ModifyProcessDTO.create(processName, description, entityName,
                entityType, entityId, commitId,
                taskName, taskDesc, taskTimeout,
                user, callbackToken, callbackUrl,
                timeouts.getMaxRetryTimes(), timeouts.getRetryInterval(),
                timeouts.getTimeout(), defaultTenantId
        );

        String uri = APIMapping.createProcess
                .replace("$projectName", projectName);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processDefinitionJson", dto.getProcessDefinitionJson());
        body.add("name", dto.getName());
        body.add("description", dto.getDescription());
        body.add("locations", dto.getLocations());
        body.add("connects", dto.getConnects());

        request(uri, HttpMethod.POST, prepareHeader(), body);
        Integer id = searchProcessByName(projectName, processName);
        if (Objects.isNull(id)) throw new ByzerException("Create DolphinScheduler Process Failed.");
        return id;
    }

    private void updateProcess(String projectName, String user, ProcessInfo processInfo,
                               String processName, String description, EntityModification modification,
                               Map<String, String> extraSettings) {
        TaskTimeoutDTO timeouts = TaskTimeoutDTO.parseFrom(extraSettings);

        ModifyProcessDTO dto;
        if (Objects.isNull(modification)) {
            dto = ModifyProcessDTO.modify(processInfo,
                    Objects.nonNull(processName) ? genTaskNamePrefix(user) + processName : null, description);
        } else {
            switch (modification.getAction().toLowerCase()) {
                case EntityModification.Actions.remove:
                    dto = ModifyProcessDTO.remove(processInfo, modification.getEntityType(),
                            modification.getEntityId());
                    break;
                case EntityModification.Actions.update:
                    Integer taskTimeout = modification.getTaskTimeout();
                    taskTimeout = Objects.isNull(taskTimeout) || taskTimeout > TASK_TIMEOUT_LIMIT ?
                            TASK_TIMEOUT_LIMIT : taskTimeout;
                    dto = ModifyProcessDTO.modify(processInfo,
                            Objects.nonNull(processName) ? genTaskNamePrefix(user) + processName : null, description,
                            modification.getEntityName(), modification.getEntityType(),
                            modification.getEntityId(), modification.getCommitId(), user, callbackToken, callbackUrl,
                            timeouts.getMaxRetryTimes(), timeouts.getRetryInterval(),
                            modification.getAttachTo(), modification.getTaskName(),
                            modification.getTaskDesc(), taskTimeout
                    );
                    break;
                default:
                    return;
            }
        }


        String uri = APIMapping.updateProcess
                .replace("$projectName", projectName);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processDefinitionJson", dto.getProcessDefinitionJson());
        body.add("name", dto.getName());
        body.add("description", dto.getDescription());
        body.add("locations", dto.getLocations());
        body.add("connects", dto.getConnects());
        body.add("id", dto.getId());

        request(uri, HttpMethod.POST, prepareHeader(), body);
    }

    private void deleteProcess(String projectName, Integer processId) {
        String uri = APIMapping.deleteProcess
                .replace("$projectName", projectName)
                .replace("$processId", processId.toString());

        request(uri, HttpMethod.GET, prepareHeader(), null);
    }

    private ProcessInfo getProcessDetail(String projectName, Integer processId) {
        String uri = APIMapping.processDetail
                .replace("$projectName", projectName)
                .replace("$processId", processId.toString());

        ProcessInfoDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ProcessInfoDTO.class);
        return ack.getData().prepare();
    }

    private void onlineProcess(String projectName, Integer processId) {
        if (getProcessDetail(projectName, processId).getReleaseState().equalsIgnoreCase("ONLINE")) return;
        String uri = APIMapping.onlineProcess
                .replace("$projectName", projectName);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processId", processId.toString());
        body.add("releaseState", "1");

        request(uri, HttpMethod.POST, prepareHeader(), body);
    }

    private void offlineProcess(String projectName, Integer processId) {
        if (getProcessDetail(projectName, processId).getReleaseState().equalsIgnoreCase("OFFLINE")) return;
        String uri = APIMapping.onlineProcess
                .replace("$projectName", projectName);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processId", processId.toString());
        body.add("releaseState", "0");

        request(uri, HttpMethod.POST, prepareHeader(), body);
    }

    private ScheduleInfo findSchedule(String projectName, Integer processId) {
        String uri = APIMapping.scheduleList
                .replace("$projectName", projectName)
                .replace("$processId", processId.toString());

        ScheduleInfoDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ScheduleInfoDTO.class);
        List<ScheduleInfo> scheduleList = ack.getData().getTotalList();
        if (scheduleList.isEmpty()) return null;
        return scheduleList.get(0);
    }

    private ScheduleInfo createSchedule(String projectName, Integer processId, ScheduleSetting scheduleSetting, Map<String, String> extraSettings) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processDefinitionId", processId.toString());
        body.add("schedule", JacksonUtils.writeJson(scheduleSetting));
        body.add("warningType", config.getDefaultWarningType());
        body.add("processInstancePriority", config.getDefaultProcessInstancePriority());
        body.add("failureStrategy", config.getDefaultFailureStrategy());
        body.add("warningGroupId", config.getDefaultWarningGroupId().toString());
        body.add("workerGroup", config.getDefaultWorker());

        if (Objects.nonNull(extraSettings)) {
            extraSettings.forEach((k, v) -> {
                if (Objects.nonNull(v)) body.set(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), v);
            });
        }
        String uri = APIMapping.createSchedule
                .replace("$projectName", projectName);

        request(uri, HttpMethod.POST, prepareHeader(), body);
        ScheduleInfo scheduleInfo = findSchedule(projectName, processId);
        if (Objects.isNull(scheduleInfo)) throw new ByzerException("Create DolphinScheduler Schedule Failed.");
        return scheduleInfo;
    }

    private ScheduleInfo updateSchedule(String projectName, Integer scheduleId, Integer processId, ScheduleSetting scheduleSetting, Map<String, String> extraSettings) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("id", scheduleId.toString());
        body.add("processDefinitionId", processId.toString());
        body.add("schedule", JacksonUtils.writeJson(scheduleSetting));
        body.add("warningType", config.getDefaultWarningType());
        body.add("processInstancePriority", config.getDefaultProcessInstancePriority());
        body.add("failureStrategy", config.getDefaultFailureStrategy());
        body.add("warningGroupId", config.getDefaultWarningGroupId().toString());
        body.add("workerGroup", config.getDefaultWorker());

        if (Objects.nonNull(extraSettings)) {
            extraSettings.forEach((k, v) -> {
                if (Objects.nonNull(v)) body.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), v);
            });
        }
        String uri = APIMapping.updateSchedule
                .replace("$projectName", projectName);

        request(uri, HttpMethod.POST, prepareHeader(), body);
        return findSchedule(projectName, processId);
    }

    private void onlineSchedule(String projectName, Integer scheduleId) {
        String uri = APIMapping.onlineSchedule
                .replace("$projectName", projectName)
                .replace("$scheduleId", scheduleId.toString());
        try {
            request(uri, HttpMethod.POST, prepareHeader(), null);
        } catch (Exception ex) {
            throw new ByzerException("Schedule cron setting error");
        }
    }

    private void offlineSchedule(String projectName, Integer scheduleId) {
        String uri = APIMapping.offlineSchedule
                .replace("$projectName", projectName)
                .replace("$scheduleId", scheduleId.toString());
        request(uri, HttpMethod.POST, prepareHeader(), null);
    }

    private void deleteSchedule(String projectName, Integer scheduleId) {
        String uri = APIMapping.deleteSchedule
                .replace("$projectName", projectName)
                .replace("$scheduleId", scheduleId.toString());
        request(uri, HttpMethod.POST, prepareHeader(), null);
    }

    private Map<String, String> getScheduleMailList(String projectName, Integer processId) {
        String uri = APIMapping.mailList
                .replace("$projectName", projectName)
                .replace("$processId", processId.toString());

        BasicDataAck ack = request(uri, HttpMethod.GET, prepareHeader(), null);
        Map<String, String> mails = Maps.newHashMap();
        if (Objects.nonNull(ack.getData())) {
            ack.getData().forEach((k, v) -> {
                if (Objects.nonNull(v)) {
                    mails.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, k), v.toString());
                }
            });
        }
        return mails;
    }

    private List<ProcessInstance> getProcessInstanceList(String projectName, String prefix) {
        String uri = APIMapping.getProcessInstanceList
                .replace("$projectName", projectName)
                .replace("$prefix", prefix)
                .replace("$page", "1");
        ProcessInstanceDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ProcessInstanceDTO.class);
        return ack.getData().getTotalList();
    }

    private ProcessInstance getProcessInstance(String projectName, Long processInstanceId) {
        String uri = APIMapping.getProcessInstance
                .replace("$projectName", projectName)
                .replace("$processInstanceId", processInstanceId.toString());
        ProcessInstanceDetailDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, ProcessInstanceDetailDTO.class);

        return ack.getData();
    }

    private List<TaskInstance> getTaskInstance(String projectName, Long processInstanceId) {
        String uri = APIMapping.getProcessInstanceTasks
                .replace("$projectName", projectName)
                .replace("$processInstanceId", processInstanceId.toString());
        TaskInstanceListDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, TaskInstanceListDTO.class);
        return ack.getData().getTaskList();
    }

    private String getTaskInstanceLog(Long taskInstanceId) {
        String uri = APIMapping.getTaskInstanceLog
                .replace("$taskInstanceId", taskInstanceId.toString());
        InstanceLogDTO ack = request(uri, HttpMethod.GET, prepareHeader(), null, InstanceLogDTO.class);
        return ack.getData();
    }

    private void runProcess(String projectName, Integer processId) {
        String uri = APIMapping.runProcess
                .replace("$projectName", projectName);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("processDefinitionId", processId.toString());
        body.add("failureStrategy", "END");
        body.add("warningType", "NONE");
        body.add("warningGroupId", config.getDefaultWarningGroupId().toString());
        body.add("taskDependType", "TASK_POST");
        body.add("runMode", "RUN_MODE_SERIAL");
        body.add("processInstancePriority", config.getDefaultProcessInstancePriority());
        body.add("workerGroup", config.getDefaultWorker());

        request(uri, HttpMethod.POST, prepareHeader(), body);
    }

    private void sendCommandToProcessInstance(String projectName, Long taskInstanceId, String command) {
        String uri = APIMapping.stopProcessInstance
                .replace("$projectName", projectName);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("processInstanceId", taskInstanceId.toString());
        body.add("executeType", command);
        request(uri, HttpMethod.POST, prepareHeader(), body);
    }
}
