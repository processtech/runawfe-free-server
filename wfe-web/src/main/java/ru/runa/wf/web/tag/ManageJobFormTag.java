package ru.runa.wf.web.tag;

import java.util.Optional;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.SaveJobAction;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "manageJobForm")
public class ManageJobFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    private static final String DATE_PICKER_INPUT_TYPE = "text";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String VALUE = "value";
    private static final String JOB_VALUE_INPUT_NAME = "dueDate";
    private static final String HIDDEN_TYPE = "hidden";
    private static final String JOB_ID = "jobId";
    private static final String PROCESS_ID = "processId";

    private Long jobId;
    private Long processId;

    @Attribute(required = true)
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    @Attribute(required = true)
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getProcessId() {
        return processId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {

        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);

        Optional<WfJob> jobOptional = Optional.ofNullable(Delegates.getExecutionService().getJob(jobId));

        if (jobOptional.isPresent()) {
            WfJob job = jobOptional.get();
            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_JOB_NAME.message(pageContext), new Span(job.getName())));

            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_JOB_NODE_ID.message(pageContext), new Span(job.getNodeId())));
            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_JOB_CREATION_DATE.message(pageContext),
                    new Span(CalendarUtil.formatDateTime(job.getCreateDate()))));
            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_JOB_DUE_DATE_EXPRESSION.message(pageContext),
                    new Span(job.getDueDateExpression())));

            Input jobValueInput = new Input();
            jobValueInput.addAttribute(NAME, JOB_VALUE_INPUT_NAME);
            jobValueInput.addAttribute(TYPE, DATE_PICKER_INPUT_TYPE);
            jobValueInput.setClass(Resources.CLASS_INPUT_DATE_TIME);
            jobValueInput.addAttribute(VALUE, CalendarUtil.formatDateTime(job.getDueDate()));
            table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_JOB_DUE_DATE.message(pageContext), jobValueInput));
            // hidden fields
            Input jobIdInput = HTMLUtils.createInput(HIDDEN_TYPE, JOB_ID, jobOptional.get().getId().toString());
            table.addElement(jobIdInput);
            Input processIdInput = HTMLUtils.createInput(HIDDEN_TYPE, PROCESS_ID, job.getProcessId().toString());
            table.addElement(processIdInput);

            tdFormElement.addElement(table);
        }
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_MANAGE_JOB.message(pageContext);
    }

    @Override
    public String getAction() {
        return SaveJobAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_SAVE_JOB.message(pageContext);
    }

    @Override
    protected boolean isCancelButtonEnabled() {
        return true;
    }

    @Override
    protected String getCancelButtonAction() {
        return "manage_process.do?id=" + getProcessId().toString();
    }

}
