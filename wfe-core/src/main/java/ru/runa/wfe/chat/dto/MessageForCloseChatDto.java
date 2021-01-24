package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageForCloseChatDto extends ChatDto {
    private Long processId;
    @JsonProperty("mentioned")
    private boolean isMentioned = false;
    @JsonProperty("coreUser")
    private boolean isCoreUser = false;
}