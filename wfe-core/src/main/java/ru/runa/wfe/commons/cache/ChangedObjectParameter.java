package ru.runa.wfe.commons.cache;

import com.google.common.base.Objects;
import org.hibernate.type.Type;
import ru.runa.wfe.InternalApplicationException;

/**
 * Changed object state. 
 */
public class ChangedObjectParameter {
    /**
     * Changed object.
     */
    public final Object object;
    
    /**
     * Change operation type.
     */
    public final Change changeType;
    
    /**
     * Current state of object properties.
     */
    public final Object[] currentState;
    
    /**
     * Previous (from database) state of object properties.
     */
    public final Object[] previousState;
    
    /**
     * Object property names.
     */
    private final String[] propertyNames;
    
    public ChangedObjectParameter(Object object, Change change, Object[] currentState, Object[] previousState, String[] propertyNames) {
        this.object = object;
        this.changeType = change;
        this.currentState = currentState;
        this.previousState = previousState;
        this.propertyNames = propertyNames;
    }

    /**
     * Check for property with given name.
     * @param propertyName Searching property name.
     * @return index of property with given name.
     * @throws InternalApplicationException if property not found.
     */
    public int getPropertyIndex(String propertyName) {
        for (int idx = 0; idx < propertyNames.length; ++idx) {
            if (Objects.equal(propertyNames[idx], propertyName)) {
                return idx;
            }
        }
        throw new InternalApplicationException("No '" + propertyName + "' found in " + object);
    }
}