package ru.runa.wfe.fop.handlers;

import java.io.FileOutputStream;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.fop.convertors.FormattedTextToPdfConvertor;

public class FormattedTextToPdfHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String filePath = handlerData.getInputParamValueNotNull(String.class, "path");
        String html = handlerData.getInputParamValueNotNull(String.class, "html");
        FormattedTextToPdfConvertor convertor = new FormattedTextToPdfConvertor();
        byte[] pdfBytes = convertor.getPdfFromHtml(html);
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(pdfBytes);
        }
    }

}
