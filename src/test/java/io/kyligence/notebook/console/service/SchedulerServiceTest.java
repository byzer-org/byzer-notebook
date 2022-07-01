package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.dto.*;
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

public class SchedulerServiceTest extends NotebookLauncherBaseTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private JobService jobService;

    @InjectMocks
    private SchedulerService mockService;

    @Mock
    private NotebookService notebookService;

    @Mock
    private RemoteSchedulerInterface mockInterface;

    private final Integer mockSchedulerId = 5;

    private final Integer mockTaskId = 50;
    private final Integer mockNotebookId = 100;
    private final Long mockTaskInstanceId = 102448L;

    private final String mockSchedulerName = "MockScheduler";
    private final String mockUser = "MockScheduler";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final NotebookConfig config = NotebookConfig.getInstance();

    @Override
    @PostConstruct
    public void mock() throws Exception {
        super.mock();
        mockService.mockEnableSchedule();
        mockService.mockScheduler(mockSchedulerId, mockInterface);
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
                null, null, null);
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
        mockService.setStatus(mockUser, mockSchedulerId, null,
                mockTaskInstanceId, 1);
        verify(mockInterface, times(1)).sendCommand(null, mockUser,
                mockTaskInstanceId, 1);
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.setStatus("mock-user", 1, null, 12L, 1);
    }

    @Test
    public void testDeleteSchedule() {
        mockService.deleteSchedule(mockUser, mockSchedulerId, null, mockTaskId);
        verify(mockInterface, times(1)).deleteTask(mockUser,
                null, mockTaskId);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.deleteSchedule("mock-user", 1, null, 1);
    }

    @Test
    public void testOnlineTask() {
        mockService.onlineTask(mockUser, mockTaskId, mockSchedulerId, null);
        verify(mockInterface, times(1)).onlineTask(mockUser,
                mockTaskId, null);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.onlineTask("mock-user", 1, null, null);
    }

    @Test
    public void testOfflineTask() {
        mockService.offlineTask(mockUser, mockTaskId, mockSchedulerId, null);
        verify(mockInterface, times(1)).offlineTask(mockUser,
                mockTaskId, null);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.offlineTask("mock-user", 1, null, null);
    }

    @Test
    public void testGetSchedulerList() {
        when(mockInterface.getServiceName()).thenReturn(mockSchedulerName);
        List<IdNameDTO> schedulers = mockService.getSchedulerList();
        Assert.assertEquals(1, schedulers.size());
        Assert.assertEquals(mockSchedulerName, schedulers.get(0).getName());
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getSchedulerList();
    }

    @Test
    public void testGetScheduleById() {
        when(mockInterface.getTask(any(), eq(mockUser), eq(mockTaskId)))
                .thenReturn(new TaskInfoDTO());
        Assert.assertNotNull(mockService.getScheduleById(mockUser, mockSchedulerId,
                null, mockTaskId));

        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getScheduleById("mock-user", null, null, 1);
    }

    @Test
    public void testGetScheduleByEntity() {
        when(mockInterface.getTask(any(), eq(mockUser), eq("notebook"), eq(mockNotebookId)))
                .thenReturn(new TaskInfoDTO());
        Assert.assertNotNull(mockService.getScheduleByEntity(mockUser, mockSchedulerId,
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
        when(mockInterface.getTasks(any(), eq(mockUser)))
                .thenReturn(mockData);
        Assert.assertEquals(mockData.size(), mockService.getScheduleList(mockUser,
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

        when(mockInterface.getTaskInstanceNodes(any(), eq(mockUser), eq(mockTaskInstanceId)))
                .thenReturn(mockData);

        Assert.assertEquals(mockData.size(),
                mockService.getInstanceNodes(mockUser, mockTaskInstanceId,
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
        t1.setTaskId(mockTaskId);
        TaskInstanceDTO t2 = new TaskInstanceDTO();
        t2.setOwner(mockUser);
        t2.setTaskId(10);
        mockData.add(t2);
        mockData.add(t1);

        when(mockInterface.getTaskInstances(any(), eq(mockUser))).thenReturn(mockData);
        Assert.assertEquals(1, mockService.getInstanceList(mockUser, mockSchedulerId,
                null, mockTaskId).size());
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getInstanceList("mock-user", null, null, mockTaskId);
    }

    @Test
    public void testGetInstanceStatus() {
        String status = "RUNNING";
        when(mockInterface.getTaskInstanceStatus(any(), eq(mockUser), eq(mockTaskInstanceId)))
                .thenReturn(status);
        Assert.assertEquals(status, mockService.getInstanceStatus(mockUser, mockTaskInstanceId, mockSchedulerId, null));
        thrown.expect(ByzerException.class);
        thrown.expectMessage("SchedulerService not enabled");
        schedulerService.getInstanceStatus("mock-user", mockTaskInstanceId, mockSchedulerId, null);
    }

    @Test
    public void testEntityUsedInSchedule() {
        String mockName = "mockNotebook";
        NotebookDTO mockNotebook = new NotebookDTO();
        mockNotebook.setName(mockName);
        when(mockInterface.searchForEntity(any(), eq("notebook"), eq(mockNotebookId)))
                .thenReturn(new TaskInfoDTO());
        when(notebookService.getNotebook(eq(mockNotebookId), any()))
                .thenReturn(mockNotebook);
        Assert.assertTrue(mockService.entityUsedInSchedule("notebook", mockNotebookId));
        Assert.assertFalse(schedulerService.entityUsedInSchedule("notebook", 1));
    }

    @Test
    public void testCallback() {
        schedulerService.callback(config.getScheduleCallbackToken(),
                "admin", "notebook", defaultMockNotebookId, null, null);

        schedulerService.callback(config.getScheduleCallbackToken(),
                "admin", "notebook", defaultMockNotebookId, null, 180);

        List<JobInfo> jobs = jobService.getJobList(null, null, null, null,
                String.valueOf(JobInfo.JobStatus.SUCCESS), config.getScheduleCallbackUser(), null).getSecond();
        Assert.assertNotEquals(0, jobs.size());
        Assert.assertNotNull(jobs.get(0));
    }
}
