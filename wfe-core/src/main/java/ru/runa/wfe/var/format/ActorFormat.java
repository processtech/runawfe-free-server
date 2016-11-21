package ru.runa.wfe.var.format;

import ru.runa.wfe.user.Actor;

public class ActorFormat extends ExecutorFormat {

    @Override
    public Class<Actor> getJavaClass() {
        return Actor.class;
    }

}
