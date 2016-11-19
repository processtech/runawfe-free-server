package ru.runa.wfe.task.dto;

import java.util.List;

import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

public interface IWfTaskFactory {

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude);

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude, boolean firstOpen);

}
