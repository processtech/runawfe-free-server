package ru.runa.wf.web.tag;

import java.util.List;
import java.util.Map;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ShowProcessErrorsTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }

    @Override
    protected ConcreteElement getEndElement() {
        List<TR> rows = Lists.newArrayList();
        for (Map.Entry<Long, List<ProcessError>> processEntry : ProcessExecutionErrors.getProcessErrors().entrySet()) {
            Map<String, Object> params = Maps.newHashMap();
            params.put(IdForm.ID_INPUT_NAME, processEntry.getKey());
            A processIdElement = new A(
                    Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render), processEntry
                            .getKey().toString());
            for (ProcessError detail : processEntry.getValue()) {
                TR tr = new TR();
                tr.addElement(new TD(processIdElement).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD(CalendarUtil.formatDateTime(detail.getOccurredDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD(detail.getTaskName()).setClass(Resources.CLASS_LIST_TABLE_TD));
                String url = "javascript:showProcessError(" + processEntry.getKey() + ", '" + detail.getNodeId() + "')";
                tr.addElement(new TD(new A(url, detail.getThrowableMessage())).setClass(Resources.CLASS_LIST_TABLE_TD));
                rows.add(tr);
            }
        }
        ErrorsHeaderBuilder tasksHistoryHeaderBuilder = new ErrorsHeaderBuilder();
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder);
    }

    private class ErrorsHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(Messages.getMessage("errors.process.id", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage("errors.occurred", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage("errors.task.name", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage("errors.error", pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

}
