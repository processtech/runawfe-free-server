package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatGetMessagesDto extends ChatDto {
    private int count;
    private Long lastMessageId;
}
