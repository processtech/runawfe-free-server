package ru.runa.wf.web.servlet;

import java.io.Serializable;

import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Objects;

/*
 * Options not implemented: acceptFileTypes, maxFileSize
 *
 * Validation error breaks on next line
 *
 * Remove buttons in EditList component break on next lines
 *
 * IE file removal does not remove it on server-side eventually
 */
public class UploadedFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String size;
    private String mimeType;
    private byte[] content;
    private IFileVariable fileVariable;

    public UploadedFile() {
    }

    public UploadedFile(IFileVariable fileVariable) {
        this.name = fileVariable.getName();
        this.mimeType = fileVariable.getContentType();
        this.fileVariable = fileVariable;
    }

    public String getName() {
        return name;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    public String getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String fileType) {
        this.mimeType = fileType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        if (content.length > 1024 * 1024) {
            this.size = content.length / (1024 * 1024) + " Mb";
        } else {
            this.size = content.length / 1024 + " Kb";
        }
    }

    public IFileVariable getFileVariable() {
        return fileVariable;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).toString();
    }

}