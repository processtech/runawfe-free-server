package ru.runa.wfe.presentation;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.presentation.filter.FilterCriteria;

public class FieldsSerializer {
    private static final Log log = LogFactory.getLog(FieldsSerializer.class);

    private static final String I = "i";
    private static final String STATE = "state";
    private static final String DISPLAY_IDS = "displayIds";
    private static final String SORT_IDS = "sortIds";
    private static final String SORT_MODES = "sortModes";
    private static final String GROUP_IDS = "groupIds";
    private static final String FILTERS = "filters";
    private static final String FILTER_CLASS_ATTR = "filterClass";
    private static final String FILTER_TEMPLATE = "tpl";
    private static final String DYNAMICS = "dynamics";
    private static final String EXPANDED_BLOCKS = "blocks";
    private static final String INDEX_ATTR = "index";
    private static final String VALUE_ATTR = "value";

    public static BatchPresentationFields fromDataSafe(ClassPresentationType type, byte[] data) {
        try {
            return fromData(type, data);
        } catch (Exception e) {
            String xml = data != null ? new String(data, Charsets.UTF_8) : "NULL";
            log.warn("Unable to load batch presentation state from " + xml + ", using default fields", e);
            return BatchPresentationFields.createDefaultFields(type);
        }
    }

    private static BatchPresentationFields fromData(ClassPresentationType type, byte[] data) {
        if (data == null) {
            return BatchPresentationFields.createDefaultFields(type);
        }
        BatchPresentationFields fields = new BatchPresentationFields();
        Document document = XmlUtils.parseWithoutValidation(data);
        Element root = document.getRootElement();
        List<Element> elements = root.element(DISPLAY_IDS).elements(I);
        fields.displayIds = new int[elements.size()];
        for (int i = 0; i < fields.displayIds.length; i++) {
            fields.displayIds[i] = Integer.parseInt(elements.get(i).getTextTrim());
        }
        elements = root.element(SORT_IDS).elements(I);
        fields.sortIds = new int[elements.size()];
        for (int i = 0; i < fields.sortIds.length; i++) {
            fields.sortIds[i] = Integer.parseInt(elements.get(i).getTextTrim());
        }
        elements = root.element(SORT_MODES).elements(I);
        fields.sortModes = new boolean[elements.size()];
        for (int i = 0; i < fields.sortModes.length; i++) {
            fields.sortModes[i] = Boolean.valueOf(elements.get(i).getTextTrim());
        }
        elements = root.element(GROUP_IDS).elements(I);
        fields.groupIds = new int[elements.size()];
        for (int i = 0; i < fields.groupIds.length; i++) {
            fields.groupIds[i] = Integer.parseInt(elements.get(i).getTextTrim());
        }
        elements = root.element(FILTERS).elements(I);
        for (Element element : elements) {
            Integer key = Integer.valueOf(element.attributeValue(INDEX_ATTR));
            FilterCriteria criteria = ClassLoaderUtil.instantiate(element.attributeValue(FILTER_CLASS_ATTR));
            List<String> templates = Lists.newArrayList();
            for (Element tplElement : (List<Element>) element.elements(FILTER_TEMPLATE)) {
                templates.add(tplElement.getText());
            }
            criteria.applyFilterTemplates(templates.toArray(new String[templates.size()]));
            fields.filters.put(key, criteria);
        }
        elements = root.element(DYNAMICS).elements(I);
        for (Element element : elements) {
            fields.dynamics.add(new DynamicField(Long.parseLong(element.attributeValue(INDEX_ATTR)), element.attributeValue(VALUE_ATTR)));
        }
        Element expandedBlocksElement = root.element(EXPANDED_BLOCKS);
        if (expandedBlocksElement != null) {
            elements = expandedBlocksElement.elements(I);
            for (Element element : elements) {
                fields.expandedBlocks.add(element.getText());
            }
        }
        return fields;
    }

    public static byte[] toData(ClassPresentationType type, BatchPresentationFields fields) {
        if (fields == null) {
            fields = BatchPresentationFields.createDefaultFields(type);
        }
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(STATE);
        Element displayIdsElement = root.addElement(DISPLAY_IDS);
        for (int i = 0; i < fields.displayIds.length; i++) {
            displayIdsElement.addElement(I).addText(String.valueOf(fields.displayIds[i]));
        }
        Element sortIdsElement = root.addElement(SORT_IDS);
        for (int i = 0; i < fields.sortIds.length; i++) {
            sortIdsElement.addElement(I).addText(String.valueOf(fields.sortIds[i]));
        }
        Element sortModesElement = root.addElement(SORT_MODES);
        for (int i = 0; i < fields.sortModes.length; i++) {
            sortModesElement.addElement(I).addText(String.valueOf(fields.sortModes[i]));
        }
        Element groupIdsElement = root.addElement(GROUP_IDS);
        for (int i = 0; i < fields.groupIds.length; i++) {
            groupIdsElement.addElement(I).addText(String.valueOf(fields.groupIds[i]));
        }
        Element filtersElement = root.addElement(FILTERS);
        for (Map.Entry<Integer, FilterCriteria> entry : fields.filters.entrySet()) {
            Element filterElement = filtersElement.addElement(I);
            filterElement.addAttribute(INDEX_ATTR, entry.getKey().toString());
            filterElement.addAttribute(FILTER_CLASS_ATTR, entry.getValue().getClass().getName());
            for (String template : entry.getValue().getFilterTemplates()) {
                filterElement.addElement(FILTER_TEMPLATE).addText(template);
            }
        }
        Element dynamicsElement = root.addElement(DYNAMICS);
        for (DynamicField dynamicField : fields.dynamics) {
            Element dynamicElement = dynamicsElement.addElement(I);
            dynamicElement.addAttribute(INDEX_ATTR, dynamicField.getFieldIdx().toString());
            dynamicElement.addAttribute(VALUE_ATTR, dynamicField.getDynamicValue());
        }
        if (fields.expandedBlocks != null) {
            // this can be for serialized batch presentation of previous version
            Element expandedBlocksElement = root.addElement(EXPANDED_BLOCKS);
            for (String expandedBlock : fields.expandedBlocks) {
                expandedBlocksElement.addElement(I).setText(expandedBlock);
            }
        }
        return XmlUtils.save(document);
    }

}
