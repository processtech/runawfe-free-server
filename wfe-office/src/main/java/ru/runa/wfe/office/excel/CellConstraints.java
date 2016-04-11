package ru.runa.wfe.office.excel;

import org.dom4j.Element;

public class CellConstraints extends OnSheetConstraints {
    private int rowIndex;
    private int columnIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        rowIndex = getIndex(element, "row");
        columnIndex = getIndex(element, "column");
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

}
