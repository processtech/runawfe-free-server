package ru.runa.wfe.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.dto.WfTransition;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Contains data for user interaction with process execution.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Interaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nodeId;
    private String name;
    private String description;
    private String type;
    private byte[] formData;
    private byte[] validationData;
    private boolean useJSValidation;
    private byte[] processScriptData;
    private byte[] formScriptData;
    private byte[] cssData;
    private byte[] templateData;
    private final List<String> requiredVariableNames = Lists.newArrayList();
    @XmlTransient
    private final HashMap<String, VariableDefinition> variableDefinitions = Maps.newHashMap();
    @XmlTransient
    private final HashMap<String, Object> defaultVariableValues = Maps.newHashMap();
    private final List<WfTransition> outputTransitions = Lists.newArrayList();
    private boolean taskButtonLabelBySingleTransitionName;

    protected Interaction() {
    }

    public Interaction(Node node, String type, byte[] formData, byte[] validationData, boolean useJSValidation, byte[] processScriptData,
            byte[] formScriptData, byte[] cssData, byte[] templateData) {
        this.nodeId = node.getNodeId();
        this.name = node.getName();
        this.description = node.getDescription() != null ? node.getDescription() : "";
        this.type = type;
        this.formData = formData;
        this.validationData = validationData;
        this.useJSValidation = useJSValidation;
        this.processScriptData = processScriptData;
        this.formScriptData = formScriptData;
        this.cssData = cssData;
        this.templateData = templateData;
        for (Transition transition : node.getLeavingTransitions()) {
            if (!transition.isTimerTransition()) {
                outputTransitions.add(new WfTransition(transition));
            }
        }
        if (outputTransitions.size() == 1 && node instanceof InteractionNode) {
            Boolean nodeExecutionButton = ((InteractionNode) node).getFirstTaskNotNull().isTaskButtonLabelBySingleTransitionName();
            Boolean processExecutionButton = node.getParsedProcessDefinition().isTaskButtonLabelBySingleTransitionName();
            this.taskButtonLabelBySingleTransitionName = nodeExecutionButton == null ? processExecutionButton != null && processExecutionButton
                    : nodeExecutionButton;
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getFormData() {
        return formData;
    }

    public boolean hasForm() {
        return formData != null;
    }

    public String getType() {
        if (type == null) {
            return "ftl";
        }
        return type;
    }

    public boolean isUseJSValidation() {
        return useJSValidation && validationData != null;
    }

    public byte[] getValidationData() {
        return validationData;
    }

    public byte[] getProcessScriptData() {
        return processScriptData;
    }

    public byte[] getFormScriptData() {
        return formScriptData;
    }

    public byte[] getCssData() {
        return cssData;
    }

    public byte[] getTemplateData() {
        return templateData;
    }

    public List<String> getRequiredVariableNames() {
        return requiredVariableNames;
    }

    public Map<String, VariableDefinition> getVariables() {
        return variableDefinitions;
    }

    public Map<String, Object> getDefaultVariableValues() {
        return defaultVariableValues;
    }

    public List<WfTransition> getOutputTransitions() {
        return outputTransitions;
    }

    public boolean isTaskButtonLabelBySingleTransitionName() {
        return taskButtonLabelBySingleTransitionName;
    }

}
