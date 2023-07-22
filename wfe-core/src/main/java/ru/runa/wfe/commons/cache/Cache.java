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

/**
 * @author Konstantinov Aleksey
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K extends Serializable, V extends Serializable> {

    /**
     * Commit cached elements. If ehcache {@linkplain Cache} is found, when it clears and all 
     * currently cached values put into {@linkplain Cache}.
     */
    void commitCache();

    /**
     * Try to get element from cache.
     * @param key Key to load cached object.
     * @return Cached element or null, if not found.
     */
    V get(K key);

    /**
     * Check, if element is present into cache.
     * @param key Key to check cached object.
     * @return true, if object with specified key cached and false otherwise.
     */
    boolean contains(K key);

    /**
     * Puts value to cache.
     * @param key Cached object key.
     * @param value Cached object.
     */
    void put(K key, V value);

    /**
     * Add all elements from collection to cache.
     * @param collection Collection of objects to add.
     */
    void putAll(Map<K, V> collection);

    /**
     * Removes cached object with specified key.
     * @param key Cached object key.
     * @return true, if element removed from cache and false if it was not found in the cache.
     */
    boolean remove(K key);

    /**
     * Clear cache by removing all cached data.
     */
    void clear();

    /**
     * Returns {@linkplain Iterable} to iterate other cached objects keys.
     * @return
     */
    Iterable<K> keySet();
}
