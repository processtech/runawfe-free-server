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
package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.collect.Maps;

/**
 * Send email.
 *
 * @author dofs[197@gmail.com]
 */
public class SendEmailActionHandler extends ActionHandlerBase {

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        // we parse config here because it changes its state during execution
        if (!EmailConfigParser.canParse(configuration)) {
            throw new Exception("Invalid configuration " + configuration);
        }
        EmailConfig config = EmailConfigParser.parse(configuration);
        try {
            Task task = executionContext.getTask();
            Interaction interaction = task != null ? executionContext.getProcessDefinition().getInteractionNotNull(task.getNodeId()) : null;
            Map<String, Object> map = Maps.newHashMap();
            map.put("task", task);
            map.put("interaction", interaction);
            map.put("process", executionContext.getProcess());
            VariableProvider emailVariableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
            EmailUtils.prepareMessage(UserHolder.get(), config, interaction, emailVariableProvider);
            EmailUtils.sendMessageRequest(config);
        } catch (Exception e) {
            if (config.isThrowErrorOnFailure()) {
                throw e;
            }
            log.error("unable to send email", e);
        }
    }
}
