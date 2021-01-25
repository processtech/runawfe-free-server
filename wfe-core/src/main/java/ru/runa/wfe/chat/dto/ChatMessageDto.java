package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import ru.runa.wfe.chat.ChatMessage;

@Getter
@Setter
public class ChatMessageDto extends ChatDto {
    private ChatMessage message;

    @JsonProperty("files")
    private List<ChatFileDto> filesDto = new ArrayList<ChatFileDto>();

    @JsonProperty("old")
    private boolean isOld = false;

    @JsonProperty("mentioned")
    private boolean isMentioned = false;

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
        if (this.getFilesDto() != null) {
            return this.getFilesDto().size() > 0;
        } else {
            return false;
        }
    }
}
