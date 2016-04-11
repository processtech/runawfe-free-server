package ru.runa.wfe.ss.dao;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;

/**
 * DAO for {@link SubstitutionCriteria}.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class SubstitutionCriteriaDAO extends GenericDAO<SubstitutionCriteria> {

    @Override
    public SubstitutionCriteria create(SubstitutionCriteria entity) {
        entity.setCreateDate(new Date());
        return super.create(entity);
    }

    public SubstitutionCriteria getByName(String name) {
        return findFirstOrNull("from SubstitutionCriteria where name=?", name);
    }

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        return (List<Substitution>) getHibernateTemplate().find("from Substitution where criteria=?", criteria);
    }

}
