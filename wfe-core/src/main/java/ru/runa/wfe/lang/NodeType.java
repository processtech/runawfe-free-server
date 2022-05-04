package ru.runa.wfe.lang;

public enum NodeType {
    // TODO rename with patch appropriate to Node names
    START_EVENT,
    ACTION_NODE,
    END_PROCESS,
    WAIT_STATE,
    TIMER,
    TASK_STATE,
    FORK,
    JOIN,
    DECISION,
    SUBPROCESS,
    MULTI_SUBPROCESS,
    SEND_MESSAGE,
    RECEIVE_MESSAGE,
    END_TOKEN,
    MULTI_TASK_STATE,
    MERGE,
    EXCLUSIVE_GATEWAY,
    PARALLEL_GATEWAY,
    TEXT_ANNOTATION;

    public String getLabelKey() {
        return "node.type." + name();
    }
}