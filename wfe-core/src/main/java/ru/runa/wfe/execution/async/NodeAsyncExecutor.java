package ru.runa.wfe.execution.async;

import ru.runa.wfe.execution.CurrentToken;

/**
 *
 * @author Alex Chernyshev
 */
public interface NodeAsyncExecutor {

    void execute(CurrentToken token, boolean retry);

}
