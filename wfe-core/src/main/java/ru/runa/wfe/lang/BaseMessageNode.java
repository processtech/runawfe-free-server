package ru.runa.wfe.lang;

import ru.runa.wfe.lang.bpmn2.MessageEventType;

public abstract class BaseMessageNode extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    private MessageEventType eventType = MessageEventType.message;

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

}
