package ru.runa.wfe.lang;

import ru.runa.wfe.lang.bpmn2.MessageEventType;

public abstract class BaseMessageNode extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    public static final String EVENT_TYPE = "event_type";
    public static final String BUSINESS_EXCEPTION_MESSAGE = "business_exception_message";
    private MessageEventType eventType = MessageEventType.message;

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

}
