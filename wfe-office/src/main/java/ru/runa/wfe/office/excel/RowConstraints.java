package ru.runa.wfe.office.excel;

import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;

public class RowConstraints extends OnSheetConstraints {
    private int rowIndex;
    private int columnStartIndex;

    private final List<ColumnMapping> columns = new ArrayList<>();

    public static class ColumnMapping {
        public final String attributeName;
        public final int column;

        public ColumnMapping(String attributeName, int column) {
            this.attributeName = attributeName;
            this.column = column;
        }
    }

    public List<ColumnMapping> getColumns() {
        return columns;
    }

    @Override
    public void configure(Element element) {
        super.configure(element);

        this.rowIndex = getIndex(element, "row", 0);
        this.columnStartIndex = getIndex(element, "columnStart", 0);

        columns.clear();
        Element parent = element.getParent();
        if (parent != null) {
            List<Element> mappingElements = parent.elements("mapping");
            for (Element mappingElement : mappingElements) {
                String attr = mappingElement.attributeValue("field");
                String colStr = mappingElement.attributeValue("column");

                if (attr != null && colStr != null) {
                    columns.add(new ColumnMapping(attr, Integer.parseInt(colStr) - 1));
                }
            }
        }
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnStartIndex() {
        return columnStartIndex;
    }
}