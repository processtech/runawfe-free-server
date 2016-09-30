package ru.runa.wf.web.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import freemarker.template.TemplateModelException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class AjaxActorsListFromGroup extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        String groupName = request.getParameter("group");
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(user, groupName, hint);
        if (actors.size() == 0) {
            jsonArray.add(createJsonObject(null, "", null, null));
        }
        for (Actor actor : actors) {
            jsonArray.add(createJsonObject(actor.getName(), actor.getFullName(), null, null));
        }
        return jsonArray;
    }

    private List<Actor> getActors(User user, String groupName, String hint) throws TemplateModelException {
        hint = hint.toLowerCase();
        if (groupName != null && groupName.length() > 0) {
            List<Actor> list = new ArrayList<Actor>();
            Group group = Delegates.getExecutorService().getExecutorByName(user, groupName);
            List<Actor> groupActors = Delegates.getExecutorService().getGroupActors(user, group);
            for (Actor actor : groupActors) {
                if (actor.getFullName().toLowerCase().startsWith(hint)) {
                    list.add(actor);
                }
            }
            Collections.sort(list);
            return list;
        } else {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = 1;
                batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS, true));
            }
            return (List<Actor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        }
    }

    private JSONObject createJsonObject(Object code, String name, String exclusion, String title) {
        JSONObject object = new JSONObject();
        object.put("code", code != null ? code : "");
        object.put("name", name);
        object.put("exclusion", exclusion != null ? exclusion : "");
        object.put("title", title != null ? title : "");
        return object;
    }
}
