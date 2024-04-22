package ru.runa.wfe.chat.dto.broadcast;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;

@Getter
@Setter
public class MessageEditedBroadcast extends MessageBroadcast {
    private static final long serialVersionUID = -1506143159564256233L;

    private Long id;
    private String text;
    private String initiator;
    private List<ChatMessageFileDetailDto> files = new ArrayList<>();

    public MessageEditedBroadcast(Long processId, Long id, String text, String initiator, List<ChatMessageFileDetailDto> files) {
        super(processId);
        this.id = id;
        this.text = text;
        this.initiator = initiator;
        this.files = files;
    }
}
