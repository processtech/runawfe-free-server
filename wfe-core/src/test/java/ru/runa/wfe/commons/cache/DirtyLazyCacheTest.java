package ru.runa.wfe.commons.cache;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.ManualResetEvent;
import ru.runa.wfe.commons.cache.common.TestCacheIface;
import ru.runa.wfe.commons.cache.common.TestCacheStateMachineAudit;
import ru.runa.wfe.commons.cache.common.TestLazyCacheCtrl;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactory;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactoryCallback;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.DefaultStateContext;

public class DirtyLazyCacheTest {

    @Test()
    public void simpleGetCacheTest() {
        final ManualResetEvent initializationCompleteEvent = new ManualResetEvent();
        TestLazyCacheFactoryCallback factoryCallback = new TestLazyCacheFactoryCallback();
        final TestLazyCacheCtrl ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactory(factoryCallback), false);
        ctrl.getAudit().set_commitCacheAudit(new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {
            @Override
            protected void _stageSwitched(CacheState<TestCacheIface, DefaultStateContext> from, CacheState<TestCacheIface, DefaultStateContext> to) {
                initializationCompleteEvent.setEvent();
            }
        });
        ctrl.getCache(false);
        initializationCompleteEvent.tryWaitEvent();
        TestCacheIface cacheInstance = ctrl.getCurrentCacheInstance();
        Assert.assertNotNull(cacheInstance);
        Assert.assertSame(ctrl.getCache(false), cacheInstance);
        Assert.assertSame(ctrl.getCacheIfNotLocked(false), cacheInstance);
        ctrl.onChanged(new ChangedObjectParameter(1L, Change.DELETE, null, null, null, null));
        Assert.assertSame(ctrl.getCurrentCacheInstance(), cacheInstance);
        Assert.assertSame(ctrl.getCacheIfNotLocked(false), cacheInstance);
        Assert.assertSame(ctrl.getCache(false), cacheInstance);
    }
}
