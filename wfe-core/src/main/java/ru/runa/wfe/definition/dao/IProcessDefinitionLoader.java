package ru.runa.wfe.definition.dao;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

public interface IProcessDefinitionLoader {

    ProcessDefinition getDefinition(long deploymentVersionId) throws DefinitionDoesNotExistException;

    ProcessDefinition getDefinition(Process process) throws DefinitionDoesNotExistException;

    ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

    ProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException;
}
