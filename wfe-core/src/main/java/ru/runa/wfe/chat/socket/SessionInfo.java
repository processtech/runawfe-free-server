package ru.runa.wfe.chat.socket;

import lombok.Getter;
import lombok.Setter;
import javax.websocket.Session;
import java.util.Objects;

@Getter
@Setter
public class SessionInfo {
    String id;
    Session session;

    public SessionInfo(Session session) {
        this.session = session;
        this.id = this.session.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
