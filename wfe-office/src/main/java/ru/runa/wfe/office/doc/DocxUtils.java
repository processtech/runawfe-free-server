package ru.runa.wfe.office.doc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.SafeIndefiniteLoop;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.office.OfficeProperties;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class DocxUtils {
    private static final Log log = LogFactory.getLog(DocxUtils.class);
    private static final String GROOVY = "groovy:";
    private static final String LINE_DELIMITER = "\n";
    private static final String ITERATOR_NAME_DELIMITER = " as ";
    private static final Pattern STRIP_HTML_TAGS_PATTERN = Pattern.compile("<.+?>");

    public static final String PLACEHOLDER_START = OfficeProperties.getDocxPlaceholderStart();
    public static final String PLACEHOLDER_END = OfficeProperties.getDocxPlaceholderEnd();
    public static final String CLOSING_PLACEHOLDER_START = PLACEHOLDER_START + "/";

    public static int getPictureType(DocxConfig config, String fileName) {
        if (fileName.endsWith(".emf")) {
            return XWPFDocument.PICTURE_TYPE_EMF;
        } else if (fileName.endsWith(".wmf")) {
            return XWPFDocument.PICTURE_TYPE_WMF;
        } else if (fileName.endsWith(".pict")) {
            return XWPFDocument.PICTURE_TYPE_PICT;
        } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        } else if (fileName.endsWith(".png")) {
            return XWPFDocument.PICTURE_TYPE_PNG;
        } else if (fileName.endsWith(".dib")) {
            return XWPFDocument.PICTURE_TYPE_DIB;
        } else if (fileName.endsWith(".gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        } else if (fileName.endsWith(".tiff")) {
            return XWPFDocument.PICTURE_TYPE_TIFF;
        } else if (fileName.endsWith(".eps")) {
            return XWPFDocument.PICTURE_TYPE_EPS;
        } else if (fileName.endsWith(".bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        } else if (fileName.endsWith(".wpg")) {
            return XWPFDocument.PICTURE_TYPE_WPG;
        }
        config.reportProblem("Unsupported picture: " + fileName + ". Expected emf|wmf|pict|jpeg|png|dib|gif|tiff|eps|bmp|wpg");
        return -1;
    }

    public static void setCellText(final XWPFTableCell cell, String text) {
        if (cell.getParagraphs().size() > 0 && cell.getParagraphs().get(0).getRuns().size() > 0) {
            new SafeIndefiniteLoop(100) {

                @Override
                protected void doOp() {
                    cell.removeParagraph(1);
                }

                @Override
                protected boolean continueLoop() {
                    return cell.getParagraphs().size() > 1;
                }
            }.doLoop();
            final XWPFParagraph paragraph0 = cell.getParagraphs().get(0);
            new SafeIndefiniteLoop(100) {

                @Override
                protected void doOp() {
                    paragraph0.removeRun(1);
                }

                @Override
                protected boolean continueLoop() {
                    return paragraph0.getRuns().size() > 1;
                }
            }.doLoop();
            paragraph0.getRuns().get(0).setText(text != null ? text : "", 0);
        } else {
            cell.setText(text != null ? text : "");
            log.warn("no paragraphs or empty one, using raw text insert");
        }
    }

    public static void setCellText(XWPFTableCell cell, String text, XWPFTableCell cellTemplate) {
        if (cellTemplate != null) {
            if (cell.getCTTc().getTcPr() != null && cellTemplate.getCTTc().getTcPr() != null) {
                cell.getCTTc().getTcPr().setTcBorders(cellTemplate.getCTTc().getTcPr().getTcBorders());
            } else if (cellTemplate.getCTTc().getTcPr() != null) {
                cell.getCTTc().addNewTcPr().setTcBorders(cellTemplate.getCTTc().getTcPr().getTcBorders());
            }
        }
        if (cellTemplate != null && cellTemplate.getParagraphs().size() > 0 && cellTemplate.getParagraphs().get(0).getRuns().size() > 0) {
            XWPFParagraph paragraph0;
            if (cell.getParagraphs().size() > 0) {
                paragraph0 = cell.getParagraphs().get(0);
            } else {
                paragraph0 = cell.addParagraph();
            }
            try {
                XWPFParagraph templateParagraph = cellTemplate.getParagraphs().get(0);
                paragraph0.setAlignment(templateParagraph.getAlignment());
                paragraph0.setBorderBetween(templateParagraph.getBorderBetween());
                paragraph0.setBorderBottom(templateParagraph.getBorderBottom());
                paragraph0.setBorderLeft(templateParagraph.getBorderLeft());
                paragraph0.setBorderRight(templateParagraph.getBorderRight());
                paragraph0.setBorderTop(templateParagraph.getBorderTop());
                paragraph0.setIndentationFirstLine(templateParagraph.getIndentationFirstLine());
                paragraph0.setIndentationHanging(templateParagraph.getIndentationHanging());
                paragraph0.setIndentationLeft(templateParagraph.getIndentationLeft());
                paragraph0.setIndentationRight(templateParagraph.getIndentationRight());
                paragraph0.setPageBreak(templateParagraph.isPageBreak());
                // paragraph0.setSpacingAfter(templateParagraph.getSpacingAfter());
                // paragraph0.setSpacingAfterLines(templateParagraph.getSpacingAfterLines());
                // paragraph0.setSpacingBefore(templateParagraph.getSpacingBefore());
                // paragraph0.setSpacingLineRule(templateParagraph.getSpacingLineRule());
                // paragraph0.setStyle(templateParagraph.getStyle());
                paragraph0.setVerticalAlignment(templateParagraph.getVerticalAlignment());
                // paragraph0.setWordWrap(templateParagraph.isWordWrap());
            } catch (Exception e) {
                log.warn("Unable to copy paragraph styles", e);
            }
            XWPFRun run0;
            if (paragraph0.getRuns().size() > 0) {
                run0 = paragraph0.getRuns().get(0);
            } else {
                run0 = paragraph0.createRun();
            }
            StylesHolder stylesHolder = new StylesHolder(cellTemplate.getParagraphs().get(0).getRuns().get(0));
            stylesHolder.applyStyles(run0);
            run0.setText(text != null ? text : "", 0);
        } else {
            cell.setText(text != null ? text : "");
            log.warn("null or invalid template cell, using raw text insert");
        }
    }

    private static Object executeGroovy(IVariableProvider variableProvider, String script) {
        script = script.substring(GROOVY.length());
        GroovyScriptExecutor executor = new GroovyScriptExecutor();
        return executor.evaluateScript(variableProvider, script);
    }

    public static Object getValue(DocxConfig config, IVariableProvider variableProvider, Object value, String selector) {
        if (value == null) {
            if (selector.startsWith(GROOVY)) {
                return executeGroovy(variableProvider, selector);
            }
            if (!Strings.isNullOrEmpty(selector)) {
                value = variableProvider.getValue(selector);
            }
        }
        if (!Strings.isNullOrEmpty(selector)) {
            StringTokenizer tokenizer = new StringTokenizer(selector, "\\.");
            while (tokenizer.hasMoreTokens()) {
                String variableName = tokenizer.nextToken();
                String keyName = null;
                int elementStartIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                if (elementStartIndex > 0 && variableName.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                    keyName = variableName.substring(elementStartIndex + VariableFormatContainer.COMPONENT_QUALIFIER_START.length(),
                            variableName.length() - VariableFormatContainer.COMPONENT_QUALIFIER_END.length());
                    variableName = variableName.substring(0, elementStartIndex);
                }
                if (value == null) {
                    value = variableProvider.getValue(variableName);
                } else {
                    if (value instanceof Map) {
                        value = ((Map<?, ?>) value).get(variableName);
                    } else {
                        try {
                            value = PropertyUtils.getProperty(value, variableName);
                        } catch (Exception e) {
                            config.reportProblem(e);
                        }
                    }
                }
                if (value == null) {
                    config.warn("returning null for " + selector + " at stage " + variableName);
                    return null;
                }
                if (keyName != null) {
                    if (value instanceof Map) {
                        Object key = variableProvider.getValue(keyName);
                        if (key == null) {
                            key = keyName;
                            if (keyName.startsWith("\"") && keyName.endsWith("\"")) {
                                key = keyName.substring(1, keyName.length() - 1);
                            }
                        }
                        value = ((Map<?, ?>) value).get(key);
                    } else if (value instanceof List) {
                        Integer index;
                        try {
                            index = Integer.parseInt(keyName);
                        } catch (Exception e) {
                            index = variableProvider.getValue(Integer.class, keyName);
                        }
                        if (index == null) {
                            config.reportProblem("Null index for " + keyName);
                        }
                        value = TypeConversionUtil.getListValue(value, index);
                    } else {
                        config.reportProblem("Unable to get element '" + keyName + "' value from " + value);
                    }
                }
            }
        } else if (value instanceof String) {
            if (((String) value).startsWith(GROOVY)) {
                return executeGroovy(variableProvider, (String) value);
            }
            value = ((String) value).replaceAll(Pattern.quote("</p>"), "\n").replaceAll("&nbsp;", " ");
            Matcher m = STRIP_HTML_TAGS_PATTERN.matcher((String) value);
            return m.replaceAll("");
        }
        return value;
    }

    public static <T extends AbstractIteratorOperation> T parseIterationOperation(DocxConfig config, IVariableProvider variableProvider,
            String string, T operation) {
        if (Strings.isNullOrEmpty(string)) {
            return null;
        }
        string = string.trim();
        if (string.startsWith(CLOSING_PLACEHOLDER_START)) {
            return null;
        }
        if (string.startsWith(PLACEHOLDER_START) && string.endsWith(PLACEHOLDER_END)) {
            String placeholder = string.substring(PLACEHOLDER_START.length(), string.length() - PLACEHOLDER_END.length());
            int iteratorNameIndex = placeholder.lastIndexOf(ITERATOR_NAME_DELIMITER);
            String iteratorWithContainerVariable = placeholder;
            if (iteratorNameIndex != -1) {
                iteratorWithContainerVariable = iteratorWithContainerVariable.substring(0, iteratorNameIndex).trim();
            }
            int colonIndex = iteratorWithContainerVariable.indexOf(":");
            if (colonIndex > 0) {
                try {
                    operation.setIterateBy(IterateBy.identifyByString(config, iteratorWithContainerVariable));
                } catch (Exception e) {
                    return null;
                }
                operation.setContainerVariableName(iteratorWithContainerVariable.substring(colonIndex + 1).trim());
            } else {
                return null;
            }
            if (iteratorNameIndex != -1) {
                String lexem = placeholder.substring(iteratorNameIndex + ITERATOR_NAME_DELIMITER.length()).trim();
                if (operation instanceof ColumnExpansionOperation) {
                    ((ColumnExpansionOperation) operation).setContainerSelector(lexem);
                }
                if (operation instanceof LoopOperation) {
                    ((LoopOperation) operation).setIteratorVariableName(lexem);
                }
            }
            if (operation.getContainerVariableName().contains(PLACEHOLDER_START)) {
                // this is the case of multiple replacements in one line
                return null;
            }
            if (operation.getContainerVariableName().startsWith(GROOVY)) {
                operation.setContainerValue(executeGroovy(variableProvider, operation.getContainerVariableName()));
            } else {
                WfVariable variable = variableProvider.getVariable(operation.getContainerVariableName());
                if (variable != null) {
                    operation.setContainerVariable(variable);
                } else {
                    config.warn("not an iteration operation: Variable not found by '" + placeholder + "' (checked '"
                            + operation.getContainerVariableName() + "')");
                }
            }
            if (!operation.isValid()) {
                // config.reportProblem("Invalid " + operation + " for '" +
                // placeholder + "'");
                return null;
            }
            return operation;
        }
        return null;
    }

    public static void replaceInParagraphs(DocxConfig config, MapDelegableVariableProvider variableProvider, List<XWPFParagraph> paragraphs) {
        Stack<Operation> operations = new Stack<Operation>();
        for (XWPFParagraph paragraph : Lists.newArrayList(paragraphs)) {
            String paragraphText = paragraph.getText();
            LoopOperation loopOperation = parseIterationOperation(config, variableProvider, paragraphText, new LoopOperation());
            if (loopOperation != null && loopOperation.isValid()) {
                loopOperation.setHeaderParagraph(paragraph);
                operations.push(loopOperation);
                continue;
            } else if (!operations.isEmpty()) {
                if (operations.peek() instanceof LoopOperation) {
                    if (operations.peek().isEndBlock(paragraphText)) {
                        XWPFDocument document = paragraph.getDocument();
                        int insertPosition = document.getParagraphPos(document.getPosOfParagraph(paragraph));
                        loopOperation = (LoopOperation) operations.pop();
                        Iterator<? extends Object> iterator = loopOperation.createIterator();
                        Object iteratorValue0 = null;
                        if (iterator.hasNext()) {
                            iteratorValue0 = iterator.next();
                        }
                        while (iterator.hasNext()) {
                            Object iteratorValue = iterator.next();
                            variableProvider.add(loopOperation.getIteratorVariable(iteratorValue));
                            for (XWPFParagraph templateParagraph : loopOperation.getBodyParagraphs()) {
                                XmlCursor cursor = document.getDocument().getBody().getPArray(insertPosition).newCursor();
                                XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
                                insertPosition++;
                                for (XWPFRun templateRun : templateParagraph.getRuns()) {
                                    XWPFRun newRun = newParagraph.createRun();
                                    StylesHolder stylesHolder = new StylesHolder(templateRun);
                                    // https://sourceforge.net/p/runawfe/bugs/474/
                                    // templateRun.getCTR().getRPr().getShd().getFill()
                                    stylesHolder.applyStyles(newRun);
                                    String text = templateRun.getText(0);
                                    if (text != null) {
                                        newRun.setText(text);
                                    }
                                }
                                replaceInParagraph(config, variableProvider, newParagraph);
                            }
                        }
                        if (iteratorValue0 != null) {
                            variableProvider.add(loopOperation.getIteratorVariable(iteratorValue0));
                            for (XWPFParagraph templateParagraph : loopOperation.getBodyParagraphs()) {
                                replaceInParagraph(config, variableProvider, templateParagraph);
                            }
                        }
                        document.removeBodyElement(document.getPosOfParagraph(loopOperation.getHeaderParagraph()));
                        document.removeBodyElement(document.getPosOfParagraph(paragraph));
                        variableProvider.remove(loopOperation.getIteratorVariableName());
                        continue;
                    }
                    ((LoopOperation) operations.peek()).getBodyParagraphs().add(paragraph);
                } else if (operations.peek() instanceof IfOperation) {
                }
            } else {
                replaceInParagraph(config, variableProvider, paragraph);
            }
        }
        if (!operations.isEmpty()) {
            config.reportProblem("Found unconsistency for operations: not ended " + operations);
        }
    }

    public static void replaceInParagraph(DocxConfig config, IVariableProvider variableProvider, XWPFParagraph paragraph) {
        String paragraphText = paragraph.getParagraphText();
        if (!paragraphText.contains(PLACEHOLDER_START)) {
            return;
        }
        if (!paragraphText.contains(PLACEHOLDER_END)) {
            config.warn("No placeholder end '" + PLACEHOLDER_END + "' found for '" + PLACEHOLDER_START + "' in " + paragraphText);
            return;
        }
        List<XWPFRun> paragraphRuns = Lists.newArrayList(paragraph.getRuns());
        int whetherSingleRunContainsPlaceholderStart = 0;
        int whetherMultRunContainsPlaceholderStart = 0;
        int whetherSingleRunContainsPlaceholderEnd = 0;
        for (int i = 0; i < paragraphRuns.size(); i++) {
            XWPFRun run = paragraphRuns.get(i);
            XWPFRun next = i + 1 < paragraphRuns.size() ? paragraphRuns.get(i + 1) : null;
            if (run == null || run.getText(0) == null) {
                continue;
            }
            if (run.getText(0).contains(PLACEHOLDER_START)) {
                whetherSingleRunContainsPlaceholderStart++;
            }
            if (run.getText(0).contains(PLACEHOLDER_END)) {
                whetherSingleRunContainsPlaceholderEnd++;
            }
            if (next == null || next.getText(0) == null || PLACEHOLDER_START.length() < 2) {
                continue;
            }
            int j = 1;
            String test = PLACEHOLDER_START.substring(0, j);
            while (j < PLACEHOLDER_START.length() && !run.getText(0).endsWith(test)) {
                test = PLACEHOLDER_START.substring(0, ++j);
            }
            if (j == PLACEHOLDER_START.length() || !next.getText(0).startsWith(PLACEHOLDER_START.substring(j, PLACEHOLDER_START.length()))) {
                continue;
            }
            whetherMultRunContainsPlaceholderStart++;
        }
        if (whetherMultRunContainsPlaceholderStart > 0) {
            fixRunsToStateInWhichSingleRunContainsPlaceholder(config, paragraph, PLACEHOLDER_START);
        }
        if (whetherSingleRunContainsPlaceholderEnd < whetherSingleRunContainsPlaceholderStart + whetherMultRunContainsPlaceholderStart) {
            fixRunsToStateInWhichSingleRunContainsPlaceholder(config, paragraph, PLACEHOLDER_END);
        }
        List<ReplaceOperation> operations = Lists.newArrayList();
        for (XWPFRun run : Lists.newArrayList(paragraph.getRuns())) {
            if (run == null) {
                log.warn("Null run in paragraph " + paragraphText);
                continue;
            }
            CTR ctr = run.getCTR();
            int tArraySize = ctr.sizeOfTArray();
            if (tArraySize == 0) {
                log.warn("Null run CTR value in paragraph " + paragraphText);
                continue;
            }
            for (int index = 0; index < tArraySize; index++) {
                String text = ctr.getTArray(index).getStringValue();
                if (text == null) {
                    log.warn("Null run[" + index + "] value in paragraph " + paragraphText);
                    continue;
                }
                String replacedText = replaceText(config, variableProvider, operations, text);
                if (!Objects.equal(replacedText, text)) {
                    if (replacedText.contains(LINE_DELIMITER)) {
                        StringTokenizer tokenizer = new StringTokenizer(replacedText, LINE_DELIMITER);
                        while (tokenizer.hasMoreTokens()) {
                            run.setText(tokenizer.nextToken(), 0);
                            if (tokenizer.hasMoreTokens()) {
                                run.addBreak();
                                run = paragraph.insertNewRun(paragraph.getRuns().indexOf(run) + 1);
                            }
                        }
                    } else {
                        run.setText(replacedText, index);
                    }
                }
            }
            for (ReplaceOperation replaceOperation : Lists.newArrayList(operations)) {
                if (replaceOperation instanceof InsertImageOperation) {
                    InsertImageOperation imageOperation = (InsertImageOperation) replaceOperation;
                    IFileVariable fileVariable = imageOperation.getFileVariable();
                    try {
                        run.addPicture(new ByteArrayInputStream(fileVariable.getData()), imageOperation.getImageType(), fileVariable.getName(),
                                imageOperation.getWidth(), imageOperation.getHeight());
                    } catch (Exception e) {
                        config.reportProblem(e);
                    }
                    operations.remove(replaceOperation);
                }
            }
        }
    }

    private static void fixRunsToStateInWhichSingleRunContainsPlaceholder(DocxConfig config, XWPFParagraph paragraph, String placeholder) {
        config.warn("Restructuring runs for '" + placeholder + "' in '" + paragraph.getParagraphText() + "'");
        try {
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = 0; i < runs.size() - 1; i++) {
                PlaceholderMatch match = new PlaceholderMatch(placeholder.toCharArray());
                PlaceholderMatch.Status status = match.testRun(runs.get(i));
                int next = i + 1;
                while (status == PlaceholderMatch.Status.MOVE_NEXT_RUN) {
                    status = match.testRun(runs.get(next));
                    next++;
                }
                if (status == PlaceholderMatch.Status.MOVE_NEW_RUN) {
                    continue;
                }
                if (status == PlaceholderMatch.Status.COMPLETED) {
                    for (int n = 0; n < match.runs.size(); n++) {
                        if (n == 0) {
                            String newText = match.runs.get(n).getText(0);
                            newText = newText.substring(0, match.comparisonStartInFirstRunIndex) + placeholder;
                            match.runs.get(n).setText(newText, 0);
                        } else if (n == match.runs.size() - 1) {
                            String newText = match.runs.get(n).getText(0);
                            newText = newText.substring(match.comparisonEndInLastRunIndex);
                            match.runs.get(n).setText(newText, 0);
                        } else {
                            match.runs.get(n).setText("", 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            config.reportProblem(new Exception("Unable to adjust runs for '" + PLACEHOLDER_START + "' in '" + paragraph.getParagraphText() + "'", e));
        }
    }

    private static class PlaceholderMatch {
        enum Status {
            MOVE_NEW_RUN,
            MOVE_NEXT_RUN,
            COMPLETED
        }

        final char[] placeholderChars;
        final List<XWPFRun> runs = Lists.newArrayList();
        int currentComparisonIndex = 0;
        int comparisonStartInFirstRunIndex = -1;
        int comparisonEndInLastRunIndex = -1;

        private PlaceholderMatch(char[] placeholderChars) {
            this.placeholderChars = placeholderChars;
        }

        Status testRun(XWPFRun run) {
            boolean firstRun = runs.size() == 0;
            runs.add(run);
            char[] runChars = run.getText(0).toCharArray();
            for (int j = 0; j < runChars.length; j++) {
                if (runChars[j] == placeholderChars[currentComparisonIndex]) {
                    if (firstRun && currentComparisonIndex == 0) {
                        comparisonStartInFirstRunIndex = j;
                    }
                    currentComparisonIndex++;
                    if (isMatchCompleted()) {
                        comparisonEndInLastRunIndex = j + 1;
                        return Status.COMPLETED;
                    }
                } else if (currentComparisonIndex != 0) {
                    if (firstRun) {
                        currentComparisonIndex = 0;
                    } else {
                        break;
                    }
                }
            }
            return currentComparisonIndex != 0 ? Status.MOVE_NEXT_RUN : Status.MOVE_NEW_RUN;
        }

        boolean isMatchCompleted() {
            return currentComparisonIndex == placeholderChars.length;
        }
    }

    private static String replaceText(DocxConfig config, IVariableProvider variableProvider, List<ReplaceOperation> operations, String text) {
        ReplaceOperation operation;
        if (operations.size() > 0 && !operations.get(operations.size() - 1).isPlaceholderRead()) {
            operation = operations.get(operations.size() - 1);
        } else {
            operation = new ReplaceOperation();
            operations.add(operation);
        }
        if (!operation.isStarted()) {
            // search start
            int placeholderStartIndex = text.indexOf(PLACEHOLDER_START);
            if (placeholderStartIndex >= 0) {
                String start = text.substring(0, placeholderStartIndex);
                operation.appendPlaceholder("");
                String remainder = text.substring(placeholderStartIndex + PLACEHOLDER_START.length());
                return start + replaceText(config, variableProvider, operations, remainder);
            }
            return text;
        } else {
            // search end
            int placeholderEndIndex = text.indexOf(PLACEHOLDER_END);
            if (placeholderEndIndex >= 0) {
                operation.appendPlaceholder(text.substring(0, placeholderEndIndex));
                operation.setEnded(true);
                String remainder = text.substring(placeholderEndIndex + PLACEHOLDER_END.length());
                Object value = getValue(config, variableProvider, null, operation.getPlaceholder());
                if (value == null) {
                    if (config.isStrictMode()) {
                        config.reportProblem("No template variable defined in process: '" + operation.getPlaceholder() + "'");
                    }
                }
                if (value instanceof IFileVariable) {
                    try {
                        operations.remove(operation);
                        IFileVariable fileVariable = (IFileVariable) value;
                        InsertImageOperation imageOperation = new InsertImageOperation(operation.getPlaceholder(), fileVariable);
                        imageOperation.setValue("");
                        int imageType = getPictureType(config, fileVariable.getName().toLowerCase());
                        if (imageType > 0) {
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileVariable.getData()));
                            // TODO does not work without ooxml
                            imageOperation.setImageType(imageType);
                            imageOperation.setWidth(Units.toEMU(image.getWidth()));
                            imageOperation.setHeight(Units.toEMU(image.getHeight()));
                            operations.add(imageOperation);
                            operation = imageOperation;
                        }
                    } catch (Exception e) {
                        config.reportProblem(e);
                    }
                } else {
                    VariableFormat valueFormat = null;
                    String placeholder = operation.getPlaceholder();
                    if (placeholder.contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)
                            && placeholder.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                        placeholder = placeholder.substring(0, placeholder.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START));
                        WfVariable containerVariable = variableProvider.getVariable(placeholder);
                        if (containerVariable != null) {
                            int index = containerVariable.getValue() instanceof Map ? 1 : 0;
                            valueFormat = FormatCommons.createComponent(containerVariable, index);
                        }
                    } else {
                        if (!placeholder.startsWith(GROOVY)) {
                            WfVariable variable = variableProvider.getVariable(placeholder);
                            if (variable != null) {
                                valueFormat = variable.getDefinition().getFormatNotNull();
                            }
                        }
                    }
                    String replacement;
                    if (valueFormat != null) {
                        replacement = valueFormat.format(value);
                        if (replacement == null) {
                            replacement = "";
                        }
                    } else {
                        replacement = TypeConversionUtil.convertTo(String.class, value);
                    }
                    operation.setValue(replacement);
                }
                return operation.getValue() + replaceText(config, variableProvider, operations, remainder);
            } else {
                operation.appendPlaceholder(text);
                return "";
            }
        }
    }

    public static class StylesHolder {
        private final boolean bold;
        private final String color;
        private final String fontFamily;
        private final int fontSize;
        private final boolean italic;
        private final boolean strike;
        private final VerticalAlign subscript;
        private final UnderlinePatterns underlinePatterns;

        public StylesHolder(XWPFRun run) {
            bold = run.isBold();
            color = run.getColor();
            fontFamily = run.getFontFamily();
            fontSize = run.getFontSize();
            italic = run.isItalic();
            strike = run.isStrike();
            subscript = run.getSubscript();
            underlinePatterns = run.getUnderline();
        }

        public void applyStyles(XWPFRun run) {
            // absence of checks caused logical errors in result document
            run.setBold(bold);
            if (color != null) {
                run.setColor(color);
            }
            if (fontFamily != null) {
                run.setFontFamily(fontFamily);
            }
            if (fontSize > 0) {
                run.setFontSize(fontSize);
            }
            run.setItalic(italic);
            run.setStrike(strike);
            if (subscript != null) {
                run.setSubscript(subscript);
            }
            if (underlinePatterns != null) {
                run.setUnderline(underlinePatterns);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StylesHolder)) {
                return false;
            }
            StylesHolder h = (StylesHolder) obj;
            return bold == h.bold && Objects.equal(color, h.color) && Objects.equal(fontFamily, h.fontFamily) && italic == h.italic
                    && strike == h.strike && Objects.equal(subscript, h.subscript) && Objects.equal(underlinePatterns, h.underlinePatterns);
        }
    }

}
