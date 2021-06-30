package ru.runa.wfe.rest.impl;

import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
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

    @PostMapping
    public PagedList<WfTaskDto> getTasks(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.TASK);
        List<WfTask> tasks = taskLogic.getMyTasks(authUser.getUser(), batchPresentation);
        WfTaskMapper mapper = Mappers.getMapper(WfTaskMapper.class);
        return new PagedList<WfTaskDto>(tasks.size(), mapper.map(tasks));
    }

    @GetMapping("{id}")
    public WfTaskDto getTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfTask task = taskLogic.getTask(authUser.getUser(), id);
        WfTaskMapper mapper = Mappers.getMapper(WfTaskMapper.class);
        WfTaskDto taskDto = mapper.map(task);
        return taskDto;
    }

    @PostMapping("{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public void completeTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        taskLogic.completeTask(authUser.getUser(), id, variables);
    }

}
