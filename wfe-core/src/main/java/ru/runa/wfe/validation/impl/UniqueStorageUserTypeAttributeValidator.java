package ru.runa.wfe.validation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

public class UniqueStorageUserTypeAttributeValidator extends FieldValidator {

    @Override
    public void validate() throws Exception {
        Object value = getFieldValue();
        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String fieldName = getFieldName();

        if (!fieldName.contains(UserType.DELIM)) {
            throw new InternalApplicationException("Validator should be attached to variable pointed at user type attribute " + fieldName);
        }

        int dotIndex = fieldName.indexOf(UserType.DELIM);
        String parentName = fieldName.substring(0, dotIndex);
        VariableDefinition parentVd = getVariableProvider().getParsedProcessDefinition().getVariable(parentName, false);

        if (parentVd == null || parentVd.getUserType() == null) {
            throw new InternalApplicationException("Parent variable should be a UserType for field: " + fieldName);
        }

        String tableName = parentVd.getUserType().getName();
        String columnName = fieldName.substring(dotIndex + 1);
        int columnIndex = resolveAttributeColumnIndex(parentVd, columnName);
        DataSource dataSource = DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);

        boolean exists = dataSource instanceof ExcelDataSource
                ? existsInExcelStorage((ExcelDataSource) dataSource, tableName, columnIndex, value)
                : existsInSqlStorage(tableName, columnName, value, dataSource);

        if (exists) {
            addError();
        }
    }

    private int resolveAttributeColumnIndex(VariableDefinition parentVd, String columnName) {
        List<VariableDefinition> attributes = parentVd.getUserType().getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            if (columnName.equals(attributes.get(i).getName())) {
                return i;
            }
        }
        throw new InternalApplicationException("Attribute '" + columnName + "' is not found in user type '" + parentVd.getUserType().getName());
    }

    private boolean existsInExcelStorage(ExcelDataSource excelDataSource, String tableName, int columnIndex, Object value) throws Exception {
        Path xlsxPath = Paths.get(excelDataSource.getFilePath(), tableName + ".xlsx");
        Path xlsPath = Paths.get(excelDataSource.getFilePath(), tableName + ".xls");
        Path storagePath = Files.exists(xlsxPath) ? xlsxPath : xlsPath;
        if (!Files.exists(storagePath)) {
            return false;
        }
        try (InputStream is = new FileInputStream(storagePath.toFile());
             Workbook workbook = WorkbookFactory.create(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                return false;
            }
            Sheet sheet = workbook.getSheetAt(0);
            String normalizedInputValue = normalize(value);
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    continue;
                }
                if (normalizedInputValue.equals(normalize(cell))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsInSqlStorage(String tableName, String columnName, Object value, DataSource dataSource) throws Exception {
        if (dataSource instanceof JdbcDataSource) {
            String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, columnName);
            try (Connection connection = ((JdbcDataSource) dataSource).getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, value);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1) > 0;
                    }
                }
            }
            return false;
        }
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = :val", tableName, columnName);
        Session session = ApplicationContextFactory.getSessionFactory().getCurrentSession();
        Number count = (Number) session.createSQLQuery(sql)
                .setParameter("val", value)
                .uniqueResult();
        return count != null && count.longValue() > 0;
    }

    private String normalize(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return normalize(cell.getStringCellValue());
            case NUMERIC:
                return normalize(BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString());
            case BOOLEAN:
                return normalize(Boolean.toString(cell.getBooleanCellValue()));
            case FORMULA:
                return normalize(cell.getCellFormula());
            default:
                return "";
        }
    }

    private String normalize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
        }
        return value.toString().trim();
    }
}
