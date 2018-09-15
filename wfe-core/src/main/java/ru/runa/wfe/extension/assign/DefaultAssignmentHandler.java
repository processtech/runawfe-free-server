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
package ru.runa.wfe.extension.assign;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.user.Executor;

public class DefaultAssignmentHandler implements AssignmentHandler {
    protected final Log log = LogFactory.getLog(getClass());
    protected String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    protected List<? extends Executor> calculateExecutors(ExecutionContext executionContext, Assignable assignable) {
        return SwimlaneInitializerHelper.evaluate(configuration, executionContext.getVariableProvider());
    }

    @Override
    public void assign(ExecutionContext executionContext, Assignable assignable) {
        List<? extends Executor> executors = calculateExecutors(executionContext, assignable);
        AssignmentHelper.assign(executionContext, assignable, executors);
    }
}
