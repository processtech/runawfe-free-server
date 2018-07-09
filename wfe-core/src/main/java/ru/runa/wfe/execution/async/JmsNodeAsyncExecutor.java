package ru.runa.wfe.execution.async;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.Token;

/**
 *
 * @author Alex Chernyshev
 */
public class JmsNodeAsyncExecutor implements NodeAsyncExecutor {

    @Override
    public void execute(Token token, boolean retry) {
        Utils.sendNodeAsyncExecutionMessage(token, retry);
    }

}