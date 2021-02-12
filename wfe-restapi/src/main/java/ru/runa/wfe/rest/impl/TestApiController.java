package ru.runa.wfe.rest.impl;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/test")
@Transactional
public class TestApiController {
    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Autowired
    private TaskLogic taskLogic;

    @ApiOperation(notes = "test method", value = "test method2")
    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public List<WfTask> getTasks() {
        User user = authenticationLogic.authenticate("Administrator", "wf");
        return taskLogic.getMyTasks(user, BatchPresentationFactory.TASKS.createNonPaged());
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String post() {
        return "string";
    }

}
