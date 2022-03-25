package io.kyligence.notebook.console.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.bean.model.JobProgress;
import io.kyligence.notebook.console.dao.JobInfoRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
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
    private EngineService engineService;

    @Transactional
    public JobLog getJobLog(String user, String jobId) {
        JobInfo jobInfo = findByJobId(jobId);
        if (jobInfo == null){
            return null;
        }
        Integer offset = -1;
        if (jobInfo.getConsoleLogOffset() != null){
            offset = jobInfo.getConsoleLogOffset();
        }
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
        if (offset == -1 && jobLog.getOffset() != null && jobLog.getOffset() != -1) {
            offset = jobLog.getOffset() - jobLog.getValue().toString().length();
            jobInfoRepository.updateLogOffset(jobId, offset);
        }
        jobLog.setOffset(offset);
        if (jobLog.getValue() != null) {
            jobLog.setValue(
                    jobLog.getValue().stream().filter(
                            s -> s.contains(String.format("DriverLogServer: [owner] [%s]", user))
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


    @Transactional
    public void killJobById(String jobId) {
        JobInfo jobInfo = findByJobId(jobId);

        if (jobInfo == null) {
            throw new ByzerException(ErrorCodeEnum.JOB_NOT_EXIST, jobId);
        }

        if (jobInfo.getStatus() != JobInfo.JobStatus.RUNNING) {
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
        CurrentJobInfo currentJobInfo = resultsMap.get(0);
        setJobAndGroup(jobId, currentJobInfo.getGroupId());

        return currentJobInfo;
    }

    public JobProgress getJobProgress(String jobId) {
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
        JobProgress jobProgress = jobProgresses.get(0);
        setJobAndGroup(jobId, jobProgress.getGroupId());

        return jobProgress;
    }

    @Transactional
    public Pair<Long, List<JobInfo>> getJobList(Integer pageSize, Integer pageOffset, String sortBy, Boolean reverse, String status, String user, String keyword) {
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

        Query query = queryBuilder.getAll(JobInfo.class, false, selectFields, pageSize, pageOffset, reverse, sortBy, filters, keywords);
        List<JobInfo> jobInfos = query.getResultList();

        Query countQuery = queryBuilder.count(JobInfo.class, filters);
        Long count = (Long) countQuery.getSingleResult();

        return Pair.of(count, jobInfos);
    }

    public String getJobContent(String jobId) {
        return jobInfoRepository.getContentByJobId(jobId);
    }




}
