package ru.runa.wfe.extension.handler.file;

import java.io.File;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.var.file.FileVariableImpl;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.net.MediaType;

public class LoadFileVariableFromFileSystemHandler extends CommonParamBasedHandler {
    private static Map<String, MediaType> extensionTypeMappings = Maps.newHashMap();
    static {
        extensionTypeMappings.put("doc", MediaType.MICROSOFT_WORD);
        extensionTypeMappings.put("docx", MediaType.OOXML_DOCUMENT);
        extensionTypeMappings.put("txt", MediaType.PLAIN_TEXT_UTF_8);
        extensionTypeMappings.put("pdf", MediaType.PDF);
        extensionTypeMappings.put("xls", MediaType.MICROSOFT_EXCEL);
        extensionTypeMappings.put("xlsx", MediaType.OOXML_SHEET);
        extensionTypeMappings.put("bmp", MediaType.BMP);
        extensionTypeMappings.put("gif", MediaType.GIF);
        extensionTypeMappings.put("jpg", MediaType.JPEG);
        extensionTypeMappings.put("jpeg", MediaType.JPEG);
        extensionTypeMappings.put("png", MediaType.PNG);
        extensionTypeMappings.put("zip", MediaType.ZIP);
        extensionTypeMappings.put("html", MediaType.HTML_UTF_8);
        extensionTypeMappings.put("xml", MediaType.XML_UTF_8);
    }

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String filePath = handlerData.getInputParamValueNotNull(String.class, "path");
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("File not found by path '" + filePath + "'");
        }
        String extension = Files.getFileExtension(filePath);
        MediaType mediaType = extensionTypeMappings.get(extension);
        if (mediaType == null) {
            mediaType = MediaType.ANY_APPLICATION_TYPE;
        }
        FileVariableImpl fileVariable = new FileVariableImpl(file.getName(), mediaType.toString());
        fileVariable.setData(Files.toByteArray(file));
        handlerData.setOutputParam("result", fileVariable);
    }

}
