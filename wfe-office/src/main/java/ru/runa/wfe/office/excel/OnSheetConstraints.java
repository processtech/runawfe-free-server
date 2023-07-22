package ru.runa.wfe.office.excel;

import org.dom4j.Element;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.VariableProvider;

public class OnSheetConstraints implements ExcelConstraints {
    private int sheetIndex;
    private String sheetName;

    @Override
    public void configure(Element element) {
        sheetName = element.attributeValue("sheetName");
        if (sheetName == null) {
            sheetIndex = getIndex(element, "sheet");
        }
    }

    @Override
    public void applyPlaceholders(VariableProvider variableProvider) {
        if (sheetName != null) {
            sheetName = ExpressionEvaluator.substitute(sheetName, variableProvider);
        }
    }

    protected int getIndex(Element element, String attributeName) {
        int index = getIndex(element, attributeName, -1);
        if (index == -1) {
            throw new RuntimeException("No required attribute " + attributeName + " is defined in " + element.getName());
        }
        return index;
    }

    protected int getIndex(Element element, String attributeName, int defaultValue) {
        String string = element.attributeValue(attributeName);
        if (string == null) {
            return defaultValue;
        }
        int number = Integer.parseInt(string);
        if (number == 0) {
            throw new RuntimeException("Since v4.1.0 indexes start with 1.");
        }
        if (number < 0) {
            throw new RuntimeException("Negative indexes do not allowed.");
        }
        number--;
        return number;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

}
