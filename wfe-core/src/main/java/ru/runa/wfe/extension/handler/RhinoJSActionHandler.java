package ru.runa.wfe.extension.handler;

import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;

import com.google.common.collect.Lists;

public class RhinoJSActionHandler extends ActionHandlerBase {

    @Override
    public void execute(ExecutionContext executionContext) throws ScriptException {
        try {
            List<VariableDefinition> rawDefinitions = Lists.newArrayList();
            for (VariableDefinition definition : executionContext.getParsedProcessDefinition().getVariables()) {
                if (definition.isUserType()) {
                    rawDefinitions.addAll(definition.expandUserType(false));
                } else {
                    rawDefinitions.add(definition);
                }
            }
            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();
            for (VariableDefinition definition : rawDefinitions) {
                Object value = executionContext.getVariableValue(definition.getName());
                if (value != null) {
                    Object js = javaToJs(context, scope, value);
                    ScriptableObject.putProperty(scope, definition.getScriptingName(), js);
                }
            }
            context.evaluateString(scope, configuration, "<cmd>", 1, null);
            for (VariableDefinition definition : rawDefinitions) {
                Object js = scope.get(definition.getScriptingName(), scope);
                if (js != Scriptable.NOT_FOUND) {
                    Object newValue = Context.jsToJava(js, FormatCommons.create(definition).getJavaClass());
                    if (newValue != null) {
                        executionContext.setVariableValue(definition.getName(), newValue);
                    }
                }
            }
        } finally {
            Context.exit();
        }
    }

    private Object javaToJs(Context context, Scriptable scope, Object value) {
        if (value instanceof Date) {
            return context.newObject(scope, "Date", new Object[] { ((Date) value).getTime() });
        }
        return Context.javaToJS(value, scope);
    }

}
