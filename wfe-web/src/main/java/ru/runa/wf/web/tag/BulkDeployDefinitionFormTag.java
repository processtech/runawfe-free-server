package ru.runa.wf.web.tag;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created 26.05.2014 bulkDeployDefinitionControlForm
 * 
 */
public class BulkDeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 5106978254165128752L;
    private static final String TYPE_DEFAULT = "_default_type_";
    private static final String TYPE_APPLYING = "typeApplying";
    public static final String TYPE_APPLYIES_TO_NEW_PROCESSES = "newProcesses";
    public static final String TYPE_APPLYIES_TO_ALL_PROCESSES = "allProcesses";

    private static Select getTypeSelectElement(String selectedValue, String[] definitionTypes, User user, PageContext pageContext) {
        ProcessTypesIterator iter = new ProcessTypesIterator(user);
        Select select = new Select("typeSel");
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

    public static void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext, WebHelper strutsWebHelper) {
        form.setEncType(Form.ENC_UPLOAD);

        String selectedValue = definitionTypes == null ? TYPE_DEFAULT : null;
        String newTypeName = "";
        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute("TypeAttributes");
        if (attr != null) {
            selectedValue = attr.get("typeSel");
            newTypeName = attr.get("type");
        }

        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(createFileInputRow(Messages.getMessage("process_definition.archive", pageContext), FileForm.FILE_INPUT_NAME, "", true, true,
                Input.FILE, strutsWebHelper));
        TD td = new TD();
        Select select = getTypeSelectElement(selectedValue, definitionTypes, user, pageContext);
        td.addElement(select);
        td.addElement(Entities.NBSP);
        Input typeInput = new Input(Input.TEXT, "type", String.valueOf(newTypeName));
        typeInput.setID("processDefinitionTypeName");
        typeInput.setStyle("width: 300px;");
        if (!TYPE_DEFAULT.equals(selectedValue)) {
            typeInput.setDisabled(true);
        }
        typeInput.setClass(Resources.CLASS_REQUIRED);
        td.addElement(typeInput);
        table.addElement(HTMLUtils.createRow(Messages.getMessage("batch_presentation.process_definition.process_type", pageContext), td));
        tdFormElement.addElement(table);

        TR applicationTypeTr = new TR();
        TD labelTd = new TD(Messages.getMessage("batch_presentation.process_definition.application_type", pageContext));
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        applicationTypeTr.addElement(labelTd);

        td = new TD();
        Input applyingNewProcessInput = new Input(Input.RADIO, TYPE_APPLYING, TYPE_APPLYIES_TO_NEW_PROCESSES);
        applyingNewProcessInput.setID(TYPE_APPLYIES_TO_NEW_PROCESSES);
        applyingNewProcessInput.setChecked(true);
        td.addElement(applyingNewProcessInput);
        Label label = new Label(TYPE_APPLYIES_TO_NEW_PROCESSES);
        label.addElement(new StringElement(Messages.getMessage("batch_presentation.process_definition.application_type.new.label", pageContext)));
        td.addElement(label);
        td.addElement(new BR());
        Input applyingAllProcessInput = new Input(Input.RADIO, TYPE_APPLYING, TYPE_APPLYIES_TO_ALL_PROCESSES);
        applyingAllProcessInput.setID(TYPE_APPLYIES_TO_ALL_PROCESSES);
        td.addElement(applyingAllProcessInput);
        label = new Label(TYPE_APPLYIES_TO_ALL_PROCESSES);
        label.addElement(new StringElement(Messages.getMessage("batch_presentation.process_definition.application_type.all.label", pageContext)));
        td.addElement(label);

        applicationTypeTr.addElement(td);
        table.addElement(applicationTypeTr);
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext, new StrutsWebHelper(pageContext));
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

    private static TR createFileInputRow(String label, String name, String value, boolean enabled, boolean required, String type,
            WebHelper strutsWebHelper) {
        TR tr = new TR();
        TD labelTd = new TD(label);
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        tr.addElement(labelTd);

        String fileInput = ViewUtil.getFileInput(strutsWebHelper, name);

        Input input = new Input(type, name, String.valueOf(value));
        input.setDisabled(!enabled);
        input.addAttribute("multiple", true);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }

        tr.addElement(new TD(fileInput).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }
}
