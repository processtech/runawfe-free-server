package ru.runa.wfe.office.doc;

import com.google.common.collect.Maps;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.doc.MergeDocxConfig.DocxInfo;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.VariableProvider;

public class MergeDocxHandler extends OfficeFilesSupplierHandler<MergeDocxConfig> {

    @Override
    protected FilesSupplierConfigParser<MergeDocxConfig> createParser() {
        return new MergeDocxConfigParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        List<DocxInfo> infos = config.getInputFileInfos();
        if (infos.isEmpty()) {
            log.warn("empty docx to merge");
            return result;
        }
        XWPFDocument document = null;
        for (DocxInfo docxInfo : infos) {
            InputStream inputStream = config.getFileInputStream(variableProvider, fileDataProvider, docxInfo);
            XWPFDocument mergingDocument = new XWPFDocument(inputStream);
            if (document == null) {
                // first document used as base
                document = mergingDocument;
            } else {
                for (IBodyElement bodyElement : mergingDocument.getBodyElements()) {
                    if (bodyElement instanceof XWPFParagraph) {
                        XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                        List<XWPFRun> runs = paragraph.getRuns();
                        XWPFParagraph newParagraph = document.createParagraph();
                        XWPFRun newRun = null;
                        for (XWPFRun run : runs) {
                            if (run instanceof XWPFHyperlinkRun) {
                                XWPFHyperlinkRun hlRun = (XWPFHyperlinkRun) run;
                                String rId = newParagraph
                                        .getPart()
                                        .getPackagePart()
                                        .addExternalRelationship(hlRun.getCTHyperlink().getRArray(0).getTArray(0).getStringValue(),
                                                XWPFRelation.HYPERLINK.getRelation()).getId();
                                CTHyperlink newHyperlink = newParagraph.getCTP().addNewHyperlink();
                                newHyperlink.set(hlRun.getCTHyperlink());
                                newHyperlink.setId(rId);
                                newRun = new XWPFHyperlinkRun(newHyperlink, newHyperlink.getRArray(0), newParagraph);
                                newParagraph.addRun(newRun);
                            } else {
                                newRun = newParagraph.createRun();
                                newRun.getCTR().set(run.getCTR());
                            }
                        }
                        if (docxInfo.addBreak) {
                            newParagraph.setPageBreak(true);
                            docxInfo.addBreak = false;
                        }
                    } else if (bodyElement instanceof XWPFTable) {
                        XWPFTable table = (XWPFTable) bodyElement;
                        document.createTable();
                        document.setTable(document.getTables().size() - 1, table);
                    }
                }
            }
        }
        OutputStream outputStream = config.getFileOutputStream(result, variableProvider, true);
        if (config.getOutputFileName().endsWith("pdf")) {
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, outputStream, options);
        } else {
            document.write(outputStream);
        }
        return result;
    }

}
