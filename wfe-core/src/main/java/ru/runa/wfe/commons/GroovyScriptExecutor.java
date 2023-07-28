package ru.runa.wfe.commons;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyExceptionInterface;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil.ExtensionObjectInputStream;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.validation.ValidatorException;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;

public class GroovyScriptExecutor {
    protected static final Log log = LogFactory.getLog(GroovyScriptExecutor.class);

    public Map<String, Object> executeScript(VariableProvider variableProvider, String script, GroovyNodeInfoLogExecutor nodeInfoLogExecutor) {
        try {
            GroovyScriptBinding binding = createBinding(variableProvider);
            binding.setVariable(GroovyScriptBinding.VARIABLE_PROVIDER_VARIABLE_NAME, variableProvider);
            binding.setVariable(GroovyScriptBinding.NODE_LOG_VARIABLE_NAME, nodeInfoLogExecutor);
            GroovyShell shell = new GroovyShell(ClassLoaderUtil.getExtensionClassLoader(), binding);
            shell.evaluate(script);
            return binding.getAdjustedVariables();
        } catch (Exception e) {
            log.error("Groovy execution failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    public Object evaluateScript(VariableProvider variableProvider, String script) {
        try {
            GroovyScriptBinding binding = createBinding(variableProvider);
            binding.setVariable(GroovyScriptBinding.VARIABLE_PROVIDER_VARIABLE_NAME, variableProvider);
            GroovyShell shell = new GroovyShell(ClassLoaderUtil.getExtensionClassLoader(), binding);
            return shell.evaluate(script);
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Groovy evaluation failed, script=" + script, e);
            if (e instanceof GroovyExceptionInterface) {
                throw new InternalApplicationException(e.getMessage());
            }
            throw Throwables.propagate(e);
        }
    }

    protected GroovyScriptBinding createBinding(VariableProvider variableProvider) {
        return new GroovyScriptBinding(variableProvider);
    }

    public static class GroovyScriptBinding extends Binding {
        private final static String EXECUTION_CONTEXT_VARIABLE_NAME = "executionContext";
        private final static String VARIABLE_PROVIDER_VARIABLE_NAME = "variableProvider";
        private final static String NODE_LOG_VARIABLE_NAME = "nodeLog";
        private final VariableProvider variableProvider;
        private final Map<String, String> variableScriptingNameToNameMap = Maps.newHashMap();
        private final Map<String, Object> startValues = new HashMap<>();

        public GroovyScriptBinding(VariableProvider variableProvider) {
            this.variableProvider = variableProvider;
            if (variableProvider.getParsedProcessDefinition() != null) {
                for (VariableDefinition variableDefinition : variableProvider.getParsedProcessDefinition().getVariables()) {
                    variableScriptingNameToNameMap.put(variableDefinition.getScriptingName(), variableDefinition.getName());
                }
                for (SwimlaneDefinition swimlaneDefinition : variableProvider.getParsedProcessDefinition().getSwimlanes()) {
                    variableScriptingNameToNameMap.put(swimlaneDefinition.getScriptingName(), swimlaneDefinition.getName());
                }
            }
        }

        private String getVariableNameByScriptingName(String name) {
            String variableName = variableScriptingNameToNameMap.get(name);
            if (variableName == null) {
                if (!WfProcess.SELECTED_TRANSITION_KEY.equals(name)) {
                    log.debug("No variable name found by scripting name '" + name + "'");
                }
                return name;
            }
            return variableName;
        }

        @Override
        public Object getVariable(String scriptingName) {
            if (super.hasVariable(scriptingName)) {
                return super.getVariable(scriptingName);
            }
            if (EXECUTION_CONTEXT_VARIABLE_NAME.equals(scriptingName)) {
                throw new InternalApplicationException(EXECUTION_CONTEXT_VARIABLE_NAME + " has been removed since 4.3.x");
            }
            Object value = getVariableFromProcess(scriptingName);
            log.debug("Passing to script '" + scriptingName + "' as '" + value + "'" + (value != null ? " of " + value.getClass() : ""));
            if (value instanceof Serializable) {
                startValues.put(scriptingName, value);
                setVariable(scriptingName, clone((Serializable) value));
            } else {
                setVariable(scriptingName, value);
            }
            return super.getVariable(scriptingName);
        }

        protected Object getVariableFromProcess(String scriptingName) {
            String name = getVariableNameByScriptingName(scriptingName);
            Object value = variableProvider.getValue(name);
            return value;
        }

        @Override
        public boolean hasVariable(String name) {
            throw new UnsupportedOperationException("Implement if will be used");
        }

        public Map<String, Object> getAdjustedVariables() {
            Map<String, Object> scriptingVariables = getVariables();
            Map<String, Object> result = Maps.newHashMapWithExpectedSize(scriptingVariables.size());
            for (Map.Entry<String, Object> entry : scriptingVariables.entrySet()) {
                String scriptingName = entry.getKey();
                if (Objects.equal(scriptingName, VARIABLE_PROVIDER_VARIABLE_NAME)) {
                    continue;
                }
                if (Objects.equal(entry.getKey(), NODE_LOG_VARIABLE_NAME)) {
                    continue;
                }
                if (startValues.containsKey(scriptingName)) {
                    // this block prevents concurrent modification between tokens in the same process
                    // block can be removed after implementation of single thread execution per process (rm676)
                    // this solution is based also on cached values comparison in ExecutionContext.setSimpleVariableValue(..)
                    Object startValue = startValues.get(scriptingName);
                    if (!(entry.getValue() instanceof Serializable)) {
                        continue;
                    }
                    if (Objects.equal(startValue, entry.getValue())) {
                        continue;
                    }
                    Object oldValue = getVariableFromProcess(scriptingName);
                    if (!Objects.equal(oldValue, startValue)) {
                        throw new InternalApplicationException("Variable concurrent modification prevented for variable " + scriptingName);
                    }
                }
                String variableName = getVariableNameByScriptingName(scriptingName);
                result.put(variableName, entry.getValue());
            }
            return result;
        }

        private Object clone(Serializable object) {
            try {
                ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream);
                objectStream.writeObject(object);
                objectStream.flush();
                try (ExtensionObjectInputStream objectInputStream = new ExtensionObjectInputStream(memoryStream.toByteArray())) {
                    return objectInputStream.readObject();
                }
            } catch (Exception e) {
                throw new InternalApplicationException("couldn't clone '" + object + "'", e);
            }

        }
    }

}
