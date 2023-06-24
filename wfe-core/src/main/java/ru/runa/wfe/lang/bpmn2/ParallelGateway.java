package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class ParallelGateway extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.PARALLEL_GATEWAY;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();
        CurrentToken token = executionContext.getCurrentToken();
        executionLogic.endToken(token, executionContext.getParsedProcessDefinition(), null, null, false);
        log.debug("Executing " + this + " with " + token);
        StateInfo stateInfo = findStateInfo(executionContext.getCurrentProcess().getRootToken(), true);
        switch (stateInfo.state) {
            case LEAVING: {
                log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
                for (CurrentToken tokenToPop : stateInfo.tokensToPop) {
                    tokenToPop.setAbleToReactivateParent(false);
                }
                if (getArrivingTransitions().size() > 1 && token.getParent() != null) {
                    CurrentToken parentToken = token.getParent();
                    leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), parentToken));
                } else {
                    leave(executionContext);
                }
                break;
            }
            case WAITING: {
                if (stateInfo.activeTokenNodeIds.contains(getNodeId())) {
                    log.debug("scheduling execution due to active concurrent token found in this node");
                    TransactionListeners.addListener(new ActiveCheck(this, executionContext.getProcess().getId(), token), false);
                } else {
                    log.debug("blocking token " + token.getId() + " execution due to waiting on " + stateInfo.notPassedTransitions);
                }
                break;
            }
            case BLOCKING: {
                log.warn("failing token " + token.getId() + " execution because " + stateInfo.unreachableTransition
                        + " cannot be passed by active tokens in nodes " + stateInfo.activeTokenNodeIds);
                executionLogic.failToken(token, new ProcessExecutionException(ProcessExecutionException.PARALLEL_GATEWAY_UNREACHABLE_TRANSITION,
                        stateInfo.unreachableTransition));
                TransactionListeners.addListener(new FailedCheck(this, executionContext.getProcess().getId()), false);
                break;
            }
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        log.debug("Leaving " + this + " with " + executionContext.toString());
        CurrentToken token = executionContext.getCurrentToken();
        checkCyclicExecution(token);
        Map<CurrentToken, Transition> childTokens = Maps.newHashMap();
        for (Transition leavingTransition : getLeavingTransitions()) {
            CurrentToken childToken = new CurrentToken(token, getNodeId() + "/" + leavingTransition.getNodeId());
            childTokens.put(childToken, leavingTransition);
        }
        ApplicationContextFactory.getCurrentTokenDao().flushPendingChanges();
        log.debug("Child tokens created: " + childTokens.keySet());
        for (Map.Entry<CurrentToken, Transition> entry : childTokens.entrySet()) {
            ExecutionContext childExecutionContext = new ExecutionContext(executionContext.getParsedProcessDefinition(), entry.getKey());
            super.leave(childExecutionContext, entry.getValue());
        }
    }

    protected StateInfo findStateInfo(CurrentToken rootToken, boolean ignoreFailedTokens) {
        StateInfo stateInfo = new StateInfo();
        fillTokensInfo(rootToken, stateInfo);
        for (Transition transition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (CurrentToken token : stateInfo.arrivedTokens) {
                if (ignoreFailedTokens && token.getExecutionStatus() == ExecutionStatus.FAILED) {
                    continue;
                }
                if (Objects.equal(transition.getNodeId(), token.getTransitionId())
                        || Objects.equal(transition.getNodeIdBackCompatibilityPre4_3_0(), token.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    stateInfo.tokensToPop.add(token);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                stateInfo.notPassedTransitions.add(transition);
            }
        }
        if (stateInfo.notPassedTransitions.isEmpty()) {
            stateInfo.state = State.LEAVING;
        } else {
            for (Transition transition : stateInfo.notPassedTransitions) {
                if (!transitionCanBePassed(transition, stateInfo.activeTokenNodeIds, new HashSet<Node>())) {
                    stateInfo.unreachableTransition = transition;
                    stateInfo.state = State.BLOCKING;
                    break;
                }
            }
        }
        return stateInfo;
    }

    private void fillTokensInfo(CurrentToken token, StateInfo stateInfo) {
        if (token.isAbleToReactivateParent()) {
            if (token.getExecutionStatus() != ExecutionStatus.ACTIVE && Objects.equal(token.getNodeId(), getNodeId())) {
                stateInfo.arrivedTokens.add(token);
            } else if (token.getExecutionStatus() == ExecutionStatus.ACTIVE || token.getExecutionStatus() == ExecutionStatus.FAILED) {
                stateInfo.activeTokenNodeIds.add(token.getNodeId());
            }
        }
        for (CurrentToken childToken : token.getChildren()) {
            fillTokensInfo(childToken, stateInfo);
        }
    }

    private boolean transitionCanBePassed(Transition transition, Set<String> activeTokenNodeIds, Set<Node> testedNodes) {
        Node node = transition.getFrom();
        if (testedNodes.contains(node)) {
            return false;
        }
        testedNodes.add(node);
        if (activeTokenNodeIds.contains(node.getNodeId())) {
            return true;
        }
        for (Transition nodeTransition : node.getArrivingTransitions()) {
            if (transitionCanBePassed(nodeTransition, activeTokenNodeIds, testedNodes)) {
                return true;
            }
        }
        return false;
    }

    private void checkCyclicExecution(CurrentToken token) {
        if (token.getDepth() > SystemProperties.getTokenMaximumDepth()) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }

    private enum State {
        LEAVING,
        WAITING,
        BLOCKING
    }

    private static class StateInfo {
        private State state = State.WAITING;
        private Set<CurrentToken> arrivedTokens = Sets.newHashSet();
        private Set<String> activeTokenNodeIds = Sets.newHashSet();
        private List<CurrentToken> tokensToPop = Lists.newArrayList();
        private List<Transition> notPassedTransitions = Lists.newArrayList();
        private Transition unreachableTransition;
    }

    private static class ActiveCheck implements TransactionListener {
        private final ParallelGateway gateway;
        private final Long processId;
        private final CurrentToken token;

        public ActiveCheck(ParallelGateway gateway, Long processId, CurrentToken token) {
            this.gateway = gateway;
            this.processId = processId;
            this.token = token;
        }

        @Override
        public void onTransactionComplete() {
            synchronized (ParallelGateway.class) {
                ApplicationContextFactory.getTransactionalExecutor().execute(() -> {
                    log.debug("Executing " + this);
                    CurrentProcess process = ApplicationContextFactory.getCurrentProcessDao().getNotNull(processId);
                    CurrentTokenDao currentTokenDao = ApplicationContextFactory.getCurrentTokenDao();
                    List<CurrentToken> endedTokens = currentTokenDao.findByProcessAndNodeIdAndExecutionStatusIsEndedAndAbleToReactivateParent(process,
                            gateway.getNodeId());
                    if (endedTokens.isEmpty()) {
                        log.debug("no ended tokens found");
                        return;
                    }
                    StateInfo stateInfo = gateway.findStateInfo(process.getRootToken(), true);
                    switch (stateInfo.state) {
                    case LEAVING: {
                        log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
                        for (CurrentToken tokenToPop : stateInfo.tokensToPop) {
                            tokenToPop.setAbleToReactivateParent(false);
                        }
                        CurrentToken parentToken = stateInfo.tokensToPop.get(0).getParent();
                        gateway.leave(new ExecutionContext(gateway.getParsedProcessDefinition(), parentToken));
                        break;
                    }
                    case WAITING: {
                        log.warn("continue waiting on " + stateInfo.notPassedTransitions);
                        break;
                        }
                    case BLOCKING: {
                        log.error("failing process " + process.getId() + " execution because " + stateInfo.unreachableTransition
                                + " cannot be passed by active tokens in nodes " + stateInfo.activeTokenNodeIds);
                        process.setExecutionStatus(ExecutionStatus.FAILED);
                        ApplicationContextFactory.getExecutionLogic().failToken(token, new ProcessExecutionException(
                                ProcessExecutionException.PARALLEL_GATEWAY_UNREACHABLE_TRANSITION, stateInfo.unreachableTransition));
                        break;
                        }
                    }
                });
            }
        }
    }

    private static class FailedCheck implements TransactionListener {
        private final ParallelGateway gateway;
        private final Long processId;

        public FailedCheck(ParallelGateway gateway, Long processId) {
            this.gateway = gateway;
            this.processId = processId;
        }

        @Override
        public void onTransactionComplete() {
            synchronized (ParallelGateway.class) {
                ApplicationContextFactory.getTransactionalExecutor().execute(() -> {
                    log.debug("Executing " + this);
                    CurrentProcess process = ApplicationContextFactory.getCurrentProcessDao().getNotNull(processId);
                    CurrentTokenDao currentTokenDao = ApplicationContextFactory.getCurrentTokenDao();
                    List<CurrentToken> failedTokens = currentTokenDao.findByProcessAndNodeIdAndExecutionStatus(process, gateway.getNodeId(),
                            ExecutionStatus.FAILED);
                    if (failedTokens.isEmpty()) {
                        log.warn("no failed tokens found");
                        return;
                    }
                    StateInfo stateInfo = gateway.findStateInfo(process.getRootToken(), false);
                    switch (stateInfo.state) {
                        case LEAVING: {
                            log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
                            for (CurrentToken tokenToPop : stateInfo.tokensToPop) {
                                tokenToPop.setAbleToReactivateParent(false);
                                tokenToPop.setExecutionStatus(ExecutionStatus.ENDED);
                            }
                            CurrentToken parentToken = stateInfo.tokensToPop.get(0).getParent();
                            gateway.leave(new ExecutionContext(gateway.getParsedProcessDefinition(), parentToken));
                            break;
                        }
                        case WAITING: {
                            log.warn("leaving failed tokens " + failedTokens + " due to waiting on " + stateInfo.notPassedTransitions);
                            break;
                        }
                        case BLOCKING: {
                            if (stateInfo.activeTokenNodeIds.contains(gateway.getNodeId())) {
                                log.warn("leaving failed tokens " + failedTokens + " due to active token in this node");
                            } else {
                                log.error("failing process " + process.getId() + " execution because " + stateInfo.unreachableTransition
                                        + " cannot be passed by active tokens in nodes " + stateInfo.activeTokenNodeIds);
                                process.setExecutionStatus(ExecutionStatus.FAILED);
                            }
                            break;
                        }
                    }
                });
            }
        }
    }
}
