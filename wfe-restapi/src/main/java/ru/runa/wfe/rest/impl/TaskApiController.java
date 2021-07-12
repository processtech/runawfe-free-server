package ru.runa.wfe.rest.impl;

import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfTaskDto;
import ru.runa.wfe.rest.dto.WfTaskMapper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;

@RestController
@RequestMapping("/task/")
@Transactional
public class TaskApiController {
    
    @Autowired
    private TaskLogic taskLogic;

    @PostMapping("list")
    public PagedList<WfTaskDto> getTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        List<WfTask> tasks = taskLogic.getTasks(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.TASK));
        return new PagedList<>(tasks.size(), Mappers.getMapper(WfTaskMapper.class).map(tasks));
    }

    @PostMapping
    public PagedList<WfTaskDto> getMyTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        List<WfTask> tasks = taskLogic.getMyTasks(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.TASK));
        return new PagedList<>(tasks.size(), Mappers.getMapper(WfTaskMapper.class).map(tasks));
    }

    @GetMapping("process")
    public PagedList<WfTaskDto> getProcessTasks(@AuthenticationPrincipal AuthUser authUser, Long processId,
            @RequestParam(required = false) boolean includeSubprocesses) {
        List<WfTask> tasks = taskLogic.getTasks(authUser.getUser(), processId, includeSubprocesses);
        return new PagedList<>(tasks.size(), Mappers.getMapper(WfTaskMapper.class).map(tasks));
    }

    @GetMapping("unassigned")
    public PagedList<WfTaskDto> getUnassignedTasks(@AuthenticationPrincipal AuthUser authUser) {
        List<WfTask> tasks = taskLogic.getUnassignedTasks(authUser.getUser());
        return new PagedList<>(tasks.size(), Mappers.getMapper(WfTaskMapper.class).map(tasks));
    }

    @GetMapping("{id}")
    public WfTaskDto get(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfTaskMapper.class).map(taskLogic.getTask(authUser.getUser(), id));
    }

    @PatchMapping("{id}/assign")
    public void assign(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long previousOwnerId, Long newExecutorId) {
        taskLogic.assignTask(authUser.getUser(), id, previousOwnerId, newExecutorId);
    }

    @PatchMapping("reassign")
    public int reassignTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        return taskLogic.reassignTasks(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.TASK));
    }

    @PatchMapping("{id}/reassign")
    public boolean reassign(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return taskLogic.reassignTask(authUser.getUser(), id);
    }

    @PostMapping("{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public void complete(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        taskLogic.completeTask(authUser.getUser(), id, variables);
    }

    @PatchMapping("{id}/markOpened")
    public void markOpened(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        taskLogic.markTaskOpened(authUser.getUser(), id);
    }

    @PatchMapping("{id}/delegate")
    public void delegate(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long currentOwnerId,
            @RequestParam(required = false) boolean keepCurrentOwners, @RequestBody List<Long> executorIds) {
        taskLogic.delegateTask(authUser.getUser(), id, currentOwnerId, keepCurrentOwners, executorIds);
    }

    @PatchMapping("delegate")
    public void delegate(@AuthenticationPrincipal AuthUser authUser, @RequestParam List<Long> ids, Long currentOwnerId,
            @RequestParam(required = false) boolean keepCurrentOwners, @RequestBody List<Long> executorIds) {
        for (Long id : ids) {
            taskLogic.delegateTask(authUser.getUser(), id, currentOwnerId, keepCurrentOwners, executorIds);
        }
    }
}
