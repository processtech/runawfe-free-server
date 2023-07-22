package ru.runa.wfe.office.excel;

import org.dom4j.Element;
import ru.runa.wfe.var.VariableProvider;

public interface ExcelConstraints {

    public void configure(Element element);

    public void applyPlaceholders(VariableProvider variableProvider);

}
