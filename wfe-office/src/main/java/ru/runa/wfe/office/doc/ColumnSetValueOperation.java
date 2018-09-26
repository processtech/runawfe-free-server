package ru.runa.wfe.office.doc;

import ru.runa.wfe.var.VariableProvider;

/**
 * @author Alekseev Vitaly
 * @since March 31, 2017
 */
public class ColumnSetValueOperation extends ColumnExpansionOperation {

    @Override
    public String getStringValue(DocxConfig config, VariableProvider variableProvider, Object key) {
        final Object value = getContainerValue();
        if (value == null) {
            return "";
        }
        final String string = String.valueOf(value);
        if (string.startsWith(DocxUtils.PLACEHOLDER_START) && string.endsWith(DocxUtils.PLACEHOLDER_END)) {
            String selector = string.substring(DocxUtils.PLACEHOLDER_START.length(), string.length() - DocxUtils.PLACEHOLDER_END.length());
            Object object = DocxUtils.getValue(config, variableProvider, null, selector);
            return object != null ? object.toString() : "";
        }
        return string;
    }
}
