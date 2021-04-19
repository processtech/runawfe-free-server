package ru.runa.wfe.definition.validation;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.lang.ParsedProcessDefinition;

/**
 * @author azyablin
 */
@Component
public class DefinitionUpdateValidatorManager {
    private static final Log log = LogFactory.getLog(DefinitionUpdateValidatorManager.class);
    @Autowired
    private List<DefinitionUpdateValidator> validators;
    @Autowired
    private CurrentProcessDao currentProcessDao;

    public void validate(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition newDefinition) {
        if (!SystemProperties.isDefinitionCompatibilityCheckEnabled()) {
            return;
        }
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        timeMeasurer.jobStarted();
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(oldDefinition.getName());
        filter.setDefinitionVersion(oldDefinition.getProcessDefinitionVersion().getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        timeMeasurer.jobEnded("Loading " + processes.size() + " active processes");
        timeMeasurer.jobStarted();
        validate(oldDefinition, newDefinition, processes);
        timeMeasurer.jobEnded("Validation of " + oldDefinition);
    }

    public void validate(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition newDefinition, CurrentProcess process) {
        if (!SystemProperties.isDefinitionCompatibilityCheckEnabled()) {
            return;
        }
        validate(oldDefinition, newDefinition, Collections.singletonList(process));
    }

    private void validate(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition newDefinition, List<CurrentProcess> processes) {
        DeploymentUpdateData deploymentUpdateData = new DeploymentUpdateData(oldDefinition, newDefinition, processes);
        for (DefinitionUpdateValidator validator : validators) {
            validator.validate(deploymentUpdateData);
        }
    }

}
