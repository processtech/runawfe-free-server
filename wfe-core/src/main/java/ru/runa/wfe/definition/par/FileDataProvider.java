package ru.runa.wfe.definition.par;

import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.IFileDataProvider;

public abstract class FileDataProvider implements IFileDataProvider {

    @Override
    public byte[] getFileDataNotNull(String fileName) {
        byte[] data = getFileData(fileName);
        if (data == null) {
            throw new DefinitionFileDoesNotExistException(fileName);
        }
        return data;
    }

}
