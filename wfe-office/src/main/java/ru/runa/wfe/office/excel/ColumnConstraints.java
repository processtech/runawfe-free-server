package ru.runa.wfe.office.excel;

import org.dom4j.Element;

public class ColumnConstraints extends OnSheetConstraints {
    private int columnIndex;
    private int rowStartIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        columnIndex = getIndex(element, "column");
        rowStartIndex = getIndex(element, "rowStart", 0);
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowStartIndex() {
        return rowStartIndex;
    }

}
