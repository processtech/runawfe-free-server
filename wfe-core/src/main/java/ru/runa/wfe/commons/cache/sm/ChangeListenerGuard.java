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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Safety guard for {@linkplain ChangeListener}. Catches all exceptions during call and log it.
 * 
 * @author Konstantinov Aleksey
 */
public class ChangeListenerGuard implements ChangeListener {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(ChangeListenerGuard.class);

    /**
     * {@linkplain ChangeListener}, used to delegate calls.
     */
    private final ChangeListener delegated;

    /**
     * Create guard for specified {@linkplain ChangeListener}.
     * 
     * @param delegated
     *            {@linkplain ChangeListener}, which must be guarded.
     */
    public ChangeListenerGuard(ChangeListener delegated) {
        super();
        this.delegated = delegated;
    }

    @Override
    public void beforeTransactionComplete(Transaction transaction) {
        try {
            delegated.beforeTransactionComplete(transaction);
        } catch (Exception e) {
            log.error("markTransactionComplete(transaction) call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void onTransactionCompleted(Transaction transaction) {
        try {
            delegated.onTransactionCompleted(transaction);
        } catch (Exception e) {
            log.error("markTransactionComplete(transaction) call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        try {
            delegated.onChange(transaction, changedObject);
        } catch (Exception e) {
            log.error("onChange(transaction, changedObject) call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void uninitialize(Object object, Change change) {
        try {
            delegated.uninitialize(object, change);
        } catch (Exception e) {
            log.error("uninitialize() call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public List<Class<?>> getListenObjectTypes() {
        return delegated.getListenObjectTypes();
    }
}
