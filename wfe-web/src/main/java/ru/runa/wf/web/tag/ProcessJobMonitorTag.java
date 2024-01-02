package ru.runa.wf.web.tag;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.ProcessJobRowBuilder;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "processJobMonitor")
public class ProcessJobMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = -5024428545159087986L;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<WfJob> jobs = Delegates.getExecutionService().getProcessJobs(getUser(), getIdentifiableId(), true).stream()
                .filter(job -> job.getNodeType() == NodeType.TIMER).collect(Collectors.toList());
        List<String> headerNames = Lists.newArrayList();
        headerNames.add(MessagesProcesses.LABEL_JOB_NAME.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_JOB_NODE_ID.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_JOB_CREATION_DATE.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_JOB_DUE_DATE_EXPRESSION.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_JOB_DUE_DATE.message(pageContext));
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(headerNames);
        RowBuilder rowBuilder = new ProcessJobRowBuilder(jobs, pageContext);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_INSANCE_JOB_LIST.message(pageContext);
    }
}
