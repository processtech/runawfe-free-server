package ru.runa.wfe.office.excel;

import org.dom4j.Element;

public class AttributeConstraints extends OnSheetConstraints {
    private int columnIndex;

    @Override
    public void configure(Element element) {
        super.configure(element);
        columnIndex = getIndex(element, "column");
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}
