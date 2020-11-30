package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.runa.wfe.chat.ChatMessage;

public class ChatMessageDto extends ChatDto {
    private ChatMessage message;
    private List<String> fileNames = new ArrayList<String>(); // объединить в массив файлов
    private List<Long> fileIds = new ArrayList<Long>(); //
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

    @JsonGetter("fileNames")
    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    @JsonGetter("fileIds")
    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
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
    public boolean isHierarchyMessageFlag() {
        return StringUtils.isNotBlank(this.getMessage().getQuotedMessageIds());
    }

    @JsonGetter("haveFile")
    public boolean haveFile() {
        return this.getFileNames().size() > 0;
    }

    @Override
    public String convert() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(this);
        //
        // JSONObject testJSON1 = super.convert();
        //
        JSONObject result = new JSONObject();
        result.put("messType", "newMessage");
        // JSONArray messagesArrayObject = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("id", this.getMessage().getId());
        messageObject.put("text", this.getMessage().getText());
        messageObject.put("author", this.getMessage().getCreateActor().getName());
        if (this.getFileNames() != null) {
            if (this.getFileNames().size() > 0) {
                messageObject.put("haveFile", true);
                JSONArray filesArrayObject = new JSONArray();
                for (int i = 0; i < this.getFileNames().size(); i++) {
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("id", this.getFileIds().get(i));
                    fileObject.put("name", this.getFileNames().get(i));
                    filesArrayObject.add(fileObject);
                }
                messageObject.put("fileIdArray", filesArrayObject);
            } else {
                messageObject.put("haveFile", false);
            }
        } else {
            messageObject.put("haveFile", false);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String createDateString = sdf.format(this.getMessage().getCreateDate());
        messageObject.put("dateTime", createDateString);
        messageObject.put("hierarchyMessageFlag", StringUtils.isNotBlank(this.getMessage().getQuotedMessageIds()));
        // messagesArrayObject.add(messageObject);
        // result.put("newMessage", 0);
        result.put("message", messageObject);
        // result.put("messages", messagesArrayObject);
        result.put("old", old);
        return res;
    }



}
