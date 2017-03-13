package ru.runa.wfe.commons.cache.sm;

public class CacheInitializationProcessContextStub implements CacheInitializationProcessContext {

    @Override
    public boolean isInitializationStillRequired() {
        return true;
    }
}
