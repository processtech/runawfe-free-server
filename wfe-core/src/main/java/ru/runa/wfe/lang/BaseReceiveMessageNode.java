package ru.runa.wfe.lang;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.runa.wfe.audit.CurrentReceiveMessageLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Signal;
import ru.runa.wfe.execution.dao.SignalDao;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;

public class BaseReceiveMessageNode extends BaseMessageNode implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getCurrentToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
        SignalDao signalDao = ApplicationContextFactory.getSignalDao();
        List<Signal> signals = signalDao.findByMessageSelectorsContainsOrEmpty(executionContext.getToken().getMessageSelector());
        for (Signal signal : signals) {
            Map<String, String> routingData = signal.getRoutingData();
            boolean suitable = true;
            VariableProvider variableProvider = executionContext.getVariableProvider();
            for (VariableMapping mapping : getVariableMappings()) {
                if (mapping.isPropertySelector()) {
                    String selectorValue = routingData.get(mapping.getName());
                    String expectedValue = Utils.getMessageSelectorValue(variableProvider, this, mapping);
                    if (!Objects.equals(expectedValue, selectorValue)) {
                        log.debug(routingData + " rejected in " + executionContext.getTask() + " due to diff in " + mapping.getName() + " ("
                                + expectedValue + "!=" + selectorValue + ")");
                        suitable = false;
                        break;

                    }
                }
            }
            if (suitable) {
                log.debug(signal.toString() + " activated incoming token");
                signalDao.delete(signal);
                executionContext.addLog(new CurrentReceiveMessageLog(this, signal.toString()));
                Map<String, Object> payloadData = signal.getPayloadData();
                for (VariableMapping variableMapping : getVariableMappings()) {
                    if (!variableMapping.isPropertySelector()) {
                        if (payloadData.containsKey(variableMapping.getMappedName())) {
                            Object value = payloadData.get(variableMapping.getMappedName());
                            executionContext.setVariableValue(variableMapping.getName(), value);
                        } else {
                            log.warn("message does not contain value for '" + variableMapping.getMappedName() + "'");
                        }
                    }
                }
                leave(executionContext);
                return;
            }
        }
    }

    @Override
    public void cancel(ExecutionContext executionContext) {
        super.cancel(executionContext);
        executionContext.getCurrentToken().setMessageSelector(null);
    }

    public void leave(ExecutionContext executionContext, Map<String, Object> map) {
        String swimlaneName = null;
        boolean setSwimlaneValue = true;
        if (this instanceof BoundaryEvent && getParentElement() instanceof BaseTaskNode) {
            TaskDefinition taskDefinition = ((BaseTaskNode) getParentElement()).getFirstTaskNotNull();
            swimlaneName = taskDefinition.getSwimlane().getName();
            setSwimlaneValue = taskDefinition.isReassignSwimlaneToTaskPerformer();
        }
        for (VariableMapping variableMapping : getVariableMappings()) {
            if (!variableMapping.isPropertySelector()) {
                if (map.containsKey(variableMapping.getMappedName())) {
                    Object value = map.get(variableMapping.getMappedName());
                    if (Objects.equals(swimlaneName, variableMapping.getName()) && !setSwimlaneValue) {
                        log.warn("ignored to set swimlane value from signal for '" + variableMapping.getMappedName() + "'");
                        continue;
                    }
                    executionContext.setVariableValue(variableMapping.getName(), value);
                } else {
                    log.warn("message does not contain value for '" + variableMapping.getMappedName() + "'");
                }
            }
        }
        super.leave(executionContext);
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        executionContext.getCurrentToken().setMessageSelector(null);
        super.leave(executionContext, transition);
    }

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

}
