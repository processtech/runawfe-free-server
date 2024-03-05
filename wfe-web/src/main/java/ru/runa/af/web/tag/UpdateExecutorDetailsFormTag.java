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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateExecutorDetailsAction;
import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.user.SystemExecutors;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateExecutorDetailsForm")
public class UpdateExecutorDetailsFormTag extends UpdateExecutorBaseFormTag {
    private static final long serialVersionUID = 9096797376521541558L;
    private String type;

    @Override
    public void fillFormData(TD tdFormElement) {
        boolean isCheckboxInputDisaabled = !isSubmitButtonEnabled();
        ExecutorTableBuilder builder = new ExecutorTableBuilder(getExecutor(), isCheckboxInputDisaabled, pageContext);
        tdFormElement.addElement(builder.buildTable());
        tdFormElement.addElement(createHiddenType());
    }

    private Input createHiddenType() {
        return new Input(Input.HIDDEN, UpdateExecutorDetailsForm.EXECUTOR_TYPE_INPUT_NAME, type);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        if (SystemExecutors.PROCESS_STARTER_NAME.equals(getExecutor().getName())) {
            return false;
        }
        return super.isSubmitButtonEnabled();
    }

    @Override
    public String getSubmitButtonName() {
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

    public String getType() {
        return type;
    }

    @Attribute(required = true)
    public void setType(String type) {
        this.type = type;
    }
}
