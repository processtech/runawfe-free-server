package ru.runa.wfe.office.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.office.excel.utils.ExcelHelper;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class ListRowExcelStorable extends ExcelStorable<RowConstraints, List<?>> {

    @Override
    public void load(Workbook workbook) {
        val list = new ArrayList<Object>();
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
        List<?> list = data;
        if (list == null) {
            return;
        }

        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        List<RowConstraints.ColumnMapping> mappings = constraints.getColumns();

        if (mappings != null && !mappings.isEmpty()) {
            int currentColumnOffset = 0;

            for (Object item : list) {
                int currentColumnIndex = constraints.getColumnStartIndex() + currentColumnOffset;
                for (RowConstraints.ColumnMapping mapping : mappings) {
                    Object value = getNestedValue(item, mapping.attributeName);
                    Row targetRow = ExcelHelper.getRow(sheet, mapping.column, true);
                    Cell cell = ExcelHelper.getCell(targetRow, currentColumnIndex, true);

                    ExcelHelper.setCellValue(cell, value);
                }
                currentColumnOffset++;
            }
        } else {
            Row row = getRow(workbook);
            int columnIndex = constraints.getColumnStartIndex();
            VariableFormat elementFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
            for (Object object : list) {
                Cell cell = ExcelHelper.getCell(row, columnIndex, true);
                ExcelHelper.setCellValue(cell, elementFormat.format(object));
                columnIndex++;
            }
        }
    }

    private Object getNestedValue(Object obj, String path) {
        if (obj == null || path == null){
            return null;
        }
        Map<String, Object> map = (obj instanceof Map) ? (Map<String, Object>) obj : TypeConversionUtil.convertTo(Map.class, obj);
        int dotIndex = path.indexOf('.');
        if (dotIndex != -1) {
            Object nextObj = map.get(path.substring(0, dotIndex));
            return getNestedValue(nextObj, path.substring(dotIndex + 1));
        }
        return map.get(path);
    }

    private Row getRow(Workbook workbook) {
        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        return ExcelHelper.getRow(sheet, constraints.getRowIndex(), true);
    }
}