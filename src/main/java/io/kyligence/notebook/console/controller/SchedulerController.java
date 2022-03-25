package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.dto.req.CreateScheduleReq;
import io.kyligence.notebook.console.bean.dto.req.ModifyScheduleReq;
import io.kyligence.notebook.console.bean.dto.req.ScheduleCallbackReq;
import io.kyligence.notebook.console.service.SchedulerService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on Schedule")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    @ApiOperation("Schedule Execute")
    @PostMapping("/schedule/execution")
    public Response<String> callback(@RequestBody @Validated ScheduleCallbackReq scheduleCallbackReq) {

        schedulerService.callback(
                scheduleCallbackReq.getToken(),
                scheduleCallbackReq.getUser(),
                scheduleCallbackReq.getEntityType(),
                scheduleCallbackReq.getEntityId(),
                scheduleCallbackReq.getCommitId()
        );
        return new Response<String>().msg("success");
    }

    @ApiOperation("List Schedulers")
    @GetMapping("/schedule/scheduler")
    @Permission
    public Response<List<IdNameDTO>> ListScheduler() {
        return new Response<List<IdNameDTO>>().data(schedulerService.getSchedulerList());
    }

    @ApiOperation("Get Schedule By ID")
    @GetMapping("/schedule/task/{id}")
    @Permission
    public Response<TaskInfoDTO> getScheduleById(@PathVariable("id") @NotNull Integer id,
                                                 @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                 @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        TaskInfoDTO taskInfoDTO = schedulerService.getScheduleById(user, schedulerId, projectName, id);
        return new Response<TaskInfoDTO>().data(taskInfoDTO);
    }

    @ApiOperation("Get Schedule List")
    @GetMapping("/schedule/task/list")
    @Permission
    public Response<List<TaskInfoDTO>> getScheduleList(@RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                       @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        List<TaskInfoDTO> taskInfoDTO = schedulerService.getScheduleList(user, schedulerId, projectName);
        return new Response<List<TaskInfoDTO>>().data(taskInfoDTO);
    }

    @ApiOperation("Get Schedule By Entity")
    @GetMapping("/schedule/task")
    @Permission
    public Response<TaskInfoDTO> getScheduleByEntity(@RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                     @RequestParam(value = "project_name", required = false) String projectName,
                                                     @RequestParam(value = "entity_type", required = false) String entityType,
                                                     @RequestParam(value = "entity_id", required = false) Integer entityId) {
        String user = WebUtils.getCurrentLoginUser();
        TaskInfoDTO taskInfoDTO = schedulerService.getScheduleByEntity(user, schedulerId, projectName, entityType, entityId);
        return new Response<TaskInfoDTO>().data(taskInfoDTO);
    }

    @ApiOperation("Create Schedule")
    @PostMapping("/schedule/task")
    @Permission
    public Response<String> createSchedule(@RequestBody @Validated CreateScheduleReq createScheduleReq) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.createSchedule(
                createScheduleReq.getSchedulerId(),
                createScheduleReq.getName(),
                createScheduleReq.getDescription(),
                user,
                createScheduleReq.getEntityType(),
                createScheduleReq.getEntityId(),
                createScheduleReq.getCommitId(),
                createScheduleReq.getTaskName(),
                createScheduleReq.getTaskDesc(),
                createScheduleReq.getSchedule(),
                createScheduleReq.getExtra()
        );
        return new Response<String>().msg("success");
    }

    @ApiOperation("Modify Schedule")
    @PutMapping("/schedule/task/{id}")
    @Permission
    public Response<String> modifySchedule(@PathVariable("id") @NotNull Integer id, @RequestBody @Validated ModifyScheduleReq modifyScheduleReq) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.updateSchedule(
                modifyScheduleReq.getSchedulerId(),
                id,
                modifyScheduleReq.getName(),
                modifyScheduleReq.getDescription(),
                user,
                modifyScheduleReq.getModification(),
                modifyScheduleReq.getSchedule(),
                modifyScheduleReq.getExtra()
        );
        return new Response<String>().msg("success");
    }

    @ApiOperation("Delete Schedule")
    @DeleteMapping("/schedule/task/{id}")
    @Permission
    public Response<String> deleteSchedule(@PathVariable("id") @NotNull Integer id,
                                           @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                           @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.deleteSchedule(user, schedulerId, projectName, id);
        return new Response<String>().msg("success");
    }

    @ApiOperation("Get Task Instance")
    @GetMapping("/schedule/task/instance")
    @Permission
    public Response<List<TaskInstanceDTO>> getTaskInstance(@RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                           @RequestParam(value = "project_name", required = false) String projectName,
                                                           @RequestParam(value = "task_id", required = false) Integer taskId) {
        String user = WebUtils.getCurrentLoginUser();
        List<TaskInstanceDTO> instances = schedulerService.getInstanceList(user, schedulerId, projectName, taskId);
        return new Response<List<TaskInstanceDTO>>().data(instances);
    }

    @ApiOperation("Get Task Instance Node")
    @GetMapping("/schedule/task/instance/{id}")
    @Permission
    public Response<List<TaskNodeInfoDTO>> getTaskInstance(@PathVariable("id") @NotNull Long id,
                                                           @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                           @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        List<TaskNodeInfoDTO> instances = schedulerService.getInstanceNodes(user, id, schedulerId, projectName);
        return new Response<List<TaskNodeInfoDTO>>().data(instances);
    }

    @ApiOperation("Online Task")
    @PostMapping("/schedule/task/{id}/online")
    @Permission
    public Response<String> online(@PathVariable("id") @NotNull Integer id,
                                   @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                   @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.onlineTask(user, id, schedulerId, projectName);
        return new Response<String>().data("success");
    }

    @ApiOperation("Offline Task")
    @PostMapping("/schedule/task/{id}/offline")
    @Permission
    public Response<String> offline(@PathVariable("id") @NotNull Integer id,
                                    @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                    @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.offlineTask(user, id, schedulerId, projectName);
        return new Response<String>().data("success");
    }

    @ApiOperation("Run Task")
    @PostMapping("/schedule/task/{id}/execution")
    @Permission
    public Response<String> run(@PathVariable("id") @NotNull Integer id,
                                @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.runTask(user, schedulerId, projectName, id);
        return new Response<String>().data("success");
    }

    @ApiOperation("Stop Task Instance")
    @PostMapping("/schedule/task/instance/{id}/status")
    @Permission
    public Response<String> setTaskInstanceStatus(@PathVariable("id") @NotNull Long id,
                                 @RequestParam(value = "set_status") Integer setStatus,
                                 @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                 @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        schedulerService.setStatus(user, schedulerId, projectName, id, setStatus);
        return new Response<String>().data("success");
    }

    @ApiOperation("Get Task Instance Status")
    @GetMapping("/schedule/task/instance/{id}/status")
    @Permission
    public Response<String> getTaskInstanceStatus(@PathVariable("id") @NotNull Long id,
                                                  @RequestParam(value = "scheduler", required = false) Integer schedulerId,
                                                  @RequestParam(value = "project_name", required = false) String projectName) {
        String user = WebUtils.getCurrentLoginUser();
        String status = schedulerService.getInstanceStatus(user, id, schedulerId, projectName);
        return new Response<String>().data(status);
    }
}
