package ru.runa.wfe.office.doc;

import ru.runa.wfe.var.IVariableProvider;

/**
 * @author Alekseev Vitaly
 * @since March 31, 2017
 */
public class ColumnSetValueOperation extends ColumnExpansionOperation {

    /*
     * (non-Javadoc)
     * 
     * @see
     * ru.runa.wfe.office.doc.ColumnExpansionOperation#getStringValue(ru.runa.wfe.office.doc.DocxConfig,runa.wfe.var.IVariableProvider,java.lang.Object
     * )
     */
    @Override
    public String getStringValue(DocxConfig config, IVariableProvider variableProvider, Object key) {
        final Object value = getContainerValue();
        return value == null ? "" : String.valueOf(value);
    }
}
