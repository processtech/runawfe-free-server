package ru.runa.wfe.office.excel;

import org.dom4j.Element;
import ru.runa.wfe.var.VariableProvider;

public interface ExcelConstraints {

    void configure(Element element);

    void applyPlaceholders(VariableProvider variableProvider);
}
