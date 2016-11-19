package ru.runa.wfe.definition.dao;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

public interface IProcessDefinitionLoader {

    public ProcessDefinition getDefinition(Long id) throws DefinitionDoesNotExistException;

    public ProcessDefinition getDefinition(Process process) throws DefinitionDoesNotExistException;

    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;
}
