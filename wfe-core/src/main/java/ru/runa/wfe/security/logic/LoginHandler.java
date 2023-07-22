package ru.runa.wfe.security.logic;

import ru.runa.wfe.user.Actor;

public interface LoginHandler {

    public void onUserLogin(Actor actor, AuthType type);
}