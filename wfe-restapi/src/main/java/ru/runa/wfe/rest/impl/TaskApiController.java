package ru.runa.wfe.rest.impl;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.WfTaskDto;
import ru.runa.wfe.rest.dto.WfTasksDto;
import ru.runa.wfe.rest.dto.WfTaskMapper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;

@RestController
@RequestMapping("/tasks/")
@Transactional
public class TaskApiController {
    Logger log = LoggerFactory.getLogger(TaskApiController.class);
    
    @Autowired
    private TaskLogic taskLogic;
    @Autowired
    private WfTaskMapper taskMapper;

    // required = false temporary for simple queries without request body
    @PostMapping
    public WfTasksDto getTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        WfTasksDto tasksDto = new WfTasksDto();
        BatchPresentation batchPresentation = request != null 
                ? request.toBatchPresentation(ClassPresentationType.TASK)
                : BatchPresentationFactory.TASKS.createDefault();
        List<WfTask> tasks = taskLogic.getMyTasks(authUser.getUser(), batchPresentation);
        tasksDto.setTasks(taskMapper.map(tasks));
        List<WfTask> total = taskLogic.getMyTasks(authUser.getUser(), BatchPresentationFactory.TASKS.createDefault());
        tasksDto.setTotal(total.size());
        return tasksDto;
    }

    @GetMapping("{id}")
    public WfTaskDto getTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfTaskDto taskDto;
        WfTask task = taskLogic.getTask(authUser.getUser(), id);
        taskDto = taskMapper.map(task);
        return taskDto;
    }

    @PostMapping("{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public void completeTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        taskLogic.completeTask(authUser.getUser(), id, variables);
    }

}
