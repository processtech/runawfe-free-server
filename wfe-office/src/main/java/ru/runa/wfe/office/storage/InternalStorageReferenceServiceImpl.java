package ru.runa.wfe.office.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.office.excel.utils.ExcelHelper;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.logic.InternalStorageReferenceService;

@CommonsLog
public class InternalStorageReferenceServiceImpl implements InternalStorageReferenceService {

    private static final String XLSX_SUFFIX = ".xlsx";
    private static final String BY_REFERENCE_MARKER = "&";
    private static final Map<String, Object> LOCKS = new ConcurrentHashMap<>();

    @Override
    public UserTypeMap loadById(UserType userType, Long id) {
        if (id == null) {
            return null;
        }
        synchronized (getLock(userType)) {
            String filePath = getFilePath(userType);
            if (!new File(filePath).exists()) {
                log.warn("byReference: file not found: " + filePath);
                return null;
            }
            try (Workbook workbook = openWorkbook(filePath)) {
                Sheet sheet = workbook.getSheetAt(0);
                List<VariableDefinition> attributes = userType.getAttributes();
                int idColumnIndex = getIdColumnIndex(attributes);

                int dataRowCount = getDataRowCount(sheet, idColumnIndex);
                for (int rowIdx = 0; rowIdx < dataRowCount; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }
                    Long rowId = readIdFromRow(row, idColumnIndex, attributes.get(idColumnIndex));
                    if (id.equals(rowId)) {
                        return readRow(row, userType, attributes);
                    }
                }
                return null;
            } catch (IOException e) {
                throw new InternalApplicationException("byReference: error reading file " + filePath, e);
            }
        }
    }

    @Override
    public long insert(UserType userType, UserTypeMap value) {
        synchronized (getLock(userType)) {
            log.info("byReference INSERT: type=" + userType.getName() + ", value keys=" + value.keySet() + ", values=" + value);
            String filePath = getFilePath(userType);
            List<VariableDefinition> attributes = userType.getAttributes();
            int idColumnIndex = getIdColumnIndex(attributes);

            if (!new File(filePath).exists()) {
                ensureFileExists(filePath, userType.getName());
                try (Workbook workbook = openWorkbook(filePath)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    long newId = 1;
                    Row dataRow = ExcelHelper.getRow(sheet, 0, true);
                    value.put(ID_ATTRIBUTE_NAME, newId);
                    writeRow(dataRow, value, attributes);
                    writeNextIdRow(sheet, 1, idColumnIndex, newId + 1);
                    saveWorkbook(workbook, filePath);
                    log.debug("byReference: inserted id=" + newId + " for type " + userType.getName());
                    return newId;
                } catch (IOException e) {
                    throw new InternalApplicationException("byReference: error inserting into " + filePath, e);
                }
            }

            try (Workbook workbook = openWorkbook(filePath)) {
                Sheet sheet = workbook.getSheetAt(0);
                int totalRows = getTotalNonEmptyRowCount(sheet, idColumnIndex);

                long newId;
                if (totalRows == 0) {
                    newId = 1;
                } else {
                    Row tailRow = sheet.getRow(totalRows - 1);
                    Long nextId = readIdFromRow(tailRow, idColumnIndex, attributes.get(idColumnIndex));
                    if (nextId != null) {
                        newId = nextId;
                    } else {
                        newId = findMaxIdInDataRows(sheet, attributes, idColumnIndex, totalRows) + 1;
                    }
                }

                int dataInsertRowIdx;
                if (totalRows == 0) {
                    dataInsertRowIdx = 0;
                } else {
                    dataInsertRowIdx = totalRows - 1;
                    Row oldTailRow = sheet.getRow(dataInsertRowIdx);
                    if (oldTailRow != null) {
                        clearRow(oldTailRow, attributes.size());
                    }
                }

                Row dataRow = ExcelHelper.getRow(sheet, dataInsertRowIdx, true);
                value.put(ID_ATTRIBUTE_NAME, newId);
                writeRow(dataRow, value, attributes);

                writeNextIdRow(sheet, dataInsertRowIdx + 1, idColumnIndex, newId + 1);

                saveWorkbook(workbook, filePath);
                log.debug("byReference: inserted id=" + newId + " for type " + userType.getName());
                return newId;
            } catch (IOException e) {
                throw new InternalApplicationException("byReference: error inserting into " + filePath, e);
            }
        }
    }

    @Override
    public void update(UserType userType, Long id, UserTypeMap value) {
        if (id == null) {
            throw new InternalApplicationException("byReference: cannot update with null id for type " + userType.getName());
        }
        synchronized (getLock(userType)) {
            log.info("byReference UPDATE: type=" + userType.getName() + ", id=" + id + ", value keys=" + value.keySet() + ", values=" + value);
            String filePath = getFilePath(userType);
            if (!new File(filePath).exists()) {
                log.warn("byReference: file not found for update, will insert with id=" + id + ": " + filePath);
                insertWithId(userType, id, value);
                return;
            }
            try (Workbook workbook = openWorkbook(filePath)) {
                Sheet sheet = workbook.getSheetAt(0);
                List<VariableDefinition> attributes = userType.getAttributes();
                int idColumnIndex = getIdColumnIndex(attributes);

                int dataRowCount = getDataRowCount(sheet, idColumnIndex);
                boolean found = false;
                for (int rowIdx = 0; rowIdx < dataRowCount; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }
                    Long rowId = readIdFromRow(row, idColumnIndex, attributes.get(idColumnIndex));
                    if (id.equals(rowId)) {
                        value.put(ID_ATTRIBUTE_NAME, id);
                        writeRow(row, value, attributes, true);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.warn("byReference: row with id=" + id + " not found in " + filePath
                            + " for type " + userType.getName()
                            + ". Will insert record with this id.");
                    workbook.close();
                    insertWithId(userType, id, value);
                    return;
                }
                saveWorkbook(workbook, filePath);
                log.debug("byReference: updated id=" + id + " for type " + userType.getName());
            } catch (IOException e) {
                throw new InternalApplicationException("byReference: error updating " + filePath, e);
            }
        }
    }

    private void insertWithId(UserType userType, long id, UserTypeMap value) {
        String filePath = getFilePath(userType);
        List<VariableDefinition> attributes = userType.getAttributes();
        int idColumnIndex = getIdColumnIndex(attributes);

        ensureFileExists(filePath, userType.getName());
        try (Workbook workbook = openWorkbook(filePath)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = getTotalNonEmptyRowCount(sheet, idColumnIndex);

            long currentNextId;
            if (totalRows == 0) {
                currentNextId = 1;
            } else {
                Row tailRow = sheet.getRow(totalRows - 1);
                Long tailId = readIdFromRow(tailRow, idColumnIndex, attributes.get(idColumnIndex));
                currentNextId = (tailId != null) ? tailId : findMaxIdInDataRows(sheet, attributes, idColumnIndex, totalRows) + 1;
            }

            int dataInsertRowIdx = (totalRows > 0) ? totalRows - 1 : 0;
            if (totalRows > 0) {
                Row oldTailRow = sheet.getRow(dataInsertRowIdx);
                if (oldTailRow != null) {
                    clearRow(oldTailRow, attributes.size());
                }
            }

            Row dataRow = ExcelHelper.getRow(sheet, dataInsertRowIdx, true);
            value.put(ID_ATTRIBUTE_NAME, id);
            writeRow(dataRow, value, attributes);

            long newNextId = Math.max(currentNextId, id + 1);
            writeNextIdRow(sheet, dataInsertRowIdx + 1, idColumnIndex, newNextId);

            saveWorkbook(workbook, filePath);
            log.info("byReference: insertWithId id=" + id + " for type " + userType.getName() + ", nextId=" + newNextId);
        } catch (IOException e) {
            throw new InternalApplicationException("byReference: error in insertWithId for " + filePath, e);
        }
    }

    @Override
    public void delete(UserType userType, Long id) {
        if (id == null) {
            return;
        }
        synchronized (getLock(userType)) {
            String filePath = getFilePath(userType);
            if (!new File(filePath).exists()) {
                return;
            }
            try (Workbook workbook = openWorkbook(filePath)) {
                Sheet sheet = workbook.getSheetAt(0);
                List<VariableDefinition> attributes = userType.getAttributes();
                int idColumnIndex = getIdColumnIndex(attributes);

                int dataRowCount = getDataRowCount(sheet, idColumnIndex);
                int totalRows = getTotalNonEmptyRowCount(sheet, idColumnIndex);
                long nextId = readNextIdFromTail(sheet, idColumnIndex, attributes, totalRows);

                for (int rowIdx = 0; rowIdx < dataRowCount; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }
                    Long rowId = readIdFromRow(row, idColumnIndex, attributes.get(idColumnIndex));
                    if (id.equals(rowId)) {
                        clearRow(row, attributes.size());
                        int lastRowToShift = (totalRows > dataRowCount) ? totalRows - 1 : dataRowCount - 1;
                        if (rowIdx < lastRowToShift) {
                            sheet.shiftRows(rowIdx + 1, lastRowToShift, -1);
                        }
                        int newDataRowCount = dataRowCount - 1;

                        Row oldRow = sheet.getRow(newDataRowCount);
                        if (oldRow != null) {
                            clearRow(oldRow, attributes.size());
                        }
                        writeNextIdRow(sheet, newDataRowCount, idColumnIndex, nextId);
                        if (newDataRowCount + 1 <= lastRowToShift) {
                            Row residualRow = sheet.getRow(newDataRowCount + 1);
                            if (residualRow != null) {
                                clearRow(residualRow, attributes.size());
                            }
                        }
                        break;
                    }
                }
                saveWorkbook(workbook, filePath);
                log.debug("byReference: deleted id=" + id + " for type " + userType.getName());
            } catch (IOException e) {
                throw new InternalApplicationException("byReference: error deleting from " + filePath, e);
            }
        }
    }

    private Object getLock(UserType userType) {
        return LOCKS.computeIfAbsent(userType.getName(), k -> new Object());
    }

    private String getFilePath(UserType userType) {
        ru.runa.wfe.datasource.DataSource ds = DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
        if (!(ds instanceof ExcelDataSource)) {
            throw new InternalApplicationException("byReference: InternalStorage datasource is not ExcelDataSource");
        }
        return ((ExcelDataSource) ds).getFilePath() + File.separator + userType.getName() + BY_REFERENCE_MARKER + XLSX_SUFFIX;
    }

    private int getIdColumnIndex(List<VariableDefinition> attributes) {
        for (int i = 0; i < attributes.size(); i++) {
            if (ID_ATTRIBUTE_NAME.equals(attributes.get(i).getName())) {
                return i;
            }
        }
        throw new InternalApplicationException("byReference: attribute '" + ID_ATTRIBUTE_NAME + "' not found in user type");
    }

    private Long readIdFromRow(Row row, int idColumnIndex, VariableDefinition idAttrDef) {
        Cell cell = ExcelHelper.getCell(row, idColumnIndex, false);
        if (cell == null || ExcelHelper.isCellEmptyOrNull(cell)) {
            return null;
        }
        VariableFormat format = FormatCommons.create(idAttrDef);
        Object value = ExcelHelper.getCellValue(cell, format);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            log.warn("byReference: cannot parse id value '" + value + "' as Long");
            return null;
        }
    }

    private UserTypeMap readRow(Row row, UserType userType, List<VariableDefinition> attributes) {
        UserTypeMap map = new UserTypeMap(userType);
        for (int colIdx = 0; colIdx < attributes.size(); colIdx++) {
            VariableDefinition attrDef = attributes.get(colIdx);
            Cell cell = ExcelHelper.getCell(row, colIdx, false);
            if (cell != null && !ExcelHelper.isCellEmptyOrNull(cell)) {
                VariableFormat format = FormatCommons.create(attrDef);
                Object value = ExcelHelper.getCellValue(cell, format);
                map.put(attrDef.getName(), value);
            }
        }
        return map;
    }

    private void writeRow(Row row, UserTypeMap value, List<VariableDefinition> attributes, boolean merge) {
        for (int colIdx = 0; colIdx < attributes.size(); colIdx++) {
            VariableDefinition attrDef = attributes.get(colIdx);
            Object attrValue = value.get(attrDef.getName());
            if (attrValue != null) {
                if (ID_ATTRIBUTE_NAME.equals(attrDef.getName()) && attrValue instanceof Number) {
                    Cell cell = ExcelHelper.getCell(row, colIdx, true);
                    cell.setCellValue(((Number) attrValue).doubleValue());
                    continue;
                }
                VariableFormat format = FormatCommons.create(attrDef);
                String formatted = format.format(attrValue);
                if (formatted != null && !formatted.isEmpty()) {
                    Cell cell = ExcelHelper.getCell(row, colIdx, true);
                    ExcelHelper.setCellValue(cell, formatted);
                } else {
                    Cell cell = ExcelHelper.getCell(row, colIdx, false);
                    if (cell != null) {
                        row.removeCell(cell);
                    }
                }
            } else if (!merge) {
                Cell cell = ExcelHelper.getCell(row, colIdx, false);
                if (cell != null) {
                    row.removeCell(cell);
                }
            }
        }
    }

    private void writeRow(Row row, UserTypeMap value, List<VariableDefinition> attributes) {
        writeRow(row, value, attributes, false);
    }

    private void clearRow(Row row, int columnCount) {
        for (int colIdx = 0; colIdx < columnCount; colIdx++) {
            Cell cell = ExcelHelper.getCell(row, colIdx, false);
            if (cell != null) {
                row.removeCell(cell);
            }
        }
    }

    private long findMaxIdInDataRows(Sheet sheet, List<VariableDefinition> attributes, int idColumnIndex, int totalRows) {
        long maxId = 0;
        int dataRowCount = getDataRowCountFromTotal(totalRows);
        VariableDefinition idAttrDef = attributes.get(idColumnIndex);
        for (int rowIdx = 0; rowIdx < dataRowCount; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue;
            }
            Long rowId = readIdFromRow(row, idColumnIndex, idAttrDef);
            if (rowId != null && rowId > maxId) {
                maxId = rowId;
            }
        }
        return maxId;
    }

    private int getDataRowCount(Sheet sheet, int idColumnIndex) {
        int totalRows = getTotalNonEmptyRowCount(sheet, idColumnIndex);
        return getDataRowCountFromTotal(totalRows);
    }

    private int getDataRowCountFromTotal(int totalRows) {
        if (totalRows == 0) {
            return 0;
        }
        return totalRows - 1;
    }

    private int getTotalNonEmptyRowCount(Sheet sheet, int idColumnIndex) {
        int count = 0;
        while (true) {
            Row row = sheet.getRow(count);
            if (row == null) {
                break;
            }
            Cell idCell = ExcelHelper.getCell(row, idColumnIndex, false);
            if (idCell == null || ExcelHelper.isCellEmptyOrNull(idCell)) {
                break;
            }
            count++;
        }
        return count;
    }

    private void writeNextIdRow(Sheet sheet, int rowIndex, int idColumnIndex, long nextId) {
        Row tailRow = ExcelHelper.getRow(sheet, rowIndex, true);
        Cell idCell = ExcelHelper.getCell(tailRow, idColumnIndex, true);
        idCell.setCellValue((double) nextId);
    }

    private long readNextIdFromTail(Sheet sheet, int idColumnIndex, List<VariableDefinition> attributes, int totalRows) {
        if (totalRows == 0) {
            return 1;
        }
        Row tailRow = sheet.getRow(totalRows - 1);
        if (tailRow != null) {
            Long nextId = readIdFromRow(tailRow, idColumnIndex, attributes.get(idColumnIndex));
            if (nextId != null) {
                return nextId;
            }
        }
        return findMaxIdInDataRows(sheet, attributes, idColumnIndex, totalRows) + 1;
    }

    private Workbook openWorkbook(String filePath) throws IOException {
        try (InputStream is = new FileInputStream(filePath)) {
            return new XSSFWorkbook(is);
        }
    }

    private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (OutputStream os = new FileOutputStream(filePath)) {
            workbook.write(os);
        }
    }

    private void ensureFileExists(String filePath, String sheetName) {
        File f = new File(filePath);
        if (f.exists()) {
            log.debug("byReference: file already exists: " + filePath);
            return;
        }
        log.info("byReference: CREATING new file: " + filePath + ", sheet=" + sheetName);
        File parentDir = f.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new InternalApplicationException("byReference: cannot create directory " + parentDir.getAbsolutePath());
            }
        }
        try (Workbook workbook = new XSSFWorkbook(); OutputStream os = new FileOutputStream(filePath)) {
            workbook.createSheet(sheetName);
            workbook.write(os);
        } catch (IOException e) {
            throw new InternalApplicationException("byReference: cannot create file " + filePath, e);
        }
    }
}
