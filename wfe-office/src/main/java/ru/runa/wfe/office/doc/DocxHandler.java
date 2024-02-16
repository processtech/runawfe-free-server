package ru.runa.wfe.office.doc;

import com.google.common.collect.Maps;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.VariableProvider;

public class DocxHandler extends OfficeFilesSupplierHandler<DocxConfig> {

    @Override
    protected FilesSupplierConfigParser<DocxConfig> createParser() {
        return new DocxConfigParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) throws Exception {
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
        document.write(outputStream);
        outputStream.close();
        return result;
    }
}
