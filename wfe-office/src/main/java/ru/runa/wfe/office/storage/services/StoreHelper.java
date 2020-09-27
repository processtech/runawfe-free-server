package ru.runa.wfe.office.storage.services;

import java.util.Properties;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class StoreHelper {
    StoreService storeService;

    DataBindings config;

    VariableFormat format;

    VariableProvider variableProvider;

    public StoreHelper(DataBindings config, VariableProvider variableProvider, StoreService storeService) {
        setConfig(config);
        this.variableProvider = variableProvider;
        this.storeService = storeService;
    }

    public UserType userType(WfVariable variable) throws InternalApplicationException {
        final VariableDefinition definition = variable.getDefinition();
        if (definition.isUserType()) {
            return definition.getUserType();
        }

        if (ListFormat.class.getName().equals(definition.getFormatClassName()) && definition.getFormatComponentUserTypes() != null
                && definition.getFormatComponentUserTypes().length != 0) {
            return definition.getFormatComponentUserTypes()[0];
        }

        throw new InternalApplicationException("Variable type" + definition.getFormat() + " not supported");
    }

    public void setVariableFormat(VariableFormat format) {
        this.format = format;
    }

    public ExecutionResult save(DataBinding binding, WfVariable variable) throws Exception {
        storeService.save(extractProperties(binding), variable, true);
        return ExecutionResult.EMPTY;
    }

    public ExecutionResult findByFilter(DataBinding binding, UserType userType, String condition) throws Exception {
        return storeService.findByFilter(extractProperties(binding), userType, condition);
    }

    public ExecutionResult update(DataBinding binding, WfVariable variable, String condition) throws Exception {
        storeService.update(extractProperties(binding), variable, condition);
        return ExecutionResult.EMPTY;
    }

    public ExecutionResult delete(DataBinding binding, UserType userType, String condition) throws Exception {
        storeService.delete(extractProperties(binding), userType, condition);
        return ExecutionResult.EMPTY;
    }

    private Properties extractProperties(DataBinding binding) {
        Properties properties = new Properties();
        properties.setProperty(StoreService.PROP_PATH, config.getInputFilePath());
        properties.put(StoreService.PROP_CONSTRAINTS, binding.getConstraints());
        properties.put(StoreService.PROP_FORMAT, format);
        return properties;
    }

    private void setConfig(DataBindings config) {
        this.config = config;
    }
}
