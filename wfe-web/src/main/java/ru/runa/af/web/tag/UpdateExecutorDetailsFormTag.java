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
package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateExecutorDetailsAction;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.SystemExecutors;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateExecutorDetailsForm")
public class UpdateExecutorDetailsFormTag extends UpdateExecutorBaseFormTag {
    private static final long serialVersionUID = 9096797376521541558L;

    @Override
    public void fillFormData(TD tdFormElement) {
        boolean isCheckboxInputDisaabled = !isFormButtonEnabled();
        ExecutorTableBuilder builder = new ExecutorTableBuilder(getExecutor(), isCheckboxInputDisaabled, pageContext);
        tdFormElement.addElement(builder.buildTable());
    }

    @Override
    protected boolean isFormButtonEnabled() {
        if (SystemExecutors.PROCESS_STARTER_NAME.equals(getExecutor().getName())) {
            return false;
        }
        return super.isFormButtonEnabled();
    }

    @Override
    protected Permission getPermission() {
        return ExecutorPermission.UPDATE;
    }

    @Override
    public String getFormButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTOR_DETAILS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateExecutorDetailsAction.ACTION_PATH;
    }
}
