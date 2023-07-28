package ru.runa.wf.web.html;

import java.util.Map;
import java.util.WeakHashMap;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.common.web.html.TdBuilder.Env;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

/**
 * Creates {@link SecuredObject} to check permissions on {@link Actor}, executed
 * action.
 */
@CommonsLog
public class ExecutorExtractor extends SecuredObjectExtractor {
    private static final long serialVersionUID = 1L;
    private final Map<Long, SecuredObject> cache = new WeakHashMap<>();

    @Override
    public SecuredObject getSecuredObject(Object o, Env env) {
        Long id = (Long) o;
        try {
            if (!cache.containsKey(id)) {
                cache.put(id, Delegates.getExecutorService().getExecutor(env.getUser(), id));
            }
            return cache.get(id);
        } catch (Exception e) {
            log.error("Can't load executor for system log with id " + id, e);
        }
        return null;
    }
}
