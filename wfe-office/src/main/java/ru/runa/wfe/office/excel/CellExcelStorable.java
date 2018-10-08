package ru.runa.wfe.office.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.runa.wfe.office.excel.utils.ExcelHelper;

public class CellExcelStorable extends ExcelStorable<CellConstraints, Object> {

    @Override
    public void load(Workbook workbook) {
        setData(ExcelHelper.getCellValue(getCell(workbook), format));
    }

    @Override
    public void storeIn(Workbook workbook) {
        ExcelHelper.setCellValue(getCell(workbook), format.format(data));
    }

    private Cell getCell(Workbook workbook) {
        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, constraints.getRowIndex(), true);
        Cell cell = ExcelHelper.getCell(row, constraints.getColumnIndex(), true);
        return cell;
    }

}
