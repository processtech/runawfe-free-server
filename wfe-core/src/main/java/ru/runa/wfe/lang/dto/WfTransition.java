package ru.runa.wfe.lang.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;

import ru.runa.wfe.lang.Transition;

/**
 * @since 4.3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfTransition implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String description;
    private String nodeFromId;
    private String nodeToId;
    private String color;

    public WfTransition() {
    }

    public WfTransition(Transition transition) {
        this.id = transition.getNodeId();
        this.name = transition.getName();
        this.description = transition.getDescription();
        this.nodeFromId = transition.getParent().getNodeId();
        this.nodeToId = transition.getTo().getNodeId();
        this.color = transition.getColor();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getNodeFromId() {
        return nodeFromId;
    }

    public String getNodeToId() {
        return nodeToId;
    }

    public String getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfTransition) {
            return Objects.equal(id, ((WfTransition) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

}
