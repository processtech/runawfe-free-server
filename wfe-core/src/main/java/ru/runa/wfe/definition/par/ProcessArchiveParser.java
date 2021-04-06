package ru.runa.wfe.definition.par;

import ru.runa.wfe.lang.ParsedProcessDefinition;

public interface ProcessArchiveParser {

    void readFromArchive(ProcessArchive archive, ParsedProcessDefinition parsedProcessDefinition);

    boolean isApplicableToEmbeddedSubprocess();
    
}
