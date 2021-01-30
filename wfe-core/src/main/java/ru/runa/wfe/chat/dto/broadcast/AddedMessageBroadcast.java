package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatFileDto;
import ru.runa.wfe.chat.jackson.serializers.ActorJacksonSerializer;
import ru.runa.wfe.chat.jackson.serializers.DateJacksonSerializer;
import ru.runa.wfe.user.Actor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AddedMessageBroadcast extends MessageBroadcast {

    private String text;
    private String quotedMessageIds;
    private Long id;

    @JsonProperty("files")
    private List<ChatFileDto> filesDto = new ArrayList<>();

    @JsonProperty("old")
    private boolean isOld = false;

    @JsonProperty("coreUser")
    private boolean isCoreUser = false;

    @JsonSerialize(using = ActorJacksonSerializer.class)
    private Actor author;

    @JsonSerialize(using = DateJacksonSerializer.class)
    private Date createDate;

    public AddedMessageBroadcast(ChatMessage message) {
        this.author = message.getCreateActor();
        this.text = message.getText();
        this.quotedMessageIds = message.getQuotedMessageIds();
        this.createDate = message.getCreateDate();
        this.id = message.getId();
        this.setProcessId(message.getProcess().getId());
    }
}
