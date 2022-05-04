package ru.runa.wfe.definition.validation;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

/**
 * Contains information about old and new definition for process definition compatibility check.
 * 
 * @author azyablin
 */
@RequiredArgsConstructor
@Getter
@Setter
public class DeploymentUpdateData {
    private final ProcessDefinition oldDefinition;
    private final ProcessDefinition newDefinition;
    private final List<Process> processes;
}
