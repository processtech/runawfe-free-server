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
package ru.runa.wfe.lang;

import java.util.Map;

import javax.jms.ObjectMessage;

import ru.runa.wfe.audit.SendMessageLog;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.collect.Maps;

public class SendMessageNode extends BaseMessageNode {
    private static final long serialVersionUID = 1L;

    private String ttlDuration;

    @Override
    public NodeType getNodeType() {
        return NodeType.SEND_MESSAGE;
    }

    public String getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(String ttlDuration) {
        this.ttlDuration = ttlDuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(Variables.CURRENT_PROCESS_ID, executionContext.getProcess().getId());
        // back compatibility
        variables.put("currentInstanceId", executionContext.getProcess().getId());
        variables.put(Variables.CURRENT_PROCESS_DEFINITION_NAME, executionContext.getProcessDefinition().getName());
        variables.put(Variables.CURRENT_NODE_NAME, executionContext.getNode().getName());
        variables.put(Variables.CURRENT_NODE_ID, executionContext.getNode().getNodeId());
        MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(variables, executionContext.getVariableProvider());
        long ttl = ExpressionEvaluator.evaluateDuration(executionContext.getVariableProvider(), ttlDuration);
        ObjectMessage message = Utils.sendBpmnMessage(variableMappings, variableProvider, ttl);
        String log = Utils.toString(message, true);
        executionContext.addLog(new SendMessageLog(this, log));
        leave(executionContext);
    }

}
