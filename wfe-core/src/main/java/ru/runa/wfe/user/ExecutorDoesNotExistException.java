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

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that {@link Executor} does not exist in DB.
 */
public class ExecutorDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = -9096157439932169776L;

    private final String executorName;
    private final Class<? extends Executor> executorClass;

    public ExecutorDoesNotExistException(String executorName, Class<? extends Executor> executorClass) {
        super("Executor " + executorName + " of class " + executorClass.getName() + " does not exist");
        this.executorName = executorName;
        this.executorClass = executorClass;
    }

    public ExecutorDoesNotExistException(Long executorId, Class<? extends Executor> executorClass) {
        this("with id = " + executorId, executorClass);
    }

    public String getExecutorName() {
        return executorName;
    }

    public Class<? extends Executor> getExecutorClass() {
        return executorClass;
    }
}
