package ru.runa.wfe.office.excel;

import org.apache.poi.ss.usermodel.Workbook;
import ru.runa.wfe.var.format.VariableFormat;

public abstract class ExcelStorable<C extends ExcelConstraints, Data> {
    protected C constraints;
    protected Data data;
    protected VariableFormat format;

    public C getConstraints() {
        return constraints;
    }

    public void setConstraints(C constraints) {
        this.constraints = constraints;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setFormat(VariableFormat format) {
        this.format = format;
    }

    public abstract void storeIn(Workbook workbook);

    public abstract void load(Workbook workbook);
}
