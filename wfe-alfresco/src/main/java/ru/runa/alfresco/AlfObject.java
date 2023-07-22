package ru.runa.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.LogFactory;

import ru.runa.ClassUtils;
import ru.runa.EqualsUtil;
import ru.runa.alfresco.anno.Property;
import ru.runa.alfresco.anno.Type;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base class for all mappable objects.
 * 
 * @author dofs
 */
@Type(name = "content", prefix = "cm")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AlfObject implements IAlfObject, Serializable {
    private final static long serialVersionUID = 197L;

    protected transient AlfConnection conn;
    private String uuidRef;
    private transient final Map<String, Object> initialFieldValues = Maps.newHashMap();
    private Map<String, String> referenceFieldUuids = Maps.newHashMap();

    @Property(name = "name")
    private String objectName;
    @Property(name = "created", readOnly = true, className = "java.util.Date")
    private Calendar created;
    @Property(name = "modified", readOnly = true)
    private Calendar lastUpdated;

    public void setLazyLoader(AlfConnection lazyLoader) {
        conn = lazyLoader;
    }

    protected void markPropertiesInitialState(AlfTypeDesc mapping) {
        for (AlfPropertyDesc desc : mapping.getAllDescs()) {
            if (desc.getProperty() == null || desc.getProperty().readOnly()) {
                continue;
            }
            String fieldName = desc.getFieldName();
            if (desc.isNodeReference()) {
                initialFieldValues.put(fieldName, getReferencePropertyUuid(fieldName, true));
            } else {
                Object object = ClassUtils.getFieldValue(this, desc);
                if (object == null) {
                    initialFieldValues.put(fieldName, null);
                } else if (object instanceof String) {
                    initialFieldValues.put(fieldName, object);
                } else if (Number.class.isAssignableFrom(object.getClass())) {
                    initialFieldValues.put(fieldName, object);
                } else if (object instanceof Boolean) {
                    initialFieldValues.put(fieldName, object);
                } else if (object instanceof Calendar) {
                    initialFieldValues.put(fieldName, CalendarUtil.clone((Calendar) object));
                } else if (object instanceof Date) {
                    initialFieldValues.put(fieldName, ((Date) object).clone());
                } else if (object.getClass().isArray()) {
                    // arrays are immutable
                } else if (List.class.isAssignableFrom(object.getClass())) {
                    initialFieldValues.put(fieldName, ((List) object).toArray());
                } else {
                    throw new InternalApplicationException("Unable to mark field " + fieldName + ". Not supported type " + object.getClass());
                }
            }
        }
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void updateObjectName() {
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass(), conn);
        setObjectName(getNewObjectName(typeDesc));
    }

    protected String getNewObjectName(AlfTypeDesc typeDesc) {
        return null;
    }

    public Set<String> getDirtyFieldNames(AlfTypeDesc mapping) {
        Set<String> dirtyFieldNames = Sets.newHashSet();
        for (AlfPropertyDesc desc : mapping.getAllDescs()) {
            if (desc.getProperty() == null || desc.getProperty().readOnly()) {
                continue;
            }
            Object initialObject = initialFieldValues.get(desc.getFieldName());
            Object currentObject;
            if (desc.isNodeReference()) {
                currentObject = getReferencePropertyUuid(desc.getFieldName(), true);
            } else {
                currentObject = ClassUtils.getFieldValue(this, desc);
                if (currentObject instanceof List) {
                    currentObject = ((List) currentObject).toArray();
                }
            }
            if (!EqualsUtil.equals(initialObject, currentObject)) {
                dirtyFieldNames.add(desc.getFieldName());
            }
        }
        return dirtyFieldNames;
    }

    public List<String> getDirtyFieldChanges(AlfTypeDesc mapping) {
        List<String> dirtyFieldNames = Lists.newArrayList();
        for (AlfPropertyDesc desc : mapping.getAllDescs()) {
            if (desc.getProperty() == null || desc.getProperty().readOnly()) {
                continue;
            }
            Object initialObject = initialFieldValues.get(desc.getFieldName());
            Object currentObject;
            if (desc.isNodeReference()) {
                currentObject = getReferencePropertyUuid(desc.getFieldName(), true);
            } else {
                currentObject = ClassUtils.getFieldValue(this, desc);
                if (currentObject instanceof List) {
                    currentObject = ((List) currentObject).toArray();
                }
            }
            if (!EqualsUtil.equals(initialObject, currentObject)) {
                dirtyFieldNames.add(desc.getFieldName() + " (" + initialObject + " -> " + currentObject + ")");
            }
        }
        return dirtyFieldNames;
    }

    public String getReferencePropertiesDiff(AlfObject another) {
        Set<String> refFieldNames = new HashSet<String>();
        refFieldNames.addAll(referenceFieldUuids.keySet());
        refFieldNames.addAll(another.referenceFieldUuids.keySet());
        for (String refFieldName : refFieldNames) {
            String thisReferenceUuid = this.getReferencePropertyUuid(refFieldName, true);
            String anotherReferenceUuid = another.getReferencePropertyUuid(refFieldName, true);
            if (!EqualsUtil.equals(thisReferenceUuid, anotherReferenceUuid)) {
                try {
                    Object thisPropertyValue = PropertyUtils.getProperty(this, refFieldName);
                    Object anotherPropertyValue = PropertyUtils.getProperty(another, refFieldName);
                    if (!EqualsUtil.equals(thisPropertyValue, anotherPropertyValue)) {
                        return EqualsUtil.getDiff(refFieldName, thisPropertyValue, anotherPropertyValue);
                    }
                } catch (Exception e) {
                    LogFactory.getLog(ClassUtils.getImplClass(getClass())).error(refFieldName, e);
                    return EqualsUtil.getDiff(refFieldName, thisReferenceUuid, anotherReferenceUuid);
                }
            }
        }
        return null;
    }

    public String getReferencePropertyUuid(String fieldName, boolean acquireProperty) {
        try {
            String referenceUuid = referenceFieldUuids.get(fieldName);
            if (referenceUuid != null) {
                return referenceUuid;
            }
            if (acquireProperty) {
                AlfPropertyDesc desc = Mappings.getMapping(getClass(), conn).getPropertyDescByFieldName(fieldName);
                AlfObject reference = (AlfObject) ClassUtils.getFieldValue(this, desc);
                if (reference != null) {
                    return reference.getUuidRef() != null ? reference.getUuidRef() : null;
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

    protected void setReferencePropertyUuid(String propertyName, String uuidRef) {
        referenceFieldUuids.put(propertyName, uuidRef);
    }

    public String getUuidRef() {
        return uuidRef;
    }

    public void setUuidRef(String uuidRef) {
        this.uuidRef = uuidRef;
    }

    protected void markCollectionsInitialState() {
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass(), conn);
        for (AlfPropertyDesc desc : typeDesc.getAllDescs()) {
            if (desc.getAssoc() != null) {
                Collection<AlfObject> collection = (Collection<AlfObject>) ClassUtils.getFieldValue(this, desc);
                markCollectionInitialState(desc, collection);
            }
        }
    }

    private void markCollectionInitialState(AlfPropertyDesc desc, Collection<AlfObject> collection) {
        List<String> assocIds = Lists.newArrayList();
        for (AlfObject collObject : collection) {
            assocIds.add(collObject.getUuidRef());
        }
        initialFieldValues.put(desc.getFieldName(), assocIds);
    }

    protected void loadCollection(AlfPropertyDesc desc, Collection<AlfObject> collection) {
        if (!initialFieldValues.containsKey(desc.getFieldName())) {
            conn.loadAssociation(getUuidRef(), collection, desc);
            markCollectionInitialState(desc, collection);
        }
    }

    public Map<AlfPropertyDesc, List<String>> getAssocToCreate() {
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass(), conn);
        Map<AlfPropertyDesc, List<String>> result = Maps.newHashMap();
        for (AlfPropertyDesc desc : typeDesc.getAllDescs()) {
            if (desc.getAssoc() == null) {
                continue;
            }
            Collection<AlfObject> collection = (Collection<AlfObject>) ClassUtils.getFieldValue(this, desc);
            if (!initialFieldValues.containsKey(desc.getFieldName()) && collection != null && !collection.isEmpty()) {
                loadCollection(desc, new ArrayList<AlfObject>());
            }
            List<String> initialUuids = (List<String>) initialFieldValues.get(desc.getFieldName());
            if (initialUuids == null) {
                continue;
            }
            List<String> assocResults = Lists.newArrayList();
            for (AlfObject alfObject : collection) {
                Preconditions.checkState(alfObject.getUuidRef() != null, "Save object before adding to association.");
                if (!initialUuids.contains(alfObject.getUuidRef())) {
                    assocResults.add(alfObject.getUuidRef());
                }
            }
            if (assocResults.size() > 0) {
                result.put(desc, assocResults);
            }
        }
        return result;
    }

    public Map<AlfPropertyDesc, List<String>> getAssocToDelete() {
        AlfTypeDesc typeDesc = Mappings.getMapping(getClass(), conn);
        Map<AlfPropertyDesc, List<String>> result = Maps.newHashMap();
        for (AlfPropertyDesc desc : typeDesc.getAllDescs()) {
            if (desc.getAssoc() == null) {
                continue;
            }
            Collection<AlfObject> collection = (Collection<AlfObject>) ClassUtils.getFieldValue(this, desc);
            if (!initialFieldValues.containsKey(desc.getFieldName()) && collection != null && !collection.isEmpty()) {
                loadCollection(desc, new ArrayList<AlfObject>());
            }
            List<String> initialUuids = (List<String>) initialFieldValues.get(desc.getFieldName());
            if (initialUuids == null) {
                continue;
            }
            List<String> assocResults = Lists.newArrayList(initialUuids);
            for (AlfObject alfObject : collection) {
                Preconditions.checkState(alfObject.getUuidRef() != null, "Save object before deleting from association.");
                assocResults.remove(alfObject.getUuidRef());
            }
            if (assocResults.size() > 0) {
                result.put(desc, assocResults);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        if (uuidRef != null) {
            return uuidRef.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getObjectName();
    }
}
