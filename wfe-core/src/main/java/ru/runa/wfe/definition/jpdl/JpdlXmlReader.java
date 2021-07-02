package ru.runa.wfe.definition.jpdl;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.LocalizationDao;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.definition.logic.SwimlaneUtils;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.EmbeddedSubprocessEndNode;
import ru.runa.wfe.lang.EmbeddedSubprocessStartNode;
import ru.runa.wfe.lang.EndNode;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiSubprocessNode;
import ru.runa.wfe.lang.MultiTaskCreationMode;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.MultiTaskSynchronizationMode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ScriptNode;
import ru.runa.wfe.lang.SendMessageNode;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.VariableContainerNode;
import ru.runa.wfe.lang.jpdl.CancelTimerAction;
import ru.runa.wfe.lang.jpdl.Conjunction;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;
import ru.runa.wfe.lang.jpdl.Decision;
import ru.runa.wfe.lang.jpdl.EndToken;
import ru.runa.wfe.lang.jpdl.Fork;
import ru.runa.wfe.lang.jpdl.Join;
import ru.runa.wfe.lang.jpdl.ReceiveMessageNode;
import ru.runa.wfe.lang.jpdl.WaitNode;
import ru.runa.wfe.var.VariableMapping;

@SuppressWarnings({ "unchecked" })
public class JpdlXmlReader {
    private String defaultDueDate;
    private final List<Object[]> unresolvedTransitionDestinations = Lists.newArrayList();

    @Autowired
    private LocalizationDao localizationDao;

    private final Document document;
    private static final boolean waitStateCompatibility = true;

    private static final String INVALID_ATTR = "invalid";
    private static final String ACCESS_ATTR = "access";
    private static final String VARIABLE_NODE = "variable";
    private static final String SUB_PROCESS_NODE = "sub-process";
    private static final String MAPPED_NAME_ATTR = "mapped-name";
    private static final String DUEDATE_ATTR = "duedate";
    private static final String DEFAULT_DUEDATE_ATTR = "default-task-duedate";
    private static final String REPEAT_ATTR = "repeat";
    private static final String TIMER_NODE = "timer";
    private static final String ASSIGNMENT_NODE = "assignment";
    private static final String ID_ATTR = "id";
    private static final String SWIMLANE_ATTR = "swimlane";
    private static final String TRANSITION_ATTR = "transition";
    private static final String TASK_NODE = "task";
    private static final String SWIMLANE_NODE = "swimlane";
    private static final String REASSIGN = "reassign";
    private static final String REASSIGN_SWIMLANE_TO_TASK_PERFORMER = "reassignSwimlaneToTaskPerformer";
    private static final String TO_ATTR = "to";
    private static final String CLASS_ATTR = "class";
    private static final String EVENT_NODE = "event";
    private static final String TRANSITION_NODE = "transition";
    private static final String HANDLER_NODE = "handler";
    private static final String DESCRIPTION_NODE = "description";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String ASYNC_ATTR = "async";
    private static final String ASYNC_COMPLETION_MODE_ATTR = "asyncCompletionMode";
    private static final String TASK_EXECUTORS_ATTR = "taskExecutors";
    private static final String TASK_EXECUTORS_USAGE = "taskExecutorsUsage";
    private static final String TASK_EXECUTION_MODE_ATTR = "taskExecutionMode";
    private static final String ACTION_NODE = "action";
    private static final String ACCESS_TYPE = "accessType";
    private static final String EMBEDDED = "embedded";
    private static final String TRANSACTIONAL = "transactional";
    private static final String IGNORE_SUBSTITUTION_RULES = "ignoreSubstitutionRules";
    private static final String MULTI_TASK_CREATION_MODE = "multiTaskCreationMode";
    private static final String NODE_ASYNC_EXECUTION = "asyncExecution";
    private static final String BEHAVIOUR = "behavior";
    private static final String BEHAVIOUR_TERMINATE = "TERMINATE";
    private static final String EXECUTION_CONDITION = "executionCondition";
    private static final String GLOBAL = "global";
    private static final String VALIDATE_AT_START = "validateAtStart";
    private static final String DISABLE_CASCADING_SUSPENSION = "disableCascadingSuspension";

    private static Map<String, Class<? extends Node>> nodeTypes = Maps.newHashMap();
    static {
        // nodeTypes.put("start-state", StartNode.class);
        // nodeTypes.put("end-token-state", EndToken.class);
        nodeTypes.put("end-state", EndNode.class);
        nodeTypes.put("wait-state", WaitNode.class);
        nodeTypes.put("task-node", TaskNode.class);
        nodeTypes.put("multi-task-node", MultiTaskNode.class);
        nodeTypes.put("fork", Fork.class);
        nodeTypes.put("join", Join.class);
        nodeTypes.put("decision", Decision.class);
        nodeTypes.put("conjunction", Conjunction.class);
        nodeTypes.put("process-state", SubprocessNode.class);
        nodeTypes.put("multiinstance-state", MultiSubprocessNode.class);
        nodeTypes.put("send-message", SendMessageNode.class);
        nodeTypes.put("receive-message", ReceiveMessageNode.class);
        nodeTypes.put("node", ScriptNode.class);
    }

    public JpdlXmlReader(Document document) {
        this.document = document;
    }

    public ProcessDefinition readProcessDefinition(ProcessDefinition processDefinition) {
        try {
            Element root = document.getRootElement();

            // read the process name
            processDefinition.setName(root.attributeValue(NAME_ATTR));
            processDefinition.setDescription(root.elementTextTrim(DESCRIPTION_NODE));
            defaultDueDate = root.attributeValue(DEFAULT_DUEDATE_ATTR);
            if ("true".equals(root.attributeValue(INVALID_ATTR))) {
                throw new InvalidDefinitionException(processDefinition.getName(), "invalid process definition");
            }
            String accessTypeString = root.attributeValue(ACCESS_TYPE);
            if (!Strings.isNullOrEmpty(accessTypeString)) {
                processDefinition.setAccessType(ProcessDefinitionAccessType.valueOf(accessTypeString));
            }
            String nodeAsyncExecutionString = root.attributeValue(NODE_ASYNC_EXECUTION);
            if (!Strings.isNullOrEmpty(nodeAsyncExecutionString)) {
                processDefinition.setNodeAsyncExecution("new".equals(nodeAsyncExecutionString));
            }

            // 1: read most content
            readSwimlanes(processDefinition, root);
            readNodes(processDefinition, root);
            readEvents(processDefinition, root, processDefinition);

            // 2: processing transitions
            resolveTransitionDestinations(processDefinition);

            // 3: verify
            verifyElements(processDefinition);
        } catch (Throwable th) {
            Throwables.propagateIfInstanceOf(th, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(processDefinition.getName(), th);
        }
        return processDefinition;
    }

    private void readSwimlanes(ProcessDefinition processDefinition, Element processDefinitionElement) {
        List<Element> elements = processDefinitionElement.elements(SWIMLANE_NODE);
        for (Element element : elements) {
            String swimlaneName = element.attributeValue(NAME_ATTR);
            if (swimlaneName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "there's a swimlane without a name");
            }
            SwimlaneDefinition swimlaneDefinition = new SwimlaneDefinition();
            swimlaneDefinition.setName(swimlaneName);
            swimlaneDefinition.setGlobal("true".equals(element.attributeValue(GLOBAL)));
            Element assignmentElement = element.element(ASSIGNMENT_NODE);
            if (assignmentElement != null) {
                swimlaneDefinition.setDelegation(readDelegation(processDefinition, assignmentElement));
            }
            SwimlaneUtils.setOrgFunctionLabel(swimlaneDefinition, localizationDao);
            processDefinition.addSwimlane(swimlaneDefinition);
        }
    }

    private void readNodes(ProcessDefinition processDefinition, Element parentElement) {
        List<Element> elements = parentElement.elements();
        for (Element element : elements) {
            String nodeName = element.getName();
            Node node = null;
            if (nodeTypes.containsKey(nodeName)) {
                node = ApplicationContextFactory.createAutowiredBean(nodeTypes.get(nodeName));
            } else if ("start-state".equals(nodeName)) {
                if (processDefinition instanceof SubprocessDefinition) {
                    node = ApplicationContextFactory.createAutowiredBean(EmbeddedSubprocessStartNode.class);
                } else {
                    node = ApplicationContextFactory.createAutowiredBean(StartNode.class);
                }
            } else if ("end-token-state".equals(nodeName)) {
                if (processDefinition instanceof SubprocessDefinition && !BEHAVIOUR_TERMINATE.equals(element.attributeValue(BEHAVIOUR))) {
                    node = ApplicationContextFactory.createAutowiredBean(EmbeddedSubprocessEndNode.class);
                } else {
                    node = ApplicationContextFactory.createAutowiredBean(EndToken.class);
                }
            }
            if (node != null) {
                node.setProcessDefinition(processDefinition);
                readNode(processDefinition, element, node);
            }
        }
    }

    private void readTasks(ProcessDefinition processDefinition, Element parentElement, InteractionNode taskNode) {
        List<Element> elements = parentElement.elements(TASK_NODE);
        for (Element element : elements) {
            readTask(processDefinition, element, taskNode, parentElement);
        }
    }

    private void readTask(ProcessDefinition processDefinition, Element element, InteractionNode node, Element parentElement) {
        if (node instanceof EmbeddedSubprocessStartNode) {
            return;
        }
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setNodeId(node.getNodeId());
        taskDefinition.setProcessDefinition(processDefinition);
        // get the task name
        String name = element.attributeValue(NAME_ATTR);
        if (name != null) {
            taskDefinition.setName(name);
        } else {
            taskDefinition.setName(node.getName());
        }
        // get the task description
        String description = element.elementTextTrim(DESCRIPTION_NODE);
        if (description != null) {
            taskDefinition.setDescription(description);
        } else {
            taskDefinition.setDescription(node.getDescription());
        }
        // parse common subelements
        readNodeTimers(processDefinition, element, taskDefinition);
        readEvents(processDefinition, element, taskDefinition);
        taskDefinition.setDeadlineDuration(element.attributeValue(DUEDATE_ATTR, defaultDueDate));
        node.addTask(taskDefinition);
        String swimlaneName = element.attributeValue(SWIMLANE_ATTR);
        if (swimlaneName != null) {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
            taskDefinition.setSwimlane(swimlaneDefinition);
            String reassignSwimlaneToInitializer = element.attributeValue(REASSIGN, null);
            if (reassignSwimlaneToInitializer != null) {
                taskDefinition.setReassignSwimlaneToInitializer(Boolean.valueOf(reassignSwimlaneToInitializer));
            }
            String reassignSwimlaneToTaskPerformer = element.attributeValue(REASSIGN_SWIMLANE_TO_TASK_PERFORMER,null);
            if (reassignSwimlaneToTaskPerformer != null) {
                taskDefinition.setReassignSwimlaneToTaskPerformer(Boolean.valueOf(reassignSwimlaneToTaskPerformer));
            }
            taskDefinition.setIgnoreSubsitutionRules(Boolean.valueOf(element.attributeValue(IGNORE_SUBSTITUTION_RULES, "false")));
        }
    }

    private List<VariableMapping> readVariableMappings(ProcessDefinition processDefinition, Element parentElement) {
        List<VariableMapping> variableAccesses = Lists.newArrayList();
        List<Element> elements = parentElement.elements(VARIABLE_NODE);
        for (Element element : elements) {
            String variableName = element.attributeValue(NAME_ATTR);
            if (variableName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(),
                        "the name attribute of a variable element is required: " + element.asXML());
            }
            String mappedName = element.attributeValue(MAPPED_NAME_ATTR);
            if (mappedName == null) {
                throw new InvalidDefinitionException(processDefinition.getName(),
                        "the mapped-name attribute of a variable element is required: " + element.asXML());
            }
            String access = element.attributeValue(ACCESS_ATTR, "read,write");
            variableAccesses.add(new VariableMapping(variableName, mappedName, access));
        }
        return variableAccesses;
    }

    private void readNode(ProcessDefinition processDefinition, Element element, Node node) {
        node.setNodeId(element.attributeValue(ID_ATTR));
        node.setName(element.attributeValue(NAME_ATTR));
        node.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
        String nodeAsyncExecutionString = element.attributeValue(NODE_ASYNC_EXECUTION);
        if (!Strings.isNullOrEmpty(nodeAsyncExecutionString)) {
            node.setAsyncExecution("new".equals(nodeAsyncExecutionString));
        }
        processDefinition.addNode(node);
        readEvents(processDefinition, element, node);
        // save the transitions and parse them at the end
        addUnresolvedTransitionDestination(element, node);

        if (node instanceof StartNode) {
            StartNode startNode = (StartNode) node;
            Element startTaskElement = element.element(TASK_NODE);
            if (startTaskElement != null) {
                readTask(processDefinition, startTaskElement, startNode, element);
            }
        }
        // if (node instanceof WaitNode) {
        // CreateTimerAction createTimerAction =
        // ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
        // createTimerAction.setName(node.getName() + "/wait");
        // createTimerAction.setTransitionName(element.attributeValue("transition"));
        // createTimerAction.setDueDate(element.attributeValue("duedate"));
        // addAction(node, Event.EVENTTYPE_NODE_ENTER, createTimerAction);
        // }
        if (node instanceof VariableContainerNode) {
            VariableContainerNode variableContainerNode = (VariableContainerNode) node;
            variableContainerNode.setVariableMappings(readVariableMappings(processDefinition, element));
        }
        if (node instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) node;
            taskNode.setAsync(Boolean.valueOf(element.attributeValue(ASYNC_ATTR, "false")));
            taskNode.setCompletionMode(
                    AsyncCompletionMode.valueOf(element.attributeValue(ASYNC_COMPLETION_MODE_ATTR, AsyncCompletionMode.NEVER.name())));
            readTasks(processDefinition, element, taskNode);
        }
        if (node instanceof MultiTaskNode) {
            MultiTaskNode multiTaskNode = (MultiTaskNode) node;
            multiTaskNode.setAsync(Boolean.valueOf(element.attributeValue(ASYNC_ATTR, "false")));
            multiTaskNode.setCompletionMode(
                    AsyncCompletionMode.valueOf(element.attributeValue(ASYNC_COMPLETION_MODE_ATTR, AsyncCompletionMode.NEVER.name())));
            multiTaskNode.setSynchronizationMode(
                    MultiTaskSynchronizationMode.valueOf(element.attributeValue(TASK_EXECUTION_MODE_ATTR, MultiTaskSynchronizationMode.LAST.name())));
            multiTaskNode.setDiscriminatorVariableName(element.attributeValue(TASK_EXECUTORS_ATTR));
            multiTaskNode.setDiscriminatorUsage(element.attributeValue(TASK_EXECUTORS_USAGE));
            multiTaskNode.setCreationMode(
                    MultiTaskCreationMode.valueOf(element.attributeValue(MULTI_TASK_CREATION_MODE, MultiTaskCreationMode.BY_EXECUTORS.name())));
            multiTaskNode.setVariableMappings(readVariableMappings(processDefinition, element));
            multiTaskNode.setDiscriminatorCondition(element.attributeValue(EXECUTION_CONDITION));
            readTasks(processDefinition, element, multiTaskNode);
        }
        if (node instanceof SubprocessNode) {
            SubprocessNode subprocessNode = (SubprocessNode) node;
            Element subProcessElement = element.element(SUB_PROCESS_NODE);
            if (subProcessElement != null) {
                subprocessNode.setSubProcessName(subProcessElement.attributeValue(NAME_ATTR));
                subprocessNode.setEmbedded(Boolean.parseBoolean(subProcessElement.attributeValue(EMBEDDED, "false")));
                subprocessNode.setTransactional(Boolean.parseBoolean(subProcessElement.attributeValue(TRANSACTIONAL, "false")));
                subprocessNode.setValidateAtStart(Boolean.parseBoolean(subProcessElement.attributeValue(VALIDATE_AT_START, "false")));
                subprocessNode.setDisableCascadingSuspension(
                        Boolean.parseBoolean(subProcessElement.attributeValue(DISABLE_CASCADING_SUSPENSION, "false")));
            }
            if (node instanceof MultiSubprocessNode) {
                ((MultiSubprocessNode) node).setDiscriminatorCondition(element.attributeValue(EXECUTION_CONDITION));
            }
        }
        if (node instanceof Decision) {
            Decision decision = (Decision) node;
            Element decisionHandlerElement = element.element(HANDLER_NODE);
            if (decisionHandlerElement == null) {
                throw new InvalidDefinitionException(processDefinition.getName(), "No handler in decision found: " + node);
            }
            decision.setDelegation(readDelegation(processDefinition, decisionHandlerElement));
        }
        if (node instanceof SendMessageNode) {
            SendMessageNode sendMessageNode = (SendMessageNode) node;
            sendMessageNode.setTtlDuration(element.attributeValue(DUEDATE_ATTR, "1 days"));
        }
        if (node instanceof ScriptNode) {
            ScriptNode serviceTask = (ScriptNode) node;
            Element actionElement = element.element(ACTION_NODE);
            Preconditions.checkNotNull(actionElement, "No action defined in " + serviceTask);
            serviceTask.setDelegation(readDelegation(processDefinition, actionElement));
        }
        readNodeTimers(processDefinition, element, node);
    }

    private void readNodeTimers(ProcessDefinition processDefinition, Element parentElement, GraphElement node) {
        List<Element> elements = parentElement.elements(TIMER_NODE);
        int timerNumber = 1;
        for (Element element : elements) {
            // 1 timer for compatibility timer names with 3.x
            String name;
            if (SystemProperties.isV3CompatibilityMode()) {
                name = element.attributeValue(NAME_ATTR, node.getName());
            } else {
                name = node.getNodeId() + (TimerJob.ESCALATION_NAME.equals(element.attributeValue(NAME_ATTR)) ? "/" + TimerJob.ESCALATION_NAME : "")
                        + "/timer-" + timerNumber++;
            }
            CreateTimerAction createTimerAction = ApplicationContextFactory.createAutowiredBean(CreateTimerAction.class);
            createTimerAction.setNodeId(node.getNodeId());
            createTimerAction.setName(name);
            createTimerAction.setTransitionName(element.attributeValue(TRANSITION_ATTR));
            String durationString = element.attributeValue(DUEDATE_ATTR);
            if (Strings.isNullOrEmpty(durationString) && node instanceof TaskNode && TimerJob.ESCALATION_NAME.equals(name)) {
                durationString = ((TaskNode) node).getFirstTaskNotNull().getDeadlineDuration();
                if (Strings.isNullOrEmpty(durationString)) {
                    throw new InvalidDefinitionException(processDefinition.getName(), "No '" + DUEDATE_ATTR + "' specified for timer in " + node);
                }
            }
            createTimerAction.setDueDate(durationString);
            createTimerAction.setRepeatDurationString(element.attributeValue(REPEAT_ATTR));
            if (node instanceof TaskDefinition) {
                throw new UnsupportedOperationException("task/timer");
            }
            String createEventType = node instanceof TaskNode ? ActionEvent.TASK_CREATE : ActionEvent.NODE_ENTER;
            addAction(node, createEventType, createTimerAction);
            Action timerAction = readSingleAction(processDefinition, element);
            if (timerAction != null) {
                timerAction.setName(name);
                addAction(node, ActionEvent.TIMER, timerAction);
            }
            CancelTimerAction cancelTimerAction = ApplicationContextFactory.createAutowiredBean(CancelTimerAction.class);
            cancelTimerAction.setNodeId(createTimerAction.getNodeId());
            cancelTimerAction.setName(createTimerAction.getName());
            String cancelEventType = node instanceof TaskDefinition ? ActionEvent.TASK_END : ActionEvent.NODE_LEAVE;
            addAction(node, cancelEventType, cancelTimerAction);
        }
    }

    private void readEvents(ProcessDefinition processDefinition, Element parentElement, GraphElement graphElement) {
        List<Element> elements = parentElement.elements(EVENT_NODE);
        for (Element eventElement : elements) {
            String eventType = eventElement.attributeValue(TYPE_ATTR);
            readActions(processDefinition, eventElement, graphElement, eventType);
        }
    }

    private void readActions(ProcessDefinition processDefinition, Element eventElement, GraphElement graphElement, String eventType) {
        // for all the elements in the event element
        List<Element> elements = eventElement.elements(ACTION_NODE);
        for (Element actionElement : elements) {
            Action action = createAction(processDefinition, actionElement);
            addAction(graphElement, eventType, action);
        }
    }

    private void addAction(GraphElement graphElement, String eventType, Action action) {
        ActionEvent actionEvent = graphElement.getEventNotNull(eventType);
        action.setParentElement(graphElement);
        actionEvent.addAction(action);
    }

    private Action readSingleAction(ProcessDefinition processDefinition, Element nodeElement) {
        Element actionElement = nodeElement.element(ACTION_NODE);
        if (actionElement != null) {
            return createAction(processDefinition, actionElement);
        }
        return null;
    }

    private Action createAction(ProcessDefinition processDefinition, Element element) {
        Action action = new Action();
        action.setName(element.attributeValue(NAME_ATTR));
        action.setDelegation(readDelegation(processDefinition, element));
        return action;
    }

    private Delegation readDelegation(ProcessDefinition processDefinition, Element element) {
        String className = element.attributeValue(CLASS_ATTR);
        if (className == null) {
            throw new InvalidDefinitionException(processDefinition.getName(), "no className specified in " + element.asXML());
        }
        String configuration = element.getText().trim();
        Delegation delegation = new Delegation(className, configuration);
        // check
        try {
            delegation.getInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return delegation;
    }

    // transition destinations are parsed in a second pass
    // //////////////////////

    private void addUnresolvedTransitionDestination(Element nodeElement, Node node) {
        unresolvedTransitionDestinations.add(new Object[] { nodeElement, node });
    }

    private void resolveTransitionDestinations(ProcessDefinition processDefinition) {
        for (Object[] unresolvedTransition : unresolvedTransitionDestinations) {
            Element nodeElement = (Element) unresolvedTransition[0];
            Node node = (Node) unresolvedTransition[1];
            List<Element> transitionElements = nodeElement.elements(TRANSITION_NODE);
            for (Element transitionElement : transitionElements) {
                resolveTransitionDestination(processDefinition, transitionElement, node);
            }
        }
    }

    /**
     * creates the transition object and configures it by the read attributes
     *
     * @return the created <code>ru.runa.wfe.lang.Transition</code> object (useful, if you want to override this method to read additional
     *         configuration properties)
     */
    private void resolveTransitionDestination(ProcessDefinition processDefinition, Element element, Node node) {
        Transition transition = new Transition();
        transition.setProcessDefinition(processDefinition);
        node.addLeavingTransition(transition);
        transition.setName(element.attributeValue(NAME_ATTR));
        transition.setNodeId(node.getNodeId() + "/" + transition.getName());
        for (CreateTimerAction createTimerAction : CreateTimerAction.getNodeTimerActions(node, false)) {
            if (Objects.equal(createTimerAction.getTransitionName(), transition.getName())) {
                transition.setTimerTransition(true);
            }
        }
        transition.setDescription(element.elementTextTrim(DESCRIPTION_NODE));
        // set destinationNode of the transition
        String toId = element.attributeValue(TO_ATTR);
        if (toId == null) {
            throw new InvalidDefinitionException(processDefinition.getName(), "node '" + node + "' has a transition without a 'to'-attribute");
        }
        Node to = processDefinition.getNodeNotNull(toId);
        to.addArrivingTransition(transition);
        // read the actions
        readActions(processDefinition, element, transition, ActionEvent.TRANSITION);
    }

    private void verifyElements(ProcessDefinition processDefinition) {
        for (Node node : processDefinition.getNodes(false)) {
            node.validate();
        }
    }
}
