package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.Element;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.DefinitionCategoriesIterator;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.action.UpgradeProcessesToDefinitionVersionAction;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "redeployDefinitionForm")
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    public static final String TYPE_UPDATE_CURRENT_VERSION = "updateCurrentVersion";
    public static final String TYPE_DAYS_BEFORE_ARCHIVING = "daysBeforeArchiving";

    private static final long serialVersionUID = 5106903896165128752L;
    private static RedeployDefinitionFormTag instance;

    protected void fillTD(TD tdFormElement, Form form, WfDefinition def, User user, PageContext pageContext) {
        form.setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true, "." + FileDataProvider.PAR_FILE);
        table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_DEFINITIONS_ARCHIVE.message(pageContext), fileInput));

        DefinitionCategoriesIterator iterator = new DefinitionCategoriesIterator(user);
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, def == null ? null : def.getCategories(), pageContext);
        table.addElement(HTMLUtils.createRow(Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.TYPE, pageContext),
                hierarchyType));

        Integer secondsBeforeArchiving = def == null ? null : def.getSecondsBeforeArchiving();
        String daysBeforeArchiving = secondsBeforeArchiving == null ? "" : Integer.toString(secondsBeforeArchiving / 86400);
        table.addElement(HTMLUtils.createRow(
                MessagesProcesses.LABEL_DEFINITIONS_DAYS_BEFORE_ARCHIVING.message(pageContext),
                new Input(Input.TEXT, TYPE_DAYS_BEFORE_ARCHIVING, daysBeforeArchiving).setStyle("width:100px")
        ));

        table.addElement(HTMLUtils.createCheckboxRow(MessagesProcesses.LABEL_UPDATE_CURRENT_VERSION.message(pageContext),
                TYPE_UPDATE_CURRENT_VERSION, false, true, false));

        tdFormElement.addElement(table);

        if (SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            WfDefinition wfDefinition = Delegates.getDefinitionService().getProcessDefinition(user, getIdentifiableId());

            TR upgradeProcessesTR = new TR();
            table.addElement(upgradeProcessesTR);
            Element upgradeProcessesElement = addUpgradeProcessesLink(wfDefinition);
            upgradeProcessesTR.addElement(new TD(upgradeProcessesElement).setColSpan(2).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition(), getUser(), pageContext);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_REDEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.TITLE_REDEPLOY_DEFINITION.message(pageContext);
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
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, getDefinition());
    }

    private Element addUpgradeProcessesLink(WfDefinition definition) {
        Div div = new Div();
        String url = Commons.getActionUrl(UpgradeProcessesToDefinitionVersionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getVersionId(),
                pageContext, PortletUrlType.Render);
        A upgradeProcessLink = new A(url, MessagesProcesses.PROCESSES_UPGRADE_TO_DEFINITION_VERSION.message(pageContext));
        upgradeProcessLink.addAttribute("data-definitionName", definition.getName());
        upgradeProcessLink.addAttribute("data-definitionVersion", definition.getVersion());
        upgradeProcessLink.setOnClick("selectProcessUpgrageVersionDialog(this); return false;");
        div.addElement(upgradeProcessLink);
        return div;
    }

    public static RedeployDefinitionFormTag getInstance() {
        if (instance == null) {
            instance = new RedeployDefinitionFormTag();
        }
        return instance;
    }
}
