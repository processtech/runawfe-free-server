package ru.runa.wfe.lang.bpmn2;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ParallelGateway extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.PARALLEL_GATEWAY;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        List<Token> arrivedTokens = Lists.newArrayList();
        fillArrivedInThisNodeTokensWhichCanActivateParent(executionContext.getProcess().getRootToken(), arrivedTokens);
        if (!arrivedTokens.contains(token)) {
            arrivedTokens.add(token);
        }
        List<Token> tokensToPop = Lists.newArrayList();
        boolean allArrivedTransitionArePassed = true;
        for (Transition arrivingTransition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token arrivedToken : arrivedTokens) {
                if (arrivingTransition.getNodeId().equals(arrivedToken.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    tokensToPop.add(arrivedToken);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                allArrivedTransitionArePassed = false;
                log.debug("execution blocked due to waiting on " + arrivingTransition);
                break;
            }
        }
        if (getArrivingTransitions().size() > 1) {
            // #850 don't end root token
            token.end(executionContext, null);
        }
        if (allArrivedTransitionArePassed) {
            log.debug("marking tokens as inactive " + tokensToPop);
            for (Token arrivedToken : tokensToPop) {
                arrivedToken.setAbleToReactivateParent(false);
            }
            if (getArrivingTransitions().size() > 1 && token.getParent() != null) {
                Token parentToken = token.getParent();
                log.debug("passed join with first parent " + parentToken);
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            } else {
                log.debug("marking token as active " + tokensToPop + " for subsequent execution");
                token.setAbleToReactivateParent(true);
                log.debug("passed join with this " + token);
                leave(executionContext, null);
            }
        }
    }

    private void fillArrivedInThisNodeTokensWhichCanActivateParent(Token parent, List<Token> tokens) {
        if (parent.isAbleToReactivateParent() && Objects.equal(parent.getNodeId(), getNodeId())) {
            tokens.add(parent);
        }
        for (Token childToken : parent.getChildren()) {
            fillArrivedInThisNodeTokensWhichCanActivateParent(childToken, tokens);
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        Token token = executionContext.getToken();
        checkCyclicExecution(token);
        Map<Token, Transition> childTokens = Maps.newHashMap();
        for (Transition leavingTransition : getLeavingTransitions()) {
            Token childToken = new Token(token, getNodeId() + "/" + leavingTransition.getNodeId());
            childTokens.put(childToken, leavingTransition);
        }
        ApplicationContextFactory.getCurrentSession().flush();
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
