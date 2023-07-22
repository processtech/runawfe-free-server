package ru.runa.wfe.office.storage.handler;

import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelper;
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
        final StoreHelper storeHelper = new StoreHelper(config, variableProvider, storeService);

        for (DataBinding binding : config.getBindings()) {
            try {
                final ExecutionResult executionResult = execute(variableProvider, binding, storeHelper);
                if (executionResult.isNeedReturn()) {
                    result.put(binding.getVariableName(), executionResult.getValue());
                }
            } catch (Exception e) {
                log.error("Error while executing operation with DataStore", e);
                throw new InternalApplicationException(e);
            }
        }
        return result;
    }

    protected ExecutionResult execute(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
        binding.getConstraints().applyPlaceholders(variableProvider);
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        switch (config.getQueryType()) {
            case INSERT: {
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                return storeHelper.save(binding, variable);
            }
            case UPDATE: {
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                return storeHelper.update(binding, variable, config.getCondition());
            }
            case SELECT: {
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                return storeHelper.findByFilter(binding, storeHelper.userType(variable), config.getCondition());
            }
            case DELETE:
                final UserType userType = storeHelper.userType(variable);
                storeHelper.setVariableFormat(new UserTypeFormat(userType));
                return storeHelper.delete(binding, userType, config.getCondition());
            default:
                throw new IllegalStateException("Unexpected value: " + config.getQueryType());
        }
    }
}
