package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatEditMessageDto extends ChatDto {
    private Long editMessageId;
    private String message;
    private Long processId;

    @JsonGetter("editMessageId")
    public Long getEditMessageId() {
        return editMessageId;
    }

    @JsonSetter("editMessageId")
    public void setEditMessageId(Long editMessageId) {
        this.editMessageId = editMessageId;
    }

    @JsonGetter("message")
    public String getMessage() {
        return message;
    }

    @JsonSetter("message")
    public void setMessage(String message) {
        this.message = message;
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
