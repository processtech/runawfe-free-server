package ru.runa.wfe.definition.validation;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.lang.ProcessDefinition;

/**
 * @author azyablin
 */
@Component
public class DefinitionUpdateValidatorManager {
    private static final Log log = LogFactory.getLog(DefinitionUpdateValidatorManager.class);
    @Autowired
    private List<DefinitionUpdateValidator> validators;
    @Autowired
    private ProcessDao processDao;

    public void validate(ProcessDefinition oldDefinition, ProcessDefinition newDefinition) {
        if (!SystemProperties.isDefinitionCompatibilityCheckEnabled()) {
            return;
        }

        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(oldDefinition.getName());
        filter.setDefinitionVersion(oldDefinition.getDeployment().getVersion());
        filter.setFinished(false);
        List<Process> processes = processDao.getProcesses(filter);
        timeMeasurer.jobEnded("Loading " + processes.size() + " active processes");
        timeMeasurer.jobStarted();

        int processesLimit = SystemProperties.getDefinitionCompatibilityCheckProcessesLimit();
        if (processesLimit == -1 || processesLimit >= processes.size()) {
            validate(oldDefinition, newDefinition, processes);
        } else {
            validate(oldDefinition, newDefinition, processes.subList(0, processesLimit));
        }

        timeMeasurer.jobEnded("Validation of " + oldDefinition);
    }

    public void validate(ProcessDefinition oldDefinition, ProcessDefinition newDefinition, Process process) {
        if (!SystemProperties.isDefinitionCompatibilityCheckEnabled()) {
            return;
        }
        validate(oldDefinition, newDefinition, Collections.singletonList(process));
    }

    private void validate(ProcessDefinition oldDefinition, ProcessDefinition newDefinition, List<Process> processes) {
        DeploymentUpdateData deploymentUpdateData = new DeploymentUpdateData(oldDefinition, newDefinition, processes);
        for (DefinitionUpdateValidator validator : validators) {
            validator.validate(deploymentUpdateData);
        }
    }

}
