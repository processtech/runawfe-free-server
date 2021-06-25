package ru.runa.wfe.office.doc;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import ru.runa.wfe.office.OfficeProperties;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

public class DocxFileChanger {
    private final DocxConfig config;
    private final MapDelegableVariableProvider variableProvider;
    private final XWPFDocument document;

    public DocxFileChanger(DocxConfig config, VariableProvider variableProvider, InputStream templateInputStream) throws IOException {
        this.config = config;
        this.variableProvider = new MapDelegableVariableProvider(new HashMap<String, Object>(), variableProvider);
        this.document = new XWPFDocument(templateInputStream);
    }

    public XWPFDocument changeAll() {
        for (XWPFHeader header : document.getHeaderList()) {
            changeBodyElements(header.getBodyElements());
        }
        changeBodyElements(document.getBodyElements());
        for (XWPFFooter footer : document.getFooterList()) {
            changeBodyElements(footer.getBodyElements());
        }
        return document;
    }

    private void changeBodyElements(List<IBodyElement> bodyElements) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            if (!paragraphs.isEmpty()) {
                DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
                paragraphs.clear();
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (int i = 0; i < rows.size(); i++) {
                    XWPFTableRow row = rows.get(i);
                    List<XWPFTableCell> cells = row.getTableCells();
                    // try to expand cells by column
                    TableExpansionOperation tableExpansionOperation = new TableExpansionOperation(row);
                    for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
                        final XWPFTableCell cell = cells.get(columnIndex);
                        ColumnExpansionOperation operation = DocxUtils.parseIterationOperation(config, variableProvider, cell.getText(),
                                new ColumnExpansionOperation());
                        if (operation != null && operation.isValid()) {
                            tableExpansionOperation.addOperation(columnIndex, operation);
                        } else {
                            operation = new ColumnSetValueOperation();
                            operation.setContainerValue(cell.getText());
                            tableExpansionOperation.addOperation(columnIndex, operation);
                        }
                        String text0 = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, 0);
                        if (!java.util.Objects.equals(text0, cell.getText())) {
                            DocxUtils.setCellText(cell, text0);
                        }
                    }
                    if (tableExpansionOperation.getRows() == 0) {
                        for (XWPFTableCell cell : cells) {
                            DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs());
                        }
                    } else {
                        int templateRowIndex = table.getRows().indexOf(tableExpansionOperation.getTemplateRow());
                        for (int rowIndex = 1; rowIndex < tableExpansionOperation.getRows(); rowIndex++) {
                            XWPFTableRow dynamicRow = table.insertNewTableRow(templateRowIndex + rowIndex);
                            for (int columnIndex = 0; columnIndex < tableExpansionOperation.getTemplateRow().getTableCells().size(); columnIndex++) {
                                dynamicRow.createCell();
                            }
                            for (int columnIndex = 0; columnIndex < dynamicRow.getTableCells().size(); columnIndex++) {
                                String text = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, rowIndex);
                                DocxUtils.setCellText(dynamicRow.getCell(columnIndex), text, tableExpansionOperation.getTemplateCell(columnIndex));
                                if (OfficeProperties.getDocxPlaceholderVMerge().equals(text)) {
                                    CTTcPr tcPr = dynamicRow.getCell(columnIndex).getCTTc().getTcPr();
                                    tcPr.addNewVMerge().setVal(STMerge.CONTINUE);

                                    int restartVMergeRowIndex = table.getRows().indexOf(dynamicRow) - 1;
                                    if (restartVMergeRowIndex >= 0) {
                                        XWPFTableRow restartRow = table.getRow(restartVMergeRowIndex);
                                        CTTcPr previousTcPr = restartRow.getCell(columnIndex).getCTTc().getTcPr();
                                        if (previousTcPr.getVMerge() == null) {
                                            previousTcPr.addNewVMerge().setVal(STMerge.RESTART);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!paragraphs.isEmpty()) {
            DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
            paragraphs.clear();
        }
    }

}
