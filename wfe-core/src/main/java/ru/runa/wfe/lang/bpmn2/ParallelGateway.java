package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.UserTransaction;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.lang.EmbeddedSubprocessEndNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.Transition;

public class ParallelGateway extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.PARALLEL_GATEWAY;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        Token token = executionContext.getToken();
        token.end(executionContext.getProcessDefinition(), null, null, false);
        log.debug("Executing " + this + " with " + token);
        StateInfo stateInfo = findStateInfo(executionContext.getProcess().getId(), true);
        switch (stateInfo.state) {
        case LEAVING: {
            log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
            for (Token tokenToPop : stateInfo.tokensToPop) {
                tokenToPop.setAbleToReactivateParent(false);
            }
            if (getArrivingTransitions().size() > 1 && token.getParent() != null) {
                Token parentToken = token.getParent();
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            } else {
                leave(executionContext);
            }
            break;
        }
        case WAITING: {
            if (stateInfo.getActiveTokenNodeIds().contains(getNodeId())) {
                log.debug("scheduling execution due to active concurrent token found in this node");
                TransactionListeners.addListener(new ActiveCheck(this, executionContext.getProcess().getId(), token), false);
            } else {
                log.debug("blocking token " + token.getId() + " execution due to waiting on " + stateInfo.notPassedTransitions);
            }
            break;
        }
        case BLOCKING: {
            log.warn("failing token " + token.getId() + " execution because " + stateInfo.unreachableTransition
                    + " cannot be passed by active tokens in nodes " + stateInfo.getActiveTokenNodeIds());
            token.fail(new ProcessExecutionException(ProcessExecutionException.PARALLEL_GATEWAY_UNREACHABLE_TRANSITION,
                    stateInfo.unreachableTransition));
            TransactionListeners.addListener(new FailedCheck(this, executionContext.getProcess().getId()), false);
            break;
        }
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        log.debug("Leaving " + this + " with " + executionContext.toString());
        Token token = executionContext.getToken();
        checkCyclicExecution(token);
        Map<Token, Transition> childTokens = Maps.newHashMap();
        for (Transition leavingTransition : getLeavingTransitions()) {
            Token childToken = new Token(token, getNodeId() + "/" + leavingTransition.getNodeId());
            childTokens.put(childToken, leavingTransition);
        }
        ApplicationContextFactory.getTokenDAO().flushPendingChanges();
        log.debug("Child tokens created: " + childTokens.keySet());
        for (Map.Entry<Token, Transition> entry : childTokens.entrySet()) {
            ExecutionContext childExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), entry.getKey());
            super.leave(childExecutionContext, entry.getValue());
        }
    }

    private StateInfo findStateInfo(Long processId, boolean ignoreFailedTokens) {
        StateInfo stateInfo = new StateInfo();
        List<Token> tokens = ApplicationContextFactory.getTokenDAO().findByProcessIdAndParentIsNull(processId);
        for (Token token : tokens) {
            fillTokensInfo(token, stateInfo);
        }
        for (Transition transition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token token : stateInfo.arrivedTokens) {
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
                if (!hasTokenAlreadyArrivedThroughExactTransitionOrTransitionCanBePassed(stateInfo, transition)) {
                    stateInfo.unreachableTransition = transition;
                    stateInfo.state = State.BLOCKING;
                    break;
                }
            }
        }
        return stateInfo;
    }

    private void fillTokensInfo(Token token, StateInfo stateInfo) {
        if (token.isAbleToReactivateParent()) {
            if (token.getExecutionStatus() != ExecutionStatus.ACTIVE && Objects.equal(token.getNodeId(), getNodeId())) {
                stateInfo.arrivedTokens.add(token);
            } else if (token.getExecutionStatus() == ExecutionStatus.ACTIVE || token.getExecutionStatus() == ExecutionStatus.FAILED) {
                stateInfo.activeTokens.add(token);
            }
        }
        for (Token childToken : token.getChildren()) {
            fillTokensInfo(childToken, stateInfo);
        }
    }
    
    private boolean hasTokenAlreadyArrivedThroughExactTransitionOrTransitionCanBePassed(StateInfo stateInfo, Transition transition) {
        boolean tokenAlreadyArrived = false;
        for (Token token : stateInfo.activeTokens) {
            if (token.getNodeId().equals(getNodeId()) && token.getTransitionId().equals(transition.getNodeId())) {
                log.debug(String.format("Skipping %s passage validation due to active token %s arriving through it", transition, token));
                tokenAlreadyArrived = true;
                break;
            }
        }
        return tokenAlreadyArrived ? tokenAlreadyArrived
                : transitionCanBePassed(transition, stateInfo.getActiveTokenNodeIds(), new HashSet<Node>());
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
        if (node instanceof EmbeddedSubprocessEndNode) {
            // code below needed for cases when subprocess have more than one end node, so arriving transition on node after subprocess return only
            // one of theese end nodes which results in only one branch of subprocess being tested on can be passed
            for (Node subprocessEndNode : ((SubprocessDefinition) node.getProcessDefinition()).getEndNodes()) {
                for (Transition nodeTransition : subprocessEndNode.getArrivingTransitions()) {
                    if (transitionCanBePassed(nodeTransition, activeTokenNodeIds, testedNodes)) {
                        return true;
                    }
                }
            }
        }
        for (Transition nodeTransition : node.getArrivingTransitions()) {
            if (transitionCanBePassed(nodeTransition, activeTokenNodeIds, testedNodes)) {
                return true;
            }
        }
        return false;
    }

    private void checkCyclicExecution(Token token) {
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
        private Set<Token> arrivedTokens = Sets.newHashSet();
        private Set<Token> activeTokens = Sets.newHashSet();
        private List<Token> tokensToPop = Lists.newArrayList();
        private List<Transition> notPassedTransitions = Lists.newArrayList();
        private Transition unreachableTransition;
        
        public Set<String> getActiveTokenNodeIds() {
            Set<String> result = Sets.newHashSet();
            for (Token token : activeTokens) {
                result.add(token.getNodeId());
            }
            return result;
        }
    }

    private static class ActiveCheck implements TransactionListener {
        private final ParallelGateway gateway;
        private final Long processId;
        private final Token token;

        public ActiveCheck(ParallelGateway gateway, Long processId, Token token) {
            this.gateway = gateway;
            this.processId = processId;
            this.token = token;
        }

        @Override
        public void onTransactionComplete(UserTransaction transaction) {
            synchronized (ParallelGateway.class) {
                new TransactionalExecutor(transaction) {

                    @Override
                    protected void doExecuteInTransaction() throws Exception {
                        log.debug("Executing " + this);
                        ru.runa.wfe.execution.Process process = ApplicationContextFactory.getProcessDAO().getNotNull(processId);
                        TokenDao tokenDao = ApplicationContextFactory.getTokenDAO();
                        List<Token> endedTokens = tokenDao.findByProcessAndNodeIdAndExecutionStatusIsEndedAndAbleToReactivateParent(process,
                                gateway.getNodeId());
                        if (endedTokens.isEmpty()) {
                            log.debug("no ended tokens found");
                            return;
                        }
                        StateInfo stateInfo = gateway.findStateInfo(process.getId(), true);
                        switch (stateInfo.state) {
                        case LEAVING: {
                            log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
                            for (Token tokenToPop : stateInfo.tokensToPop) {
                                tokenToPop.setAbleToReactivateParent(false);
                            }
                            Token parentToken = stateInfo.tokensToPop.get(0).getParent();
                            gateway.leave(new ExecutionContext(gateway.getProcessDefinition(), parentToken));
                            break;
                        }
                        case WAITING: {
                            log.warn("continue waiting on " + stateInfo.notPassedTransitions);
                            break;
                        }
                        case BLOCKING: {
                            log.error("failing process " + process.getId() + " execution because " + stateInfo.unreachableTransition
                                    + " cannot be passed by active tokens in nodes " + stateInfo.getActiveTokenNodeIds());
                            process.setExecutionStatus(ExecutionStatus.FAILED);
                            token.fail(new ProcessExecutionException(
                                    ProcessExecutionException.PARALLEL_GATEWAY_UNREACHABLE_TRANSITION, stateInfo.unreachableTransition));
                            break;
                        }
                        }
                    }

                    @Override
                    public String toString() {
                        return MoreObjects.toStringHelper(getClass()).add("processId", processId).add("gateway", gateway).toString();
                    }

                }.executeInTransaction(false);
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
        public void onTransactionComplete(UserTransaction transaction) {
            synchronized (ParallelGateway.class) {
                new TransactionalExecutor(transaction) {

                    @Override
                    protected void doExecuteInTransaction() throws Exception {
                        log.debug("Executing " + this);
                        ru.runa.wfe.execution.Process process = ApplicationContextFactory.getProcessDAO().getNotNull(processId);
                        TokenDao tokenDao = ApplicationContextFactory.getTokenDAO();
                        List<Token> failedTokens = tokenDao.findByProcessAndNodeIdAndExecutionStatus(process, gateway.getNodeId(),
                                ExecutionStatus.FAILED);
                        if (failedTokens.isEmpty()) {
                            log.warn("no failed tokens found");
                            return;
                        }
                        StateInfo stateInfo = gateway.findStateInfo(process.getId(), false);
                        switch (stateInfo.state) {
                        case LEAVING: {
                            log.debug("marking tokens as inactive " + stateInfo.tokensToPop);
                            for (Token tokenToPop : stateInfo.tokensToPop) {
                                tokenToPop.setAbleToReactivateParent(false);
                                tokenToPop.setExecutionStatus(ExecutionStatus.ENDED);
                            }
                            Token parentToken = stateInfo.tokensToPop.get(0).getParent();
                            gateway.leave(new ExecutionContext(gateway.getProcessDefinition(), parentToken));
                            break;
                        }
                        case WAITING: {
                            log.warn("leaving failed tokens " + failedTokens + " due to waiting on " + stateInfo.notPassedTransitions);
                            break;
                        }
                        case BLOCKING: {
                            if (stateInfo.getActiveTokenNodeIds().contains(gateway.getNodeId())) {
                                log.warn("leaving failed tokens " + failedTokens + " due to active token in this node");
                            } else {
                                log.error("failing process " + process.getId() + " execution because " + stateInfo.unreachableTransition
                                        + " cannot be passed by active tokens in nodes " + stateInfo.getActiveTokenNodeIds());
                                process.setExecutionStatus(ExecutionStatus.FAILED);
                            }
                            break;
                        }
                        }
                    }

                    @Override
                    public String toString() {
                        return MoreObjects.toStringHelper(getClass()).add("processId", processId).add("gateway", gateway).toString();
                    }

                }.executeInTransaction(false);
            }
        }
    }
}
