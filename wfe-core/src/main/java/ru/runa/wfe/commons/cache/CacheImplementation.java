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
     * Called to notify about object change (in rare cases, see usage).
     * 
     * @param changedObject
     *            Changed object
     * @return Return true, if cache is still may be used and false if cache must be dropped.
     */
    boolean onChange(ChangedObjectParameter changedObject);
}
