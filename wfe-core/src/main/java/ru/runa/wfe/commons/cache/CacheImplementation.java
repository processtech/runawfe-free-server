package ru.runa.wfe.commons.cache;

/**
 * Interface for all caches implementations.
 * 
 * @author Konstantinov Aleksey
 */
public interface CacheImplementation {
    /**
     * Commit current cache as default. After committing cache will be used from many threads to check cached values.
     */
    void commitCache();

    /**
     * All dirty transaction is completed. Cache instance may decide to create new cache for future use. Do not return current cache instance - create
     * new.
     * 
     * @return Return cache implementation to be used in future or null, if cache must be dropped.
     */
    CacheImplementation unlock();

    /**
     * Called to notify about object change.
     * 
     * @param changedObject
     *            Changed object
     * @return Return true, if cache is still may be used and false if cache must be dropped.
     */
    boolean onChange(ChangedObjectParameter changedObject);
}
