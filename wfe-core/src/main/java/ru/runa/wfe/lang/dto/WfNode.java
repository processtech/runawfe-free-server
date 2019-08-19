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
package ru.runa.wfe.lang.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.Transition;

/**
 * @since 4.3.0
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class WfNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String parentId;
    private String id;
    private NodeType type;
    private String name;
    private String description;
    private final List<WfTransition> arrivingTransitionIds = new ArrayList<>();
    private final List<WfTransition> leavingTransitionIds = new ArrayList<>();
    private boolean hasErrorEventHandler;
    private String swimlaneName;
    private final List<WfNode> boundaryEvents = new ArrayList<>();

    public WfNode() {
    }

    public WfNode(Node node) {
        this.parentId = node.getParentElement() instanceof ProcessDefinition ? null : node.getParentElement().getNodeId();
        this.id = node.getNodeId();
        this.type = node.getNodeType();
        this.name = node.getName();
        this.description = node.getDescription();
        for (Transition transition : node.getArrivingTransitions()) {
            arrivingTransitionIds.add(new WfTransition(transition));
        }
        for (Transition transition : node.getLeavingTransitions()) {
            leavingTransitionIds.add(new WfTransition(transition));
        }
        hasErrorEventHandler = node.hasErrorEventHandler();
        if (node instanceof InteractionNode) {
            TaskDefinition taskDefinition = (TaskDefinition) TypeConversionUtil.getListFirstValueOrNull(((InteractionNode) node).getTasks());
            if (taskDefinition != null && taskDefinition.getSwimlane() != null) {
                this.swimlaneName = taskDefinition.getSwimlane().getName();
            }
        }
        if (node instanceof BoundaryEventContainer) {
            for (BoundaryEvent event : ((BoundaryEventContainer) node).getBoundaryEvents()) {
                if (event instanceof Node) {
                    boundaryEvents.add(new WfNode((Node) event));
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfNode) {
            return Objects.equal(id, ((WfNode) obj).id);
        }
        return super.equals(obj);
    }

    public boolean hasErrorEventHandler() {
        return hasErrorEventHandler;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

}
