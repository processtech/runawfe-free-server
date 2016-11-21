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

/**
 * Interface for all caches implementations.
 * 
 * @author Konstantinov Aleksey
 */
public interface CacheImplementation {
    /**
     * Commit current cache as default. After committing cache will be used from many threads to check cached values.
     */
    public void commitCache();

    /**
     * All dirty transaction is completed. Cache instance may decide to create new cache for future use. Do not return current cache instance - create
     * new.
     * 
     * @return Return cache implementation to be used in future or null, if cache must be dropped.
     */
    public CacheImplementation unlock();

    /**
     * Called to notify about object change.
     * 
     * @param changedObject
     *            Changed object
     * @return Return true, if cache is still may be used and false if cache must be dropped.
     */
    public boolean onChange(ChangedObjectParameter changedObject);
}
