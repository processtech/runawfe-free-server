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
package ru.runa.wf.logic.bot.startprocess;

public class StartProcessTask {
    private final String name;
    private final StartProcessVariableMapping[] startProcessVariableMappings;
    private String startedProcessIdValueName;

    /**
     * @param name
     *            process name
     * @param variables
     *            {@link StartProcessVariableMapping}
     */
    public StartProcessTask(String name, StartProcessVariableMapping[] variables) {
        this.name = name;
        this.startProcessVariableMappings = variables.clone();
    }

    public StartProcessTask(String name, StartProcessVariableMapping[] variables, String startedProcessIdName) {
        this(name, variables);
        startedProcessIdValueName = startedProcessIdName;
    }

    public String getName() {
        return name;
    }

    public int getVariablesCount() {
        return startProcessVariableMappings.length;
    }

    public StartProcessVariableMapping getStartProcessVariableMapping(int i) {
        return startProcessVariableMappings[i];
    }

    public String getStartedProcessIdValueName() {
        return startedProcessIdValueName;
    }
}
