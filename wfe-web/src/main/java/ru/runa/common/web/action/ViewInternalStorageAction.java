package ru.runa.common.web.action;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.ViewInternalStorageForm;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;

/**
 * @struts:action path="/viewInternalStorage" name="viewInternalStorageForm" validate="false"
 * @struts.action-forward name="success" path="/displayInternalStorage.do" redirect = "false"
 */
public class ViewInternalStorageAction extends ActionBase {

    public static final String ACTION_PATH = "/viewInternalStorage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            ViewInternalStorageForm form = (ViewInternalStorageForm) actionForm;
            String workbookPath = null;
            DataSource ds = DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
            if (ds instanceof ExcelDataSource) {
                ExcelDataSource eds = (ExcelDataSource) ds;
                workbookPath = eds.getFilePath() + "/" + eds.getFileName();
                request.setAttribute("workbookPath", workbookPath);
                if (form.getMode() == ViewInternalStorageForm.MODE_DOWNLOAD) {
                    String encodedFileName = HTMLUtils.encodeFileName(request, eds.getFileName());
                    response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
                    OutputStream os = response.getOutputStream();
                    Files.copy(new File(form.getWorkbookPath()), os);
                    os.flush();
                    return null;
                }
            }
            form.setWorkbookPath(workbookPath);
            if (!Strings.isNullOrEmpty(workbookPath) && !Strings.isNullOrEmpty(form.getSheetName())) {
                try (InputStream is = new FileInputStream(workbookPath)) {
                    Workbook wb = null;
                    if (workbookPath.endsWith(".xls")) {
                        wb = new HSSFWorkbook(is);
                    } else if (workbookPath.endsWith(".xlsx")) {
                        wb = new XSSFWorkbook(is);
                    } else {
                        throw new IllegalArgumentException("excel file extension is incorrect");
                    }
                    Sheet sheet = wb.getSheet(form.getSheetName());
                    List<List<Cell>> data = new ArrayList<>();
                    int columnNumber = getSheetContent(sheet, data);
                    StringBuffer sheetContent = new StringBuffer();
                    if (columnNumber > 0 && data.size() > 0) {
                        sheetContent.append("<table class=\"list\">");
                        for (List<Cell> row : data) {
                            sheetContent.append("<tr class>");
                            for (Cell cell : row) {
                                sheetContent.append("<td class=\"list\">");
                                sheetContent.append(cellValue(cell));
                                sheetContent.append("</td>");
                            }
                            sheetContent.append("</tr>");
                        }
                        sheetContent.append("</table>");
                    } else {
                        sheetContent.append("Empty sheet");

                    }
                    request.setAttribute("sheetContent", sheetContent.toString());
                    wb.close();
                }
            }
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }

    private Object cellValue(Cell cell) {
        Object value;
        switch (cell.getCellTypeEnum()) {
        case STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                value = cell.getDateCellValue();
            } else {
                value = cell.getNumericCellValue();
            }
            break;
        case BOOLEAN:
            value = cell.getBooleanCellValue();
            break;
        case FORMULA:
            value = cell.getCellFormula();
            break;
        default:
            value = cell.getStringCellValue();
        }
        return value;
    }

    private int getSheetContent(Sheet sheet, List<List<Cell>> data) {
        int columnNumber = 0;
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            List<Cell> cells = new ArrayList<>();
            Row row = sheet.getRow(r);
            for (int c = 0; c < row.getLastCellNum(); c++) {
                cells.add(row.getCell(c));
            }
            data.add(cells);
            columnNumber = Math.max(columnNumber, cells.size());
        }
        return columnNumber;
    }

}
