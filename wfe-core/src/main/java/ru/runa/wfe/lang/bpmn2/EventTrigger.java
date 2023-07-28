package ru.runa.wfe.lang.bpmn2;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import ru.runa.wfe.var.VariableMapping;

// Seems like EventTrigger should be combined with BaseMessageNode
@Data
public class EventTrigger implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private MessageEventType eventType;
    private final List<VariableMapping> variableMappings = Lists.newArrayList();
    private String ttlDuration;

    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
    }

}
