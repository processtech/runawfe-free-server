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

import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.extension.assign.AssignmentHelper;

import com.google.common.base.Preconditions;

public class AssignSwimlaneActionHandler extends ActionHandlerBase {
    private static final String SWIMLANE_INITITALIZER = "swimlaneInititalizer";
    private static final String SWIMLANE = "swimlaneName";
    private String swimlaneName;
    private String swimlaneInitializer;

    @Override
    public void setConfiguration(String configuration) {
        super.setConfiguration(configuration);
        Element root = XmlUtils.parseWithoutValidation(configuration).getRootElement();
        swimlaneName = root.attributeValue(SWIMLANE);
        if (swimlaneName == null) {
            swimlaneName = root.elementTextTrim(SWIMLANE);
            Preconditions.checkNotNull(swimlaneName, SWIMLANE);
        }
        swimlaneInitializer = root.attributeValue(SWIMLANE_INITITALIZER);
        if (swimlaneInitializer == null) {
            swimlaneInitializer = root.elementTextTrim(SWIMLANE_INITITALIZER);
            Preconditions.checkNotNull(swimlaneInitializer, SWIMLANE_INITITALIZER);
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        AssignmentHelper.assignSwimlane(executionContext, swimlaneName, swimlaneInitializer);
    }
}
