package ru.runa.wfe.office.storage.handler;

import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.VariableProvider;

/**
 * @author Alekseev Mikhail
 * @since #1507
 */
public class InternalStorageHandler extends ExternalStorageHandler {
    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) {
        final Map<String, Object> result = new HashMap<>();
        final StoreService storeService = StoreServiceFactory.create(
                DataSourceStorage.parseDataSource(config.getInputFilePath(), variableProvider),
                variableProvider
        );
        final StoreHelper storeHelper = new StoreHelperImpl(config, variableProvider, storeService);

        final DataBinding binding = Iterables.getOnlyElement(config.getBindings());
        final ExecutionResult executionResult = execute(variableProvider, binding, storeHelper);

        if (executionResult.isNeedReturn()) {
            result.put(config.getOutputFileVariableName() != null ?
                    config.getOutputFileVariableName() : binding.getVariableName(), executionResult.getValue());
        }
        return result;
    }
}
