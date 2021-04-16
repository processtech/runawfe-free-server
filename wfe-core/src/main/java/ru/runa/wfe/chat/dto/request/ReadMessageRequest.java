package ru.runa.wfe.chat.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadMessageRequest extends MessageRequest {
    private Long messageId;
}
