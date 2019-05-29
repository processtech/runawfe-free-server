package ru.runa.wfe.execution.dto;

import com.google.common.base.Objects;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.NonNull;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.user.Executor;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfSwimlane implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private SwimlaneDefinition definition;
    private Executor executor;

    /**
     * for web services
     */
    public WfSwimlane() {
    }

    public WfSwimlane(@NonNull SwimlaneDefinition definition, Swimlane swimlane, Executor assignedExecutor) {
        this.definition = definition;
        this.id = swimlane != null ? swimlane.getId() : null;
        this.executor = assignedExecutor;
    }
    
    public SwimlaneDefinition getDefinition() {
        return definition;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(definition.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfSwimlane) {
            return Objects.equal(definition.getName(), ((WfSwimlane) obj).definition.getName());
        }
        return super.equals(obj);
    }

}
