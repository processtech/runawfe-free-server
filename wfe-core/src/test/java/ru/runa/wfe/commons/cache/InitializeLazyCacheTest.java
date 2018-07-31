package ru.runa.wfe.commons.cache;

import java.util.concurrent.Semaphore;
import javax.transaction.Transaction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.DaemonSafeThread;
import ru.runa.wfe.commons.ManualResetEvent;
import ru.runa.wfe.commons.TestUtils;
import ru.runa.wfe.commons.cache.common.TestCacheIface;
import ru.runa.wfe.commons.cache.common.TestCacheStateMachineAudit;
import ru.runa.wfe.commons.cache.common.TestLazyCache;
import ru.runa.wfe.commons.cache.common.TestLazyCacheCtrl;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactory;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactoryCallback;
import ru.runa.wfe.commons.cache.common.TestLazyCacheProxy;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.audit.CommitCacheAudit;
import ru.runa.wfe.commons.cache.states.audit.GetCacheAudit;

public class InitializeLazyCacheTest {

    final Class<? extends TestCacheIface> cacheClass = TestLazyCache.class;
    final Class<? extends TestCacheIface> proxyClass = TestLazyCacheProxy.class;

    @DataProvider
    public Object[][] getCacheMethodType() {
        return new Object[][] { { false }, { true } };
    }

    /**
     * Simple initialization process. Single thread requesting cache and checking state before/after cache initialization.
     */
    @Test(dataProvider = "getCacheMethodType")
    public void simpleInitializationTest(boolean getCacheIfNoLocked) {

        final ManualResetEvent createCacheEvt = new ManualResetEvent();
        final ManualResetEvent commitCacheEvt = new ManualResetEvent();
        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback() {

            @Override
            public void beforeCacheCreation() {
                super.beforeCacheCreation();
                createCacheEvt.tryWaitEvent();
            }
        };
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        GetCacheAudit<TestCacheIface> _getCacheAudit = new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {

            @Override
            protected void _afterCreation(Transaction transaction, TestCacheIface cache) {
                Assert.assertFalse(ctrl.isCacheInstanceExists());
                Assert.assertEquals(cache.getClass(), proxyClass);
            }

            @Override
            protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                Assert.assertTrue(ctrl.isCacheInstanceExists());
                Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), proxyClass);
            }
        };
        ctrl.getAudit().set_getCacheAudit(_getCacheAudit);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _afterCommit(TestCacheIface cache) {
                        // Cache stated has not changed. Only after successful change an initialized cached is returned.
                        Assert.assertTrue(ctrl.isCacheInstanceExists());
                        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), proxyClass);
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitCacheEvt.setEvent();
                        Assert.assertTrue(ctrl.isCacheInstanceExists());
                        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        // No cache initially.
        Assert.assertFalse(ctrl.isCacheInstanceExists());
        // Creation of object proxy. Initialization is running and waits for signal.
        Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), proxyClass);
        // Allow initialization to go on waiting for its completion.
        createCacheEvt.setEvent();
        commitCacheEvt.tryWaitEvent();
        // Cache initialization is completed.
        Assert.assertTrue(ctrl.isCacheInstanceExists());

        ctrl.getAudit().set_getCacheAudit(new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {
            @Override
            protected void _beforeCreation(Transaction transaction) {
                throw new AssertionError("cache is already created and may not be recreated");
            }
        });

        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
        Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), cacheClass);
        Assert.assertTrue(ctrl.isCacheInstanceExists());
        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
        Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), cacheClass);

        Assert.assertEquals(factoryCallback.getProxyCreated(), 1);
        Assert.assertEquals(factoryCallback.getCacheCreated(), 1);
    }

    /**
     * Multiple threads requested initialization process. Proxy cache object and cache must be builded only once (State machine must support
     * synchronization between readers to not create multiple instances of caches). All threads not blocked until cache build and receives proxy cache
     * object.
     */
    @Test(dataProvider = "getCacheMethodType")
    public void multiThreadInitializationTest(final boolean getCacheIfNoLocked) {
        final ManualResetEvent createCacheEvt = new ManualResetEvent();
        final ManualResetEvent commitCacheEvt = new ManualResetEvent();
        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback() {

            @Override
            public void beforeCacheCreation() {
                super.beforeCacheCreation();
                createCacheEvt.tryWaitEvent();
            }
        };
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        GetCacheAudit<TestCacheIface> _getCacheAudit = new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {

            @Override
            protected void _afterCreation(Transaction transaction, TestCacheIface cache) {
                Assert.assertEquals(cache.getClass(), proxyClass);
            }

            @Override
            protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                Assert.assertTrue(ctrl.isCacheInstanceExists());
                Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), proxyClass);
            }

            @Override
            protected void _stageSwitchFailed(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                throw new RuntimeException("multiThreadInitializationTest: stageSwitchFailed is unexpected");
            }
        };
        ctrl.getAudit().set_getCacheAudit(_getCacheAudit);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _afterCommit(TestCacheIface cache) {
                        // Cache stated has not changed. Only after successful change an initialized cached is returned.
                        Assert.assertTrue(ctrl.isCacheInstanceExists());
                        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), proxyClass);
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitCacheEvt.setEvent();
                        Assert.assertTrue(ctrl.isCacheInstanceExists());
                        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        final int threadsCount = 10;
        final Semaphore threadWaitSemaphore = new Semaphore(1 - threadsCount);
        Runnable getCacheThreadRunnable = new Runnable() {

            @Override
            public void run() {
                // Creation of object proxy.
                Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), proxyClass);
                threadWaitSemaphore.release();
                TestUtils.tryAcquireSemaphore(threadWaitSemaphore);
                threadWaitSemaphore.release(1000);
                // Allow initialization to go on waiting for its completion.
                createCacheEvt.setEvent();
                commitCacheEvt.tryWaitEvent();
                // Cache initialization is completed.
                Assert.assertTrue(ctrl.isCacheInstanceExists());

                ctrl.getAudit().set_getCacheAudit(new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {
                    @Override
                    protected void _beforeCreation(Transaction transaction) {
                        throw new AssertionError("cache is already created and may not be recreated");
                    }
                });

                Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
                Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), cacheClass);
                Assert.assertTrue(ctrl.isCacheInstanceExists());
                Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
                Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), cacheClass);
            }
        };

        DaemonSafeThread[] threads = new DaemonSafeThread[threadsCount];
        for (int i = 0; i < threadsCount; ++i) {
            threads[i] = DaemonSafeThread.createAndStart(getCacheThreadRunnable);
        }
        for (int i = 0; i < threadsCount; ++i) {
            threads[i].join();
        }

        Assert.assertEquals(factoryCallback.getProxyCreated(), 1);
        Assert.assertEquals(factoryCallback.getCacheCreated(), 1);
    }

    /**
     * Write thread must not be blocked on initialization. It may receive different copy of cache (not the same as readers).
     */
    @Test(dataProvider = "getCacheMethodType")
    public void writeThreadNotBlockTest(final boolean getCacheIfNoLocked) {

        final ManualResetEvent readThreadBlockedEvt = new ManualResetEvent();
        final ManualResetEvent readThreadAllowUnblockEvt = new ManualResetEvent();
        final ManualResetEvent readThreadGetCacheCompleted = new ManualResetEvent();
        final ManualResetEvent commitAllowEvt = new ManualResetEvent();
        final ManualResetEvent commitedEvt = new ManualResetEvent();

        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        GetCacheAudit<TestCacheIface> readThreadGetCacheAudit =
                new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _beforeCreation(Transaction transaction) {
                        readThreadBlockedEvt.setEvent();
                        readThreadAllowUnblockEvt.tryWaitEvent();
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        throw new RuntimeException("read thread must not switch state - it must be done by write thread");
                    }
                };
        ctrl.getAudit().set_getCacheAudit(readThreadGetCacheAudit);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _beforeCommit(TestCacheIface cache) {
                        commitAllowEvt.tryWaitEvent();
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitedEvt.setEvent();
                        Assert.assertTrue(ctrl.isCacheInstanceExists());
                        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), cacheClass);
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        DaemonSafeThread readThread = DaemonSafeThread.createAndStart(new Runnable() {

            @Override
            public void run() {
                Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), proxyClass);
                readThreadGetCacheCompleted.setEvent();
                commitedEvt.tryWaitEvent();
                Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), cacheClass);
            }
        });

        readThreadBlockedEvt.tryWaitEvent();
        ctrl.getAudit().set_getCacheAudit(new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>());
        // In the thread of writing transaction cache must be initialized despite the reading transaction thread being blocked.
        Assert.assertEquals(ctrl.getCacheWithChoise(true, getCacheIfNoLocked).getClass(), proxyClass);
        readThreadAllowUnblockEvt.setEvent();
        readThreadGetCacheCompleted.tryWaitEvent();
        // Both threads got instance of proxy class
        Assert.assertEquals(ctrl.getCurrentCacheInstance().getClass(), proxyClass);
        // Allow cache initialization to complete.
        commitAllowEvt.setEvent();
        commitedEvt.tryWaitEvent();
        Assert.assertEquals(ctrl.getCacheWithChoise(true, getCacheIfNoLocked).getClass(), cacheClass);

        readThread.join();

        Assert.assertEquals(factoryCallback.getProxyCreated(), 2);
        Assert.assertEquals(factoryCallback.getCacheCreated(), 1);
    }

    /**
     * Cache changed while lazy initialized cache is committing. Cache must not be accepted.
     */
    @Test(dataProvider = "getCacheMethodType")
    public void changeCacheOnInititalization(final boolean getCacheIfNoLocked) {
        final ManualResetEvent commitInProgressEvt = new ManualResetEvent();
        final ManualResetEvent commitAllowEvt = new ManualResetEvent();
        final ManualResetEvent commitedEvt = new ManualResetEvent();

        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _afterCommit(TestCacheIface cache) {
                        commitInProgressEvt.setEvent();
                        commitAllowEvt.tryWaitEvent();
                    }

                    @Override
                    protected void _stageSwitchFailed(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitedEvt.setEvent();
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        throw new RuntimeException(
                                "changeCacheOnInititalization: stageSwitched must not be success (stage is changed before commit)");
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        Assert.assertEquals(ctrl.getCacheWithChoise(false, getCacheIfNoLocked).getClass(), proxyClass);
        commitInProgressEvt.tryWaitEvent();
        Assert.assertTrue(ctrl.isCacheInstanceExists());
        ctrl.onChanged(new ChangedObjectParameter(11L, Change.UPDATE, null, null, null));
        Assert.assertFalse(ctrl.isCacheInstanceExists());
        commitAllowEvt.setEvent();
        commitedEvt.tryWaitEvent();
        Assert.assertFalse(ctrl.isCacheInstanceExists());
    }

    /**
     * Some write threads requested cache initialization.
     */
    @Test(dataProvider = "getCacheMethodType")
    public void multipleWriteThreadsInitialization(final boolean getCacheIfNoLocked) {
        final int threadsCount = 10;
        final Semaphore beforeCreationSemaphore = new Semaphore(1 - threadsCount);
        final Semaphore allCreatedSemaphore = new Semaphore(1 - threadsCount);
        final ManualResetEvent commitAllowEvt = new ManualResetEvent();
        final ManualResetEvent commitedEvt = new ManualResetEvent();

        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _afterCommit(TestCacheIface cache) {
                        commitAllowEvt.tryWaitEvent();
                    }

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitedEvt.setEvent();
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        ctrl.getAudit().set_getCacheAudit(new TestCacheStateMachineAudit.TestGetCacheAudit<TestCacheIface>() {
            @Override
            protected void _beforeCreation(Transaction transaction) {
                beforeCreationSemaphore.release();
                TestUtils.tryAcquireSemaphore(beforeCreationSemaphore);
                beforeCreationSemaphore.release(1000);
            }
        });

        DaemonSafeThread[] threads = new DaemonSafeThread[threadsCount];
        for (int i = 0; i < threadsCount; ++i) {
            threads[i] = DaemonSafeThread.createAndStart(new Runnable() {

                @Override
                public void run() {
                    Assert.assertEquals(ctrl.getCacheWithChoise(true, getCacheIfNoLocked).getClass(), proxyClass);
                    allCreatedSemaphore.release();
                    TestUtils.tryAcquireSemaphore(allCreatedSemaphore);
                    allCreatedSemaphore.release(1000);
                    commitAllowEvt.setEvent();
                    commitedEvt.tryWaitEvent();
                    Assert.assertEquals(ctrl.getCacheWithChoise(true, getCacheIfNoLocked).getClass(), cacheClass);
                }
            });
        }

        for (int i = 0; i < threadsCount; ++i) {
            threads[i].join();
        }

        Assert.assertEquals(factoryCallback.getProxyCreated(), threadsCount);
        Assert.assertEquals(factoryCallback.getCacheCreated(), 1);
    }

    /**
     * Read transaction must not be blocked by dirty thread - returns proxy object or current cache instance if exists.
     */
    @Test(dataProvider = "getCacheMethodType")
    public void cacheProxyCreatedOnDirty(final boolean getCacheIfNoLocked) {
        final ManualResetEvent commitedEvt = new ManualResetEvent();

        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);

        CommitCacheAudit<TestCacheIface> _commitCacheAudit =
                new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {

                    @Override
                    protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                        commitedEvt.setEvent();
                    }
                };
        ctrl.getAudit().set_commitCacheAudit(_commitCacheAudit);

        Assert.assertEquals(ctrl.getCache(false).getClass(), proxyClass);
        commitedEvt.tryWaitEvent();
        Assert.assertEquals(ctrl.getCache(false).getClass(), cacheClass);
        // Next call not clears cache. Cache remains in cut state.
        ctrl.onChanged(new ChangedObjectParameter(1L, Change.DELETE, null, null, null));
        Assert.assertEquals(ctrl.getCache(false).getClass(), cacheClass);
        // Next call clears cache.
        ctrl.onChanged(new ChangedObjectParameter(11L, Change.DELETE, null, null, null));
        Assert.assertEquals(ctrl.getCache(false).getClass(), proxyClass);
    }
}
