package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.entity.JobInfoArchive;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.util.EngineExceptionUtils;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Data
@NoArgsConstructor
public class JobInfoDTO {

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("notebook")
    private String notebook;

    @JsonProperty("content")
    private String content;

    @JsonProperty("user")
    private String user;

    @JsonProperty("status")
    private String status;

    @JsonProperty("result")
    private String result;

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("root_cause")
    private String rootCause;

    @JsonProperty("total_job_count")
    private String totalJobCount;

    @JsonProperty("current_job_index")
    private String currentJobIndex;

    @JsonProperty("completed_jobs")
    private Integer completedJobs;

    @JsonProperty("in_progress_jobs")
    private Integer inProgressJobs;

    @JsonProperty("failed_jobs")
    private Integer failedJobs;

    @JsonProperty("progress")
    private Double progress;

    public static JobInfoDTO valueOf(JobInfo jobInfo, JobProgressDTO jobProgress, CurrentJobInfo currentJobInfo) {
        if (jobInfo == null) {
            return null;
        }

        JobInfoDTO resp = new JobInfoDTO();
        resp.jobId = jobInfo.getJobId();
        resp.user = jobInfo.getUser();
        resp.jobName = jobInfo.getName();
        resp.content = jobInfo.getContent();
        resp.status = EntityUtils.toStr(jobInfo.getStatus());
        resp.startTime = EntityUtils.toStr(jobInfo.getCreateTime());
        resp.endTime = EntityUtils.toStr(jobInfo.getFinishTime());
        resp.result = jobInfo.getResult();
        resp.msg = jobInfo.getMsg();
        resp.rootCause = EngineExceptionUtils.getRootCause(jobInfo.getMsg());
        resp.notebook = StringUtils.isBlank(jobInfo.getNotebook()) ? "untitled" : jobInfo.getNotebook();
        resp.engine = StringUtils.isBlank(jobInfo.getEngine()) ? "default" : jobInfo.getEngine();
        if (Objects.nonNull(jobProgress)) {
            resp.completedJobs = jobProgress.getCompletedJobs();
            resp.failedJobs = jobProgress.getFailedJobs();
            resp.inProgressJobs = jobProgress.getInProgressJobs();
            resp.progress = jobProgress.getProgress();
        }

        if (resp.startTime != null) {
            long endTimestamp = (resp.endTime == null) ? System.currentTimeMillis() : jobInfo.getFinishTime().getTime();
            resp.duration = String.valueOf(endTimestamp - jobInfo.getCreateTime().getTime());
        }

        if (currentJobInfo != null && currentJobInfo.getProgress() != null) {
            resp.totalJobCount = EntityUtils.toStr(currentJobInfo.getProgress().getTotalJob());
            resp.currentJobIndex = EntityUtils.toStr(currentJobInfo.getProgress().getCurrentJobIndex());
        }


        return resp;
    }

    public static JobInfoDTO valueOf(JobInfo jobInfo) {
        return valueOf(jobInfo, null, null);
    }

    public static JobInfoDTO valueOf(JobInfoArchive jobInfoArchive) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setStatus(jobInfoArchive.getStatus());
        jobInfo.setJobId(jobInfoArchive.getJobId());
        jobInfo.setEngine(jobInfoArchive.getEngine());
        jobInfo.setUser(jobInfoArchive.getUser());
        jobInfo.setNotebook(jobInfoArchive.getNotebook());
        jobInfo.setCreateTime(jobInfoArchive.getCreateTime());
        jobInfo.setFinishTime(jobInfoArchive.getFinishTime());
        jobInfo.setMsg(jobInfoArchive.getMsg());
        return valueOf(jobInfo);
    }

    public static JobInfoDTO valueOf(JobInfo jobInfo, JobProgressDTO jobProgress) {
        return valueOf(jobInfo, jobProgress, null);
    }

    public static JobInfoDTO valueOf(CurrentJobInfo currentJobInfo) {
        if (currentJobInfo == null) {
            return null;
        }
        JobInfo jobInfo = new JobInfo();
        jobInfo.setContent(currentJobInfo.getProgress().getScript());
        return valueOf(jobInfo, null, currentJobInfo);
    }
}
