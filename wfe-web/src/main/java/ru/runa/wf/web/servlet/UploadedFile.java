package ru.runa.wf.web.servlet;

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
public class UploadedFile {
    private String name;
    private String size;
    private String mimeType;
    private byte[] content;
    private IFileVariable fileVariable;

    /**
     * Bug fix #1095(http://sourceforge.net/p/runawfe/bugs/1095/) Indicates that the user has uploaded a file to the server
     */
    private boolean flagFor1095;

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

    public boolean isFlagFor1095() {
        return flagFor1095;
    }

    public void setFlagFor1095(boolean flagFor1095) {
        this.flagFor1095 = flagFor1095;
    }
}