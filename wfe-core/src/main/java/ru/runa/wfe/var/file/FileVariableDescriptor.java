package ru.runa.wfe.var.file;

import java.io.File;
import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.io.Files;

/**
 * Used only for back compatibility to read from stream.
 *
 * @see LocalFileSystemVariable.
 *
 * @author dofs
 *
 */
@Deprecated
public class FileVariableDescriptor extends FileVariableImpl {
    private static final long serialVersionUID = 1L;
    private String variablePath;
    private transient byte[] localData;

    @Override
    public byte[] getData() {
        if (localData == null) {
            File file = LocalFileSystemStorage.getContentFile(variablePath, false);
            try {
                localData = Files.toByteArray(file);
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to read file variable from '" + file + "'", e);
            }
        }
        return localData;
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public String getStringValue() {
        return variablePath;
    }
}
