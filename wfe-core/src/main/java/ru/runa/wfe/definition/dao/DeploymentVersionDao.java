package ru.runa.wfe.definition.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionVersion;

@Component
public class DeploymentVersionDao extends GenericDao<ProcessDefinitionVersion> {

    @Override
    protected void checkNotNull(ProcessDefinitionVersion entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }
}
