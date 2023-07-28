package ru.runa.wfe.rest.auth;

import ru.runa.wfe.user.User;

public class AuthUser {

    private final User user;

    public AuthUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
