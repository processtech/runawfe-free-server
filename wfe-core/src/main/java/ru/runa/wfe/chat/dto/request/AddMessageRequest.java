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
    private String message;
    private String idHierarchyMessage;
    private boolean isPrivate;
    private Map<String, byte[]> files;
}
