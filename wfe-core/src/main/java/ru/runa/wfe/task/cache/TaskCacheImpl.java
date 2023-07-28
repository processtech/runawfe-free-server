package ru.runa.wfe.task.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;

class TaskCacheImpl extends BaseCacheImpl {
    public static final String taskCacheName = "ru.runa.wfe.task.cache.taskLists";
    private final Cache<Long, ConcurrentHashMap<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>>> actorToTasksCache;

    public TaskCacheImpl() {
        actorToTasksCache = createCache(taskCacheName);
    }

    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation) {
        Map<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            return getVersionnedData(null);
        }
        return getVersionnedData(lists.get(new BatchPresentationFieldEquals(batchPresentation)));
    }

    public void setTasks(VersionedCacheData<List<WfTask>> oldCachedData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        if (!mayUpdateVersionnedData(oldCachedData)) {
            return;
        }
        ConcurrentHashMap<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            lists = new ConcurrentHashMap<>();
            actorToTasksCache.put(actorId, lists);
        }
        lists.put(new BatchPresentationFieldEquals(batchPresentation), tasks);
    }

    /**
     * Class need to compare BatchPresentation with strongEquals, instead of equals criteria. It necessary in task list cache which using
     * BatchPresentation as key.
     */
    private static class BatchPresentationFieldEquals implements Serializable {
        private static final long serialVersionUID = 1L;
        BatchPresentation batchPresentation;

        BatchPresentationFieldEquals(BatchPresentation batchPresentation) {
            this.batchPresentation = batchPresentation.clone();
        }

        @Override
        public boolean equals(Object obj) {
            // TODO "equals" method breaks contract
            if (obj instanceof TaskCacheImpl.BatchPresentationFieldEquals) {
                return batchPresentation.fieldEquals(((TaskCacheImpl.BatchPresentationFieldEquals) obj).batchPresentation);
            } else if (obj instanceof BatchPresentation) {
                return batchPresentation.fieldEquals((BatchPresentation) obj);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return batchPresentation.hashCode();
        }
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
