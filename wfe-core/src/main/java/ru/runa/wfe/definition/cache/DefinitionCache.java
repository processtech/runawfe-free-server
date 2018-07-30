package ru.runa.wfe.definition.cache;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.lang.ProcessDefinition;

public interface DefinitionCache {

    ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException;

    ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;
}
