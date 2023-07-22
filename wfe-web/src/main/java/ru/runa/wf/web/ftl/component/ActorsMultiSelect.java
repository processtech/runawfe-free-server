package ru.runa.wf.web.ftl.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxJsonFormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * @deprecated Use List<User> variable type + InputVariableTag.
 */
@SuppressWarnings("unchecked")
@Deprecated
public class ActorsMultiSelect extends AjaxJsonFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingVariableName = variable.getDefinition().getScriptingNameWithoutDots();
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("VARIABLE", variableName);
        substitutions.put("UNIQUENAME", scriptingVariableName);
        StringBuffer html = new StringBuffer();
        List<Object> actors = variableProvider.getValue(List.class, variableName);
        if (actors == null) {
            actors = Lists.newArrayList();
        }
        substitutions.put("START_COUNTER", String.valueOf(actors.size()));
        html.append(exportScript(substitutions, false));
        html.append("<div id=\"actorsMultiSelect_").append(scriptingVariableName).append("\">");
        html.append("<div id=\"actorsMultiSelectCnt_").append(scriptingVariableName).append("\">");
        for (int i = 0; i < actors.size(); i++) {
            String divId = "div_" + scriptingVariableName + i;
            String e = "<div id='" + divId + "'>";
            Actor actor = TypeConversionUtil.convertTo(Actor.class, actors.get(i));
            e += "<input type='hidden' name='" + variableName + "' value='ID" + actor.getId() + "' /> " + getDisplayName(actor);
            e += " <a href='javascript:{}' onclick='$(\"#" + divId + "\").remove();'>[ X ]</a>";
            e += "</div>";
            html.append(e);
        }
        html.append("</div>");
        html.append("<div id=\"actorsMultiSelectAddButton_").append(scriptingVariableName).append("\">");
        html.append("<a href=\"javascript:{}\" id=\"btnAdd_").append(scriptingVariableName).append("\">[ + ]</a>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    private String getDisplayName(Actor actor) {
        String displayFormat = getParameterAsString(1);
        return "login".equals(displayFormat) ? actor.getName() : actor.getFullName();
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        String displayFormat = getParameterAsString(1);
        Object groupIdentity = getRichComboParameterAs(Object.class, 2);
        Group group = TypeConversionUtil.convertToExecutor(groupIdentity, new DelegateExecutorLoader(user));
        boolean byLogin = "login".equals(displayFormat);
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(group, byLogin, hint);
        if (actors.size() == 0) {
            jsonArray.add(createJsonObject(null, ""));
        }
        for (Actor actor : actors) {
            jsonArray.add(createJsonObject(actor.getId(), byLogin ? actor.getName() : actor.getFullName()));
        }
        return jsonArray;
    }

    private JSONObject createJsonObject(Long id, String name) {
        JSONObject object = new JSONObject();
        object.put("id", id != null ? "ID" + id : "");
        object.put("name", name);
        return object;
    }

    private List<Actor> getActors(Group group, boolean byLogin, String hint) {
        int rangeSize = 50;
        List<Actor> actors = Lists.newArrayListWithExpectedSize(rangeSize);
        if (group != null) {
            List<Actor> groupActors = Delegates.getExecutorService().getGroupActors(user, group);
            for (Actor actor : groupActors) {
                if (byLogin) {
                    if (actor.getName().startsWith(hint)) {
                        actors.add(actor);
                    }
                } else {
                    if (actor.getFullName().startsWith(hint)) {
                        actors.add(actor);
                    }
                }
            }
            Collections.sort(actors);
            if (actors.size() > rangeSize) {
                return actors.subList(0, rangeSize);
            }
            return actors;
        } else {
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
            batchPresentation.setRangeSize(rangeSize);
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            if (hint.length() > 0) {
                int filterIndex = byLogin ? 0 : 1;
                batchPresentation.getFilteredFields().put(filterIndex, new StringFilterCriteria(hint + StringFilterCriteria.ANY_SYMBOLS));
            }
            actors.addAll((Collection<? extends Actor>) Delegates.getExecutorService().getExecutors(user, batchPresentation));
        }
        return actors;
    }

}
