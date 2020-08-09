package ru.runa.wfe.office.storage.handler;

import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.UserTypeFormat;

public class ExternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {

    @Override
    protected FilesSupplierConfigParser<DataBindings> createParser() {
        return new StorageBindingsParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) {
        final Map<String, Object> result = new HashMap<>();
        final StoreService storeService = StoreServiceFactory.create(
                DataSourceStorage.parseDataSource(config.getInputFilePath(), variableProvider),
                variableProvider
        );
        final StoreHelper storeHelper = new StoreHelperImpl(config, variableProvider, storeService);

        for (DataBinding binding : config.getBindings()) {
            final ExecutionResult executionResult = execute(variableProvider, binding, storeHelper);
            if (executionResult.isNeedReturn()) {
                result.put(binding.getVariableName(), executionResult.getValue());
            }
        }
        return result;
    }

    protected ExecutionResult execute(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) {
        binding.getConstraints().applyPlaceholders(variableProvider);
        switch (config.getQueryType()) {
            case INSERT:
            case UPDATE: {
                final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                return storeHelper.execute(binding, variable);
            }
            case SELECT: {
                final WfVariable variable = variableProvider.getVariableNotNull(config.getOutputFileVariableName());
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                return storeHelper.execute(binding, variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName()));
            }
            default:
                final UserType userType = variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName());
                storeHelper.setVariableFormat(new UserTypeFormat(userType));
                return storeHelper.execute(binding, userType);
        }
    }
}
