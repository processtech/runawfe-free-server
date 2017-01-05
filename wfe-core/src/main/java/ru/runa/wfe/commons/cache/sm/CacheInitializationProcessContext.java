package ru.runa.wfe.commons.cache.sm;

/**
 * Interface for interaction with cache initialization process.
 */
public interface CacheInitializationProcessContext {

    /**
     * Check if initialization is still required. It's recommended to check this flag to stop initialization process as soon as possible. System may
     * decide to stop initialization if cache become invalid (other transaction change persistent object).
     *
     * @return Return true, if cache initialization is required and false, if cache initialization may be stopped.
     */
    boolean isInitializationStillRequired();
}