package io.kyligence.notebook.console.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.JobProgressDTO;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.entity.JobInfoArchive;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.bean.model.JobProgress;
import io.kyligence.notebook.console.dao.JobInfoArchiveRepository;
import io.kyligence.notebook.console.dao.JobInfoRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import io.kyligence.notebook.console.util.EngineExceptionUtils;
import io.kyligence.notebook.console.util.ExceptionUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobService {

    private static final Cache<String, String> mapJobId2Group = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(12, TimeUnit.HOURS).build();

    @Resource
    private CriteriaQueryBuilder queryBuilder;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private JobInfoArchiveRepository jobInfoArchiveRepository;

    @Autowired
    private EngineService engineService;

    private final NotebookConfig config = NotebookConfig.getInstance();

    public JobLog getJobLog(String user, String jobId, Long offset) {
        String groupId = getGroupOrJobId(jobId);
        String response = null;
        try {
            response = engineService.runScript(new EngineService
                    .RunScriptParams()
                    .withOwner(user)
                    .withSql(String.format("load _mlsql_.`log/%d` where filePath=\"engine_log\" as output;", offset))
                    .withAsync("false")
                    .with("sessionPerRequest", "true")
            );
        } catch (Exception e) {
            log.error(ExceptionUtils.getRootCause(e));
        }
        if (StringUtils.isEmpty(response) || response.equals("[]")) {
            return null;
        }

        List<JobLog> resultsMap = JacksonUtils.readJsonArray(response, JobLog.class);
        JobLog jobLog = Objects.requireNonNull(resultsMap).get(0);
        if (jobLog.getValue() != null) {
            jobLog.setValue(
                    jobLog.getValue().stream().filter(
                            s -> s.contains(String.format("[owner] [%s] [groupId] [%s]", user, groupId))
                                    || s.contains(String.format("DriverLogServer: [owner] [%s]", user))
                    ).collect(Collectors.toList())
            );
        } else {
            jobLog.setValue(Lists.newArrayList());
        }
        return jobLog;
    }

    public void setJobAndGroup(String jobId, String groupId) {
        if (StringUtils.isEmpty(jobId) || StringUtils.isEmpty(groupId)) {
            return;
        }
        mapJobId2Group.put(jobId, groupId);
    }

    public String getGroupOrJobId(String jobId) {
        String groupId = mapJobId2Group.getIfPresent(jobId);
        return groupId == null ? jobId : groupId;
    }


    public void insert(JobInfo jobInfo) {
        jobInfoRepository.save(jobInfo);
    }

    @Transactional
    public void updateByJobId(JobInfo jobInfo) {
        Query query = queryBuilder.updateNotNullByField(jobInfo, "jobId");
        query.executeUpdate();
    }

    public JobInfo findByJobId(String jobId) {
        List<JobInfo> jobInfoList = jobInfoRepository.findByJobId(jobId);
        return jobInfoList.isEmpty() ? null : jobInfoList.get(0);
    }

    public Integer getJobStatus(String jobId) {
        return Optional.ofNullable(jobInfoRepository.getJobStatus(jobId)).orElse(JobInfo.JobStatus.NOT_EXIST);
    }

    public Timestamp getJobStartTime(String jobId) {
        return jobInfoRepository.getJobStartTime(jobId);
    }

    @Transactional
    public void killJobById(String jobId) {
        JobInfo jobInfo = findByJobId(jobId);

        if (jobInfo == null) {
            throw new ByzerException(ErrorCodeEnum.JOB_NOT_EXIST, jobId);
        }

        if (!isRunning(jobInfo.getStatus())) {
            return;
        }

        String groupId = getGroupOrJobId(jobId);

        try {
            engineService.runScript(new EngineService
                    .RunScriptParams()
                    .withAsync("false")
                    .withSql(String.format("!kill %s;", groupId)));
        } catch (Exception e) {
            log.error(ExceptionUtils.getRootCause(e));
        }

        jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setStatus(JobInfo.JobStatus.KILLED);
        jobInfo.setFinishTime(new Timestamp(System.currentTimeMillis()));

        Query query = queryBuilder.updateNotNullByField(jobInfo, "jobId");
        query.executeUpdate();
    }

    public CurrentJobInfo getCurrentJob(String jobId) {
        String groupId = getGroupOrJobId(jobId);
        String response = engineService.runScript(new EngineService
                .RunScriptParams()
                .withAsync("false")
                .withSql(String.format("!show \"jobs/get/%s\";", groupId)));

        if (StringUtils.isEmpty(response) || response.equals("[]")) {
            return null;
        }

        List<CurrentJobInfo> resultsMap = JacksonUtils.readJsonArray(response, CurrentJobInfo.class);
        CurrentJobInfo currentJobInfo = Objects.requireNonNull(resultsMap).get(0);
        setJobAndGroup(jobId, currentJobInfo.getGroupId());

        return currentJobInfo;
    }

    public JobProgressDTO getJobProgress(String jobId) {
        String groupId = getGroupOrJobId(jobId);
        String response = null;

        try {
            response = engineService.runScript(new EngineService
                    .RunScriptParams()
                    .withAsync("false")
                    .withSql(String.format("!show \"jobs/v2/%s\";", groupId)));
        } catch (Exception e) {
            log.info("can not fetch job progress, {}", ExceptionUtils.getRootCause(e));
        }

        if (StringUtils.isEmpty(response) || response.equals("[]")) {
            return null;
        }

        List<JobProgress> jobProgresses = JacksonUtils.readJsonArray(response, JobProgress.class);
        JobProgress jobProgress = Objects.requireNonNull(jobProgresses).get(0);
        setJobAndGroup(jobId, jobProgress.getGroupId());

        return JobProgressDTO.valueOf(jobProgress);
    }

    public Pair<Long, List<JobInfo>> getJobList(Integer pageSize, Integer pageOffset, String sortBy, Boolean reverse, String status, String user, String keyword) {
        return getJobList(JobInfo.class, pageSize, pageOffset, sortBy, reverse, status, user, keyword);
    }

    public Pair<Long, List<JobInfoArchive>> getJobArchiveList(Integer pageSize, Integer pageOffset, String sortBy, Boolean reverse, String status, String user, String keyword) {
        return getJobList(JobInfoArchive.class, pageSize, pageOffset, sortBy, reverse, status, user, keyword);
    }

    @Transactional
    public <T> Pair<Long, List<T>> getJobList(Class<T> clazz, Integer pageSize, Integer pageOffset, String sortBy, Boolean reverse, String status, String user, String keyword) {
        List<String> selectFields = Arrays.asList("jobId", "status", "user", "notebook", "engine", "createTime", "finishTime");

        if ("start_time".equals(sortBy)) {
            sortBy = "createTime";
        }

        Map<String, String> filters = new HashMap<>();
        if (StringUtils.isNotBlank(status)) {
            filters.put("status", status);
        }

        if (!user.equalsIgnoreCase("admin")) {
            filters.put("user", user);
        }

        Map<String, String> keywords = new HashMap<>();
        if (StringUtils.isNotBlank(keyword)) {
            keywords.put("jobId", keyword);
        }

        Query query = queryBuilder.getAll(clazz, false, selectFields, pageSize, pageOffset, reverse, sortBy, filters, keywords);
        List<T> jobInfos = query.getResultList();

        Query countQuery = queryBuilder.count(clazz, filters, keywords);
        Long count = (Long) countQuery.getSingleResult();

        return Pair.of(count, jobInfos);
    }

    public String getJobContent(String jobId) {
        String content = jobInfoRepository.getContentByJobId(jobId);
        return Objects.isNull(content) ? jobInfoArchiveRepository.getContentByJobId(jobId) : content;
    }

    public boolean isRunning(Integer status) {
        return Objects.nonNull(status) && (status == JobInfo.JobStatus.RUNNING || status >= JobInfo.JobStatus.RETRYING);
    }

    public boolean needRetry(Integer status){
        return config.getExecutionEngineCallbackRetries() > 0 && Objects.nonNull(status)
                && (status == JobInfo.JobStatus.RUNNING || status > JobInfo.JobStatus.RETRYING);
    }

    @Transactional
    public boolean jobDone(String jobId, Integer status, String result, String msg, Timestamp finishTime) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setResult(result);
        jobInfo.setFinishTime(finishTime);
        jobInfo.setStatus(status);
        jobInfo.setMsg(status == JobInfo.JobStatus.SUCCESS ? msg :
                EngineExceptionUtils.parseStackTrace(getJobContent(jobId), msg));

        try {
            JobProgressDTO jobProgress = getJobProgress(jobId);
            if (jobProgress != null) {
                jobInfo.setJobProgress(JacksonUtils.writeJson(jobProgress));
            }
        } catch (Exception e) {
            log.warn("get job progress failed, job id is {}.", jobId);
        }
        try {
            updateByJobId(jobInfo);
            return true;
        } catch (Exception e) {
            log.warn("Error when save job_info callback, job id is {}.", jobId);
            Integer currentStatus = getJobStatus(jobId);
            if (needRetry(currentStatus)) {
                log.warn("Retry for save job_info callback, job id is {}.", jobId);
                int nextStatus;
                if (currentStatus == JobInfo.JobStatus.RUNNING) {
                    nextStatus = JobInfo.JobStatus.RETRYING + config.getExecutionEngineCallbackRetries() - 1;
                } else {
                    nextStatus = currentStatus - 1;
                }
                updateByJobId(new JobInfo(jobId, nextStatus));
                return false;
            } else {
                jobInfo.setMsg(e.getMessage());
                jobInfo.setStatus(JobInfo.JobStatus.FAILED);
                jobInfo.setJobProgress(null);
                jobInfo.setResult(null);
                updateByJobId(jobInfo);
                log.error(String.format("Fail to save job_info callback for job: [%s], mark job status as [Failed].\n" +
                        "Trace stack as below:\n", jobId) + ExceptionUtils.getRootCause(e));
                return true;
            }

        }

    }
}
