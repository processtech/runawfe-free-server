package ru.runa.wfe.definition.update;

import java.util.Optional;
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
public class ProcessDefinitionUpdateData {
    private final ProcessDefinition oldDefinition;
    private final ProcessDefinition newDefinition;
    private final Optional<Process> process;

    public boolean inBatchMode() {
        return !process.isPresent();
    }

}
