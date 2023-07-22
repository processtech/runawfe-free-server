package ru.runa.wfe.office.excel;

import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.runa.wfe.office.excel.utils.ExcelHelper;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class ListRowExcelStorable extends ExcelStorable<RowConstraints, List<?>> {

    @Override
    public void load(Workbook workbook) {
        List<Object> list = new ArrayList<Object>();
        Row row = getRow(workbook);
        int columnIndex = constraints.getColumnStartIndex();
        VariableFormat elementFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
        while (true) {
            Cell cell = ExcelHelper.getCell(row, columnIndex, false);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            list.add(ExcelHelper.getCellValue(cell, elementFormat));
            columnIndex++;
        }
        setData(list);
    }

    @Override
    public void storeIn(Workbook workbook) {
        Row row = getRow(workbook);
        List<?> list = data;
        int columnIndex = constraints.getColumnStartIndex();
        VariableFormat elementFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
        for (Object object : list) {
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            ExcelHelper.setCellValue(cell, elementFormat.format(object));
            columnIndex++;
        }
    }

    private Row getRow(Workbook workbook) {
        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, constraints.getRowIndex(), true);
        return row;
    }

}
