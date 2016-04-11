package ru.runa.wfe.commons.ftl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FreemarkerConfiguration {
    private Log log = LogFactory.getLog(FreemarkerConfiguration.class);
    private static final String CONFIG = "ftl.form.components.xml";
    private static final String TAG_ELEMENT = "component";
    private static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    private final Map<String, Class<? extends FormComponent>> map = Maps.newHashMap();

    private static FreemarkerConfiguration instance;

    public static FreemarkerConfiguration getInstance() {
        if (instance == null) {
            instance = new FreemarkerConfiguration();
        }
        return instance;
    }

    public String getRegistrationInfo() {
        return Joiner.on(", ").join(map.values());
    }

    private FreemarkerConfiguration() {
        if (SystemProperties.isV3CompatibilityMode() || "true".equals(System.getProperty("deprecated.ftl.tags.enabled"))) {
            parseTags(SystemProperties.DEPRECATED_PREFIX + CONFIG, false);
        }
        parseTags(CONFIG, true);
        parseTags(SystemProperties.RESOURCE_EXTENSION_PREFIX + CONFIG, false);
    }

    private void parseTags(String fileName, boolean required) {
        InputStream is;
        if (required) {
            is = ClassLoaderUtil.getAsStreamNotNull(fileName, getClass());
        } else {
            is = ClassLoaderUtil.getAsStream(fileName, getClass());
        }
        if (is != null) {
            Document document = XmlUtils.parseWithoutValidation(is);
            Element root = document.getRootElement();
            List<Element> tagElements = root.elements(TAG_ELEMENT);
            for (Element tagElement : tagElements) {
                String name = tagElement.attributeValue(NAME_ATTR);
                try {
                    String className = tagElement.attributeValue(CLASS_ATTR);
                    Class<? extends FormComponent> tagClass = (Class<? extends FormComponent>) ClassLoaderUtil.loadClass(className);
                    addComponent(name, tagClass);
                    addComponent(FormComponent.TARGET_PROCESS_PREFIX + name, tagClass);
                } catch (Throwable e) {
                    log.warn("Unable to create freemarker tag " + name, e);
                }
            }
        }
    }

    private void addComponent(String name, Class<? extends FormComponent> componentClass) {
        if (componentClass != null) {
            // test creation
            ClassLoaderUtil.instantiate(componentClass);
        }
        map.put(name, componentClass);
        log.debug("Registered tag " + name + " as " + componentClass);
    }

    public FormComponent getComponent(String name) {
        Class<? extends FormComponent> componentClass = map.get(name);
        if (componentClass != null) {
            return ClassLoaderUtil.instantiate(componentClass);
        }
        return null;
    }
}
