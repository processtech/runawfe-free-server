package ru.runa.wfe.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/task")
@Transactional
public class TaskApiController {
    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Autowired
    private TaskLogic taskLogic;

    // @ApiOperation
    @RequestMapping("/")
    public List<WfTask> get() {
        User user = authenticationLogic.authenticate("Administrator", "wf");
        return taskLogic.getMyTasks(user, BatchPresentationFactory.TASKS.createNonPaged());
    }

}
