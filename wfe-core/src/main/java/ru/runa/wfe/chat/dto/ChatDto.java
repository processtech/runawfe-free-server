package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@JsonPropertyOrder({ "messType" })
@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonTypeInfo(use = JsonTypeInfo.Id.NAME) // правится
public class ChatDto {

    private String messageType;

    public String convert() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(this);
        return res;
    }

    public ChatDto load(String jsonObject) throws JsonParseException, JsonMappingException, IOException {
        return new ObjectMapper().reader(this.getClass()).readValue(jsonObject);
    }

    static public ChatDto load(String jsonObject, Class<? extends ChatDto> dtoClass) throws JsonProcessingException, IOException {
        return new ObjectMapper().reader(dtoClass).readValue(jsonObject);
    }

    @JsonGetter("messType")
    public String getMessageType() {
        return messageType;
    }

    @JsonSetter("messType")
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
