package ru.runa.wfe.chat.dto;

import java.io.Serializable;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.user.Actor;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class WfChatMessageBroadcast<T extends MessageBroadcast> implements Serializable {
    private static final long serialVersionUID = -5769862919262484584L;

    private final T broadcast;
    private final Set<Actor> recipients;
}
