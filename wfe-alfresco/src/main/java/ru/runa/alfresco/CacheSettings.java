package ru.runa.alfresco;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;

public class CacheSettings extends Settings {
    private static InputStream configurationInputStream;

    static {
        load();
    }

    private static void load() {
        try {
            Document document = getConfigDocument();
            Element root = document.getRootElement();
            Element connectionElement = root.element("cache");
            if (connectionElement == null) {
                log.info("Cache is disabled (no element).");
                return;
            }
            if (!parseBoolean(connectionElement.attributeValue("enabled"), false)) {
                log.info("Cache is disabled (enabled='false').");
                return;
            }
            String configuration = connectionElement.attributeValue("configuration");
            configurationInputStream = CacheSettings.class.getResourceAsStream(configuration);
            if (configurationInputStream != null) {
                log.info("Cache is enabled (" + configuration + ").");
            } else {
                log.info("Cache is disabled (Resource not found in " + configuration + ").");
            }
        } catch (Throwable e) {
            log.error("Cache is disabled (error)", e);
        }
    }

    public static boolean isCacheEnabled() {
        return configurationInputStream != null;
    }

    public static InputStream getConfigurationInputStream() {
        return configurationInputStream;
    }

}
