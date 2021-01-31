package ru.runa.wfe.chat.dto.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ErrorMessageBroadcast extends MessageBroadcast {
    private String message;
}
