package ru.runa.af.web.tag;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.B;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.tag.ListTasksFormTag;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskObservableClassPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskListBuilder;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listObservableTasksForm")
public class ListObservableTasksFormTag extends ListTasksFormTag {
    private static final long serialVersionUID = 1L;
    private Long executorId;

    @Attribute(required = false, rtexprvalue = true)
    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
        if (executorId != null) {
            Executor executor = Delegates.getExecutorService().getExecutor(getUser(), executorId);
            BatchPresentation batchPresentation = getBatchPresentation();
            int fieldIndex = batchPresentation.getClassPresentation().getFieldIndex(TaskObservableClassPresentation.TASK_OBSERVABLE_EXECUTOR);
            batchPresentation.getFilteredFields().put(fieldIndex, new StringFilterCriteria(executor.getName()));
        }
    }

    public Long getExecutorId() {
        return executorId;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_OBSERVABLE_TASKS.message(pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Actor actor = ProfileHttpSessionHelper.getProfile(pageContext.getSession()).getActor();
        try {
            // TODO move to service method
            Utils.getTransactionManager().begin();
            int fieldIndex = getBatchPresentation().getClassPresentation().getFieldIndex(TaskObservableClassPresentation.TASK_OBSERVABLE_EXECUTOR);
            FilterCriteria filterCriteria = getBatchPresentation().getFilteredFields().get(fieldIndex);
            String executorName = filterCriteria != null ? filterCriteria.getFilterTemplates()[0] : null;
            TaskListBuilder taskListBuilder = ApplicationContextFactory.getContext().getBean(TaskListBuilder.class);
            Set<Executor> executors = taskListBuilder.getObservableExecutors(actor, executorName);
            Utils.getTransactionManager().rollback();
            String title = MessagesProcesses.TITLE_OBSERVABLE_EXECUTORS.message(pageContext) + " (" + executors.size() + "):<br/>";
            int maxCount = 20;
            for (Executor executor : executors) {
                title += executor.getName() + "<br/>";
                if (--maxCount <= 0) {
                    title += "...<br/>";
                    break;
                }
            }
            B b = new B();
            b.addElement(Commons.getMessage("content.observable_tasks.help", pageContext));
            b.setTitle(title);
            tdFormElement.addElement(b);
        } catch (Exception e) {
            LogFactory.getLog(getClass()).error("Unable to build header", e);
        }
        super.fillFormElement(tdFormElement);
    }

    @Override
    protected List<WfTask> getTasksList(BatchPresentation batchPresentation) {
        return Delegates.getTaskService().getTasks(getUser(), batchPresentation);
    }
}
