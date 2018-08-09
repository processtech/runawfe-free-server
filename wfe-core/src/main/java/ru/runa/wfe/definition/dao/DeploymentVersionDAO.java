package ru.runa.wfe.definition.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DeploymentVersion;

@Component
public class DeploymentVersionDAO extends GenericDAO<DeploymentVersion> {

    @Override
    protected void checkNotNull(DeploymentVersion entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }
}
