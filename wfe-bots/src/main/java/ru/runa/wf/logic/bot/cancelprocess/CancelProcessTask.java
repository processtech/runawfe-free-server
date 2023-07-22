/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
