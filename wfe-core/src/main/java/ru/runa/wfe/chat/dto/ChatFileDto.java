package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatFileDto extends ChatDto {
    @JsonProperty("id")
    private Long fileId;
    @JsonProperty("name")
    private String fileName;
}
