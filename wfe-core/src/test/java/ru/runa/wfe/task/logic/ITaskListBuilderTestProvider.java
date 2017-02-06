package ru.runa.wfe.task.logic;

import java.util.Set;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;

public interface ITaskListBuilderTestProvider {

    public WfTask getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation, Set<Executor> executorsToGetTasksByMembership);

    public boolean isTaskAcceptableBySubstitutionRules(ExecutionContext executionContext, Task task, Actor assignedActor, Actor substitutorActor);

    public int checkSubstitutionRules(SubstitutionCriteria criteria, Set<Long> ids, ExecutionContext executionContext, Task task,
            Actor assignedActor, Actor substitutorActor);

    public boolean isActorInInactiveEscalationGroup(Actor actor, EscalationGroup group);

}
