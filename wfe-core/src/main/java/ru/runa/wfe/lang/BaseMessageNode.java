package ru.runa.wfe.lang;

import ru.runa.wfe.lang.bpmn2.MessageEventType;

public abstract class BaseMessageNode extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    public static final String EVENT_TYPE = "event_type";
    public static final String ERROR_EVENT_PROCESS_ID = "processId";
    public static final String ERROR_EVENT_NODE_ID = "processNodeId";
    public static final String ERROR_EVENT_TOKEN_ID = "tokenId";
    public static final String ERROR_EVENT_MESSAGE = "error_event_message";
    public static final String EXPIRATION_PROPERTY = "_expiration";
    private MessageEventType eventType = MessageEventType.message;

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

}
