package ru.runa.wfe.definition.validation;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.ParsedProcessDefinition;

/**
 * Contains information about old and new definition for process definition compatibility check.
 * 
 * @author azyablin
 */
@RequiredArgsConstructor
@Getter
@Setter
public class DeploymentUpdateData {
    private final ParsedProcessDefinition oldDefinition;
    private final ParsedProcessDefinition newDefinition;
    private final List<CurrentProcess> processes;
}
