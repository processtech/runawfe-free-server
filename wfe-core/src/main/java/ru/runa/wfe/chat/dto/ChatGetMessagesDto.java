package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatGetMessagesDto extends ChatDto {
    private int count;
    private Long lastMessageId;
    private Long processId;

    @JsonGetter("count")
    public int getCount() {
        return count;
    }

    @JsonSetter("count")
    public void setCount(int count) {
        this.count = count;
    }

    @JsonGetter("lastMessageId")
    public Long getLastMessageId() {
        return lastMessageId;
    }

    @JsonSetter("lastMessageId")
    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @JsonGetter("processId")
    public Long getProcessId() {
        return processId;
    }

    @JsonSetter("processId")
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

}
