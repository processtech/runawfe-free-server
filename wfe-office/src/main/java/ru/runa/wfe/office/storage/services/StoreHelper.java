package ru.runa.wfe.office.storage.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.CompositeQueryProperties;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryProperties;
import ru.runa.wfe.office.storage.binding.QueryRole;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
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

    public ExecutionResult findByComposite(DataBindings dataBindings) throws Exception {
        DataBinding triggerBinding = getTriggerBinding(dataBindings);
        List<DataBinding> subqueryBindings = getSubqueryBindings(dataBindings.getBindings());

        QueryProperties trigger = new QueryProperties(
                extractProperties(triggerBinding),
                getUserType(triggerBinding),
                triggerBinding.getCondition(),
                triggerBinding.getQueryRole()
        );

        List<QueryProperties> subqueries = new ArrayList<>();

        for (DataBinding binding : subqueryBindings) {
            UserType userType = getUserType(binding);

            subqueries.add(new QueryProperties(
                    extractProperties(binding, new UserTypeFormat(userType)),
                    userType,
                    binding.getCondition(),
                    binding.getQueryRole()
            ));
        }

        CompositeQueryProperties queryProperties = new CompositeQueryProperties(trigger, subqueries);

        return storeService.findByComposite(queryProperties);
    }

    private DataBinding getTriggerBinding(DataBindings bindings) {
        DataBinding triggerBinding = bindings.getBindings().get(0);
        QueryRole queryRole = triggerBinding.getQueryRole();
        if (queryRole != null && queryRole != QueryRole.TRIGGER) {
            throw new IllegalArgumentException("Invalid trigger role " + queryRole);
        }
        return triggerBinding;
    }

    private List<DataBinding> getSubqueryBindings(List<DataBinding> bindings) {
        if (bindings.size() <= 1) {
            return Collections.emptyList();
        }
        List<DataBinding> subqueryBindings = bindings.subList(1, bindings.size());
        for (DataBinding binding : subqueryBindings) {
            if (binding.getQueryRole() == QueryRole.TRIGGER) {
                throw new IllegalArgumentException("Invalid subquery role " + binding.getQueryRole());
            }
        }
        return subqueryBindings;
    }

    private Properties extractProperties(DataBinding binding) {
        return extractProperties(binding, format);
    }

    private Properties extractProperties(DataBinding binding, VariableFormat format) {
        Properties properties = new Properties();
        properties.setProperty(StoreService.PROP_PATH, config.getInputFilePath());
        properties.put(StoreService.PROP_CONSTRAINTS, binding.getConstraints());
        properties.put(StoreService.PROP_FORMAT, format);
        return properties;
    }

    private UserType getUserType(DataBinding binding) {
        return variableProvider.getUserType(((OnSheetConstraints) binding.getConstraints()).getSheetName());
    }

    private void setConfig(DataBindings config) {
        this.config = config;
    }
}
