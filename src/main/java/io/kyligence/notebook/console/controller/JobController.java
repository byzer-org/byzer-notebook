package io.kyligence.notebook.console.controller;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.JobInfoDTO;
import io.kyligence.notebook.console.bean.dto.JobInfoListDTO;
import io.kyligence.notebook.console.bean.dto.JobLogDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.bean.model.JobProgress;
import io.kyligence.notebook.console.dao.JobInfoRepository;
import io.kyligence.notebook.console.service.JobService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on job")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    JobInfoRepository jobInfoRepository;

    @ApiOperation("Get Job Result")
    @GetMapping("/job/{jobId}")
    @Permission
    public Response<JobInfoDTO> getJobResult(@PathVariable("jobId") @NotNull String jobId) {
        JobInfo jobInfo = jobService.findByJobId(jobId);
        JobProgress jobProgress = null;
        if (jobInfo == null) {
            jobInfo = new JobInfo(jobId, JobInfo.JobStatus.KILLED
                    , null, null, null, null, null);
        }

        if (JobInfo.JobStatus.RUNNING == jobInfo.getStatus()) {
            jobProgress = jobService.getJobProgress(jobId);
        } else if (jobInfo.getJobProgress() != null) {
            jobProgress = JacksonUtils.readJson(jobInfo.getJobProgress(), JobProgress.class);
        }

        return new Response<JobInfoDTO>().data(JobInfoDTO.valueOf(jobInfo, jobProgress));
    }


    @ApiOperation("Job Running Script")
    @GetMapping("/job/{jobId}/current_script")
    @Permission
    public Response<JobInfoDTO> getCurrentJobScript(@PathVariable("jobId") @NotNull String jobId) {
        CurrentJobInfo currentJobInfo = jobService.getCurrentJob(jobId);
        return new Response<JobInfoDTO>().data(JobInfoDTO.valueOf(currentJobInfo));
    }


    @ApiOperation("Get Current Job Running Script")
    @GetMapping("/job/{jobId}/content")
    @Permission
    public Response<JobInfo> getJobContent(@PathVariable("jobId") @NotNull String jobId) {
        String content = jobService.getJobContent(jobId);
        JobInfo jobInfo = new JobInfo();
        jobInfo.setContent(content);

        return new Response<JobInfo>().data(jobInfo);
    }


    @ApiOperation("Get Current Job Running Script")
    @PostMapping("/job/{jobId}/cancel")
    @Permission
    public Response<String> killJob(@PathVariable("jobId") @NotNull String jobId) {
        jobService.killJobById(jobId);
        return new Response<String>().data(jobId);
    }

    @ApiOperation("Get Current Job Console Log")
    @GetMapping("/job/{jobId}/log")
    @Permission
    public Response<JobLogDTO> getJobLog(@PathVariable("jobId") @NotNull String jobId) {
        String user = WebUtils.getCurrentLoginUser();
        JobLog jobLog = jobService.getJobLog(user, jobId);
        return new Response<JobLogDTO>().data(JobLogDTO.valueOf(jobId, jobLog));
    }

    @ApiOperation("MLSQL Engine job callback")
    @PostMapping("/job/callback")
    public Response callback(HttpServletRequest request) {

        String stat = request.getParameter("stat");
        String msg = request.getParameter("msg");
        String result = request.getParameter("res");
        Map jobInfoMap = JacksonUtils.readJson(request.getParameter("jobInfo"), Map.class);
        String jobId = jobInfoMap.get("jobName").toString();
        String groupId = jobInfoMap.get("groupId").toString();

        jobService.setJobAndGroup(jobId, groupId);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setMsg(msg);
        jobInfo.setResult(result);
        jobInfo.setFinishTime(new Timestamp(System.currentTimeMillis()));
        if (stat != null && stat.equals("succeeded")) {
            jobInfo.setStatus(JobInfo.JobStatus.SUCCESS);
        } else {
            jobInfo.setStatus(JobInfo.JobStatus.FAILED);
        }

        // get job progress
        try {
            JobProgress jobProgress = jobService.getJobProgress(jobId);
            if (jobProgress != null) {
                jobInfo.setJobProgress(JacksonUtils.writeJson(jobProgress));
            }
        } catch (Exception e) {
            log.warn("get job progress failed, job id is {}.", jobId);
        }

        jobService.updateByJobId(jobInfo);

        return new Response<>().data("{}");
    }

    @ApiOperation("Get Job List")
    @GetMapping("/jobs")
    @Permission
    public Response<JobInfoListDTO> getJobList(@RequestParam(value = "page_size", required = false) Integer pageSize,
                                               @RequestParam(value = "page_offset", required = false) Integer pageOffset,
                                               @RequestParam(value = "sort_by", required = false) String sortBy,
                                               @RequestParam(value = "status", required = false) String status,
                                               @RequestParam(value = "reverse", required = false) Boolean reverse,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "engine", required = false) String engine) {

        String user = WebUtils.getCurrentLoginUser();
        Pair<Long, List<JobInfo>> jobSearchResult = jobService.getJobList(pageSize, pageOffset, sortBy, reverse, status, user, keyword);

        Long count = jobSearchResult.getFirst();
        if (count == null || count == 0) {
            return new Response();
        }

        List<JobInfoDTO> jobInfoDTOS = jobSearchResult.getSecond()
                .stream()
                .map(jobInfo -> JobInfoDTO.valueOf(jobInfo))
                .collect(Collectors.toList());
        JobInfoListDTO jobInfoListDTO = JobInfoListDTO.valueOf(count, jobInfoDTOS);

        return new Response<JobInfoListDTO>().data(jobInfoListDTO);
    }

    @ApiOperation("Job cleaner for test")
    @DeleteMapping("/job/clean")
    @Permission
    public Response<String> cleanJobHistory() {
        NotebookConfig notebookConfig = NotebookConfig.getInstance();
        Integer maxSize = Integer.parseInt(notebookConfig.getJobHistorySize());
        Long maxTime = Long.valueOf(notebookConfig.getJobHistoryTime());

        Timestamp time = new Timestamp(System.currentTimeMillis() - maxTime * 24 * 60 * 60 * 1000);
        Integer effectedNum = jobInfoRepository.deleteJobBefore(String.valueOf(time), maxSize);
        String result = String.format("clean %s job history at %s", effectedNum, new Date());
        log.info(result);
        return new Response<String>().data(result);
    }
}
