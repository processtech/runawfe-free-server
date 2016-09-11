package ru.runa.wfe.lang.bpmn2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
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
        Set<Token> arrivedTokens = Sets.newHashSet(token);
        Set<String> activeTokenNodeIds = Sets.newHashSet();
        if (getArrivingTransitions().size() > 1) {
            // #850 don't end root token
            token.end(executionContext, null);
        }
        fillTokensInfo(executionContext.getProcess().getRootToken(), arrivedTokens, activeTokenNodeIds);
        List<Token> tokensToPop = Lists.newArrayList();
        List<Transition> notPassedTransitions = Lists.newArrayList();
        for (Transition transition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token arrivedToken : arrivedTokens) {
                if (Objects.equal(transition.getNodeId(), arrivedToken.getTransitionId())
                        || Objects.equal(transition.getNodeIdBackCompatibilityPre4_3_0(), arrivedToken.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    tokensToPop.add(arrivedToken);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                notPassedTransitions.add(transition);
            }
        }
        if (notPassedTransitions.isEmpty()) {
            log.debug("marking tokens as inactive " + tokensToPop);
            for (Token arrivedToken : tokensToPop) {
                arrivedToken.setAbleToReactivateParent(false);
            }
            if (getArrivingTransitions().size() > 1 && token.getParent() != null) {
                Token parentToken = token.getParent();
                log.debug("passed with first parent " + parentToken);
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            } else {
                log.debug("passed with this " + token);
                leave(executionContext);
            }
        } else {
            log.debug("execution blocked in " + this + " due to waiting on " + notPassedTransitions);
            boolean markProcessFailedExecutionStatus = false;
            for (Transition transition : notPassedTransitions) {
                if (activeTokenNodeIds.contains(transition.getNodeId())) {
                    log.debug("concurrent token found for " + transition);
                    continue;
                }
                if (!transitionCanBePassed(transition, activeTokenNodeIds, new HashSet<Node>())) {
                    log.error("blocking " + executionContext.getProcess() + " execution because " + transition
                            + " will not be passed by tokens in nodes " + activeTokenNodeIds);
                    markProcessFailedExecutionStatus = true;
                }
            }
            if (markProcessFailedExecutionStatus) {
                executionContext.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                // TODO set process error = no token can activate this node
            }
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

    private void fillTokensInfo(Token token, Set<Token> arrivedTokens, Set<String> activeTokenNodeIds) {
        if (token.isAbleToReactivateParent() && token.hasEnded() && Objects.equal(token.getNodeId(), getNodeId())) {
            arrivedTokens.add(token);
        }
        if (token.isAbleToReactivateParent() && !token.hasEnded()) {
            if (Objects.equal(token.getNodeId(), getNodeId())) {
                // special case: concurred tokens
                activeTokenNodeIds.add(token.getTransitionId());
            } else {
                activeTokenNodeIds.add(token.getNodeId());
            }
        }
        for (Token childToken : token.getChildren()) {
            fillTokensInfo(childToken, arrivedTokens, activeTokenNodeIds);
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        Token token = executionContext.getToken();
        token.setAbleToReactivateParent(true);
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

    private void checkCyclicExecution(Token token) {
        if (token.getDepth() > SystemProperties.getTokenMaximumDepth()) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }

}
