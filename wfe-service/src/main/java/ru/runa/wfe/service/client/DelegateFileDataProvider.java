package ru.runa.wfe.service.client;

import ru.runa.wfe.definition.par.FileDataProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Implementation which uses service call for each file data retrieval (through
 * RunaWFE delegates).
 * 
 * @author Dofs
 * @since 4.0.6
 */
public class DelegateFileDataProvider extends FileDataProvider {
    private final User user;
    private final Long definitionId;

    public DelegateFileDataProvider(User user, Long definitionId) {
        this.user = user;
        this.definitionId = definitionId;
    }

    @Override
    public byte[] getFileData(String fileName) {
        return Delegates.getDefinitionService().getProcessDefinitionFile(user, definitionId, fileName);
    }

}
