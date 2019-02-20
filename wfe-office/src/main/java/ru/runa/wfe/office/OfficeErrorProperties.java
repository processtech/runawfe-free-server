package ru.runa.wfe.office;

import java.util.Properties;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class OfficeErrorProperties {

    public static final String BLOCKED_FILE_ERROR = "error.blocked.file";
    public static final String WRONG_PARAMETER_ERROR = "error.wrong.parameter";
    public static final String WRONG_OPERATOR_ERROR = "error.wrong.operator";

    private static final Properties properties = ClassLoaderUtil.getLocalizedProperties("office.error", OfficeErrorProperties.class, null);

    public static String getMessage(String pattern, Object... arguments) {
        String message = properties.getProperty(pattern);
        if (arguments != null) {
            return String.format(message, arguments);
        }
        return message;
    }

}
