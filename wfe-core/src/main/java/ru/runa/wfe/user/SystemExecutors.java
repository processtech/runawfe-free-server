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
