package ru.runa.wfe;

import java.util.Locale;
import java.util.Properties;

import ru.runa.wfe.commons.ClassLoaderUtil;

public abstract class LocalizableException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    private final Object[] details;

    public LocalizableException(String messageKey, Object... details) {
        super(messageKey);
        this.details = details;
    }

    public LocalizableException(String messageKey, Throwable cause, Object... details) {
        super(messageKey, cause);
        this.details = details;
    }

    protected abstract String getResourceBaseName();

    @Override
    public String getLocalizedMessage() {
        return getLocalizedMessage(null);
    }

    /**
     * Load localized message for specified locale.
     * 
     * @param locale
     *            Preferred locale to load message.
     * @return Returns loaded localized message.
     */
    public String getLocalizedMessage(Locale locale) {
        try {
            Properties properties = ClassLoaderUtil.getLocalizedProperties(getResourceBaseName(), getClass(), locale);
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
