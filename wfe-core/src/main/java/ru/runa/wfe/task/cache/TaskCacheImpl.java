/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.task.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;

class TaskCacheImpl extends BaseCacheImpl implements ManageableTaskCache {
    public static final String taskCacheName = "ru.runa.wfe.task.cache.taskLists";
    private final Cache<Long, ConcurrentHashMap<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>>> actorToTasksCache;

    public TaskCacheImpl() {
        actorToTasksCache = createCache(taskCacheName);
    }

    @Override
    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation) {
        Map<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            return getVersionnedData(null);
        }
        return getVersionnedData(lists.get(new BatchPresentationFieldEquals(batchPresentation)));
    }

    @Override
    public void setTasks(VersionedCacheData<List<WfTask>> oldCachedData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        if (!mayUpdateVersionnedData(oldCachedData)) {
            return;
        }
        ConcurrentHashMap<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            lists = new ConcurrentHashMap<TaskCacheImpl.BatchPresentationFieldEquals, List<WfTask>>();
            actorToTasksCache.put(actorId, lists);
        }
        lists.put(new BatchPresentationFieldEquals(batchPresentation), tasks);
    }

    public void clearActorTasks(Long actorId) {
        actorToTasksCache.remove(actorId);
        commitCache();
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
    public CacheImplementation unlock() {
        return null;
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
