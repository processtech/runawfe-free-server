package ru.runa.wfe.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import ru.runa.wfe.chat.dto.AbstractChatDto;
import ru.runa.wfe.chat.dto.ClientMessage;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MessageRequest extends AbstractChatDto implements ClientMessage {
}
