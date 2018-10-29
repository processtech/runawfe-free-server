package ru.runa.wfe.user;

/**
 * Common declarations for system executors. System executors is not accessible by users
 * and used for special system purposes. 
 */
public final class SystemExecutors {

    /**
     * Prefix for system executors name.
     */
    public static final String SYSTEM_EXECUTORS_PREFIX = "SystemExecutor:";

    /**
     * Name of executor, used to set special permission for executor, which start process instance.
     */
    public static final String PROCESS_STARTER_NAME = SYSTEM_EXECUTORS_PREFIX + "ProcessStarter";

    /**
     * Description of executor, used to set special permission for executor, which start process instance.
     */
    public static final String PROCESS_STARTER_DESCRIPTION = "Executor, which start process instance, got permission on process instance according to this executor permissions";
}
