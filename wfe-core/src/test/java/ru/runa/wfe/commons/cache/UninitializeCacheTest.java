package ru.runa.wfe.commons.cache;

import javax.transaction.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.DaemonSafeThread;
import ru.runa.wfe.commons.ManualResetEvent;
import ru.runa.wfe.commons.cache.common.TestCacheIface;
import ru.runa.wfe.commons.cache.common.TestCacheStateMachineAudit;
import ru.runa.wfe.commons.cache.common.TestCacheStateMachineAudit.TestOnChangeAudit;
import ru.runa.wfe.commons.cache.common.TestLazyCacheCtrl;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactory;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactoryCallback;
import ru.runa.wfe.commons.cache.states.CacheState;

public class UninitializeCacheTest {

    @Test()
    public void uninitializeEmptyStateTest() {
        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        Assert.assertNull(ctrl.getCurrentCacheInstance());
        ctrl.onChanged(new ChangedObjectParameter(1, Change.DELETE, null, null, null));
        Assert.assertNull(ctrl.getCurrentCacheInstance());
    }

    @Test()
    public void multipleUninitializeTest() {
        final ManualResetEvent initializationCompleteEvent = new ManualResetEvent();
        final ManualResetEvent thread1BlockedEvent = new ManualResetEvent();
        final ManualResetEvent thread1ReleaseBlockEvent = new ManualResetEvent();
        final ManualResetEvent thread2ReleaseBlockEvent = new ManualResetEvent();
        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        ctrl.getAudit().set_commitCacheAudit(new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {
            @Override
            protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                initializationCompleteEvent.setEvent();
            }
        });
        ctrl.getCache(false);
        initializationCompleteEvent.tryWaitEvent();
        Assert.assertNotNull(ctrl.getCurrentCacheInstance());

        ctrl.getAudit().set_onChangeAudit(new TestCacheStateMachineAudit.TestOnChangeAudit<TestCacheIface>() {
            @Override
            protected void _afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
                thread1BlockedEvent.setEvent();
                thread1ReleaseBlockEvent.tryWaitEvent();
            }
        });
        DaemonSafeThread thread1 = DaemonSafeThread.createAndStart(new Runnable() {

            @Override
            public void run() {
                ctrl.onChanged(new ChangedObjectParameter(1L, Change.DELETE, null, null, null));
                Assert.assertNull(ctrl.getCurrentCacheInstance().cachedValue(1));
                Assert.assertEquals(new Long(2L), ctrl.getCurrentCacheInstance().cachedValue(2));
                thread2ReleaseBlockEvent.setEvent();
            }
        });
        thread1BlockedEvent.tryWaitEvent();

        DaemonSafeThread thread2 = DaemonSafeThread.createAndStart(new Runnable() {

            @Override
            public void run() {
                ctrl.getAudit().set_onChangeAudit(new TestCacheStateMachineAudit.TestOnChangeAudit<TestCacheIface>() {
                    @Override
                    protected void _afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
                        thread1ReleaseBlockEvent.setEvent();
                        thread2ReleaseBlockEvent.tryWaitEvent();
                    }
                });
                ctrl.onChanged(new ChangedObjectParameter(-1L, Change.DELETE, null, null, null));
                Assert.assertNull(ctrl.getCurrentCacheInstance());
                TestOnChangeAudit<TestCacheIface> changeAudit = (TestOnChangeAudit<TestCacheIface>) ctrl.getAudit().auditOnChange();
                Assert.assertEquals(2, changeAudit.getBeforeOnChangeCount());
                Assert.assertEquals(2, changeAudit.getAfterOnChangeCount());
                Assert.assertEquals(1, changeAudit.getSwitchedCount());
                Assert.assertEquals(1, changeAudit.getSwitchFailedCount());
            }
        });
        thread1.join();
        thread2.join();
    }
}
