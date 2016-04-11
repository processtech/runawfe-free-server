package ru.runa;

import java.lang.reflect.Field;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import ru.runa.alfresco.AlfPropertyDesc;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ClassUtils {
    private static Map<String, Field> FIELDS_CACHE = Maps.newHashMap();

    public static Class<?> getImplClass(Class<?> implOrProxyClass) {
        if (Enhancer.isEnhanced(implOrProxyClass)) {
            return implOrProxyClass.getSuperclass();
        }
        return implOrProxyClass;
    }

    public static Object getFieldValue(Object object, AlfPropertyDesc desc) {
        String key = object.getClass().getSimpleName() + "." + desc.getFieldName();
        try {
            if (!FIELDS_CACHE.containsKey(key)) {
                Class<?> clazz = object.getClass();
                while (clazz != Object.class) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (Objects.equal(desc.getFieldName(), field.getName())) {
                            field.setAccessible(true);
                            FIELDS_CACHE.put(key, field);
                            break;
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
            if (!FIELDS_CACHE.containsKey(key)) {
                throw new InternalApplicationException("No field found by name '" + desc.getFieldName() + "' in " + object.getClass());
            }
            return FIELDS_CACHE.get(key).get(object);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
