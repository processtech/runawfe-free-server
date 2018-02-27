package ru.runa.wfe.extension.function;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class Mapping extends Function<String> {

    private static final Log log = LogFactory.getLog(Mapping.class);

    private static TreeMap<String, HashMap<String, String>> mappingConf = new TreeMap<String, HashMap<String, String>>();

    static {
        readMappingConfig("mappingConf.xml");
    }

    @Override
    protected String doExecute(Object... parameters) {
        String input = (String) parameters[0];
        String rule = (String) parameters[1];
        try {
            return mappingConf.get(rule).get(input);
        } catch (Exception e) {
            log.error("No mapping rule for " + input + " / " + rule, e);
        }
        return input;
    }

    @SuppressWarnings("unchecked")
    private static void readMappingConfig(String path) {
        try {
            InputStream is = ClassLoaderUtil.getAsStream(path, Mapping.class);
            if (is == null) {
                log.warn("No " + path + " found");
                return;
            }
            Document document = XmlUtils.parseWithoutValidation(is);
            List<Element> childs = document.getRootElement().elements();
            for (Element rule : childs) {
                String title = rule.attributeValue("title");
                HashMap<String, String> rmap = new HashMap<String, String>();
                for (Element item : (List<Element>) rule.elements()) {
                    String input = item.attributeValue("input");
                    String output = item.attributeValue("output");
                    rmap.put(input, output);
                }
                mappingConf.put(title, rmap);
            }
        } catch (Exception e) {
            log.error("Can`t parse " + path, e);
        }
    }

}
