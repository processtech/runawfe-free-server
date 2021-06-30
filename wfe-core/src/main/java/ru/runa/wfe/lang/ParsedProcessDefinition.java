package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class ParsedProcessDefinition extends GraphElement implements FileDataProvider {
    private static final long serialVersionUID = 1L;
    // TODO remove association for efficiency
    protected ProcessDefinition processDefinition;
    protected ProcessDefinitionVersion processDefinitionVersion;
    protected Map<String, byte[]> processFiles = Maps.newHashMap();
    protected StartNode startNode;
    protected final List<Node> nodesList = Lists.newArrayList();
    protected final Map<String, Node> nodesMap = Maps.newHashMap();
    protected final List<SwimlaneDefinition> swimlaneDefinitions = Lists.newArrayList();
    protected final Map<String, SwimlaneDefinition> swimlaneDefinitionsMap = Maps.newHashMap();
    protected final Map<String, Interaction> interactions = Maps.newHashMap();
    protected final Map<String, UserType> userTypes = Maps.newHashMap();
    protected final List<VariableDefinition> variables = Lists.newArrayList();
    protected final Map<String, VariableDefinition> variablesMap = Maps.newHashMap();
    protected ProcessDefinitionAccessType accessType = ProcessDefinitionAccessType.Process;
    protected Map<String, ParsedSubprocessDefinition> embeddedSubprocesses = Maps.newHashMap();
    private Boolean nodeAsyncExecution;
    private boolean graphActionsEnabled;
    private final List<ProcessDefinitionChange> changes = Lists.newArrayList();

    protected ParsedProcessDefinition() {
    }

    public ParsedProcessDefinition(@NonNull ProcessDefinition d, @NonNull ProcessDefinitionVersion dv) {
        this.processDefinition = d;
        this.processDefinitionVersion = dv;
        parsedProcessDefinition = this;
    }

    public ParsedProcessDefinition(ProcessDefinitionWithVersion dwv) {
        this(dwv.processDefinition, dwv.processDefinitionVersion);
    }

    /**
     * @return processDefinitionVersion.id
     */
    public Long getId() {
        return processDefinitionVersion.getId();
    }

    /**
     * @return processDefinition.name
     */
    @Override
    public String getName() {
        return processDefinition.getName();
    }

    @Override
    public void setName(String name) {
        if (processDefinition.getName() != null) {
            // don't override name from database
            return;
        }
        processDefinition.setName(name);
    }

    @Override
    public String getDescription() {
        return processDefinition.getDescription();
    }

    @Override
    public void setDescription(String description) {
        processDefinition.setDescription(description);
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public ProcessDefinitionVersion getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public ProcessDefinitionAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(ProcessDefinitionAccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * add a file to this definition.
     */
    public void addFile(String name, byte[] bytes) {
        processFiles.put(name, bytes);
    }

    public void addInteraction(String name, Interaction interaction) {
        interactions.put(name, interaction);
    }

    public void addUserType(UserType userType) {
        userTypes.put(userType.getName(), userType);
    }

    public void addVariable(VariableDefinition variableDefinition) {
        variablesMap.put(variableDefinition.getScriptingName(), variableDefinition);
        variablesMap.put(variableDefinition.getName(), variableDefinition);
        variables.add(variableDefinition);
    }

    public VariableDefinition getVariable(String name, boolean searchInSwimlanes) {
        VariableDefinition variableDefinition = variablesMap.get(name);
        if (variableDefinition != null) {
            return variableDefinition;
        }
        if (searchInSwimlanes) {
            SwimlaneDefinition swimlaneDefinition = getSwimlane(name);
            if (swimlaneDefinition != null) {
                return swimlaneDefinition.toVariableDefinition();
            }
        }
        if (name.endsWith(VariableFormatContainer.SIZE_SUFFIX)) {
            String listVariableName = name.substring(0, name.length() - VariableFormatContainer.SIZE_SUFFIX.length());
            VariableDefinition listVariableDefinition = getVariable(listVariableName, false);
            if (listVariableDefinition != null) {
                return new VariableDefinition(name, null, LongFormat.class.getName(), null);
            }
            log.debug("Unable to build list (map) size variable by name '" + name + "'");
            return null;
        }
        return buildVariable(name);
    }

    private VariableDefinition buildVariable(String variableName) {
        int dotIndex = variableName.indexOf(UserType.DELIM);
        if (dotIndex != -1) {
            String parentName = variableName.substring(0, dotIndex);
            String remainderName = variableName.substring(dotIndex + 1);
            int componentStartIndex = parentName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
            String parentVariableName = componentStartIndex != -1 ? parentName.substring(0, componentStartIndex) : parentName;
            VariableDefinition parentVariableDefinition = variablesMap.get(parentVariableName);
            VariableDefinition parentUserTypeVariableDefinition = null;
            if (parentVariableDefinition != null && parentVariableDefinition.isUserType()) {
                parentUserTypeVariableDefinition = parentVariableDefinition;
            }
            if (parentVariableDefinition != null && componentStartIndex != -1) {
                if (ListFormat.class.getName().equals(parentVariableDefinition.getFormatClassName())) {
                    String appendix = parentName.substring(componentStartIndex);
                    String format = parentVariableDefinition.getFormatComponentClassNames()[0];
                    parentUserTypeVariableDefinition = new VariableDefinition(parentVariableDefinition.getName() + appendix,
                            parentVariableDefinition.getScriptingName() + appendix, format, getUserType(format));
                    parentUserTypeVariableDefinition.initComponentUserTypes(this);
                } else if (MapFormat.class.getName().equals(parentVariableDefinition.getFormatClassName())) {
                    String appendix = parentName.substring(componentStartIndex);
                    String userTypeName;
                    if (appendix.contains(":k")) {
                        userTypeName = parentVariableDefinition.getFormatComponentClassNames()[0];
                    } else {
                        userTypeName = parentVariableDefinition.getFormatComponentClassNames()[1];
                    }
                    parentUserTypeVariableDefinition = new VariableDefinition(parentVariableDefinition.getName() + appendix,
                            parentVariableDefinition.getScriptingName() + appendix, parentVariableDefinition.getFormat(), getUserType(userTypeName));
                    parentUserTypeVariableDefinition.initComponentUserTypes(this);
                }
            }
            if (parentUserTypeVariableDefinition != null) {
                VariableDefinition attributeDefinition = parentUserTypeVariableDefinition.getUserType().getAttributeExpanded(remainderName);
                if (attributeDefinition != null) {
                    String name = parentUserTypeVariableDefinition.getName() + UserType.DELIM + attributeDefinition.getName();
                    VariableDefinition variableDefinition = new VariableDefinition(name, null, attributeDefinition);
                    return variableDefinition;
                }
            }
            log.debug("Unable to build syntetic variable by name '" + variableName + "', last checked " + parentVariableDefinition + " with "
                    + remainderName);
            return null;
        }
        int componentStartIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
        if (componentStartIndex != -1) {
            String containerVariableName = variableName.substring(0, componentStartIndex);
            VariableDefinition containerVariableDefinition = variablesMap.get(containerVariableName);
            if (containerVariableDefinition == null) {
                log.debug("Unable to build syntetic container variable by name '" + variableName + "'");
                return null;
            }
            if (containerVariableDefinition.getFormatComponentClassNames().length == 0) {
                throw new InternalApplicationException("Not a list variable: " + containerVariableDefinition.getName());
            }
            String format = containerVariableDefinition.getFormatComponentClassNames()[0];
            VariableDefinition variableDefinition = new VariableDefinition(variableName, null, format, getUserType(format));
            variableDefinition.initComponentUserTypes(this);
            return variableDefinition;
        }
        return null;
    }

    public VariableDefinition getVariableNotNull(String name, boolean searchInSwimlanes) {
        VariableDefinition variableDefinition = getVariable(name, searchInSwimlanes);
        if (variableDefinition == null) {
            throw new InternalApplicationException("variable '" + name + "' not found in " + this);
        }
        return variableDefinition;
    }

    public UserType getUserType(String name) {
        return userTypes.get(name);
    }

    public UserType getUserTypeNotNull(String name) {
        UserType userType = getUserType(name);
        if (userType == null) {
            throw new InternalApplicationException("UserType '" + name + "' not found");
        }
        return userType;
    }

    public List<UserType> getUserTypes() {
        return Lists.newArrayList(userTypes.values());
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public Interaction getInteractionNotNull(String nodeId) {
        Interaction interaction = interactions.get(nodeId);
        if (interaction == null) {
            InteractionNode node = (InteractionNode) getNodeNotNull(nodeId);
            interaction = new Interaction(node, null, null, null, false, null, null, null, null);
        }
        return interaction;
    }

    public Map<String, byte[]> getProcessFiles() {
        return processFiles;
    }

    @Override
    public byte[] getFileData(String fileName) {
        Preconditions.checkNotNull(fileName, "fileName");
        return processFiles.get(fileName);
    }

    @Override
    public byte[] getFileDataNotNull(String fileName) {
        byte[] bytes = getFileData(fileName);
        if (bytes == null) {
            throw new DefinitionFileDoesNotExistException(fileName);
        }
        return bytes;
    }

    public byte[] getGraphImageBytesNotNull() {
        byte[] graphBytes = parsedProcessDefinition.getFileData(FileDataProvider.GRAPH_IMAGE_NEW_FILE_NAME);
        if (graphBytes == null) {
            graphBytes = parsedProcessDefinition.getFileData(FileDataProvider.GRAPH_IMAGE_OLD2_FILE_NAME);
        }
        if (graphBytes == null) {
            graphBytes = parsedProcessDefinition.getFileData(FileDataProvider.GRAPH_IMAGE_OLD1_FILE_NAME);
        }
        if (graphBytes == null) {
            throw new InternalApplicationException("No process graph image file found in process definition");
        }
        return graphBytes;
    }

    public Map<String, Object> getDefaultVariableValues() {
        Map<String, Object> result = new HashMap<>();
        for (VariableDefinition variableDefinition : variables) {
            if (variableDefinition.getDefaultValue() != null) {
                result.put(variableDefinition.getName(), variableDefinition.getDefaultValue());
            }
        }
        return result;
    }

    public StartNode getStartStateNotNull() {
        Preconditions.checkNotNull(startNode, "startNode");
        return startNode;
    }

    public List<Node> getNodes(boolean withEmbeddedSubprocesses) {
        List<Node> result = Lists.newArrayList(nodesList);
        if (withEmbeddedSubprocesses) {
            for (ParsedSubprocessDefinition subprocessDefinition : embeddedSubprocesses.values()) {
                result.addAll(subprocessDefinition.getNodes(withEmbeddedSubprocesses));
            }
        }
        return result;
    }

    public Node addNode(Node node) {
        Preconditions.checkArgument(node != null, "can't add a null node to a processdefinition");
        nodesList.add(node);
        if (nodesMap.put(node.getNodeId(), node) != null) {
            throw new InvalidDefinitionException(getName(), "found duplicated node " + node.getNodeId());
        }
        node.parsedProcessDefinition = this;
        if (node instanceof StartNode) {
            if (startNode != null) {
                throw new InvalidDefinitionException(getName(), "only one start-state allowed in a process");
            }
            startNode = (StartNode) node;
        }
        return node;
    }

    public Node getNode(String id) {
        Preconditions.checkNotNull(id);
        Node node = nodesMap.get(id);
        if (node != null) {
            return node;
        }
        if (id.startsWith(FileDataProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            for (ParsedSubprocessDefinition subprocessDefinition : embeddedSubprocesses.values()) {
                node = subprocessDefinition.getNode(id);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    public Node getNodeNotNull(String id) {
        Preconditions.checkNotNull(id);
        Node node = getNode(id);
        if (node != null) {
            return node;
        }
        throw new InternalApplicationException("node '" + id + "' not found");
    }

    @Override
    public GraphElement getParentElement() {
        return null;
    }

    public void addSwimlane(SwimlaneDefinition swimlaneDefinition) {
        swimlaneDefinitions.add(swimlaneDefinition);
        swimlaneDefinitionsMap.put(swimlaneDefinition.getName(), swimlaneDefinition);
    }

    public void setSwimlaneScriptingName(String name, String scriptingName) {
        SwimlaneDefinition swimlaneDefinition = getSwimlaneNotNull(name);
        swimlaneDefinition.setScriptingName(scriptingName);
        swimlaneDefinitionsMap.put(swimlaneDefinition.getScriptingName(), swimlaneDefinition);
    }

    public List<SwimlaneDefinition> getSwimlanes() {
        return swimlaneDefinitions;
    }

    public SwimlaneDefinition getSwimlane(String swimlaneName) {
        return swimlaneDefinitionsMap.get(swimlaneName);
    }

    public SwimlaneDefinition getSwimlaneById(String id) {
        for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
            if (Objects.equal(id, swimlaneDefinition.getNodeId())) {
                return swimlaneDefinition;
            }
        }
        return null;
    }

    public SwimlaneDefinition getSwimlaneNotNull(String swimlaneName) {
        SwimlaneDefinition swimlaneDefinition = getSwimlane(swimlaneName);
        if (swimlaneDefinition == null) {
            throw new InternalApplicationException("swimlane '" + swimlaneName + "' not found in " + this);
        }
        return swimlaneDefinition;
    }

    public boolean ignoreSubsitutionRulesForTask(Task task) {
        InteractionNode interactionNode = (InteractionNode) getNodeNotNull(task.getNodeId());
        return interactionNode.getFirstTaskNotNull().isIgnoreSubsitutionRules();
    }

    public Boolean getNodeAsyncExecution() {
        return nodeAsyncExecution;
    }

    public void setNodeAsyncExecution(Boolean nodeAsyncExecution) {
        this.nodeAsyncExecution = nodeAsyncExecution;
    }

    public boolean isGraphActionsEnabled() {
        return graphActionsEnabled;
    }

    public void setGraphActionsEnabled(boolean graphActionsEnabled) {
        this.graphActionsEnabled = graphActionsEnabled;
    }

    public void addEmbeddedSubprocess(ParsedSubprocessDefinition subprocessDefinition) {
        embeddedSubprocesses.put(subprocessDefinition.getNodeId(), subprocessDefinition);
    }

    public List<String> getEmbeddedSubprocessNodeIds() {
        List<String> result = Lists.newArrayList();
        for (Node node : nodesList) {
            if (node instanceof SubprocessNode && ((SubprocessNode) node).isEmbedded()) {
                result.add(node.getNodeId());
            }
        }
        return result;
    }

    public String getEmbeddedSubprocessNodeId(String subprocessName) {
        for (Node node : nodesList) {
            if (node instanceof SubprocessNode) {
                SubprocessNode subprocessNode = (SubprocessNode) node;
                if (subprocessNode.isEmbedded() && Objects.equal(subprocessName, subprocessNode.getSubProcessName())) {
                    return node.getNodeId();
                }
            }
        }
        for (ParsedSubprocessDefinition subprocessDefinition : embeddedSubprocesses.values()) {
            String nodeId = subprocessDefinition.getEmbeddedSubprocessNodeId(subprocessName);
            if (nodeId != null) {
                return nodeId;
            }
        }
        return null;
    }

    public String getEmbeddedSubprocessNodeIdNotNull(String subprocessName) {
        String subprocessNodeId = getEmbeddedSubprocessNodeId(subprocessName);
        if (subprocessNodeId == null) {
            throw new NullPointerException("No subprocess state found by subprocess name '" + subprocessName + "' in " + this);
        }
        return subprocessNodeId;
    }

    public Map<String, ParsedSubprocessDefinition> getEmbeddedSubprocesses() {
        return embeddedSubprocesses;
    }

    public ParsedSubprocessDefinition getEmbeddedSubprocessByIdNotNull(String id) {
        ParsedSubprocessDefinition subprocessDefinition = getEmbeddedSubprocesses().get(id);
        if (subprocessDefinition == null) {
            throw new InternalApplicationException(
                    "Embedded subprocess definition not found by id '" + id + "' in " + this + ", all = " + getEmbeddedSubprocesses().keySet());
        }
        return subprocessDefinition;
    }

    public ParsedSubprocessDefinition getEmbeddedSubprocessByNameNotNull(String name) {
        for (ParsedSubprocessDefinition subprocessDefinition : getEmbeddedSubprocesses().values()) {
            if (Objects.equal(name, subprocessDefinition.getName())) {
                return subprocessDefinition;
            }
        }
        throw new InternalApplicationException(
                "Embedded subprocess definition not found by name '" + name + "' in " + this + ", all = " + getEmbeddedSubprocesses().values());
    }

    public void mergeWithEmbeddedSubprocesses() {
        for (Node node : Lists.newArrayList(nodesList)) {
            if (node instanceof SubprocessNode) {
                SubprocessNode subprocessNode = (SubprocessNode) node;
                if (subprocessNode.isEmbedded()) {
                    ParsedSubprocessDefinition subprocessDefinition = getEmbeddedSubprocessByNameNotNull(subprocessNode.getSubProcessName());
                    EmbeddedSubprocessStartNode startNode = subprocessDefinition.getStartStateNotNull();
                    for (Transition transition : subprocessNode.getArrivingTransitions()) {
                        startNode.addArrivingTransition(transition);
                    }
                    startNode.setSubprocessNode(subprocessNode);
                    for (EmbeddedSubprocessEndNode endNode : subprocessDefinition.getEndNodes()) {
                        endNode.addLeavingTransition(subprocessNode.getLeavingTransitions().get(0));
                        endNode.setSubprocessNode(subprocessNode);
                    }
                    subprocessDefinition.mergeWithEmbeddedSubprocesses();
                }
            }
        }
    }

    public void setChanges(List<ProcessDefinitionChange> changes) {
        this.changes.addAll(changes);
    }

    public List<ProcessDefinitionChange> getChanges() {
        return changes;
    }

    @Override
    public String toString() {
        if (processDefinition != null) {
            return processDefinition.toString();
        }
        return name;
    }
}
