/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
