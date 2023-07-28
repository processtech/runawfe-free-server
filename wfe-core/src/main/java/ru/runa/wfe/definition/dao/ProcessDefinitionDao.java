package ru.runa.wfe.definition.dao;

import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.definition.QProcessDefinitionPack;

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

    public List<Long> findIds(String name, Long from, Long to) {
        QProcessDefinitionPack p = QProcessDefinitionPack.processDefinitionPack;
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.select(d.id)
                .from(d)
                .innerJoin(d.pack, p)
                .where(p.name.eq(name).and(d.version.between(from, to)))
                .orderBy(d.version.asc())
                .fetch();
    }

    public ProcessDefinition getByNameAndVersion(@NonNull String name, Long version) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        ProcessDefinition t = queryFactory.select(d)
                .from(d)
                .where(d.pack.name.eq(name).and(d.version.eq(version)))
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(name + " v" + version);
        }
        return t;
    }

    public Long findLatestIdBeforeDate(String name, Date date) {
        QProcessDefinitionPack p = QProcessDefinitionPack.processDefinitionPack;
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.select(d.id)
                .from(d)
                .innerJoin(d.pack, p)
                .where(p.name.eq(name).and(d.createDate.lt(date))).orderBy(d.version.desc()).fetchFirst();
    }

    public List<ProcessDefinition> findAllByNameOrderByVersionDesc(String name) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.select(d).from(d).where(d.pack.name.eq(name)).orderBy(d.version.desc()).fetch();
    }

    public Long findLatestIdLessThan(String name, Long version) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.select(d.id)
                .from(d)
                .where(d.pack.name.eq(name).and(d.version.lt(version)))
                .orderBy(d.version.desc())
                .fetchFirst();
    }


    public void deleteAll(ProcessDefinitionPack pack) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        queryFactory.delete(d).where(d.pack.eq(pack)).execute();
    }
}
