package ru.runa.wfe.execution.async;

/**
 *
 * @author Alex Chernyshev
 */
public interface INodeAsyncExecutor {

    void execute(Long processId, Long tokenId, String nodeId);

}
