package ru.runa.wf.logic.bot.webservice;

/**
 * Action to be performed if error response received from web service. 
 */
public enum ErrorResponseProcessingResult {
    /**
     * Ignore error in interaction and advice to next interaction. 
     */
    IGNORE,

    /**
     * Break task processing with error. Next time all interactions starts from beginning. 
     */
    BREAK,

    /**
     * Save state and stop task processing without error. Next time start from current
     * interaction. 
     */
    RETRY
}
