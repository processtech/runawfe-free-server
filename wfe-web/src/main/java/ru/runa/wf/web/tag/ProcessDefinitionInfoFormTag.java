package ru.runa.wf.web.tag;

import java.util.HashMap;
import java.util.Map;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.LoadProcessDefinitionArchiveAction;
import ru.runa.wf.web.action.SetProcessDefinitionSubprocessBindingDateAction;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.Permission;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processDefinitionInfoForm")
public class ProcessDefinitionInfoFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7118850164438509260L;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfDefinition definition = getDefinition();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD nameTD = new TD();
        nameTD.setClass(Resources.CLASS_LIST_TABLE_TD);
        nameTD.addElement(definition.getName() + " (");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ListDefinitionsHistoryFormTag.NAME_PARAMETER, definition.getName());
        parameters.put(PagingNavigationHelper.PAGE_PARAMETER, PagingNavigationHelper.FIRST_PAGE);
        String historyUrl = Commons.getActionUrl(ListDefinitionsHistoryFormTag.ACTION_PATH, parameters, pageContext, PortletUrlType.Render);
        nameTD.addElement(new A(historyUrl, MessagesProcesses.TITLE_DEFINITIONS_HISTORY.message(pageContext)));
        nameTD.addElement(")");
        nameTR.addElement(nameTD);

        TR versionTR = new TR();
        table.addElement(versionTR);
        String versionName = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.VERSION, pageContext);
        versionTR.addElement(new TD(versionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD versionTD = new TD();
        versionTD.addElement(definition.getVersion() + " (");
        String downloadUrl = Commons.getActionUrl(LoadProcessDefinitionArchiveAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                pageContext, PortletUrlType.Render);
        versionTD.addElement(new A(downloadUrl, MessagesOther.LABEL_EXPORT.message(pageContext)));
        versionTD.addElement(")");
        versionTR.addElement(versionTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR createdDateTR = new TR();
        table.addElement(createdDateTR);
        String createDateMessage = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.CREATE_DATE, pageContext);
        createdDateTR.addElement(new TD(createDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
        createdDateTR.addElement(new TD(CalendarUtil.formatDateTime(definition.getCreateDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR createdByTR = new TR();
        table.addElement(createdByTR);
        String createdByMessage = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.CREATE_ACTOR, pageContext);
        createdByTR.addElement(new TD(createdByMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
        String createdBy = definition.getCreateActor() != null ? definition.getCreateActor().getFullName() : "";
        createdByTR.addElement(new TD(createdBy).setClass(Resources.CLASS_LIST_TABLE_TD));

        if (definition.getUpdateDate() != null) {
            TR updateDateTR = new TR();
            table.addElement(updateDateTR);
            String updateDateMessage = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.UPDATE_DATE, pageContext);
            updateDateTR.addElement(new TD(updateDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
            updateDateTR.addElement(new TD(CalendarUtil.formatDateTime(definition.getUpdateDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

            TR updatedByTR = new TR();
            table.addElement(updatedByTR);
            String updatedByMessage = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.UPDATE_ACTOR, pageContext);
            updatedByTR.addElement(new TD(updatedByMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
            String updatedBy = definition.getUpdateActor() != null ? definition.getUpdateActor().getFullName() : "";
            updatedByTR.addElement(new TD(updatedBy).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        TR descriptionTR = new TR();
        table.addElement(descriptionTR);
        String description = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.DESCRIPTION, pageContext);
        descriptionTR.addElement(new TD(description).setClass(Resources.CLASS_LIST_TABLE_TD));
        descriptionTR.addElement(new TD(definition.getDescription()).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR subprocessBindingDateTr = new TR();
        table.addElement(subprocessBindingDateTr);
        String updateDateMessage = Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.SUBPROCESS_BINDING_DATE,
                pageContext);
        subprocessBindingDateTr.addElement(new TD(updateDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD subprocessBindingDateTd = new TD();
        subprocessBindingDateTd.setClass(Resources.CLASS_LIST_TABLE_TD + " subprocessBindingDate");
        Span displayDataSpan = new Span();
        if (definition.getSubprocessBindingDate() != null) {
            displayDataSpan.addElement(CalendarUtil.formatDateTime(definition.getSubprocessBindingDate()));
        }
        displayDataSpan.setClass("displayData");
        subprocessBindingDateTd.addElement(displayDataSpan);
        Span editDataSpan = new Span();
        editDataSpan.addElement(HTMLUtils.createInput(Input.HIDDEN, IdForm.ID_INPUT_NAME, definition.getId().toString()));
        Input input = HTMLUtils.createInput("subprocessBindingDate", CalendarUtil.formatDateTime(definition.getSubprocessBindingDate()));
        input.setClass("inputDateTime");
        input.setStyle("width: 150px;");
        editDataSpan.addElement(input);
        editDataSpan.setClass("editData");
        editDataSpan.setStyle("display: none;");
        subprocessBindingDateTd.addElement(editDataSpan);
        subprocessBindingDateTd.addElement(" (");
        Span buttonsSpan = new Span();
        buttonsSpan.setClass("buttons");
        subprocessBindingDateTd.addElement(buttonsSpan);
        A changeSubprocessBindingDateLink = new A("javascript:void(0);", MessagesCommon.BUTTON_CHANGE.message(pageContext));
        changeSubprocessBindingDateLink.setClass("change");
        buttonsSpan.addElement(changeSubprocessBindingDateLink);
        A saveSubprocessBindingDateLink = new A("javascript:void(0);", MessagesCommon.BUTTON_APPLY.message(pageContext));
        saveSubprocessBindingDateLink.setClass("apply");
        saveSubprocessBindingDateLink.setStyle("display: none;");
        buttonsSpan.addElement(saveSubprocessBindingDateLink);
        A cancelSubprocessBindingDateLink = new A("javascript:void(0);", MessagesCommon.BUTTON_CANCEL.message(pageContext));
        cancelSubprocessBindingDateLink.setClass("cancel");
        cancelSubprocessBindingDateLink.setStyle("display: none;");
        buttonsSpan.addElement(cancelSubprocessBindingDateLink);
        subprocessBindingDateTd.addElement(buttonsSpan);
        subprocessBindingDateTd.addElement(")");
        subprocessBindingDateTr.addElement(subprocessBindingDateTd);
    }

    @Override
    public String getAction() {
        return SetProcessDefinitionSubprocessBindingDateAction.ACTION_PATH;
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION.message(pageContext);
    }
}
