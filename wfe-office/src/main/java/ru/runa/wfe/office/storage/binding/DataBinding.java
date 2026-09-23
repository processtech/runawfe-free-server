package ru.runa.wfe.office.storage.binding;

import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.office.excel.ExcelConstraints;

@Getter
@Setter
public class DataBinding {
    private ExcelConstraints constraints;
    private String variableName;
    private String condition;
    private QueryType queryType;
    private QueryRole queryRole;
    private LogicalOperator logicalOperator;
}
