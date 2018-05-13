package ru.runa.wfe.service.security;

import java.util.HashMap;
import org.springframework.util.Assert;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * The purpose of this class is to generalize JSP tags (and maybe not only them). It incapsulates SecuredObject polymorphic construction
 * (polymorphic by SecuredObjectType). Thanks to this I could implement generalized permission editor form.
 * <p>
 * This class supports permission system extensibility (adding new SecuredObjectType and Permission pseudo-enum items),
 * by using pluggable lambda loaders.
 *
 * @see SecuredObjectType
 * @see ru.runa.wfe.security.Permission
 * @see ru.runa.wfe.security.ApplicablePermissions
 * @see ru.runa.wfe.security.PermissionSubstitutions
 */
public class SecuredObjectFactory {

//    @FunctionalInterface
    interface Loader {
        SecuredObject apply(User user, Long id);
    }

    private static class SingletonLoader implements Loader {
        private SecuredObject instance;

        SingletonLoader(SecuredObject instance) {
            Assert.notNull(instance);
            this.instance = instance;
        }

        @Override
        public SecuredObject apply(User user, Long id) {
            if (id != null && id != 0) {
                throw new RuntimeException("id = " + id.toString() + ", expected 0 or null");
            }
            return instance;
        }
    }


    private static HashMap<SecuredObjectType, Loader> loaders = new HashMap<>();


    /**
     * Loads entity or returns singleton.
     *
     * @param id Boxed Long because it is met in many places in JSP tags. Null means 0, which means singleton.
     * @return Null if object does not exist.
     * @throws RuntimeException If type is unknown, or singleton is requested with non-empty id, or exception from object loader.
     */
    public static SecuredObject get(User user, SecuredObjectType type, Long id) {
        Loader loader = loaders.get(type);
        if (loader == null) {
            throw new RuntimeException("Loader does not exist for type " + type);
        }
        return loader.apply(user, id);
    }

    /**
     * Shortcut for <code>get(user, SecuredObjectType.valueOf(type), id)</code> for simpler web forms processing.
     */
    public static SecuredObject get(User user, String type, Long id) {
        return get(user, SecuredObjectType.valueOf(type), id);
    }


    public static void add(SecuredObjectType type, Loader loader) {
        if (loaders.put(type, loader) != null) {
            throw new RuntimeException("Loader already existed for type " + type);
        }
    }

    public static void add(SecuredSingleton singleton) {
        add(singleton.getSecuredObjectType(), new SingletonLoader(singleton));
    }

    static {
        // TODO Spring initialization crashes deep inside (refresh() call from SystemContext constructor) if I use anonymous function syntax.
        //      Spring 3.1.2, Icedtea 3.7.0 / Gentoo x64. Maybe error will go away someday, e.g. after spring upgrade?
        add(SecuredObjectType.ACTOR, new Loader() {
            @Override
            public SecuredObject apply(User user, Long id) {
                Executor o = Delegates.getExecutorService().getExecutor(user, id);
                Assert.isTrue(o == null || o instanceof Actor);
                return o;
            }
        });

        add(SecuredSingleton.BOTSTATIONS);
        add(SecuredSingleton.DATAFILE);
        add(SecuredSingleton.DEFINITIONS);

//        add(SecuredObjectType.DEFINITION, (user, id) -> Delegates.getDefinitionService().getProcessDefinition(user, id));
        add(SecuredObjectType.DEFINITION, new Loader() {
            @Override
            public SecuredObject apply(User user, Long id) {
                return Delegates.getDefinitionService().getProcessDefinition(user, id);
            }
        });

        add(SecuredSingleton.ERRORS);

        add(SecuredSingleton.EXECUTORS);

        add(SecuredObjectType.GROUP, new Loader() {
            @Override
            public SecuredObject apply(User user, Long id) {
                Executor o = Delegates.getExecutorService().getExecutor(user, id);
                Assert.isTrue(o == null || o instanceof Group);
                return o;
            }
        });

        add(SecuredSingleton.LOGS);

//        add(SecuredObjectType.PROCESS, (user, id) -> Delegates.getExecutionService().getProcess(user, id));
        add(SecuredObjectType.PROCESS, new Loader() {
            @Override
            public SecuredObject apply(User user, Long id) {
                return Delegates.getExecutionService().getProcess(user, id);
            }
        });

        add(SecuredSingleton.PROCESSES);
        add(SecuredSingleton.RELATIONS);
        add(SecuredSingleton.REPORTS);

//        add(SecuredObjectType.REPORT, (user, id) -> Delegates.getReportService().getReportDefinition(user, id));
        add(SecuredObjectType.REPORT, new Loader() {
            @Override
            public SecuredObject apply(User user, Long id) {
                return Delegates.getReportService().getReportDefinition(user, id);
            }
        });

        add(SecuredSingleton.SCRIPTS);
        add(SecuredSingleton.SUBSTITUTION_CRITERIAS);
        add(SecuredSingleton.SYSTEM);
    }
}
