package ru.runa.wfe.commons;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

public class BSHScriptExecutor extends GroovyScriptExecutor {

    @Override
    public Map<String, Object> executeScript(IVariableProvider variableProvider, String script) {
        try {
            return super.executeScript(variableProvider, adjustScript(script));
        } catch (RuntimeException e) {
            log.error("BSH adjusted conf: " + script);
            throw e;
        }
    }

    @Override
    public Object evaluateScript(IVariableProvider variableProvider, String script) {
        try {
            return super.evaluateScript(variableProvider, adjustScript(script));
        } catch (RuntimeException e) {
            log.error("BSH adjusted conf: " + script);
            throw e;
        }
    }

    private static String adjustScript(String script) {
        script = script.replaceAll(Pattern.quote("}"), Matcher.quoteReplacement("};"));
        script = script.replaceAll("transition", Matcher.quoteReplacement(WfProcess.SELECTED_TRANSITION_KEY));
        script = script.replaceAll("void", Matcher.quoteReplacement("null"));
        return script;
    }

    @Override
    protected GroovyScriptBinding createBinding(IVariableProvider variableProvider) {
        if (SystemProperties.isV3CompatibilityMode()) {
            return new BackCompatibilityBinding(variableProvider);
        }
        return super.createBinding(variableProvider);
    }

    private static class BackCompatibilityBinding extends GroovyScriptBinding {

        public BackCompatibilityBinding(IVariableProvider variableProvider) {
            super(variableProvider);
        }

        @Override
        protected Object getVariableFromProcess(String scriptingName) {
            Object value = super.getVariableFromProcess(scriptingName);
            if (value instanceof Executor) {
                log.debug("Converting Executor -> String");
                value = TypeConversionUtil.convertTo(String.class, value);
            }
            return value;
        }

    }

}
