package ru.runa.wfe.var.file;

import java.io.File;
import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.CurrentVariable;

import com.google.common.base.Objects;
import com.google.common.io.Files;

/**
 * This class eliminates need to persist large bytes array in database.
 * 
 * @author dofs
 * @since 4.0
 */
public class LocalFileSystemVariable implements FileVariable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String contentType;
    private String variablePath;

    public LocalFileSystemVariable() {
    }

    public LocalFileSystemVariable(CurrentVariable<?> variable, String variableName, FileVariable fileVariable) {
        name = fileVariable.getName();
        contentType = fileVariable.getContentType();
        long version = variable.getVersion() != null ? variable.getVersion() + 1 : 0;
        StringBuilder b = new StringBuilder();
        for (char ch : variableName.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                b.append(ch);
            } else {
                b.append('_');
            }
        }
        variablePath = variable.getProcess().getId() + "/" + b + "/" + version;
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public byte[] getData() {
        File file = LocalFileSystemStorage.getContentFile(variablePath, false);
        try {
            return Files.toByteArray(file);
        } catch (IOException e) {
            throw new InternalApplicationException("Unable to read file variable from '" + file + "'", e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getStringValue() {
        return variablePath;
    }

    @Override
    public int hashCode() {
        return variablePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalFileSystemVariable) {
            LocalFileSystemVariable f = (LocalFileSystemVariable) obj;
            return Objects.equal(variablePath, f.variablePath);
        }
        return false;
    }

}
