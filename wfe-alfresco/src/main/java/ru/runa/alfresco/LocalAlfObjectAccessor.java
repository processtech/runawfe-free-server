package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Converts java properties from/to alfresco {@link Serializable}.
 * 
 * @author dofs
 */
@SuppressWarnings({ "unchecked" })
public class LocalAlfObjectAccessor extends AlfObjectAccessor<Map<QName, Serializable>> {

    public LocalAlfObjectAccessor(AlfTypeDesc typeDesc, AlfObject alfObject) {
        super(typeDesc, alfObject);
    }

    @Override
    public Map<QName, Serializable> getProperties(boolean all, boolean includeName) {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        Collection<AlfPropertyDesc> propDescs;
        if (all) {
            propDescs = typeDesc.getAllDescs();
        } else {
            propDescs = new ArrayList<AlfPropertyDesc>();
            for (String fieldName : alfObject.getDirtyFieldNames(typeDesc)) {
                propDescs.add(typeDesc.getPropertyDescByFieldName(fieldName));
            }
        }
        for (AlfPropertyDesc desc : propDescs) {
            if (desc.getProperty() != null && !desc.getProperty().readOnly()) {
                props.put(desc.getPropertyQName(), getProperty(desc));
            }
        }
        if (includeName) {
            props.put(ContentModel.PROP_NAME, alfObject.getObjectName());
        }
        return props;
    }

    private Serializable getProperty(AlfPropertyDesc propertyDesc) {
        try {
            String fieldName = propertyDesc.getFieldName();
            if (propertyDesc.isNodeReference()) {
                String uuidRef = alfObject.getReferencePropertyUuid(fieldName, true);
                return uuidRef != null ? new NodeRef(uuidRef) : null;
            }
            Object javaValue = PropertyUtils.getProperty(alfObject, fieldName);
            // if (propertyDesc.getDataType() != null) {
            Class<? extends Serializable> alfrescoType = (Class<? extends Serializable>) propertyDesc.getPropertyClassNotNull();
            if (javaValue != null) {
                if (javaValue.getClass().isArray() || javaValue instanceof Collection) {
                    return (Serializable) TypeConversionUtil.convertToList(alfrescoType, javaValue, null, null);
                } else {
                    return TypeConversionUtil.convertTo(alfrescoType, javaValue);
                }
            }
            // }
            return (Serializable) javaValue;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to get property " + propertyDesc, e);
        }
    }

    @Override
    public void setProperties(Map<QName, Serializable> properties) {
        for (QName propName : properties.keySet()) {
            AlfPropertyDesc propertyDesc = typeDesc.getPropertyDescByTypeName(propName.toString());
            if (propertyDesc != null) {
                setProperty(propertyDesc, properties.get(propName));
            }
        }
    }

    private void setProperty(AlfPropertyDesc propertyDesc, Serializable alfrescoValue) {
        try {
            String fieldName = propertyDesc.getFieldName();
            if (propertyDesc.isNodeReference()) {
                alfObject.setReferencePropertyUuid(fieldName, alfrescoValue != null ? alfrescoValue.toString() : null);
                return;
            }
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
            if (propertyDescriptor == null) {
                throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
            }
            Object javaValue;
            if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                Class<?> componentClass = propertyDesc.getPropertyClassNotNull();
                javaValue = TypeConversionUtil.convertToList(componentClass, alfrescoValue, null, null);
            } else {
                javaValue = TypeConversionUtil.convertTo(propertyDescriptor.getPropertyType(), alfrescoValue);
            }
            PropertyUtils.setProperty(alfObject, fieldName, javaValue);
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to set property " + propertyDesc + " to value " + alfrescoValue, e);
        }
    }

}
