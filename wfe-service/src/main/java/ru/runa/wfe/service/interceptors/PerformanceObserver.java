package ru.runa.wfe.service.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoringInterceptor;

@CommonsLog
public class PerformanceObserver extends MonitoringInterceptor {
    private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        long startTime = System.currentTimeMillis();
        Object result = ic.proceed();
        long jobTime = System.currentTimeMillis() - startTime;
        if (jobTime > 1000) {
            log.info(jobTime + " ms: " + DebugUtils.getDebugString(ic, false));
        } else if (log.isDebugEnabled()) {
            log.debug(jobTime + " ms: " + DebugUtils.getDebugString(ic, false));
        }
        return result;
    }
}
