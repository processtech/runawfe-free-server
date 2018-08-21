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

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

/**
 * Interface for process definition graph elements presentation components.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeGraphElement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nodeId;
    private NodeType nodeType;
    private String name;
    private int[] graphConstraints;

    /**
     * Process logs concerning this node, can be <code>null</code> if not entered yet.
     */
    private List<? extends BaseProcessLog> data;
    private String label;

    /**
     * Initializes data.
     */
    public void initialize(Node node, int[] graphConstraints) {
        this.graphConstraints = graphConstraints;
        this.nodeId = node.getNodeId();
        this.nodeType = node.getNodeType();
        this.name = node.getName();
    }

    /**
     * Graph element ID.
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Graph element name.
     */
    public String getName() {
        return name;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Graph element position constraints. For rectangles represents upper-left and bottom-right corners.
     */
    public int[] getGraphConstraints() {
        return graphConstraints;
    }

    /**
     * Some additional data, assigned to graph element.
     */
    public List<? extends BaseProcessLog> getData() {
        return data;
    }

    /**
     * Some additional data, assigned to graph element.
     */
    public void setData(List<? extends BaseProcessLog> data) {
        this.data = data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return nodeType + ": " + nodeId;
    }
}
