package ru.runa.wfe.audit.presentation;

import java.io.Serializable;

public class FileValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private String fileName;

    public FileValue() {
    }

    public FileValue(Long logId, String fileName) {
        this.logId = logId;
        this.fileName = fileName;
    }

    public Long getLogId() {
        return logId;
    }

    public String getFileName() {
        return fileName;
    }

}
