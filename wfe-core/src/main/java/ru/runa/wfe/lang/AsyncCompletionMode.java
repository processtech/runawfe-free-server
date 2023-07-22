package ru.runa.wfe.lang;

/**
 * Describes behavior of asynchronous nodes in the process.
 * 
 * @author Dofs
 * @since 4.0.4
 */
public enum AsyncCompletionMode {
    /**
     * Async nodes should be ended at containing process completion
     */
    ON_PROCESS_END,
    /**
     * Async nodes should be ended at main process completion
     */
    ON_MAIN_PROCESS_END,
    /**
     * Async nodes should never be ended by the process engine
     */
    NEVER
}
