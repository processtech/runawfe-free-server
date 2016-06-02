package ru.runa.wfe.office.excel.handler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.office.excel.ExcelDataStore;
import ru.runa.wfe.office.excel.ExcelStorable;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExcelSaveHandler extends OfficeFilesSupplierHandler<ExcelBindings> {

    @Override
    protected FilesSupplierConfigParser<ExcelBindings> createParser() {
        return new ExcelBindingsParser();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        ExcelDataStore dataStore = new ExcelDataStore();
        InputStream templateInputStream = config.getFileInputStream(variableProvider, fileDataProvider, true);
        Workbook workbook = dataStore.loadWorkbook(templateInputStream, config.isInputFileXLSX(variableProvider, false));
        for (ExcelBinding binding : config.getBindings()) {
            binding.getConstraints().applyPlaceholders(variableProvider);
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            if (variable.getValue() != null) {
                ExcelStorable storable = dataStore.create(binding.getConstraints());
                storable.setFormat(variable.getDefinition().getFormatNotNull());
                storable.setData(variable.getValue());
                dataStore.save(workbook, storable);
            } else {
                log.warn("Omitted binding as variable was null: " + binding.getVariableName());
            }
        }
        dataStore.saveWorkbook(workbook, config.getFileOutputStream(result, variableProvider, true));
        return result;
    }

}
