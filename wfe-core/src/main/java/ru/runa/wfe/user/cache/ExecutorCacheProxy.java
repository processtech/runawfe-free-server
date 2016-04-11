package ru.runa.wfe.user.cache;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

class ExecutorCacheProxy implements ManageableExecutorCache {

    @Override
    public Actor getActor(Long code) {
        return null;
    }

    @Override
    public Executor getExecutor(String name) {
        return null;
    }

    @Override
    public Executor getExecutor(Long id) {
        return null;
    }

    @Override
    public Set<Executor> getGroupMembers(Group group) {
        return null;
    }

    @Override
    public Set<Actor> getGroupActorsAll(Group group) {
        return null;
    }

    @Override
    public Set<Group> getExecutorParents(Executor executor) {
        return null;
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor) {
        return null;
    }

    @Override
    public <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch) {
        return null;
    }

    @Override
    public <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCached, Class<?> clazz, BatchPresentation batch, List<T> executors) {
    }

    @Override
    public void commitCache() {
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
