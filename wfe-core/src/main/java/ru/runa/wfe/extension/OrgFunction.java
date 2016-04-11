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
package ru.runa.wfe.extension;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

/**
 * General abstraction for organization function
 * 
 * @since 2.1
 */
public abstract class OrgFunction {
    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    protected ExecutorDAO executorDAO;

    /**
     * @param parameters
     *            - array of parameters
     * @return id's of executors
     * @throws OrgFunctionException
     */
    public abstract List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException;

}
