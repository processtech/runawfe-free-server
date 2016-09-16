package ru.runa.wfe.lang.bpmn2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ITransactionListener;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ParallelGateway extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.PARALLEL_GATEWAY;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        token.end(executionContext, null);
        log.debug("Executing " + this + " with " + token);
        StateInfo stateInfo = findStateInfo(executionContext.getProcess().getRootToken(), true);
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
            log.debug("blocking token " + token.getId() + " execution due to waiting on " + stateInfo.notPassedTransitions);
            break;
        }
        case BLOCKING: {
            log.warn("failing token " + token.getId() + " execution because " + stateInfo.unreachableTransition
                    + " cannot be passed by active tokens in nodes " + stateInfo.activeTokenNodeIds);
            token.setExecutionStatus(ExecutionStatus.FAILED);
            TransactionListeners.addListener(new ParallelGatewayPostExecutionCheck(this, executionContext.getProcess().getId()), false);
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

    protected StateInfo findStateInfo(Token rootToken, boolean ignoreFailedTokens) {
        StateInfo stateInfo = new StateInfo();
        fillTokensInfo(rootToken, stateInfo);
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
                if (!transitionCanBePassed(transition, stateInfo.activeTokenNodeIds, new HashSet<Node>())) {
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
            } else if (token.getExecutionStatus() == ExecutionStatus.ACTIVE) {
                stateInfo.activeTokenNodeIds.add(token.getNodeId());
            }
        }
        for (Token childToken : token.getChildren()) {
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

    private void checkCyclicExecution(Token token) {
        if (token.getDepth() > SystemProperties.getTokenMaximumDepth()) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }

    private static enum State {
        LEAVING,
        WAITING,
        BLOCKING
    }

    private static class StateInfo {
        private State state = State.WAITING;
        private Set<Token> arrivedTokens = Sets.newHashSet();
        private Set<String> activeTokenNodeIds = Sets.newHashSet();
        private List<Token> tokensToPop = Lists.newArrayList();
        private List<Transition> notPassedTransitions = Lists.newArrayList();
        private Transition unreachableTransition;
    }

    private static class ParallelGatewayPostExecutionCheck implements ITransactionListener {
        private final ParallelGateway gateway;
        private final Long processId;

        public ParallelGatewayPostExecutionCheck(ParallelGateway gateway, Long processId) {
            this.gateway = gateway;
            this.processId = processId;
        }

        @Override
        public void onTransactionComplete(UserTransaction transaction) {
            synchronized (ParallelGatewayPostExecutionCheck.class) {
                new TransactionalExecutor(transaction) {

                    @Override
                    protected void doExecuteInTransaction() throws Exception {
                        log.debug("Executing " + this);
                        ru.runa.wfe.execution.Process process = ApplicationContextFactory.getProcessDAO().getNotNull(processId);
                        TokenDAO tokenDAO = ApplicationContextFactory.getTokenDAO();
                        List<Token> failedTokens = tokenDAO.findByProcessAndNodeIdAndExecutionStatusIsFailed(process, gateway.getNodeId());
                        if (failedTokens.isEmpty()) {
                            log.warn("no failed tokens found");
                            return;
                        }
                        StateInfo stateInfo = gateway.findStateInfo(process.getRootToken(), false);
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
                            log.error("failing process " + process.getId() + " execution because " + stateInfo.unreachableTransition
                                    + " cannot be passed by active tokens in nodes " + stateInfo.activeTokenNodeIds);
                            process.setExecutionStatus(ExecutionStatus.FAILED);
                            Exception exception = new InternalApplicationException("no token can activate this node");
                            ProcessExecutionErrors.addProcessError(processId, gateway.getNodeId(), gateway.getName(), null, exception);
                            break;
                        }
                        }
                    }

                    @Override
                    public String toString() {
                        return Objects.toStringHelper(getClass()).add("processId", processId).add("gateway", gateway).toString();
                    }

                }.executeInTransaction(false);
            }
        }
    }
}
