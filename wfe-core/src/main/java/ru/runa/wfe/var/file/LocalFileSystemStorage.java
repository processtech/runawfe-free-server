package ru.runa.wfe.var.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;

import com.google.common.io.Files;

public class LocalFileSystemStorage implements IFileVariableStorage {
    private static File storageDir = new File(SystemProperties.getLocalFileStoragePath());

    static {
        if (SystemProperties.isLocalFileStorageEnabled()) {
            storageDir.mkdirs();
        }
    }

    public static File getContentFile(String path, boolean create) {
        File file = new File(storageDir, path);
        if (create) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to create file '" + file + "'");
            }
        }
        if (!file.exists()) {
            throw new InternalApplicationException("No file found by path '" + file + "'");
        }
        return file;
    }

    @Override
    public Object save(ExecutionContext executionContext, Variable<?> variable, Object object) {
        if (object instanceof IFileVariable) {
            IFileVariable fileVariable = (IFileVariable) object;
            object = save(variable, fileVariable, null);
        } else {
            List<IFileVariable> list = (List<IFileVariable>) object;
            for (int i = 0; i < list.size(); i++) {
                IFileVariable fileVariable = list.get(i);
                fileVariable = save(variable, fileVariable, i);
                list.set(i, fileVariable);
            }
        }
        return object;
    }

    private IFileVariable save(Variable<?> variable, IFileVariable fileVariable, Integer index) {
        if (SystemProperties.isLocalFileStorageEnabled() && fileVariable != null
                && fileVariable.getData().length > SystemProperties.getLocalFileStorageFileLimit()) {
            try {
                String variableName = index != null ? variable.getName() + index : variable.getName();
                LocalFileSystemVariable fileSystemVariable = new LocalFileSystemVariable(variable, variableName, fileVariable);
                Files.write(fileVariable.getData(), getContentFile(fileSystemVariable.getVariablePath(), true));
                return fileSystemVariable;
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to save file variable to local drive", e);
            }
        }
        return fileVariable;
    }
}
