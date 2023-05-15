package ru.runa.wfe.definition.dao;

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.definition.QProcessDefinitionVersion;

/**
 * DAO for {@link ProcessDefinition}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class ProcessDefinitionDao extends GenericDao<ProcessDefinition> {

    public ProcessDefinitionDao() {
        super(ProcessDefinition.class);
    }

    @Override
    protected void checkNotNull(ProcessDefinition entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * @throws DefinitionDoesNotExistException If not found.
     */
    public ProcessDefinition getByName(@NonNull String definitionName) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        val o = queryFactory.selectFrom(d).from(d).where(d.name.eq(definitionName)).fetchFirst();
        if (o == null) {
            throw new DefinitionDoesNotExistException(definitionName);
        }
        return o;
    }


    public ProcessDefinitionWithVersion getByNameAndVersion(@NonNull String name, long version) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.definition, d)
                .where(d.name.eq(name).and(dv.version.eq(version)))
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(name + " v" + version);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     * @deprecated Use {@link #getByName(String)}.
     */
    public ProcessDefinitionWithVersion findLatestDefinition(@NonNull String definitionName) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.definition, d)
                .where(d.name.eq(definitionName))
                .orderBy(dv.version.desc())
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(definitionName);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     */
    public ProcessDefinitionWithVersion findLatestDefinition(long definitionId) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(d)
                .innerJoin(d.latestVersion, dv)
                .where(d.id.eq(definitionId))
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("definitionId = " + definitionId);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * Eager load both ProcessDefinition and ProcessDefinitionVersion. Probably this could be done "more Hibernate way"
     * (like marking @ManyToOne field ProcessDefinitionVersion.processDefinition as eager loaded if that's possible),
     * but this implementation is a step closer to getting rid of Hibernate.
     */
    public ProcessDefinitionWithVersion findDefinition(long processDefinitionVersionId) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv).from(dv).innerJoin(dv.definition, d).where(dv.id.eq(processDefinitionVersionId)).fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("processDefinitionVersionId = " + processDefinitionVersionId);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * queries the database for definition names.
     */
    public List<Long> findAllDefinitionIds() {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.selectDistinct(d.id).from(d).orderBy(d.id.asc()).fetch();
    }

    /**
     * Returns ids of all ProcessDefinitionVersion-s which belong to same ProcessDefinition as processDefinitionVersionId, ordered by version.
     */
    public List<Long> findAllDefinitionVersionIds(long processDefinitionVersionId, boolean ascending) {
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;

        // TODO This can be implemented as subquery (hopefully in Hibernate):
        Long definitionId = queryFactory.select(dv.definition.id).from(dv).where(dv.id.eq(processDefinitionVersionId)).fetchFirst();
        Preconditions.checkNotNull(definitionId);

        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.definition.id.eq(definitionId))
                .orderBy(ascending ? dv.version.asc() : dv.version.desc())
                .fetch();
    }

    public List<Long> findDefinitionVersionIds(String name, Long from, Long to) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .innerJoin(dv.definition, d)
                .where(d.name.eq(name).and(dv.version.between(from, to)))
                .orderBy(dv.version.asc())
                .fetch();
    }

    public Long findDefinitionVersionIdLatestVersionLessThan(long definitionId, long version) {
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.definition.id.eq(definitionId).and(dv.version.lt(version)))
                .orderBy(dv.version.desc())
                .fetchFirst();
    }

    public Long findDefinitionVersionIdLatestBeforeDate(String name, Date date) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .innerJoin(dv.definition, d)
                .where(d.name.eq(name).and(dv.createDate.lt(date))).orderBy(dv.version.desc()).fetchFirst();
    }

    /**
     * queries the database for all versions of process definitions with the given name, ordered by version (descending).
     */
    public List<ProcessDefinitionWithVersion> findAllDefinitionVersions(String name) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        List<Tuple> tt = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.definition, d)
                .where(d.name.eq(name))
                .orderBy(dv.version.desc())
                .fetch();

        // TODO After migrating to Spring5, use stream() with lambdas.
        val result = new ArrayList<ProcessDefinitionWithVersion>(tt.size());
        for (Tuple t : tt) {
            result.add(new ProcessDefinitionWithVersion(t.get(d), t.get(dv)));
        }
        return result;
    }
}
