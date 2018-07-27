package ru.runa.wfe.office.storage.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.office.storage.BlockedFileException;
import ru.runa.wfe.office.storage.JdbcStoreException;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreOperation;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.binding.QueryType;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class StoreHelperImpl implements StoreHelper {

    private static final Log log = LogFactory.getLog(StoreHelperImpl.class);

    private Map<QueryType, Method> invocationMap = Maps.newHashMap();

    StoreService storeService;

    DataBindings config;

    VariableFormat format;

    IVariableProvider variableProvider;

    Map<String, ParamDef> inputParams;

    public StoreHelperImpl(DataBindings config, IVariableProvider variableProvider, StoreService storeService) {
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
            if (e instanceof InvocationTargetException) {
                Throwable targetEx = ((InvocationTargetException) e).getTargetException();
                if (targetEx instanceof BlockedFileException) {
                    throw (BlockedFileException) targetEx;
                } else if (targetEx instanceof JdbcStoreException) {
                    throw (JdbcStoreException) targetEx;
                }
            } else if (e instanceof BlockedFileException) {
                throw (BlockedFileException) e;
            } else if (e instanceof JdbcStoreException) {
                throw (JdbcStoreException) e;
            }
            log.error("", e);
            return ExecutionResult.EMPTY;
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
