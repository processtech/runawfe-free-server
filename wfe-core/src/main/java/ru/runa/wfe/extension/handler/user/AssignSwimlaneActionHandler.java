/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.extension.handler.user;

import java.util.List;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;

public class AssignSwimlaneActionHandler extends ActionHandlerBase {
    private static final String STRICT_MODE = "strictMode";
    private static final String SWIMLANE_NAME = "swimlaneName";
    private static final String SWIMLANE_INITITALIZER = "swimlaneInititalizer";
    private boolean strictMode = true;
    private String swimlaneName;
    private String swimlaneInitializer;
    @Autowired
    private SwimlaneDao swimlaneDao;

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
            Swimlane swimlane = swimlaneDao.findOrCreateInitialized(executionContext, swimlaneDefinition, true);
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
        Swimlane swimlane = swimlaneDao.findOrCreate(executionContext.getProcess(), swimlaneDefinition);
        return AssignmentHelper.assign(executionContext, swimlane, executors);
    }

}
