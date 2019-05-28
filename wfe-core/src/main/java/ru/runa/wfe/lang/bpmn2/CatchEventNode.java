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
package ru.runa.wfe.lang.bpmn2;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableMapping;

public class CatchEventNode extends BaseMessageNode implements BoundaryEventContainer, BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();
    private Boolean boundaryEventInterrupting;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    public void cancelBoundaryEvent(Token token) {
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting(ExecutionContext executionContext) {
        if (getParentElement() instanceof BaseTaskNode) {
            String swimlaneName = ((BaseTaskNode) getParentElement()).getFirstTaskNotNull().getSwimlane().getName();
            for (VariableMapping variableMapping : getVariableMappings()) {
                if (!variableMapping.isPropertySelector()) {
                    if (Objects.equals(swimlaneName, variableMapping.getName())) {
                        return TaskCompletionInfo.createForSignal((Executor) executionContext.getVariableValue(swimlaneName));
                    }
                }
            }
        }
        return TaskCompletionInfo.createForSignal(null);
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        super.leave(executionContext, transition);
        executionContext.getToken().setMessageSelector(null);
    }

}
