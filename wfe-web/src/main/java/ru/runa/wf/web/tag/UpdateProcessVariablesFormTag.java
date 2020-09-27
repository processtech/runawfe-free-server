package ru.runa.wf.web.tag;

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
import ru.runa.wf.web.action.UpdateProcessVariableAction;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessVariables")
public class UpdateProcessVariablesFormTag extends TitledFormTag {

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
        WfProcess process = Delegates.getExecutionService().getProcess(getUser(), getProcessId());
        List<VariableDefinition> variables = Delegates.getDefinitionService().getVariableDefinitions(getUser(), process.getDefinitionVersionId());
        if (!variables.isEmpty()) {
            if (WebResources.isUpdateProcessVariablesEnabled() && Delegates.getExecutorService().isAdministrator(getUser())) {
                getForm().setEncType(Form.ENC_UPLOAD);
                String labelTDWidth = "150px";

                Table table = new Table();
                table.setClass(Resources.CLASS_LIST_TABLE);
                tdFormElement.addElement(table);

                TR variableComboboxTr = new TR();
                variableComboboxTr.setClass("variableSelect");

                TD labelTd = new TD();
                Label labelVariable = new Label("variableSel");
                labelVariable.addElement(MessagesProcesses.LABEL_VARIABLE.message(pageContext) + "&nbsp;");
                labelTd.addElement(labelVariable);
                labelTd.setWidth(labelTDWidth);
                variableComboboxTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));
                Select variablesSelect = new Select("variableSelect");
                variablesSelect.setID("variableSelect");
                for (VariableDefinition variable : variables) {
                    variablesSelect.addElement(HTMLUtils.createOption(variable.getName(), false));
                }

                TD selectTd = new TD();
                selectTd.addElement(variablesSelect);
                variableComboboxTr.addElement(selectTd.setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(variableComboboxTr);

                TR variableScriptingValueTr = new TR();

                labelTd = new TD();
                Label labelScriptingValue = new Label("variableScriptingValue");
                labelScriptingValue.addElement(MessagesProcesses.LABEL_VARIABLE_SCRIPTING_VALUE.message(pageContext) + ":&nbsp;");
                labelTd.addElement(labelScriptingValue);
                labelTd.setWidth(labelTDWidth);
                variableScriptingValueTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                TD scriptingValueTd = new TD();
                Div variableScriptingInfoDiv = new Div();
                variableScriptingInfoDiv.setID("variableScriptingInfo");
                scriptingValueTd.addElement(variableScriptingInfoDiv);
                variableScriptingValueTr.addElement(scriptingValueTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                table.addElement(variableScriptingValueTr);

                TR variableCurrentValueTr = new TR();

                labelTd = new TD();
                Label labelCurrentValue = new Label("variableCurrentValue");
                labelCurrentValue.addElement(MessagesProcesses.LABEL_VARIABLE_OLD_VALUE.message(pageContext) + ":&nbsp;");
                labelTd.addElement(labelCurrentValue);
                labelTd.setWidth(labelTDWidth);
                variableCurrentValueTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                TD currentValueTd = new TD();
                Div variableCurrentInfoDiv = new Div();
                variableCurrentInfoDiv.setID("variableCurrentInfo");
                currentValueTd.addElement(variableCurrentInfoDiv);
                variableCurrentValueTr.addElement(currentValueTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                table.addElement(variableCurrentValueTr);

                TR variableInputTr = new TR();
                variableInputTr.setClass("variableInputRegion");

                labelTd = new TD();
                Label labelInputValue = new Label("variableInput");
                labelInputValue.addElement(MessagesProcesses.LABEL_VARIABLE_NEW_VALUE.message(pageContext) + ":&nbsp;");
                labelTd.addElement(labelInputValue);
                labelTd.setWidth(labelTDWidth);
                variableInputTr.addElement(labelTd.setClass(Resources.CLASS_LIST_TABLE_TD));

                TD inputTd = new TD();
                Div inputDiv = new Div();
                inputDiv.setID("variableInput");
                inputTd.addElement(inputDiv);

                Input nullValue = new Input(Input.CHECKBOX, "isNullValue");
                nullValue.setID("nullValueCheckbox");
                nullValue.setChecked(false);
                inputTd.addElement(nullValue);

                Label labelNullValue = new Label("isNullValue");
                labelNullValue.setID("nullValueLabel");
                labelNullValue.addElement(MessagesProcesses.LABEL_VARIABLE_NULL_VALUE.message(pageContext) + "&nbsp;");
                inputTd.addElement(labelNullValue);

                variableInputTr.addElement(inputTd.setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(variableInputTr);
            }
        } else {
            Label variablesExist = new Label("variables");
            variablesExist.addElement(MessagesProcesses.LABEL_NO_VARIABLES.message(pageContext) + "&nbsp;");
            tdFormElement.addElement(variablesExist);
        }
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.BUTTON_UPDATE_VARIABLE.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateProcessVariableAction.ACTION_PATH + "?id=" + processId;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_UPDATE_VARIABLE.message(pageContext);
    }

}
