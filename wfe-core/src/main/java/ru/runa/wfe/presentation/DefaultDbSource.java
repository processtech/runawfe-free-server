package ru.runa.wfe.presentation;


/**
 * Default implementation of {@link DbSource} interface. Reference directly to field value. E.q. id property will be referenced as 'alias.id'. This
 * {@link DbSource} not contains join restrictions, so can be used only to access properties of root persistence object.
 */
public class DefaultDbSource extends DbSource {

     /**
     * HQL path to access property value. For example for id property this path is 'id'; for id property of field child is 'child.id'.
     */
    protected final String valueDBPath;

    /**
     * Creates default implementation of {@link DbSource}. This implementation reference directly to field value.
     * 
     * @param sourceObject
     *            Persistent object of field. Property will be accessed throw this object instance.
     * @param valueDBPath
     *            HQL path to access property value.
     */
    public DefaultDbSource(Class<?> sourceObject, String valueDBPath) {
        super(sourceObject);
        this.valueDBPath = valueDBPath;
    }

    @Override
    public String getValueDBPath(AccessType accessType, String alias) {
        return alias == null ? valueDBPath : alias + "." + valueDBPath;
    }

    @Override
    public String getJoinExpression(String alias) {
        return "";
    }
}
