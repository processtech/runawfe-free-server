package ru.runa.wfe.chat.socket;

import java.util.Objects;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class SessionInfo {
    private final String id;
    private final WebSocketSession session;

    public SessionInfo(WebSocketSession session) {
        this.session = session;
        this.id = this.session.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
