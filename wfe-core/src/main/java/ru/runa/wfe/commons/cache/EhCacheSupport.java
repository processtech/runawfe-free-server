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
package ru.runa.wfe.commons.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.lang.SerializationUtils;

/**
 * Cache component, which support ehcache or local cache storage.
 * 
 * @author Konstantinov Aleksey
 */
class EhCacheSupport<K extends Serializable, V extends Serializable> implements ru.runa.wfe.commons.cache.Cache<K, V> {

    /**
     * Local storage. Used if ehcache is unavailable or cache is not committed.
     */
    private final ConcurrentHashMap<K, V> localStorage = new ConcurrentHashMap<K, V>();

    /**
     * Ehcache {@linkplain Cache} name, used to store cached values.
     */
    private final String ehcacheName;

    /**
     * Flag equals true, if element lifetime must be infinite; false to use ehcache settings.
     */
    private final boolean infiniteLifeTime;

    /**
     * {@linkplain Cache}, used to store cached values.
     */
    private volatile Cache ehcache;

    /**
     * Creates caching component.
     * 
     * @param ehcacheName
     *            Ehcache {@linkplain Cache} name, used to store cached values.
     * @param infiniteLifeTime
     *            Flag equals true, if element lifetime must be infinite; false to use ehcache settings.
     */
    public EhCacheSupport(String ehcacheName, boolean infiniteLifeTime) {
        super();
        this.ehcacheName = ehcacheName;
        this.infiniteLifeTime = infiniteLifeTime;
    }

    /**
     * Creates caching component.
     * 
     * @param ehcacheName
     *            Ehcache {@linkplain Cache} name, used to store cached values.
     */
    public EhCacheSupport(String ehcacheName) {
        super();
        this.ehcacheName = ehcacheName;
        this.infiniteLifeTime = false;
    }

    /**
     * Commit cached elements. If ehcache {@linkplain Cache} is found, when it clears and all currently cached values put into {@linkplain Cache}.
     */
    @Override
    public void commitCache() {
        CacheManager manager = EhcacheHelper.getCacheManager();
        if (manager == null) {
            return;
        }
        ehcache = manager.getCache(ehcacheName);
        if (ehcache == null) {
            return;
        }
        CacheConfiguration cacheConfiguration = ehcache.getCacheConfiguration();
        if (infiniteLifeTime && (!cacheConfiguration.isEternal() || cacheConfiguration.getMaxElementsInMemory() < 100000)) {
            cacheConfiguration.setEternal(true);
            cacheConfiguration.setTimeToIdleSeconds(0);
            cacheConfiguration.setTimeToLiveSeconds(0);
            cacheConfiguration.setMaxElementsInMemory(100000);
        }
        ehcache.removeAll();
        for (Map.Entry<K, V> cachedValue : localStorage.entrySet()) {
            ehcache.put(createElement(cachedValue.getKey(), cachedValue.getValue()));
        }
    }

    /**
     * Try to get element from cache.
     * 
     * @param key
     *            Key to load cached object.
     * @return Cached element or null, if not found.
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        V value = getImpl(key);
        return value == null ? null : (V) SerializationUtils.clone(value);
    }

    /**
     * Try to get element from cache.
     * 
     * @param key
     *            Key to load cached object.
     * @return Cached element or null, if not found.
     */
    private V getImpl(K key) {
        Cache cache = ehcache;
        if (cache == null) {
            return localStorage.get(key);
        }
        Element cached = cache.get(key);
        return cached == null ? null : (V) cached.getValue();
    }

    /**
     * Check, if element is present into cache.
     * 
     * @param key
     *            Key to check cached object.
     * @return true, if object with specified key cached and false otherwise.
     */
    @Override
    public boolean contains(K key) {
        Cache cache = ehcache;
        if (cache == null) {
            return localStorage.contains(key);
        }
        return cache.isKeyInCache(key);
    }

    /**
     * Puts value to cache.
     * 
     * @param key
     *            Cached object key.
     * @param value
     *            Cached object.
     */
    @Override
    public void put(K key, V value) {
        Cache cache = ehcache;
        if (cache == null) {
            localStorage.put(key, value);
            return;
        }
        cache.put(createElement(key, value));
    }

    /**
     * Add all elements from collection to cache.
     * 
     * @param collection
     *            Collection of objects to add.
     */
    @Override
    public void putAll(Map<K, V> collection) {
        Cache cache = ehcache;
        if (cache == null) {
            localStorage.putAll(collection);
            return;
        }
        for (Map.Entry<K, V> newValue : collection.entrySet()) {
            cache.put(createElement(newValue.getKey(), newValue.getValue()));
        }
    }

    /**
     * Removes cached object with specified key.
     * 
     * @param key
     *            Cached object key.
     * @return true, if element removed from cache and false if it was not found in the cache.
     */
    @Override
    public boolean remove(K key) {
        Cache cache = ehcache;
        if (cache == null) {
            return localStorage.remove(key) != null;
        }
        return cache.remove(key);
    }

    /**
     * Clear cache by removing all cached data.
     */
    @Override
    public void clear() {
        Cache cache = ehcache;
        if (cache == null) {
            localStorage.clear();
            return;
        }
        cache.removeAll();
    }

    /**
     * Returns {@linkplain Iterable} to iterate other cached objects keys.
     * 
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterable<K> keySet() {
        Cache cache = ehcache;
        if (cache == null) {
            return localStorage.keySet();
        }
        return cache.getKeys();
    }

    /**
     * Creates {@linkplain Element} to put into {@linkplain CacheControl}.
     * 
     * @param key
     *            Cached object key.
     * @param value
     *            Cached object.
     * @return {@linkplain Element} to put into {@linkplain CacheControl}.
     */
    private Element createElement(K key, V value) {
        if (value != null) {
            value = (V) SerializationUtils.clone(value);
        }
        return infiniteLifeTime ? new Element(key, value, true, 0, 0) : new Element(key, value);
    }
}
