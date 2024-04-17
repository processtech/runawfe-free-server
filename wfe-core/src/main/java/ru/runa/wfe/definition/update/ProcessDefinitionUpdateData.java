package ru.runa.wfe.definition.update;

import java.util.Optional;
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
public class ProcessDefinitionUpdateData {
    private final ParsedProcessDefinition oldDefinition;
    private final ParsedProcessDefinition newDefinition;
    private final Optional<CurrentProcess> process;

    public boolean inBatchMode() {
        return !process.isPresent();
    }
}
