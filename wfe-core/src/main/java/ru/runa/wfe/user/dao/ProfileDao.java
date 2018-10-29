package ru.runa.wfe.user.dao;

import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.QProfile;

/**
 * DAO for managing user profiles.
 * 
 * @author Konstantinov Aleksey 23.02.2012
 */
@Component
public class ProfileDao extends GenericDao<Profile> {

    public ProfileDao() {
        super(Profile.class);
    }

    /**
     * Load profile for user. Return null, if no profile for user can be found.
     * 
     * @param actor
     *            Actor to load profile.
     * @return Actor profile or null.
     */
    public Profile get(Actor actor) {
        val p = QProfile.profile;
        return queryFactory.selectFrom(p).where(p.actor.eq(actor)).fetchFirst();
    }

    /**
     * Removes profile for user.
     * 
     * @param actor
     *            Actor to remove profile.
     */
    public void delete(Actor actor) {
        Profile profile = get(actor);
        if (profile != null) {
            sessionFactory.getCurrentSession().delete(profile);
        }
    }
}
