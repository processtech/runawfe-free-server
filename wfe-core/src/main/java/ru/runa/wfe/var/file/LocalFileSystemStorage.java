package ru.runa.wfe.var.file;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;

public class LocalFileSystemStorage implements FileVariableStorage {

    static File storageDir;

    static synchronized File getLocalFileStorage() {
        if (storageDir != null) {
            return storageDir;
        }
        storageDir = new File(SystemProperties.getLocalFileStoragePath());
        if (SystemProperties.isLocalFileStorageEnabled()) {
            storageDir.mkdirs();
        }
        return storageDir;
    }

    public static File getContentFile(String path, boolean create) {
        File file = new File(getLocalFileStorage(), path);
        if (create) {
            try {
                file.getParentFile().mkdirs();
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
    public Object save(ExecutionContext executionContext, Variable variable, Object object) {
        if (object instanceof FileVariable) {
            FileVariable fileVariable = (FileVariable) object;
            object = save(variable, fileVariable, null);
        } else {
            @SuppressWarnings("unchecked")
            List<FileVariable> list = (List<FileVariable>) object;
            for (int i = 0; i < list.size(); i++) {
                FileVariable fileVariable = list.get(i);
                fileVariable = save(variable, fileVariable, i);
                list.set(i, fileVariable);
            }
        }
        return object;
    }

    private FileVariable save(Variable variable, FileVariable fileVariable, Integer index) {
        if (SystemProperties.isLocalFileStorageEnabled() && fileVariable != null
                && fileVariable.getData().length > SystemProperties.getLocalFileStorageFileLimit()) {
            try {
                val variableName = index != null ? variable.getName() + index : variable.getName();
                val fileSystemVariable = new LocalFileSystemVariable(variable, variableName, fileVariable);
                Files.write(fileVariable.getData(), getContentFile(fileSystemVariable.getVariablePath(), true));
                return fileSystemVariable;
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to save file variable to local drive", e);
            }
        }
        return fileVariable;
    }
}
