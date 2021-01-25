package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEditMessageResponseDto extends ChatDto {
    private Long messageId;
    private String messageText;
}
