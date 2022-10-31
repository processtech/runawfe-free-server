package ru.runa.wfe.definition.update.validator;

import ru.runa.wfe.definition.update.ProcessDefinitionUpdateData;

/**
 * @author azyablin
 */
public interface ProcessDefinitionUpdateValidator {

    void validate(ProcessDefinitionUpdateData processDefinitionUpdateData);

}
