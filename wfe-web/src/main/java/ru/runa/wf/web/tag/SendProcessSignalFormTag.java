package ru.runa.wf.web.tag;

import org.apache.ecs.html.Button;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.SendProcessSignalAction;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "sendProcessSignalForm")
public class SendProcessSignalFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getTitle() {
        return MessagesCommon.MAIN_MENU_ITEM_SEND_SIGNAL.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.MAIN_MENU_ITEM_SEND_SIGNAL.message(pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        fillFormData(tdFormElement);
    }

    protected void fillFormData(TD tdFormElement) {
        Button pasteButton = new Button();
        pasteButton.setID("pasteButton");
        pasteButton.setOnClick("javascript:pasteRows();");
        pasteButton.setType("button");
        pasteButton.addElement(MessagesProcesses.PASTE_SIGNAL_DATA_BUTTON_NAME.message(pageContext));
        tdFormElement.addElement(pasteButton);
        addTable(tdFormElement, "routing", MessagesProcesses.ROUTING_PARAMETER_NAME, MessagesProcesses.ROUTING_PARAMETER_VALUE, "addRow('routing')");
        addTable(tdFormElement, "payload", MessagesProcesses.PAYLOAD_PARAMETER_NAME, MessagesProcesses.PAYLOAD_PARAMETER_VALUE, "addRow('payload')");
    }

    private void addTable(TD tdFormElement, String tableId, StrutsMessage paramName, StrutsMessage paramValue, String onclick) {
        Table table = new Table();
        table.setID(tableId);
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);
        TR tr = new TR();
        table.addElement(tr);
        tr.addElement(new TH(paramName.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(paramValue.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));

        TR addTr = new TR();
        Button button = new Button();
        button.setOnClick("javascript:" + onclick);
        button.setType("button");
        button.addElement("+");
        TD addTd = new TD(button);
        addTd.setClass("list");
        addTr.addElement(addTd);
        table.addElement(addTr);
    }


    @Override
    public String getAction() {
        return SendProcessSignalAction.ACTION_PATH;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return true;
    }
}
