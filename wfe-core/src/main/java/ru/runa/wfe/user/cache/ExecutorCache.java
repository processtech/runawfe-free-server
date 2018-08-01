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
package ru.runa.wfe.user.cache;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/**
 * Interface for executor cache components.
 */
public interface ExecutorCache {

    /**
     * Return {@link Actor} with specified code, or null, if such actor not exists or cache is not valid.
     * 
     * @param code
     *            Actor code.
     * @return {@link Actor} with specified code.
     */
    Actor getActor(Long code);

    /**
     * Return {@link Executor} with specified name, or null, if such executor not exists or cache is not valid.
     * 
     * @param name
     *            Executor name.
     * @return {@link Executor} with specified name.
     */
    Executor getExecutor(String name);

    /**
     * Return {@link Executor} with specified id, or null, if such executor not exists or cache is not valid.
     * 
     * @param id
     *            Executor identity.
     * @return {@link Executor} with specified identity.
     */
    Executor getExecutor(Long id);

    /**
     * Return first level {@link Group} members. Only {@link Executor}, which directly contains in {@link Group} is returning. No recursive group
     * search performs. May return null, if cache is not valid.
     * 
     * @param group
     *            {@link Group}, which members will be returned.
     * @return First level {@link Group} members.
     */
    Set<Executor> getGroupMembers(Group group);

    /**
     * Return all {@link Actor} members of specified {@link Group} and all her subgroups. {@link Actor} members searching recursive and all actors
     * from subgroups is also contains in result set. May return null, if cache is not valid.
     * 
     * @param group
     *            {@link Group}, which actor members will be returned.
     * @return All {@link Actor} members of specified {@link Group} and all her subgroups.
     */
    Set<Actor> getGroupActorsAll(Group group);

    /**
     * Return all {@link Group}, which contains specified {@link Executor} as first level member. May return null, if cache is not valid.
     * 
     * @param executor
     *            {@link Executor}, which parents will be returned.
     * @return All {@link Group}, which contains specified {@link Executor} as first level member.
     */
    Set<Group> getExecutorParents(Executor executor);

    /**
     * Return all {@link Group}, which contains specified {@link Executor} as member (direct or recursive by subgroups). May return null, if cache is
     * not valid.
     * 
     * @param executor
     *            {@link Executor}, which parents will be returned.
     * @return All {@link Group}, which contains specified {@link Executor} as member.
     */
    Set<Group> getExecutorParentsAll(Executor executor);

    /**
     * Return all {@link Executor} of specified class according to {@link BatchPresentation}. May return null, if executor list for specified class
     * and presentation wasn't set yet (with addAllExecutor()). May return null, if cache is not valid.
     * 
     * @param <T>
     *            Type of returned objects. Must be {@link Executor} or it subclass.
     * @param clazz
     *            Type of returned objects. Must be {@link Executor} or it subclass.
     * @param batch
     *            {@link BatchPresentation} to sort/filter result.
     * @return All {@link Executor} of specified class according to {@link BatchPresentation}.
     */
    <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch);

    /**
     * Set {@link Executor} list for specified class and {@link BatchPresentation}.
     * 
     * @param oldCached
     *            Old state for caching data.
     * @param clazz
     *            Type of executors. Must be {@link Executor} or it subclass.
     * @param batch
     *            Presentation for executors.
     * @param executors
     *            Executor list for specified class and presentation. Will be returned on next {@link #getAllExecutor(Class, BatchPresentation)} call
     *            with specified class and presentation.
     */
    <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCached, Class<?> clazz, BatchPresentation batch, List<T> executors);
}
