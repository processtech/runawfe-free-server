package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import ru.runa.wfe.chat.ChatMessage;

@NoArgsConstructor
@Getter
@Setter
public class ChatMessageDto extends ChatDto {
    private ChatMessage message;

    @JsonProperty("files")
    private List<ChatMessageFileDto> files = new ArrayList<>();

    @JsonProperty("old")
    private boolean isOld = false;

    @JsonProperty("coreUser")
    private boolean isCoreUser = false;

    public ChatMessageDto(ChatMessage message) {
        this.message = message;
    }

    @JsonGetter("hierarchyMessage")
    public Boolean isHierarchyMessage() {
        return StringUtils.isNotBlank(this.getMessage().getQuotedMessageIds());
    }

    @JsonGetter("haveFile")
    public Boolean haveFile() {
        if (this.getFiles() != null) {
            return this.getFiles().size() > 0;
        } else {
            return false;
        }
    }
}
