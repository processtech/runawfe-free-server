package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
import ru.runa.wfe.chat.jackson.serializers.ActorJacksonSerializer;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@NoArgsConstructor
public class MessageAddedBroadcast extends MessageBroadcast implements Serializable {

    private Long id;
    private String text;

    @JsonProperty("files")
    private List<ChatMessageFileDetailDto> files = new ArrayList<>();

    @JsonSerialize(using = ActorJacksonSerializer.class)
    private Actor author;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private Date createDate;
}
