package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import ru.runa.wfe.chat.ChatMessage;

public class ChatMessageDto extends ChatDto {
    private ChatMessage message;
    private List<ChatFileDto> filesDto = new ArrayList<ChatFileDto>();
    private boolean old = false;
    private boolean mentionedFlag = false;
    private boolean coreUserFlag = false;

    public ChatMessageDto(ChatMessage message) {
        this();
        this.message = message;
    }

    public ChatMessageDto() {
        this.setMessageType("newMessage");
    }

    @JsonGetter("message")
    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    @JsonGetter("old")
    public boolean isOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }

    @JsonGetter("mentioned")
    public boolean isMentioned() {
        return mentionedFlag;
    }

    public void setMentionedFlag(boolean mentionedFlag) {
        this.mentionedFlag = mentionedFlag;
    }

    @JsonGetter("coreUser")
    public boolean isCoreUser() {
        return coreUserFlag;
    }

    public void setCoreUserFlag(boolean coreUserFlag) {
        this.coreUserFlag = coreUserFlag;
    }

    @JsonGetter("hierarchyMessageFlag")
    public Boolean isHierarchyMessageFlag() {
        return StringUtils.isNotBlank(this.getMessage().getQuotedMessageIds());
    }

    @JsonGetter("haveFile")
    public Boolean haveFile() {
        if (this.getFilesDto() != null) {
            return this.getFilesDto().size() > 0;
        } else {
            return false;
        }
    }

    @JsonGetter("fileArray")
    public List<ChatFileDto> getFilesDto() {
        return filesDto;
    }

    public void setFilesDto(List<ChatFileDto> filesDto) {
        this.filesDto = filesDto;
    }



}
