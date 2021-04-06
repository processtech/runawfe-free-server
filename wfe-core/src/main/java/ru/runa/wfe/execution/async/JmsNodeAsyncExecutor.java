package ru.runa.wfe.execution.async;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.CurrentToken;

/**
 *
 * @author Alex Chernyshev
 */
public class JmsNodeAsyncExecutor implements NodeAsyncExecutor {

    @Override
    public void execute(CurrentToken token, boolean retry) {
        Utils.sendNodeAsyncExecutionMessage(token, retry);
    }

}