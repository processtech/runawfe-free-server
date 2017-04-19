package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.format.VariableFormatVisitor;
import ru.runa.wfe.var.legacy.ComplexVariable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Load variable value depends of variable type.
 */
public class LoadVariableOfType implements VariableFormatVisitor<Object, LoadVariableOfTypeContext> {
    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(LoadVariableOfType.class);

    @Override
    public Object onDate(DateFormat dateFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(dateFormat, context);
    }

    @Override
    public Object onTime(TimeFormat timeFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(timeFormat, context);
    }

    @Override
    public Object onDateTime(DateTimeFormat dateTimeFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(dateTimeFormat, context);
    }

    @Override
    public Object onExecutor(ExecutorFormat executorFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(executorFormat, context);
    }

    @Override
    public Object onBoolean(BooleanFormat booleanFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(booleanFormat, context);
    }

    @Override
    public Object onBigDecimal(BigDecimalFormat bigDecimalFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(bigDecimalFormat, context);
    }

    @Override
    public Object onDouble(DoubleFormat doubleFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(doubleFormat, context);
    }

    @Override
    public Object onLong(LongFormat longFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(longFormat, context);
    }

    @Override
    public Object onFile(FileFormat fileFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(fileFormat, context);
    }

    @Override
    public Object onHidden(HiddenFormat hiddenFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(hiddenFormat, context);
    }

    @Override
    public Object onList(ListFormat listFormat, LoadVariableOfTypeContext context) {
        List<Object> list = Lists.newArrayList();
        String sizeVariableName = context.variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
        Number size = (Number) sizeDefinition.getFormatNotNull().processBy(this, context.сreateFor(sizeDefinition));
        if (size == null && SystemProperties.isV4ListVariableCompatibilityMode()) {
            Variable<?> variable = context.variableLoader.get(context.process, context.variableDefinition.getName());
            if (variable != null) {
                return processComplexVariables(context.processDefinition, context.variableDefinition, null, variable.getValue());
            }
            return null;
        }
        String[] formatComponentClassNames = context.variableDefinition.getFormatComponentClassNames();
        String componentFormat = formatComponentClassNames.length > 0 ? formatComponentClassNames[0] : null;
        UserType[] formatComponentUserTypes = context.variableDefinition.getFormatComponentUserTypes();
        UserType componentUserType = formatComponentUserTypes.length > 0 ? formatComponentUserTypes[0] : null;
        for (int i = 0; i < size.intValue(); i++) {
            String componentName = context.variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            VariableDefinition componentDefinition = new VariableDefinition(componentName, null, componentFormat, componentUserType);
            Object componentValue = componentDefinition.getFormatNotNull().processBy(this, context.сreateFor(componentDefinition));
            list.add(componentValue);
        }
        return list;
    }

    @Override
    public Object onMap(MapFormat mapFormat, LoadVariableOfTypeContext context) {
        VariableDefinition variableDefinition = context.variableDefinition;
        Variable<?> variable = context.variableLoader.get(context.process, variableDefinition.getName());
        if (variable == null) {
            return variableDefinition.getDefaultValue();
        }
        Object value = variable.getValue();
        value = processComplexVariables(context.processDefinition, variableDefinition, variableDefinition.getUserType(), value);
        return value;
    }

    @Override
    public Object onProcessId(ProcessIdFormat processIdFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(processIdFormat, context);
    }

    @Override
    public Object onString(StringFormat stringFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(stringFormat, context);
    }

    @Override
    public Object onTextString(TextFormat textFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(textFormat, context);
    }

    @Override
    public Object onFormattedTextString(FormattedTextFormat textFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(textFormat, context);
    }

    @Override
    public Object onUserType(UserTypeFormat userTypeFormat, LoadVariableOfTypeContext context) {
        VariableDefinition variableDefinition = context.variableDefinition;
        UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String fullName = variableDefinition.getName() + UserType.DELIM + attributeDefinition.getName();
            VariableDefinition definition = new VariableDefinition(fullName, null, attributeDefinition);
            Object value = definition.getFormatNotNull().processBy(this, context.сreateFor(definition));
            userTypeMap.put(attributeDefinition.getName(), value);
        }
        if (userTypeMap.isEmpty()) {
            Variable<?> variable = context.variableLoader.get(context.process, variableDefinition.getName());
            if (variable != null) {
                // Back compatibility for variables stored as blob.
                if (variable.getValue() == null) {
                    return variableDefinition.getDefaultValue();
                }
                if (!(variable.getValue() instanceof ComplexVariable)) {
                    log.error("User type variable " + variableDefinition.getName() + " has unexpected value of type "
                            + variable.getValue().getClass() + " in process " + context.process.getId());
                    return variableDefinition.getDefaultValue();
                }
                UserTypeMap map = new UserTypeMap(userTypeFormat.getUserType());
                // limitation: embedded complex variables
                map.putAll((ComplexVariable) variable.getValue());
                return map;
            }
        }
        return userTypeMap;
    }

    @Override
    public Object onOther(VariableFormat variableFormat, LoadVariableOfTypeContext context) {
        return loadSimpleVariable(variableFormat, context);
    }

    /**
     * Loading variable of simple type (one variable value -> one variable record).
     * 
     * @param format
     *            Loaded variable format.
     * @param context
     *            Loading context.
     * @return Returns loaded variable value.
     */
    private Object loadSimpleVariable(VariableFormat format, LoadVariableOfTypeContext context) {
        VariableDefinition variableDefinition = context.variableDefinition;
        Variable<?> variable = context.variableLoader.get(context.process, variableDefinition.getName());
        if (variable == null) {
            return variableDefinition.getDefaultValue();
        }
        Object value = variable.getValue();
        return value;
    }

    /**
     * ComplexVariable -> UserTypeMap conversion was made before v4.3.0
     */
    private Object processComplexVariables(ProcessDefinition processDefinition, VariableDefinition variableDefinition, UserType userType, Object value) {
        if (value instanceof ComplexVariable) {
            UserTypeMap map = new UserTypeMap(userType);
            // limitation: embedded complex variables
            map.putAll((ComplexVariable) value);
            return map;
        }
        if (value instanceof UserTypeMap) {
            return value;
        }
        if (value instanceof Map) {
            Preconditions.checkNotNull(variableDefinition, "embedded containers does not supported now");
            Map<Object, Object> map = (Map<Object, Object>) value;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (variableDefinition.getFormatComponentUserTypes()[0] != null) {
                    map.put(entry.getKey(),
                            processComplexVariables(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[0], entry.getValue()));
                }
                if (variableDefinition.getFormatComponentUserTypes()[1] != null) {
                    map.put(entry.getKey(),
                            processComplexVariables(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[1], entry.getValue()));
                }
            }
        }
        if (value instanceof List) {
            Preconditions.checkNotNull(variableDefinition, "embedded containers does not supported now");
            List<Object> list = (List<Object>) value;
            for (int i = 0; i < list.size(); i++) {
                if (variableDefinition.getFormatComponentUserTypes()[0] != null) {
                    list.set(i, processComplexVariables(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[0], list.get(i)));
                }
            }
        }
        return value;
    }
}
