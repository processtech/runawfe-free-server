package ru.runa.wfe.service.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;

public class CacheReloader {

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            CachingLogic.setEnabled(false);
            Object result = ic.proceed();
            return result;
        } finally {
            CachingLogic.setEnabled(true);
            CachingLogic.resetAllCaches();
        }
    }

}
