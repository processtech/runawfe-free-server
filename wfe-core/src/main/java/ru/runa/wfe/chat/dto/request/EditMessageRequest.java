package ru.runa.wfe.chat.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMessageRequest extends MessageRequest {
    private Long editMessageId;
    private String message;
}
