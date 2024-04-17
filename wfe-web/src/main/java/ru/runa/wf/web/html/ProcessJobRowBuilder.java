package ru.runa.wf.web.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.job.dto.WfJob;

public class ProcessJobRowBuilder implements RowBuilder {
    private final List<WfJob> jobs;
    private int currentIndex = 0;
    private PageContext pageContext;

    public ProcessJobRowBuilder(List<WfJob> jobs, PageContext pageContext) {
        this.jobs = jobs;
        this.pageContext = pageContext;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < jobs.size();
    }

    @Override
    public TR buildNext() {
        TR tr = new TR();
        WfJob job = jobs.get(currentIndex++);

        Map<String, Object> params = new HashMap<>();
        params.put("jobId", job.getId());
        params.put("processId", job.getProcessId());
        String updateJobUrl = Commons.getActionUrl(WebResources.ACTION_MANAGE_JOB, params, pageContext, PortletUrlType.Render);
        A a = new A(updateJobUrl, job.getName());
        TD nameTD = new TD(a);
        tr.addElement(nameTD);
        nameTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD nodeIdTD = new TD(job.getNodeId());
        tr.addElement(nodeIdTD);
        nodeIdTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD createDateTD = new TD(CalendarUtil.formatDateTime(job.getCreateDate()));
        tr.addElement(createDateTD);
        createDateTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD dueDateExpressionTD = new TD(job.getDueDateExpression());
        tr.addElement(dueDateExpressionTD);
        dueDateExpressionTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD dueDateTD = new TD(CalendarUtil.formatDateTime(job.getDueDate()));
        tr.addElement(dueDateTD);
        dueDateTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        return tr;
    }

    public int getEnabledRowsCount() {
        return jobs.size();
    }

    @Override
    public List<TR> buildNextArray() {
        return new ArrayList<>();
    }
}
