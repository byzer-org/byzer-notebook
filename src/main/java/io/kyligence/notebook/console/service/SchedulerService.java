package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.IdNameDTO;
import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.scheduler.RemoteSchedulerInterface;
import io.kyligence.notebook.console.scheduler.SchedulerConfig;
import io.kyligence.notebook.console.scheduler.SchedulerFactory;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;

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

    private final Boolean enabled = config.schedulerEnabled();

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


    public void callback(String token, String scheduleOwner, String entityType, String entityId) {
        if (!config.getScheduleCallbackToken().equals(token)) {
            throw new ByzerException();
        }

        String user = config.getScheduleCallbackUser();
        EngineService.RunScriptParams runScriptParams = new EngineService.RunScriptParams()
                .withAsync("false")
                .withOwner(scheduleOwner)
                .with("sessionPerRequest", "true");

        String scripts = getScript(entityType, entityId, runScriptParams.getAll());

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
        } catch (Exception ex) {
            // update job status to FAILED if exception happened
            status = JobInfo.JobStatus.FAILED;
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

    public void createSchedule(Integer schedulerId, String name, String desc, String user, String entityType, Integer entityId, ScheduleSetting scheduleSetting,
                               Map<String, String> extraSettings) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        scheduler.createTask(user, name, desc, entityType, entityId, getEntityName(entityType, entityId), scheduleSetting, extraSettings);
    }

    public void updateSchedule(Integer schedulerId, Integer id, String name, String desc, String user, EntityModification modification, ScheduleSetting scheduleSetting,
                               Map<String, String> extraSettings) {
        if (!enabled) throw new ByzerException("SchedulerService not enabled");
        RemoteSchedulerInterface scheduler = schedulerMap.get(Objects.isNull(schedulerId) ? 1 : schedulerId);
        modification.setEntityName(getEntityName(modification.getEntityType(), modification.getEntityId()));
        scheduler.updateTask(user, id, name, desc, modification, scheduleSetting, extraSettings);
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

    public boolean entityUsedInSchedule(String entityType, Integer entityId){
        for (RemoteSchedulerInterface scheduler: schedulerMap.values()){
            TaskInfoDTO exist = scheduler.searchForEntity(getEntityName(entityType, entityId), entityType, entityId);
            if (Objects.nonNull(exist)) return true;
        }
        return false;
    }

    private String getScript(String entityType, String entityId, Map<String, String> options) {
        switch (entityType.toLowerCase()) {
            case "notebook":
                return notebookService.getNotebookScripts("admin", Integer.valueOf(entityId), options);
            case "workflow":
                return workflowService.getWorkflowScripts("admin", Integer.valueOf(entityId), options);
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
}
