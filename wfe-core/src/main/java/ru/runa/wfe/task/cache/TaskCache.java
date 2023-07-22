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

import java.util.List;

import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;

/**
 * Interface for task cache components.
 */
public interface TaskCache {
    /**
     * Returns tasks for {@link Actor} with specified id, according to {@link BatchPresentation}. May return null, if tasks wasn't set by
     * {@link #setTasks(long, BatchPresentation, WfTask[])} call.
     * 
     * @param actorId
     *            {@link Actor} identity, which tasks will be returned.
     * @param batchPresentation
     *            {@link BatchPresentation} to filter/sort tasks.
     * @return Tasks for {@link Actor} with specified id, according to {@link BatchPresentation}.
     */
    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation);

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
    public void setTasks(VersionedCacheData<List<WfTask>> oldCacheData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks);

}
