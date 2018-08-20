package ru.runa.wfe.execution.async;

import ru.runa.wfe.execution.Token;

/**
 *
 * @author Alex Chernyshev
 */
public interface NodeAsyncExecutor {

    void execute(Token token, boolean retry);

}
