package ru.runa.wfe.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.VariableValueUnwrapper;
import ru.runa.wfe.rest.converter.WfeTaskMapper;
import ru.runa.wfe.rest.converter.WfeVariableMapper;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeTask;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.logic.VariableLogic;

@RestController
@RequestMapping("/task/")
@Transactional
public class TaskController {
    
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private VariableLogic variableLogic;

    @PostMapping("list")
    public WfePagedList<WfeTask> getTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        List<WfTask> tasks = taskLogic.getTasks(authUser.getUser(), filter.toBatchPresentation(ClassPresentationType.TASK));
        return new WfePagedList<>(tasks.size(), Mappers.getMapper(WfeTaskMapper.class).map(tasks));
    }

    @PostMapping("my")
    public WfePagedList<WfeTask> getMyTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        List<WfTask> tasks = taskLogic.getMyTasks(authUser.getUser(), filter.toBatchPresentation(ClassPresentationType.TASK));
        return new WfePagedList<>(tasks.size(), Mappers.getMapper(WfeTaskMapper.class).map(tasks));
    }

    @GetMapping("process/{processId}")
    public List<WfeTask> getProcessTasks(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long processId,
            @RequestParam(required = false) boolean includeSubprocesses) {
        List<WfTask> tasks = taskLogic.getTasks(authUser.getUser(), processId, includeSubprocesses);
        return Mappers.getMapper(WfeTaskMapper.class).map(tasks);
    }

    @GetMapping("unassigned")
    public List<WfeTask> getUnassignedTasks(@AuthenticationPrincipal AuthUser authUser) {
        List<WfTask> tasks = taskLogic.getUnassignedTasks(authUser.getUser());
        return Mappers.getMapper(WfeTaskMapper.class).map(tasks);
    }

    @GetMapping("{id}")
    public WfeTask getTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeTaskMapper.class).map(taskLogic.getTask(authUser.getUser(), id));
    }

    @PatchMapping("{id}/assign")
    public void assignTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String currentOwnerName,
            @RequestParam String newOwnerName) {
        Executor currentOwner = executorLogic.getExecutor(authUser.getUser(), currentOwnerName);
        Executor newOwner = executorLogic.getExecutor(authUser.getUser(), newOwnerName);
        taskLogic.assignTask(authUser.getUser(), id, currentOwner, newOwner);
    }

    @PatchMapping("reassign")
    public int reassignTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        return taskLogic.reassignTasks(authUser.getUser(), filter.toBatchPresentation(ClassPresentationType.TASK));
    }

    @PatchMapping("{id}/reassign")
    public boolean reassignTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return taskLogic.reassignTask(authUser.getUser(), id);
    }

    @ValidationException
    @PostMapping("{id}/complete")
    public WfeTask completeTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        WfTask task = taskLogic.getTask(authUser.getUser(), id);
        Map<String, Object> converted = new VariableValueUnwrapper().process(taskLogic.getDefinition(task.getDefinitionId()), variables);
        WfTask nextTask = taskLogic.completeTask(authUser.getUser(), id, converted);
        return Mappers.getMapper(WfeTaskMapper.class).map(nextTask);
    }

    @GetMapping("{id}/variable")
    public WfeVariable getTaskVariable(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name) {
        return Mappers.getMapper(WfeVariableMapper.class).map(variableLogic.getTaskVariable(authUser.getUser(), id, name));
    }

    @PatchMapping("{id}/markOpened")
    public void markTaskOpened(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        taskLogic.markTaskOpened(authUser.getUser(), id);
    }

    @PatchMapping("{id}/delegate")
    public void delegateTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String currentOwnerName,
            @RequestParam boolean keepCurrentOwner, @RequestBody List<String> executorNames) {
        Executor currentOwner = executorLogic.getExecutor(authUser.getUser(), currentOwnerName);
        List<Executor> executors = new ArrayList<>();
        for (String executorName : executorNames) {
            executors.add(executorLogic.getExecutor(authUser.getUser(), executorName));
        }
        taskLogic.delegateTask(authUser.getUser(), id, currentOwner, keepCurrentOwner, executors);
    }

    @PatchMapping("delegate")
    public void delegateTasks(@AuthenticationPrincipal AuthUser authUser, @RequestParam List<Long> ids, @RequestParam String currentOwnerName,
            @RequestParam boolean keepCurrentOwner, @RequestBody List<String> executorNames) {
        for (Long id : ids) {
            delegateTask(authUser, id, currentOwnerName, keepCurrentOwner, executorNames);
        }
    }
}
