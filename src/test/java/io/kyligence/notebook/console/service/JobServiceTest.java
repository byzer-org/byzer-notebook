package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherTestBase;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.dao.JobInfoRepository;

import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class JobServiceTest extends NotebookLauncherTestBase {

    public static boolean initialized;

    private final String mockJobId = "1782986a-2ad7-40ac-9f5f-d680f5bca1e4";
    private JobInfo mockJobInfo;
    private final String mockGroupId = "d4f757aa-83a8-42f2-a874-9babec18e5aa";
    private final String testJobId = "cf2ea2a0-8548-4d90-93a3-f4a7e53d4b3e";
    private final String testJobContent = "!delta help;";

    private final String testJobId2 = "de9cd6e6-2275-44fd-87cd-11846c844f9c";

    private final String MOCK_JOB_LOG = "mock_job_log";

    @Autowired
    private JobService jobService;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @InjectMocks
    private JobService mockJobService;

    @Mock
    private JobInfoRepository mockJobInfoRepository;

    @Mock
    private EngineService mockEngineService;

    @Override
    @PostConstruct
    public void mock() {
        mockJobService.setJobAndGroup(mockJobId, mockGroupId);
        mockJobInfo = new JobInfo();
        mockJobInfo.setJobId(mockJobId);
        mockJobInfo.setStatus(JobInfo.JobStatus.RUNNING);
        List<JobInfo> mockJobs = Lists.newArrayList();
        mockJobs.add(mockJobInfo);
        when(mockJobInfoRepository.findByJobId(eq(mockJobId))).thenReturn(new ArrayList<>(mockJobs));

        if (initialized) return;
        insertJobInfo(testJobId, JobInfo.JobStatus.RUNNING, "TEST", testJobContent);
        insertJobInfo(testJobId2, JobInfo.JobStatus.SUCCESS);
        insertJobInfo("test2", JobInfo.JobStatus.FAILED);
        insertJobInfo("test3", JobInfo.JobStatus.KILLED);
        initialized = true;
    }

    private void insertJobInfo(String jobId, Integer status) {
        insertJobInfo(jobId, status, "admin");
    }

    private void insertJobInfo(String jobId, Integer status, String owner) {
        insertJobInfo(jobId, status, owner, "!show version;");
    }

    private void insertJobInfo(String jobId, Integer status, String owner, String content) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setStatus(status);
        jobInfo.setName("TestJob" + jobId);
        jobInfo.setUser(owner);
        jobInfo.setContent(content);
        jobService.insert(jobInfo);
    }


    @Test
    public void testGetJobById() {
        Assert.assertNull(mockJobService.findByJobId("not-exist"));
        JobInfo matched = mockJobService.findByJobId(mockJobId);
        Assert.assertNotNull(matched);
        Assert.assertEquals(matched, mockJobInfo);
    }

    @Override
    public String getCollectionName() {
        return "jobs";
    }

    @Test
    public void testGetJobLog() {
        when(mockEngineService.runScript(any())).thenReturn(getResponseContent(MOCK_JOB_LOG));
        JobLog jobLog = mockJobService.getJobLog("admin", mockJobId);
        Assert.assertNotNull(jobLog);
    }

    @Test
    public void testGetJobAndGroup() {
        String jobId = "05be74e3-30ad-4055-b524-cc5101e10e2b";
        Assert.assertEquals(jobId, mockJobService.getGroupOrJobId(jobId));
        Assert.assertEquals(mockGroupId, mockJobService.getGroupOrJobId(mockJobId));
    }

    @Test
    public void testKillJobById() {
        jobService.killJobById(testJobId);
        JobInfo matched = jobService.findByJobId(testJobId);
        Assert.assertEquals(JobInfo.JobStatus.KILLED, (int) matched.getStatus());
        try {
            jobService.killJobById("fake-job-id");
        } catch (ByzerException byzerException) {
            Assert.assertEquals(byzerException.getCode(), ErrorCodeEnum.JOB_NOT_EXIST.getCode());
        }
    }

    @Test
    public void testUpdateByJobId() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(testJobId2);
        String content = "!profiler sql ''' select 1 as a ''' ;";
        jobInfo.setContent(content);
        jobService.updateByJobId(jobInfo);
        JobInfo updatedJobInfo = jobService.findByJobId(testJobId2);
        Assert.assertEquals(content, updatedJobInfo.getContent());
    }

    @Test
    public void testInsert() {
        String jobId = "05be74e3-30ad-4055-b524-cc5101e10e2b";
        insertJobInfo(jobId, JobInfo.JobStatus.KILLED, "TEST2");
        JobInfo jobInfo = jobService.findByJobId(jobId);
        Assert.assertEquals(jobInfo.getJobId(), jobId);
        jobInfoRepository.delete(jobInfo);
    }

    @Test
    public void testGetJobList() {
        Pair<Long, List<JobInfo>> result = jobService.getJobList(null, null, null,
                null, null, "admin", testJobId);
        Assert.assertEquals(1, result.getSecond().size());

        Pair<Long, List<JobInfo>> result2 = jobService.getJobList(null, null, null,
                null, null, "admin", "not-exist-job-id");
        Assert.assertEquals(0, result2.getSecond().size());

        Pair<Long, List<JobInfo>> result3 = jobService.getJobList(null, null, null,
                null, null, "TEST", null);
        Assert.assertEquals(1, result3.getFirst().intValue());


    }
}
