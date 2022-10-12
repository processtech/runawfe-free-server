package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageFileDetailDto extends AbstractChatDto {
    private static final long serialVersionUID = -8973315918319737142L;

    private Long id;
    private String name;
}