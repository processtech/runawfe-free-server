package ru.runa.af.web.tag;

import java.util.Map;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import com.google.common.collect.Maps;

import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.tag.ListTasksFormTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.ObservableExecutorNameFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listObservableTasksForm")
public class ListObservableTasksFormTag extends ListTasksFormTag {
    private static final long serialVersionUID = 1L;
    private Long executorId;

    @Attribute(required = false, rtexprvalue = true)
    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
        if (executorId != 0) {
            Executor executor = Delegates.getExecutorService().getExecutor(getUser(), executorId);
            String presentationName = (executor instanceof Group ? MessagesOther.GROUP.message(pageContext) : MessagesOther.USER.message(pageContext))
                    + " " + executor.getName();
            BatchPresentation activePresentation = null;
            for (BatchPresentation presentation : getProfile().getBatchPresentations(getBatchPresentationId())) {
                if (presentationName.equals(presentation.getName())) {
                    activePresentation = presentation;
                    break;
                }
            }
            if (activePresentation == null) {
                activePresentation = BatchPresentationFactory.OBSERVABLE_TASKS.createDefault(getBatchPresentationId());
                activePresentation.setName(presentationName);
                FilterCriteria executorNameCriteria = new ObservableExecutorNameFilterCriteria();
                executorNameCriteria.applyFilterTemplates(new String[] { executor.getName() });
                Map<Integer, FilterCriteria> filterMap = Maps.newHashMap();
                filterMap.put(activePresentation.getAllFields().length - 1, executorNameCriteria);
                activePresentation.setFilteredFields(filterMap);
                Delegates.getProfileService().createBatchPresentation(getUser(), activePresentation);
            }
            Delegates.getProfileService().setActiveBatchPresentation(getUser(), getBatchPresentationId(), presentationName);
            ProfileHttpSessionHelper.reloadProfile(pageContext.getSession());

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

}
