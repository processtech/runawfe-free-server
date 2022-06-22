package ru.runa.wfe.chat.dto.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddMessageRequest extends MessageRequest {
    private static final long serialVersionUID = -2343987208864174162L;

    private String message;
    private Boolean isPrivate = false;
    private Map<String, byte[]> files;
}
