package ru.runa.wfe.execution.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.NonNull;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.user.Executor;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfSwimlane implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Executor executor;
    /**
     * for web services
     */
    public WfSwimlane() {
    }

    public WfSwimlane(@NonNull Swimlane swimlane, Executor assignedExecutor) {
        this.id = swimlane.getId();
        this.name = swimlane.getName();
        this.executor = assignedExecutor;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Executor getExecutor() {
        return executor;
    }

}
