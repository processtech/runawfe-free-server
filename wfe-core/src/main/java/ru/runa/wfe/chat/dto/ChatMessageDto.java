package ru.runa.wfe.chat.dto;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.chat.ChatMessage;

public class ChatMessageDto {
    private ChatMessage message;
    private List<String> fileNames = new ArrayList<String>();
    private List<Long> fileIds = new ArrayList<Long>();

    public ChatMessageDto(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
}
