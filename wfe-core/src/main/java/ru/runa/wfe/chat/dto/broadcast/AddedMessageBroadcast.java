package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatFileDto;
import ru.runa.wfe.user.Actor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AddedMessageBroadcast extends MessageBroadcast {

    @JsonProperty("files")
    private List<ChatFileDto> filesDto = new ArrayList<>();
    @JsonProperty("old")
    private boolean isOld = false;
    @JsonProperty("coreUser")
    private boolean isCoreUser = false;
    private Actor createActor;
    private String text;
    private String quotedMessageIds;
    private Date createDate;
    private Long id;

    public AddedMessageBroadcast(ChatMessage message) {
        this.createActor = message.getCreateActor();
        this.text = message.getText();
        this.quotedMessageIds = message.getQuotedMessageIds();
        this.createDate = message.getCreateDate();
        this.id = message.getId();
    }
}
