package ru.runa.wfe.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.chat.ChatMessage;

/**
 * @author Sergey Inyakin
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatMessageFileDto extends ChatDto{
    private Long id;
    private ChatMessage message;
    private String fileName;
    private byte[] bytes;

    public ChatMessageFileDto(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }
}
