package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

public abstract class ChatDto {
    JSONObject convert() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String testJSON = mapper.writeValueAsString(this);
        JSONObject res = new JSONObject();
        res.put("value", testJSON);
        return res;
    }
}
