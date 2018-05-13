package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import com.google.common.base.Strings;

import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UpdateProcessSwimlaneAction;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

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
        User user = getUser();
        Long processId = getProcessId();
        List<WfSwimlane> swimlanes = Delegates.getExecutionService().getSwimlanes(user, processId);
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        List<? extends Executor> allowedExecutors = Delegates.getExecutorService().getExecutors(user, batchPresentation);
        if (WebResources.isUpdateProcessSwimlanesEnabled() && Delegates.getExecutorService().isAdministrator(user)) {
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
            for (WfSwimlane swimlane : swimlanes) {
                swimlaneSelect.addElement(HTMLUtils.createOption(swimlane.getDefinition().getName(), swimlane.equals(swimlanes.get(0))));
            }

            TD selectTd = new TD();
            selectTd.addElement(swimlaneSelect);
            swimlanesComboboxTr.addElement(selectTd.setClass(Resources.CLASS_LIST_TABLE_TD));

            table.addElement(swimlanesComboboxTr);

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

            TR newExecutorTr = new TR();
            newExecutorTr.setClass("newExecutor");

            labelTd = new TD();
            Label labelInputNewExecutor = new Label("newExecutorLabel");
            labelInputNewExecutor.addElement(MessagesProcesses.LABEL_SWIMLANE_NEW_EXECUTOR.message(pageContext) + ":&nbsp;");
            labelTd.addElement(labelInputNewExecutor);
            labelTd.setWidth(labelTDWidth);
            newExecutorTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

            TD inputTd = new TD();
            Select newExecutorSelect = new Select("newExecutorSelect");
            newExecutorSelect.setID("newExecutorSelect");
            for (Executor executor : allowedExecutors) {
                String label = executor.getName();
                if (executor instanceof Actor) {
                    label = !Strings.isNullOrEmpty(executor.getFullName()) ? executor.getFullName() : executor.getName();
                } else if (executor instanceof Group) {
                    label = !Strings.isNullOrEmpty(executor.getName()) ? executor.getName() : executor.getFullName();
                }
                newExecutorSelect.addElement(HTMLUtils.createOption(executor.getId().intValue(), label, executor.equals(allowedExecutors.get(0))));
            }
            inputTd.addElement(newExecutorSelect);

            newExecutorTr.addElement(inputTd.setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(newExecutorTr);
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

}
