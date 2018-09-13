package ru.runa.wfe.definition.dao;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public interface IProcessDefinitionLoader {

    ParsedProcessDefinition getDefinition(long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    ParsedProcessDefinition getDefinition(Process process) throws DefinitionDoesNotExistException;

    ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

    ParsedProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException;
}
