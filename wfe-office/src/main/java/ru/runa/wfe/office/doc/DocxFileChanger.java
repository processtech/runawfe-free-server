package ru.runa.wfe.office.doc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.collect.Lists;

public class DocxFileChanger {
    private final DocxConfig config;
    private final MapDelegableVariableProvider variableProvider;
    private final XWPFDocument document;

    public DocxFileChanger(DocxConfig config, IVariableProvider variableProvider, InputStream templateInputStream) throws IOException {
        this.config = config;
        this.variableProvider = new MapDelegableVariableProvider(new HashMap<String, Object>(), variableProvider);
        this.document = new XWPFDocument(templateInputStream);
    }

    public XWPFDocument changeAll() {
        List<IBodyElement> bodyElements = new ArrayList<IBodyElement>(document.getBodyElements());
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            if (paragraphs.size() > 0) {
                DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
                paragraphs.clear();
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : Lists.newArrayList(rows)) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    // try to expand cells by column
                    TableExpansionOperation tableExpansionOperation = new TableExpansionOperation(cells);
                    for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
                        final XWPFTableCell cell = cells.get(columnIndex);
                        ColumnExpansionOperation operation = DocxUtils.parseIterationOperation(config, variableProvider, cell.getText(),
                                new ColumnExpansionOperation());
                        if (operation != null && operation.isValid()) {
                            tableExpansionOperation.addOperation(columnIndex, operation);
                            String text0 = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, 0);
                            DocxUtils.setCellText(cell, text0);
                        }
                    }
                    if (tableExpansionOperation.getRows() == 0) {
                        for (XWPFTableCell cell : cells) {
                            DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs());
                        }
                    } else {
                        for (int rowIndex = 1; rowIndex < tableExpansionOperation.getRows(); rowIndex++) {
                            XWPFTableRow dynamicRow = table.createRow();
                            for (int columnIndex = 0; columnIndex < dynamicRow.getTableCells().size(); columnIndex++) {
                                String text = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, rowIndex);
                                DocxUtils.setCellText(dynamicRow.getCell(columnIndex), text, tableExpansionOperation.getCell(columnIndex));
                            }
                        }
                    }
                }
            }
        }
        if (paragraphs.size() > 0) {
            DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
            paragraphs.clear();
        }
        return document;
    }

}
