package ru.runa.wfe.definition.cache;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.lang.ProcessDefinition;

public interface DefinitionCache {

    public abstract ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException;

    public abstract ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

}