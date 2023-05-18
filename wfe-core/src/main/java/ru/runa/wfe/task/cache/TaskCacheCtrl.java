package ru.runa.wfe.task.cache;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.cache.SubstitutionCacheImpl;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.var.CurrentVariable;

@Component("taskCache")
public class TaskCacheCtrl extends BaseCacheCtrl<TaskCacheImpl> {

    public TaskCacheCtrl() {
        super(
                new TaskCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(Task.class));
                    add(new ListenObjectDefinition(CurrentSwimlane.class));
                    add(new ListenObjectDefinition(CurrentVariable.class));
                    add(new ListenObjectDefinition(Substitution.class));
                    add(new ListenObjectDefinition(SubstitutionCriteria.class));
                    add(new ListenObjectDefinition(ExecutorGroupMembership.class));
                    add(new ListenObjectDefinition(Executor.class));
                    add(new ListenObjectDefinition(ProcessDefinitionPack.class));
                    add(new ListenObjectDefinition(ProcessDefinition.class));
                    // Must be invalidated in case of non runtime substitution cache update.
                    add(new ListenObjectDefinition(SubstitutionCacheImpl.class));
                }}
        );
    }

    /**
     * Returns tasks for {@link Actor} with specified id, according to {@link BatchPresentation}. May return null, if tasks wasn't set by
     * {@link #setTasks(VersionedCacheData, Long, BatchPresentation, List)} call.
     *
     * @param actorId
     *            {@link Actor} identity, which tasks will be returned.
     * @param batchPresentation
     *            {@link BatchPresentation} to filter/sort tasks.
     * @return Tasks for {@link Actor} with specified id, according to {@link BatchPresentation}.
     */
    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation) {
        TaskCacheImpl cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache != null) {
            return cache.getTasks(actorId, batchPresentation);
        }
        return null;
    }

    /**
     * Set tasks for {@link Actor} with specified id, and specified {@link BatchPresentation}. Next call to {@link #getTasks(Long, BatchPresentation)}
     * with same parameters will return this tasks.
     *
     * @param oldCacheData
     *            Old cached state for data.
     * @param actorId
     *            {@link Actor} identity, which owns tasks list.
     * @param batchPresentation
     *            {@link BatchPresentation} to filter/sort tasks.
     * @param tasks
     *            {@link Actor} tasks.
     */
    public void setTasks(VersionedCacheData<List<WfTask>> oldCacheData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        TaskCacheImpl cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache != null) {
            cache.setTasks(oldCacheData, actorId, batchPresentation, tasks);
        }
    }

    private static class TaskCacheFactory extends SMCacheFactory<TaskCacheImpl> {

        TaskCacheFactory() {
            super(Type.EAGER, null);
        }

        @Override
        protected TaskCacheImpl createCacheImpl(CacheInitializationProcessContext context) {
            return new TaskCacheImpl();
        }
    }
}
