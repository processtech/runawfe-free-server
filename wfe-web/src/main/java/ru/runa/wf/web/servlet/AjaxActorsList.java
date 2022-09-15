package ru.runa.wf.web.servlet;

import com.google.common.base.Strings;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;

public class AjaxActorsList extends JsonAjaxCommand {

    @SuppressWarnings("unchecked")
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject options = (JSONObject) jsonParser.parse(request.getReader());
            Long page = (Long) options.get("page");
            Long perPage = (Long) options.get("perPage");
            String hint = (String) options.get("hint");
            String target = (String) options.get("target");
            Boolean excludeme = (Boolean) options.get("excludeme");
            JSONObject root = new JSONObject();
            JSONArray data = new JSONArray();
            BatchPresentation batchPresentation = getPresentation(target, page.intValue() + 1, perPage.intValue(), hint);
            Integer count = Delegates.getExecutorService().getExecutorsCount(user, batchPresentation);
            Long totalPages = (count + perPage - 1) / perPage;
            root.put("count", count);
            root.put("totalPages", totalPages);
            root.put("page", page);
            List<? extends Executor> executors = Delegates.getExecutorService().getExecutors(user, batchPresentation);
            for (Executor executor : executors) {
                if (isExcluded(user, executor, excludeme)) {
                    continue;
                }
                Object obj = executorToJson(executor);
                if (obj != null) {
                    data.add(obj);
                }
            }
            root.put("data", data);
            return root;
        } catch (Exception e) {
            log.error("Bad request", e);
            throw new InternalApplicationException(e);
        }
    }

    private BatchPresentation getPresentation(String target, Integer page, Integer perPage, String hint) {
        BatchPresentationFactory factory;
        int filterIndex;
        if ("actor".equals(target)) {
            factory = BatchPresentationFactory.ACTORS;
            filterIndex = 1;
        } else {
            factory = BatchPresentationFactory.GROUPS;
            filterIndex = 0;
        }
        BatchPresentation batchPresentation = factory.createDefault();
        batchPresentation.setRangeSize(perPage);
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        if (!Strings.isNullOrEmpty(hint)) {
            batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS, true));
        }
        batchPresentation.setPageNumber(page);
        return batchPresentation;
    }

    @SuppressWarnings("unchecked")
    private JSONObject executorToJson(Executor executor) {
        JSONObject r = new JSONObject();
        r.put("id", executor.getId());
        r.put("name", executor.getName());
        if (executor instanceof Actor) {
            r.put("type", "actor");
            r.put("fullname", executor.getLabel());
        } else if (executor instanceof Group) {
            r.put("type", "group");
        }
        return r;
    }

    private boolean isExcluded(User user, Executor executor, Boolean excludeme) {
        if (executor.getName().startsWith(ru.runa.wfe.user.SystemExecutors.SYSTEM_EXECUTORS_PREFIX)) {
            return true;
        }
        if (executor instanceof TemporaryGroup) {
            return true;
        }
        if (Boolean.TRUE.equals(excludeme) && executor.getId().equals(user.getActor().getId())) {
            return true;
        }
        return false;
    }
}
