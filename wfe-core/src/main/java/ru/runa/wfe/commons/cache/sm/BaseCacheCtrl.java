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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import javax.transaction.Transaction;
import lombok.AllArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Base class for components receiving events on objects change and transaction complete. Components, receiving events must register self in
 * {@link CachingLogic}. All methods must be thread safe and may be called in many threads.
 *
 * @param <CacheImpl> Controlled cache implementation.
 */
public abstract class BaseCacheCtrl<CacheImpl extends CacheImplementation> {
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Cache lifetime control state machine.
     */
    protected final CacheStateMachine<CacheImpl> stateMachine;

    /**
     * Definitions for objects, which may invalidate cache.
     */
    private final List<ListenObjectDefinition> listenObjects;

    protected BaseCacheCtrl(SMCacheFactory<CacheImpl> factory, List<ListenObjectDefinition> listenObjects) {
        this.stateMachine = new CacheStateMachine<>(factory, CachingLogic.class);
        this.listenObjects = listenObjects;
        CachingLogic.registerChangeListener(this);
    }

    public final void onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        try {
            if (log.isDebugEnabled()) {
                for (ListenObjectDefinition def : listenObjects) {
                    if (def.listenClass.isAssignableFrom(changedObject.object.getClass())) {
                        def.logType.logChange(stateMachine, transaction, changedObject, log);
                        break;
                    }
                }
            }
            stateMachine.onChange(transaction, changedObject);
        } catch (Exception e) {
            log.error("onChange(transaction, changedObject) call failed on " + getClass().getName(), e);
        }
    }

    public final void onBeforeTransactionComplete(Transaction transaction) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(getCacheStateDescription(stateMachine, transaction) + " Preparing transaction " + transaction + " completition.");
            }
            stateMachine.onBeforeTransactionComplete(transaction);
        } catch (Exception e) {
            log.error("onBeforeTransactionComplete(transaction) call failed on " + getClass().getName(), e);
        }
    }

    public final void onAfterTransactionComplete(Transaction transaction) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(getCacheStateDescription(stateMachine, transaction) + " Transaction " + transaction + " is completed.");
            }
            stateMachine.onAfterTransactionComplete(transaction);
        } catch (Exception e) {
            log.error("onTransactionCompleted(transaction) call failed on " + getClass().getName(), e);
        }
    }

    public final void dropCache() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Dropping cache.");
            }
            stateMachine.dropCache();
        } catch (Exception e) {
            log.error("uninitialize() call failed on " + getClass().getName(), e);
        }
    }

    public final List<Class<?>> getListenObjectTypes() {
        return Lists.transform(listenObjects, new Function<ListenObjectDefinition, Class<?>>() {
            @Override
            public Class<?> apply(ListenObjectDefinition input) {
                return input.listenClass;
            }
        });
    }

    /**
     * Get string description for current cache state (cache is empty or current cache implementation description).
     *
     * @param transaction
     *            Current transaction.
     * @return Return string description for current cache state.
     */
    private static String getCacheStateDescription(CacheStateMachine<?> stateMachine, Transaction transaction) {
        Object cacheImpl = stateMachine.getCacheQuick(transaction);
        return cacheImpl == null ? "(cache is empty)" : "(cache is " + cacheImpl + ")";
    }

    @AllArgsConstructor
    protected static class ListenObjectDefinition {
        /**
         * Class, which change may lead to cache invalidation.
         */
        private final Class<?> listenClass;

        /**
         * Change event logging strategy.
         */
        private final ListenObjectLogType logType;
    }

    protected enum ListenObjectLogType {
        /**
         * Cache invalidation logging is not required (trace logging).
         */
        NONE {
            @Override
            public void logChange(CacheStateMachine<?> stateMachine, Transaction transaction, ChangedObjectParameter changedObject, Log log) {
                if (log.isTraceEnabled()) {
                    log.trace(getLogMessage(stateMachine, transaction, changedObject));
                }
            }
        },

        /**
         * Cache invalidation logging is required (debug logging).
         */
        ALL {
            @Override
            public void logChange(CacheStateMachine<?> stateMachine, Transaction transaction, ChangedObjectParameter changedObject, Log log) {
                log.debug(getLogMessage(stateMachine, transaction, changedObject));
            }
        },

        /**
         * Cache invalidation logging is debug for first change and trace for other.
         */
        BECOME_DIRTY {
            @Override
            public void logChange(CacheStateMachine<?> stateMachine, Transaction transaction, ChangedObjectParameter changedObject, Log log) {
                if (!stateMachine.isDirtyTransaction(transaction)) {
                    log.debug(getLogMessage(stateMachine, transaction, changedObject));
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace(getLogMessage(stateMachine, transaction, changedObject));
                    }
                }
            }
        };

        /**
         * Log cache invalidation event.
         *
         * @param stateMachine
         *            Cache lifetime control state machine.
         * @param transaction
         *            Transaction, which change object and invalidating cache.
         * @param changedObject
         *            Changed object.
         * @param log
         *            Object for logging.
         */
        public abstract void logChange(CacheStateMachine<?> stateMachine, Transaction transaction, ChangedObjectParameter changedObject, Log log);

        /**
         * Create message to log change. Cache lifetime control state machine.
         * 
         * @param transaction
         *            Transaction, which change object and invalidating cache.
         * @param changedObject
         *            Changed object.
         * @return Message to log change.
         */
        String getLogMessage(CacheStateMachine<?> stateMachine, Transaction transaction, ChangedObjectParameter changedObject) {
            String cacheState = BaseCacheCtrl.getCacheStateDescription(stateMachine, transaction);
            return cacheState + " On " + changedObject.changeType + " at transaction " + transaction + ": " + changedObject.object + ".";
        }
    }
}
