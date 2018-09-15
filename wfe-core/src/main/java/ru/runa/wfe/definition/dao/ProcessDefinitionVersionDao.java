package ru.runa.wfe.definition.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.QProcessDefinitionVersion;

@Component
public class ProcessDefinitionVersionDao extends GenericDao<ProcessDefinitionVersion> {

    public ProcessDefinitionVersionDao() {
        super(ProcessDefinitionVersion.class);
    }

    @Override
    protected void checkNotNull(ProcessDefinitionVersion entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    public void deleteAll(long processDefinitionId) {
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        queryFactory.delete(dv).where(dv.definition.id.eq(processDefinitionId)).execute();
    }
}
