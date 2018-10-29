package ru.runa.wfe.user.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.user.Actor;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

/**
 * Created on 14.12.2004
 */
@Entity
@Table(name = "ACTOR_PASSWORD")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class ActorPassword {
    private static final String DIGEST_ALGORITHM = "MD5";
    private Long actorId;
    private byte[] password;

    public ActorPassword() {
    }

    public ActorPassword(Actor actor, String password) {
        setActorId(actor.getId());
        setPassword(password);
    }

    @Id
    @Column(name = "ACTOR_ID", nullable = false)
    protected Long getActorId() {
        return actorId;
    }

    private void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    /**
     * @return encrypted password for an actor
     */
    @Lob
    @Column(name = "PASSWORD", nullable = false)
    protected byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setPassword(String password) {
        try {
            setPassword(MessageDigest.getInstance(DIGEST_ALGORITHM).digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actorId, password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ActorPassword other = (ActorPassword) obj;
        return Objects.equal(actorId, other.actorId) && Arrays.equals(password, other.password);
    }
}
