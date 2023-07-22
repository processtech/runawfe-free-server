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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.factories.LazyInitializedCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.NonRuntimeCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;

/**
 * Base implementation of cache control objects.
 *
 * @author Konstantinov Aleksey
 * @param <CacheImpl>
 *            Controlled cache implementation.
 */
public abstract class BaseCacheCtrl<CacheImpl extends CacheImplementation, StateContext> implements ChangeListener {
    /**
     * Logging support.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Cache lifetime control state machine.
     */
    protected final CacheStateMachine<CacheImpl, StateContext> stateMachine;

    /**
     * Definitions for objects, which may invalidate cache.
     */
    private final List<ListenObjectDefinition> listenObjects;

    public BaseCacheCtrl(LazyInitializedCacheFactory<CacheImpl> factory, List<ListenObjectDefinition> listenObjects) {
        this.stateMachine = (CacheStateMachine<CacheImpl, StateContext>) CacheStateMachine.createStateMachine(factory, CachingLogic.class);
        this.listenObjects = listenObjects;
    }

    public BaseCacheCtrl(StaticCacheFactory<CacheImpl> factory, List<ListenObjectDefinition> listenObjects) {
        this.stateMachine = (CacheStateMachine<CacheImpl, StateContext>) CacheStateMachine.createStateMachine(factory, CachingLogic.class);
        this.listenObjects = listenObjects;
    }

    public BaseCacheCtrl(NonRuntimeCacheFactory<CacheImpl> factory, List<ListenObjectDefinition> listenObjects) {
        this.stateMachine = (CacheStateMachine<CacheImpl, StateContext>) CacheStateMachine.createStateMachine(factory, CachingLogic.class);
        this.listenObjects = listenObjects;
    }

    @Override
    public boolean onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        if (log.isTraceEnabled()) {
            String cacheState = BaseCacheCtrl.getCacheStateDescription(stateMachine, transaction);
            String message = cacheState + " On " + changedObject.changeType + " at transaction " + transaction + ": " + changedObject.object + ".";
            log.trace(message);
        }
        stateMachine.onChange(transaction, changedObject);
        return true;
    }

    @Override
    public void beforeTransactionComplete(Transaction transaction) {
        if (log.isTraceEnabled()) {
            log.trace(getCacheStateDescription(stateMachine, transaction) + " Preparing transaction " + transaction + " completion.");
        }
        stateMachine.beforeTransactionComplete(transaction);
    }

    @Override
    public void onTransactionCompleted(Transaction transaction) {
        if (log.isTraceEnabled()) {
            log.trace(getCacheStateDescription(stateMachine, transaction) + " Transaction " + transaction + " is completed.");
        }
        stateMachine.onTransactionCompleted(transaction);
    }

    @Override
    public void uninitialize(Object object, Change change) {
        if (log.isTraceEnabled()) {
            log.trace("Cache is uninitialized due to " + change + " of " + object);
        }
        stateMachine.dropCache();
    }

    @Override
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
    public static String getCacheStateDescription(CacheStateMachine<?, ?> stateMachine, Transaction transaction) {
        Object cacheImpl = stateMachine.getCacheQuick(transaction);
        return cacheImpl == null ? "(cache is empty)" : "(cache is " + cacheImpl + ")";
    }

    protected static class ListenObjectDefinition {
        /**
         * Class, which change may lead to cache invalidation.
         */
        private final Class<?> listenClass;

        public ListenObjectDefinition(Class<?> listenClass) {
            this.listenClass = listenClass;
        }
    }

}
