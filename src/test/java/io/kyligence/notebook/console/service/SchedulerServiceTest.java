package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.NotebookLauncherTestBase;
import io.kyligence.notebook.console.bean.dto.IdNameDTO;
import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.dto.TaskInstanceDTO;
import io.kyligence.notebook.console.bean.dto.TaskNodeInfoDTO;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.scheduler.RemoteSchedulerInterface;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

public class SchedulerServiceTest extends NotebookLauncherTestBase {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private JobService jobService;

    @InjectMocks
    private SchedulerService mockSchedulerService;

    @Mock
    private RemoteSchedulerInterface mockRemoteSchedulerInterface;

    private final Integer mockSchedulerId = 5;

    private final Integer mockSchedulerTaskId = 50;
    private final Integer mockNotebookId = 100;
    private final Long mockSchedulerTaskInstanceId = 102448L;

    private final String mockSchedulerName = "MockScheduler";
    private final String mockUser = "MockScheduler";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final NotebookConfig config = NotebookConfig.getInstance();

    @Override
    @PostConstruct
    public void mock() throws Exception {
        super.mock();
        mockSchedulerService.mockEnableSchedule();
        mockSchedulerService.mockScheduler(mockSchedulerId, mockRemoteSchedulerInterface);
    }

    @Override
    public String getCollectionName() {
        return "scheduler";
    }

    @Test
    public void testCreateSchedule() {
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.createSchedule(1, "task-not-exist", null, mockUser,
                "notebook", 10, null, null, null,
                null, null);
    }

    @Test
    public void testUpdateSchedule() {
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.updateSchedule(1, 12, null, null,
                "mock-user", null, null, null);
    }

    @Test
    public void testRunTask() {
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.runTask("mock-user", 1, null, null);
    }

    @Test
    public void testSetStatus() {
        mockSchedulerService.setStatus(mockUser, mockSchedulerId, null,
                mockSchedulerTaskInstanceId, 1);
        verify(mockRemoteSchedulerInterface, times(1)).sendCommand(null, mockUser,
                mockSchedulerTaskInstanceId, 1);
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.setStatus("mock-user", 1, null, 12L, 1);
    }

    @Test
    public void testDeleteSchedule() {
        mockSchedulerService.deleteSchedule(mockUser, mockSchedulerId, null, mockSchedulerTaskId);
        verify(mockRemoteSchedulerInterface, times(1)).deleteTask(mockUser,
                null, mockSchedulerTaskId);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.deleteSchedule("mock-user", 1, null, 1);
    }

    @Test
    public void testOnlineTask() {
        mockSchedulerService.onlineTask(mockUser, mockSchedulerTaskId, mockSchedulerId, null);
        verify(mockRemoteSchedulerInterface, times(1)).onlineTask(mockUser,
                mockSchedulerTaskId, null);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.onlineTask("mock-user", 1, null, null);
    }

    @Test
    public void testOfflineTask() {
        mockSchedulerService.offlineTask(mockUser, mockSchedulerTaskId, mockSchedulerId, null);
        verify(mockRemoteSchedulerInterface, times(1)).offlineTask(mockUser,
                mockSchedulerTaskId, null);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.offlineTask("mock-user", 1, null, null);
    }

    @Test
    public void testGetSchedulerList() {
        when(mockRemoteSchedulerInterface.getServiceName()).thenReturn(mockSchedulerName);
        List<IdNameDTO> schedulers = mockSchedulerService.getSchedulerList();
        Assert.assertEquals(1, schedulers.size());
        Assert.assertEquals(mockSchedulerName, schedulers.get(0).getName());
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getSchedulerList();
    }

    @Test
    public void testGetScheduleById() {
        when(mockRemoteSchedulerInterface.getTask(any(), eq(mockUser), eq(mockSchedulerTaskId)))
                .thenReturn(new TaskInfoDTO());
        Assert.assertNotNull(mockSchedulerService.getScheduleById(mockUser, mockSchedulerId,
                null, mockSchedulerTaskId));

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getScheduleById("mock-user", null, null, 1);
    }

    @Test
    public void testGetScheduleByEntity() {
        when(mockRemoteSchedulerInterface.getTask(any(), eq(mockUser), eq("notebook"), eq(mockNotebookId)))
                .thenReturn(new TaskInfoDTO());
        Assert.assertNotNull(mockSchedulerService.getScheduleByEntity(mockUser, mockSchedulerId,
                null, "notebook", mockNotebookId));

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getScheduleByEntity("mock-user", null, null,
                "notebook", 1);
    }

    @Test
    public void testGetScheduleList() {
        List<TaskInfoDTO> mockData = new ArrayList<>();
        mockData.add(new TaskInfoDTO());
        mockData.add(new TaskInfoDTO());
        when(mockRemoteSchedulerInterface.getTasks(any(), eq(mockUser)))
                .thenReturn(mockData);
        Assert.assertEquals(mockData.size(), mockSchedulerService.getScheduleList(mockUser,
                mockSchedulerId, null).size());

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getScheduleList("mock-user", null, null);
    }

    @Test
    public void testGetInstanceNodes() {
        List<TaskNodeInfoDTO> mockData = new ArrayList<>();
        mockData.add(new TaskNodeInfoDTO());
        mockData.add(new TaskNodeInfoDTO());

        when(mockRemoteSchedulerInterface.getTaskInstanceNodes(any(), eq(mockUser), eq(mockSchedulerTaskInstanceId)))
                .thenReturn(mockData);

        Assert.assertEquals(mockData.size(),
                mockSchedulerService.getInstanceNodes(mockUser, mockSchedulerTaskInstanceId,
                        mockSchedulerId, null).size());
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getInstanceNodes("mock-user", 1000L, null, null);
    }

    @Test
    public void testGetInstanceList() {
        List<TaskInstanceDTO> mockData = new ArrayList<>();
        TaskInstanceDTO t1 = new TaskInstanceDTO();
        t1.setOwner(mockUser);
        t1.setTaskId(mockSchedulerTaskId);
        TaskInstanceDTO t2 = new TaskInstanceDTO();
        t2.setOwner(mockUser);
        t2.setTaskId(10);
        mockData.add(t2);
        mockData.add(t1);

        when(mockRemoteSchedulerInterface.getTaskInstances(any(), eq(mockUser))).thenReturn(mockData);
        Assert.assertEquals(1, mockSchedulerService.getInstanceList(mockUser, mockSchedulerId,
                null, mockSchedulerTaskId).size());
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getInstanceList("mock-user", null, null, mockSchedulerTaskId);
    }

    @Test
    public void testEntityUsedInSchedule() {
        Assert.assertFalse(schedulerService.entityUsedInSchedule("notebook", 1));
    }

    @Test
    public void testCallback() {
        schedulerService.callback(config.getScheduleCallbackToken(),
                "admin", "notebook", defaultMockNotebookId, null);
        List<JobInfo> jobs = jobService.getJobList(null, null, null, null,
                String.valueOf(JobInfo.JobStatus.SUCCESS), config.getScheduleCallbackUser(), null).getSecond();
        Assert.assertNotEquals(0, jobs.size());
        Assert.assertNotNull(jobs.get(0));
    }
}
