package ru.runa.wf.web.tag;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ProcessTaskAssignmentAction;
import ru.runa.wf.web.html.AssignTaskCheckboxTdBuilder;
import ru.runa.wf.web.html.TaskUrlStrategy;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * Created on 15.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listTasksForm")
public class ListTasksFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = -6863052817853155919L;
    private static boolean isButtonEnabled;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfTask> tasks = getTasksList(batchPresentation);
        Table table = buildTasksTable(pageContext, batchPresentation, tasks, getReturnAction(), false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, tasks.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(table);
        navigation.addPagingNavigationTable(tdFormElement);

        if (!tasks.isEmpty()) {
            String tasksIds = Joiner.on(",").join(Lists.transform(tasks, new Function<WfTask, Long>() {

                @Override
                public Long apply(WfTask input) {
                    return input.getId();
                }
            }));
            pageContext.setAttribute("tasksIds", tasksIds, PageContext.REQUEST_SCOPE);
        }
    }

    public static Table buildTasksTable(PageContext pageContext, BatchPresentation batchPresentation, List<WfTask> tasks, String returnAction,
            boolean disableCheckbox) {
        isButtonEnabled = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isGroupAssigned()) {
                if (!disableCheckbox) {
                    isButtonEnabled = true;
                    break;
                }
            }
        }
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { new AssignTaskCheckboxTdBuilder(!disableCheckbox) },
                batchPresentation, null);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, returnAction, pageContext);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(tasks, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, returnAction, new TaskUrlStrategy(pageContext), builders);
        rowBuilder.setCssClassStrategy(new TasksCssClassStrategy());
        return new TableBuilder().build(headerBuilder, rowBuilder);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return isButtonEnabled;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_TASKS.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_ACCEPT_TASK.message(pageContext);
    }

    @Override
    public String getAction() {
        return ProcessTaskAssignmentAction.ACTION_PATH;
    }

    public static class TasksCssClassStrategy implements CssClassStrategy {
        private final Date currentDate = new Date();

        @Override
        public String getClassName(Object item, User user) {
            WfTask task = (WfTask) item;
            Date deadlineDate = task.getDeadlineDate();
            if (deadlineDate == null) {
                return null;
            }
            if (task.isEscalated()) {
                return "escalatedTask";
            }
            if (task.isDelegated()) {
                return "delegatedTask";
            }
            if (task.isAcquiredBySubstitution()) {
                return "substitutionTask";
            }
            if (deadlineDate.before(currentDate)) {
                return "deadlineExpired";
            }
            if (task.getDeadlineWarningDate() != null && task.getDeadlineWarningDate().before(currentDate)) {
                return "deadlineAlmostExpired";
            }
            return "deadlineExists";
        }

        @Override
        public String getCssStyle(Object item) {
            if (((WfTask) item).isFirstOpen()) {
                return "font-weight: bold;";
            }
            return null;
        }
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.ACCEPT_TASK_PARAMETER;
    }

    protected List<WfTask> getTasksList(BatchPresentation batchPresentation) {
        return Delegates.getTaskService().getMyTasks(getUser(), batchPresentation);
    }

}
