package ru.runa.wfe.task.logic;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public interface ITaskListBuilder {

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation);

}
