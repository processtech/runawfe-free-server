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
package ru.runa.wfe.commons.cache.sm;

import java.util.List;
import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Interface for components, receiving events on objects change and transaction complete. Components, receiving events must register self in
 * {@link CachingLogic}.
 *
 * All methods must be thread safe and may be called in many threads.
 */
public interface ChangeListener {

    /**
     * Called, when changed one of listened object type.
     *
     * @param transaction
     *            Transaction, changed object belongs to.
     * @param changedObject
     *            Changed object data.
     * @return has been changed
     */
    public boolean onChange(Transaction transaction, ChangedObjectParameter changedObject);

    /**
     * Called, before transaction in current thread will be completed (commit or rollback). This method called only if this listener was notified
     * about changes in completed transaction. If no object changed in transaction, then method wasn't called. Cache controller must mark transaction
     * as completed, but must not recreate cache.
     *
     * @param transaction
     *            Commit or rollback transaction.
     */
    public void beforeTransactionComplete(Transaction transaction);

    /**
     * Called, then transaction in current thread is completed. This method called only if this listener was notified about changes in completed
     * transaction. If no object changed in transaction, then method wasn't called.
     *
     * @param transaction
     *            Commit or rollback transaction.
     */
    public void onTransactionCompleted(Transaction transaction);

    /**
     * Drops current cache implementation.
     *
     * @param object
     *            Changed object, which leads to cache drop.
     * @param change
     *            Change type.
     */
    public void uninitialize(Object object, Change change);

    /**
     * Object types, which change may cause cache invalidation. Cache will be notified about changes only for this objects.
     *
     * @return Return object types, which change may cause cache invalidation.
     */
    public List<Class<?>> getListenObjectTypes();
}
