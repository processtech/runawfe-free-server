package ru.runa.wfe.office.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.office.excel.AttributeConstraints;
import ru.runa.wfe.office.excel.IExcelConstraints;
import ru.runa.wfe.office.excel.utils.ExcelHelper;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.ParamBasedVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class StoreServiceImpl implements StoreService {

    private static final int START_ROW_INDEX = 0;

    private static final Log log = LogFactory.getLog(StoreServiceImpl.class);

    private IExcelConstraints constraints;
    private VariableFormat format;
    private String fullPath;
    IVariableProvider variableProvider;

    public StoreServiceImpl(IVariableProvider variableProvider) {
        this.variableProvider = variableProvider;
    }

    @Override
    public void createFileIfNotExist(String path) throws Exception {
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return;
        }
        Workbook workbook = null;
        if (path.endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
        workbook.createSheet();
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            workbook.write(os);
        } catch (Exception e) {
            log.error("", e);
            Throwables.propagate(e);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public ExecutionResult findByFilter(Properties properties, WfVariable variable, String condition) throws Exception {
        if (!existOutputParamByVariableName(variable)) {
            return ExecutionResult.EMPTY;
        }
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        return new ExecutionResult(find(wb, constraints, format, condition));
    }

    @Override
    public void update(Properties properties, WfVariable variable, String condition) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        update(wb, constraints, variable.getValue(), format, condition, false);
        OutputStream os = null;
        try {
            os = new FileOutputStream(fullPath);
            wb.write(os);
        } catch (IOException e) {
            log.error("", e);
            throw new BlockedFileException();
        } finally {
            os.close();
        }
    }

    @Override
    public void delete(Properties properties, WfVariable variable, String condition) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        update(wb, constraints, variable.getValue(), format, condition, true);
        OutputStream os = null;
        try {
            os = new FileOutputStream(fullPath);
            wb.write(os);
        } catch (IOException e) {
            log.error("", e);
            throw new BlockedFileException();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        save(wb, constraints, format, variable, appendTo);
        OutputStream os = null;
        try {
            os = new FileOutputStream(fullPath);
            wb.write(os);
        } catch (IOException e) {
            log.error("", e);
            throw new BlockedFileException();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void initParams(Properties properties) throws Exception {
        Preconditions.checkNotNull(properties);
        constraints = (IExcelConstraints) properties.get(PROP_CONSTRAINTS);
        format = (VariableFormat) properties.get(PROP_FORMAT);
        fullPath = properties.getProperty(PROP_PATH);
        if (fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE) || fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
            String dsName = null;
            if (fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                dsName = fullPath.substring(DataSourceStuff.PATH_PREFIX_DATA_SOURCE.length());
            } else {
                dsName = (String) variableProvider.getValueNotNull(fullPath.substring(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE.length()));
            }
            DataSource ds = DataSourceStorage.getDataSource(dsName);
            if (ds instanceof ExcelDataSource) {
                ExcelDataSource eds = (ExcelDataSource) ds;
                fullPath = eds.getFilePath() + "/" + eds.getFileName();
            }
        }
        createFileIfNotExist(fullPath);
    }

    @SuppressWarnings("unchecked")
    private void update(Workbook workbook, IExcelConstraints constraints, Object variable, VariableFormat variableFormat, String condition,
            boolean clear) {
        List list = findAll(workbook, constraints, variableFormat);
        boolean changed = false;
        if (Strings.isNullOrEmpty(condition)) {
            int i = 0;
            for (Object object : list) {
                changeVariable(constraints, variable, clear, list, i, object);
                i++;
            }
            changed = true;
        } else {
            int i = 0;
            for (Object object : list) {
                if (variableFormat instanceof UserTypeFormat) {
                    if (ConditionProcessor.filter(condition, (Map<String, Object>) object, variableProvider)) {
                        changeVariable(constraints, variable, clear, list, i, object);
                        changed = true;
                    }
                }
                i++;
            }
        }
        if (changed) {
            save(workbook, constraints, variableFormat, list, false);
        }
    }

    @SuppressWarnings("unchecked")
    private void changeVariable(IExcelConstraints constraints, Object variable, boolean clear, List list, int i, Object object) {
        if (clear) {
            list.set(i, null);
            return;
        }
        if (variable instanceof UserTypeMap) {
            UserTypeMap sourceMap = (UserTypeMap) variable;
            UserType userType = sourceMap.getUserType();
            List<VariableDefinition> sourceAttributes = userType.getAttributes();

            UserTypeMap targetMap = (UserTypeMap) object;

            for (VariableDefinition sourceAttribute : sourceAttributes) {
                targetMap.put(sourceAttribute.getName(), sourceMap.get(sourceAttribute.getName()));
            }
            list.set(i, targetMap);
        } else {
            UserTypeMap targetMap = (UserTypeMap) object;

            UserType userType = targetMap.getUserType();
            List<VariableDefinition> targetAttributes = userType.getAttributes();

            int columnIndex = 0;
            if (constraints instanceof AttributeConstraints) {
                columnIndex = ((AttributeConstraints) constraints).getColumnIndex();
            }
            int indexAttribute = 0;
            for (VariableDefinition targetAttribute : targetAttributes) {
                if (indexAttribute == columnIndex) {
                    targetMap.put(targetAttribute.getName(), variable);
                    break;
                }
                indexAttribute++;
            }
            list.set(i, targetMap);
        }
    }

    private void save(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, WfVariable variable, boolean appendTo) {
        VariableFormat format = getVariableFormat(variableFormat);
        if (constraints instanceof AttributeConstraints) {
            fillResultToCell(workbook, constraints, format, variable.getValue(), appendTo);
        }
    }

    @SuppressWarnings("rawtypes")
    private void save(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List records, boolean append) {
        VariableFormat format = getVariableFormat(variableFormat);
        if (constraints instanceof AttributeConstraints) {
            fillResultToCell(workbook, constraints, format, records, append);
        }
    }

    @SuppressWarnings("resource")
    private Workbook getWorkbook(String fullPath) throws IOException, FileNotFoundException {
        Workbook wb = null;
        InputStream is = new FileInputStream(fullPath);
        if (fullPath.endsWith(".xls")) {
            wb = new HSSFWorkbook(is);
        } else if (fullPath.endsWith(".xlsx")) {
            wb = new XSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("excel file extension is incorrect!");
        }
        is.close();
        return wb;
    }

    @SuppressWarnings("rawtypes")
    private List find(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, String condition) {
        boolean all = Strings.isNullOrEmpty(condition);
        List result = findAll(workbook, constraints, variableFormat);
        if (!all) {
            return filter(result, condition);
        }
        return result;
    }

    private List filter(List records, String condition) {
        List filtered = Lists.newArrayList();
        for (Object object : records) {
            boolean conditionResult = false;
            if (object instanceof UserTypeMap) {
                conditionResult = ConditionProcessor.filter(condition, (UserTypeMap) object, variableProvider);
                if (!conditionResult) {
                    continue;
                }
            } else {
                // TODO need implement filter for non user type variables
            }
            if (conditionResult) {
                filtered.add(object);
            }
        }
        return filtered;
    }

    private List<?> findAll(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat) {
        List<?> result = Lists.newArrayList();
        VariableFormat format = getVariableFormat(variableFormat);
        if (constraints instanceof AttributeConstraints) {
            fillResultFromCell(workbook, constraints, format, result);
        }
        return result;
    }

    private int getLastColumnIndex(Sheet sheet, int startColumnIndex, int rowIndex) {
        while (true) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, startColumnIndex, true);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            startColumnIndex++;
        }
        return startColumnIndex;
    }

    private int getLastRowIndex(Sheet sheet, int columnIndex, VariableFormat variableFormat) {
        int startRowIndex = START_ROW_INDEX;
        boolean isUserTypeVariable = variableFormat instanceof UserTypeFormat;
        List<VariableDefinition> attributes = null;
        if (isUserTypeVariable) {
            attributes = ((UserTypeFormat) variableFormat).getUserType().getAttributes();
        }
        while (true) {
            Row row = ExcelHelper.getRow(sheet, startRowIndex, true);
            if (isUserTypeVariable) {
                int colIndex = columnIndex;
                int emptyCount = 0;
                for (int i = 0; i < attributes.size(); i++) {
                    Cell cell = ExcelHelper.getCell(row, colIndex, true);
                    if (ExcelHelper.isCellEmptyOrNull(cell)) {
                        emptyCount++;
                    }
                    colIndex++;
                }
                if (emptyCount == attributes.size()) {
                    break;
                }
            } else {
                Cell cell = ExcelHelper.getCell(row, columnIndex, true);
                if (ExcelHelper.isCellEmptyOrNull(cell)) {
                    break;
                }
            }
            startRowIndex++;
        }
        return startRowIndex;
    }

    private void fillResultFromCell(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List result) {
        AttributeConstraints attributeConstraints = (AttributeConstraints) constraints;
        int columnIndex = 0;// attributeConstraints.getColumnIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, attributeConstraints.getSheetName(), attributeConstraints.getSheetIndex());
        int rowIndex = getLastRowIndex(sheet, columnIndex, variableFormat);
        int currentRow = START_ROW_INDEX;
        while (currentRow < rowIndex) {
            Row row = ExcelHelper.getRow(sheet, currentRow, true);
            if (variableFormat instanceof UserTypeFormat) {
                UserType userType = ((UserTypeFormat) variableFormat).getUserType();
                List<VariableDefinition> attributes = userType.getAttributes();
                int colIndex = columnIndex;
                UserTypeMap userTypeMap = new UserTypeMap(userType);
                for (VariableDefinition variableDefinition : attributes) {
                    Cell cell = ExcelHelper.getCell(row, colIndex, true);
                    Object attributeValue = ExcelHelper.getCellValue(cell, variableDefinition.getFormatNotNull());
                    userTypeMap.put(variableDefinition.getName(), attributeValue);
                    colIndex++;
                }
                result.add(userTypeMap);
            } else {
                Cell cell = ExcelHelper.getCell(row, columnIndex, true);
                if (ExcelHelper.isCellEmptyOrNull(cell)) {
                    break;
                }
                result.add(ExcelHelper.getCellValue(cell, format));
            }
            currentRow++;
        }
    }

    @SuppressWarnings("rawtypes")
    private void fillResultToCell(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, Object result, boolean append) {
        AttributeConstraints attributeConstraints = (AttributeConstraints) constraints;
        int columnIndex = attributeConstraints.getColumnIndex();
        int rowIndex = START_ROW_INDEX;
        Sheet sheet = ExcelHelper.getSheet(workbook, attributeConstraints.getSheetName(), attributeConstraints.getSheetIndex());
        if (!append) {
            if (result instanceof List) {
                Object object = ((List) result).get(0);
                if (object instanceof UserTypeMap) {
                    int size = ((UserTypeMap) object).getUserType().getAttributes().size();
                    clearSheet(sheet, columnIndex, size, getLastRowIndex(sheet, columnIndex, variableFormat));
                } else {
                    clearSheet(sheet, columnIndex, getLastColumnIndex(sheet, columnIndex, rowIndex),
                            getLastRowIndex(sheet, columnIndex, variableFormat));
                }
            }
        } else {
            rowIndex = getLastRowIndex(sheet, columnIndex, variableFormat);
        }
        if (result instanceof List) {
            for (Object obj : (List) result) {
                if (obj == null) {
                    continue;
                }
                setVariableToCell(variableFormat, obj, columnIndex, rowIndex, sheet);
                rowIndex++;
            }
        } else {
            setVariableToCell(variableFormat, result, columnIndex, rowIndex, sheet);
        }
    }

    private void clearSheet(Sheet sheet, int startColumn, int endColumn, int lastRow) {
        int rowIndex = START_ROW_INDEX;
        while (rowIndex < lastRow) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            int column = startColumn;
            while (column <= endColumn) {
                Cell cell = ExcelHelper.getCell(row, column, true);
                cell.setCellValue((String) null);
                column++;
            }
            rowIndex++;
        }
    }

    private void setVariableToCell(VariableFormat variableFormat, Object result, int columnIndex, int rowIndex, Sheet sheet) {
        Row row = ExcelHelper.getRow(sheet, rowIndex, true);
        if (result instanceof UserTypeMap) {
            UserTypeMap userTypeMap = (UserTypeMap) result;
            UserType userType = userTypeMap.getUserType();
            List<VariableDefinition> attributes = userType.getAttributes();
            int colIndex = columnIndex;
            for (VariableDefinition variableDefinition : attributes) {
                Cell cell = ExcelHelper.getCell(row, colIndex, true);
                VariableFormat cellFormat = variableDefinition.getFormatNotNull();
                String val = cellFormat.format(userTypeMap.get(variableDefinition.getName()));
                ExcelHelper.setCellValue(cell, val);
                colIndex++;
            }
        } else {
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (result == null) {
                cell.setCellValue((String) null);
            } else {
                ExcelHelper.setCellValue(cell, variableFormat.format(result));
            }
        }
    }

    private VariableFormat getVariableFormat(VariableFormat variableFormat) {
        VariableFormat format = null;
        if (variableFormat instanceof ListFormat) {
            format = FormatCommons.createComponent((VariableFormatContainer) variableFormat, 0);
        } else {
            format = variableFormat;
        }
        return format;
    }

    private boolean existOutputParamByVariableName(WfVariable variable) {
        Preconditions.checkNotNull(variable);
        if (variableProvider instanceof ParamBasedVariableProvider) {
            ParamsDef paramsDef = ((ParamBasedVariableProvider) variableProvider).getParamsDef();
            if (paramsDef != null) {
                Map<String, ParamDef> outputParams = paramsDef.getOutputParams();
                if (outputParams != null) {
                    for (Entry<String, ParamDef> entry : outputParams.entrySet()) {
                        String variableName = entry.getValue().getVariableName();
                        if (variable.getDefinition().getName().equals(variableName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
