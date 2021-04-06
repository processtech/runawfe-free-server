package ru.runa.af.web.tag;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;

public class ActorSelect extends Select {
    private static final long serialVersionUID = 1L;

    public ActorSelect(User user, String name, String current, boolean actorsOnly) {
        super(name);
        boolean exist = false;
        BatchPresentation batchPresentation;
        if (actorsOnly) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        } else {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        ArrayList<Option> options = new ArrayList<>();
        for (Executor executor : executors) {
            if (executor instanceof TemporaryGroup) {
                continue;
            }
            String label = executor.getName();
            if (executor instanceof Actor && !Strings.isNullOrEmpty(executor.getLabel())) {
                label += " (" + executor.getLabel() + ")";
            }
            boolean isCurrent = executor.getName().equals(current);
            if (isCurrent) {
                exist = true;
            }
            options.add(HTMLUtils.createOption(executor.getName(), label, isCurrent));
        }
        if (!exist && !Strings.isNullOrEmpty(current)) {
            options.add(HTMLUtils.createOption(current, true).setDisabled(true));
        }
        addElement(options.toArray(new Option[options.size()]));
    }
}
