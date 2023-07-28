package ru.runa.wfe.commons.ftl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

@SuppressWarnings("unchecked")
@CommonsLog
public class FreemarkerConfiguration {
    private static final String TAG_ELEMENT = "component";
    private static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    private static final Map<String, Class<? extends FormComponent>> definitions = Maps.newHashMap();

    static {
        ClassLoaderUtil.withExtensionResources("ftl.form.components.xml", new Function<InputStream, Object>() {

            @Override
            public Object apply(InputStream input) {
                try (InputStream inputStream = input) {
                    Document document = XmlUtils.parseWithoutValidation(inputStream);
                    Element root = document.getRootElement();
                    List<Element> tagElements = root.elements(TAG_ELEMENT);
                    for (Element tagElement : tagElements) {
                        String name = tagElement.attributeValue(NAME_ATTR);
                        try {
                            String className = tagElement.attributeValue(CLASS_ATTR);
                            @SuppressWarnings("unchecked")
                            Class<? extends FormComponent> tagClass = (Class<? extends FormComponent>) ClassLoaderUtil.loadClass(className);
                            addComponent(name, tagClass);
                            addComponent(FormComponent.TARGET_PROCESS_PREFIX + name, tagClass);
                        } catch (Throwable e) {
                            log.warn("Unable to create freemarker tag " + name, e);
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static void forceLoadInThisClassLoader() {
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
