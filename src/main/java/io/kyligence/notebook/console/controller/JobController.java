package io.kyligence.notebook.console.controller;


import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.entity.JobInfoArchive;
import io.kyligence.notebook.console.bean.model.CurrentJobInfo;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.service.JobService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on job")
public class JobController {

    @Autowired
    private JobService jobService;


    @ApiOperation("Get Job Result")
    @GetMapping("/job/{jobId}")
    @Permission
    public Response<JobInfoDTO> getJobResult(@PathVariable("jobId") @NotNull String jobId) {
        Integer status = jobService.getJobStatus(jobId);
        JobInfo jobInfo = new JobInfo(jobId, JobInfo.JobStatus.KILLED,
                null, null, null, null, null);
        JobProgressDTO jobProgress = null;

        if (Objects.equals(status, JobInfo.JobStatus.NOT_EXIST)) {
            return new Response<JobInfoDTO>().data(JobInfoDTO.valueOf(jobInfo, null));
        }

        if (jobService.isRunning(status)) {
            jobProgress = jobService.getJobProgress(jobId);
            jobInfo.setStatus(JobInfo.JobStatus.RUNNING);
            jobInfo.setCreateTime(jobService.getJobStartTime(jobId));
        } else {
            jobInfo = jobService.findByJobId(jobId);
            if (Objects.nonNull(jobInfo.getJobProgress())) {
                jobProgress = JacksonUtils.readJson(jobInfo.getJobProgress(), JobProgressDTO.class);
            }
            if (Objects.nonNull(jobProgress) && Objects.equals(status, JobInfo.JobStatus.SUCCESS)) {
                jobProgress.setProgress(1.0);
            }
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
    public Response<JobLogDTO> getJobLog(@PathVariable("jobId") @NotNull String jobId,
                                         @RequestParam(value = "offset", required = false, defaultValue = "-1") Long offset) {
        String user = WebUtils.getCurrentLoginUser();
        JobLog jobLog = jobService.getJobLog(user, jobId, offset);
        return new Response<JobLogDTO>().data(JobLogDTO.valueOf(jobId, jobLog));
    }

    @ApiOperation("MLSQL Engine job callback")
    @PostMapping("/job/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.OK.value());

        Timestamp finishTime = new Timestamp(System.currentTimeMillis());
        String stat = request.getParameter("stat");
        String msg = request.getParameter("msg");
        String result = request.getParameter("res");
        Map jobInfoMap = JacksonUtils.readJson(request.getParameter("jobInfo"), Map.class);
        String jobId = jobInfoMap.get("jobName").toString();
        String groupId = jobInfoMap.get("groupId").toString();
        Integer status = Objects.nonNull(stat) && stat.equals("succeeded") ?
                JobInfo.JobStatus.SUCCESS : JobInfo.JobStatus.FAILED;
        jobService.setJobAndGroup(jobId, groupId);

        boolean success = jobService.jobDone(jobId, status, result, msg, finishTime);

        if (!success) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
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
        if (count == 0) {
            return new Response<>();
        }

        List<JobInfoDTO> jobInfoDTOS = jobSearchResult.getSecond()
                .stream()
                .map(JobInfoDTO::valueOf)
                .collect(Collectors.toList());
        JobInfoListDTO jobInfoListDTO = JobInfoListDTO.valueOf(count, jobInfoDTOS);

        return new Response<JobInfoListDTO>().data(jobInfoListDTO);
    }

    @ApiOperation("Get Job List")
    @GetMapping("/jobs/archive")
    @Permission
    public Response<JobInfoListDTO> getJobArchiveList(@RequestParam(value = "page_size", required = false) Integer pageSize,
                                               @RequestParam(value = "page_offset", required = false) Integer pageOffset,
                                               @RequestParam(value = "sort_by", required = false) String sortBy,
                                               @RequestParam(value = "status", required = false) String status,
                                               @RequestParam(value = "reverse", required = false) Boolean reverse,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "engine", required = false) String engine) {

        String user = WebUtils.getCurrentLoginUser();
        Pair<Long, List<JobInfoArchive>> jobSearchResult = jobService.getJobArchiveList(pageSize, pageOffset, sortBy, reverse, status, user, keyword);

        Long count = jobSearchResult.getFirst();
        if (count == 0) {
            return new Response<>();
        }

        List<JobInfoDTO> jobInfoDTOS = jobSearchResult.getSecond()
                .stream()
                .map(JobInfoDTO::valueOf)
                .collect(Collectors.toList());
        JobInfoListDTO jobInfoListDTO = JobInfoListDTO.valueOf(count, jobInfoDTOS);

        return new Response<JobInfoListDTO>().data(jobInfoListDTO);
    }
}
