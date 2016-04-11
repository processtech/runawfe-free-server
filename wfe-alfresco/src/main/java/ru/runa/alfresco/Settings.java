package ru.runa.alfresco;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Base class for configurable items. Configuration of the system is extendible through one XML file. S
 * 
 * @author dofs
 */
public class Settings {
    protected static Log log = LogFactory.getLog(Settings.class);
    private static final String CONFIG_RESOURCE = "alfwf.settings.xml";

    protected static Document getConfigDocument() throws Exception {
        InputStream is = null;
        if (!EXTERNAL_SETTINGS) {
            is = ClassLoaderUtil.getAsStreamNotNull(CONFIG_RESOURCE, Settings.class);
        } else {
            is = new FileInputStream(EXTERNAL_RESOURCE);
        }
        return XmlUtils.parseWithoutValidation(is);
    }

    protected static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return "true".equals(value);
    }

    protected static int parseInt(String value, int defaultValue) {
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Unparsable int: " + value, e);
            return defaultValue;
        }
    }

    private static String EXTERNAL_RESOURCE = "";
    private static boolean EXTERNAL_SETTINGS = false;

    public static void setExternalSettingsFile(String file) {
        EXTERNAL_RESOURCE = file;
        EXTERNAL_SETTINGS = true;
    }

    public static void setInternalSettings() {
        EXTERNAL_SETTINGS = false;
    }

}