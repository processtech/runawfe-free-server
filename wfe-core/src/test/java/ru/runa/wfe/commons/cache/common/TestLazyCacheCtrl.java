package ru.runa.wfe.commons.cache.common;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;
import ru.runa.wfe.commons.cache.states.CacheState;

public final class TestLazyCacheCtrl {
    private final CacheStateMachine<TestCacheIface> stateMachine;
    private final TestCacheStateMachineAudit<TestCacheIface> audit;
    private final ThreadLocal<TestTransaction> transactions = new ThreadLocal<>();

    public TestLazyCacheCtrl(TestLazyCacheFactoryCallback factoryCallback) {
        audit = new TestCacheStateMachineAudit<>();
        stateMachine = new CacheStateMachine<>(new TestLazyCacheFactory(factoryCallback), TestLazyCacheCtrl.class, audit);
    }

    public TestCacheIface getCacheWithChoise(boolean isWriteTransaction, boolean getCacheIfNotLocked) {
        if (getCacheIfNotLocked) {
            return getCacheIfNotLocked(isWriteTransaction);
        } else {
            return getCache(isWriteTransaction);
        }
    }

    public TestCacheIface getCache(boolean isWriteTransaction) {
        TestTransaction transaction = transactions.get();
        if (transaction == null) {
            transaction = new TestTransaction();
            transactions.set(transaction);
        }
        return stateMachine.getCache(transaction, isWriteTransaction);
    }

    public TestCacheIface getCache(Transaction transaction, boolean isWriteTransaction) {
        return stateMachine.getCache(transaction, isWriteTransaction);
    }

    public TestCacheIface getCacheIfNotLocked(boolean isWriteTransaction) {
        TestTransaction transaction = transactions.get();
        if (transaction == null) {
            transaction = new TestTransaction();
            transactions.set(transaction);
        }
        return stateMachine.getCacheIfNotLocked(transaction, isWriteTransaction);
    }

    public TestCacheIface getCacheIfNotLocked(Transaction transaction, boolean isWriteTransaction) {
        return stateMachine.getCacheIfNotLocked(transaction, isWriteTransaction);
    }

    public void onChanged(ChangedObjectParameter changedObject) {
        TestTransaction transaction = transactions.get();
        if (transaction == null) {
            transaction = new TestTransaction();
            transactions.set(transaction);
        }
        stateMachine.onChange(transaction, changedObject);
    }

    public void onChanged(Transaction transaction, ChangedObjectParameter changedObject) {
        stateMachine.onChange(transaction, changedObject);
    }

    public boolean isCacheInstanceExists() {
        return getStateMachineState().getCacheQuickNoBuild(transactions.get()) != null;
    }

    public TestCacheIface getCurrentCacheInstance() {
        return getStateMachineState().getCacheQuickNoBuild(transactions.get());
    }

    private CacheState<TestCacheIface> getStateMachineState() {
        try {

            Field stateField = stateMachine.getClass().getDeclaredField("state");
            stateField.setAccessible(true);
            AtomicReference<CacheState<TestCacheIface>> state =
                    (AtomicReference<CacheState<TestCacheIface>>) stateField.get(stateMachine);
            return state.get();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TestCacheStateMachineAudit<TestCacheIface> getAudit() {
        return audit;
    }
}