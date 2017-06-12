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
package ru.runa.wfe.job.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.user.logic.ExecutorLogic;

/**
 * Executor for removal of unused temporary groups.
 * 
 * @author Pavel Perminov
 */
public class RemoveUnusedTemporaryGroupsExecutor extends TransactionalExecutor {
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private ProcessDAO processDAO;

    @Override
    protected void doExecuteInTransaction() {
        List<TemporaryGroup> groups = executorDAO.getTemporaryGroupsForEndedProcesses();
        log.debug("Starting with " + groups.size() + " groups");
        int count = 0;
        for (TemporaryGroup group : groups) {
            if (processDAO.getDependentProcessIds(group).isEmpty()) {
                log.debug(group + " is not referenced anymore and will be removed");
                executorLogic.remove(group);
                count++;
            }
        }
        log.debug("Removed " + count + " groups");
    }

}
