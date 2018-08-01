package ru.runa.wfe.commons.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.xml.XmlUtils;

@CommonsLog
public class LocalizationParser {

    @SuppressWarnings("unchecked")
    public static List<Localization> parseLocalizations(InputStream stream) {
        Preconditions.checkNotNull(stream, "No localization data to parse.");
        List<Localization> localizations = Lists.newArrayList();
        try {
            Document document = XmlUtils.parseWithoutValidation(stream);
            Element root = document.getRootElement();
            List<Element> elements = root.elements("artifact");
            for (Element element : elements) {
                localizations.add(new Localization(element.attributeValue("name"), element.attributeValue("label")));
            }
        } catch (Exception e) {
            log.error("Unable parse localizations", e);
        }
        return localizations;
    }

}
