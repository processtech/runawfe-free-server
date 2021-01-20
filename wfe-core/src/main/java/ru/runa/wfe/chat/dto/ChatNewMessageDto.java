package ru.runa.wfe.chat.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatNewMessageDto extends ChatDto {
    private String message;
    private String idHierarchyMessage;
    private boolean isPrivate;
    private String privateNames;
    private String processId;
    private Map<String, byte[]> files;
}
