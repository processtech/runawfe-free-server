package ru.runa.wfe.office.storage.handler;

import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.InternalStorageDispatcher;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelper;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * @author Alekseev Mikhail
 * @since #1507
 */
public class InternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {
    @Override
    protected FilesSupplierConfigParser<DataBindings> createParser() {
        return new StorageBindingsParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) {
        final Map<String, Object> result = new HashMap<>();
        final DataBinding binding = Iterables.getOnlyElement(config.getBindings());
        final Supplier<StoreHelper> storeHelperSupplier = () -> new StoreHelper(
                config,
                variableProvider,
                StoreServiceFactory.create(
                        DataSourceStorage.parseDataSource(config.getInputFilePath(), variableProvider),
                        variableProvider));
        try {
            final ExecutionResult executionResult = execute(variableProvider, binding, storeHelperSupplier);
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

    protected ExecutionResult execute(VariableProvider variableProvider, DataBinding binding, Supplier<StoreHelper> storeHelperSupplier)
            throws Exception {
        binding.getConstraints().applyPlaceholders(variableProvider);
        switch (config.getQueryType()) {
            case INSERT:  return executeInsert(variableProvider, binding, storeHelperSupplier);
            case UPDATE:  return executeUpdate(variableProvider, binding, storeHelperSupplier);
            case SELECT:  return executeSelect(variableProvider, binding, storeHelperSupplier);
            case DELETE:  return executeDelete(variableProvider, binding, storeHelperSupplier);
            default: throw new IllegalStateException("Unexpected value: " + config.getQueryType());
        }
    }

    private ExecutionResult executeInsert(VariableProvider variableProvider, DataBinding binding, Supplier<StoreHelper> storeHelperSupplier) {
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        return InternalStorageDispatcher.getInstance().save(variable,
                () -> prepareHelper(storeHelperSupplier, variable.getDefinition().getFormatNotNull()).save(binding, variable));
    }

    private ExecutionResult executeUpdate(VariableProvider variableProvider, DataBinding binding, Supplier<StoreHelper> storeHelperSupplier) {
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        return InternalStorageDispatcher.getInstance().update(variable,
                () -> prepareHelper(storeHelperSupplier, variable.getDefinition().getFormatNotNull())
                        .update(binding, variable, config.getCondition()));
    }

    private ExecutionResult executeSelect(VariableProvider variableProvider, DataBinding binding, Supplier<StoreHelper> storeHelperSupplier) {
        final WfVariable variable = variableProvider.getVariableNotNull(config.getOutputFileVariableName());
        final UserType userType = resolveUserType(variable);
        return InternalStorageDispatcher.getInstance().findIdsByFilter(
                variable, userType, config.getCondition(), variableProvider,
                () -> prepareHelper(storeHelperSupplier, variable.getDefinition().getFormatNotNull()).findByFilter(
                        binding,
                        variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName()),
                        config.getCondition()));
    }

    private ExecutionResult executeDelete(VariableProvider variableProvider, DataBinding binding, Supplier<StoreHelper> storeHelperSupplier) {
        final UserType userType = variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName());
        if (userType.isByReference() && binding.getVariableName() != null) {
            return new ExecutionResult(null);
        }
        return InternalStorageDispatcher.getInstance().delete(
                userType, config.getCondition(), variableProvider,
                () -> prepareHelper(storeHelperSupplier, new UserTypeFormat(userType))
                        .delete(binding, userType, config.getCondition()));
    }

    private static StoreHelper prepareHelper(Supplier<StoreHelper> supplier, VariableFormat format) {
        StoreHelper helper = supplier.get();
        helper.setVariableFormat(format);
        return helper;
    }

    private static UserType resolveUserType(WfVariable variable) {
        VariableDefinition def = variable.getDefinition();
        if (def.isUserType()) {
            return def.getUserType();
        }
        if (ListFormat.class.getName().equals(def.getFormatClassName())
                && def.getFormatComponentUserTypes() != null
                && def.getFormatComponentUserTypes().length != 0) {
            return def.getFormatComponentUserTypes()[0];
        }
        throw new InternalApplicationException("Variable type " + def.getFormat() + " not supported");
    }
}
