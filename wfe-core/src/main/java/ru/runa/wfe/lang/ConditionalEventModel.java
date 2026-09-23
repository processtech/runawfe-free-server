package ru.runa.wfe.lang;

import com.google.common.base.Strings;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;

/**
 * Configuration model for conditional {@link CatchEventNode}.
 *
 * <p>Represents configuration stored as XML string
 * in {@link Delegation#getConfiguration()}.
 *
 * <p>Currently used only for parsing (no serialization logic).
 */
public class ConditionalEventModel {

    private static final String EXPRESSION = "expression";
    private static final String STORAGE = "storage";
    private static final String INTERVAL = "interval";


    private String expression = "";
    private Element storage;
    private String interval;

    // --- parse ---
    public static ConditionalEventModel fromXml(String xml) {
        ConditionalEventModel model = new ConditionalEventModel();

        if (Strings.isNullOrEmpty(xml)) {
            return model;
        }

        Document doc = XmlUtils.parseWithoutValidation(xml);
        Element root = doc.getRootElement();

        model.expression = Strings.nullToEmpty(root.elementText(EXPRESSION));

        Element storage = root.element(STORAGE);
        if (storage != null && !storage.elements().isEmpty()) {
            model.storage = storage;
        }

        model.interval = root.elementText(INTERVAL);

        return model;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Element getStorage() {
        if (storage == null) {
            return null;
        }
        return storage.createCopy();
    }

    public Element getStorageUnsafe() {
        return storage;
    }

    public void setStorage(Element storage) {
        if (storage == null) {
            this.storage = null;
            return;
        }

        Element copy = storage.createCopy();
        copy.setName(STORAGE);
        this.storage = copy;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
