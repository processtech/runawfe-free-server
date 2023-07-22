package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.DefinitionCategoriesIterator;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created 26.05.2014 bulkDeployDefinitionControlForm
 * 
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "bulkDeployDefinitionForm")
public class BulkDeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 5106978254165128752L;
    public static final String TYPE_APPLYIES_TO_NEW_PROCESSES = "newProcesses";
    public static final String TYPE_APPLYIES_TO_ALL_PROCESSES = "allProcesses";

    public static void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext, WebHelper strutsWebHelper) {
        form.setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(createFileInputRow(MessagesProcesses.LABEL_DEFINITIONS_ARCHIVE.message(pageContext), FileForm.FILE_INPUT_NAME, "", true,
                true, Input.FILE, strutsWebHelper));
        DefinitionCategoriesIterator iterator = new DefinitionCategoriesIterator(user);
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, definitionTypes, pageContext);
        table.addElement(HTMLUtils.createRow(Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.TYPE, pageContext),
                hierarchyType));
        tdFormElement.addElement(table);

        TR applicationTypeTr = new TR();
        TD labelTd = new TD(MessagesProcesses.LABEL_DEPLOY_APPLY_TYPE.message(pageContext));
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        applicationTypeTr.addElement(labelTd);

        TD td = new TD();
        Input applyingNewProcessInput = new Input(Input.RADIO, RedeployDefinitionFormTag.TYPE_UPDATE_CURRENT_VERSION, TYPE_APPLYIES_TO_NEW_PROCESSES);
        applyingNewProcessInput.setID(TYPE_APPLYIES_TO_NEW_PROCESSES);
        applyingNewProcessInput.setChecked(true);
        td.addElement(applyingNewProcessInput);
        Label label = new Label(TYPE_APPLYIES_TO_NEW_PROCESSES);
        label.addElement(new StringElement(MessagesProcesses.LABEL_DEPLOY_APPLY_NEW.message(pageContext)));
        td.addElement(label);
        td.addElement(new BR());
        Input applyingAllProcessInput = new Input(Input.RADIO, RedeployDefinitionFormTag.TYPE_UPDATE_CURRENT_VERSION, TYPE_APPLYIES_TO_ALL_PROCESSES);
        applyingAllProcessInput.setID(TYPE_APPLYIES_TO_ALL_PROCESSES);
        td.addElement(applyingAllProcessInput);
        label = new Label(TYPE_APPLYIES_TO_ALL_PROCESSES);
        label.addElement(new StringElement(MessagesProcesses.LABEL_DEPLOY_APPLY_ALL.message(pageContext)));
        td.addElement(label);

        applicationTypeTr.addElement(td);
        table.addElement(applicationTypeTr);
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext, new StrutsWebHelper(pageContext));
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return isVisible();
    }

    @Override
    protected Permission getSubmitPermission() {
        throw new IllegalAccessError();
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESSES.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.TITLE_PROCESSES.message(pageContext);
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
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM);
    }

    private static TR createFileInputRow(String label, String name, String value, boolean enabled, boolean required, String type,
            WebHelper strutsWebHelper) {
        TR tr = new TR();
        TD labelTd = new TD(label);
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        tr.addElement(labelTd);
        String fileInput = ViewUtil.getFileInput(strutsWebHelper, name, true, "." + FileDataProvider.PAR_FILE);
        tr.addElement(new TD(fileInput).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }
}
