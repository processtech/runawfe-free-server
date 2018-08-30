package ru.runa.wfe.ss.dao;

import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
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
public class SubstitutionCriteriaDao extends GenericDao<SubstitutionCriteria> {

    public SubstitutionCriteriaDao() {
        super(SubstitutionCriteria.class);
    }

    @Override
    public SubstitutionCriteria create(SubstitutionCriteria entity) {
        entity.setCreateDate(new Date());
        return super.create(entity);
    }

    public SubstitutionCriteria getByName(String name) {
        val sc = QSubstitutionCriteria.substitutionCriteria;
        return queryFactory.selectFrom(sc).where(sc.name.eq(name)).fetchFirst();
    }

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria) {
        val s = QSubstitution.substitution;
        return queryFactory.selectFrom(s).where(s.criteria.eq(criteria)).fetch();
    }
}
