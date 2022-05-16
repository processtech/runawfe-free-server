package ru.runa.wfe.chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.ClassLoaderUtil;

@Component
public class ChatLocalizationService {

    public static final Locale DEFAULT_LOCALE = new Locale("en");

    public static final List<Locale> SUPPORTED_CHAT_LOCALES = Arrays.asList(DEFAULT_LOCALE, new Locale("ru"));

    private static final String RESOURCE_NAME = "chat.localization";

    private final Map<Locale, Properties> localizedProperties = new HashMap<>();

    public ChatLocalizationService() {
        for (Locale locale : SUPPORTED_CHAT_LOCALES) {
            localizedProperties.put(locale, ClassLoaderUtil.getLocalizedProperties(RESOURCE_NAME, ChatLocalizationService.class, locale));
        }
    }

    public String getLocalizedString(String propertyKey, Locale clientLocale) {
        return localizedProperties.getOrDefault(clientLocale, localizedProperties.get(DEFAULT_LOCALE)).getProperty(propertyKey);
    }
}
