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
package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Cancels process by id.
 *
 * @author dofs
 * @since 4.3.0
 */
public class CancelProcessTaskHandler extends TaskHandlerBase {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        Long processId = paramsDef.getInputParamValueNotNull("processId", variableProvider);
        Delegates.getExecutionService().cancelProcess(user, processId);
        return null;
    }

}
