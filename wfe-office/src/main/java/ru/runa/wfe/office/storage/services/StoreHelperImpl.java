package ru.runa.wfe.office.storage.services;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreOperation;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

@CommonsLog
public class StoreHelperImpl implements StoreHelper {

    private Map<QueryType, Method> invocationMap = Maps.newHashMap();

    StoreService storeService;

    DataBindings config;

    VariableFormat format;

    VariableProvider variableProvider;

    Map<String, ParamDef> inputParams;

    public StoreHelperImpl(DataBindings config, VariableProvider variableProvider, StoreService storeService) {
        setConfig(config);
        registerHandlers();
        this.variableProvider = variableProvider;
        this.storeService = storeService;
    }

    private void setConfig(DataBindings config) {
        this.config = config;
    }

    @Override
    public void setVariableFormat(VariableFormat format) {
        this.format = format;
    }

    @Override
    public ExecutionResult execute(DataBinding binding, WfVariable variable) {
        VariableDefinition vd = variable.getDefinition();
        if (!vd.isUserType() && (!vd.getFormatClassName().equals(ListFormat.class.getName()) || vd.getFormatComponentUserTypes() == null
                || vd.getFormatComponentUserTypes().length == 0)) {
            log.error("Variable type" + vd.getFormat() + " not supported.");
            return ExecutionResult.EMPTY;
        }
        try {
            Method method = invocationMap.get(config.getQueryType());
            return (ExecutionResult) method.invoke(this, binding, variable, config.getCondition());
        } catch (Exception e) {
            throw Throwables.propagate(Throwables.getRootCause(e));
        }
    }

    @StoreOperation(QueryType.INSERT)
    public ExecutionResult save(DataBinding binding, WfVariable variable, String condition) throws Exception {
        storeService.save(extractProperties(binding), variable, true);
        return ExecutionResult.EMPTY;
    }

    @StoreOperation(QueryType.SELECT)
    public ExecutionResult findByFilter(DataBinding binding, WfVariable variable, String condition) throws Exception {
        return storeService.findByFilter(extractProperties(binding), variable, condition);
    }

    @StoreOperation(QueryType.UPDATE)
    public ExecutionResult update(DataBinding binding, WfVariable variable, String condition) throws Exception {
        storeService.update(extractProperties(binding), variable, condition);
        return ExecutionResult.EMPTY;
    }

    @StoreOperation(QueryType.DELETE)
    public ExecutionResult delete(DataBinding binding, WfVariable variable, String condition) throws Exception {
        storeService.delete(extractProperties(binding), variable, condition);
        return ExecutionResult.EMPTY;
    }

    private void registerHandlers() {
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            StoreOperation annotation = method.getAnnotation(StoreOperation.class);
            if (annotation != null) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters == null || parameters.length < 1) {
                    log.warn("wrong parameters");
                    continue;
                }
                if (!invocationMap.containsKey(annotation.value())) {
                    invocationMap.put(annotation.value(), method);
                }
            }
        }
    }

    private Properties extractProperties(DataBinding binding) {
        Properties properties = new Properties();
        properties.setProperty(StoreService.PROP_PATH, config.getInputFilePath());
        properties.put(StoreService.PROP_CONSTRAINTS, binding.getConstraints());
        properties.put(StoreService.PROP_FORMAT, format);
        return properties;
    }
}
