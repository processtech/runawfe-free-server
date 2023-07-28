package ru.runa.wf.logic.bot;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Preconditions;

/**
 * (Re)Assigns swimlane.
 *
 * @author dofs
 * @since 2.0
 */
public class SwimlaneAssignerTaskHandler extends TaskHandlerBase {
    private static final String SWIMLANE_NAME_PROPERTY = "swimlaneName";
    private static final String ASSIGNER_FUNCTION_PROPERTY = "assignerFunction";
    private String swimlaneName;
    private String swimlaneInitializer;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        Properties properties = new Properties();
        properties.load(new StringReader(configuration));
        swimlaneName = properties.getProperty(SWIMLANE_NAME_PROPERTY);
        Preconditions.checkNotNull(swimlaneName, SWIMLANE_NAME_PROPERTY);
        swimlaneInitializer = properties.getProperty(ASSIGNER_FUNCTION_PROPERTY);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(swimlaneInitializer, variableProvider);
        Delegates.getExecutionService().assignSwimlane(user, task.getProcessId(), swimlaneName, executors.get(0));
        return null;
    }
}
