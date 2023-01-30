package ru.runa.wfe.office.doc;

import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
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
        XWPFNumbering newNumbering = null;
        for (DocxInfo docxInfo : infos) {
            InputStream inputStream = config.getFileInputStream(variableProvider, fileDataProvider, docxInfo);
            XWPFDocument mergingDocument = new XWPFDocument(inputStream);
            if (document == null) {
                // first document used as base
                document = mergingDocument;
            } else {
                XWPFNum num = null;
                XWPFNumbering mergingNumbering = mergingDocument.getNumbering();
                if (mergingNumbering != null) {
                    if (newNumbering == null) {
                        newNumbering = document.createNumbering();
                        newNumbering.addAbstractNum(
                                new XWPFAbstractNum((CTAbstractNum) mergingNumbering.getAbstractNum(BigInteger.ZERO).getCTAbstractNum().copy()));
                    }
                    XWPFAbstractNum abstractNum = newNumbering.getAbstractNum(BigInteger.ZERO);
                    BigInteger numId = newNumbering.addNum(abstractNum.getAbstractNum().getAbstractNumId());
                    num = newNumbering.getNum(numId);
                    CTNumLvl lvlOverride = num.getCTNum().addNewLvlOverride();
                    lvlOverride.setIlvl(BigInteger.ZERO);
                    CTDecimalNumber number = lvlOverride.addNewStartOverride();
                    number.setVal(BigInteger.ONE);
                }
                for (IBodyElement bodyElement : mergingDocument.getBodyElements()) {
                    if (bodyElement instanceof XWPFParagraph) {
                        XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                        XWPFParagraph newParagraph = document.createParagraph();
                        List<XWPFRun> runs = paragraph.getRuns();
                        boolean picturesEmbedded = false;
                        for (int i = 0; i < runs.size(); i++) {
                            XWPFRun run = runs.get(i);
                            if (run.getEmbeddedPictures().size() > 0) {
                                XWPFRun newRun = newParagraph.createRun();
                                newRun.getCTR().set(run.getCTR().copy());
                                newRun.getEmbeddedPictures().clear();
                                newRun.getCTR().getDrawingList().clear();
                                for (XWPFPicture picture : run.getEmbeddedPictures()) {
                                    byte[] data = picture.getPictureData().getData();
                                    newRun.addPicture(new ByteArrayInputStream(data), picture.getPictureData().getPictureType(),
                                            picture.getPictureData().getFileName(), (int) picture.getCTPicture().getSpPr().getXfrm().getExt().getCx(),
                                            (int) picture.getCTPicture().getSpPr().getXfrm().getExt().getCy());
                                }
                                picturesEmbedded = true;
                            }
                        }
                        if (!picturesEmbedded) {
                            newParagraph.getCTP().set(paragraph.getCTP().copy());
                        }
                        if (mergingNumbering != null && newParagraph.getNumID() != null) {
                            newParagraph.setNumID(num.getCTNum().getNumId());
                        }
                        if (docxInfo.addBreak) {
                            newParagraph.setPageBreak(true);
                            docxInfo.addBreak = false;
                        }
                    } else if (bodyElement instanceof XWPFTable) {
                        XmlObject xmlo = ((XWPFTable) bodyElement).getCTTbl().copy();
                        XWPFTable table = new XWPFTable((CTTbl) xmlo, document);
                        for (CTStyle style : mergingDocument.getStyle().getStyleList()) {
                            if (style.getStyleId().equals(table.getStyleID())) {
                                document.getStyles().addStyle(new XWPFStyle((CTStyle) style.copy()));
                            }
                        }
                        document.createTable();
                        document.setTable(document.getTables().size() - 1, table);
                    }
                }
            }
        }
        OutputStream outputStream = config.getFileOutputStream(result, variableProvider, true);
        document.write(outputStream);
        return result;
    }
}
