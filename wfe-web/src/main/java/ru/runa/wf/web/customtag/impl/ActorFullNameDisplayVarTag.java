package ru.runa.wf.web.customtag.impl;

import ru.runa.wfe.user.Actor;

/**
 * Created on 09.05.2005
 * 
 */
public class ActorFullNameDisplayVarTag extends AbstractActorVarTag {

    public String actorToString(Actor actor) {
        return actor.getFullName();
    }
}
