package ru.runa.wfe.office.shared;

import com.google.common.io.Files;
import com.google.common.net.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

public abstract class FilesSupplierConfig {
    protected String inputFilePath;
    protected String inputFileVariableName;
    private String outputDirPath;
    private String outputFileName;
    private String outputFileVariableName;

    protected FilesSupplierConfig() {
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public void setInputFileVariableName(String inputFileVariableName) {
        this.inputFileVariableName = inputFileVariableName;
    }

    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setOutputFileVariableName(String outputFileVariableName) {
        this.outputFileVariableName = outputFileVariableName;
    }

    protected abstract MediaType getContentType();

    public abstract String getDefaultOutputFileName();

    public String getOutputFileName() {
        if (outputFileName == null) {
            return getDefaultOutputFileName();
        }
        return outputFileName;
    }

    public InputStream getFileInputStream(VariableProvider variableProvider, FileDataProvider fileDataProvider, boolean required) {
        if (inputFileVariableName != null) {
            return getInputStreamByVariableName(variableProvider, inputFileVariableName);
        }
        if (inputFilePath != null) {
            return getInputStreamByPath(variableProvider, fileDataProvider, inputFilePath);
        }
        if (required) {
            throw new InternalApplicationException("No input file defined in configuration");
        }
        return null;
    }

    protected InputStream getInputStreamByPath(VariableProvider variableProvider, FileDataProvider fileDataProvider, String inputFilePath) {
        if (inputFilePath.startsWith(FileDataProvider.PROCESS_FILE_PROTOCOL) || inputFilePath.startsWith(FileDataProvider.BOT_TASK_FILE_PROTOCOL)) {

            String inputFilePathWithoutProtocol = inputFilePath;
            if (inputFilePath.startsWith(FileDataProvider.PROCESS_FILE_PROTOCOL)) {
                inputFilePathWithoutProtocol = inputFilePath.substring(FileDataProvider.PROCESS_FILE_PROTOCOL.length());
            }
            if (inputFilePath.startsWith(FileDataProvider.BOT_TASK_FILE_PROTOCOL)) {
                inputFilePathWithoutProtocol = inputFilePath.substring(FileDataProvider.BOT_TASK_FILE_PROTOCOL.length());
            }

            byte[] data = fileDataProvider.getFileDataNotNull(inputFilePathWithoutProtocol);
            return new ByteArrayInputStream(data);
        }
        String path = (String) ExpressionEvaluator.evaluateVariableNotNull(variableProvider, inputFilePath);
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            try {
                return Files.asByteSource(file).openStream();
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to read input file from location '" + path + "'");
            }
        }
        return ClassLoaderUtil.getAsStreamNotNull(path, getClass());
    }

    protected InputStream getInputStreamByVariableName(VariableProvider variableProvider, String inputFileVariableName) {
        Object value = variableProvider.getValue(inputFileVariableName);
        if (value instanceof FileVariable) {
            FileVariable fileVariable = (FileVariable) value;
            return new ByteArrayInputStream(fileVariable.getData());
        }
        if (value instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) value);
        }
        throw new InternalApplicationException("Variable '" + inputFileVariableName + "' should contains a file");
    }

    public OutputStream getFileOutputStream(Map<String, Object> outputVariables, VariableProvider variableProvider, boolean required) {
        final String outputFileName = ExpressionEvaluator.process(null, getOutputFileName(), variableProvider, null);
        if (outputFileVariableName != null) {
            FileVariableImpl fileVariable = new FileVariableImpl(outputFileName, getContentType().toString());
            outputVariables.put(outputFileVariableName, fileVariable);
            return new FileVariableOutputStream(fileVariable);
        }
        if (outputDirPath != null) {
            File dir = new File(outputDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!dir.exists() || !dir.isDirectory()) {
                throw new InternalApplicationException("Unable to locate output directory '" + outputDirPath + "'");
            }
            File file = new File(dir, outputFileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new InternalApplicationException("Unable to create new output file in location '" + file.getAbsolutePath() + "'", e);
                }
            }
            try {
                return Files.asByteSink(file).openStream();
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to write output file to location '" + file.getAbsolutePath() + "'", e);
            }
        }
        if (required) {
            throw new InternalApplicationException("No output file defined in configuration");
        }
        return null;
    }

    public static class FileVariableOutputStream extends ByteArrayOutputStream {
        private final FileVariableImpl fileVariable;

        public FileVariableOutputStream(FileVariableImpl fileVariable) {
            this.fileVariable = fileVariable;
        }

        @Override
        public void close() throws IOException {
            super.close();
            fileVariable.setData(toByteArray());
        }
    }
}
