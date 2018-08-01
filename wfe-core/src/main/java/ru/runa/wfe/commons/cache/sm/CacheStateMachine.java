package ru.runa.wfe.commons.cache.sm;

import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.Transaction;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.factories.LazyInitializedCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.NonRuntimeCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;
import ru.runa.wfe.commons.cache.states.DefaultCacheStateFactory;
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
import ru.runa.wfe.commons.cache.states.nonruntime.NonRuntimeCacheStateFactory;

/**
 * State machine for managing cache lifetime.
 */
@CommonsLog
public class CacheStateMachine<CacheImpl extends CacheImplementation> implements CacheInitializationCallback<CacheImpl> {

    /**
     * Factory, used to create cache instances.
     */
    private final CacheFactoryProxy<CacheImpl> cacheFactory;

    /**
     * Factory to create states.
     */
    private final CacheStateFactory<CacheImpl> stateFactory;

    /**
     * Monitor, used for exclusive access.
     */
    private final Object monitor;

    /**
     * Current cache lifetime state.
     */
    private final AtomicReference<CacheState<CacheImpl>> state;

    /**
     * Component for state machine actions audit.
     */
    private final CacheStateMachineAudit<CacheImpl> audit;

    public CacheStateMachine(
            CacheFactoryProxy<CacheImpl> cacheFactory, CacheStateFactory<CacheImpl> stateFactory, Object monitor,
            CacheStateMachineAudit<CacheImpl> audit
    ) {
        this.cacheFactory = cacheFactory;
        stateFactory.setOwner(this);
        this.stateFactory = stateFactory;
        this.monitor = monitor;
        state = new AtomicReference<>(stateFactory.createEmptyState(null));
        this.audit = audit;
    }

    public CacheStateMachine(CacheFactoryProxy<CacheImpl> cacheFactory, CacheStateFactory<CacheImpl> stateFactory, Object monitor) {
        this(cacheFactory, stateFactory, monitor, new DefaultCacheStateMachineAudit<>());
    }

    public CacheFactoryProxy<CacheImpl> getCacheFactory() {
        return cacheFactory;
    }

    public CacheStateFactory<CacheImpl> getStateFactory() {
        return stateFactory;
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
        GetCacheAudit<CacheImpl> commandAudit = audit.auditGetCache();
        while (true) {
            CacheState<CacheImpl> currentState = state.get();
            CacheImpl quickResult = currentState.getCacheQuickNoBuild(transaction);
            if (quickResult != null) {
                commandAudit.quickResult(transaction, quickResult);
                return quickResult;
            }
            if (isWriteTransaction) {
                commandAudit.beforeCreation(transaction);
                StateCommandResultWithCache<CacheImpl> result = currentState.getCache(transaction);
                commandAudit.afterCreation(transaction, result.getCache());
                if (applyCommandResult(currentState, result, commandAudit)) {
                    return result.getCache();
                }
            } else {
                synchronized (monitor) {
                    currentState = state.get();
                    commandAudit.beforeCreation(transaction);
                    StateCommandResultWithCache<CacheImpl> result = currentState.getCache(transaction);
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
        GetCacheAudit<CacheImpl> commandAudit = audit.auditGetCache();
        while (true) {
            CacheState<CacheImpl> currentState = state.get();
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
                StateCommandResultWithCache<CacheImpl> result = currentState.getCacheIfNotLocked(transaction);
                commandAudit.afterCreation(transaction, result.getCache());
                if (applyCommandResult(currentState, result, commandAudit)) {
                    return result.getCache();
                }
            } else {
                synchronized (monitor) {
                    currentState = state.get();
                    commandAudit.beforeCreation(transaction);
                    StateCommandResultWithCache<CacheImpl> result = currentState.getCacheIfNotLocked(transaction);
                    commandAudit.afterCreation(transaction, result.getCache());
                    if (applyCommandResult(currentState, result, commandAudit)) {
                        return result.getCache();
                    }
                }
            }
        }
    }

    /**
     * Called then object is changed and cache must be notified about changing transaction.
     *
     * @param transaction
     *            Transaction, which change persistent object.
     * @param changedObject
     *            Changed object description.
     */
    public void onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        OnChangeAudit<CacheImpl> commandAudit = audit.auditOnChange();
        while (true) {
            CacheState<CacheImpl> currentState = state.get();
            commandAudit.beforeOnChange(transaction, changedObject);
            StateCommandResult<CacheImpl> result = currentState.onChange(transaction, changedObject);
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
    public void onBeforeTransactionComplete(Transaction transaction) {
        BeforeTransactionCompleteAudit<CacheImpl> commandAudit = audit.auditBeforeTransactionComplete();
        while (true) {
            CacheState<CacheImpl> currentState = state.get();
            StateCommandResult<CacheImpl> result = currentState.onBeforeTransactionComplete(transaction);
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
    public void onAfterTransactionComplete(Transaction transaction) {
        CompleteTransactionAudit<CacheImpl> commandAudit = audit.auditCompleteTransaction();
        while (true) {
            CacheState<CacheImpl> currentState = state.get();
            commandAudit.beforeCompleteTransaction(transaction);
            StateCommandResultWithData<CacheImpl, Boolean> result = currentState.onAfterTransactionComplete(transaction);
            commandAudit.afterCompleteTransaction(transaction);
            if (result.getNextState() == null) {
                commandAudit.nextStageFatalError();
                log.error("onAfterTransactionComplete must lead to state switch. Incorrect behaviour on state " + currentState.getClass().getName());
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
    public void commitCache(CacheState<CacheImpl> commitedState, CacheImpl cache) {
        CommitCacheAudit<CacheImpl> commandAudit = audit.auditCommitCache();
        CacheState<CacheImpl> currentState = state.get();
        if (commitedState != currentState) {
            commandAudit.stageIsNotCommitStage(cache);
            return;
        }
        commandAudit.beforeCommit(cache);
        StateCommandResult<CacheImpl> result = currentState.commitCache(cache);
        commandAudit.afterCommit(cache);
        if (result.getNextState() == null) {
            commandAudit.nextStageFatalError();
            log.error("commitCache must lead to state switch. Incorrect behaviour on state " + currentState.getClass().getName());
        }
        // If state machine can't switch, when cache state is changed. Saving this cache is incorrect, so return without saving cache.
        if (applyCommandResult(currentState, result, commandAudit)) {
            CachingLogic.onChange(cache, Change.UPDATE, new Object[] {}, new Object[] {}, new String[] {});
        }
    }

    @Override
    public void onError(CacheState<CacheImpl> commitedState, Throwable e) {
        InitializationErrorAudit<CacheImpl> commandAudit = audit.auditInitializationError();
        log.error("cache initialization throws exception", e);
        commandAudit.onInitializationError(e);
        StateCommandResult<CacheImpl> result = StateCommandResult.create(getStateFactory().createEmptyState(null));
        applyCommandResult(commitedState, result, commandAudit);
    }

    /**
     * Drops cache and moves to empty state.
     */
    public void dropCache() {
        StageSwitchAudit<CacheImpl> commandAudit = audit.auditDropCache();
        CacheState<CacheImpl> currentState = state.get();
        while (!applyCommandResult(currentState, currentState.dropCache(), commandAudit)) {
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
    private boolean applyCommandResult(CacheState<CacheImpl> currentState, StateCommandResult<CacheImpl> result,
            StageSwitchAudit<CacheImpl> commandAudit) {
        CacheState<CacheImpl> nextState = result.getNextState();
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
            nextState.accept();
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
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl> createStateMachine(
            LazyInitializedCacheFactory<CacheImpl> factory, Object monitor
    ) {
        DefaultCacheTransactionalExecutor transactionalExecutor = new DefaultCacheTransactionalExecutor();
        boolean isIsolated = SystemProperties.useIsolatedCacheStateMachine();
        CacheStateFactory<CacheImpl> stateFactory = isIsolated ? new IsolatedCacheStateFactory<>() : new DefaultCacheStateFactory<>();
        LazyCacheFactoryProxy<CacheImpl> cacheFactory = new LazyCacheFactoryProxy<>(factory, transactionalExecutor);
        return new CacheStateMachine<>(cacheFactory, stateFactory, monitor);
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
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl> createStateMachine(
            StaticCacheFactory<CacheImpl> factory, Object monitor
    ) {
        boolean isIsolated = SystemProperties.useIsolatedCacheStateMachine();
        CacheStateFactory<CacheImpl> stateFactory = isIsolated ? new IsolatedCacheStateFactory<>() : new DefaultCacheStateFactory<>();
        StaticCacheFactoryProxy<CacheImpl> cacheFactory = new StaticCacheFactoryProxy<>(factory);
        return new CacheStateMachine<>(cacheFactory, stateFactory, monitor);
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
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl> createStateMachine(
            NonRuntimeCacheFactory<CacheImpl> factory, Object monitor
    ) {
        DefaultCacheTransactionalExecutor transactionalExecutor = new DefaultCacheTransactionalExecutor();
        CacheStateFactory<CacheImpl> stateFactory = new NonRuntimeCacheStateFactory<>();
        NonRuntimeCacheFactoryProxy<CacheImpl> cacheFactory = new NonRuntimeCacheFactoryProxy<>(factory, transactionalExecutor);
        return new CacheStateMachine<>(cacheFactory, stateFactory, monitor);
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
    public static <CacheImpl extends CacheImplementation> CacheStateMachine<CacheImpl> createStateMachine(
            LazyInitializedCacheFactory<CacheImpl> factory, CacheStateFactory<CacheImpl> stateFactory, Object monitor,
            CacheTransactionalExecutor transactionalExecutor, CacheStateMachineAudit<CacheImpl> audit
    ) {
        LazyCacheFactoryProxy<CacheImpl> cacheFactory = new LazyCacheFactoryProxy<>(factory, transactionalExecutor);
        return new CacheStateMachine<>(cacheFactory, stateFactory, monitor, audit);
    }

    /**
     * Factory for adopt {@link NonRuntimeCacheFactory} implementation for state machine.
     *
     * @param <CacheImpl>
     *            Cache implementation type.
     */
    final static class NonRuntimeCacheFactoryProxy<CacheImpl extends CacheImplementation> implements CacheFactoryProxy<CacheImpl> {
        /**
         * Delegated (adopted) factory.
         */
        private final NonRuntimeCacheFactory<CacheImpl> factory;

        /**
         * Instance of {@link CacheTransactionalExecutor} for executing initialization in transaction.
         */
        private final CacheTransactionalExecutor transactionalExecutor;

        public NonRuntimeCacheFactoryProxy(NonRuntimeCacheFactory<CacheImpl> factory, CacheTransactionalExecutor transactionalExecutor) {
            this.factory = factory;
            this.transactionalExecutor = transactionalExecutor;
        }

        @Override
        public boolean hasDelayedInitialization() {
            return true;
        }

        @Override
        public CacheImpl createCache() {
            return factory.createStub();
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AtomicReference<CacheImpl> cache = new AtomicReference<>();
                        if (!context.isInitializationStillRequired()) {
                            return;
                        }
                        transactionalExecutor.executeInTransaction(new Runnable() {

                            @Override
                            public void run() {
                                if (!context.isInitializationStillRequired()) {
                                    return;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("Creating cache from " + factory);
                                }
                                cache.set(factory.buildCache(context));
                                if (log.isDebugEnabled()) {
                                    log.debug("Created cache from " + factory + ": " + cache.get());
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
    final static class LazyCacheFactoryProxy<CacheImpl extends CacheImplementation> implements CacheFactoryProxy<CacheImpl> {
        /**
         * Delegated (adopted) factory.
         */
        private final LazyInitializedCacheFactory<CacheImpl> factory;

        /**
         * Instance of {@link CacheTransactionalExecutor} for executing initialization in transaction.
         */
        private final CacheTransactionalExecutor transactionalExecutor;

        public LazyCacheFactoryProxy(LazyInitializedCacheFactory<CacheImpl> factory, CacheTransactionalExecutor transactionalExecutor) {
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
            return factory.createStub();
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AtomicReference<CacheImpl> cache = new AtomicReference<>();
                        if (!context.isInitializationStillRequired()) {
                            return;
                        }
                        transactionalExecutor.executeInTransaction(new Runnable() {

                            @Override
                            public void run() {
                                if (!context.isInitializationStillRequired()) {
                                    return;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("Creating cache from " + factory);
                                }
                                cache.set(factory.buildCache(context));
                                if (log.isDebugEnabled()) {
                                    log.debug("Created cache from " + factory + ": " + cache.get());
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
    final static class StaticCacheFactoryProxy<CacheImpl extends CacheImplementation> implements CacheFactoryProxy<CacheImpl> {
        private final StaticCacheFactory<CacheImpl> factory;

        public StaticCacheFactoryProxy(StaticCacheFactory<CacheImpl> factory) {
            this.factory = factory;
        }

        @Override
        public boolean hasDelayedInitialization() {
            return false;
        }

        @Override
        public CacheImpl createCache() {
            return factory.buildCache();
        }

        @Override
        public void startDelayedInitialization(final CacheInitializationContext<CacheImpl> context) {
        }
    }
}
