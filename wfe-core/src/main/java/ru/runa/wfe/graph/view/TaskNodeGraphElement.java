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
package ru.runa.wfe.graph.view;

import com.google.common.base.Objects;
import lombok.Getter;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

/**
 * Represents an task state graph element.
 */
@Getter
public class TaskNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    /**
     * Flag, equals true, if task state is minimized; false otherwise.
     */
    private boolean minimized;

    /**
     * Swimlane name of this task element.
     */
    private String swimlaneName;

    private String botTaskHandlerClassName = "";

    private String botTaskHandlerConfiguration = "";

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        minimized = node.isGraphMinimizedView();
        if (null != taskDefinition.getSwimlane()) {
            swimlaneName = taskDefinition.getSwimlane().getName();
        }
    }

    public void initializeBotTaskInfo(String botName, String botTaskHandlerClassName, String botTaskHandlerConfiguration) {
        if (!Objects.equal(swimlaneName, botName)) {
            swimlaneName += " / " + botName;
        }
        this.botTaskHandlerClassName = botTaskHandlerClassName;
        this.botTaskHandlerConfiguration = botTaskHandlerConfiguration;
    }

}
