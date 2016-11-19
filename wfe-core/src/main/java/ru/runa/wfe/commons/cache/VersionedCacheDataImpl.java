package ru.runa.wfe.commons.cache;

/**
 * Internal implementation of VersionnedCacheData<TData> for versionned cache data.
 * 
 * @param <TData>
 *            Type of data, stored in cache.
 */
class VersionedCacheDataImpl<TData> implements VersionedCacheData<TData> {
    /**
     * Data, stored in cache or null if no data present.
     */
    private final TData data;

    /**
     * Cache version at data request moment.
     */
    private final int version;

    public VersionedCacheDataImpl(TData data, int version) {
        super();
        this.data = data;
        this.version = version;
    }

    @Override
    public TData getData() {
        return data;

    }

    @Override
    public int getVersion() {
        return version;
    }
}
