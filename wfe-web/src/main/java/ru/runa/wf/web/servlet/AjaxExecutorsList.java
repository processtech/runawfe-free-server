package ru.runa.wf.web.servlet;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorClassPresentation;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class AjaxExecutorsList extends JsonAjaxCommand {
    
    @RequiredArgsConstructor
    @Getter
    public static enum Type {
        actor(Actor.DISCRIMINATOR_VALUE),
        group(Group.DISCRIMINATOR_VALUE),
        executor(null);

        private final String filterValue;
        
    }

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        try {
            String hint = request.getParameter("hint");
            Type type = Type.valueOf(request.getParameter("type"));
            boolean includingTemporaryGroups = Boolean.valueOf(request.getParameter("includingTemporaryGroups"));
            List<Executor> executors = new ArrayList<>();
            switch (type) {
                case actor: {
                    executors.addAll(getExecutors(user, type, includingTemporaryGroups, hint));
                    break;
                }
                case group: {
                    executors.addAll(getExecutors(user, type, includingTemporaryGroups, hint));
                    break;
                }
                case executor: {
                    Set<Executor> executorSet = new HashSet<>();
                    executorSet.addAll(getExecutors(user, type, includingTemporaryGroups, hint));
                    executorSet.addAll(getExecutors(user, type, includingTemporaryGroups, hint));
                    executors.addAll(executorSet);
                    break;
                }
            }
            JSONObject root = new JSONObject();
            JSONArray data = new JSONArray();
            for (Executor executor : executors) {
                if (executor.getName().startsWith(ru.runa.wfe.user.SystemExecutors.SYSTEM_EXECUTORS_PREFIX)) {
                    continue;
                }
                JSONObject r = new JSONObject();
                r.put("id", executor.getId());
                r.put("value", executor.getName());
                r.put("label", executor.getFullName());
                data.add(r);
            }
            data.sort(new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    return ((String) o1.get("label")).compareTo((String) o2.get("label"));
                }

            });
            if (data.size() > 10) {
                data.removeIf((o) -> {
                    return data.indexOf(o) >= 10;
                });
            }
            root.put("data", data);
            return root;
        } catch (Exception e) {
            log.error("Bad request", e);
            throw new InternalApplicationException(e);
        }
    }

    private List<? extends Executor> getExecutors(User user, Type type, boolean includingTemporaryGroups, String hint) {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        batchPresentation.setRangeSize(20); // to omit system executors and executors with empty names
        if (type.getFilterValue() != null) {
            int typeFieldIndex = batchPresentation.getType().getFieldIndex(ExecutorClassPresentation.TYPE);
            batchPresentation.getFilteredFields().put(typeFieldIndex, new StringFilterCriteria(type.getFilterValue()));
        }
        int nameFieldIndex = batchPresentation.getType().getFieldIndex(ExecutorClassPresentation.FULL_NAME);
        if (!Strings.isNullOrEmpty(hint)) {
            batchPresentation.getFilteredFields().put(nameFieldIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS, true));
        }
        batchPresentation.setFieldsToSort(new int[] { nameFieldIndex }, new boolean[] { true });
        if (includingTemporaryGroups) {
            return Delegates.getExecutorService().getExecutors(user, batchPresentation);
        } else {
            return Delegates.getExecutorService().getNotTemporaryExecutors(user, batchPresentation);
        }
    }

}
