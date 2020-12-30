package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ChatFileDto extends ChatDto {

    private Long fileId;
    private String fileName;

    public ChatFileDto() {
        super();
    }

    public ChatFileDto(Long fileId, String fileName) {
        super();
        this.fileId = fileId;
        this.fileName = fileName;
    }

    @JsonGetter("id")
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    @JsonGetter("name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
