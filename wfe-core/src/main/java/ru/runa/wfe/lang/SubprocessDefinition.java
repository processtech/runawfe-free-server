package ru.runa.wfe.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.bpmn2.EndToken;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

public class SubprocessDefinition extends ProcessDefinition {
    private static final long serialVersionUID = 1L;
    private ProcessDefinition parentProcessDefinition;

    protected SubprocessDefinition() {
    }

    public SubprocessDefinition(ProcessDefinition parentProcessDefinition) {
        super(parentProcessDefinition.getDeploymentVersion().createCopyWithDeployment());
        this.parentProcessDefinition = parentProcessDefinition;
    }

    public ProcessDefinition getParentProcessDefinition() {
        return parentProcessDefinition;
    }

    @Override
    public void setName(String name) {
        deployment.setName(name);
    }

    @Override
    public Map<String, SubprocessDefinition> getEmbeddedSubprocesses() {
        return parentProcessDefinition.getEmbeddedSubprocesses();
    }

    @Override
    public void validate() {
        super.validate();
        if (getStartStateNotNull().getLeavingTransitions().size() != 1) {
            throw new InternalApplicationException("Start state in embedded subprocess should have 1 leaving transition");
        }
        int endNodesCount = 0;
        for (Node node : nodes) {
            if (node instanceof EndNode) {
                throw new InternalApplicationException("In embedded subprocess it is not allowed end state");
            }
            if (node instanceof EndToken || node instanceof ru.runa.wfe.lang.jpdl.EndToken) {
                throw new RuntimeException("There should be EmbeddedSubprocessEndNode");
            }
            if (node instanceof EmbeddedSubprocessEndNode) {
                endNodesCount++;
            }
        }
        if (endNodesCount == 0) {
            throw new RuntimeException("In embedded subprocess there are should be at least 1 end node");
        }
    }

    @Override
    public EmbeddedSubprocessStartNode getStartStateNotNull() {
        return (EmbeddedSubprocessStartNode) super.getStartStateNotNull();
    }

    public List<EmbeddedSubprocessEndNode> getEndNodes() {
        val list = new ArrayList<EmbeddedSubprocessEndNode>();
        for (Node node : nodes) {
            if (node instanceof EmbeddedSubprocessEndNode) {
                list.add((EmbeddedSubprocessEndNode) node);
            }
        }
        return list;
    }

    @Override
    public byte[] getGraphImageBytesNotNull() {
        byte[] graphBytes = processDefinition.getFileData(getNodeId() + "." + IFileDataProvider.GRAPH_IMAGE_NEW_FILE_NAME);
        if (graphBytes == null) {
            graphBytes = processDefinition.getFileData(getNodeId() + "." + IFileDataProvider.GRAPH_IMAGE_OLD2_FILE_NAME);
        }
        if (graphBytes == null) {
            graphBytes = processDefinition.getFileData(getNodeId() + "." + IFileDataProvider.GRAPH_IMAGE_OLD1_FILE_NAME);
        }
        if (graphBytes == null) {
            throw new InternalApplicationException("No process graph image file found in embedded process definition");
        }
        return graphBytes;
    }

    @Override
    public void addInteraction(String name, Interaction interaction) {
        parentProcessDefinition.addInteraction(name, interaction);
    }

    @Override
    public UserType getUserType(String name) {
        return parentProcessDefinition.getUserType(name);
    }

    @Override
    public UserType getUserTypeNotNull(String name) {
        return parentProcessDefinition.getUserTypeNotNull(name);
    }

    @Override
    public VariableDefinition getVariable(String name, boolean searchInSwimlanes) {
        return parentProcessDefinition.getVariable(name, searchInSwimlanes);
    }

    @Override
    public VariableDefinition getVariableNotNull(String name, boolean searchInSwimlanes) {
        return parentProcessDefinition.getVariableNotNull(name, searchInSwimlanes);
    }

    @Override
    public List<VariableDefinition> getVariables() {
        return parentProcessDefinition.getVariables();
    }

    @Override
    public Interaction getInteractionNotNull(String nodeId) {
        return parentProcessDefinition.getInteractionNotNull(nodeId);
    }

    @Override
    public byte[] getFileData(String fileName) {
        return parentProcessDefinition.getFileData(fileName);
    }

    @Override
    public byte[] getFileDataNotNull(String fileName) {
        return parentProcessDefinition.getFileDataNotNull(fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes() {
        return parentProcessDefinition.getSwimlanes();
    }

    @Override
    public SwimlaneDefinition getSwimlane(String swimlaneName) {
        return parentProcessDefinition.getSwimlane(swimlaneName);
    }

    @Override
    public SwimlaneDefinition getSwimlaneNotNull(String swimlaneName) {
        return parentProcessDefinition.getSwimlaneNotNull(swimlaneName);
    }

}
