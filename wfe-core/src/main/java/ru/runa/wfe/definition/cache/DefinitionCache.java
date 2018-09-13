package ru.runa.wfe.definition.cache;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public interface DefinitionCache {

    ParsedProcessDefinition getDefinition(long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

    ParsedProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException;
}
