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
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.task.logic.TaskAssigner;

/**
 * Try to assign unassigned tasks.
 *
 * @author Konstantinov Aleksey
 */
public class AssignStalledTasksExecutor extends TransactionalExecutor {
    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private TaskDAO taskDAO;

    @Override
    protected void doExecuteInTransaction() {
        List<Task> unassignedTasks = taskDAO.findUnassignedTasks();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            taskAssigner.assignTask(unassignedTask, false);
        }
    }

}
