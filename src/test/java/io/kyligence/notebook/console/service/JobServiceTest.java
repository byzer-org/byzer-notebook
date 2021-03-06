package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.bean.entity.JobInfoArchive;
import io.kyligence.notebook.console.bean.model.JobLog;
import io.kyligence.notebook.console.dao.JobInfoArchiveRepository;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class JobServiceTest extends NotebookLauncherBaseTest {

    public static boolean initialized;

    private final String mockJobId = "1782986a-2ad7-40ac-9f5f-d680f5bca1e4";
    private JobInfo mockJobInfo;
    private final String mockGroupId = "d4f757aa-83a8-42f2-a874-9babec18e5aa";
    private final String testJobId = "cf2ea2a0-8548-4d90-93a3-f4a7e53d4b3e";
    private final String testJobContent = "!delta help;";

    private final String testJobId2 = "de9cd6e6-2275-44fd-87cd-11846c844f9c";
    private final String testJobId3 = "9f14dc04-362d-46ea-b7e9-c38ebdde7d5f";

    private final String MOCK_JOB_LOG = "mock_job_log";
    private final String MOCK_JOB_PROGRESS = "mock_job_progress";
    private final String MOCK_CURRENT_JOB = "mock_current_job";

    @Autowired
    private JobService jobService;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private JobInfoArchiveRepository jobInfoArchiveRepository;

    @InjectMocks
    private JobService mockJobService;

    @Mock
    private JobInfoRepository mockJobInfoRepository;

    @Mock
    private JobInfoArchiveRepository mockJobInfoArchiveRepository;

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
        insertJobInfo(testJobId3, JobInfo.JobStatus.RUNNING);
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
        JobLog jobLog = mockJobService.getJobLog("admin", mockJobId, -1L);
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
        Assert.assertEquals(1, (long) result.getFirst());

        Pair<Long, List<JobInfo>> result2 = jobService.getJobList(null, null, null,
                null, null, "admin", "not-exist-job-id");
        Assert.assertEquals(0, (long) result2.getFirst());

        Pair<Long, List<JobInfo>> result3 = jobService.getJobList(null, null, null,
                null, null, "TEST", null);
        Assert.assertEquals(1, (long) result3.getFirst());
    }

    @Test
    public void testGetCurrentJob() {
        when(mockEngineService.runScript(any())).thenReturn("");
        Assert.assertNull(mockJobService.getCurrentJob(mockJobId));

        when(mockEngineService.runScript(any())).thenReturn(getResponseContent(MOCK_CURRENT_JOB));
        Assert.assertEquals(mockGroupId, mockJobService.getCurrentJob(mockJobId).getGroupId());

    }

    @Test
    public void testGetJobProgress() {
        when(mockEngineService.runScript(any())).thenReturn("[]");
        Assert.assertNull(mockJobService.getJobProgress(mockJobId));

        when(mockEngineService.runScript(any())).thenReturn(getResponseContent(MOCK_JOB_PROGRESS));

        Assert.assertEquals(1, (long) mockJobService.getJobProgress(mockJobId).getInProgressJobs());

    }

    @Test
    public void testIsRunning() {
        Assert.assertTrue(jobService.isRunning(JobInfo.JobStatus.RUNNING));
        Assert.assertTrue(jobService.isRunning(JobInfo.JobStatus.RETRYING));

        Assert.assertFalse(jobService.isRunning(null));
        Assert.assertFalse(jobService.isRunning(JobInfo.JobStatus.KILLED));
        Assert.assertFalse(jobService.isRunning(JobInfo.JobStatus.SUCCESS));
        Assert.assertFalse(jobService.isRunning(JobInfo.JobStatus.FAILED));
    }

    @Test
    public void testNeedRetry() {
        Assert.assertTrue(jobService.needRetry(JobInfo.JobStatus.RUNNING));
        Assert.assertTrue(jobService.needRetry(JobInfo.JobStatus.RETRYING + 1));

        Assert.assertFalse(jobService.needRetry(null));
        Assert.assertFalse(jobService.needRetry(JobInfo.JobStatus.RETRYING));
        Assert.assertFalse(jobService.needRetry(JobInfo.JobStatus.SUCCESS));
        Assert.assertFalse(jobService.needRetry(JobInfo.JobStatus.FAILED));
    }

    @Test
    public void testJobDone() {
        boolean resp = jobService.jobDone(testJobId3, JobInfo.JobStatus.SUCCESS,
                "mockResult", "", new Timestamp(System.currentTimeMillis()));
        Assert.assertTrue(resp);

        Assert.assertEquals(JobInfo.JobStatus.SUCCESS, (long) jobService.findByJobId(testJobId3).getStatus());
        String mockPythonCode = "#%python\n" +
                "#%env=source activate ray1.8.0\n" +
                "#%schema=st(field(info,string))\n" +
                "#%input=data\n" +
                "#%output=contentsTb\n" +
                "#%dataMode=model\n" +
                "\n" +
                "from pyjava.api.mlsql import RayContext, PythonContext\n" +
                "import pandas as pd\n" +
                "\n" +
                "context:PythonContext = context\n" +
                "ray_context = RayContext.connect(globals(),None)\n" +
                "data = ray_context.to_pandas()\n" +
                "\n" +
                "def test(d):\n" +
                "    context.log_client.log_to_driver(d)\n" +
                "    raise RuntimeError()\n" +
                "for d in data.iterrows():\n" +
                "    test(d)\n" +
                "context.build_result([{\"info\":\"data\"}])";

        insertJobInfo("testPythonError-jobId", JobInfo.JobStatus.RUNNING,
                "testPythonError-user", mockPythonCode);
        String mockMsg = "Job aborted due to stage failure: Task 0 in stage 34446.0 failed 1 times, most recent failure:" +
                " Lost task 0.0 in stage 34446.0 (TID 181638) (b77ec5268d1a executor driver): " +
                "org.apache.spark.SparkException: Traceback (most recent call last):\n" +
                "File \"/opt/conda/envs/ray1.8.0/lib/python3.6/site-packages/pyjava/worker.py\", line 155, in main\n" +
                "process()\n" +
                "File \"/opt/conda/envs/ray1.8.0/lib/python3.6/site-packages/pyjava/worker.py\", line 132, in process\n" +
                "exec(code, n_local, n_local)\n" +
                "File \"<string>\", line 14, in <module>\n" +
                "File \"<string>\", line 12, in test\n" +
                "RuntimeError\n" +
                "at tech.mlsql.arrow.python.runner.BasePythonRunner$ReaderIterator.handlePythonException(PythonRunner.scala:324)";
        jobService.jobDone("testPythonError-jobId", JobInfo.JobStatus.FAILED, "",
                mockMsg, new Timestamp(System.currentTimeMillis()));

        Assert.assertTrue(jobService.findByJobId("testPythonError-jobId").getMsg().startsWith("File"));

        String mockScript = "select * from ab as output;";
        insertJobInfo("testByzerError-jobId", JobInfo.JobStatus.RUNNING, "testPythonError-user",
                mockScript);
        mockMsg = "Table or view not found: ab; line 1 pos 14;";
        jobService.jobDone("testByzerError-jobId", JobInfo.JobStatus.FAILED, "",
                mockMsg, new Timestamp(System.currentTimeMillis()));

        Assert.assertEquals(jobService.findByJobId("testByzerError-jobId").getMsg(), mockMsg);

    }

    @Test
    public void testGetJobContent() {
        Assert.assertEquals(testJobContent, jobService.getJobContent(testJobId));
        Assert.assertNull(jobService.getJobContent("not-exist-job"));

        String archivedContent = "!show resource;";
        String archivedId = "test-archived";
        JobInfoArchive archived = new JobInfoArchive(archivedId, JobInfo.JobStatus.SUCCESS);
        archived.setContent(archivedContent);
        archived.setUser("testGetContent-user");
        archived.setName("mock-test-archive");
        jobInfoArchiveRepository.save(archived);

        Assert.assertEquals(archivedContent, jobService.getJobContent(archivedId));

    }

    @Test
    public void testGetJobArchiveList() {
        JobInfoArchive jobInfoArchive = new JobInfoArchive(testJobId2, JobInfo.JobStatus.SUCCESS);
        jobInfoArchive.setUser("TEST");
        jobInfoArchive.setContent("");
        jobInfoArchive.setName("mock-test-archive");
        jobInfoArchiveRepository.save(jobInfoArchive);


        Pair<Long, List<JobInfoArchive>> result = jobService.getJobArchiveList(null, null, null,
                null, null, "TEST", null);
        Assert.assertEquals(1, (long) result.getFirst());
    }
}
