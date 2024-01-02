package ru.runa.wfe.definition.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionWithContent;

/**
 * DAO for {@link ProcessDefinitionWithContent}.
 * 
 * @author dofs
 * @since 4.4
 */
@Component
public class ProcessDefinitionWithContentDao extends GenericDao<ProcessDefinitionWithContent> {

    public ProcessDefinitionWithContentDao() {
        super(ProcessDefinitionWithContent.class);
    }

    @Override
    protected void checkNotNull(ProcessDefinitionWithContent entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

}
