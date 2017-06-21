package ru.runa.wfe.task.logic;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;

public interface IObservableTaskListBuilder {

    public List<WfTask> getObservableTasks(Actor actor, BatchPresentation batchPresentation);

}
