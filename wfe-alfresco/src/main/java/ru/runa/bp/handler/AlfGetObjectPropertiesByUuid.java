package ru.runa.bp.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.webservice.types.NamedValue;

import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.AlfPropertyDesc;
import ru.runa.alfresco.RemoteAlfConnection;
import ru.runa.alfresco.RemoteAlfObjectAccessor;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.extension.handler.ParamDef;

import com.google.common.base.Strings;

public class AlfGetObjectPropertiesByUuid extends AlfHandler {
    private static final String DEFAULT_FORMAT_CLASS = String.class.getName();

    @Override
    protected void executeAction(AlfConnection alfConnection, AlfHandlerData alfHandlerData) throws Exception {
        Map<String, ParamDef> outputParams = alfHandlerData.getOutputParams();
        String uuid = alfHandlerData.getInputParamValue(String.class, "uuid");
        if (!Strings.isNullOrEmpty(uuid)) {
            uuid = uuid.trim();
        }
        if (!Strings.isNullOrEmpty(uuid)) {
            NamedValue[] props = ((RemoteAlfConnection) alfConnection).loadObjectProperties(uuid);
            if (props == null) {
                throw new InternalApplicationException("No object can be loaded by uuid = '" + uuid + "'");
            }
            for (NamedValue namedValue : props) {
                int index = namedValue.getName().lastIndexOf('}');
                String keyName = namedValue.getName().substring(index + 1);
                if (outputParams.containsKey(keyName)) {
                    String targetClassName = alfHandlerData.getInputParamValue(String.class, keyName, DEFAULT_FORMAT_CLASS);
                    Object value = getProperty(namedValue, null, targetClassName);
                    String outputVarName = alfHandlerData.getOutputParams().get(keyName).getVariableName();
                    alfHandlerData.setOutputVariable(outputVarName, value);
                }
            }
        } else {
            log.warn("Ignored to load properties by empty uuid '" + uuid + "'");
        }
    }

    /**
     * @see RemoteAlfObjectAccessor
     */
    private Object getProperty(NamedValue prop, AlfPropertyDesc desc, String targetClassName) throws Exception {
        String stringValue = prop.getValue();
        Class<?> targetClass = Class.forName(targetClassName);
        return alfrescoToJava(stringValue, targetClass);
    }

    private Object alfrescoToJava(String value, Class<?> targetClass) throws Exception {
        if (value == null) {
            return null;
        }
        if (targetClass == Long.class || targetClass == long.class) {
            return Long.parseLong(value);
        }
        if (targetClass == Integer.class || targetClass == int.class) {
            return Integer.parseInt(value);
        }
        if (targetClass == Boolean.class || targetClass == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetClass == Date.class) {
            Calendar c = Calendar.getInstance();
            c.setTime(ISO8601DateFormat.parse(value));
            return c.getTime();
        }
        return value;
    }
}
