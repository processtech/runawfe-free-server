package ru.runa.wfe.lang.bpmn2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
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
    protected void execute(ExecutionContext executionContext) throws Exception {
        Token token = executionContext.getToken();
        token.end(executionContext.getProcessDefinition(), null, null, false);
        log.debug("Executing " + this + " with " + token);
        StateInfo stateInfo = findStateInfo(executionContext.getProcess().getRootToken());
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
            Utils.failProcessExecution(token, new ProcessExecutionException(ProcessExecutionException.PARALLEL_GATEWAY_UNREACHABLE_TRANSITION,
                    stateInfo.unreachableTransition).getLocalizedMessage());
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

    protected StateInfo findStateInfo(Token rootToken) {
        StateInfo stateInfo = new StateInfo();
        fillTokensInfo(rootToken, stateInfo);
        for (Transition transition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token token : stateInfo.arrivedTokens) {
                if (token.getExecutionStatus() == ExecutionStatus.ACTIVE) {
                    continue;
                }
                if (isTransitionIdEqual(token, transition)) {
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
                boolean transitionIsReachable = false;
                for (Token token : stateInfo.arrivedTokens) {
                    if (token.getExecutionStatus() == ExecutionStatus.ACTIVE && isTransitionIdEqual(token, transition)) {
                        transitionIsReachable = true;
                        break;
                    }
                }
                if (!transitionIsReachable && !transitionCanBePassed(transition, stateInfo.activeTokenNodeIds, new HashSet<Node>())) {
                    stateInfo.unreachableTransition = transition;
                    stateInfo.state = State.BLOCKING;
                    break;
                }
            }
        }
        return stateInfo;
    }

    private boolean isTransitionIdEqual(Token token, Transition transition) {
        return Objects.equal(transition.getNodeId(), token.getTransitionId())
                || Objects.equal(transition.getNodeIdBackCompatibilityPre4_3_0(), token.getTransitionId());
    }

    private void fillTokensInfo(Token token, StateInfo stateInfo) {
        if (token.isAbleToReactivateParent()) {
            if (Objects.equal(token.getNodeId(), getNodeId())) {
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

    private enum State {
        LEAVING,
        WAITING,
        BLOCKING
    }

    private static class StateInfo {
        private State state = State.WAITING;
        // arrived tokens to this gateway; candidates to pop up
        private Set<Token> arrivedTokens = Sets.newHashSet();
        // active node ids to check transition can be passed from them
        private Set<String> activeTokenNodeIds = Sets.newHashSet();
        // tokens to mark as completed join-cycle
        private List<Token> tokensToPop = Lists.newArrayList();
        private List<Transition> notPassedTransitions = Lists.newArrayList();
        private Transition unreachableTransition;
    }

}
