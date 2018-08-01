package ru.runa.wfe.service.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;

public class CacheReloader {

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            CachingLogic.disableChangesTracking();
            return ic.proceed();
        } finally {
            CachingLogic.enableChangesTracking();
            CachingLogic.resetAllCaches();
        }
    }
}
