package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageFileDetailDto extends ChatDto {
    private Long id;
    private String name;
}