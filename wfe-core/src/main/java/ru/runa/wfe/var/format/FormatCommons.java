package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Strings;

public class FormatCommons {

    private static VariableFormat create(String className, UserType userType) {
        if (userType != null) {
            return new UserTypeFormat(userType);
        }
        if (Strings.isNullOrEmpty(className)) {
            className = StringFormat.class.getName();
        }
        VariableFormat format = ClassLoaderUtil.instantiate(className);
        if (format instanceof VariableFormatContainer) {
            // see
            // ru.runa.wfe.var.VariableDefinition.initComponentUserTypes(IUserTypeLoader)
        }
        return format;
    }

    public static VariableFormat create(VariableDefinition variableDefinition) {
        VariableFormat format = create(variableDefinition.getFormatClassName(), variableDefinition.getUserType());
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setComponentClassNames(variableDefinition.getFormatComponentClassNames());
            ((VariableFormatContainer) format).setComponentUserTypes(variableDefinition.getFormatComponentUserTypes());
        }
        return format;
    }

    public static VariableFormat createComponent(VariableFormatContainer formatContainer, int index) {
        String elementFormatClassName = formatContainer.getComponentClassName(index);
        return create(elementFormatClassName, formatContainer.getComponentUserType(index));
    }

    public static VariableFormat createComponent(VariableDefinition variableDefinition, int index) {
        String elementFormatClassName = ((VariableFormatContainer) variableDefinition.getFormatNotNull()).getComponentClassName(index);
        return create(elementFormatClassName, variableDefinition.getFormatComponentUserTypes()[index]);
    }

    public static VariableFormat createComponent(WfVariable containerVariable, int index) {
        return createComponent(containerVariable.getDefinition(), index);
    }

    public static String formatComponentValue(WfVariable containerVariable, int index, Object value) {
        return createComponent(containerVariable, index).format(value);
    }
}
