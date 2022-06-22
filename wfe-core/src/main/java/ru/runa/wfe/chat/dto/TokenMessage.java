package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TokenMessage implements ClientMessage {
    private String payload;
}
