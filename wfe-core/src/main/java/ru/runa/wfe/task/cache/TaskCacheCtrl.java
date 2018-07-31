package ru.runa.wfe.task.cache;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.cache.SubstitutionCacheStateImpl;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.var.Variable;

class TaskCacheCtrl extends BaseCacheCtrl<ManageableTaskCache> implements TaskCache {

    TaskCacheCtrl() {
        super(new TaskCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation) {
        ManageableTaskCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache != null) {
            return cache.getTasks(actorId, batchPresentation);
        }
        return null;
    }

    @Override
    public void setTasks(VersionedCacheData<List<WfTask>> oldCacheData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        ManageableTaskCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache != null) {
            cache.setTasks(oldCacheData, actorId, batchPresentation, tasks);
        }
    }

    private static final List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<>();
        result.add(new ListenObjectDefinition(Task.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Swimlane.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Variable.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Substitution.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(SubstitutionCriteria.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(ExecutorGroupMembership.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Executor.class, ListenObjectLogType.BECOME_DIRTY));
        // Must be invalidated in case of non runtime substitution cache update.
        result.add(new ListenObjectDefinition(SubstitutionCacheStateImpl.class, ListenObjectLogType.BECOME_DIRTY));
        return result;
    }

    private static class TaskCacheFactory implements StaticCacheFactory<ManageableTaskCache> {

        @Override
        public ManageableTaskCache buildCache() {
            return new TaskCacheImpl();
        }
    }
}
