package ru.runa.wfe.office.storage.projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Value;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * @author Alekseev Mikhail
 * @since #1394
 */
@Value
public class ProjectionModel {
    String fieldName;
    Visibility visibility;
    Sort sort;

    @SuppressWarnings("unchecked")
    public static List<ProjectionModel> parse(String xml) {
        final Document document = XmlUtils.parseWithoutValidation(XmlUtils.unwrapCdata(xml));
        final Element root = document.getRootElement();

        final List<Element> projectionElements = root.elements("projection");
        if (projectionElements.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ProjectionModel> projectionModels = new ArrayList<>(projectionElements.size());
        for (Element projectionElement : projectionElements) {
            final String fieldName = projectionElement.attributeValue("name");
            final Visibility visibility = Visibility.valueOf(projectionElement.attributeValue("visibility", Visibility.VISIBLE.name()));
            final Sort sort = Sort.valueOf(projectionElement.attributeValue("sort", Sort.NONE.name()));
            projectionModels.add(new ProjectionModel(fieldName, visibility, sort));
        }

        return projectionModels;
    }
}
