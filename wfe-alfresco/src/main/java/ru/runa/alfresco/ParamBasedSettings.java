package ru.runa.alfresco;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

/**
 * Settings for predefined abstract configuration format.
 * 
 * @author dofs
 */
public class ParamBasedSettings extends Settings {
    protected static Map<String, String> properties = new HashMap<String, String>();

    @SuppressWarnings("unchecked")
    protected static void load(Element mainElement) {
        try {
            List<Element> propElements = mainElement.elements("property");
            for (Element element : propElements) {
                String name = element.attributeValue("name");
                String value = element.getTextTrim();
                properties.put(name, value);
            }
            log.info(mainElement.getName() + " settings: " + properties);
        } catch (Throwable e) {
            log.error("Unable to load production info", e);
        }
    }

    public static String getProperty(String name) {
        return properties.get(name);
    }

    public static int getIntProperty(String name, int defaultValue) {
        return parseInt(getProperty(name), defaultValue);
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        return parseBoolean(getProperty(name), defaultValue);
    }

}
