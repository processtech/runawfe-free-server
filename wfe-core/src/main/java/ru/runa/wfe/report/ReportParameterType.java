package ru.runa.wfe.report;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the type of parameter required for report construction.
 */
public enum ReportParameterType {
    /**
     * String parameter.
     */
    STRING(String.class) {
        @Override
        public String getDescription() {
            return "String";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onString(data);
        }
    },

    /**
     * Number parameter.
     */
    NUMBER(Long.class) {
        @Override
        public String getDescription() {
            return "Integer";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onNumber(data);
        }
    },

    /**
     * Date parameter.
     */
    DATE(Date.class) {
        @Override
        public String getDescription() {
            return "Date";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onDate(data);
        }
    },

    /**
     * BP name selection or null, if user chooses all processes (not process filter is set).
     */
    PROCESS_NAME_OR_NULL(String.class) {
        @Override
        public String getDescription() {
            return "BP type selection";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onProcessNameOrNull(data);
        }
    },

    /**
     * BP swimlate selection.
     */
    SWIMLANE(String.class) {
        @Override
        public String getDescription() {
            return "BP swimlane selection";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onSwimlane(data);
        }
    },

    /**
     * Flag (is not set (false) by default).
     */
    BOOLEAN_UNCHECKED(Boolean.class) {
        @Override
        public String getDescription() {
            return "Flag (unchecked by default)";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onUncheckedBoolean(data);
        }
    },

    /**
     * Flag (is set (true) by default)
     */
    BOOLEAN_CHECKED(Boolean.class) {
        @Override
        public String getDescription() {
            return "Flag (checked by default)";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onCheckedBoolean(data);
        }
    };

    /**
     * Java type that is used for processed parameter.
     */
    private final Class<?> javaType;

    /**
     * Map of java type to parameter type.
     */
    private static Map<Class<?>, ReportParameterType> classRegistry = new HashMap<Class<?>, ReportParameterType>();

    private ReportParameterType(Class<?> javaType) {
        this.javaType = javaType;
    }

    static {
        for (ReportParameterType type : values()) {
            if (classRegistry.get(type.getJavaType()) == null) {
                classRegistry.put(type.getJavaType(), type);
            }
        }
    }

    /**
     * Returns java type that corresponds to parameter type.
     * 
     * @return java type that corresponds to parameter type.
     */
    public final Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Converts java type to correspondent parameter type.
     * 
     * @param javaType
     *            java type of the parameter.
     * @return parameter type.
     */
    public static ReportParameterType getForClass(Class<?> javaType) {
        ReportParameterType type = classRegistry.get(javaType);
        return type == null ? STRING : type;
    }

    /**
     * Returns parameters description that can be shown to user.
     * 
     * @return parameters description that can be shown to user..
     */
    public abstract String getDescription();

    /**
     * Applies operation to parameter type.
     * 
     * @param visitor
     *            Operation that can be applied depending on parameter type.
     * @param data
     *            Data that is supplied to operation.
     * @return result of operation being applied.
     * @throws Exception
     */
    public abstract <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data);

    /**
     * Interface of the operation that can be applied to parameter type.
     * 
     * @param <TResult>
     *            Result type of operation being applied.
     * @param <TData>
     *            Data type that is supplied to operation.
     */
    public interface ReportParameterTypeVisitor<TResult, TData> {
        /**
         * Called for string parameter.
         * 
         * @param data
         *            Data supplied to operation.
         * @return result of operation being applied.
         */
        TResult onString(TData data);

        /**
         * Called for number parameter
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onNumber(TData data);

        /**
         * Called for date parameter
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onDate(TData data);

        /**
         * Called for flag (boolean) parameter (unchecked by default).
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onUncheckedBoolean(TData data);

        /**
         * Called for flag (boolean) parameter (checked by default).
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onCheckedBoolean(TData data);

        /**
         * Called for parameter with BP type selection or null for all types.
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onProcessNameOrNull(TData data);

        /**
         * Called for parameter with BP swimlane selection.
         * 
         * @param data
         *            Data supplied to operation.
         * 
         * @return result of operation being applied.
         */
        TResult onSwimlane(TData data);
    }
}
