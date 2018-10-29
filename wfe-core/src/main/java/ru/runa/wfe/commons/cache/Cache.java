package ru.runa.wfe.commons.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Konstantinov Aleksey
 */
public abstract class Cache<K extends Serializable, V extends Serializable> {

    /**
     * Commit cached elements. If ehcache {@linkplain Cache} is found, when it clears and all 
     * currently cached values put into {@linkplain Cache}.
     */
    public abstract void commitCache();

    /**
     * Try to get element from cache.
     *
     * @param key Key to load cached object.
     * @return Cached element or null, if not found.
     */
    public abstract V get(K key);

    /**
     * Check if element is present into cache.
     *
     * @param key Key to check cached object.
     * @return true, if object with specified key cached and false otherwise.
     */
    public abstract boolean contains(K key);

    /**
     * Puts value to cache.
     *
     * @param key Cached object key.
     * @param value Cached object.
     */
    public abstract void put(K key, V value);

    /**
     * Add all elements from collection to cache.
     *
     * @param collection Collection of objects to add.
     */
    public abstract void putAll(Map<K, V> collection);

    /**
     * Removes cached object with specified key.
     *
     * @param key Cached object key.
     * @return true, if element removed from cache and false if it was not found in the cache.
     */
    public abstract boolean remove(K key);

    /**
     * Clear cache by removing all cached data.
     */
    public abstract void clear();

    /**
     * Returns {@linkplain Iterable} to iterate other cached objects keys.
     */
    public abstract Iterable<K> keySet();

    public V getAndRemove(K key) {
        V value = get(key);
        if (value != null) {
            // Small optimization.
            remove(key);
        }
        return value;
    }
}
