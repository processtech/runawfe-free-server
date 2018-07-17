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

import javax.servlet.http.HttpServletRequest;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.DeleteDataSourceAction;
import ru.runa.af.web.html.DataSourceTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesDataSource;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "dataSourceList")
public class DataSourceListTag extends TitledFormTag {

    private static final long serialVersionUID = -4263750161023575386L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setName("dataSourceList");
        getForm().setID("dataSourceList");
        getForm().setAction(
                ((HttpServletRequest) pageContext.getRequest()).getContextPath() + DeleteDataSourceAction.DELETE_DATA_SOURCE_ACTION_PATH + ".do");
        getForm().setMethod("post");
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, "1"));
        tdFormElement.addElement(new DataSourceTableBuilder(pageContext).build());
    }

    @Override
    protected String getTitle() {
        return MessagesDataSource.TITLE_DATA_SOURCES.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeleteDataSourceAction.DELETE_DATA_SOURCE_ACTION_PATH;
    }

    @Override
    public boolean isSubmitButtonEnabled() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_DATA_SOURCE_PARAMETER;
    }

}
