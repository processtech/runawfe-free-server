package ru.runa.wfe.office.excel.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.IFileVariable;

/**
 * @author egorlitvinenko
 */
public class ExcelFormulaHandler extends CommonParamBasedHandler {
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_OUTPUT_FILE = "outputFile";

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        final IFileVariable fileVariable = handlerData.getInputParamValueNotNull(PARAM_SOURCE);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileVariable.getData());
        boolean xlsx = ExcelBindings.isFileNameBelongsToXLSX(fileVariable.getName(), true);
        final Workbook workbook = xlsx ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
        final FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateAll();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        final FileVariable resultFileVariable = new FileVariable(fileVariable.getName(), outputStream.toByteArray(), fileVariable.getContentType());
        handlerData.setOutputParam(PARAM_OUTPUT_FILE, resultFileVariable);
    }
}
