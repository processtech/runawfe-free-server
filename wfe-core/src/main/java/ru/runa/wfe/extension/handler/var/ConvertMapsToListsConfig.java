package ru.runa.wfe.extension.handler.var;

import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.collect.Lists;

public class ConvertMapsToListsConfig extends Observable {

    private final List<ConvertMapToListOperation> operations = Lists.newArrayList();
    private final Sorting sorting = new Sorting();

    public List<ConvertMapToListOperation> getOperations() {
        return operations;
    }

    public Sorting getSorting() {
        return sorting;
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    public void deleteOperation(int index) {
        operations.remove(index);
        notifyObservers();
    }

    public void addOperation() {
        ConvertMapToListOperation operation = new ConvertMapToListOperation();
        operations.add(operation);
        notifyObservers();
    }

    @Override
    public String toString() {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("conversions");
        for (ConvertMapToListOperation operation : operations) {
            operation.serialize(rootElement);
        }
        if (sorting.isEnabled()) {
            Element sortingElement = rootElement.addElement("sorting");
            sortingElement.addAttribute("by", sorting.getSortBy());
            sortingElement.addAttribute("mode", sorting.getSortMode());
        }
        return XmlUtils.toString(document);
    }

    public static ConvertMapsToListsConfig fromXml(String xml) {
        ConvertMapsToListsConfig model = new ConvertMapsToListsConfig();
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        List<Element> operationElements = rootElement.elements("operation");
        for (Element operationElement : operationElements) {
            ConvertMapToListOperation mapping = ConvertMapToListOperation.deserialize(operationElement);
            model.operations.add(mapping);
        }
        Element sortingElement = rootElement.element("sorting");
        if (sortingElement != null) {
            model.sorting.setSortBy(sortingElement.attributeValue("by"));
            model.sorting.setSortMode(sortingElement.attributeValue("mode"));
        }
        return model;
    }

    public static class Sorting {

        public static final String NONE = "none";
        public static final String KEYS = "keys";
        public static final String VALUES = "values";

        public static final String MODE_ASC = "asc";
        public static final String MODE_DESC = "desc";

        private String sortBy = NONE;
        private String sortMode = MODE_ASC;

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        public String getSortMode() {
            return sortMode;
        }

        public void setSortMode(String sortMode) {
            this.sortMode = sortMode;
        }

        public boolean isEnabled() {
            return !NONE.equals(sortBy);
        }

    }

}
