package ru.runa.wfe.commons.cache;

/**
 * Interface for data from cache, which may be updated later, if cache version is not changed. Data may be read from cache and if it's absent, when
 * after data calculation it may be stored in cache, if version is not changed (no changing transaction while data calculation was performed).
 * 
 * @param <TData>
 *            Type of data, stored in cache
 */
public interface VersionedCacheData<TData> {
    /**
     * Returns data, stored in cache. May be null, if no data in cache found.
     * 
     * @return Returns data, stored in cache.
     */
    TData getData();

    /**
     * Cache version, which contains data.
     * 
     * @return Returns cache version.
     */
    int getVersion();
}
