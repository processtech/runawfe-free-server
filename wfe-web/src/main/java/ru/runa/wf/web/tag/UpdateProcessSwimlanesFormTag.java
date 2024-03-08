package ru.runa.wf.web.tag;

import com.google.common.base.Strings;
import java.util.List;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UpdateProcessSwimlaneAction;
import ru.runa.wf.web.ftl.component.GenerateHtmlForVariable;
import ru.runa.wf.web.servlet.AjaxExecutorsList.Type;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessSwimlanes")
public class UpdateProcessSwimlanesFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    private Long processId;

    @Attribute(required = false, rtexprvalue = true)
    public void setProcessId(Long id) {
        processId = id;
    }

    public Long getProcessId() {
        return processId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        WfProcess process = Delegates.getExecutionService().getProcess(getUser(), processId);
        List<SwimlaneDefinition> swimlaneDefinitions = Delegates.getDefinitionService().getSwimlaneDefinitions(getUser(), process.getDefinitionId());
        if (WebResources.isUpdateProcessSwimlanesEnabled() && Delegates.getExecutorService().isAdministrator(getUser())) {
            getForm().setEncType(Form.ENC_UPLOAD);
            String labelTDWidth = "150px";

            Table table = new Table();
            table.setClass(Resources.CLASS_LIST_TABLE);
            tdFormElement.addElement(table);

            TR swimlanesComboboxTr = new TR();
            swimlanesComboboxTr.setClass("swimlane");

            TD labelTd = new TD();
            Label labelSwimlane = new Label("swimlaneSel");
            labelSwimlane.addElement(MessagesProcesses.LABEL_SWIMLANE.message(pageContext) + ":&nbsp;");
            labelTd.addElement(labelSwimlane);
            labelTd.setWidth(labelTDWidth);
            swimlanesComboboxTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

            Select swimlaneSelect = new Select("swimlaneSelect");
            swimlaneSelect.setID("swimlaneSelect");
            for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
                swimlaneSelect
                        .addElement(HTMLUtils.createOption(swimlaneDefinition.getName(), swimlaneDefinition.equals(swimlaneDefinitions.get(0))));
            }
            TD selectTd = new TD();
            selectTd.addElement(swimlaneSelect);
            swimlanesComboboxTr.addElement(selectTd.setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(swimlanesComboboxTr);

            TR nullSwimlaneTr = new TR();
            nullSwimlaneTr.setClass("nullSwimlane");
            labelTd = new TD();
            Label labelNullSwimlane = new Label("swimlaneNull");
            labelNullSwimlane.addElement(MessagesProcesses.LABEL_VARIABLE_NULL_VALUE.message(pageContext) + ":&nbsp;");
            labelTd.addElement(labelNullSwimlane);
            labelTd.setWidth(labelTDWidth);
            nullSwimlaneTr.addElement(labelTd);

            TD nullCheckboxTd= new TD();
            Input nullValue = new Input(Input.CHECKBOX, "isNullValue");
            nullValue.setID("nullValueCheckbox");
            nullValue.setChecked(false);
            nullCheckboxTd.addElement(nullValue);
            nullSwimlaneTr.addElement(nullCheckboxTd);

            table.addElement(nullSwimlaneTr);

            TR currentExecutorTr = new TR();

            labelTd = new TD();
            Label labelCurrentExecutor = new Label("labelCurrentExecutor");
            labelCurrentExecutor.addElement(MessagesProcesses.LABEL_SWIMLANE_ASSIGNMENT.message(pageContext) + ":&nbsp;");
            labelTd.addElement(labelCurrentExecutor);
            labelTd.setWidth(labelTDWidth);
            currentExecutorTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

            TD currentExecutorTd = new TD();
            Div currentExecutorDiv = new Div();
            currentExecutorDiv.setID("currentExecutor");
            currentExecutorTd.addElement(currentExecutorDiv);
            currentExecutorTr.addElement(currentExecutorTd.setClass(Resources.CLASS_LIST_TABLE_TD));

            table.addElement(currentExecutorTr);

            String newExecutorInput = GenerateHtmlForVariable.createExecutorAutoSelect("newExecutor", Type.executor, false, null);
            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_SWIMLANE_NEW_EXECUTOR.message(pageContext) + ":&nbsp;", 
                    new TD(newExecutorInput)));
        } else {
            Label variablesExist = new Label("swimlanes");
            variablesExist.addElement(MessagesProcesses.LABEL_NO_SWIMLANES.message(pageContext) + "&nbsp;");
            tdFormElement.addElement(variablesExist);
        }
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.BUTTON_UPDATE_SWIMLANE.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateProcessSwimlaneAction.ACTION_PATH + "?id=" + processId;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_UPDATE_SWIMLANE.message(pageContext);
    }
    @Override
    protected boolean isCancelButtonEnabled() {
        return true;
    }
    @Override
    protected String getCancelButtonAction() {
        return "manage_process.do?id=" + getProcessId();
    }
}
