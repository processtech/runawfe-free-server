package ru.runa.wf.web.tag;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.SignalUtils;
import ru.runa.wf.web.action.SendProcessSignalAction;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "sendProcessSignalForm")
public class SendProcessSignalFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getTitle() {
        return MessagesProcesses.LABEL_SEND_PROCESS_SIGNAL.message(pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        fillFormData(tdFormElement);
    }


    protected void fillFormData(TD tdFormElement) {
        addTable(tdFormElement, "routing", MessagesProcesses.ROUTING_PARAMETER_NAME, MessagesProcesses.ROUTING_PARAMETER_VALUE, "addRoutingRow()");
        addTable(tdFormElement, "payload", MessagesProcesses.PAYLOAD_PARAMETER_NAME, MessagesProcesses.PAYLOAD_PARAMETER_VALUE, "addPayloadRow()");
        getRoutingJs(tdFormElement);
        getPayloadJs(tdFormElement);
    }

    private void getRoutingJs(TD tdFormElement) {
        String js = "function addRoutingRow() {\n" +
                "  var tableId = 'routing';\n" +
                "  var table = document.getElementById('routing');\n" +
                "  var len = table.getElementsByTagName(\"tr\").length - 1;\n" +
                "  var index = len - 1;\n" +
                "  var row = table.insertRow(len);\n" +
                "  var cell1 = row.insertCell(0);\n" +
                "  var cell2 = row.insertCell(1);\n" +
                "  cell1.setAttribute(\"class\", \"list\");\n" +
                "  cell2.setAttribute(\"class\", \"list\");\n" +
                "  cell1.innerHTML = '<input type=\"TEXT\" name=\"' + tableId + 'Param(' + index + ')\"/>';\n" +
                "  cell2.innerHTML = '<input type=\"TEXT\" name=\"' + tableId + 'Value(' + index + ')\"/>';\n" +
                "}";

        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");
        script.addElement(new StringElement(js));
        tdFormElement.addElement(script);
    }

    private void getPayloadJs(TD tdFormElement) {
        StringBuilder optionsBuilder = new StringBuilder();
        for (String option : SignalUtils.getOptions()) {
            optionsBuilder.append("<option value=\"")
                    .append(option)
                    .append("\"");
            if (option.equals("string")) {
                optionsBuilder.append(" selected");
            }
            optionsBuilder.append(">")
                    .append(option)
                    .append("</option>");

        }
        String js = "function addPayloadRow() {\n" +
                "  var table = document.getElementById('payload');\n" +
                "  var len = table.getElementsByTagName(\"tr\").length - 1;\n" +
                "  var index = len - 1;\n" +
                "  var row = table.insertRow(len);\n" +
                "  var cell1 = row.insertCell(0);\n" +
                "  var cell2 = row.insertCell(1);\n" +
                "  cell1.setAttribute(\"class\", \"list\");\n" +
                "  cell2.setAttribute(\"class\", \"list\");\n" +
                "  cell1.innerHTML = '<input type=\"TEXT\" name=\"payloadParam(' + index + ')\"/>';\n" +
                "  cell2.innerHTML = '<input type=\"TEXT\" style=\"width:200px\" name=\"payloadValue(' + index + ')\"/>" +
                                     " <select name=\"payloadType(' + index + ')\">" +
                                        optionsBuilder.toString() +
                                     " </select> " +
                "';\n" +
                "}";

        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");
        script.addElement(new StringElement(js));
        tdFormElement.addElement(script);
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
        TD addTd = new TD("+");
        addTd.setClass("list");
        addTd.setOnClick(onclick);
        addTr.addElement(addTd);
        table.addElement(addTr);

    }


    @Override
    public String getAction() {
        return SendProcessSignalAction.ACTION_PATH;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }
}
