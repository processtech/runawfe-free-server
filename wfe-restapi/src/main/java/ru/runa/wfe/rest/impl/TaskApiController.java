package ru.runa.wfe.rest.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/tasks")
@Transactional
public class TaskApiController {
    @Autowired
    private TaskLogic taskLogic;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<WfTask> get() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return taskLogic.getMyTasks(user, BatchPresentationFactory.TASKS.createNonPaged());
    }

}
