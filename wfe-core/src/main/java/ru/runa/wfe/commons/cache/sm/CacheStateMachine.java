package ru.runa.wfe.commons.cache.sm;

import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.factories.LazyInitializedCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.NonRuntimeCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;
import ru.runa.wfe.commons.cache.states.DefaultStateContext;
import ru.runa.wfe.commons.cache.states.IsolatedCacheStateFactory;
import ru.runa.wfe.commons.cache.states.StateCommandResult;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithCache;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithData;
import ru.runa.wfe.commons.cache.states.audit.BeforeTransactionCompleteAudit;
import ru.runa.wfe.commons.cache.states.audit.CacheStateMachineAudit;
import ru.runa.wfe.commons.cache.states.audit.CommitCacheAudit;
import ru.runa.wfe.commons.cache.states.audit.CompleteTransactionAudit;
import ru.runa.wfe.commons.cache.states.audit.DefaultCacheStateMachineAudit;
import ru.runa.wfe.commons.cache.states.audit.GetCacheAudit;
import ru.runa.wfe.commons.cache.states.audit.InitializationErrorAudit;
import ru.runa.wfe.commons.cache.states.audit.OnChangeAudit;
import ru.runa.wfe.commons.cache.states.audit.StageSwitchAudit;
import ru.runa.wfe.commons.cache.states.nonruntime.NonRuntimeCacheContext;
import ru.runa.wfe.commons.cache.states.nonruntime.NonRuntimeCacheStateFactory;

/**
 * State machine for managing cache lifetime.
 */
public class CacheStateMachine<CacheImpl extends CacheImplementation, StateContext> implements CacheInitializationCallback<CacheImpl, StateContext> {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(CacheStateMachine.class);

    /**
     * Cache state machine context with common used data.
     */
    private final CacheStateMachineContext<CacheImpl, StateContext> context;

    /**
     * Current cache lifetime state.
     */
    private final AtomicReference<CacheState<CacheImpl, StateContext>> state;

    /**
     * Component for state machine actions audit.
     */
    private final CacheStateMachineAudit<CacheImpl, StateContext> audit;

    public CacheStateMachine(CacheFactory<CacheImpl> cacheFactory, CacheStateFactory<CacheImpl, StateContext> stateFactory, Object monitor) {
        CacheTransactionalExecutor transactionalExecutor = new DefaultCacheTransactionalExecutor();
        context = new CacheStateMachineContext<CacheImpl, StateContext>(cacheFactory, this, transactionalExecutor, monitor, stateFactory);
        state = new AtomicReference<CacheState<CacheImpl, StateContext>>(context.getStateFactory().createEmptyState(null, null));
        audit = new DefaultCacheStateMachineAudit<CacheImpl, StateContext>();
    }

    public CacheStateMachine(CacheFactory<CacheImpl> cacheFactory, CacheStateFactory<CacheImpl, StateContext> stateFactory, Object monitor,
            CacheTransactionalExecutor transactionalExecutor, CacheStateMachineAudit<CacheImpl, StateContext> audit) {
        context = new CacheStateMachineContext<CacheImpl, StateContext>(cacheFactory, this, transactionalExecutor, monitor, stateFactory);
        state = new AtomicReference<CacheState<CacheImpl, StateContext>>(context.getStateFactory().createEmptyState(null, null));
        this.audit = audit;
    }

    /**
     * Check if transaction is dirty for cache.
     *
     * @param transaction
     *            Transaction to check.
     * @return Return true, if transaction is dirty and false otherwise.
     */
    public boolean isDirtyTransaction(Transaction transaction) {
        return state.get().isDirtyTransaction(transaction);
    }

    /**
     * Get current cache instance or null, if cache is not created. No build step is allowed.
     */
    public CacheImpl getCacheQuick(Transaction transaction) {
        return state.get().getCacheQuickNoBuild(transaction);
    }

    /**
     * Get or create cache. Cache (or proxy) will be returned in all case.
     *
     * @param transaction
     *            Transaction, requested cache.
     * @param isWriteTransaction
     *            Flag, equals true, if transaction is changing some cache related objects (not for this cache, but others) and false otherwise.
     * @return Returns cache.
     */
    public CacheImpl getCache(Transaction transaction, boolean isWriteTransaction) {
        GetCacheAudit<CacheImpl, StateContext> commandAudit = audit.auditGetCache();
        while (true) {
            CacheState<CacheImpl, StateContext> currentState = state.get();
            CacheImpl quickResult = currentState.getCacheQuickNoBuild(transaction);
            if (quickResult != null) {
                commandAudit.quickResult(transaction, quickResult);
                return quickResult;
            }
            if (isWriteTransaction) {
                commandAudit.beforeCreation(transaction);
                StateCommandResultWithCache<CacheImpl, StateContext> result = currentState.getCache(context, transaction);
                commandAudit.afterCreation(transaction, result.getCache());
                if (applyCommandResult(currentState, result, commandAudit)) {
                    return result.getCache();
                }
            } else {
                synchronized (context.getMonitor()) {
                    currentState = state.get();
                    commandAudit.beforeCreation(transaction);
                    StateCommandResultWithCache<CacheImpl, StateContext> result = currentState.getCache(context, transaction);
                    commandAudit.afterCreation(transaction, result.getCache());
                    if (applyCommandResult(currentState, result, commandAudit)) {
                        return result.getCache();
                    }
                }
            }
        }
    }

    /**
     * Get or create cache if cache is not locked (no transaction changing cached persistent object).
     *
     * @param transaction
     *            Transaction, requested cache.
     * @param isWriteTransaction
     *            Flag, equals true, if transaction is changing some cache related objects (not for this cache, but others) and false otherwise.
     * @return Returns cache or null, if cache is locked.
     */
    public CacheImpl getCacheIfNotLocked(Transaction transaction, boolean isWriteTransaction) {
        GetCacheAudit<CacheImpl, StateContext> commandAudit = audit.auditGetCache();
        while (true) {
            CacheState<CacheImpl, StateContext> currentState = state.get();
            CacheImpl quickResult = currentState.getCacheQuickNoBuild(transaction);
            if (quickResult != null) {
                commandAudit.quickResult(transaction, quickResult);
                return quickResult;
            }
            if (currentState.isDirtyTransactionExists()) {
                return null;
            }
            if (isWriteTransaction) {
                commandAudit.beforeCreation(transaction);
                StateCommandResultWithCache<CacheImpl, StateContext> result = currentState.getCacheIfNotLocked(context, transaction);
                commandAudit.afterCreation(transaction, result.getCache());
                if (applyCommandResult(currentState, result, commandAudit)) {
                    return result.getCache();
                }
            } else {
                synchronized (context.getMonitor()) {
                    currentState = state.get();
                    commandAudit.beforeCreation(transaction);
                    StateCommandResultWithCache<CacheImpl, StateContext> result = currentState.getCacheIfNotLocked(context, transaction);
                    commandAudit.afterCreation(transaction, result.getCache());
                    if (applyCommandResult(currentState, result, commandAudit)) {
                        return result.getCache();
                    }
                }
            }
        }
    }

    /**
     * Called when object is changed and cache must be notified about changing transaction.
     *
     * @param transaction
     *            Transaction, which change persistent object.
     * @param changedObject
     *            Changed object description.
     */
    public void onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        OnChangeAudit<CacheImpl, StateContext> commandAudit = audit.auditOnChange();
        while (true) {
            CacheState<CacheImpl, StateContext> currentState = state.get();
            commandAudit.beforeOnChange(transaction, changedObject);
            StateCommandResult<CacheImpl, StateContext> result = currentState.onChange(context, transaction, changedObject);
            commandAudit.afterOnChange(transaction, changedObject);
            if (result.getNextState() == null) {
                commandAudit.nextStageFatalError();
                log.error("onChange must lead to state switch. Incorrect behaviour on state " + currentState.getClass().getName());
            }
            if (applyCommandResult(currentState, result, commandAudit)) {
                return;
            }
        }
    }

    /**
     * Notifies about preparing to complete transaction.
     *
     * @param transaction
     *            Transaction, which will be completed.
     */
    public void beforeTransactionComplete(Transaction transaction) {
        BeforeTransactionCompleteAudit<CacheImpl, StateContext> commandAudit = audit.auditBeforeTransactionComplete();
        while (true) {
            CacheState<CacheImpl, StateContext> currentState = state.get();
            StateCommandResult<CacheImpl, StateContext> result = currentState.beforeTransactionComplete(context, transaction);
            if (applyCommandResult(currentState, result, commandAudit)) {
                return;
            }
        }
    }

    /**
     * Notifies about completed transaction (commit or rollback).
     *
     * @param transaction
     *            Completed transaction.
     */
    public void onTransactionCompleted(Transaction transaction) {
        CompleteTransactionAudit<CacheImpl, StateContext> commandAudit = audit.auditCompleteTransaction();
        while (true) {
            CacheState<CacheImpl, StateContext> currentState = state.get();
            commandAudit.beforeCompleteTransaction(transaction);
            StateCommandResultWithData<CacheImpl, Boolean, StateContext> result = currentState.completeTransaction(context, transaction);
            commandAudit.afterCompleteTransaction(transaction);
            if (result.getNextState() == null) {
                commandAudit.nextStageFatalError();
                log.error("completeTransaction must lead to state switch. Incorrect behaviour on state " + currentState.getClass().getName());
            }
            if (applyCommandResult(currentState, result, commandAudit)) {
                if (result.getData()) {
                    commandAudit.allTransactionsCompleted(transaction);
                }
                return;
            }
        }
    }

    @Override
    public void commitCache(CacheState<CacheImpl, StateContext> commitedState, CacheImpl cache) {
        CommitCacheAudit<CacheImpl, StateContext> commandAudit = audit.auditCommitCache();
        CacheState<CacheImpl, StateContext> currentState = state.get();
        if (commitedState != currentState) {
            commandAudit.stageIsNotCommitStage(cache);
            return;
        }
        commandAudit.beforeCommit(cache);
        StateCommandResult<CacheImpl, StateContext> result = currentState.commitCache(context, cache);
        commandAudit.afterCommit(cache);
        if (result.getNextState() == null) {
            commandAudit.nextStageFatalError();
            log.error("commitCache must lead to state switch. Incorrect behaviour on state " + currentState.getClass().getName());
        }
        // If state machine can't switch, when cache state is changed. Saving this cache is incorrect, so return without saving cache.
        if (applyCommandResult(currentState, result, commandAudit)) {
            CachingLogic.onChange(cache, Change.UPDATE, new Object[] {}, new Object[] {}, new String[] {}, new Type[] {});
        }
    }

    @Override
    public void onError(CacheState<CacheImpl, StateContext> commitedState, Throwable e) {
        InitializationErrorAudit<CacheImpl, StateContext> commandAudit = audit.auditInitializationError();
        log.error("cache initialization throws exception", e);
        commandAudit.onInitializationError(e);
        StateCommandResult<CacheImpl, StateContext> result = StateCommandResult.create(context.getStateFactory().createEmptyState(null, null));
        applyCommandResult(commitedState, result, commandAudit);
    }

    /**
     * Drops cache and moving to empty state.
     */
    public void dropCache() {
        StageSwitchAudit<CacheImpl, StateContext> commandAudit = audit.auditUninitialize();
        CacheState<CacheImpl, StateContext> currentState = state.get();
        while (!applyCommandResult(currentState, currentState.dropCache(context), commandAudit)) {
            currentState = state.get();
        }
    }

    /**
     * Try to change state machine state after command execution. Unused states will receive discard event.
     *
     * @param currentState
     *            Current state of state machine.
     * @param result
     *            Command execution result.
     * @param commandAudit
     *            Object for state machine action audit.
     * @return Returns true, if command result successfully applied and false otherwise (no changes was made).
     */
    private boolean applyCommandResult(CacheState<CacheImpl, StateContext> currentState, StateCommandResult<CacheImpl, StateContext> result,
            StageSwitchAudit<CacheImpl, StateContext> commandAudit) {
        CacheState<CacheImpl, StateContext> nextState = result.getNextState();
        if (nextState == null) {
            commandAudit.stayStage();
            return true;
        }
        if (nextState == currentState) {
            log.error("Switch to the same state is detected. Use null to stay on same state. Incorrect behaviour on state "
                    + currentState.getClass().getName());
            commandAudit.nextStageFatalError();
            commandAudit.stayStage();
            return true;
        }
        if (state.compareAndSet(currentState, nextState)) {
            commandAudit.stageSwitched(currentState, nextState);
            currentState.discard();
            nextState.accept(context);
            return true;
        }
        commandAudit.stageSwitchFailed(currentState, nextState);
        nextState.discard();
        return false;
    }

    /**
     * Creates default implementation of cache state machine for cache, initialized via {@link LazyInitializedCacheFactory}.
     *
     * @param factory
     *            Factory to create cache instances.
     * @param monitor
     *            Monitor, used for exclusive access.
     * @return Returns cache state machine for cache control.
     */
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl, DefaultStateContext> createStateMachine(
            LazyInitializedCacheFactory<CacheImpl> factory, Object monitor) {
        DefaultCacheTransactionalExecutor transactionalExecutor = new DefaultCacheTransactionalExecutor();
        CacheStateFactory<CacheImpl, DefaultStateContext> stateFactory = new IsolatedCacheStateFactory<CacheImpl>();
        LazyCacheFactoryImpl<CacheImpl> cacheFactory = new LazyCacheFactoryImpl<CacheImpl>(factory, transactionalExecutor);
        return new CacheStateMachine<CacheImpl, DefaultStateContext>(cacheFactory, stateFactory, monitor);
    }

    /**
     * Creates default implementation of cache state machine for cache, initialized via {@link StaticCacheFactory}.
     *
     * @param factory
     *            Factory to create cache instances.
     * @param monitor
     *            Monitor, used for exclusive access.
     * @return Returns cache state machine for cache control.
     */
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl, DefaultStateContext> createStateMachine(
            StaticCacheFactory<CacheImpl> factory, Object monitor) {
        CacheStateFactory<CacheImpl, DefaultStateContext> stateFactory = new IsolatedCacheStateFactory<CacheImpl>();
        StaticCacheFactoryImpl<CacheImpl> cacheFactory = new StaticCacheFactoryImpl<CacheImpl>(factory);
        return new CacheStateMachine<CacheImpl, DefaultStateContext>(cacheFactory, stateFactory, monitor);
    }

    /**
     * Creates default implementation of cache state machine for cache, initialized via {@link NonRuntimeCacheFactory}.
     *
     * @param factory
     *            Factory to create cache instances.
     * @param monitor
     *            Monitor, used for exclusive access.
     * @return Returns cache state machine for cache control.
     */
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl, NonRuntimeCacheContext> createStateMachine(
            NonRuntimeCacheFactory<CacheImpl> factory, Object monitor) {
        DefaultCacheTransactionalExecutor transactionalExecutor = new DefaultCacheTransactionalExecutor();
        CacheStateFactory<CacheImpl, NonRuntimeCacheContext> stateFactory = new NonRuntimeCacheStateFactory<CacheImpl>();
        NonRuntimeCacheFactoryImpl<CacheImpl> cacheFactory = new NonRuntimeCacheFactoryImpl<CacheImpl>(factory, transactionalExecutor);
        return new CacheStateMachine<CacheImpl, NonRuntimeCacheContext>(cacheFactory, stateFactory, monitor);
    }

    /**
     * Creates implementation of cache state machine for cache, initialized via {@link LazyInitializedCacheFactory}. Used for tests and receives all
     * parameters.
     *
     * @param factory
     *            Factory to create cache instances.
     * @param monitor
     *            Monitor, used for exclusive access.
     * @param transactionalExecutor
     *            Implementation for executing cache initialization in transaction.
     * @param audit
     *            Audit interface implementation for audit state machine actions.
     * @return Returns cache state machine for cache control.
     */
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl, DefaultStateContext> createStateMachine(
            LazyInitializedCacheFactory<CacheImpl> factory, CacheStateFactory<CacheImpl, DefaultStateContext> stateFactory, Object monitor,
            CacheTransactionalExecutor transactionalExecutor, CacheStateMachineAudit<CacheImpl, DefaultStateContext> audit) {
        LazyCacheFactoryImpl<CacheImpl> cacheFactory = new LazyCacheFactoryImpl<CacheImpl>(factory, transactionalExecutor);
        return new CacheStateMachine<CacheImpl, DefaultStateContext>(cacheFactory, stateFactory, monitor, transactionalExecutor, audit);
    }

    /**
     * Factory for adopt {@link NonRuntimeCacheFactory} implementation for state machine.
     *
     * @param <CacheImpl>
     *            Cache implementation type.
     */
    final static class NonRuntimeCacheFactoryImpl<CacheImpl extends CacheImplementation> implements CacheFactory<CacheImpl> {
        /**
         * Delegated (adopted) factory.
         */
        private final NonRuntimeCacheFactory<CacheImpl> factory;

        /**
         * Instance of {@link CacheTransactionalExecutor} for executing initialization in transaction.
         */
        private final CacheTransactionalExecutor transactionalExecutor;

        public NonRuntimeCacheFactoryImpl(NonRuntimeCacheFactory<CacheImpl> factory, CacheTransactionalExecutor transactionalExecutor) {
            super();
            this.factory = factory;
            this.transactionalExecutor = transactionalExecutor;
        }

        @Override
        public boolean hasDelayedInitialization() {
            return true;
        }

        @Override
        public CacheImpl createCache() {
            return factory.createProxy();
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AtomicReference<CacheImpl> cache = new AtomicReference<CacheImpl>();
                        if (!context.isInitializationStillRequired()) {
                            return;
                        }
                        transactionalExecutor.executeInTransaction(new Runnable() {

                            @Override
                            public void run() {
                                if (!context.isInitializationStillRequired()) {
                                    return;
                                }
                                if (log.isTraceEnabled()) {
                                    log.trace("Creating cache from " + factory);
                                }
                                cache.set(factory.buildCache(context));
                                if (log.isDebugEnabled()) {
                                    log.debug("Created cache " + cache.get());
                                }
                                context.onComplete(cache.get());
                            }
                        });
                    } catch (Throwable e) {
                        context.onError(e);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Factory for adopt {@link LazyInitializedCacheFactory} implementation for state machine.
     *
     * @param <CacheImpl>
     *            Cache implementation type.
     */
    final static class LazyCacheFactoryImpl<CacheImpl extends CacheImplementation> implements CacheFactory<CacheImpl> {
        /**
         * Delegated (adopted) factory.
         */
        private final LazyInitializedCacheFactory<CacheImpl> factory;

        /**
         * Instance of {@link CacheTransactionalExecutor} for executing initialization in transaction.
         */
        private final CacheTransactionalExecutor transactionalExecutor;

        public LazyCacheFactoryImpl(LazyInitializedCacheFactory<CacheImpl> factory, CacheTransactionalExecutor transactionalExecutor) {
            super();
            this.factory = factory;
            this.transactionalExecutor = transactionalExecutor;
        }

        @Override
        public boolean hasDelayedInitialization() {
            return true;
        }

        @Override
        public CacheImpl createCache() {
            return factory.createProxy();
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AtomicReference<CacheImpl> cache = new AtomicReference<CacheImpl>();
                        if (!context.isInitializationStillRequired()) {
                            return;
                        }
                        transactionalExecutor.executeInTransaction(new Runnable() {

                            @Override
                            public void run() {
                                if (!context.isInitializationStillRequired()) {
                                    return;
                                }
                                if (log.isTraceEnabled()) {
                                    log.trace("Creating cache from " + factory);
                                }
                                cache.set(factory.buildCache(context));
                                if (log.isDebugEnabled()) {
                                    log.debug("Created cache " + cache.get());
                                }
                                context.onComplete(cache.get());
                            }
                        });
                    } catch (Throwable e) {
                        context.onError(e);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Factory for adopt {@link StaticCacheFactory} implementation for state machine.
     *
     * @param <CacheImpl>
     *            Cache implementation type.
     */
    final static class StaticCacheFactoryImpl<CacheImpl extends CacheImplementation> implements CacheFactory<CacheImpl> {
        /**
         * Delegated (adopted) factory.
         */
        private final StaticCacheFactory<CacheImpl> factory;

        public StaticCacheFactoryImpl(StaticCacheFactory<CacheImpl> factory) {
            super();
            this.factory = factory;
        }

        @Override
        public boolean hasDelayedInitialization() {
            return false;
        }

        @Override
        public CacheImpl createCache() {
            if (log.isTraceEnabled()) {
                log.trace("Creating cache from " + factory);
            }
            CacheImpl cache = factory.buildCache();
            if (log.isDebugEnabled()) {
                log.debug("Created cache " + cache);
            }
            return cache;
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
        }
    }
}
