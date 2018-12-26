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
package ru.runa.wfe.lang.jpdl;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class ReceiveMessageNode extends BaseMessageNode  implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
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

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

}
