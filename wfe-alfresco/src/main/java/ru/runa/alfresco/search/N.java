package ru.runa.alfresco.search;

import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.Mappings;

/**
 * Operand for search condition.
 * @author dofs
 */
public class N {
    private String value;
    
    private N(String value) {
        this.value = value;
    }
    
    /**
     * Applicable for almost all {@link Op}
     * @param name field qualified name
     * @return instance of operand
     */
    public static N field(QName name) {
        String prefix = Mappings.getNamespacePrefix(name.getNamespaceURI());
        String v = "@" + prefix + "\\:" + name.getLocalName();
        return new N(v);
    }
    
    /**
     * Applicable for {@link Op#IS_NULL} and {@link Op#IS_NOT_NULL}
     * @param name field qualified name
     * @return instance of operand
     */
    public static N fieldNullCheck(QName name) {
        String prefix = Mappings.getNamespacePrefix(name.getNamespaceURI());
        String v = prefix + ":" + name.getLocalName();
        return new N(v);
    }
    
    /**
     * Applicable for {@link Op#TYPE_OF}
     * @param name type qualified name
     * @return instance of operand
     */
    public static N type(QName name) {
        return new N(name.toString());
    }
    
    /**
     * Applicable for {@link Op#PRIMARYPARENT}
     * @param name node uuid
     * @return instance of operand
     */
    public static N uuid(String uuid) {
        return new N(uuid);
    }

    @Override
    public String toString() {
        return value;
    }
}
