package ru.runa.wfe.rest.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class WfeTaskNodeInteraction {
    private String nodeId;
    private String name;
    private String description;
    private String type;
    private String swimlaneName;
    private byte[] formData;
    private byte[] validationData;
    private byte[] processScriptData;
    private byte[] formScriptData;
    private byte[] cssData;
    private byte[] templateData;
    private Map<String, WfeVariableDefinition> variableDefinitions;
    private List<String> requiredVariableNames;
    private List<WfeTransition> outputTransitions;
    private boolean taskButtonLabelBySingleTransitionName;
}
