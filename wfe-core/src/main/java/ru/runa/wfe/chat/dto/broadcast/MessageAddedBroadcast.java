package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
import ru.runa.wfe.chat.jackson.serializers.ActorJacksonSerializer;
import ru.runa.wfe.chat.jackson.serializers.DateJacksonSerializer;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@NoArgsConstructor
public class MessageAddedBroadcast extends MessageBroadcast {

    private String text;
    private String quotedMessageIds;
    private Long id;

    @JsonProperty("files")
    private List<ChatMessageFileDetailDto> filesDto = new ArrayList<>();

    @JsonProperty("old")
    private boolean isOld = false;

    @JsonProperty("coreUser")
    private boolean isCoreUser = false;

    @JsonSerialize(using = ActorJacksonSerializer.class)
    private Actor author;

    @JsonSerialize(using = DateJacksonSerializer.class)
    private Date createDate;
}
