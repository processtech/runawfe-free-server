package ru.runa.wfe.chat.socket;

import lombok.Getter;
import javax.websocket.Session;
import java.util.Objects;

@Getter
public class SessionInfo {
    private final String id;
    private final Session session;

    public SessionInfo(Session session) {
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
