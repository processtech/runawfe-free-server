package ru.runa.wfe.execution;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
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

/**
 * Operation for converting variable to simple variables, wich may be stored to database without additional transformations.
 */
public class ConvertToSimpleVariables implements VariableFormatVisitor<List<ConvertToSimpleVariablesResult>, ConvertToSimpleVariablesContext> {
    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(ExecutionContext.class);

    @Override
    public List<ConvertToSimpleVariablesResult> onDate(DateFormat dateFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onTime(TimeFormat timeFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onDateTime(DateTimeFormat dateTimeFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> OnExecutor(ExecutorFormat executorFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onBoolean(BooleanFormat booleanFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onBigDecimal(BigDecimalFormat bigDecimalFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onDouble(DoubleFormat doubleFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onLong(LongFormat longFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onFile(FileFormat fileFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onHidden(HiddenFormat hiddenFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onList(ListFormat listFormat, ConvertToSimpleVariablesContext context) {
        List<ConvertToSimpleVariablesResult> results = Lists.newLinkedList();
        int newSize = TypeConversionUtil.getListSize(context.value);
        String sizeVariableName = context.variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        WfVariable oldSizeVariable = context.variableLoader.getVariable(context.processDefinition, context.process, sizeVariableName);
        int maxSize = newSize;
        if (oldSizeVariable != null && oldSizeVariable.getValue() instanceof Integer) {
            maxSize = Math.max((Integer) oldSizeVariable.getValue(), newSize);
        }
        VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
        results.add(new ConvertToSimpleVariablesResult(sizeDefinition, context.value != null ? newSize : null));

        String[] formatComponentClassNames = context.variableDefinition.getFormatComponentClassNames();
        String componentFormat = formatComponentClassNames.length > 0 ? formatComponentClassNames[0] : null;
        UserType[] formatComponentUserTypes = context.variableDefinition.getFormatComponentUserTypes();
        UserType componentUserType = formatComponentUserTypes.length > 0 ? formatComponentUserTypes[0] : null;
        List<?> list = (List<?>) context.value;
        for (int i = 0; i < maxSize; i++) {
            String name = context.variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            VariableDefinition definition = new VariableDefinition(name, null, componentFormat, componentUserType);
            Object object = list != null && list.size() > i ? list.get(i) : null;
            results.addAll(definition.getFormatNotNull().processBy(this, context.createFor(definition, object)));
        }
        if (SystemProperties.isV4ListVariableCompatibilityMode()) {
            // delete old list variables as blobs (pre 4.3.0)
            Variable<?> variable = context.variableLoader.get(context.process, context.variableDefinition.getName());
            if (variable != null) {
                log.debug("Removing old-style list variable '" + context.variableDefinition.getName() + "'");
                context.variableDAO.delete(variable);
            }
        }
        return results;
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onMap(MapFormat mapFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onProcessId(ProcessIdFormat processIdFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onString(StringFormat stringFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onTextString(TextFormat textFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onUserType(UserTypeFormat userTypeFormat, ConvertToSimpleVariablesContext context) {
        UserTypeMap userTypeValue = (UserTypeMap) context.value;
        List<ConvertToSimpleVariablesResult> results = Lists.newLinkedList();
        String namePrefix = context.variableDefinition.getName() + UserType.DELIM;
        String scriptingNamePrefix = context.variableDefinition.getScriptingName() + UserType.DELIM;
        for (VariableDefinition attribute : userTypeFormat.getUserType().getAttributes()) {
            Object attributeValue = userTypeValue == null ? null : userTypeValue.get(attribute.getName());
            String name = namePrefix + attribute.getName();
            String scriptingName = scriptingNamePrefix + attribute.getScriptingName();
            VariableDefinition attributeVariable = new VariableDefinition(name, scriptingName, attribute);
            ConvertToSimpleVariablesContext convertAttributeContext = context.createFor(attributeVariable, attributeValue);
            results.addAll(attributeVariable.getFormatNotNull().processBy(this, convertAttributeContext));
        }
        return results;
    }

    @Override
    public List<ConvertToSimpleVariablesResult> onOther(VariableFormat variableFormat, ConvertToSimpleVariablesContext context) {
        return Lists.newArrayList(new ConvertToSimpleVariablesResult(context));
    }
}
