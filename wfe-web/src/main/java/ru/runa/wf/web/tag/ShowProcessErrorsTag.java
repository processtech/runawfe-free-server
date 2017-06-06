package ru.runa.wf.web.tag;

import java.util.List;
import java.util.Map;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.MessagesError;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ActivateFailedProcessesAction;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "viewProcessErrors")
public class ShowProcessErrorsTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }

    @Override
    protected ConcreteElement getEndElement() {
        List<TR> rows = Lists.newArrayList();
        boolean areFailedProcessesExist = false;
        for (ProcessError processError : Delegates.getSystemService().getAllProcessErrors(getUser())) {
            Long processId = processError.getProcessId();
            Map<String, Object> params = Maps.newHashMap();
            params.put(IdForm.ID_INPUT_NAME, processId);
            String processIdUrl = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
            A processIdElement = new A(processIdUrl, processId.toString());
            TR tr = new TR();
            String typeLabel = Messages.getMessage("errors.type." + processError.getType(), pageContext);
            tr.addElement(new TD(typeLabel).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(processIdElement).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(CalendarUtil.formatDateTime(processError.getOccurredDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(processError.getNodeName()).setClass(Resources.CLASS_LIST_TABLE_TD));
            if (processError.getStackTrace() != null) {
                String url = "javascript:showProcessError('" + processError.getType() + "', " + processId + ", '" + processError.getNodeId() + "')";
                tr.addElement(new TD(new A(url, processError.getMessage())).setClass(Resources.CLASS_LIST_TABLE_TD));
            } else {
                tr.addElement(new TD(processError.getMessage()).setClass(Resources.CLASS_LIST_TABLE_TD));
            }
            TD deleteTd;
            if (processError.getType() != ProcessErrorType.execution) {
                A a = new A("javascript:void(0);", "X");
                a.setOnClick("deleteProcessError(this, '" + processError.getType() + "', " + processId + ", '" + processError.getNodeId() + "')");
                deleteTd = new TD(a);
            } else {
                deleteTd = new TD();
                areFailedProcessesExist = true;
            }
            tr.addElement(deleteTd.setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }
        Div resultElement = new Div();
        StringBuilder filters = new StringBuilder();
        filters.append("<div class='processErrorsFilter' style='float: right;'>");
        for (ProcessErrorType type : ProcessErrorType.values()) {
            filters.append("<label><input type='checkbox' checked='true'>");
            filters.append(Messages.getMessage("errors.type." + type, pageContext));
            filters.append("</label>");
        }
        filters.append("</div>");
        resultElement.addElement(filters.toString());
        ErrorsHeaderBuilder headerBuilder = new ErrorsHeaderBuilder();
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        Table table = tableBuilder.build(headerBuilder, rowBuilder);
        resultElement.addElement(table);
        if (areFailedProcessesExist) {
            Div div = new Div();
            div.setStyle("float: right;");
            div.addElement(new A(Commons.getActionUrl(ActivateFailedProcessesAction.ACTION_PATH, pageContext, PortletUrlType.Render),
                    MessagesProcesses.ACTIVATE_FAILED_PROCESSES.message(pageContext)));
            resultElement.addElement(div);
        }
        return resultElement;
    }

    private class ErrorsHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(MessagesError.ERRORS_TYPE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesError.ERRORS_PROCESS_ID.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesError.ERRORS_OCCURED.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesError.ERRORS_NODE_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesError.ERRORS_ERROR.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH("").setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

}
