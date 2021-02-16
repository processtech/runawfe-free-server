package ru.runa.wfe.rest.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;

@RestController
@RequestMapping("/tasks")
@Transactional
public class TaskApiController {
    @Autowired
    private TaskLogic taskLogic;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<WfTask> getTasks(@AuthenticationPrincipal AuthUser authUser) {
        return taskLogic.getMyTasks(authUser.getUser(), BatchPresentationFactory.TASKS.createNonPaged());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public WfTask getTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long taskId) {
        return taskLogic.getTask(authUser.getUser(), taskId);
    }

}
