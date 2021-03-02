package ru.runa.wfe.chat.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.runa.wfe.user.User;
import javax.websocket.Session;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSessionUtils {

    public static User getUser(Session session) {
        return (User) session.getUserProperties().get("user");
    }
}
