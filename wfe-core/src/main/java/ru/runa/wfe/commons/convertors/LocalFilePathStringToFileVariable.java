package ru.runa.wfe.commons.convertors;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import ru.runa.wfe.commons.TypeConvertor;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

public class LocalFilePathStringToFileVariable implements TypeConvertor {

    private static final String DEFAULT_MIME = "application/octet-stream";

    /**
     * Convert {@link java.lang.String} representation of file path to {@link FileVariableImpl}.
     *
     * @param object         {@link java.lang.String} representation of file path
     * @param classConvertTo {@link FileVariable} or {@link FileVariableImpl}
     * @param <T>            type {@link FileVariable} or {@link FileVariableImpl}
     * @return instance of {@link FileVariableImpl}
     */
    @Override
    public <T> T convertTo(Object object, Class<T> classConvertTo) {
        try {
            checkParams(object, classConvertTo);
            Path path = Paths.get(object.toString());
            File file = new File(path.toUri());
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            FileInputStream reportFileInputStream = new FileInputStream(file);
            byte[] fileContent = ByteStreams.toByteArray(reportFileInputStream);
            FileVariable fileVariable = new FileVariableImpl(file.getName(), fileContent, getMime(path));
            return (T) fileVariable;
        } catch (Exception e) {
            throw new IllegalStateException("conversion to file variable problem", e);
        }
    }

    private void checkParams(@NonNull Object object, Class classConvertTo) {
        if (!String.class.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("file path must be String type");
        }
        if (!(classConvertTo == FileVariableImpl.class || classConvertTo == FileVariable.class)) {
            throw new IllegalArgumentException("to FileVariableImpl conver only");
        }
    }

    private String getMime(Path path) throws IOException {
        String mime = Files.probeContentType(path);
        return mime != null && !mime.isEmpty() ? DEFAULT_MIME : mime;
    }

}