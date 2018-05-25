package ru.runa.wfe.office.doc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.office.doc.DocxConfig.TableConfig;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * @deprecated remove before release 4.2.0
 */
@Deprecated
public class DocxFileChangerPre406 {
    private static final String PLACEHOLDER_END = "}";
    private static final String PLACEHOLDER_START = "${";
    private final DocxConfig config;
    private final IVariableProvider variableProvider;
    private final XWPFDocument document;

    public DocxFileChangerPre406(DocxConfig config, IVariableProvider variableProvider, InputStream templateInputStream) throws IOException {
        this.config = config;
        this.variableProvider = variableProvider;
        document = new XWPFDocument(templateInputStream);
    }

    public XWPFDocument changeAll() throws Exception {
        List<IBodyElement> bodyElements = new ArrayList<IBodyElement>(document.getBodyElements());
        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                handleParagraph(paragraph);
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        List<XWPFParagraph> paragraphs = cell.getParagraphs();
                        for (XWPFParagraph paragraph : paragraphs) {
                            handleParagraph(paragraph);
                        }
                    }
                }
            }
        }
        return document;
    }

    private String getStyleIdbyName(String styleName) throws Exception {
        List<CTStyle> styles = document.getStyle().getStyleList();
        List<String> styleNames = new ArrayList<String>();
        for (CTStyle ctStyle : styles) {
            String name = ctStyle.getName().getVal();
            if (styleName.equals(name)) {
                return ctStyle.getStyleId();
            }
            styleNames.add(name);
        }
        throw new InternalApplicationException("Style '" + styleName + "' not found in template, all style names: " + styleNames);
    }

    private void handleParagraph(XWPFParagraph paragraph) throws Exception {
        String pText = paragraph.getParagraphText();
        if (!pText.contains(PLACEHOLDER_START)) {
            return;
        }
        if (!pText.contains(PLACEHOLDER_END)) {
            throw new InternalApplicationException("No placeholder end found in " + pText);
        }
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String text = run.getText(0);
            if (!text.contains(PLACEHOLDER_START)) {
                continue;
            }
            String placeholder = text.substring(text.indexOf(PLACEHOLDER_START) + 2);
            while (!placeholder.contains(PLACEHOLDER_END)) {
                // search end in next run
                int nextIndex = i + 1;
                if (runs.size() <= nextIndex) {
                    throw new InternalApplicationException("No placeholder end can be found in " + pText);
                }
                run = runs.get(nextIndex);
                text = run.getText(0);
                paragraph.removeRun(nextIndex);
                placeholder += text;
            }
            int plEndIndex = placeholder.indexOf(PLACEHOLDER_END);
            String remainder = placeholder.substring(plEndIndex + 1);
            placeholder = placeholder.substring(0, plEndIndex);

            TableConfig tableConfig = config.getTables().get(placeholder);
            if (tableConfig != null) {
                paragraph.removeRun(i);
                XWPFRun newRun = paragraph.insertNewRun(i);
                newRun.setText(remainder);

                int rows = 0;
                int columns = tableConfig.getColumns().size();
                List<List<?>> content = new ArrayList<List<?>>();
                for (String variableName : tableConfig.getColumns()) {
                    List<?> list = variableProvider.getValueNotNull(List.class, variableName);
                    if (list.size() > rows) {
                        rows = list.size();
                    }
                    content.add(list);
                }
                // tables
                XWPFTable table = document.createTable(rows, columns);
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        XWPFTableRow tableRow = table.getRow(r);
                        Object o = null;
                        if (content.get(c).size() > r) {
                            o = content.get(c).get(r);
                        }
                        String s = o != null ? o.toString() : "";
                        XWPFTableCell cell = tableRow.getCell(c);
                        cell.setText(s);
                    }
                }
                if (tableConfig.isAddBreak()) {
                    // Add a break between the tables
                    document.createParagraph().createRun().addBreak();
                }
                if (tableConfig.getStyleName() != null) {
                    table.setStyleID(getStyleIdbyName(tableConfig.getStyleName()));
                }
            } else {
                WfVariable variable = variableProvider.getVariable(placeholder);
                if (variable == null || variable.getValue() == null) {
                    if (config.isStrictMode()) {
                        throw new InternalApplicationException("No template variable defined in process: '" + placeholder + "'");
                    }
                    continue;
                }
                VariableFormat format = config.getTypeHints().get(placeholder);
                if (format == null) {
                    format = variable.getDefinition().getFormatNotNull();
                }
                String replacement = format.format(variable.getValue());
                paragraph.removeRun(i);
                XWPFRun newRun = paragraph.insertNewRun(i);
                newRun.setText(replacement + remainder);
                DocxUtils.copyStyles(newRun, run);
            }
        }
    }

}
