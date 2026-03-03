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

public class ListColumnExcelStorable extends ExcelStorable<ColumnConstraints, List<?>> {

    @Override
    public void load(Workbook workbook) {
        val list = new ArrayList<Object>();
        int rowIndex = constraints.getRowStartIndex();
        VariableFormat elementFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
        while (true) {
            Cell cell = getCell(workbook, rowIndex, false, constraints.getColumnIndex());
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            list.add(ExcelHelper.getCellValue(cell, elementFormat));
            rowIndex++;
        }
        setData(list);
    }

    @Override
    public void storeIn(Workbook workbook) {
        List<?> list = data;
        if (list == null) return;

        List<ColumnConstraints.ColumnMapping> mappings = constraints.getColumns();

        if (mappings != null && !mappings.isEmpty()) {
            int rowIndex = constraints.getRowStartIndex();
            for (Object item : list) {
                for (ColumnConstraints.ColumnMapping mapping : mappings) {
                    Object value = getNestedValue(item, mapping.attributeName);
                    Cell cell = getCell(workbook, rowIndex, true, mapping.column);
                    ExcelHelper.setCellValue(cell, value);
                }
                rowIndex++;
            }
        } else {
            int rowIndex = constraints.getRowStartIndex();
            VariableFormat elementFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
            for (Object object : list) {
                Cell cell = getCell(workbook, rowIndex, true, constraints.getColumnIndex());
                ExcelHelper.setCellValue(cell, elementFormat.format(object));
                rowIndex++;
            }
        }
    }

    private Object getNestedValue(Object obj, String path) {
        if (obj == null || path == null) return null;
        Map<String, Object> map = (obj instanceof Map) ? (Map<String, Object>) obj : TypeConversionUtil.convertTo(Map.class, obj);
        int dotIndex = path.indexOf('.');
        if (dotIndex != -1) {
            Object nextObj = map.get(path.substring(0, dotIndex));
            return getNestedValue(nextObj, path.substring(dotIndex + 1));
        }
        return map.get(path);
    }

    private Cell getCell(Workbook workbook, int rowIndex, boolean createIfLost, int columnIndex) {
        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, rowIndex, true);
        return ExcelHelper.getCell(row, columnIndex, createIfLost);
    }

    private Cell getCell(Workbook workbook, int rowIndex, boolean createIfLost) {
        return getCell(workbook, rowIndex, createIfLost, constraints.getColumnIndex());
    }
}
