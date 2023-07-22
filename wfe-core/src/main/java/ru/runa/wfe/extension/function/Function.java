package ru.runa.wfe.extension.function;

import com.google.common.base.MoreObjects;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;

public abstract class Function<T extends Object> {
    protected final Log log = LogFactory.getLog(getClass());
    private final Param[] parameterDefinitions;
    
    protected Function(Param... parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public String getName() {
        return getClass().getSimpleName();
    }
    
    public Param[] getParameterDefinitions() {
        return parameterDefinitions;
    }
    
    public final T execute(Object... parameters) {
        log.debug("Executing function with " + Arrays.toString(parameters));
        if (parameters.length < parameterDefinitions.length) {
            Object[] tmp = new Object[parameterDefinitions.length];
            System.arraycopy(parameters, 0, tmp, 0, parameters.length);
            for (int i = parameters.length; i < tmp.length; i++) {
                if (!parameterDefinitions[i].optional) {
                    throw new InternalApplicationException("Required parameter [" + i + "] is not provided in " + this);
                }
            }
            parameters = tmp;
        }
        Param lastParam = parameterDefinitions.length > 0 ? parameterDefinitions[parameterDefinitions.length - 1] : null;
        for (int i=0; i<parameters.length; i++) {
            Param paramDefinition;
            if (i >= parameterDefinitions.length) {
                if (lastParam != null && lastParam.multiple) {
                    paramDefinition = lastParam;
                } else {
                    throw new InternalApplicationException("Parameters count (" + parameters.length + ") differs from defined in " + this);
                }
            } else {
                paramDefinition = parameterDefinitions[i];
            }
            if (parameters[i] == null && paramDefinition.optional) {
                parameters[i] = paramDefinition.optionalValue;
            }
            parameters[i] = TypeConversionUtil.convertTo(paramDefinition.definedClass, parameters[i]);
            if (parameters[i] == null) {
                parameters[i] = TypeConversionUtil.getNotNullValue(paramDefinition.definedClass);
            }
        }
        T result = doExecute(parameters);
        log.debug("Result = " + result);
        return result;
    }
    
    protected abstract T doExecute(Object... parameters);
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("parameters count", parameterDefinitions.length).toString();
    }
    
    public static class Param {
        public final Class<?> definedClass;
        public final boolean optional;
        public final Object optionalValue;
        public final boolean multiple;
                
        private Param(Class<?> definedClass, boolean optional, Object optionalValue, boolean multiple) {
            this.definedClass = definedClass;
            this.optional = optional;
            this.optionalValue = optionalValue;
            this.multiple = multiple;
        }

        public static Param required(Class<?> definedClass) {
            return new Param(definedClass, false, null, false);
        }

        public static Param optional(Class<?> definedClass, Object defaultValue) {
            return new Param(definedClass, true, defaultValue, false);
        }

        public static Param multiple(Class<?> definedClass) {
            return new Param(definedClass, false, null, true);
        }

    }
}
