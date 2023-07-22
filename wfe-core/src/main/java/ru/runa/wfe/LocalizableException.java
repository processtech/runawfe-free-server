package ru.runa.wfe;

import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Properties;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;

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

    private static Locale preferredLocale = Locale.getDefault();

    @Override
    public String getMessage() {
        return getLocalizedMessage();
    }

    @Override
    public String getLocalizedMessage() {
        String preferredLanguage = SystemProperties.getPreferredMessagesLanguage();
        if (Strings.isNullOrEmpty(preferredLanguage)) {
            preferredLocale = Locale.getDefault();
        } else if (!preferredLocale.getLanguage().equals(preferredLanguage)) {
            preferredLocale = new Locale(preferredLanguage);
        }
        return getLocalizedMessage(preferredLocale);
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
            String s = properties.getProperty(super.getMessage());
            if (s != null) {
                if (details != null) {
                    return String.format(s, details);
                }
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getMessage();
    }
}
