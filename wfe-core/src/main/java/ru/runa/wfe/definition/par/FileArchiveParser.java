package ru.runa.wfe.definition.par;

import java.util.Map;

import ru.runa.wfe.lang.ParsedProcessDefinition;

public class FileArchiveParser implements ProcessArchiveParser {

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }
    
    @Override
    public void readFromArchive(ProcessArchive processArchive, ParsedProcessDefinition parsedProcessDefinition) {
        for (Map.Entry<String, byte[]> entry : processArchive.getFileData().entrySet()) {
            parsedProcessDefinition.addFile(entry.getKey(), entry.getValue());
        }
    }

}
