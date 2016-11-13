package ru.runa.wfe.office.excel.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.office.shared.FilesSupplierConfig;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.net.MediaType;

public class ExcelBindings extends FilesSupplierConfig {
    private final List<ExcelBinding> bindings = new ArrayList<ExcelBinding>();

    public static boolean isFileNameBelongsToXLSX(String fileName, boolean defaultValue) {
        if (fileName == null) {
            return defaultValue;
        }
        return fileName.endsWith("xlsx");
    }

    @Override
    protected MediaType getContentType() {
        if (isFileNameBelongsToXLSX(getOutputFileName(), false)) {
            return MediaType.OOXML_SHEET;
        } else {
            return MediaType.MICROSOFT_EXCEL;
        }
    }

    public boolean isInputFileXLSX(IVariableProvider variableProvider, boolean defaultValue) {
        if (inputFileVariableName != null) {
            Object value = variableProvider.getValue(inputFileVariableName);
            if (value instanceof IFileVariable) {
                IFileVariable fileVariable = (IFileVariable) value;
                return isFileNameBelongsToXLSX(fileVariable.getName(), defaultValue);
            }
            throw new InternalApplicationException("Variable '" + inputFileVariableName + "' should contains a file");
        }
        if (inputFilePath != null) {
            String path = (String) ExpressionEvaluator.evaluateVariableNotNull(variableProvider, inputFilePath);
            return isFileNameBelongsToXLSX(path, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public String getDefaultOutputFileName() {
        return "spreadsheet.xls";
    }

    public List<ExcelBinding> getBindings() {
        return bindings;
    }

    public static class FileVariableOutputStream extends ByteArrayOutputStream {
        private final FileVariable fileVariable;

        public FileVariableOutputStream(FileVariable fileVariable) {
            this.fileVariable = fileVariable;
        }

        @Override
        public void close() throws IOException {
            super.close();
            fileVariable.setData(buf);
        }
    }
}
