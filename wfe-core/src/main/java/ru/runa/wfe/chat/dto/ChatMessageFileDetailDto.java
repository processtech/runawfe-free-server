package ru.runa.wfe.chat.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageFileDetailDto extends ChatDto implements Serializable {
    private Long id;
    private String name;
}