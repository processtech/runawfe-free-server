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
package ru.runa.wf.web.tag;

import static ru.runa.wf.web.action.BaseDeployProcessDefinitionAction.TYPE_ATTRIBUTES;
import static ru.runa.wf.web.action.BaseDeployProcessDefinitionAction.TYPE_DEFAULT;
import static ru.runa.wf.web.action.BaseDeployProcessDefinitionAction.TYPE_SEL;
import static ru.runa.wf.web.action.BaseDeployProcessDefinitionAction.TYPE_TYPE;
import static ru.runa.wf.web.action.BaseDeployProcessDefinitionAction.TYPE_UPDATE_CURRENT_VERSION;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "redeployDefinitionForm" body-content = "empty"
 */
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 5106903896165128752L;

    private static Select getTypeSelectElement(String selectedValue, String[] definitionTypes, User user, PageContext pageContext) {
        ProcessTypesIterator iter = new ProcessTypesIterator(user);
        Select select = new Select(TYPE_SEL);
        select.setID("processDefinitionTypeSelect");
        {
            Option option = new Option();
            option.addElement(Messages.getMessage("batch_presentation.process.no_type", pageContext));
            option.setValue(TYPE_DEFAULT);
            if (TYPE_DEFAULT.equals(selectedValue)) {
                option.setSelected(true);
            }
            select.addElement(option);
        }
        int idx = 0;
        while (iter.hasNext()) {
            String[] type = iter.next();

            StringBuilder typeBuild = new StringBuilder();
            for (int i = 1; i < type.length; ++i) {
                typeBuild.append(Entities.NBSP).append(Entities.NBSP).append(Entities.NBSP);
            }
            typeBuild.append(type[type.length - 1]);
            Option option = new Option();
            option.setValue(Integer.toString(idx));
            option.addElement(typeBuild.toString());
            if ((selectedValue == null && Arrays.equals(type, definitionTypes)) || Integer.toString(idx).equals(selectedValue)) {
                option.setSelected(true);
            }
            select.addElement(option);
            ++idx;
        }
        return select;
    }

    public static void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext) {
        form.setEncType(Form.ENC_UPLOAD);

        String selectedValue = definitionTypes == null ? TYPE_DEFAULT : null;
        String newTypeName = "";

        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute(TYPE_ATTRIBUTES);
        if (attr != null) {
            selectedValue = attr.get(TYPE_SEL);
            newTypeName = attr.get(TYPE_TYPE);
        }

        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(HTMLUtils.createInputRow(Messages.getMessage("process_definition.archive", pageContext), FileForm.FILE_INPUT_NAME, "", true,
                true, Input.FILE));

        TD td = new TD();
        Select select = getTypeSelectElement(selectedValue, definitionTypes, user, pageContext);
        td.addElement(select);
        td.addElement(Entities.NBSP);
        Input typeInput = new Input(Input.TEXT, TYPE_TYPE, String.valueOf(newTypeName));
        typeInput.setID("processDefinitionTypeName");
        typeInput.setStyle("width: 300px;");
        if (!TYPE_DEFAULT.equals(selectedValue)) {
            typeInput.setDisabled(true);
        } else {
            typeInput.setClass(Resources.CLASS_REQUIRED);
        }
        td.addElement(typeInput);
        table.addElement(HTMLUtils.createRow(Messages.getMessage("batch_presentation.process_definition.process_type", pageContext), td));
        tdFormElement.addElement(table);

        table.addElement(HTMLUtils.createCheckboxRow(
                Messages.getMessage("batch_presentation.process_definition.update_current_version", pageContext), TYPE_UPDATE_CURRENT_VERSION, false,
                true, false));
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext);
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.REDEPLOY_DEFINITION;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    @Override
    public String getAction() {
        return RedeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), DefinitionPermission.REDEPLOY_DEFINITION, getIdentifiable());
    }
}
