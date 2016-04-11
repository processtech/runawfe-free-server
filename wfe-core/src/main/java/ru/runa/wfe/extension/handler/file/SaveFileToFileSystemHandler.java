package ru.runa.wfe.extension.handler.file;

import java.io.File;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Strings;
import com.google.common.io.Files;

public class SaveFileToFileSystemHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        IFileVariable fileVariable = handlerData.getInputParamValueNotNull(IFileVariable.class, "file");
        String filePath = handlerData.getInputParamValueNotNull("path");
        if (Strings.isNullOrEmpty(Files.getFileExtension(filePath))) {
            filePath += File.separator + fileVariable.getName();
        }
        boolean override = handlerData.getInputParamValueNotNull(boolean.class, "override");
        File file = new File(filePath);
        if (file.exists() && !override) {
            throw new Exception("File alfready exists in '" + filePath + "' and override=false");
        }
        file.getParentFile().mkdirs();
        file.createNewFile();
        Files.write(fileVariable.getData(), file);
    }

}
