package ru.runa.wfe.chat.dto.broadcast;

import ru.runa.wfe.chat.dto.ServerMessage;

public class AuthenticationRequired implements ServerMessage {
    private final boolean expired;

    private AuthenticationRequired(boolean expired) {
        this.expired = expired;
    }

    public static AuthenticationRequired missedToken() {
        return new AuthenticationRequired(false);
    }

    public static AuthenticationRequired expired() {
        return new AuthenticationRequired(true);
    }

    public boolean isExpired() {
        return expired;
    }
}
