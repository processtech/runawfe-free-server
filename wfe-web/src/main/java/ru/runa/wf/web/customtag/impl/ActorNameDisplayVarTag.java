package ru.runa.wf.web.customtag.impl;

import ru.runa.wfe.user.Actor;

public class ActorNameDisplayVarTag extends AbstractActorVarTag {

    public String actorToString(Actor actor) {
        return actor.getName();
    }
}
