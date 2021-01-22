package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEditMessageDto extends ChatDto {
    private Long editMessageId;
    private String message;
    private Long processId;
}
