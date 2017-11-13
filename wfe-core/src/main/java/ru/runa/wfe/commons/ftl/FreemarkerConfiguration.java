package ru.runa.wfe.commons.ftl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FreemarkerConfiguration {
    private static Log log = LogFactory.getLog(FreemarkerConfiguration.class);
    private static final String CONFIG = "ftl.form.components.xml";
    private static final String TAG_ELEMENT = "component";
    private static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    private static final Map<String, Class<? extends FormComponent>> definitions = Maps.newHashMap();

    static {
        InputStream deprecatedInputStream = ClassLoaderUtil.getAsStream(SystemProperties.DEPRECATED_PREFIX + CONFIG, FreemarkerConfiguration.class);
        if (deprecatedInputStream != null) {
            registerDefinitions(deprecatedInputStream);
        }
        registerDefinitions(ClassLoaderUtil.getAsStream(CONFIG, FreemarkerConfiguration.class));
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + SystemProperties.RESOURCE_EXTENSION_PREFIX + CONFIG;
            Resource[] resources = ClassLoaderUtil.getResourcePatternResolver().getResources(pattern);
            for (Resource resource : resources) {
                registerDefinitions(resource.getInputStream());
            }
        } catch (IOException e) {
            log.error("unable load wfe.custom form component definitions", e);
        }
    }

    public static void forceLoad() {
    }

    private static void registerDefinitions(InputStream inputStream) {
        try {
            Preconditions.checkNotNull(inputStream);
            Document document = XmlUtils.parseWithoutValidation(inputStream);
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
            inputStream.close();
        } catch (Exception e) {
            log.error("unable load form component definitions", e);
        }
    }

    private static void addComponent(String name, Class<? extends FormComponent> componentClass) {
        if (componentClass != null) {
            // test creation
            ClassLoaderUtil.instantiate(componentClass);
        }
        definitions.put(name, componentClass);
        log.debug("Registered form component " + name + " as " + componentClass);
    }

    public static FormComponent getComponent(String name) {
        Class<? extends FormComponent> componentClass = definitions.get(name);
        if (componentClass != null) {
            return ClassLoaderUtil.instantiate(componentClass);
        }
        return null;
    }
}
