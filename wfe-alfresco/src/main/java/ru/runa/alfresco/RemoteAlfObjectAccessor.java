package ru.runa.alfresco;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ITypeConvertor;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Converts java properties from/to alfresco {@link NamedValue}.
 * 
 * Limitations: list of strings only supported.
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class RemoteAlfObjectAccessor extends AlfObjectAccessor<NamedValue[]> {
    public static final FromStringDateConverter FROM_STRING_DATE_CONVERTER = new FromStringDateConverter();
    public static final ToStringDateConverter TO_STRING_DATE_CONVERTER = new ToStringDateConverter();

    public RemoteAlfObjectAccessor(AlfTypeDesc typeDesc, AlfObject alfObject) {
        super(typeDesc, alfObject);
    }

    @Override
    public NamedValue[] getProperties(boolean all, boolean includeName) {
        Map<String, NamedValue> properties = new HashMap<String, NamedValue>();
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
                NamedValue namedValue = getProperty(desc);
                properties.put(namedValue.getName(), namedValue);
            }
        }
        if (includeName) {
            NamedValue namedValue = Utils.createNamedValue(Constants.PROP_NAME, alfObject.getObjectName());
            properties.put(namedValue.getName(), namedValue);
        }
        return new ArrayList<NamedValue>(properties.values()).toArray(new NamedValue[properties.size()]);
    }

    private NamedValue getProperty(AlfPropertyDesc propertyDesc) {
        try {
            String fieldName = propertyDesc.getFieldName();
            if (propertyDesc.isNodeReference()) {
                String uuidRef = alfObject.getReferencePropertyUuid(fieldName, true);
                return Utils.createNamedValue(propertyDesc.getPropertyNameWithNamespace(), uuidRef);
            }
            Object javaValue = PropertyUtils.getProperty(alfObject, fieldName);
            if (javaValue != null && (javaValue.getClass().isArray() || javaValue instanceof Collection<?>)) {
                List<String> list = TypeConversionUtil.convertToList(String.class, javaValue, TO_STRING_DATE_CONVERTER, null);
                return Utils.createNamedValue(propertyDesc.getPropertyNameWithNamespace(), list.toArray(new String[list.size()]));
            } else {
                String alfrescoString = TypeConversionUtil.convertTo(String.class, javaValue, TO_STRING_DATE_CONVERTER, null);
                return Utils.createNamedValue(propertyDesc.getPropertyNameWithNamespace(), alfrescoString);
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to get property " + propertyDesc, e);
        }
    }

    @Override
    public void setProperties(NamedValue[] properties) {
        for (NamedValue prop : properties) {
            AlfPropertyDesc propertyDesc = typeDesc.getPropertyDescByTypeName(prop.getName());
            if (propertyDesc != null) {
                setProperty(propertyDesc, prop);
            }
        }
    }

    private void setProperty(AlfPropertyDesc propertyDesc, NamedValue prop) {
        try {
            String fieldName = propertyDesc.getFieldName();
            if (propertyDesc.isNodeReference()) {
                alfObject.setReferencePropertyUuid(fieldName, prop.getValue());
                return;
            }
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(alfObject, fieldName);
            if (propertyDescriptor == null) {
                throw new IllegalArgumentException("No property '" + fieldName + "' found in " + getClass());
            }
            Object javaValue;
            if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                Class<?> componentClass = propertyDesc.getPropertyClassNotNull();
                javaValue = TypeConversionUtil.convertToList(componentClass, prop.getValues(), FROM_STRING_DATE_CONVERTER, null);
            } else {
                Object alfrescoValue = prop.getIsMultiValue() ? prop.getValues() : prop.getValue();
                javaValue = TypeConversionUtil.convertTo(propertyDescriptor.getPropertyType(), alfrescoValue, FROM_STRING_DATE_CONVERTER, null);
            }
            PropertyUtils.setProperty(alfObject, fieldName, javaValue);
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to set property " + propertyDesc + " to value " + prop, e);
        }
    }

    public static class FromStringDateConverter implements ITypeConvertor {

        @Override
        public <T> T convertTo(Object object, Class<T> classConvertTo) {
            if (object instanceof String) {
                if (classConvertTo == Date.class || classConvertTo == Calendar.class) {
                    synchronized (ISO8601DateFormat.class) {
                        Date date = ISO8601DateFormat.parse((String) object);
                        if (classConvertTo == Calendar.class) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(date);
                            return (T) c;
                        }
                        return (T) date;
                    }
                }
            }
            return null;
        }

    }

    public static class ToStringDateConverter implements ITypeConvertor {

        @Override
        public <T> T convertTo(Object object, Class<T> classConvertTo) {
            if (object instanceof Calendar) {
                object = ((Calendar) object).getTime();
            }
            if (object instanceof Date) {
                synchronized (ISO8601DateFormat.class) {
                    return (T) ISO8601DateFormat.format((Date) object);
                }
            }
            return null;
        }

    }

}
