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
import lombok.val;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * Cache component, which support ehcache or local cache storage.
 * 
 * @author Konstantinov Aleksey
 */
class EhCacheSupport<K extends Serializable, V extends Serializable> extends ru.runa.wfe.commons.cache.Cache<K, V> {

    /**
     * Local storage. Used if ehcache is unavailable or cache is not committed.
     */
    private final ConcurrentHashMap<K, V> localStorage = new ConcurrentHashMap<>();

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
        this.ehcacheName = ehcacheName;
        this.infiniteLifeTime = infiniteLifeTime;
    }

    /**
     * Commit cached elements. If ehcache {@linkplain Cache} is found, when it clears and all locally-cached values put into {@linkplain Cache}.
     */
    @Override
    public void commitCache() {
        val manager = EhcacheHelper.getCacheManager();
        if (manager == null) {
            return;
        }
        ehcache = manager.getCache(ehcacheName);
        if (ehcache == null) {
            return;
        }
        val cacheConfiguration = ehcache.getCacheConfiguration();
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
        // TODO Should I clean localStorage here?
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
        if (ehcache == null) {
            return localStorage.get(key);
        }
        Element e = ehcache.get(key);
        return e == null ? null : (V) e.getValue();
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
        return ehcache == null
                ? localStorage.containsKey(key)
                : ehcache.isKeyInCache(key);
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
        if (ehcache == null) {
            localStorage.put(key, value);
        } else {
            ehcache.put(createElement(key, value));
        }
    }

    /**
     * Add all elements from collection to cache.
     * 
     * @param collection
     *            Collection of objects to add.
     */
    @Override
    public void putAll(Map<K, V> collection) {
        if (ehcache == null) {
            localStorage.putAll(collection);
            return;
        }
        for (Map.Entry<K, V> kv : collection.entrySet()) {
            ehcache.put(createElement(kv.getKey(), kv.getValue()));
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
        return ehcache == null
                ? localStorage.remove(key) != null
                : ehcache.remove(key);
    }

    /**
     * Clear cache by removing all cached data.
     */
    @Override
    public void clear() {
        if (ehcache == null) {
            localStorage.clear();
            return;
        }
        ehcache.removeAll();
    }

    /**
     * Returns {@linkplain Iterable} to iterate other cached objects keys.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterable<K> keySet() {
        return ehcache == null
                ? localStorage.keySet()
                : ehcache.getKeys();
    }

    /**
     * Creates {@linkplain Element} to put into EhCache.
     * 
     * @param key
     *            Cached object key.
     * @param value
     *            Cached object.
     * @return {@linkplain Element} to put into EhCache.
     */
    private Element createElement(K key, V value) {
        return infiniteLifeTime ? new Element(key, value, true, 0, 0) : new Element(key, value);
    }
}
