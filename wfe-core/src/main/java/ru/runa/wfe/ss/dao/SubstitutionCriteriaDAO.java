package ru.runa.wfe.ss.dao;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.ss.QSubstitution;
import ru.runa.wfe.ss.QSubstitutionCriteria;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;

/**
 * DAO for {@link SubstitutionCriteria}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class SubstitutionCriteriaDAO extends GenericDAO<SubstitutionCriteria> {

    @Override
    public SubstitutionCriteria create(SubstitutionCriteria entity) {
        entity.setCreateDate(new Date());
        return super.create(entity);
    }

    public SubstitutionCriteria getByName(String name) {
        QSubstitutionCriteria sc = QSubstitutionCriteria.substitutionCriteria;
        return queryFactory.selectFrom(sc).where(sc.name.eq(name)).fetchFirst();
    }

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        QSubstitution s = QSubstitution.substitution;
        return queryFactory.selectFrom(s).where(s.criteria.eq(criteria)).fetch();
    }
}
