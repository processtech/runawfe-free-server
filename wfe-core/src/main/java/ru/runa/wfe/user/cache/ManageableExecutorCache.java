package ru.runa.wfe.user.cache;

import java.util.List;
import java.util.Set;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

interface ManageableExecutorCache extends CacheImplementation {

    Actor getActor(Long code);

    Executor getExecutor(String name);

    Executor getExecutor(Long id);

    Set<Executor> getGroupMembers(Group group);

    Set<Actor> getGroupActorsAll(Group group);

    Set<Group> getExecutorParents(Executor executor);

    Set<Group> getExecutorParentsAll(Executor executor);

    <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch);

    <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCached, Class<?> clazz, BatchPresentation batch, List<T> executors);
}
