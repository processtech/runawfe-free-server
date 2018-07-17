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

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.DeployDataSourceAction;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesDataSource;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployDataSource")
public class DeployDataSourceTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isFormButtonEnabled() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    protected String getFormButtonName() {
        return MessagesDataSource.BUTTON_DEPLOY_DATA_SOURCE.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesDataSource.BUTTON_DEPLOY_DATA_SOURCE.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeployDataSourceAction.ACTION_PATH;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        Input fileUploadInput = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
        fileUploadInput.setClass(Resources.CLASS_REQUIRED);
        tdFormElement.addElement(fileUploadInput);
    }
}
