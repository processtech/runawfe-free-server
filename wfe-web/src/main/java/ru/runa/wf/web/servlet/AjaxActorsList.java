package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
    private User user;
    private Boolean excludeSelf;

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
        if (hint.length() > 0) {
            batchPresentation.getFilteredFields().put(filterIndex,
                    new StringFilterCriteria(StringFilterCriteria.ANY_SYMBOLS + hint + StringFilterCriteria.ANY_SYMBOLS));
        }
        batchPresentation.setPageNumber(page);
        return batchPresentation;
    }

    private List<? extends Executor> getExecutors(BatchPresentation batchPresentation) {
        return Delegates.getExecutorService().getExecutors(user, batchPresentation);
    }

    private Integer getExecutorsCount(BatchPresentation batchPresentation) {
        return Delegates.getExecutorService().getExecutorsCount(user, batchPresentation);
    }

    @SuppressWarnings("unchecked")
    private JSONObject executorToJson(Executor executor) {
        JSONObject r = new JSONObject();

        r.put("id", executor.getId());
        if (executor instanceof Actor) {
            r.put("type", "actor");
            r.put("fullname", executor.getFullName());
        } else if (executor instanceof Group) {
            r.put("type", "group");
            r.put("name", executor.getName());
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        this.user = user;

        JSONObject options;
        try {
            JSONParser jsonParser = new JSONParser();
            options = (JSONObject) jsonParser.parse(request.getReader());
        } catch (Exception e) {
            options = new JSONObject();
        }

        Long page = (Long) options.get("page");
        Long perPage = (Long) options.get("perPage");
        String hint = (String) options.get("hint");
        String target = (String) options.get("target");
        excludeSelf = (Boolean) options.get("excludeme");

        if (page == null) {
            page = Long.valueOf(0);
        }
        if (perPage == null) {
            perPage = Long.valueOf(20);
        }
        if (hint == null) {
            hint = "";
        }
        if (target == null) {
            target = "actor";
        }
        if (excludeSelf == null) {
            excludeSelf = false;
        }

        JSONObject root = new JSONObject();
        JSONArray data = new JSONArray();
        BatchPresentation presentation = getPresentation(target, page.intValue() + 1, perPage.intValue(), hint);

        Integer count = getExecutorsCount(presentation);
        Long totalPages = (count + perPage - 1) / perPage;

        root.put("count", count);
        root.put("totalPages", totalPages);
        root.put("page", page);
        for (Executor executor : getExecutors(presentation)) {
            if (isExcluded(executor)) {
                continue;
            }
            Object obj = executorToJson(executor);
            if (obj != null) {
                data.add(obj);
            }
        }
        root.put("data", data);

        return root;
    }

    private boolean isExcluded(Executor executor) {
        if (executor.getName().startsWith(ru.runa.wfe.user.SystemExecutors.SYSTEM_EXECUTORS_PREFIX)) {
            return true;
        }
        if (executor instanceof TemporaryGroup) {
            return true;
        }
        if (excludeSelf && executor.getId().equals(user.getActor().getId())) {
            return true;
        }
        return false;
    }
}
