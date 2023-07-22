package ru.runa.alfresco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AlfObjectAccessor<T> {
    protected Log log = LogFactory.getLog(getClass());
    protected final AlfObject alfObject;
    protected final AlfTypeDesc typeDesc;

    public AlfObjectAccessor(AlfTypeDesc typeDesc, AlfObject alfObject) {
        this.typeDesc = typeDesc;
        this.alfObject = alfObject;
    }

    public abstract T getProperties(boolean all, boolean includeName);

    public abstract void setProperties(T properties);

}
