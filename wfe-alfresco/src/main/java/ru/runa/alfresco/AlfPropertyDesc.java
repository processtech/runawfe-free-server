package ru.runa.alfresco;

import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.anno.Assoc;
import ru.runa.alfresco.anno.Property;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Descriptor for Alfresco property and association mapping.
 * 
 * @author dofs
 */
public class AlfPropertyDesc {
    protected final String fieldName;
    protected String propertyName;
    protected final String namespace;
    protected String title;
    protected Class<?> propertyClass;
    protected String defaultValue;
    protected final Assoc assoc;
    protected final Property property;
    private final boolean referencedProperty;
    private boolean childAssociation;
    private boolean sourceAssociation;

    private AlfPropertyDesc(String namespace, String javaPropertyName, Assoc assoc, Property property, boolean referencedProperty) {
        this.namespace = namespace;
        fieldName = javaPropertyName;
        this.property = property;
        if (property != null) {
            propertyName = property.name();
            if (!Strings.isNullOrEmpty(property.className())) {
                propertyClass = ClassLoaderUtil.loadClass(property.className());
            }
        }
        this.assoc = assoc;
        if (assoc != null) {
            propertyName = assoc.name();
        }
        this.referencedProperty = referencedProperty;
    }

    public static AlfPropertyDesc newProp(String namespace, String javaPropertyName, Property property, boolean referencedProperty) {
        return new AlfPropertyDesc(namespace, javaPropertyName, null, property, referencedProperty);
    }

    public static AlfPropertyDesc newAssoc(String namespace, String javaPropertyName, Assoc assoc) {
        return new AlfPropertyDesc(namespace, javaPropertyName, assoc, null, false);
    }

    public String getPropertyNameWithNamespace() {
        return getPropertyQName().toString();
    }

    public QName getPropertyQName() {
        if (propertyName.contains(":")) {
            int index = propertyName.indexOf(":");
            String prefix = propertyName.substring(0, index);
            String propName = propertyName.substring(index + 1);
            return QName.createQName(Mappings.getNamespace(prefix), propName);
        }
        return QName.createQName(namespace, propertyName);
    }

    public Assoc getAssoc() {
        return assoc;
    }

    public boolean isChildAssociation() {
        return childAssociation;
    }

    public void setChildAssociation(boolean childAssociation) {
        this.childAssociation = childAssociation;
    }

    /**
     * is defined type belongs to source side of association (or target)?
     */
    public boolean isSourceAssociation() {
        return sourceAssociation;
    }

    public void setSourceAssociation(boolean sourceAssociation) {
        this.sourceAssociation = sourceAssociation;
    }

    public Property getProperty() {
        return property;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isNodeReference() {
        return referencedProperty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Class<?> getPropertyClassNotNull() {
        if (propertyClass == null) {
            throw new InternalApplicationException("Property class is null in " + propertyName);
        }
        return propertyClass;
    }

    public void setPropertyClass(Class<?> propertyClass) {
        if (this.propertyClass == null) {
            this.propertyClass = propertyClass;
        }
    }

    public String getDefaultValue() {
        return defaultValue != null ? defaultValue : "";
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("alf", propertyName).add("java", fieldName).add("type", propertyClass).toString();
    }
}
