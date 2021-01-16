package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.olap4j.impl.Base64;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatNewMessageDto extends ChatDto {
    private String message;
    private String idHierarchyMessage;
    private boolean haveFile;
    private boolean isPrivate;
    private String privateNames;
    private String processId;
    private List<String> fileNames;
    private List<Long> fileSizes;
    private Map<String, byte[]> files;

    @JsonGetter("message")
    public String getMessage() {
        return message;
    }

    @JsonSetter("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonGetter("idHierarchyMessage")
    public String getIdHierarchyMessage() {
        return idHierarchyMessage;
    }

    @JsonSetter("idHierarchyMessage")
    public void setIdHierarchyMessage(String idHierarchyMessage) {
        this.idHierarchyMessage = idHierarchyMessage;
    }

    @JsonGetter("haveFile")
    public boolean getIsHaveFile() {
        return haveFile;
    }

    @JsonSetter("haveFile")
    public void setHaveFile(boolean haveFile) {
        this.haveFile = haveFile;
    }

    @JsonGetter("isPrivate")
    public boolean getIsPrivate() {
        return isPrivate;
    }

    @JsonSetter("isPrivate")
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @JsonGetter("privateNames")
    public String getPrivateNames() {
        return privateNames;
    }

    @JsonSetter("privateNames")
    public void setPrivateNames(String privateNames) {
        this.privateNames = privateNames;
    }

    @JsonGetter("processId")
    public String getProcessId() {
        return processId;
    }

    @JsonSetter("processId")
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @JsonGetter("fileNames")
    public List<String> getFileNames() {
        return fileNames;
    }

    @JsonSetter("fileNames")
    public void setFileNames(List<String> activeFileNames) {
        this.fileNames = activeFileNames;
    }

    @JsonGetter("fileSizes")
    public List<Long> getFileSizes() {
        return fileSizes;
    }

    @JsonSetter("fileSizes")
    public void setFileSizes(List<Long> fileSizes) {
        this.fileSizes = fileSizes;
    }

    @JsonGetter("files")
    public Map<String, byte[]> getFiles() {
        return files;
    }

    @JsonSetter("files")
    public void setFiles(Map<String, Object> map){
        Map<String, byte[]> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()){
            byte[] bytes = Base64.decode((String) entry.getValue());
            result.put(entry.getKey(), bytes);
        }
        this.files = result;
    }
}
