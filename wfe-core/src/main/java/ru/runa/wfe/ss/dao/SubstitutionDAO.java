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
package ru.runa.wfe.ss.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.Actor;

import com.google.common.collect.Lists;

/**
 * DAO level interface for managing {@linkplain Substitution}'s.
 * 
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class SubstitutionDAO extends GenericDAO<Substitution> {

    @Override
    public Substitution create(Substitution entity) {
        entity.setCreateDate(new Date());
        return super.create(entity);
    }

    @Override
    protected void checkNotNull(Substitution entity, Object identity) {
        if (entity == null) {
            throw new SubstitutionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain Substitution}'s by identity. Result
     * {@linkplain Substitution}'s order is not specified.
     * 
     * @param ids
     *            {@linkplain Substitution}'s identity to load.
     * @return Loaded {@linkplain Substitution}'s.
     */
    public List<Substitution> get(final List<Long> substitutionIds) {
        if (substitutionIds.size() == 0) {
            return Lists.newArrayList();
        }
        return (List<Substitution>) getHibernateTemplate().executeFind(new HibernateCallback<List<Substitution>>() {

            @Override
            public List<Substitution> doInHibernate(Session session) {
                Query query = session.createQuery("from Substitution where id in (:ids)");
                query.setParameterList("ids", substitutionIds);
                return query.list();
            }
        });
    }

    /**
     * Loads all {@linkplain Substitution}'s for {@linkplain Actor}. Loaded
     * {@linkplain Substitution}'s is ordered by substitution position.
     * 
     * @param actorId
     *            {@linkplain Actor} identity to load {@linkplain Substitution}
     *            's.
     * @return {@linkplain Substitution}'s for {@linkplain Actor}.
     */
    public List<Substitution> getByActorId(Long actorId, boolean orderByPositionAscending) {
        String order = orderByPositionAscending ? "asc" : "desc";
        return (List<Substitution>) getHibernateTemplate().find("from Substitution where actorId=? order by position " + order, actorId);
    }

    public void deleteAllActorSubstitutions(Long actorId) {
        List<Substitution> substitutions = getByActorId(actorId, true);
        for (Substitution substitution : substitutions) {
            delete(substitution);
        }
    }

}
