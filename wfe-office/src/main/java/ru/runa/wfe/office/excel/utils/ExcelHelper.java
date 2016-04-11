package ru.runa.wfe.office.excel.utils;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Preconditions;

public class ExcelHelper {

    public static Sheet getSheet(Workbook workbook, String sheetName, int sheetIndex) {
        if (sheetName != null) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }
            return sheet;
        } else {
            return workbook.getSheetAt(sheetIndex);

        }
    }

    public static Row getRow(Sheet sheet, int rowIndex, boolean createIfLost) {
        Row row = sheet.getRow(rowIndex);
        if (row == null && createIfLost) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    public static Cell getCell(Row row, int columnIndex, boolean createIfLost) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null && createIfLost) {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }

    public static void setCellValue(Cell cell, Object value) {
        Preconditions.checkNotNull(value);
        if (value instanceof Date) {
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
            cell.setCellStyle(cellStyle);
            cell.setCellValue((Date) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    public static Object getCellValue(Cell cell, VariableFormat elementFormat) {
        Object value;
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                value = cell.getDateCellValue();
            } else {
                value = cell.getNumericCellValue();
            }
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            value = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_FORMULA:
            value = cell.getCellFormula();
            break;
        default:
            value = cell.getStringCellValue();
        }
        return TypeConversionUtil.convertTo(elementFormat.getJavaClass(), value);
    }

    public static boolean isCellEmptyOrNull(Cell cell) {
        if (cell == null) {
            return true;
        }
        return cell.getCellType() == Cell.CELL_TYPE_BLANK;
    }

}
