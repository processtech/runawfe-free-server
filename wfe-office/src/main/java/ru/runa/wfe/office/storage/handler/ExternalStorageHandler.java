package ru.runa.wfe.office.storage.handler;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {

    @Override
    protected FilesSupplierConfigParser<DataBindings> createParser() {
        return new StorageBindingsParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        StoreHelper storeHelper = new StoreHelperImpl(config, variableProvider);
        for (DataBinding binding : config.getBindings()) {
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            binding.getConstraints().applyPlaceholders(variableProvider);
            storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
            ExecutionResult executionResult = storeHelper.execute(binding, variable);
            if (executionResult.isNeedReturn()) {
                result.put(binding.getVariableName(), executionResult.getValue());
            }
        }
        return result;
    }

}
