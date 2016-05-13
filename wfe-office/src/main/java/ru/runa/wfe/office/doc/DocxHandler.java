package ru.runa.wfe.office.doc;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

public class DocxHandler extends OfficeFilesSupplierHandler<DocxConfig> {

    @Override
    protected FilesSupplierConfigParser<DocxConfig> createParser() {
        return new DocxConfigParser();
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        InputStream templateInputStream = config.getFileInputStream(variableProvider, fileDataProvider, true);
        XWPFDocument document;
        if (config.getTables().size() > 0) {
            log.warn("Using deprecated pre 4.0.6 changer for table configs");
            DocxFileChangerPre406 fileChanger = new DocxFileChangerPre406(config, variableProvider, templateInputStream);
            document = fileChanger.changeAll();
        } else {
            DocxFileChanger fileChanger = new DocxFileChanger(config, variableProvider, templateInputStream);
            document = fileChanger.changeAll();
        }
        OutputStream outputStream = config.getFileOutputStream(result, variableProvider, true);
        if (config.getOutputFileName().endsWith(DocxConfig.PDF_EXTENSION)) {
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, outputStream, options);
        } else {
            document.write(outputStream);
        }
        return result;
    }

}
