package ru.runa.wfe.office.storage.handler;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
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
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.logic.InternalStorageReferenceService;

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
        final StoreService storeService = StoreServiceFactory.create(
                DataSourceStorage.parseDataSource(config.getInputFilePath(), variableProvider),
                variableProvider
        );
        final StoreHelper storeHelper = new StoreHelper(config, variableProvider, storeService);

        final DataBinding binding = Iterables.getOnlyElement(config.getBindings());
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
        switch (config.getQueryType()) {
            case INSERT:  return executeInsert(variableProvider, binding, storeHelper);
            case UPDATE:  return executeUpdate(variableProvider, binding, storeHelper);
            case SELECT:  return executeSelect(variableProvider, binding, storeHelper);
            case DELETE:  return executeDelete(variableProvider, binding, storeHelper);
            default: throw new IllegalStateException("Unexpected value: " + config.getQueryType());
        }
    }

    private ExecutionResult executeInsert(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        if (UserType.isByReferenceVariable(variable)) {
            log.warn("byReference: skipping INSERT for variable '" + variable.getDefinition().getName()
                    + "' — insert is automatic for byReference types");
            return ExecutionResult.EMPTY;
        }
        storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
        return storeHelper.save(binding, variable);
    }

    private ExecutionResult executeUpdate(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
        final WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
        if (UserType.isByReferenceVariable(variable)) {
            log.warn("byReference: skipping UPDATE for variable '" + variable.getDefinition().getName()
                    + "' — update is automatic for byReference types");
            return ExecutionResult.EMPTY;
        }
        storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
        return storeHelper.update(binding, variable, config.getCondition());
    }

    private ExecutionResult executeSelect(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
        final WfVariable variable = variableProvider.getVariableNotNull(config.getOutputFileVariableName());
        if (UserType.isByReferenceVariable(variable)) {
            UserType userType = storeHelper.userType(variable);
            List<UserTypeMap> found = ApplicationContextFactory.getInternalStorageReferenceService()
                    .findByFilter(userType, config.getCondition(), variableProvider);
            List<UserTypeMap> idOnlyList = new ArrayList<>(found.size());
            for (UserTypeMap m : found) {
                UserTypeMap idOnly = new UserTypeMap(userType);
                idOnly.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME,
                        m.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME));
                idOnlyList.add(idOnly);
            }
            return new ExecutionResult(idOnlyList);
        }
        storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
        return storeHelper.findByFilter(
                binding,
                variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName()),
                config.getCondition()
        );
    }

    private ExecutionResult executeDelete(VariableProvider variableProvider, DataBinding binding, StoreHelper storeHelper) throws Exception {
        final UserType userType = variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName());
        storeHelper.setVariableFormat(new UserTypeFormat(userType));
        if (userType.isByReference()) {
            return executeDeleteByReference(variableProvider, binding, userType);
        }
        return storeHelper.delete(binding, userType, config.getCondition());
    }

    private ExecutionResult executeDeleteByReference(VariableProvider variableProvider, DataBinding binding, UserType userType) {
        if (binding.getVariableName() != null) {
            return deleteByReferenceVariable();
        }
        return deleteByReferenceCondition(variableProvider, userType);
    }

    private ExecutionResult deleteByReferenceVariable() {
        return new ExecutionResult(null);
    }

    private ExecutionResult deleteByReferenceCondition(VariableProvider variableProvider, UserType userType) {
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
        List<UserTypeMap> found = refService.findByFilter(userType, config.getCondition(), variableProvider);
        for (UserTypeMap item : found) {
            Object rawId = item.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
            if (rawId instanceof Number) {
                long id = ((Number) rawId).longValue();
                refService.delete(userType, id);
                log.info("byReference DELETE: type='" + userType.getName() + "', id=" + id + ", values=" + item);
            }
        }
        log.info("byReference: condition DELETE completed for type='" + userType.getName()
                + "', deleted=" + found.size() + ", condition='" + config.getCondition() + "'");
        return ExecutionResult.EMPTY;
    }
}
