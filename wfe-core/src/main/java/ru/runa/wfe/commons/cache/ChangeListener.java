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

import javax.transaction.Transaction;

/**
 * Interface for components, receiving events on objects change and transaction complete. All methods from interface is called under
 * {@link CachingLogic} class synchronization. Components, receiving events must implement one or more sub interface of {@link ChangeListener} and
 * register self in {@link CachingLogic}.
 */
public interface ChangeListener {

    /**
     * Called, then changed one of predefined object (e. q. specific sub interface exists).
     * 
     * @param transaction
     *            Transaction, changed object belongs to.
     * @param changedObject
     *            Changed object data.
     */
    void onChange(Transaction transaction, ChangedObjectParameter changedObject);

    /**
     * Called, then transaction in current thread is completed. Cache controller must mark transaction as completed, but must not recreate cache.
     * <p/>
     * Cache recreation may be done in {@link #onTransactionComplete()}, then all caches is marked transaction.
     * <p/>
     * {@link CachingLogic} guarantees, what all caches receive {@link #markTransactionComplete()}, and only after what all caches receive
     * {@link #onTransactionComplete()}.
     * 
     * @param transaction
     *            Commit or rollback transaction.
     */
    void markTransactionComplete(Transaction transaction);

    /**
     * Drops current cache implementation.
     * 
     * @param object
     *            Changed object, which leads to cache drop.
     */
    void uninitialize(Object object, Change change);
}
