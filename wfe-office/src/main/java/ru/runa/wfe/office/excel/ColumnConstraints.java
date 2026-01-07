package ru.runa.wfe.office.excel;

import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;

public class ColumnConstraints extends OnSheetConstraints {
    private int columnIndex;
    private int rowStartIndex;

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

        this.columnIndex = getIndex(element, "column");
        this.rowStartIndex = getIndex(element, "rowStart", 0);

        columns.clear();

        Element parent = element.getParent();

        if(parent != null){
            List<Element> mappingElements = parent.elements("mapping");

            for (Element mappingElement: mappingElements){
                String attr = mappingElement.attributeValue("field");
                String colStr = mappingElement.attributeValue("column");

                if(attr != null && colStr != null){
                    try {
                        int col = Integer.parseInt(colStr);

                        columns.add(new ColumnMapping(attr, col - 1));
                    }catch (NumberFormatException e){
                    }
                }
            }
        }
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowStartIndex() {
        return rowStartIndex;
    }

}
