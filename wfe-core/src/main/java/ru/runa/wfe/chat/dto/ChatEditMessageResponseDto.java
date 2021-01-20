package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEditMessageResponseDto extends ChatDto {
    @JsonProperty("mesId")
    private Long messageId;
    @JsonProperty("newText")
    private String messageText;
}
