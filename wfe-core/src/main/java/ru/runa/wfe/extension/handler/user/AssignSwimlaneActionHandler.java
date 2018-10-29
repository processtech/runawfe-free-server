package ru.runa.wfe.extension.handler.user;

import com.google.common.base.Preconditions;
import java.util.List;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentSwimlaneDao;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.user.Executor;

public class AssignSwimlaneActionHandler extends ActionHandlerBase {
    private static final String STRICT_MODE = "strictMode";
    private static final String SWIMLANE_NAME = "swimlaneName";
    private static final String SWIMLANE_INITITALIZER = "swimlaneInititalizer";
    private boolean strictMode = true;
    private String swimlaneName;
    private String swimlaneInitializer;

    @Autowired
    private CurrentSwimlaneDao currentSwimlaneDao;

    @Override
    public void setConfiguration(String configuration) {
        super.setConfiguration(configuration);
        Element root = XmlUtils.parseWithoutValidation(configuration).getRootElement();
        String strictModeString = root.attributeValue(STRICT_MODE);
        if (strictModeString != null) {
            strictMode = Boolean.valueOf(strictModeString);
        }
        swimlaneName = root.attributeValue(SWIMLANE_NAME);
        if (swimlaneName == null) {
            swimlaneName = root.elementTextTrim(SWIMLANE_NAME);
            Preconditions.checkNotNull(swimlaneName, SWIMLANE_NAME);
        }
        swimlaneInitializer = root.attributeValue(SWIMLANE_INITITALIZER);
        if (swimlaneInitializer == null) {
            swimlaneInitializer = root.elementTextTrim(SWIMLANE_INITITALIZER);
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        boolean assigned;
        if (Utils.isNullOrEmpty(swimlaneInitializer)) {
            log.debug("using process definition swimlane initializer");
            SwimlaneDefinition swimlaneDefinition = executionContext.getParsedProcessDefinition().getSwimlaneNotNull(swimlaneName);
            CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreateInitialized(executionContext, swimlaneDefinition, true);
            assigned = swimlane.getExecutor() != null;
        } else {
            log.debug("using handler swimlane initializer");
            assigned = assignSwimlane(executionContext, swimlaneName, swimlaneInitializer);
        }
        if (strictMode && !assigned) {
            throw new Exception("Swimlane " + swimlaneName + " is not assigned");
        }
    }

    private boolean assignSwimlane(ExecutionContext executionContext, String swimlaneName, String swimlaneInitializer) {
        List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(swimlaneInitializer, executionContext.getVariableProvider());
        SwimlaneDefinition swimlaneDefinition = executionContext.getParsedProcessDefinition().getSwimlaneNotNull(swimlaneName);
        CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(executionContext.getCurrentProcess(), swimlaneDefinition);
        return AssignmentHelper.assign(executionContext, swimlane, executors);
    }
}
