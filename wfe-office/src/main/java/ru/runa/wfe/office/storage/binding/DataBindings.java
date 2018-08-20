package ru.runa.wfe.office.storage.binding;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.office.shared.FilesSupplierConfig;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;

import com.google.common.net.MediaType;

public class DataBindings extends FilesSupplierConfig {

    private final List<DataBinding> bindings = new ArrayList<DataBinding>();

    private QueryType queryType;

    private String condition;

    @Override
    protected MediaType getContentType() {
        if (isFileNameBelongsToXLSX(getOutputFileName(), false)) {
            return MediaType.OOXML_SHEET;
        } else {
            return MediaType.MICROSOFT_EXCEL;
        }
    }

    public boolean isFileNameBelongsToXLSX(String fileName, boolean defaultValue) {
        if (fileName == null) {
            return defaultValue;
        }
        return fileName.endsWith("xlsx");
    }

    public boolean isInputFileXLSX(VariableProvider variableProvider, boolean defaultValue) {
        if (inputFileVariableName != null) {
            Object value = variableProvider.getValue(inputFileVariableName);
            if (value instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable) value;
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

    public List<DataBinding> getBindings() {
        return bindings;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
