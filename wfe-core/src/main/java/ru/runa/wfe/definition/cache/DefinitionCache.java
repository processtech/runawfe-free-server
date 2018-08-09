package ru.runa.wfe.definition.cache;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.lang.ProcessDefinition;

public interface DefinitionCache {

    ProcessDefinition getDefinition(long deploymentVersionId) throws DefinitionDoesNotExistException;

    ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

    ProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException;
}
