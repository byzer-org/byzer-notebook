package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.IdNameDTO;
import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.dto.TaskInstanceDTO;
import io.kyligence.notebook.console.bean.dto.TaskNodeInfoDTO;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.scheduler.RemoteSchedulerInterface;
import io.kyligence.notebook.console.scheduler.SchedulerConfig;
import io.kyligence.notebook.console.scheduler.SchedulerFactory;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchedulerService {

    @Autowired
    private EngineService engineService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private JobService jobService;

    private final NotebookConfig config = NotebookConfig.getInstance();

    // default callback script execution timout is 4 hours
    private static final  Integer CALLBACK_TIMEOUT_SECONDS = 14400;

    private boolean enabled = config.getIsSchedulerEnabled();

    private final Map<Integer, RemoteSchedulerInterface> schedulerMap = new LinkedHashMap<>();

    @PostConstruct
    public void initSchedulers() {
        if (!enabled) return;
        int id = 1;
        for (SchedulerConfig conf : config.getSchedulerConfig()) {
            RemoteSchedulerInterface scheduler = SchedulerFactory.create(id, conf);
            schedulerMap.put(id, scheduler);
            id++;
        }
    }


    public void callback(String token, String scheduleOwner, String entityType,
                         String entityId, String commitId, Integer timeout) {
        if (!config.getScheduleCallbackToken().equals(token)) {
            throw new ByzerException("Scheduler callback token auth failed!");
        }
        log.info("Receive scheduler callback for {} execute Entity[{}, {}, {}]", scheduleOwner, entityType, entityId, commitId);
        String user = config.getScheduleCallbackUser();
        timeout = Objects.isNull(timeout) || timeout > CALLBACK_TIMEOUT_SECONDS ? CALLBACK_TIMEOUT_SECONDS : timeout;
        EngineService.RunScriptParams runScriptParams = new EngineService.RunScriptParams()
                .withAsync("false")
                .withOwner(scheduleOwner)
                .withOwnerPathPrefix(scheduleOwner)
                .with("timeout", String.valueOf(timeout * 1000))
                .with("sessionPerRequest", "true");

        String scripts = getScript(entityType, entityId, commitId, runScriptParams.getAll());

        runScriptParams.withSql(scripts);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(UUID.randomUUID().toString());
        jobInfo.setContent(scripts);
        jobInfo.setStatus(JobInfo.JobStatus.RUNNING);
        jobInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        jobInfo.setName(MessageFormat.format("ByzerScheduleTask-{0}-{1}_{2}", scheduleOwner, entityType, entityId));
        jobInfo.setUser(user);
        jobInfo.setNotebook(getEntityName(entityType, Integer.parseInt(entityId)));

        String engine = config.getExecutionEngine();
        jobInfo.setEngine(engine);

        jobService.insert(jobInfo);
        int status = JobInfo.JobStatus.SUCCESS;
        // 发送查询
        try {
            engineService.runScript(runScriptParams);
            log.info("Scheduler callback for {} execute Entity[{}, {}, {}] succeed!",
                    scheduleOwner, entityType, entityId, commitId);
        } catch (Exception ex) {
            // update job status to FAILED if exception happened
            status = JobInfo.JobStatus.FAILED;
            log.error("Scheduler callback for {} execute Entity[{}, {}, {}] failed!",
                    scheduleOwner, entityType, entityId, commitId);
            throw ex;
        } finally {
            jobInfo.setFinishTime(new Timestamp(System.currentTimeMillis()));
            jobInfo.setStatus(status);
            jobService.updateByJobId(jobInfo);
        }


    }

    public boolean isEnabled(){
        return enabled;
    }

    public List<IdNameDTO> getSchedulerList() {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        List<IdNameDTO> schedulers = Lists.newArrayList();
        schedulerMap.forEach((k, v) -> schedulers.add(IdNameDTO.valueOf(k, v.getServiceName())));
        return schedulers;
    }

    public void createSchedule(Integer schedulerId, String name, String desc, String user, String entityType,
                               Integer entityId, String commitId, String taskName, String taskDesc,
                               Integer taskTimeout, ScheduleSetting scheduleSetting,
                               Map<String, String> extraSettings) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        commitId = Objects.isNull(commitId) ? autoCommit(user, entityType, entityId) : commitId;
        scheduler.createTask(
                user, name, desc, entityType, entityId, commitId, taskName, taskDesc,
                getEntityName(entityType, entityId), taskTimeout, scheduleSetting, extraSettings
        );
    }

    public void updateSchedule(Integer schedulerId, Integer id, String name, String desc, String user,
                               EntityModification modification, ScheduleSetting scheduleSetting,
                               Map<String, String> extraSettings) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        if (Objects.nonNull(modification)) {
            modification.setEntityName(getEntityName(modification.getEntityType(), modification.getEntityId()));
            if (Objects.equals(modification.getAction(), EntityModification.Actions.update) && Objects.isNull(modification.getCommitId())){
                modification.setCommitId(autoCommit(user, modification.getEntityType(), modification.getEntityId()));
            }
        }
        scheduler.updateTask(user, id, name, desc, modification, scheduleSetting, extraSettings);
    }

    public void runTask(String user, Integer schedulerId, String projectName, Integer taskId){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.runTask(projectName, user, taskId);
    }

    public void setStatus(String user, Integer schedulerId, String projectName, Long taskInstanceId, Integer setStatus){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.sendCommand(projectName, user, taskInstanceId, setStatus);
    }

    public void deleteSchedule(String user, Integer schedulerId, String projectName, Integer taskId) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.deleteTask(user, projectName, taskId);
    }

    public TaskInfoDTO getScheduleById(String user, Integer schedulerId, String projectName, Integer taskId){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        return scheduler.getTask(projectName, user, taskId);
    }

    public TaskInfoDTO getScheduleByEntity(String user, Integer schedulerId, String projectName, String entityType, Integer entityId){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        return scheduler.getTask(projectName, user, entityType, entityId);
    }

    public List<TaskInfoDTO> getScheduleList(String user, Integer schedulerId, String projectName){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        return scheduler.getTasks(projectName, user);
    }

    public List<TaskInstanceDTO> getInstanceList(String user, Integer schedulerId, String projectName, Integer taskId){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        List<TaskInstanceDTO> instances = scheduler.getTaskInstances(projectName, user);
        return Objects.isNull(taskId) ? instances : instances.stream().filter(
                instance-> Objects.equals(instance.getTaskId(), taskId)).collect(Collectors.toList());
    }


    public String getInstanceStatus(String user, Long taskInstanceId, Integer schedulerId, String projectName) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        return scheduler.getTaskInstanceStatus(projectName, user, taskInstanceId);
    }


    public List<TaskNodeInfoDTO> getInstanceNodes(String user, Long taskInstanceId, Integer schedulerId, String projectName){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        return scheduler.getTaskInstanceNodes(projectName, user, taskInstanceId);
    }

    public void onlineTask(String user, Integer taskId, Integer schedulerId, String projectName){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.onlineTask(user, taskId, projectName);
    }

    public void offlineTask(String user, Integer taskId, Integer schedulerId, String projectName){
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.offlineTask(user, taskId, projectName);
    }

    public boolean entityUsedInSchedule(String entityType, Integer entityId){
        for (RemoteSchedulerInterface scheduler: schedulerMap.values()){
            TaskInfoDTO exist = scheduler.searchForEntity(getEntityName(entityType, entityId), entityType, entityId);
            if (Objects.nonNull(exist)) return true;
        }
        return false;
    }

    private String getScript(String entityType, String entityId, String commitId, Map<String, String> options) {
        switch (entityType.toLowerCase()) {
            case "notebook":
                return notebookService.getNotebookScripts("admin", Integer.valueOf(entityId), commitId, options);
            case "workflow":
                return workflowService.getWorkflowScripts("admin", Integer.valueOf(entityId), commitId, options);
            default:
                return "";
        }
    }

    private String getEntityName(String entityType, Integer entityId) {
        switch (entityType.toLowerCase()) {
            case "notebook":
                return notebookService.getNotebook(entityId, "admin").getName();
            case "workflow":
                return workflowService.getWorkflow(entityId, "admin").getName();
            default:
                return "";
        }
    }

    private String autoCommit(String user, String entityType, Integer entityId){
        switch (entityType.toLowerCase()) {
            case "notebook":
                return notebookService.commit(user, entityId).getCommitId();
            case "workflow":
                return workflowService.commit(user, entityId).getCommitId();
            default:
                return "";
        }
    }

    public void getTaskInstance(Integer schedulerId, String projectName, String user) {
    }

    // for test
    public void mockScheduler(Integer id, RemoteSchedulerInterface mockInterface) {
        schedulerMap.put(id, mockInterface);
    }

    // for test
    public void mockEnableSchedule() {
        enabled = true;
    }
}
