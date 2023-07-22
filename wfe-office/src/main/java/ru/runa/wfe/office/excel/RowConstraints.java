package ru.runa.wfe.office.excel;

import org.dom4j.Element;

public class RowConstraints extends OnSheetConstraints {
    private int rowIndex;
    private int columnStartIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        rowIndex = getIndex(element, "row");
        columnStartIndex = getIndex(element, "columnStart", 0);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnStartIndex() {
        return columnStartIndex;
    }

}
