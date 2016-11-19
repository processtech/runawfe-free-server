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
package ru.runa.wfe.user;

import java.util.Set;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that some processes are depends from this {@link Executor}.
 * 
 * @since 4.0.5
 */
public class ExecutorParticipatesInProcessesException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String executorName;
    private final String idsInfo;

    public ExecutorParticipatesInProcessesException(String executorName, Set<Number> processIds) {
        super(executorName + " " + processIds);
        this.executorName = executorName;
        this.idsInfo = processIds.size() > 100 ? " > 100" : processIds.toString();
    }

    public String getExecutorName() {
        return executorName;
    }
    
    public String getIdsInfo() {
        return idsInfo;
    }
}
