package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.bean.model.JobProgress;
import io.kyligence.notebook.console.util.EntityUtils;
import io.kyligence.notebook.console.util.ExceptionUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

    @JsonProperty("console_log_offset")
    private String consoleLogOffset;

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
    private List<SparkJobProgressDTO> completedJobs;

    @JsonProperty("in_progress_jobs")
    private List<SparkJobProgressDTO> inProgressJobs;

    @JsonProperty("failed_jobs")
    private List<SparkJobProgressDTO> failedJobs;

    public static JobInfoDTO valueOf(JobInfo jobInfo, JobProgress jobProgress, CurrentJobInfo currentJobInfo) {
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
        resp.consoleLogOffset = EntityUtils.toStr(jobInfo.getConsoleLogOffset());
        resp.msg = jobInfo.getMsg();
        resp.rootCause = ExceptionUtils.getRootCause(jobInfo.getMsg());
        resp.notebook = StringUtils.isBlank(jobInfo.getNotebook()) ? "untitled" : jobInfo.getNotebook();
        resp.engine = StringUtils.isBlank(jobInfo.getEngine()) ? "default" : jobInfo.getEngine();

        if (resp.startTime != null) {
            long endTimestamp = (resp.endTime == null) ? System.currentTimeMillis() : jobInfo.getFinishTime().getTime();
            resp.duration = String.valueOf(endTimestamp - jobInfo.getCreateTime().getTime());
        }

        if (jobProgress != null && jobProgress.getActiveJobs() != null && jobProgress.getActiveJobs().size() != 0) {
            resp.completedJobs = new ArrayList<>();
            resp.inProgressJobs = new ArrayList<>();
            resp.failedJobs = new ArrayList<>();
            for (JobProgress.ActiveJob activeJob : jobProgress.getActiveJobs()) {
                if ((activeJob.getNumFailedTasks() != null && activeJob.getNumFailedTasks() > 0) ||
                        (activeJob.getNumFailedStages() != null && activeJob.getNumFailedStages() > 0)) {
                    resp.failedJobs.add(SparkJobProgressDTO.valueOf(activeJob));
                    continue;
                }
                if (StringUtils.isEmpty(activeJob.getCompletionTime())) {
                    resp.inProgressJobs.add(SparkJobProgressDTO.valueOf(activeJob));
                    continue;
                }
                if (StringUtils.isNotEmpty(activeJob.getCompletionTime())) {
                    resp.completedJobs.add(SparkJobProgressDTO.valueOf(activeJob));
                }


            }
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

    public static JobInfoDTO valueOf(JobInfo jobInfo, JobProgress jobProgress) {
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


    @Data
    @NoArgsConstructor
    public static class SparkJobProgressDTO {

        @JsonProperty("spark_job_id")
        private String sparkJobId;

        @JsonProperty("total_task_count")
        private String totalTaskCount;

        @JsonProperty("completed_task_count")
        private String completedTaskCount;

        @JsonProperty("skipped_task_count")
        private String skippedTaskCount;

        @JsonProperty("duration")
        private String duration;

        public static SparkJobProgressDTO valueOf(JobProgress.ActiveJob activeJob) {
            SparkJobProgressDTO sparkJobProgressDTO = new SparkJobProgressDTO();
            sparkJobProgressDTO.sparkJobId = EntityUtils.toStr(activeJob.getJobId());
            sparkJobProgressDTO.totalTaskCount = EntityUtils.toStr(activeJob.getNumTasks());
            // complete include skipped
            if (activeJob.getNumCompletedTasks() != null && activeJob.getNumSkippedTasks() != null) {
                int completedTasksCount = activeJob.getNumCompletedTasks() + activeJob.getNumSkippedTasks();
                sparkJobProgressDTO.completedTaskCount = String.valueOf(completedTasksCount);
            } else {
                sparkJobProgressDTO.completedTaskCount = EntityUtils.toStr(activeJob.getNumCompletedTasks());
            }
            sparkJobProgressDTO.skippedTaskCount = EntityUtils.toStr(activeJob.getNumSkippedTasks());
            sparkJobProgressDTO.duration = EntityUtils.toStr(activeJob.getDuration());
            return sparkJobProgressDTO;
        }

    }
}
