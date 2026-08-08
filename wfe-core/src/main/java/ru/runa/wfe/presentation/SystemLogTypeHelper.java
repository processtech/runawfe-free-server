package ru.runa.wfe.presentation;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.audit.ArchivedProcessDeleteLog;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.audit.ProcessDeleteLog;
import ru.runa.wfe.audit.ProcessLogsCleanLog;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.ExecutorCreateLog;
import ru.runa.wfe.audit.ExecutorUpdateLog;
import ru.runa.wfe.audit.ExecutorDeleteLog;
import ru.runa.wfe.audit.ExecutorGroupAddLog;
import ru.runa.wfe.audit.ExecutorGroupRemoveLog;

/**
 * Helper class to map {@link SystemLog} descriminator value to display name.
 */
public final class SystemLogTypeHelper {
    /**
     * {@link Map} from enumerated value (type descriminator value) to property
     * display name (struts property).
     */
    private static final Map<String, String> enumerationValues = new HashMap<String, String>();

    /**
     * {@link Map} from type to property display name (struts property).
     */
    private static final Map<Class<? extends SystemLog>, String> typeValues = Maps.newHashMap();

    static {
        addType(ProcessDefinitionDeleteLog.class, "PDDel", "history.system.type.process_definition_delete");
        addType(ProcessDeleteLog.class, "PIDel", "history.system.type.process_delete");
        addType(ProcessLogsCleanLog.class, "PLDel", "history.system.type.process_logs_cleaned");
        addType(ArchivedProcessDeleteLog.class, "ArPIDel", "history.system.type.archived_process_delete");
	addType(ExecutorCreateLog.class, "ExecutorCreate", "history.system.type.executor_create");
	addType(ExecutorUpdateLog.class, "ExecutorUpdate", "history.system.type.executor_update");
	addType(ExecutorDeleteLog.class, "ExecutorDelete", "history.system.type.executor_delete");
	addType(ExecutorGroupAddLog.class, "ExecutorGroupAdd", "history.system.type.executor_group_add");
	addType(ExecutorGroupRemoveLog.class, "ExecutorGroupRemove", "history.system.type.executor_group_remove");
    }

    /**
     * {@link Map} from enumerated value ({@link SystemLog} discriminator value)
     * to property display name (struts property).
     */
    public static Map<String, String> getValues() {
        return new HashMap<String, String>(enumerationValues);
    }

    /**
     * {@link Map} from type to property display name (struts property).
     */
    public static Map<Class<? extends SystemLog>, String> getClasses() {
        return typeValues;
    }

    /**
     * Add class and it's descriminator value to maps.
     * 
     * @param clazz
     *            Class, inherited from {@link SystemLog}.
     * @param discriminator
     *            Class discriminator value.
     * @param displayProperty
     *            Struts property, to display for this class.
     */
    private static void addType(Class<? extends SystemLog> clazz, String discriminator, String displayProperty) {
        enumerationValues.put(discriminator, displayProperty);
        typeValues.put(clazz, displayProperty);
    }
}
