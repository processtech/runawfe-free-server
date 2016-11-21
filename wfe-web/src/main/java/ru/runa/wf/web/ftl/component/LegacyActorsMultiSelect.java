package ru.runa.wf.web.ftl.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ftl.AjaxFormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @deprecated Use List<User> variable type + InputVariable.
 */
@Deprecated
public class LegacyActorsMultiSelect extends AjaxFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingVariableName = variable.getDefinition().getScriptingNameWithoutDots();
        Map<String, String> substitutions = Maps.newHashMap();
        substitutions.put("VARIABLE", variableName);
        substitutions.put("UNIQUENAME", scriptingVariableName);
        substitutions.put("START_COUNTER", "0");
        StringBuffer html = new StringBuffer();
        html.append(exportScript(substitutions, true));
        html.append("<div id=\"actorsMultiSelect").append(variableName).append("\"><div id=\"actorsMultiSelectCnt").append(variableName)
        .append("\"></div><div id=\"actorsMultiSelectAddButton\"><a href=\"javascript:{}\" id=\"btnAdd").append(variableName)
        .append("\">[ + ]</a></div></div>");
        return html.toString();
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String displayFormat = getParameterAsString(1);
        String resultFormat = getParameterAsString(2);
        String groupName = getParameterAsString(3);
        boolean byLogin = "login".equals(displayFormat);
        StringBuffer json = new StringBuffer("[");
        String hint = request.getParameter("hint");
        List<Actor> actors = getActors(user, groupName, byLogin, hint);
        if (actors.size() == 0) {
            json.append("{\"code\": \"\", \"name\": \"\"}");
        }
        for (Actor actor : actors) {
            if (json.length() > 10) {
                json.append(", ");
            }
            Object data;
            if ("login".equals(resultFormat)) {
                data = actor.getName();
            } else if ("email".equals(resultFormat)) {
                data = actor.getEmail();
            } else if ("fio".equals(resultFormat)) {
                data = actor.getFullName();
            } else {
                data = actor.getCode();
            }
            json.append("{\"code\": \"").append(data).append("\", \"name\": \"");
            if (byLogin) {
                json.append(actor.getName());
            } else {
                json.append(actor.getFullName());
            }
            json.append("\"}");
        }
        json.append("]");
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    private List<Actor> getActors(User user, String groupName, boolean byLogin, String hint) {
        int rangeSize = 50;
        List<Actor> actors = Lists.newArrayListWithExpectedSize(rangeSize);
        if (groupName != null && groupName.length() > 0) {
            Group group = Delegates.getExecutorService().getExecutorByName(user, groupName);
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
