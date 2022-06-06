package ru.runa.wfe.chat.dto.request;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMessageRequest extends MessageRequest implements Serializable {
    private static final long serialVersionUID = -851411986695432630L;

    private Long editMessageId;
    private String message;
}
