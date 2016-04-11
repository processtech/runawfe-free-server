package ru.runa.wfe;

import java.util.Properties;

import ru.runa.wfe.commons.ClassLoaderUtil;

public abstract class LocalizableException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    private final Object[] details;

    public LocalizableException(String message, Object... details) {
        super(message);
        this.details = details;
    }

    public LocalizableException(String message, Throwable cause, Object... details) {
        super(message, cause);
        this.details = details;
    }

    protected abstract String getResourceBaseName();

    @Override
    public String getLocalizedMessage() {
        try {
            Properties properties = ClassLoaderUtil.getLocalizedProperties(getResourceBaseName(), getClass());
            String s = properties.getProperty(getMessage());
            if (s != null) {
                if (details != null) {
                    return String.format(s, details);
                }
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getLocalizedMessage();
    }
}
