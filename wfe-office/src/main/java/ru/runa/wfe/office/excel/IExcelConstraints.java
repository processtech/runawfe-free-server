package ru.runa.wfe.office.excel;

import org.dom4j.Element;

import ru.runa.wfe.var.IVariableProvider;

public interface IExcelConstraints {

    public void configure(Element element);

    public void applyPlaceholders(IVariableProvider variableProvider);

}
