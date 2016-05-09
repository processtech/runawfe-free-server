package ru.runa.wfe.report;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Описывает тип параметра, требуемого для построения отчета.
 */
public enum ReportParameterType {
    /**
     * Строковый параметр.
     */
    STRING(String.class) {
        @Override
        public String getDescription() {
            return "Строка";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onString(data);
        }
    },

    /**
     * Числовой параметр.
     */
    NUMBER(Long.class) {
        @Override
        public String getDescription() {
            return "Целое число";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onNumber(data);
        }
    },

    /**
     * Дата.
     */
    DATE(Date.class) {
        @Override
        public String getDescription() {
            return "Дата";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onDate(data);
        }
    },

    /**
     * Выбор названия бизнесс процесса или null, если пользователя выбирает все процессы (нет фильтра по процессу).
     */
    PROCESS_NAME_OR_NULL(String.class) {
        @Override
        public String getDescription() {
            return "Выбор типа процесса";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onProcessNameOrNull(data);
        }
    },

    /**
     * Выбор роли бизнес процесса.
     */
    SWIMLANE(String.class) {
        @Override
        public String getDescription() {
            return "Выбор роли бизнес процесса";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onSwimlane(data);
        }
    },

    /**
     * Флажок (по умолчанию не проставлен).
     */
    BOOLEAN_UNCHECKED(Boolean.class) {
        @Override
        public String getDescription() {
            return "Флаг (по умолчанию снят)";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onUncheckedBoolean(data);
        }
    },

    /**
     * Выбор роли бизнес процесса.
     */
    BOOLEAN_CHECKED(Boolean.class) {
        @Override
        public String getDescription() {
            return "Флаг (по умолчанию проставлен)";
        }

        @Override
        public <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data) {
            return visitor.onCheckedBoolean(data);
        }
    };

    /**
     * Java тип, в который преобразуется параметр.
     */
    private final Class<?> javaType;

    /**
     * Отображение из типа java, в тип параметра.
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
     * Возвращает Java тип, в который преобразуется параметр.
     * 
     * @return Возвращает Java тип, в который преобразуется параметр.
     */
    public final Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Преобразует java тип, в тип параметра.
     * 
     * @param javaType
     *            java тип, для которого создаётся параметр.
     * @return Возвращает тип параметра.
     */
    public static ReportParameterType getForClass(Class<?> javaType) {
        ReportParameterType type = classRegistry.get(javaType);
        return type == null ? STRING : type;
    }

    /**
     * Возвращает описание типа параметра, которое может быть показано пользователю.
     * 
     * @return Возвращает описание типа параметра, которое может быть показано пользователю.
     */
    public abstract String getDescription();

    /**
     * Применяет операцию к типу параметра.
     * 
     * @param visitor
     *            Операция, применяемая в зависимости от типа параметра.
     * @param data
     *            Данные, передаваемые в операцию.
     * @return Возвращает результат применения операции.
     * @throws Exception
     */
    public abstract <TResult, TData> TResult processBy(ReportParameterTypeVisitor<TResult, TData> visitor, TData data);

    /**
     * Интерфейс операции, которая может быть применена к типу параметра.
     * 
     * @param <TResult>
     *            Тип результата применения операции.
     * @param <TData>
     *            Тип данных, передаваемых в операцию.
     */
    public interface ReportParameterTypeVisitor<TResult, TData> {
        /**
         * Вызывается для строкового параметра.
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * @return Возвращает результат применения операции.
         */
        TResult onString(TData data);

        /**
         * Вызывается для числового параметра.
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onNumber(TData data);

        /**
         * Вызывается для параметра типа дата.
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onDate(TData data);

        /**
         * Вызывается для параметра типа флажок (по умолчанию снят).
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onUncheckedBoolean(TData data);

        /**
         * Вызывается для параметра типа флажок (по умолчанию проставлен).
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onCheckedBoolean(TData data);

        /**
         * Вызывается для параметра с выбором типа процесса или null для всех процессов.
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onProcessNameOrNull(TData data);

        /**
         * Вызывается для параметра с выбором роли бизнес процесса.
         * 
         * @param data
         *            Данные, передаваемые в операцию.
         * 
         * @return Возвращает результат применения операции.
         */
        TResult onSwimlane(TData data);
    }
}
