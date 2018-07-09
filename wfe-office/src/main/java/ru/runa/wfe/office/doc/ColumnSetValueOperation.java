package ru.runa.wfe.office.doc;

import ru.runa.wfe.var.VariableProvider;

/**
 * @author Alekseev Vitaly
 * @since March 31, 2017
 */
public class ColumnSetValueOperation extends ColumnExpansionOperation {

    /*
     * (non-Javadoc)
     * 
     * @see
     * ru.runa.wfe.office.doc.ColumnExpansionOperation#getStringValue(ru.runa.wfe.office.doc.DocxConfig,runa.wfe.var.VariableProvider,java.lang.Object
     * )
     */
    @Override
    public String getStringValue(DocxConfig config, VariableProvider variableProvider, Object key) {
        final Object value = getContainerValue();
        return value == null ? "" : String.valueOf(value);
    }
}
