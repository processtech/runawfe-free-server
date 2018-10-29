package ru.runa.wfe.user;

import java.io.Serializable;
import java.security.Principal;

/**
 * <p>
 * This class implements the <code>Principal</code> interface and represents a
 * logged actor.
 * 
 * <p>
 * Principals such as this may be associated with a particular
 * <code>Subject</code> to augment that <code>Subject</code> with an additional
 * identity. Refer to the <code>Subject</code> class for more information on how
 * to achieve this. Authorization decisions can then be based upon the
 * Principals associated with a <code>Subject</code>. Created on 16.07.2004
 */
public class User implements Principal, Serializable {
    private static final long serialVersionUID = 43549879345L;

    private Actor actor;
    private byte[] securedKey;

    protected User() {
    }

    public User(Actor actor, byte[] securedKey) {
        this.actor = actor;
        this.securedKey = securedKey;
    }

    @Override
    public String getName() {
        return actor.getName();
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public byte[] getSecuredKey() {
        return securedKey;
    }

    public void setSecuredKey(byte[] securedKey) {
        this.securedKey = securedKey;
    }

    @Override
    public String toString() {
        return actor.toString();
    }
}
