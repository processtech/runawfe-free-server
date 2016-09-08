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

import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

/**
 * Represents an task state graph element.
 */
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

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        swimlaneName = taskDefinition.getSwimlane().getName();
        minimized = node.isGraphMinimizedView();
    }

    /**
     * Flag, equals true, if state is collapsed; false otherwise.
     */
    public boolean isMinimized() {
        return minimized;
    }

    /**
     * Swimlane name of this task element.
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

}
