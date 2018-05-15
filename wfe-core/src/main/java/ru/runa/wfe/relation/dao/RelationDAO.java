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
package ru.runa.wfe.relation.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.relation.QRelation;
import ru.runa.wfe.relation.QRelationPair;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;

/**
 * Relation dao implementation via Hibernate.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.3
 */
@Component
public class RelationDAO extends GenericDAO<Relation> {

    @Override
    protected void checkNotNull(Relation entity, Object identity) {
        if (entity == null) {
            throw new RelationDoesNotExistException(identity);
        }
    }

    @Override
    public Relation create(Relation relation) {
        if (get(relation.getName()) != null) {
            throw new RelationAlreadyExistException(relation.getName());
        }
        return super.create(relation);
    }

    /**
     * Return {@link Relation} with specified name or throws
     * {@link RelationDoesNotExistException} if relation with such name does not
     * exists.
     * 
     * @param name
     *            Relation name
     * @return Relation with specified name.
     */
    public Relation getNotNull(String name) {
        Relation relation = get(name);
        checkNotNull(relation, name);
        return relation;
    }

    public Relation get(String name) {
        QRelation r = QRelation.relation;
        return queryFactory.selectFrom(r).where(r.name.eq(name)).fetchFirst();
    }

    @Override
    public void delete(Long id) {
        QRelationPair rp = QRelationPair.relationPair;
        queryFactory.delete(rp).where(rp.relation.id.eq(id)).execute();
        super.delete(id);
    }
}
