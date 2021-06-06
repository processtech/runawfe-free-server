package ru.runa.wfe.office.storage.handler;

import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.excel.OnSheetConstraints;
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
import ru.runa.wfe.datasource.DataSourceStuff;

/**
 * @author Alekseev Mikhail
 * @since #1507
 */
public class InternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {
	private static final Log log = LogFactory.getLog(InternalStorageHandler.class);
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
        DataBinding binding = null;
        final StoreHelper storeHelper = new StoreHelper(config, variableProvider, storeService);
        
        if (config.getInputFilePath().substring(DataSourceStuff.PATH_PREFIX_DATA_SOURCE.length()+1).equals(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME)){
             binding = Iterables.getOnlyElement(config.getBindings());
        }
        else {
            for (DataBinding variableBinding : config.getBindings()) {
                WfVariable variableBind = variableProvider.getVariableNotNull(variableBinding.getVariableName());
                if (variableBind.getDefinition().getUserType() != null) {
                    binding = variableBinding;
                }
            }
        }
        
        try {
            final ExecutionResult executionResult = execute(variableProvider, binding, storeHelper);

            if (executionResult.isNeedReturn()) {
                result.put(config.getOutputFileVariableName() != null ?
                        config.getOutputFileVariableName() : binding.getVariableName(), executionResult.getValue());
            }
            return result;
        } catch (Exception e) {
            log.error("Error while executing operation with DataStore", e);
            throw new InternalApplicationException(e);
        }
    }

    protected ExecutionResult execute(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
    	binding.getConstraints().applyPlaceholders(variableProvider);
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        switch (config.getQueryType()) {
            case INSERT: {
                storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
                log.warn("binding name" + binding.getVariableName());
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
