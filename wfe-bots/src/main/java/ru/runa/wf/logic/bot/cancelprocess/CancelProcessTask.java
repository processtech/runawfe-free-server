package ru.runa.wf.logic.bot.cancelprocess;

import java.util.Map;

/**
 * Created on 22.04.2005
 * 
 */
public class CancelProcessTask {
    private final String processIdVariableName;
    private final Map<String, String> databaseTaskMap;

    /**
     * 
     * @param processIdVariableName
     *            process variable containing id of process to cancel
     * @param databaseTaskMap
     *            contains name of process as key and database task as value
     */
    public CancelProcessTask(String processIdVariableName, Map<String, String> databaseTaskMap) {
        this.processIdVariableName = processIdVariableName;
        this.databaseTaskMap = databaseTaskMap;

    }

    public String getProcessIdVariableName() {
        return processIdVariableName;
    }

    public Map<String, String> getDatabaseTaskMap() {
        return databaseTaskMap;
    }
}
